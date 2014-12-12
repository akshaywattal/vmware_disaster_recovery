/**
 * WWWAuthInfo.java
 *
 * (C) Copyright IBM Corp. 2006, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author: Alexander Wolf-Reber, IBM, a.wolf-reber@de.ibm.com 
 * 
 * Change History
 * Flag       Date        Prog         Description
 * -------------------------------------------------------------------------------
 * 1536711    2006-08-15  lupusalex    NullPointerException causes client call to never return 
 * 1516242    2006-11-27  lupusalex    Support of OpenPegasus local authentication
 * 1604329    2006-12-18  lupusalex    Fix OpenPegasus auth module
 * 1627832    2007-01-08  lupuslaex    Incorrect retry behaviour on HTTP 401
 * 1892041    2008-02-13  blaschke-oss Basic/digest authentication problem for Japanese users
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */
package org.sblim.wbem.http;

import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.util.SessionProperties;

/**
 * Implements HTTP basic and digest authentication
 */
public class WwwAuthInfo extends AuthInfo {

	/**
	 * Default ctor.
	 */
	public WwwAuthInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		String _nc = Long.toHexString(iNc);
		
		if (iScheme.equalsIgnoreCase("Digest")){
		
			if (iRealm == null) { // support for some ICAT CIMOMs buggy Digest authentication
				try {
					MessageDigest messageDigest = null;
					messageDigest = MessageDigest.getInstance("MD5");

					messageDigest.update(getBytes((iCredentials!=null && iCredentials.getUserName()!=null) ? String.valueOf(iCredentials.getPassword()) : "null", "UTF-8"));
					String pass = HttpClient.convertToHexString(messageDigest.digest());
					return "Digest username="+((iCredentials!=null && iCredentials.getUserName()!=null) ? iCredentials.getUserName() : "null")+", response="+pass;
				} catch (NoSuchAlgorithmException e) {
					//TODO log
					e.printStackTrace();
				}
			}
		
			result.append(iScheme);
			result.append(" username=\"");
			result.append((iCredentials!=null && iCredentials.getUserName()!=null) ? iCredentials.getUserName() : "null");
			result.append("\"");
			if (iRealm != null) {
				result.append(", realm=\""); 
				result.append(iRealm);
				result.append("\"");
			}
			if (iNonce != null) {
				result.append(", nonce=\"");
				result.append(iNonce);
				result.append("\"");
			}
			result.append(", uri=\"");
			result.append(iUri);
			result.append("\", response=\"");
			result.append(iResponse);
			result.append("\"");
//			if (algorithm != null) {
//				result.append(", algorithm=");
//				result.append(algorithm);
//			}
			if (iCnonce != null) {
				result.append(", cnonce=\"");
				result.append(iCnonce);
				result.append("\"");
			}
			if (iOpaque != null) {
				result.append(", opaque=\"");
				result.append(iOpaque);
				result.append("\"");
			}
			if (iQop != null) {
				result.append(", qop=");
				result.append(iQop);
			}
			if (iNc > -1) {
				result.append(", nc=");
				result.append("00000000".substring(_nc.length()));
				result.append(_nc);
			}
		} else if (iScheme.equalsIgnoreCase("Basic")) {

			result.append("Basic ");

			StringBuffer tmp = new StringBuffer();
			tmp.append((iCredentials!=null && iCredentials.getUserName()!=null) ? iCredentials.getUserName() : "null");
			tmp.append(':');
			tmp.append((iCredentials!=null && iCredentials.getPassword()!=null) ? String.valueOf(iCredentials.getPassword()) : "null");
			
			result.append(BASE64Encoder.encode(getBytes(tmp.toString(), "UTF-8")));
		}
		return result.toString();
	}

	public static String[] split(String line) {
		Vector elem = new Vector();
		int start = 0; 
		int end;
		while ((end = line.indexOf(',')) >-1) {
			elem.add(line.substring(start, end));
			start = end+1;
		}
		if (end < line.length())
			elem.add(line.substring(start));
		String[] result = new String[elem.size()];
		elem.toArray(result);
		return result;
	}

	private static byte[] getBytes(String str, String encoding) {
		byte[] bytes ;
		try {
			bytes = str.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			bytes = str.getBytes();
		}
		return bytes;
	}

	/* (non-Javadoc)
	 * @see org.sblim.wbem.http.AuthInfo#updateAuthenticationInfo(org.sblim.wbem.http.Challenge, java.net.URI, java.lang.String)
	 */
	public void updateAuthenticationInfo( 
			Challenge challenge, String authenticate, URI url,
			String requestMethod) throws NoSuchAlgorithmException {

		setURI(url.getPath());
		HttpHeader params = challenge.getParams();

		iScheme = challenge.getScheme();

		if (!iScheme.equalsIgnoreCase("Digest")) {
			return;
		}

		iRealm = params.getField("realm");

		String algorithm = params.getField("algorithm");
		String opaque = params.getField("opaque");
		String nonce = params.getField("nonce");
		String qop = params.getField("qop");

		iAlgorithm = (algorithm != null) ? algorithm : iAlgorithm;
		iOpaque = (opaque != null) ? opaque : iOpaque;
		iNonce = (nonce != null) ? nonce : iNonce;
		iQop = (qop != null) ? qop : iQop;

		MessageDigest messageDigest = null;

		messageDigest = MessageDigest.getInstance("MD5");

		if (qop != null || ("md5-sess".equalsIgnoreCase(algorithm))) {
			long time = System.currentTimeMillis();
			byte[] b = new byte[8];

			b[0] = (byte) ((time >> 0) & 0xFF);
			b[1] = (byte) ((time >> 8) & 0xFF);
			b[2] = (byte) ((time >> 16) & 0xFF);
			b[3] = (byte) ((time >> 24) & 0xFF);
			b[4] = (byte) ((time >> 32) & 0xFF);
			b[5] = (byte) ((time >> 40) & 0xFF);
			b[6] = (byte) ((time >> 48) & 0xFF);
			b[7] = (byte) ((time >> 56) & 0xFF);

			messageDigest.reset();
			messageDigest.update(b);

			iCnonce = HttpClient.convertToHexString(messageDigest.digest());
		}
		if (qop != null) {
			String[] list_qop = split(qop);
			for (int i = 0; i < list_qop.length; i++) {
				if (list_qop[i].equalsIgnoreCase("auth-int")) {
					qop = "auth-int"; // this one has higher priority
					break;
				}
				if ((list_qop[i].equalsIgnoreCase("auth")))
					qop = "auth";
			}
			if (qop != null) {
				setQop(qop);
			}
		}
		String nc1 = params.getField("nc");
		long challengeNc = 1;
		if (nc1 != null) {
			try {
				challengeNc = Long.parseLong(nc1, 16);
			} catch (Exception e) {
				Logger logger = SessionProperties.getGlobalProperties()
						.getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING,
							"exception while parsing challenge NC", e);
				}
			}
			if (getNc() == challengeNc) {
				setNc(++challengeNc);
			}
		} else {
			setNc(challengeNc = 1);
		}

		messageDigest.reset();
		PasswordAuthentication credentials1 = getCredentials();
		iA1 = credentials1.getUserName() + ":" + getRealm() + ":"
				+ String.valueOf(credentials1.getPassword());
		// System.out.println("Base:"+A1);
		messageDigest.update(getBytes(iA1, "UTF-8"));

		if ("md5-sess".equalsIgnoreCase(algorithm)) {
			messageDigest.update(getBytes(
					(":" + getNonce() + ":" + getCnonce()), "UTF-8"));

		}

		byte[] digest1 = messageDigest.digest();
		String sessionKey = HttpClient.convertToHexString(digest1);

		// System.out.println("A1:"+sessionKey);
		String method = requestMethod;

		// if (con instanceof HttpURLConnection)
		// method = ((HttpURLConnection) con).getRequestMethod();

		String A2 = method + ":" + getURI();

		if ("auth-int".equalsIgnoreCase(qop)) {
			messageDigest.reset();
			// messageDigest.update(readFully(con)); //TODO
			messageDigest.update(new byte[] {}); // TODO this must be the
													// entity body, not the
													// message body
			A2 = A2 + ":"
					+ HttpClient.convertToHexString(messageDigest.digest());
		}
		messageDigest.reset();
		messageDigest.update(getBytes(A2, "UTF-8"));
		A2 = HttpClient.convertToHexString(messageDigest.digest());

		messageDigest.reset();
		if (qop == null) {
			messageDigest.update(getBytes((sessionKey + ":" + nonce
					+ ":" + A2), "UTF-8"));
		} else {
			String _nc = Long.toHexString(challengeNc);
			messageDigest.update(getBytes((sessionKey + ":" + nonce
					+ ":" + "00000000".substring(_nc.length()) + _nc + ":"
					+ getCnonce() + ":" + qop + ":" + A2), "UTF-8"));
		}
		iResponse = HttpClient
				.convertToHexString(messageDigest.digest());

		//TODO handle digest-required header	
	}

	/* (non-Javadoc)
	 * @see org.sblim.wbem.http.AuthInfo#getHeaderFieldName()
	 */
	public String getHeaderFieldName() {
		return "Authorization";
	}

	/* (non-Javadoc)
	 * @see org.sblim.wbem.http.AuthInfo#isSentOnFirstRequest()
	 */
	public boolean isSentOnFirstRequest() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sblim.wbem.http.AuthInfo#isKeptAlive()
	 */
	public boolean isKeptAlive() {
		return true;
	}
}
