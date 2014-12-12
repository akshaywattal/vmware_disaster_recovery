/**
 * CIMTransportException.java
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

/**
 * 
 * @author Roberto
 * 
 */
public class CIMTransportException extends CIMException {

	private static final long serialVersionUID = -4015466575351285337L;

	public static final String EXT_ERR_NO_CIMOM = "EXT_ERR_NO_CIMOM";

	public static final String EXT_ERR_UNKNOWN_SERVER = "EXT_ERR_UNKNOWN_SERVER";

	public static final String EXT_ERR_UNABLE_TO_CONNECT = "EXT_ERR_UNABLE_TO_CONNECT";

	public static final String EXT_ERR_TIME_OUT = "EXT_ERR_TIME_OUT";

	public CIMTransportException() {
		super();
	}

	public CIMTransportException(String s) {
		super(s);
	}

	public CIMTransportException(String s, Throwable t) {
		super(s, t);
	}

	public CIMTransportException(String s, Object param) {
		super(s, param);
	}

	public CIMTransportException(String s, Object[] params) {
		super(s, params);
	}

	public CIMTransportException(String s, Object param1, Object param2) {
		super(s, param1, param2);
	}

	public CIMTransportException(String s, Object param1, Object param2, Object param3) {
		super(s, param1, param2, param3);
	}

}
