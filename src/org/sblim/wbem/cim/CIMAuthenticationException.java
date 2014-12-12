/**
 * CIMAuthenticationException.java
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

import org.sblim.wbem.cim.CIMException;

/**
 * Describes exceptional events associated with the authentication of the client.
 */
public class CIMAuthenticationException extends CIMException {

	private static final long serialVersionUID = 9182648226594887549L;
	
	public static final String EXT_ERR_PROXY_AUTHENTICATION = "EXT_ERR_PROXY_AUTHENTICATION";
	public static final String EXT_ERR_AUTHENTICATION = "EXT_ERR_AUTHENTICATION";
	
	public CIMAuthenticationException() {
		super();
	}
	
	public CIMAuthenticationException(String s) {
		super(s);
	}

	public CIMAuthenticationException(String s, Throwable t) {
		super(s,t);
	}

	public CIMAuthenticationException(String s, Object param) {
		super(s, param);
	}

	public CIMAuthenticationException(String s, Object[] params) {
		super(s, params);
	}

	public CIMAuthenticationException(String s, Object param1, Object param2) {
		super(s, param1, param2);
	}

	public CIMAuthenticationException(String s, Object param1, Object param2, Object param3) {
		super(s, param1, param2, param3);
	}

}
