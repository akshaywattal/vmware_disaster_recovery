/**
 * HttpUrlConnection.java
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
 * @author: Roberto Pineiro, IBM, roberto.pineiro@us.ibm.com  
 * @author: Chung-hao Tan, IBM ,chungtan@us.ibm.com
 * 
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1353168    2005-11-24  fiuczy       Possible NullPointerExcection in HttpClient.streamFinished()
 * 1488924    2006/05/15  lupusalex    Intermittent connection loss
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1646434    2007-01-28  lupusalex    CIMClient close() invalidates all it's enumerations
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */
package org.sblim.wbem.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketPermission;
import java.net.URI;
import java.security.Permission;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.util.SessionProperties;

/**
 * @author Roberto
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HttpUrlConnection extends HttpURLConnection {

	private static final String CLASSNAME = "org.sblim.wbem.http.HttpUrlConnection";

	private InputStream iInputStream;

	private boolean iConnected;

	public HttpClient iHttpClient;

	protected URI iUrl;

	private HttpClientPool iHttpClientPool;

	AuthorizationHandler iAuthHandler;

	private Logger iLogger = null;

	public HttpUrlConnection(URI uri, HttpClientPool httpClientPool,
			AuthorizationHandler auth_handler) {
		super(null);
		this.iUrl = uri;
		this.iHttpClientPool = httpClientPool;
		this.iAuthHandler = auth_handler;
		iLogger = SessionProperties.getGlobalProperties().getLogger();
	}

	public String toString() {
		return "HttpUrlConnection=[url=" + iUrl + ",PoolSize="
				+ iHttpClientPool.getNumberOfAvailableConnections() + "," + iAuthHandler + "]";
	}

	public Permission getPermission() throws IOException {
		int port = iUrl.getPort();
		port = port < 0 ? 80 : port;
		String host = iUrl.getHost() + ":" + port;
		Permission permission = new SocketPermission(host, "connect");
		return permission;
	}

	public synchronized void connect() throws IOException {
		if (!iConnected) getClient();
		iHttpClient.connect();
	}

	private synchronized void getClient() {
		if (iConnected) return;

		iHttpClient = HttpClient.getClient(iUrl, iHttpClientPool, iAuthHandler);
		iHttpClient.reset();
		iConnected = true;
	}

	public synchronized void setRequestMethod(String pMethod) {
		if (!iConnected) getClient();
		iHttpClient.setRequestMethod(pMethod);
		method = pMethod;
	}

	public synchronized void setRequestProperty(String key, String value) {
		if (!iConnected) getClient();
		iHttpClient.setRequestProperty(key, value);
	}

	public synchronized void disconnect() {
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, "disconnect()");
		}
		if (iConnected) {
			iConnected = false;
			iHttpClient.disconnect();
			if (iHttpClientPool != null) {
				iHttpClientPool.removeConnectionFromPool(iHttpClient);
				iHttpClient = null;
			}
		}
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.exiting(CLASSNAME, "disconnect()");
		}
	}

	public synchronized void close(boolean pKeepActive) {
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, "close()");
		}
		// disconnect();
		if (iHttpClientPool != null) {
			synchronized (iHttpClientPool) {
				iHttpClientPool.closePool(pKeepActive);
				iHttpClientPool = null;
			}
		}
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.exiting(CLASSNAME, "close()");
		}
	}

	public synchronized InputStream getInputStream() throws IOException {
		if (iInputStream != null) return iInputStream;

		if (!iConnected) getClient();
		try {
			return iHttpClient.getInputStream();
		} catch (RuntimeException e) {
			disconnect();
			throw e;
		} catch (IOException e) {
			disconnect();
			throw e;
		}
	}

	public synchronized OutputStream getOutputStream() throws IOException {
		if (!iConnected) getClient();
		try {
			return iHttpClient.getOutputStream();
		} catch (RuntimeException e) {
			disconnect();
			throw e;
		} catch (IOException e) {
			disconnect();
			throw e;
		}
	}

	public boolean usingProxy() {
		return false;
	}

	public synchronized HttpClient getHttpClient() {
		if (!iConnected) getClient();
		return iHttpClient;
	}

	public synchronized String getHeaderField(String name) {
		if (!iConnected) getClient();
		return iHttpClient.getHeaderField(name);
	}

	public synchronized String getHeaderFieldKey(int index) {
		if (!iConnected) getClient();
		return iHttpClient.getHeaderFieldKey(index);
	}

	public synchronized String getHeaderField(int index) {
		if (!iConnected) getClient();
		return iHttpClient.getHeaderField(index);
	}

	public synchronized String getRequestProperty(String key) {
		if (!iConnected) getClient();
		return iHttpClient.getRequestProperty(key);
	}

	public synchronized String getRequestMethod() {
		if (!iConnected) getClient();
		return iHttpClient.getRequestMethod();
	}

	public synchronized void reset() {
		if (!iConnected) getClient();
		iHttpClient.reset();
	}

	public synchronized int getResponseCode() throws IOException {
		if (!iConnected) getClient();
		return iHttpClient.getResponseCode();
	}

	public synchronized String getResponseMessage() {
		if (!iConnected) getClient();
		return iHttpClient.getResponseMessage();
	}

	public synchronized void useHttp11(boolean bool) {
		if (!iConnected) getClient();
		iHttpClient.useHttp11(bool);
	}
}
