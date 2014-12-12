/**
 * AuthInfo.java
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
 *   17970    2005-08-11  pineiro5     Logon from z/OS not possible
 * 1516242    2006-07-05  lupusalex    Support of OpenPegasus local authentication
 * 1604329    2006-12-18  lupusalex    Fix OpenPegasus auth module
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.http;

import java.net.PasswordAuthentication;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

import org.sblim.wbem.util.SessionProperties;

/**
 * Abstract superclass for HTTP authorization information.
 * 
 * @see org.sblim.wbem.http.WwwAuthInfo
 * @see org.sblim.wbem.http.PegasusLocalAuthInfo
 */
public abstract class AuthInfo {

	protected String iAddr;

	protected int iPort;

	protected String iProtocol;

	protected String iRealm;

	protected String iScheme;

	protected PasswordAuthentication iCredentials;

	protected long iNc = 1;

	protected String iCnonce;

	protected String iOpaque;

	protected String iAlgorithm;

	protected String iUri;

	protected String iNonce;

	protected String iQop;

	protected String iA1;

	protected String iResponse;

	// protected byte[] iDigest;

	protected AuthInfo() {}

	public void init(Boolean proxy, String addr, int port, String protocol, String realm,
			String scheme) {
		iAddr = addr;
		iPort = port;
		iProtocol = protocol;
		iRealm = realm;
		iScheme = scheme;
	}

	public void setOpaque(String opaque) {
		iOpaque = opaque;
	}

	public String getOpaque() {
		return iOpaque;
	}

	public String getQop() {
		return iQop;
	}

	public void setQop(String qop) {
		iQop = qop;
	}

	public long getNc() {
		return iNc;
	}

	public void setNc(long nc) {
		iNc = nc;
	}

	public void setNonce(String nonce) {
		iNonce = nonce;
	}

	public String getNonce() {
		return iNonce;
	}

	public void setCnonce(String cnonce) {
		iCnonce = cnonce;
	}

	public String getCnonce() {
		return iCnonce;
	}

	public void setAlgorithm(String algorithm) {
		iAlgorithm = algorithm;
	}

	public String getAlgorith() {
		return iAlgorithm;
	}

	public String getA1() {
		return iA1;
	}

	public void setA1(String A1) {
		iA1 = A1;
	}

	public void setResponse(String response) {
		iResponse = response;
	}

	public String getResponse() {
		return iResponse;
	}

	public String getURI() {
		return iUri;
	}

	public void setURI(String uri) {
		iUri = uri;
	}

	public void setCredentials(PasswordAuthentication credentials) {
		iCredentials = credentials;
	}

	public String getAddr() {
		return iAddr;
	}

	public int getPort() {
		return iPort;
	}

	public String getProtocol() {
		return iProtocol;
	}

	public String getRealm() {
		return iRealm;
	}

	public void setRealm(String realm) {
		iRealm = realm;
	}

	public String getScheme() {
		return iScheme;
	}

	public void setScheme(String scheme) {
		iScheme = scheme;
	}

	public PasswordAuthentication getCredentials() {
		return iCredentials;
	}

	/**
	 * Compares two authorization informations.
	 * 
	 * @param obj
	 *            The other authorization information
	 * @return <code>true</code> if type, realm, scheme, address, protocol and
	 *         port of both authorization informations are equal,
	 *         <code>false</code> otherwise.
	 */
	public boolean match(Object obj) {
		if (obj == null || !(obj instanceof AuthInfo)) return false;
		AuthInfo that = (AuthInfo) obj;

		boolean type = getClass().equals(that.getClass());
		// boolean prxt = (iProxy == null || that.iProxy == null)?
		// true:iProxy.equals(that.iProxy);
		boolean prmpt = (iRealm == null || that.iRealm == null) ? true : iRealm.equals(that.iRealm);
		boolean schm = (iScheme == null || that.iScheme == null) ? true : iScheme
				.equals(that.iScheme);
		boolean adr = (iAddr == null || that.iAddr == null) ? true : iAddr.equals(that.iAddr);
		boolean prot = (iProtocol == null || that.iProtocol == null) ? true : iProtocol
				.equals(that.iProtocol);
		boolean prt = (iPort <= 0 || that.iPort <= 0) ? true : (iPort == that.iPort);
		return (type && prmpt && schm && adr && prot && prt);
	}

	/**
	 * Updates the authorization information acording to a received challenge.
	 * 
	 * @param challenge
	 *            The received challenge
	 * @param authenticate
	 *            The authenticate header field
	 * @param url
	 *            The url of the CIM server
	 * @param requestMethod
	 *            The HTTP request method (POST or MPOST)
	 * @throws NoSuchAlgorithmException
	 */
	public abstract void updateAuthenticationInfo(Challenge challenge, String authenticate,
			URI url, String requestMethod) throws NoSuchAlgorithmException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public abstract String toString();

	/**
	 * Gets the HTTP header field name for this authentication information
	 * 
	 * @return The field name
	 */
	public abstract String getHeaderFieldName();

	/**
	 * Determines if the authorization information is already sent on the very
	 * first http request or after the "401 Unauthorized" response
	 * 
	 * @return <code>true</code> or <code>false</code>
	 */
	public abstract boolean isSentOnFirstRequest();

	/**
	 * Determines if the connection is kept alive after the "401 Unauthorized" response
	 * 
	 * @return <code>true</code> or <code>false</code>
	 */
	public abstract boolean isKeptAlive();

	/**
	 * Factory method for AuthInfo objects. Returns an instance of a subclass
	 * according to the requested type.
	 * 
	 * @param pModule
	 *            The authorization info type to be constructed
	 * @param pProxy
	 *            Proxy authentication ?
	 * @param pAddress
	 *            Server address
	 * @param pPort
	 *            Server port
	 * @param pProtocol
	 *            Protocol (http/https)
	 * @param pRealm
	 *            Realm
	 * @param pScheme
	 *            Scheme (e.g. Basic, Digest)
	 * @return An instance of a AuthInfo subclass or <code>null</code>
	 * @see SessionProperties#WWW_AUTHENTICATION
	 * @see SessionProperties#PEGASUS_LOCAL_AUTHENTICATION
	 */
	public static AuthInfo createAuthorizationInfo(String pModule, Boolean pProxy, String pAddress,
			int pPort, String pProtocol, String pRealm, String pScheme) {

		AuthInfo info = createAuthorizationInfo(pModule);

		if (info != null) {
			info.init(pProxy, pAddress, pPort, pProtocol, pRealm, pScheme);
		}

		return info;

	}

	/**
	 * Factory method for AuthInfo objects. Returns an instance of a subclass
	 * according to the requested type.
	 * 
	 * @param pModule
	 *            The authorization info type to be constructed
	 * @return An instance of a AuthInfo subclass or <code>null</code>
	 * @see SessionProperties#WWW_AUTHENTICATION
	 * @see SessionProperties#PEGASUS_LOCAL_AUTHENTICATION
	 */
	public static AuthInfo createAuthorizationInfo(String pModule) {

		if (SessionProperties.WWW_AUTHENTICATION.equals(pModule)) { return new WwwAuthInfo(); }
		if (SessionProperties.PEGASUS_LOCAL_AUTHENTICATION.equals(pModule)) { return new PegasusLocalAuthInfo(); }

		try {
			Class module = Class.forName(pModule);
			AuthInfo info = (AuthInfo) module.newInstance();
			return info;
		} catch (Exception e) {
			// TODO log this
		}
		return null;
	}
}
