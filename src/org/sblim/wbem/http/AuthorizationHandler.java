/**
 * AuthorizationHandler.java
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
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1516242    2006-11-27  lupusalex    Support of OpenPegasus local authentication
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.http;

import java.util.Iterator;
import java.util.Vector;

public class AuthorizationHandler {

	private Vector iAuthList = new Vector();

	public synchronized void addAuthorizationInfo(AuthInfo authInfo) {
		iAuthList.add(authInfo);
	}

	public synchronized AuthInfo getAuthorizationInfo(String authorizationModule, Boolean proxy,
			String addr, int port, String protocol, String realm, String scheme) {

		AuthInfo request = AuthInfo.createAuthorizationInfo(authorizationModule, proxy, addr, port,
				protocol, realm, scheme);
		
		Iterator iter = iAuthList.iterator();
		while (iter.hasNext()) {
			AuthInfo authInfo = (AuthInfo) iter.next();

			if (authInfo.match(request)) return authInfo;

		}
		return null;
	}

	public synchronized AuthInfo getAuthorizationInfo(int index) {
		return (AuthInfo) iAuthList.elementAt(index);
	}

	public String toString() {
		return "AuthorizationHandler=[AuthInfoList=" + iAuthList + "]";
	}
}
