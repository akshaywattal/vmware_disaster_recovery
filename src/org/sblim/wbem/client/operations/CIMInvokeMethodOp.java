/**
 * CIMInvokeMethodOp.java
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

package org.sblim.wbem.client.operations;

import java.util.Vector;

import org.sblim.wbem.cim.CIMObjectPath;

public class CIMInvokeMethodOp extends CIMOperation {

	protected String iMethodName;

	protected Vector iInParams;

	protected Vector iOutParams;

	public CIMInvokeMethodOp(CIMObjectPath pObjectName, String pMethodName, Vector pInParams,
			Vector pOutParams) {
		iMethodCall = "InvokeMethod";
		iObjectName = pObjectName;
		iMethodName = pMethodName;
		iInParams = pInParams;
		iOutParams = pOutParams;
	}

	
	/**
	 * Returns inParameters
	 * @return The value of inParameters.
	 */
	public Vector getInParams() {
		return iInParams;
	}

	
	/**
	 * Returns methodName
	 * @return The value of methodName.
	 */
	public String getMethodName() {
		return iMethodName;
	}

	
	/**
	 * Returns outParameters
	 * @return The value of outParameters.
	 */
	public Vector getOutParams() {
		return iOutParams;
	}


}
