/**
 * CIMResponse.java
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

package org.sblim.wbem.xml;

import java.util.Vector;

import org.sblim.wbem.cim.CIMException;

/**
 * Represent a CIMReponse message.
 */
public class CIMResponse extends CIMMessage {

	protected Vector iResponses = new Vector(0);

	protected CIMException iError = null;

	protected Vector iReturnValue = new Vector();

	protected Vector iParamValue = new Vector(0);

	/**
	 * Constructs a CIMResponse object.
	 * 
	 */
	public CIMResponse() {}

	/**
	 * Constructs a CIMResponse object with the specified CIMVersion, DTDVersion
	 * and method.
	 * 
	 * @param cimVersion
	 * @param dtdVersion
	 * @param method
	 */
	public CIMResponse(String cimVersion, String dtdVersion, String method) {
		super(cimVersion, dtdVersion, method);
	}

	/**
	 * Constructs a CIM Response message from a given CIM Request.
	 * 
	 * @param request
	 */
	public CIMResponse(CIMRequest request) {
		super();
		iCimVersion = request.getCIMVersion();
		iDtdVersion = request.getDTDVersion();
	}

	/**
	 * Adds a
	 * 
	 * @param o
	 */
	public void addParamValue(Object o) {
		iParamValue.add(o);
	}

	public void addParamValue(Vector v) {
		iParamValue.addAll(v);
	}

	public void addResponse(CIMResponse response) {
		iResponses.add(response);
	}

	public void addReturnValue(Object o) {
		iReturnValue.add(o);
	}

	/**
	 * Verify the status code for this CIMResponse.
	 * 
	 * @throws CIMException
	 *             if the status code is other than success.
	 */
	public void checkError() throws CIMException {
		if (iError != null) throw iError;
	}

	public Vector getAllResponses() {
		return iResponses;
	}

	public CIMException getException() {
		return iError;
	}

	public boolean isSuccessul() {
		return iError == null;
	}

	public CIMResponse getFirstResponse() {
		if (iResponses != null && iResponses.size() > 0) return (CIMResponse) iResponses.elementAt(0);
		return null;
	}

	public Vector getParamValues() {
		return iParamValue;
	}

	public Vector getFirstReturnValue() {
		return iReturnValue;
	}

	public void setError(CIMException error) {
		this.iError = error;
	}

	public void setParamValue(Vector paramValue) {
		this.iParamValue = paramValue;
	}

	public void setReturnValue(Vector returnValue) {
		this.iReturnValue = returnValue;
	}

}
