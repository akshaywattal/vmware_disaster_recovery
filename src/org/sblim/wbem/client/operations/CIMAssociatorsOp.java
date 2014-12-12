/**
 * CIMAssociatorsOp.java
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

public class CIMAssociatorsOp extends CIMOperation {

	protected String iAssociationClass;

	protected String iResultClass;

	protected String iRole;

	protected String iResultRole;

	protected boolean iIncludeQualifiers;

	protected boolean iIncludeClassOrigin;

	protected String[] iPropertyList;

	public CIMAssociatorsOp(CIMObjectPath pObjectName, String pAssociationClass,
			String pResultClass, String pRole, String pResultRole, boolean pIncludeQualifiers,
			boolean pIncludeClassOrigin, String[] pPropertyList) {

		iMethodCall = "Associators";
		iObjectName = pObjectName;
		iAssociationClass = pAssociationClass;
		iResultClass = pResultClass;
		iRole = pRole;
		iIncludeClassOrigin = pIncludeClassOrigin;
		iIncludeQualifiers = pIncludeQualifiers;
		iPropertyList = pPropertyList;
	}

	/**
	 * Returns the association class
	 * 
	 * @return The association class
	 */
	public String getAssocClass() {
		return iAssociationClass;
	}

	/**
	 * Returns if includeQualifiers is set
	 * 
	 * @return The value of includeClassOrigin
	 */
	public boolean isIncludeClassOrigin() {
		return iIncludeClassOrigin;
	}

	/**
	 * Returns if includeQualifiers is set
	 * 
	 * @return The value of includeClassOrigin
	 */
	public boolean isIncludeQualifiers() {
		return iIncludeQualifiers;
	}

	/**
	 * Returns the property list
	 * 
	 * @return The property list
	 */
	public String[] getPropertyList() {
		return iPropertyList;
	}

	/**
	 * Returns the result class
	 * 
	 * @return The result class
	 */
	public String getResultClass() {
		return iResultClass;
	}

	/**
	 * Returns the result role
	 * 
	 * @return The result role
	 */
	public String getResultRole() {
		return iResultRole;
	}

	/**
	 * Returns the role
	 * 
	 * @return The role
	 */
	public String getRole() {
		return iRole;
	}

}
