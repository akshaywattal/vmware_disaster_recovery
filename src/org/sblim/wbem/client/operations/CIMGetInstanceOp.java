/**
 * CIMGetInstanceOp.java
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

import org.sblim.wbem.cim.CIMObjectPath;

public class CIMGetInstanceOp extends CIMSingleResultOperation {

	protected boolean iLocalOnly;

	protected boolean iIncludeQualifiers;

	protected boolean iIncludeClassOrigin;

	protected String[] iPropertyList;

	public CIMGetInstanceOp(CIMObjectPath pObjectName, boolean pLocalOnly, boolean pIncludeQualifiers,
			boolean pIncludeClassOrigin, String[] pPropertyList) {

		iMethodCall = "GetInstance";
		iObjectName = pObjectName;
		iLocalOnly = pLocalOnly;
		iIncludeClassOrigin = pIncludeClassOrigin;
		iIncludeQualifiers = pIncludeQualifiers;
		iPropertyList = pPropertyList;
	}

	
	/**
	 * Returns includeClassOrigin
	 * @return The value of includeClassOrigin.
	 */
	public boolean isIncludeClassOrigin() {
		return iIncludeClassOrigin;
	}

	
	/**
	 * Returns includeQualifiers
	 * @return The value of includeQualifiers.
	 */
	public boolean isIncludeQualifiers() {
		return iIncludeQualifiers;
	}

	
	/**
	 * Returns localOnly
	 * @return The value of localOnly.
	 */
	public boolean isLocalOnly() {
		return iLocalOnly;
	}

	
	/**
	 * Returns propertyList
	 * @return The value of propertyList.
	 */
	public String[] getPropertyList() {
		return iPropertyList;
	}
}
