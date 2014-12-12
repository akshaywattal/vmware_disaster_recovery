/**
 * HttpClientConnectionMgr.java
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
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.http;

public class HttpClientConnectionMgr {

	HttpClientConnectionMgr iInstance = new HttpClientConnectionMgr();

	private HttpClientConnectionMgr() {}
}

class ServerInfo {

	String iHostname;

	int iPort;

	public ServerInfo(String hostname, int port) {
		iHostname = hostname;
		iPort = port;
	}

	public boolean equal(Object obj) {
		if (obj == null || !(obj instanceof ServerInfo)) return false;
		if (obj == this) return true;
		ServerInfo that = (ServerInfo) obj;

		return (iHostname.equalsIgnoreCase(that.iHostname) && iPort == that.iPort);
	}

	public int hashCode() {
		return iHostname.hashCode() << 16 + iPort;
	}
}
