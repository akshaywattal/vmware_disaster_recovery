/**
 * CIMError.java
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
package org.sblim.wbem.client.indications;

import org.sblim.wbem.cim.CIMException;

public class CIMError {

	private int iStatusCode;

	private String iDescription;

	/**
	 * Construct a CIMError object using the default status (CIM_ERR_FAIL).
	 */
	public CIMError() {
		this(1, "");
	}

	/**
	 * Construct a CIMError object from the given CIMException.
	 * 
	 * @param pCimException
	 */
	public CIMError(CIMException pCimException) {
		if (pCimException == null) throw new IllegalArgumentException("null cim exception argument");

		iStatusCode = pCimException.getStatusCode();
		iDescription = pCimException.getDescription();
	}

	/**
	 * Construct a CIMError object with the specified status code.
	 * 
	 * @param pStatus
	 *            The status code
	 */
	public CIMError(int pStatus) {
		this(pStatus, null);
	}

	public CIMError(int status, String msg) {
		setCode(status);
		setDescription(msg);
	}

	/**
	 * Gets the status code.
	 * 
	 * @return The status code
	 */
	public int getCode() {
		return iStatusCode;
	}

	/**
	 * Gets the description associated with this status.
	 * 
	 * @return The description
	 */
	public String getDescription() {
		return iDescription;
	}

	/**
	 * Specifies the status code.
	 * 
	 * @param pStatus
	 */
	public void setCode(int pStatus) {
		if (pStatus > 18 || pStatus < 1) throw new IllegalArgumentException("invalid error code");
		iStatusCode = pStatus;
	}

	/**
	 * Specifies the description associated to this status.
	 * 
	 * @param pDescription
	 */
	public void setDescription(String pDescription) {
		iDescription = pDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "CIMError: " + CIMException.getStatusFromCode(iStatusCode + 1)
				+ ((iDescription != null) ? ("(" + iDescription + ")") : "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof CIMError)) return false;

		CIMError that = (CIMError) o;

		if (iStatusCode == that.iStatusCode
				&& (iDescription == null ? that.iDescription == null : iDescription
						.equals(that.iDescription))) return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (iDescription != null ? iDescription.hashCode() : 0) << 16 + iStatusCode;
	}
}
