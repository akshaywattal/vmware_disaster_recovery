/**
 * CIMEvent.java
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
 *------------------------------------------------------------------------------
 *   17931    2005-07-28  thschaef     Add InetAddress field 
 *                                     + constructor and getter
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.client.indications;

import java.net.InetAddress;

import org.sblim.wbem.cim.CIMInstance;

public class CIMEvent {

	protected CIMInstance iIndication;

	protected String iID;

	protected InetAddress iInetAddress = null;

	public CIMEvent(CIMInstance pIndication) {
		this(pIndication, null);
	}

	public CIMEvent(CIMInstance pIndication, String id) {
		iIndication = pIndication;
		iID = id;
	}

	/**
	 * Constructor that takes the CIMInstance of the indication, the id as well
	 * as the InetAddress of the remote machine.
	 * 
	 * @param pIndication
	 *            The indication instance
	 * @param pId
	 *            The id
	 * @param pInetAddress
	 *            The address
	 */
	public CIMEvent(CIMInstance pIndication, String pId, InetAddress pInetAddress) {
		iIndication = pIndication;
		iID = pId;
		iInetAddress = pInetAddress;
	}

	/**
	 * This method returns the InetAddress of the machine that hosts the CIM
	 * Agent that sent the indication. Be aware the remote machine could have
	 * multiple network adapters - thus the result can be ambigous.
	 * 
	 * @return The InetAddress of the remote machine
	 */
	public InetAddress getInetAddress() {
		return iInetAddress;
	}

	public CIMInstance getIndication() {
		return iIndication;
	}

	public String getID() {
		return iID;
	}
}
