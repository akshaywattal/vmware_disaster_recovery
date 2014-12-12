/**
 * CIMEnumClassesOp.java
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

public class CIMEnumClassesOp extends CIMOperation {

	protected boolean iDeep;

	protected boolean iLocalOnly;

	protected boolean iIncludeQualifiers;

	protected boolean iIncludeClassOrigin;

	public CIMEnumClassesOp(CIMObjectPath pObjectName, boolean pDeep, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin) {
		iMethodCall = "EnumerateClasses";
		iObjectName = pObjectName;
		iDeep = pDeep;
		iLocalOnly = pLocalOnly;
		iIncludeClassOrigin = pIncludeClassOrigin;
		iIncludeQualifiers = pIncludeQualifiers;
	}

	/**
	 * Returns if deep is set
	 * 
	 * @return The value of deep
	 */
	public boolean isDeep() {
		return iDeep;
	}

	/**
	 * Returns if includeClassOrigin is set
	 * 
	 * @return The value of includeClassOrigin
	 */
	public boolean isIncludeClassOrigin() {
		return iIncludeClassOrigin;
	}

	/**
	 * Returns if includeQualifiers is set
	 * 
	 * @return The value of includeQualifiers
	 */
	public boolean isIncludeQualifiers() {
		return iIncludeQualifiers;
	}

	/**
	 * Returns if localOnly is set
	 * 
	 * @return The value of localOnly
	 */
	public boolean isLocalOnly() {
		return iLocalOnly;
	}

}
