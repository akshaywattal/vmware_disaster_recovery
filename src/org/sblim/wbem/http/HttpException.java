/**
 * HttpException.java
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

package org.sblim.wbem.http;

import java.io.IOException;

public class HttpException extends IOException {

	private static final long serialVersionUID = 934925248736106630L;

	int iStatus;

	public HttpException() {
		this(-1, null, null);
	}

	public HttpException(String reason) {
		this(-1, reason, null);
	}

	public HttpException(int status, String reason) {
		this(status, reason, null);
	}

	public HttpException(int status, String reason, String cimError) {
		super(reason);
		iStatus = status;
	}

	public int getStatus() {
		return iStatus;
	}

	public String toString() {
		return super.toString() + "(status:" + iStatus + ")";
	}
}
