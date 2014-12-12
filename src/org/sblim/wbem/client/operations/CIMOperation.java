/**
 * CIMOperation.java
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

import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObjectPath;

public abstract class CIMOperation {

	protected CIMObjectPath iObjectName;

	protected CIMNameSpace iNameSpace;

	protected String iMethodCall;

	protected Object iResult;

	/**
	 * Returns the object name
	 * 
	 * @return The object name
	 */
	public CIMObjectPath getObjectName() {
		return iObjectName;
	}

	/**
	 * Returns the namespace
	 * 
	 * @return The namespace
	 */
	public CIMNameSpace getNameSpace() {
		return iNameSpace;
	}

	/**
	 * Sets the namespace
	 * 
	 * @param pNamespace
	 *            The namespace
	 */
	public void setNameSpace(CIMNameSpace pNamespace) {
		iNameSpace = pNamespace;
	}

	/**
	 * Returns the method call
	 * 
	 * @return The method call
	 */
	public String getMethodCall() {
		return iMethodCall;
	}

	/**
	 * Returns if an (uncaught) exception occurred
	 * 
	 * @return <code>true</code> if an (uncaught) exception occurred,
	 *         <code>false</code> otherwise
	 */
	public boolean isException() {
		return (iResult instanceof Exception);
	}

	/**
	 * Returns the result of the operation
	 * 
	 * @return The result
	 * @throws CIMException
	 */
	public Object getResult() throws CIMException {
		if (iResult instanceof Exception) throw (CIMException) iResult;
		return iResult;
	}

	/**
	 * Sets the operation result
	 * 
	 * @param pResult
	 *            The result
	 */
	public void setResult(Object pResult) {
		iResult = pResult;
	}
}
