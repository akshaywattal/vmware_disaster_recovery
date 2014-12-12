/**
 * CIMOMHandle.java
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
 * ------------------------------------------------------------------------------- 
 * 1498130    2006-05-31  lupusalex    Selection of xml parser on a per connection basis
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1646434    2007-01-28  lupusalex    CIMClient close() invalidates all it's enumerations
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.client;

import org.sblim.wbem.cim.*;
import org.sblim.wbem.client.indications.*;
import org.sblim.wbem.util.SessionProperties;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

public interface CIMOMHandle {

	public void addCIMListener(CIMListener pListener) throws CIMException;

	public Enumeration associatorNames(CIMObjectPath pPath) throws CIMException;

	public Enumeration associatorNames(CIMObjectPath pPath, String pAssociationClass,
			String pResultClass, String pRole, String pResultRole) throws CIMException;

	public Enumeration associators(CIMObjectPath pPath, String pAssociationClass,
			String pResultClass, String pRole, String pResultRole, boolean pIncludeQualifiers,
			boolean pIncludeClassOrigin, String[] pPropertyList) throws CIMException;

	public void close() throws CIMException;

	public void close(boolean pKeepEnumerations) throws CIMException;

	public void createClass(CIMObjectPath pPath, CIMClass pClass) throws CIMException;

	public CIMObjectPath createInstance(CIMObjectPath pPath, CIMInstance instance)
			throws CIMException;

	public void createNameSpace(CIMNameSpace pNamespace) throws CIMException;

	public void createQualifierType(CIMObjectPath pPath, CIMQualifierType pQualifierType)
			throws CIMException;

	public void deleteClass(CIMObjectPath pPath) throws CIMException;

	public void deleteInstance(CIMObjectPath pPath) throws CIMException;

	public void deleteNameSpace(CIMNameSpace pNamespace) throws CIMException;

	public void deleteQualifierType(CIMObjectPath pPath) throws CIMException;

	public Enumeration enumerateInstanceNames(CIMObjectPath pPath) throws CIMException;

	public Enumeration enumerateClasses(CIMObjectPath pPath, boolean pDeep, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin) throws CIMException;

	public Enumeration enumerateClassNames(CIMObjectPath pPath, boolean pDeep) throws CIMException;

	public Enumeration enumerateInstances(CIMObjectPath pPath, boolean pDeep, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin,
			java.lang.String[] pPropertyList) throws CIMException;

	public Enumeration enumNameSpace(CIMObjectPath pPath, boolean pDeep) throws CIMException;

	public Enumeration enumQualifierTypes(CIMObjectPath pPath) throws CIMException;

	public Enumeration execQuery(CIMObjectPath pPath, String pQuery, String pQueryLanguage)
			throws CIMException;

	public CIMClass getClass(CIMObjectPath pPath, boolean pLocalOnly, boolean pIncludeQualifiers,
			boolean pIncludeClassOrigin, java.lang.String[] pPropertyList) throws CIMException;

	public CIMInstance getIndicationHandler(CIMListener pListener) throws CIMException;

	public CIMInstance getIndicationListener(CIMListener pListener) throws CIMException;

	public CIMInstance getInstance(CIMObjectPath pPath, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin,
			java.lang.String[] pPropertyList) throws CIMException;

	public CIMValue getProperty(CIMObjectPath pPath, String pPropertyName) throws CIMException;

	public CIMQualifierType getQualifierType(CIMObjectPath pPath) throws CIMException;

	public CIMValue invokeMethod(CIMObjectPath pPath, String pMethodName, Vector pInputArguments,
			Vector pOutputArguments) throws CIMException;

	public BatchResult performBatchOperations(BatchHandle pBatchHandle) throws CIMException;

	public Enumeration referenceNames(CIMObjectPath pPath) throws CIMException;

	public Enumeration referenceNames(CIMObjectPath pPath, String pResultClass, String pRole)
			throws CIMException;

	public Enumeration references(CIMObjectPath pPath) throws CIMException;

	public Enumeration references(CIMObjectPath pPath, String pResultClass, String pRole,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin, String[] pPropertyList)
			throws CIMException;

	public void removeCIMListener(CIMListener pListener) throws CIMException;

	public void setClass(CIMObjectPath pPath, CIMClass pClass) throws CIMException;

	public void setInstance(CIMObjectPath pPath, CIMInstance pInstance, boolean pIncludeQualifiers,
			String[] pPropertyList) throws CIMException;

	public void setProperty(CIMObjectPath pPath, String pPropertyName) throws CIMException;

	public void setProperty(CIMObjectPath pPath, String pPropertyName, CIMValue pValue)
			throws CIMException;

	public void setQualifierType(CIMObjectPath pPath, CIMQualifierType pQualifierType)
			throws CIMException;

	public void useHttp11(boolean pValue);

	public void useMPost(boolean pValue);

	public CIMNameSpace getNameSpace();

	public void setLocale(Locale pLocale);

	public Locale getLocale();

	public SessionProperties getSessionProperties();

	public void setSessionProperties(SessionProperties pProperties);
}
