/**
 * CIMSetInstanceOp.java
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

import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMObjectPath;

public class CIMSetInstanceOp extends CIMOperation {

	protected CIMInstance iInstance;

	protected boolean iIncludeQualifiers;

	protected String[] iPropertyList;

	public CIMSetInstanceOp(CIMObjectPath objectName, CIMInstance instance,
			boolean includeQualifiers, String[] propertyList) {
		iMethodCall = "SetInstance";
		iObjectName = objectName;
		iInstance = instance;
		iIncludeQualifiers = includeQualifiers;
		iPropertyList = propertyList;
	}

	
	/**
	 * Returns includeQualifiers
	 *
	 * @return The value of includeQualifiers.
	 */
	public boolean isIncludeQualifiers() {
		return iIncludeQualifiers;
	}

	
	/**
	 * Returns instance
	 *
	 * @return The value of instance.
	 */
	public CIMInstance getInstance() {
		return iInstance;
	}

	
	/**
	 * Returns propertyList
	 *
	 * @return The value of propertyList.
	 */
	public String[] getPropertyList() {
		return iPropertyList;
	}


}
