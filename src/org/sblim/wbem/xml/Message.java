/**
 * Message.java
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

package org.sblim.wbem.xml;

/**
 * @author Roberto
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
class Message {

	String iId;

	String iProtocolVersion;

	public Message(String id, String protocolVersion) {
		this.iId = id;
		this.iProtocolVersion = protocolVersion;
	}

	public String getID() {
		return iId;
	}

	public String getProtocolVersion() {
		return iProtocolVersion;
	}

	public String toString() {
		return "MESSAGE ID=\"" + iId + "\" PROTOCOLVERSION=\"" + iProtocolVersion + "\"";
	}
}
