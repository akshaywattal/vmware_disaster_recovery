/**
 * CIMException.java
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

package org.sblim.wbem.cim;

import java.io.Serializable;

/**
 * This class is the super class for classes used to describe exceptional CIM
 * conditions. Standard status codes are marked starts with the CIM prefix as
 * part of the status message. Extended status codes are start with the EXT_
 * prefix and must be added in classes extending the CIMException.
 */
public class CIMException extends RuntimeException implements Serializable {

	private static final long serialVersionUID = 2720629774634502419L;

	/**
	 * CIM Status Code value: General error without more specific information.
	 * Origin: CIM Server/Provider as well as CIM Client code. TODO Docu: How
	 * are description and extendedReason used ? TODO Can we change creaiton of
	 * exceptions with this status code in the CIM Client code so they use some
	 * other code ?
	 */
	public final static String CIM_ERR_FAILED = "CIM_ERR_FAILED";

	/**
	 * CIM Status Code value: Access to a CIM resource is not available, or the
	 * logon attempt to the CIM Server failed. Origin: CIM Server/Provider only.
	 * TODO Docu: How are description and extendedReason used ?
	 */
	public final static String CIM_ERR_ACCESS_DENIED = "CIM_ERR_ACCESS_DENIED";

	/**
	 * CIM Status Code value: The target CIM namespace does not exist in the CIM
	 * Server. Origin: CIM Server/Provider only. TODO Docu: How are description
	 * and extendedReason used ?
	 */
	public final static String CIM_ERR_INVALID_NAMESPACE = "CIM_ERR_INVALID_NAMESPACE";

	/**
	 * CIM Status Code value: One or more parameter values passed to the CIM
	 * method or Java method are not valid. This includes cases of missing,
	 * duplicate, unrecognized or otherwise incorrect parameters. The
	 * description mentions in free form text which parameter is invalid for
	 * what reason. Origin: CIM Server/Provider as well as CIM Client code. TODO
	 * Docu: How is extendedReason used ? TODO Can we change creaiton of
	 * exceptions with this status code in the CIM Client code so they use some
	 * other code ?
	 */
	public final static String CIM_ERR_INVALID_PARAMETER = "CIM_ERR_INVALID_PARAMETER";

	/**
	 * CIM Status Code value: The CIM class does not exist in the target CIM
	 * namespace. Origin: CIM Server/Provider only. TODO Docu: How are
	 * description and extendedReason used ?
	 */
	public final static String CIM_ERR_INVALID_CLASS = "CIM_ERR_INVALID_CLASS";

	/**
	 * CIM Status Code value: For CIM Server/Provider originated errors, the CIM
	 * object (instance or class) does not exist in the target CIM namespace.
	 * For CIM Client originated errors, something has not been found. Origin:
	 * CIM Server/Provider as well as CIM Client code. TODO Docu: How are
	 * description and extendedReason used ? TODO Can we change creaiton of
	 * exceptions with this status code in the CIM Client code so they use some
	 * other code ?
	 */
	public final static String CIM_ERR_NOT_FOUND = "CIM_ERR_NOT_FOUND";

	/**
	 * CIM Status Code value: The CIM operation is not supported by the CIM
	 * Server/Provider. Origin: CIM Server/Provider only. TODO Docu: How are
	 * description and extendedReason used ?
	 */
	public final static String CIM_ERR_NOT_SUPPORTED = "CIM_ERR_NOT_SUPPORTED";

	/**
	 * CIM Status Code value: The CIM class has subclasses. Origin: CIM
	 * Server/Provider only. TODO Docu: How are description and extendedReason
	 * used ?
	 */
	public final static String CIM_ERR_CLASS_HAS_CHILDREN = "CIM_ERR_CLASS_HAS_CHILDREN";

	/**
	 * CIM Status Code value: One or more instances of this CIM class exist.
	 * Origin: CIM Server/Provider only. TODO Docu: How are description and
	 * extendedReason used ?
	 */
	public final static String CIM_ERR_CLASS_HAS_INSTANCES = "CIM_ERR_CLASS_HAS_INSTANCES";

	/**
	 * CIM Status Code value: The CIM superclass does not exist. Origin: CIM
	 * Server/Provider only. TODO Docu: How are description and extendedReason
	 * used ?
	 */
	public final static String CIM_ERR_INVALID_SUPERCLASS = "CIM_ERR_INVALID_SUPERCLASS";

	/**
	 * CIM Status Code value: The CIM object (class or instance) already exists.
	 * Origin: CIM Server/Provider only. TODO Docu: How are description and
	 * extendedReason used ?
	 */
	public final static String CIM_ERR_ALREADY_EXISTS = "CIM_ERR_ALREADY_EXISTS";

	/**
	 * CIM Status Code value: The CIM property is not defined on the class. Note
	 * that if the property is defined on the class but not supported by a CIM
	 * Provider, it is still supposed to be returned to the client with its
	 * class defined default value. Origin: CIM Server/Provider only. TODO Docu:
	 * How are description and extendedReason used ?
	 */
	public final static String CIM_ERR_NO_SUCH_PROPERTY = "CIM_ERR_NO_SUCH_PROPERTY";

	/**
	 * CIM Status Code value: The value supplied is not compatible with the CIM
	 * type. Origin: CIM Server/Provider only. TODO Docu: How are description
	 * and extendedReason used ?
	 */
	public final static String CIM_ERR_TYPE_MISMATCH = "CIM_ERR_TYPE_MISMATCH";

	/**
	 * Origin: Not clear. TODO Not used anywhere in the CIM Client code, and
	 * also not a DMTF defined CIM status code.
	 */
	public final static String CIM_ERR_LOW_ON_MEMORY = "CIM_ERR_LOW_ON_MEMORY";

	/**
	 * CIM Status Code value: The query language is not recognized or supported.
	 * Origin: CIM Server/Provider only. TODO Docu: How are description and
	 * extendedReason used ?
	 */
	public final static String CIM_ERR_QUERY_LANGUAGE_NOT_SUPPORTED = "CIM_ERR_QUERY_LANGUAGE_NOT_SUPPORTED";

	/**
	 * CIM Status Code value: The query is not valid for the specified query
	 * language. Origin: CIM Server/Provider only. TODO Docu: How are
	 * description and extendedReason used ?
	 */
	public final static String CIM_ERR_INVALID_QUERY = "CIM_ERR_INVALID_QUERY";

	/**
	 * CIM Status Code value: The CIM method is not supported by the CIM
	 * Provider for the class. Note that it is still be defined on the class.
	 * Origin: CIM Server/Provider only. TODO Docu: How are description and
	 * extendedReason used ?
	 */
	public final static String CIM_ERR_METHOD_NOT_AVAILABLE = "CIM_ERR_METHOD_NOT_AVAILABLE";

	/**
	 * CIM Status Code value: The CIM method is not defined on the class.
	 * Origin: CIM Server/Provider only. TODO Docu: How are description and
	 * extendedReason used ?
	 */
	public final static String CIM_ERR_METHOD_NOT_FOUND = "CIM_ERR_METHOD_NOT_FOUND";

	/**
	 * Origin: Not clear. TODO Not used anywhere in the CIM Client code, and
	 * also not a DMTF defined CIM status code.
	 */
	public final static String CIM_ERR_NOT_IMPLEMENTED = "CIM_ERR_NOT_IMPLEMENTED";

	/**
	 * Origin: Not clear. TODO Not used anywhere in the CIM Client code, and
	 * also not a DMTF defined CIM status code.
	 */
	public final static String IBM_ERR_VER_ERROR = "IBM_ERR_VER_ERROR";

	/**
	 * Origin: Not clear. TODO Not used anywhere in the CIM Client code, and
	 * also not a DMTF defined CIM status code.
	 */
	public final static String IBM_ERR_TIMED_OUT = "IBM_ERR_TIMED_OUT";

	/*
	 * TODO Add missing CIM Status Codes from CIM Ops 1.2:
	 * CIM_ERR_INVALID_RESPONSE_DESTINATION, CIM_ERR_NAMESPACE_NOT_EMPTY
	 */

	protected static final String[] CIM_ERROR_NAMES = { CIM_ERR_FAILED, /*
																		 * CIM
																		 * status
																		 * code
																		 * 1,
																		 * must
																		 * be at
																		 * index
																		 * 0
																		 */
	CIM_ERR_ACCESS_DENIED, /* CIM status code 2, must be at index 1 */
	CIM_ERR_INVALID_NAMESPACE, /* CIM status code 3, must be at index 2 */
	CIM_ERR_INVALID_PARAMETER, /* CIM status code 4, must be at index 3 */
	CIM_ERR_INVALID_CLASS, /* CIM status code 5, must be at index 4 */
	CIM_ERR_NOT_FOUND, /* CIM status code 6, must be at index 5 */
	CIM_ERR_NOT_SUPPORTED, /* CIM status code 7, must be at index 6 */
	CIM_ERR_CLASS_HAS_CHILDREN, /* CIM status code 8, must be at index 7 */
	CIM_ERR_CLASS_HAS_INSTANCES, /* CIM status code 9, must be at index 8 */
	CIM_ERR_INVALID_SUPERCLASS, /* CIM status code 10, must be at index 9 */
	CIM_ERR_ALREADY_EXISTS, /* CIM status code 11, must be at index 10 */
	CIM_ERR_NO_SUCH_PROPERTY, /* CIM status code 12, must be at index 11 */
	CIM_ERR_TYPE_MISMATCH, /* CIM status code 13, must be at index 12 */
	CIM_ERR_QUERY_LANGUAGE_NOT_SUPPORTED, /*
											 * CIM status code 14, must be at
											 * index 13
											 */
	CIM_ERR_INVALID_QUERY, /* CIM status code 15, must be at index 14 */
	CIM_ERR_METHOD_NOT_AVAILABLE, /* CIM status code 16, must be at index 15 */
	CIM_ERR_METHOD_NOT_FOUND }; /* CIM status code 17, must be at index 16 */

	private Throwable iCause; /* TODO Docu: How does this work ? */

	private String iReason; /* CIM Status code string */

	private String iDescription; /* base textual description */

	private Object[] iExtendedReason; /*
										 * array of Throwable objects or
										 * additional textual descriptions
										 */

	/**
	 * Constructs a CIMException with no detail message.
	 * 
	 */
	public CIMException() {
		this((String) null, (Object[]) null);
	}

	/**
	 * Constructs a CIMException with the specified message.
	 * 
	 * @param pReason -
	 *            a symbolic name of the CIMException. e.g. CIM_ERR_FAILED,
	 *            CIM_ERR_NOT_FOUND, etc.
	 */
	public CIMException(String pReason) {
		this(pReason, (String[]) null);
	}

	/**
	 * Constructs a CIMException with the specified message and one extended
	 * reason parameter.
	 * 
	 * @param pReason -
	 *            a symbolic name of the CIMException. e.g. CIM_ERR_FAILED,
	 *            CIM_ERR_NOT_FOUND, etc.
	 * @param pExtendedReason
	 *            extended reason describing the exception.
	 */
	public CIMException(String pReason, Object pExtendedReason) {
		this(pReason, new Object[] { pExtendedReason });
	}

	/**
	 * Constructs a CIMException with the specified message and one extended
	 * reason parameter.
	 * 
	 * @param pReason -
	 *            a symbolic name of the CIMException. e.g. CIM_ERR_FAILED,
	 *            CIM_ERR_NOT_FOUND, etc.
	 * @param pExtendedReason1
	 *            first extended reason describing the exception.
	 * @param pExtendedReason2
	 *            second extended reason describing the exception.
	 */
	public CIMException(String pReason, Object pExtendedReason1, Object pExtendedReason2) {
		this(pReason, new Object[] { pExtendedReason1, pExtendedReason2 });
	}

	/**
	 * Constructs a CIMException with the specified message and one extended
	 * reason parameter.
	 * 
	 * @param pReason -
	 *            a symbolic name of the CIMException. e.g. CIM_ERR_FAILED,
	 *            CIM_ERR_NOT_FOUND, etc.
	 * @param pExtendedReason1
	 *            first extended reason describing the exception.
	 * @param pExtendedReason2
	 *            second extended reason describing the exception.
	 * @param pExtendedReason3
	 *            third extended reason describing the exception.
	 */
	public CIMException(String pReason, Object pExtendedReason1, Object pExtendedReason2,
			Object pExtendedReason3) {
		this(pReason, new Object[] { pExtendedReason1, pExtendedReason2, pExtendedReason3 });
	}

	/**
	 * Constructs a CIMException with the specified message and one extended
	 * reason parameter.
	 * 
	 * @param pReason -
	 *            a symbolic name of the CIMException. e.g. CIM_ERR_FAILED,
	 *            CIM_ERR_NOT_FOUND, etc.
	 * @param pExtendedReason
	 *            an array of extended reasons describing the exception.
	 */
	public CIMException(String pReason, Object[] pExtendedReason) {
		super(pReason);
		if (pExtendedReason != null) {
			for (int i = 0; i < pExtendedReason.length; i++) {
				if (pExtendedReason[i] instanceof Throwable) {
					this.iCause = (Throwable) pExtendedReason[i];
					break;
				}
			}
		}
		this.iReason = pReason;
		this.iExtendedReason = pExtendedReason;
	}

	/**
	 * Constructs a CIMException with the specified message and one extended
	 * reason parameter.
	 * 
	 * @param pReason -
	 *            a symbolic name of the CIMException. e.g. CIM_ERR_FAILED,
	 *            CIM_ERR_NOT_FOUND, etc.
	 * @param pThrowable
	 *            the original cause for this exception.
	 */
	public CIMException(String pReason, Throwable pThrowable) {
		super(pReason);
		iCause = pThrowable;
		this.iReason = pReason;
		this.iExtendedReason = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#initCause(java.lang.Throwable)
	 */
	public synchronized Throwable initCause(Throwable pCause) {
		if (this.iCause != this) throw new IllegalStateException("Can't overwrite cause");
		if (pCause == this) throw new IllegalArgumentException("Self-causation not permitted");
		this.iCause = pCause;
		return this;
	}

	/**
	 * Gets the reason for this exception.
	 * 
	 * @return The reason
	 */
	public String getID() {
		return iReason;
	}

	/**
	 * Sets a description for this exception.
	 * 
	 * @param pDescription
	 */
	public void setDescription(String pDescription) {
		this.iDescription = pDescription;
	}

	/**
	 * Returns a description for this exception. If no description is specified,
	 * a description is formulated from the reason and extended reasons.
	 * 
	 * @return The description
	 */
	public String getDescription() {
		if (iDescription == null) {

			if (iExtendedReason != null) {
				StringBuffer sb = new StringBuffer();
				int i = 0;

				sb.append('(');
				sb.append(iReason);
				sb.append(": ");
				for (; i < iExtendedReason.length - 1; i++) {
					if (iExtendedReason[i] != null) {
						sb.append(iExtendedReason[i].toString());
					} else {
						sb.append("null");
					}
					sb.append(',');
				}
				sb.append(iExtendedReason[i]);
				sb.append(')');
				sb.insert(0, iReason);
				return sb.toString();
			}
			return iReason;
		}
		return iDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {

		if (iExtendedReason != null) {
			StringBuffer sb = new StringBuffer();
			int i = 0;

			sb.append('(');
			for (; i < iExtendedReason.length - 1; i++) {
				Object o = iExtendedReason[i];
				if (o != null && !(o instanceof Throwable)) {
					sb.append(iExtendedReason[i].toString());
				} else {
					sb.append("null");
				}
				sb.append(',');
			}
			sb.append(iExtendedReason[i]);
			sb.append(')');
			sb.insert(0, super.toString());
			return sb.toString();
		}
		return super.toString();
	}

	/**
	 * Returns the status code that may be returned by a conforming CIM Server
	 * application as the value of the CODE attribute of an <ERROR> subelement
	 * within a <METHODRESPONSE> or <IMETHODRESPONSE>. This method returns the
	 * status code assigned to the CIMException. If the status message is a
	 * standard status code defined by the specification, it will return the
	 * corresponding value. (i.e "CIM_ERR_FAILED" = 1, "CIM_ERR_ACCESS_DENIED"
	 * =2), otherwise it will return -1
	 * 
	 * @return an integer value associated to the cim status code
	 */
	public int getStatusCode() {
		return getStatusCode(iReason);
	}

	/**
	 * Returns the status code that may be returned by a conforming CIM Server
	 * application as the value of the CODE attribute of an <ERROR> subelement
	 * within a <METHODRESPONSE> or <IMETHODRESPONSE>. This method returns the
	 * status code for a given string value. If the status message is a standard
	 * status code defined by the specification, it will return the
	 * corresponding value. (i.e "CIM_ERR_FAILED" = 1, "CIM_ERR_ACCESS_DENIED"
	 * =2), otherwise it will return -1
	 * 
	 * @param pMessage
	 *            The status code string (i.e. "CIM_ERR_FAILED")
	 * 
	 * @return an integer value associated to the cim status code
	 */
	public static int getStatusCode(String pMessage) {
		if (pMessage == null) return -1;

		for (int i = 0; i < CIM_ERROR_NAMES.length; i++) {
			if (pMessage.equalsIgnoreCase(CIM_ERROR_NAMES[i])) return i + 1;
		}
		return -1;
	}

	/**
	 * Returns the string representation of the status code. (i.e. status code 6
	 * will return "CIM_ERR_NOT_FOUND". If the status code is not a standard
	 * status code then "CIM_ERR_FAILED" is returned.
	 * 
	 * @param i
	 *            an integer representing the status code
	 * @return the string representation of the status code
	 */
	public static String getStatusFromCode(int i) {
		return CIM_ERROR_NAMES[((i > 0) && (i <= CIM_ERROR_NAMES.length)) ? i - 1 : 0];
	}

	/**
	 * Specifies the extended reason for this exception.
	 * 
	 * @param pReason
	 *            The extended reason
	 */
	public void setParams(Object[] pReason) {
		this.iExtendedReason = pReason;
	}

	/**
	 * Gets the extended reason for this exception.
	 * 
	 * @return The extended reason
	 */
	public Object[] getParams() {
		return iExtendedReason;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof CIMException)) return false;

		CIMException that = (CIMException) o;
		if (!((this.iDescription == null && that.iDescription == null) || (this.iDescription != null && this.iDescription
				.equals(that.iDescription)))) return false;

		if (!((this.iReason == null && that.iReason == null) || (this.iReason != null && this.iReason
				.equals(that.iReason)))) return false;

		if (!((this.iExtendedReason == null && that.iExtendedReason == null) || (this.iExtendedReason != null && this.iExtendedReason
				.equals(that.iExtendedReason)))) return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() + ((iReason != null) ? iReason.hashCode() : 0) << 16 + ((iDescription != null) ? iDescription
				.hashCode()
				: 0) << 8 + ((iExtendedReason != null) ? iExtendedReason.hashCode() : 0);
	}

	/**
	 * Gets the message associated to this exception.
	 * 
	 * @return The message
	 */
	public String getMessage() {
		if (iCause == null) { return super.getMessage(); }
		return super.getMessage() + "; nested exception is: \n\t" + iCause.toString();
	}

	/**
	 * Gets the thowable object which causes this exception.
	 * 
	 * @return The cause
	 */
	public Throwable getCause() {
		return (iCause == this) ? null : iCause;
	}

}
