/**
 * BatchHandle.java
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

public interface BatchHandle {

	Vector getOperations();

	int associatorNames(CIMObjectPath pPath);

	int associatorNames(CIMObjectPath pPath, String pAssociationClass, String pResultClass,
			String pRole, String pResultRole);

	int associators(CIMObjectPath pPath);

	int associators(CIMObjectPath pPath, String pAssociationClass, String pResultClass,
			String pRole, String pResultRole, boolean pIncludeQualifiers,
			boolean pIncludeClassOrirgin, String[] pPropertyList);

	int createClass(CIMObjectPath pPath, CIMClass pClass);

	int createInstance(CIMObjectPath pPath, CIMInstance pInstance);

	int createNameSpace(CIMNameSpace pNamespace);

	int createQualifierType(CIMObjectPath pPath, CIMQualifierType pQualifierType);

	int deleteClass(CIMObjectPath pPath);

	int deleteInstance(CIMObjectPath pPath);

	int deleteNameSpace(CIMNameSpace pNamespace);

	int deleteQualifierType(CIMObjectPath pPath);

	int enumerateClasses(CIMObjectPath pPath, boolean pDeep, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrirgin);

	int enumerateClassNames(CIMObjectPath pPath, boolean pDeep);

	int enumerateInstanceNames(CIMObjectPath pPath);

	int enumerateInstances(CIMObjectPath pPath, boolean pDeep, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrirgin, String[] pPropertyList);

	int enumNameSpace(CIMNameSpace pNamespace);

	int enumQualifierTypes(CIMObjectPath pPath);

	int execQuery(CIMObjectPath pPath, String pQuery, String pQueryLanguage);

	int getClass(CIMObjectPath pPath, boolean pLocalOnly, boolean pIncludeQualifiers,
			boolean pIncludeClassOrirgin, String[] pPropertyList);

	int getInstance(CIMObjectPath pPath, boolean pLocalOnly, boolean pIncludeQualifiers,
			boolean pIncludeClassOrirgin, String[] pPropertyList);

	int getProperty(CIMObjectPath pPath, String pName);

	int getQualifierType(CIMObjectPath pPath);

	int invokeMethod(CIMObjectPath pPath, String pName, Vector pInParameters, Vector pOutParameters);

	int referenceNames(CIMObjectPath pPath);

	int referenceNames(CIMObjectPath pPath, String pResultClass, String pRole);

	int references(CIMObjectPath pPath);

	int references(CIMObjectPath pPath, String pResultClass, String pRole,
			boolean pIncludeQualifiers, boolean pIncludeClassOrirgin, String[] pPropertyList);

	int setClass(CIMObjectPath pPath, CIMClass pClass);

	int setInstance(CIMObjectPath pPath, CIMInstance pInstance);

	int setProperty(CIMObjectPath pPath, String pName);

	int setProperty(CIMObjectPath pPath, String pName, CIMValue pValue);

	int setQualifierType(CIMObjectPath pPath, CIMQualifierType pQualifierType);
}
