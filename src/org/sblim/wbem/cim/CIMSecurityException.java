/**
 * CIMSecurityException.java
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
 * 1535756    2006-08-08  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.cim;


public class CIMSecurityException extends CIMException {
	
	private static final long serialVersionUID = -1519781860053084680L;

	public static final String EXT_ERR_CANNOT_ASSUME_ROLE = "EXT_ERR_CANNOT_ASSUME_ROLE";
	public static final String EXT_ERR_CHECKSUME_ERROR = "EXT_ERR_CHECKSUME_ERROR";
	public static final String EXT_ERR_ACCESS_DENIED = "EXT_ERR_ACCESS_DENIED";
	public static final String EXT_ERR_INVALID_CREDENTIAL = "EXT_ERR_INVALID_CREDENTIAL";
	public static final String EXT_ERR_INVALID_DATA = "EXT_ERR_INVALID_DATA"; 
	public static final String EXT_ERR_NO_SUCH_PRINCIPAL = "EXT_ERR_NO_SUCH_PRINCIPAL";
	public static final String EXT_ERR_NO_SUCH_ROLE = "EXT_ERR_NO_SUCH_ROLE";
	public static final String EXT_ERR_NO_SUCH_SESSION = "EXT_ERR_NO_SUCH_SESSION";
	public static final String EXT_ERR_NOT_HELLO = "EXT_ERR_NOT_HELLO";
	public static final String EXT_ERR_NOT_RESPONSE = "EXT_ERR_NOT_RESPONSE";

	public final static String EXT_ERR_INVALID_TRUSTSTORE = "EXT_ERR_INVALID_TRUSTSTORE";
	public final static String EXT_ERR_INVALID_CERTIFICATE = "EXT_ERR_INVALID_CERTIFICATE";
	
	public CIMSecurityException() {
		super();
	}
	
	public CIMSecurityException(String s) {
		super(s);
	}

	public CIMSecurityException(String s, Throwable t) {
		super(s,t);
	}

	public CIMSecurityException(String s, Object param) {
		super(s, param);
	}

	public CIMSecurityException(String s, Object[] params) {
		super(s, params);
	}

	public CIMSecurityException(String s, Object param1, Object param2) {
		super(s, param1, param2);
	}

	public CIMSecurityException(String s, Object param1, Object param2, Object param3) {
		super(s, param1, param2, param3);
	}
}
