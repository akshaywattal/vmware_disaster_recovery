/**
 * HttpClient.java
 *
 * (C) Copyright IBM Corp. 2005, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author Roberto Pineiro, IBM, roberto.pineiro@us.ibm.com  
 * @author Chung-hao Tan, IBM ,chungtan@us.ibm.com
 * 
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 *   13799    2004-12-07  thschaef     Fix chunking support 
 *   17620    2005-06-29  thschaef     eliminate ASCIIPrintStream1 in import statement
 *   17970    2005-08-11  pineiro5     Logon from z/OS not possible
 * 1353168    2005-11-24  fiuczy       Possible NullPointerExcection in HttpClient.streamFinished()
 * 1422316    2006-05-15  lupusalex    Disable delayed acknowledgement
 * 1486379    2006-05-29  lupusalex    CIM client retries twice when HTTP/1.1 401 is returned
 * 1498130    2006-05-31  lupusalex    Selection of xml parser on a per connection basis
 * 1516244    2006-07-10  ebak         GCJ support
 * 1536711    2006-08-15  lupusalex    NullPointerException causes client call to never return
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1565091    2006-10-17  ebak         ssl handshake exception
 * 1516242    2006-11-27  lupusalex    Support of OpenPegasus local authentication
 * 1604329    2006-12-18  lupusalex    Fix OpenPegasus auth module
 * 1620526    2007-01-08  lupusalex    Socket Leak in HTTPClient.getResponseCode()
 * 1627832    2007-01-08  lupuslaex    Incorrect retry behaviour on HTTP 401
 * 1637546    2007-01-27  lupusalex    CIMEnumerationImpl has faulty close function
 * 1647148    2007-01-29  lupusalex    HttpClient.resetSocket() doesn't set socket timeout
 * 1647159    2007-01-29  lupusalex    HttpClientPool runs out of HttpClients
 * 1649595    2007-02-01  lupusalex    No chunking requested
 * 1688270    2007-03-26  lupusalex    Disable chunking because of trailer issues
 * 1892041    2008-02-13  blaschke-oss Basic/digest authentication problem for Japanese users
 * 1931332    2008-04-01  blaschke-oss In HTTPClient need to get status before closing connection
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2372679    2008-12-01  blaschke-oss Add property to control synchronized SSL handshaking
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */
package org.sblim.wbem.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
//import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.sblim.wbem.http.io.ASCIIPrintStream;
import org.sblim.wbem.http.io.BoundedInputStream;
import org.sblim.wbem.http.io.ChunkedInputStream;
import org.sblim.wbem.http.io.KeepAliveInputStream;
import org.sblim.wbem.http.io.PersistentInputStream;
import org.sblim.wbem.util.SessionProperties;

public class HttpClient implements HandshakeCompletedListener {

	static class HostPortPair {

		String iHostport;

		public HostPortPair(URI url) {
			iHostport = url.getScheme().toLowerCase() + ':' + url.getHost().toLowerCase() + ':'
					+ url.getPort();
		}

		public boolean equals(Object o) {
			if (!(o instanceof HostPortPair)) return false;

			return iHostport.equals(((HostPortPair) o).iHostport);
		}

		public int hashCode() {
			return iHostport.hashCode();
		}

		public String toString() {
			return "HostPortPair=[+" + iHostport + "]";
		}
	}

	private class GetProperty implements PrivilegedAction {

		String iPropertyName;

		GetProperty(String propertyName) {
			iPropertyName = propertyName;
		}

		public Object run() {
			return System.getProperty(iPropertyName);
		}
	}

	private static final String CLASSNAME = "org.sblim.wbem.http.HttpClient";

	private static String iEncoding;

	static {
		try {
			iEncoding = (String) AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					return System.getProperty("file.encoding", "ISO8859_1");
				}
			});
			if (!isASCIISuperset(iEncoding)) iEncoding = "ISO8859_1";
		} catch (Exception exception) {
			iEncoding = "ISO8859_1";
		}
	}

	public static String convertToHexString(byte[] digest) {
		char hexDigit[] = "0123456789abcdef".toCharArray();

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < digest.length; i++) {
			int b = digest[i];
			buf.append(hexDigit[(b >> 4) & 0xF]);
			buf.append(hexDigit[(b) & 0xF]);
		}
		return buf.toString();
	}

	public static HttpClient getClient(URI url, HttpClientPool clientPool,
			AuthorizationHandler auth_handler) {
		
		return clientPool.retrieveAvailableConnectionFromPool(url, auth_handler);
	}

	protected static String dequote(String str) {
		int len = str.length();
		if (len > 1 && str.charAt(0) == '\"' && str.charAt(len - 1) == '\"') return str.substring(
				1, len - 1);
		return str;
	}

	protected static void handleRsp(String authInfo, AuthInfo prevAuthInfo) throws IOException {
		if (authInfo != null) {
			HttpHeader params = HttpHeader.parse(authInfo);

			String nonce = params.getField("nextnonce");
			if (nonce != null) {
				prevAuthInfo.setNonce(nonce);
				prevAuthInfo.setNc(0);
			} else {
				nonce = prevAuthInfo.getNonce();
			}
			String qop = params.getField("qop");
			if (qop != null) {
				if (!"auth".equalsIgnoreCase(qop) && !"auth-int".equalsIgnoreCase(qop)) {
					// TODO
					throw new IOException(
							"Authentication Digest with integrity check not supported");
				}
				byte[] rspauth;
				String rspauthStr = dequote(params.getField("rspauth"));
				if (rspauthStr != null) {
					rspauth = parseHex(rspauthStr);

					String cnonce = dequote(params.getField("cnonce"));
					if (cnonce != null && !cnonce.equals(prevAuthInfo.getCnonce())) { throw new IOException(
							"Digest authentication: Invalid nonce counter"); }
					String ncStr = params.getField("nc");
					if (ncStr != null) {
						try {
							long nc = Long.parseLong(ncStr, 16);
							if (nc != prevAuthInfo.getNc()) { throw new IOException(); }
						} catch (Exception e) {
							throw new IOException("Digest authentication: Invalid nonce counter");
						}
					}

					String HA1, HA2;
					MessageDigest md5;
					try {
						md5 = MessageDigest.getInstance("MD5");
						md5.reset();
						byte[] bytes = prevAuthInfo.getA1().getBytes("UTF-8");
						md5.update(bytes);
						HA1 = convertToHexString(md5.digest());
						if ("MD5-sess".equalsIgnoreCase(params.getField("algorithm"))) {
							md5.reset();
							md5.update((HA1 + ":" + nonce + ":" + cnonce).getBytes("UTF-8"));
							HA1 = convertToHexString(md5.digest());
						}

						HA2 = ":" + prevAuthInfo.getURI();
						if ("auth-int".equalsIgnoreCase(qop)) {
							md5.reset();
							md5.update(new byte[] {});
							HA2 += ":" + convertToHexString(md5.digest());
						}
						md5.reset();
						md5.update(HA2.getBytes("UTF-8"));
						HA2 = convertToHexString(md5.digest());

						md5.reset();
						md5.update((HA1 + ":" + nonce + ":" + ncStr + ":" + cnonce + ":" + qop
								+ ":" + HA2).getBytes("UTF-8"));
						String hsh = convertToHexString(md5.digest());
						byte[] hash = parseHex(hsh);

						if (!Arrays.equals(hash, rspauth)) throw new IOException(
								"Digest Authentication failed!");

					} catch (NoSuchAlgorithmException e1) {
						throw new IOException(
								"Unable to validate Authentication response: NoSuchAlgorithmException");
					}
				}
			} else {
				// TODO compute md5 of the entity-body
			}
		}
	}

	protected static byte[] parseHex(String hex) {
		byte[] value = new byte[hex.length() >> 1];
		int n = 0;
		for (int i = 0; i < value.length; i++) {
			value[i] = (byte) (0xff & Integer.parseInt(hex.substring(n, n + 1), 16));
			n += 2;
		}
		return value;
	}

	private static boolean isASCIISuperset(String charset) throws Exception {
		String asciiSuperSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_.!~*'();/?:@&=+$,";
		byte abyte0[] = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72,
				73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99,
				100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115,
				116, 117, 118, 119, 120, 121, 122, 45, 95, 46, 33, 126, 42, 39, 40, 41, 59, 47, 63,
				58, 64, 38, 61, 43, 36, 44 };
		byte convertedArray[] = asciiSuperSet.getBytes(charset);
		return Arrays.equals(convertedArray, abyte0);
	}

	private boolean iConnected = false;

	private HttpClientPool iHttpClientPool = new HttpClientPool();

	private AuthorizationHandler iAuth_handler;

	private Logger iLogger = SessionProperties.getGlobalProperties().getLogger();

	//private SSLSession iSession;

	private InputStream iIStream;

	private boolean iUseHttp11 = true;

	private boolean iKeepAlive = true;

	private HttpClientMethod iMethod;

	private OutputStream iOStream;

	private AuthInfo iPrevAuthInfo;

	private AuthInfo iPrevProxy;

	private HttpHeader iRequestHeaders = new HttpHeader();

	private String iRequestMethod = "POST";

	private boolean iReset = true;

	private HttpClientMethod iResponse;

	private HttpHeader iResponseHeaders = new HttpHeader();

	private InputStream iServerInput;

	private ByteArrayOutputStream iServerOutput;

	private Socket iSocket;

	private URI iUrl;

	private boolean iSynchronizedHandshake = SessionProperties.getGlobalProperties().getSynchronizedSslHandshake();

	public HttpClient(URI url, HttpClientPool clientPool, AuthorizationHandler auth_handler) {
		iUrl = url;
		iAuth_handler = auth_handler;
		iHttpClientPool = clientPool;
	}

	public void connect() throws IOException {
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, "connect()");
		}
		iReset = true;
		iResponse = null;
		iConnected = true;
		iServerOutput = null;
		resetSocket();
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.exiting(CLASSNAME, "connect()");
		}
	}

	public synchronized void disconnect() {
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, "disconnect()");
		}
		iConnected = false;
		if (iSocket != null) {
			try {
				iSocket.close();
			} catch (IOException e) {
				if (iLogger.isLoggable(Level.WARNING)) {
					iLogger.log(Level.WARNING, "Unexpected exception while closing the socket", e);
				}
			}
			iSocket = null;
			iServerInput = null;
			iReset = true;
			iResponse = null;
		}
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.exiting(CLASSNAME, "disconnect()");
		}
	}

	public void finalize() {
		String methodName = "finalize()";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName);
		}

		this.disconnect();

		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.exiting(CLASSNAME, methodName);
		}
	}

	public String getCipherSuite() {
		return null;
	}

	public synchronized String getHeaderField(int index) {
		if (index < 0) throw new IllegalArgumentException();
		if (index == 0) return iResponse.toString();

		Iterator iterator = iResponseHeaders.iterator();
		while (iterator.hasNext() && --index >= 0) {
			Map.Entry entry = (Map.Entry) iterator.next();
			if (index == 0) return entry.getValue().toString();
		}
		return null;
	}

	// public Certificate[] getLocalCertificates() {
	// return null;
	// }

	public synchronized String getHeaderField(String name) {
		return iResponseHeaders.getField(name);
	}

	public synchronized String getHeaderFieldKey(int index) {
		if (index < 0) throw new IllegalArgumentException();
		if (index == 0) return null;

		Iterator iterator = iResponseHeaders.iterator();
		while (iterator.hasNext() && --index >= 0) {
			Map.Entry entry = (Map.Entry) iterator.next();
			if (index == 0) return entry.getKey().toString();
		}
		return null;
	}

	public synchronized InputStream getInputStream() throws IOException {
		if (getResponseCode() < 500 && iResponse != null && iServerInput != null) return iServerInput;

		throw new IOException("Failed to open an input stream from server: HTTPResponse "
				+ getResponseCode());
	}

	public synchronized OutputStream getOutputStream() throws IOException {
		if (iServerOutput == null) {
			iServerOutput = new ByteArrayOutputStream();
		}
		return iServerOutput;
	}

	public String getRequestMethod() {
		return iRequestMethod;
	}

	public String getRequestProperty(String key) {
		return iRequestHeaders.getField(key);
	}

	public synchronized int getResponseCode() throws IOException {
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, "getResponseCode()");
		}
		Exception delayedException = null;
		if (iReset && iResponse == null) {
			boolean authFailed = false;
			int IoRetry = 1;
			int AuthentificationRetry = 1;
			do {
				if (iLogger.isLoggable(Level.FINER)) {
					iLogger.log(Level.FINER, "Attempting request.. retry counters:" + IoRetry +"/" + AuthentificationRetry);
				}
				resetSocket();
				iReset = false;
				try {
					ASCIIPrintStream out = (ASCIIPrintStream) iOStream;
					if (out == null) throw new IOException("could not open output stream");

					String file = iUrl.getPath();
					if (file == null || file.length() == 0) file = "/";
					String query = iUrl.getQuery();
					if (query != null) file = file + '?' + query;

					iMethod = new HttpClientMethod(iRequestMethod, iUrl.getPath(), 1, iUseHttp11 ? 1
							: 0);
					if (iLogger.isLoggable(Level.FINER)) {
						iLogger.log(Level.FINER, "HTTP Operation= " + iMethod);
					}

					iMethod.write(out);

					iRequestHeaders.addField("Host", iUrl.getHost());
					if (iServerOutput != null) iRequestHeaders.addField("Content-length", ""
							+ iServerOutput.size());
					else iRequestHeaders.addField("Content-length", "0");
//					iRequestHeaders.addField("TE", "chunked, trailers");
					iRequestHeaders.addField("Connection", "Keep-alive");

					if (iPrevAuthInfo == null) {
						AuthInfo authInfo = iAuth_handler.getAuthorizationInfo(0);
						if (authInfo.isSentOnFirstRequest()) {
							iRequestHeaders.addField(authInfo.getHeaderFieldName(), authInfo
									.toString());
						}
					} else {
						iRequestHeaders.addField(iPrevAuthInfo.getHeaderFieldName(), iPrevAuthInfo
								.toString());
					}
										
					if (iPrevProxy != null) iRequestHeaders.addField("Proxy-authorization",
							iPrevProxy.toString());

					iRequestHeaders.write(out);
					if (iLogger.isLoggable(Level.FINER)) {
						iLogger.log(Level.FINER, "HTTP Headers= " + iRequestHeaders);
					}

					if (out.checkError() != null) {
						delayedException = out.checkError();
						if (iLogger.isLoggable(Level.FINER)) {
							iLogger.log(Level.FINER,
									"Got exception while writting to the output stream",
									delayedException);
						}
						if (iSocket!=null && !iSocket.isClosed()) {
							try {
								iSocket.close();
							} catch (IOException e) {
								iLogger.log(Level.FINER,
										"Got exception while closing socket",
										e);
							}
						}
						iSocket = null;
						iReset = true;
						continue;
					}
					if (iServerOutput != null) {
						iServerOutput.writeTo(out);
					}
					out.flush();

					// byte[] header = new byte[8];
					// istream.mark(8);
					// int totalRead;
					// int k;
					// for (totalRead = 0; totalRead < 8; totalRead += k) {
					// k = istream.read(header, totalRead, 8 - totalRead);
					// if (k < 0)
					// break;
					// }
					//				
					// if (header[0] != 72 // HTTP1.
					// || header[1] != 84
					// || header[2] != 84
					// || header[3] != 80
					// || header[4] != 47
					// || header[5] != 49
					// || header[6] != 46
					// || totalRead != 8) {
					// retry = 0;
					// throw new IOException("Unexpected end of file from
					// server. Header does not match HTTP header: "+new
					// String(header));
					// }
					//						
					// istream.reset();

					if (iLogger.isLoggable(Level.FINER)) {
						iLogger.log(Level.FINER, "Retrieving response...");
					}
					iResponse = new HttpClientMethod(iIStream);
					if (iLogger.isLoggable(Level.FINER)) {
						iLogger.log(Level.FINER, "HTTP Response= " + iResponse);
					}
					iResponseHeaders = new HttpHeader(iIStream);
					iKeepAlive = false;
					if ("Keep-alive".equalsIgnoreCase(iResponseHeaders.getField("Connection"))
							|| (iResponse.getMajorVersion() == 1 && iResponse.getMinorVersion() == 1)
							|| iResponseHeaders.getField("Keep-alive") != null) {
						iKeepAlive = true;
					}

					iServerInput = new PersistentInputStream(iIStream);
//					String keepAliveHdr = iResponseHeaders.getField("Keep-Alive");

					String contentLength = iResponseHeaders.getField("Content-length");
					long length = -1;
					try {
						if (contentLength != null && contentLength.length() > 0) length = Long
								.parseLong(contentLength);
					} catch (Exception e) {
						if (iLogger.isLoggable(Level.WARNING)) {
							iLogger
									.log(
											Level.WARNING,
											"exception while parsing the content length of the response",
											e);
						}
					}
					iKeepAlive = (length >= 0 || iResponse.getStatus() == 304 || iResponse
							.getStatus() == 204);

					String transferEncoding = iResponseHeaders.getField("Transfer-encoding");

					if (transferEncoding != null
							&& transferEncoding.toLowerCase().endsWith("chunked")) {
						iServerInput = new ChunkedInputStream(iServerInput);
						iKeepAlive = true;
					}
					iServerInput = new BoundedInputStream(iServerInput, length);

					if (iLogger.isLoggable(Level.FINER)) {
						iLogger.log(Level.FINER, "KeepAlive", new Boolean(iKeepAlive));
					}

					if (iKeepAlive) {
						iServerInput = new KeepAliveInputStream(iServerInput, this);
					}

					switch (iResponse.getStatus()) {
						case 100: {
							continue;
						}
						case HttpURLConnection.HTTP_OK:
							String authInfo = iResponseHeaders.getField("Authentication-Info");
							handleRsp(authInfo, iPrevAuthInfo);

							authInfo = iResponseHeaders.getField("Authentication-Proxy");
							handleRsp(authInfo, iPrevProxy);

							if (iServerOutput != null) iServerOutput = null;

							return HttpURLConnection.HTTP_OK;

						case HttpURLConnection.HTTP_UNAUTHORIZED:
							--AuthentificationRetry;
							String authenticate = iResponseHeaders.getField("WWW-Authenticate");
							try {
								iPrevAuthInfo = getAuthentication(false, iPrevAuthInfo,
										authenticate);
								if (iPrevAuthInfo != null) {
									iRequestHeaders.addField(iPrevAuthInfo.getHeaderFieldName(),
											iPrevAuthInfo.toString());
								}
							} catch (NoSuchAlgorithmException e) {
								if (iLogger.isLoggable(Level.SEVERE)) {
									iLogger.log(Level.SEVERE,
											"error unable to find digest algorithm", e);
								}
							}

							if (!authFailed) {
								authFailed = true;
								if (iLogger.isLoggable(Level.INFO)) {
									iLogger
											.log(Level.INFO,
													"Authorization failed, retring with authorization info..");
								}
							}
							if (iPrevAuthInfo.isKeptAlive()) {
								iKeepAlive = true;
							}
							break;
						case HttpURLConnection.HTTP_PROXY_AUTH:
							if (iLogger.isLoggable(Level.SEVERE)) {
								iLogger.log(Level.SEVERE,
										"Proxy authentication required, but not supported");
							}
							// TODO
							/*
							 * authenticate =
							 * responseHeaders.getField("Proxy-Authenticate");
							 * prevProxy = getAuthentication(true, prevProxy,
							 * authenticate, this); if (prevAuthInfo != null)
							 * requestHeaders.addField("Proxy-Authorization",
							 * prevProxy.toString());
							 * 
							 * while ((total = serverInput.available()) > 0) {
							 * serverInput.skip(total); } break;
							 */
							break;
						default:
							int status = iResponse.getStatus();
							if (!iKeepAlive) closeConnection();
							else iServerInput.close();
							return status;
					}
				} catch (SocketTimeoutException e) {
					throw e;
				} catch (IOException e) {
					if (iLogger.isLoggable(Level.WARNING)) {
						iLogger.log(Level.WARNING, "exception while connection to server", e);
					}
					delayedException = e;
					if (iSocket!=null && !iSocket.isClosed()) {
						try {
							iSocket.close();
						} catch (IOException e2) {
							iLogger.log(Level.FINER,
									"exception while closing socket",
									e2);
						}
					}
					iSocket = null;
					iReset = true;
					--IoRetry;
				}
			} while (AuthentificationRetry >= 0 && IoRetry >= 0);
		}

		if (iResponse != null) {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, "getResponseCode()", new Integer(iResponse.getStatus()));
			}
			return iResponse.getStatus();
		}

		throw (IOException) delayedException;
	}

	public String getResponseMessage() {
		if (iResponse != null) return iResponse.getResponseMessage();
		return null;
	}

	public void handshakeCompleted(HandshakeCompletedEvent event) {
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.log(Level.FINER, "handshake completed... getting session");
		}
		//iSession = event.getSession();
	}

	public void reset() {
		iRequestHeaders.clear();
		iResponseHeaders.clear();
		iResponse = null;
		iReset = true;
	}

	public void setRequestMethod(String method) {
		iRequestMethod = method;
	}

	public void setRequestProperty(String key, String value) {
		iRequestHeaders.addField(key, value);
	}

	public void streamFinished() {
		streamFinished(true);
	}

	public void streamFinished(boolean keep) {
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, "streamFinished(boolean)", new Boolean(keep));
		}
		HostPortPair hpp = new HostPortPair(iUrl);
		if (keep) { // TODO configurable from property file
			iHttpClientPool.returnAvailableConnectionToPool(this);
		} else {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.log(Level.FINER, "[disconnecting:"
						+ hpp + "]");
			}
			iHttpClientPool.removeConnectionFromPool(this);
			disconnect();
		}
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.exiting(CLASSNAME, "streamFinished(boolean)");
		}

	}

	public void useHttp11(boolean bool) {
		iUseHttp11 = bool;
	}

	public boolean usingProxy() {
		// TODO Auto-generated method stub
		return false;
	}

	protected AuthInfo getAuthentication(boolean proxy, AuthInfo prevAuthInfo, String authenticate)
			throws HttpParseException, NoSuchAlgorithmException {
		Challenge[] challenges = Challenge.parseChallenge(authenticate);

		// AuthorizationHandler auth_handler =
		// AuthorizationHandler.getInstance();
		int cntr = 0;
		prevAuthInfo = null;
		while (prevAuthInfo == null && cntr < challenges.length) {
			Challenge challenge = challenges[cntr];
			cntr++;
			// if (challenge.getScheme().equalsIgnoreCase("Digest")) {
			// HttpHeader headers = challenge.getParams();
			// String stale = headers.getField("stale");
			// }
			prevAuthInfo = iAuth_handler.getAuthorizationInfo(iHttpClientPool.getSessionProperties()
					.getHttpAuthenticationModule(), proxy ? Boolean.TRUE : Boolean.FALSE, iUrl
					.getHost(), iUrl.getPort(), iUrl.getScheme(), challenge.getRealm(), challenge
					.getScheme());

			if (prevAuthInfo != null) {
				prevAuthInfo.updateAuthenticationInfo(challenge, authenticate, iUrl, iRequestMethod);
				return prevAuthInfo;
			}
		}
		return null;
	}

	private void closeConnection() throws IOException {
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, "closeConnection()");
		}
		if (iSocket != null) {
			try {
				iSocket.close();
			} catch (IOException e) {
				if (iLogger.isLoggable(Level.WARNING)) {
					iLogger.log(Level.WARNING, "Unexpected exception while closing the socket", e);
				}
			}
			iSocket = null;
			iServerInput = null;
			// response = null;
		}
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.exiting(CLASSNAME, "closeConnection()");
		}
	}

	private String[] parseProperty(String propertyName) {
		String s = (String) AccessController.doPrivileged(new GetProperty(propertyName));
		String as[];
		if (s == null || s.length() == 0) {
			as = null;
		} else {
			Vector vector = new Vector();
			for (StringTokenizer stringtokenizer = new StringTokenizer(s, ","); stringtokenizer
					.hasMoreElements(); vector.addElement(stringtokenizer.nextElement()))
				;
			as = new String[vector.size()];
			for (int i1 = 0; i1 < as.length; i1++)
				as[i1] = (String) vector.elementAt(i1);
		}
		return as;
	}

	private void resetSocket() throws IOException {
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, "resetSocket()");
		}
		if (!iKeepAlive) {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.log(Level.FINER, "KeepAlive=false, closing connection...");
			}
			closeConnection();
		}

		if (iSocket == null
		// || socket.isClosed() // java1.4
		) {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.log(Level.FINER, "Socket=null, creating socket...");
			}
			SecurityManager sm = System.getSecurityManager();
			if (sm != null) {
				if (iLogger.isLoggable(Level.FINER)) {
					iLogger.log(Level.FINER, "Checking for permisions to connect to:"
							+ iUrl.getHost() + ":" + iUrl.getPort());
				}
				sm.checkConnect(iUrl.getHost(), iUrl.getPort());
			}

			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.log(Level.FINER, "Retrieving socket factory for scheme:" + iUrl.getScheme());
			}
			SocketFactory factory = HttpSocketFactory.getInstance().getSocketFactory(
					iUrl.getScheme(), iHttpClientPool.getSessionProperties());
			if (factory == null) { throw new IllegalStateException(
					"Unable to load socket socket factory:" + iUrl.getScheme()); }
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.log(Level.FINER, "creating new socket connecting to: " + iUrl.getHost()
						+ ":" + iUrl.getPort());
			}
			iSocket = factory.createSocket(iUrl.getHost(), iUrl.getPort());
			iSocket.setTcpNoDelay(true);
			iSocket.setKeepAlive(true);
			if (iSocket instanceof SSLSocket) {
				if (iLogger.isLoggable(Level.FINER)) {
					iLogger.log(Level.FINER, "Got instance of SSLSocket...");
				}
				SSLSocket sk = (SSLSocket) iSocket;

				String protocols[] = parseProperty("https.protocols");
				if (protocols != null) {
					if (iLogger.isLoggable(Level.FINER)) {
						iLogger.log(Level.FINER,
								"Setting SSLSocket.setEnabledProtocols() from \"https.protocols\"="
										+ new Vector(Arrays.asList(protocols)));
					}
					sk.setEnabledProtocols(protocols);
				}

				String ciphersuites[] = parseProperty("https.cipherSuites");
				if (ciphersuites != null) {
					if (iLogger.isLoggable(Level.FINER)) {
						iLogger.log(Level.FINER,
								"Setting SSLSocket.setEnableCipheSuites() from \"httpscipherSuites\"="
										+ new Vector(Arrays.asList(ciphersuites)));
					}
					sk.setEnabledCipherSuites(ciphersuites);
				}
				if (iLogger.isLoggable(Level.FINER)) {
					iLogger.log(Level.FINER, "Starting handshake (synchronized = " + iSynchronizedHandshake + ")...");
				}
				sk.addHandshakeCompletedListener(this);
				if (iSynchronizedHandshake) {
					synchronized (SSLSocket.class) {
						sk.startHandshake();
					}
				} else {
					sk.startHandshake();
				}
				// verifyHost(sock, )
			}
			// socket.setTcpNoDelay(true);
			int timeout = iHttpClientPool.getSessionProperties().getHttpTimeOut();
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.log(Level.FINER, "setting Socket.setSoTimeout(" + timeout + ")");
			}

			iSocket.setSoTimeout(timeout); // BB fixed - two minutes (120000
			// ms) are not enought for CbC
			// istream = new PushbackInputStream(new
			// BufferedInputStream(socket.getInputStream()));
			iIStream = new BufferedInputStream(iSocket.getInputStream());

			// ostream = socket.getOutputStream();

			iOStream = new ASCIIPrintStream(new BufferedOutputStream(iSocket.getOutputStream(),
					1024), false, iEncoding);
			iServerInput = null;
		} else {
			if (iServerInput != null && !(iServerInput instanceof KeepAliveInputStream)) {
				if (iLogger.isLoggable(Level.FINER)) {
					iLogger.log(Level.FINER, "Socket!=null, flushing the stream...");
				}
				long totalBytes = 0;
				long total;
				while ((total = iServerInput.available()) > 0) {
					iServerInput.skip(total);
					totalBytes += total;
				}
				if (iLogger.isLoggable(Level.FINER)) {
					iLogger.log(Level.FINER, "total bytes on the stream=" + totalBytes);
				}
			}
			int timeout = iHttpClientPool.getSessionProperties().getHttpTimeOut();
			iSocket.setSoTimeout(timeout);
		}
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.exiting(CLASSNAME, "resetSocket()");
		}

	}

	
	/**
	 * Returns connected
	 *
	 * @return The value of connected.
	 */
	public boolean isConnected() {
		return iConnected;
	}
}
