/**
 * BatchCIMClient.java
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

package org.sblim.wbem.client;

import java.util.Vector;
import org.sblim.wbem.cim.CIMClass;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.cim.CIMQualifierType;
import org.sblim.wbem.cim.CIMValue;
import org.sblim.wbem.client.operations.*;

public class BatchCIMClient implements BatchHandle {

	Vector iOperations;

	public BatchCIMClient() {
		iOperations = new Vector();
	}

	public Vector getOperations() {
		return (Vector) iOperations.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#associatorNames(org.sblim.wbem.cim.CIMObjectPath)
	 */
	public int associatorNames(CIMObjectPath pObjectName) {
		return associatorNames(pObjectName, null, null, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#associatorNames(org.sblim.wbem.cim.CIMObjectPath,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public int associatorNames(CIMObjectPath pObjectName, String pAssociationClass,
			String pResultClass, String pRole, String pResultRole) {
		CIMOperation op = new CIMAssociatorNamesOp(pObjectName, pAssociationClass, pResultClass,
				pRole, pResultRole);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#associators(org.sblim.wbem.cim.CIMObjectPath)
	 */
	public int associators(CIMObjectPath pObjectName) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#associators(org.sblim.wbem.cim.CIMObjectPath,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, boolean, boolean, java.lang.String[])
	 */
	public int associators(CIMObjectPath pObjectName, String pAssociationClass,
			String pResultClass, String pRole, String pResultRole, boolean pIncludeQualifiers,
			boolean pIncludeClassOrigin, String[] pPropertyList) {
		CIMOperation op = new CIMAssociatorsOp(pObjectName, pAssociationClass, pResultClass, pRole,
				pResultRole, pIncludeQualifiers, pIncludeClassOrigin, pPropertyList);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#createClass(org.sblim.wbem.cim.CIMObjectPath,
	 *      org.sblim.wbem.cim.CIMClass)
	 */
	public int createClass(CIMObjectPath pPath, CIMClass pClass) {
		CIMOperation op = new CIMCreateClassOp(pPath, pClass);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#createInstance(org.sblim.wbem.cim.CIMObjectPath,
	 *      org.sblim.wbem.cim.CIMInstance)
	 */
	public int createInstance(CIMObjectPath pPath, CIMInstance pInstance) {
		CIMOperation op = new CIMCreateInstanceOp(pPath, pInstance);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#createNameSpace(org.sblim.wbem.cim.CIMNameSpace)
	 */
	public int createNameSpace(CIMNameSpace pNamespace) {
		CIMOperation op = new CIMCreateNameSpaceOp(pNamespace);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#createQualifierType(org.sblim.wbem.cim.CIMObjectPath,
	 *      org.sblim.wbem.cim.CIMQualifierType)
	 */
	public int createQualifierType(CIMObjectPath pPath, CIMQualifierType pQualifierType) {
		CIMOperation op = new CIMCreateQualifierTypeOp(pPath, pQualifierType);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#deleteClass(org.sblim.wbem.cim.CIMObjectPath)
	 */
	public int deleteClass(CIMObjectPath pPath) {
		CIMOperation op = new CIMDeleteClassOp(pPath);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#deleteInstance(org.sblim.wbem.cim.CIMObjectPath)
	 */
	public int deleteInstance(CIMObjectPath pPath) {
		CIMOperation op = new CIMDeleteInstanceOp(pPath);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#deleteNameSpace(org.sblim.wbem.cim.CIMNameSpace)
	 */
	public int deleteNameSpace(CIMNameSpace pNamespace) {
		CIMOperation op = new CIMDeleteNameSpaceOp(pNamespace);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#deleteQualifierType(org.sblim.wbem.cim.CIMObjectPath)
	 */
	public int deleteQualifierType(CIMObjectPath pPath) {
		CIMOperation op = new CIMDeleteQualifierTypeOp(pPath);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#enumerateClasses(org.sblim.wbem.cim.CIMObjectPath,
	 *      boolean, boolean, boolean, boolean)
	 */
	public int enumerateClasses(CIMObjectPath pPath, boolean pDeep, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin) {
		CIMOperation op = new CIMEnumClassesOp(pPath, pDeep, pLocalOnly, pIncludeQualifiers,
				pIncludeClassOrigin);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#enumerateClassNames(org.sblim.wbem.cim.CIMObjectPath,
	 *      boolean)
	 */
	public int enumerateClassNames(CIMObjectPath pPath, boolean pDeep) {
		CIMOperation op = new CIMEnumClassNamesOp(pPath, pDeep);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#enumerateInstanceNames(org.sblim.wbem.cim.CIMObjectPath)
	 */
	public int enumerateInstanceNames(CIMObjectPath pPath) {
		CIMOperation op = new CIMEnumInstanceNamesOp(pPath);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#enumerateInstances(org.sblim.wbem.cim.CIMObjectPath,
	 *      boolean, boolean, boolean, boolean, java.lang.String[])
	 */
	public int enumerateInstances(CIMObjectPath pPath, boolean pDeep, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin, String[] pPropertyList) {
		CIMOperation op = new CIMEnumInstancesOp(pPath, pDeep, pLocalOnly, pIncludeQualifiers,
				pIncludeClassOrigin, pPropertyList);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#enumNameSpace(org.sblim.wbem.cim.CIMObjectPath,
	 *      boolean)
	 */
	public int enumNameSpace(CIMNameSpace pNamespace) {
		CIMOperation op = new CIMEnumNameSpaceOp(pNamespace);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#enumQualifierTypes(org.sblim.wbem.cim.CIMObjectPath)
	 */
	public int enumQualifierTypes(CIMObjectPath pPath) {
		CIMOperation op = new CIMEnumQualifierTypesOp(pPath);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#execQuery(org.sblim.wbem.cim.CIMObjectPath,
	 *      java.lang.String, java.lang.String)
	 */
	public int execQuery(CIMObjectPath pPath, String pQuery, String pQueryLanguage) {
		CIMOperation op = new CIMExecQueryOp(pPath, pQuery, pQueryLanguage);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#getClass(org.sblim.wbem.cim.CIMObjectPath,
	 *      boolean, boolean, boolean, java.lang.String[])
	 */
	public int getClass(CIMObjectPath pPath, boolean pLocalOnly, boolean pIncludeQualifiers,
			boolean pIncludeClassOrigin, String[] pPropertyList) {
		CIMOperation op = new CIMGetClassOp(pPath, pLocalOnly, pIncludeQualifiers,
				pIncludeClassOrigin, pPropertyList);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#getInstance(org.sblim.wbem.cim.CIMObjectPath,
	 *      boolean, boolean, boolean, java.lang.String[])
	 */
	public int getInstance(CIMObjectPath pPath, boolean pLocalOnly, boolean pIncludeQualifiers,
			boolean pIncludeClassOrigin, String[] pPropertyList) {
		CIMOperation op = new CIMGetInstanceOp(pPath, pLocalOnly, pIncludeQualifiers,
				pIncludeClassOrigin, pPropertyList);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#getProperty(org.sblim.wbem.cim.CIMObjectPath,
	 *      java.lang.String)
	 */
	public int getProperty(CIMObjectPath pPath, String pName) {
		CIMOperation op = new CIMGetPropertyOp(pPath, pName);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#getQualifierType(org.sblim.wbem.cim.CIMObjectPath)
	 */
	public int getQualifierType(CIMObjectPath pPath) {
		return getQualifierType(pPath, null);
	}

	/**
	 * No idea what this method is for
	 * @param pPath
	 * @param pQualifierType
	 * @return int
	 */
	public int getQualifierType(CIMObjectPath pPath, String pQualifierType) {
		CIMOperation op = new CIMGetQualifierTypeOp(pPath, pQualifierType);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#invokeMethod(org.sblim.wbem.cim.CIMObjectPath,
	 *      java.lang.String, org.sblim.wbem.cim.CIMArgument[],
	 *      org.sblim.wbem.cim.CIMArgument[])
	 */
	public int invokeMethod(CIMObjectPath pPath, String pMethodName, Vector pInParameters,
			Vector pOutParameters) {
		CIMOperation op = new CIMInvokeMethodOp(pPath, pMethodName, pInParameters, pOutParameters);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#referenceNames(org.sblim.wbem.cim.CIMObjectPath)
	 */
	public int referenceNames(CIMObjectPath pPath) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#referenceNames(org.sblim.wbem.cim.CIMObjectPath,
	 *      java.lang.String, java.lang.String)
	 */
	public int referenceNames(CIMObjectPath pPath, String pResultClass, String pRole) {
		CIMOperation op = new CIMReferenceNamesOp(pPath, pResultClass, pRole);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#references(org.sblim.wbem.cim.CIMObjectPath)
	 */
	public int references(CIMObjectPath pPath) {
		return references(pPath, null, null, true, true, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#references(org.sblim.wbem.cim.CIMObjectPath,
	 *      java.lang.String, java.lang.String, boolean, boolean,
	 *      java.lang.String[])
	 */
	public int references(CIMObjectPath pPath, String pResultClass, String pRole,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin, String[] pPropertyList) {
		CIMOperation op = new CIMReferencesOp(pPath, pResultClass, pRole, pIncludeQualifiers,
				pIncludeClassOrigin, pPropertyList);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#setClass(org.sblim.wbem.cim.CIMObjectPath,
	 *      org.sblim.wbem.cim.CIMClass)
	 */
	public int setClass(CIMObjectPath pPath, CIMClass pClass) {
		CIMOperation op = new CIMSetClassOp(pPath, pClass);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#setInstance(org.sblim.wbem.cim.CIMObjectPath,
	 *      org.sblim.wbem.cim.CIMInstance)
	 */
	public int setInstance(CIMObjectPath pPath, CIMInstance pInstance) {
		CIMOperation op = new CIMSetInstanceOp(pPath, pInstance, true, null);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#setInstance(org.sblim.wbem.cim.CIMObjectPath,
	 *      org.sblim.wbem.cim.CIMInstance)
	 */
	public int setInstance(CIMObjectPath pPath, CIMInstance pInstance, boolean pIncludeQualifiers,
			String[] pPropertyList) {
		CIMOperation op = new CIMSetInstanceOp(pPath, pInstance, pIncludeQualifiers, pPropertyList);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#setProperty(org.sblim.wbem.cim.CIMObjectPath,
	 *      java.lang.String)
	 */
	public int setProperty(CIMObjectPath pPath, String pName) {
		return setProperty(pPath, pName, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#setProperty(org.sblim.wbem.cim.CIMObjectPath,
	 *      java.lang.String, org.sblim.wbem.cim.CIMValue)
	 */
	public int setProperty(CIMObjectPath pPath, String pName, CIMValue pValue) {
		CIMOperation op = new CIMSetPropertyOp(pPath, pName, pValue);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.client.BatchHandle#setQualifierType(org.sblim.wbem.cim.CIMObjectPath,
	 *      org.sblim.wbem.cim.CIMQualifierType)
	 */
	public int setQualifierType(CIMObjectPath pPath, CIMQualifierType pQualifierType) {
		CIMOperation op = new CIMSetQualifierTypeOp(pPath, pQualifierType);
		iOperations.add(op);
		return iOperations.size() - 1;
	}

}
