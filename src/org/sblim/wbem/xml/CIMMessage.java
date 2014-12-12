/**
 * CIMMessage.java
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

import java.util.Hashtable;
import org.w3c.dom.Document;

class CIMMessage {

	protected Document iDoc;

	protected Hashtable iElements;

	protected String iCimVersion;

	protected String iDtdVersion;

	protected String iId;

	protected String iProtocolVersion;

	protected String iMethod;

	protected boolean iIsCIMExport = false;

	protected boolean iIsSimple = false;

	protected boolean iIsRequest = false;

	protected CIMMessage() {}

	public CIMMessage(String pCimVersion, String pDtdVersion, String pMethod) {
		this.iCimVersion = pCimVersion;
		this.iDtdVersion = pDtdVersion;
		this.iMethod = pMethod;
	}

	public String getCIMVersion() {
		return iCimVersion;
	}

	public String getDTDVersion() {
		return iDtdVersion;
	}

	public boolean isCIMOperation() {
		return !iIsCIMExport;
	}

	public boolean isCIMExport() {
		return iIsCIMExport;
	}

	public void setMethod(String pMethod) {

		this.iMethod = pMethod;
		iIsCIMExport = (pMethod.toUpperCase().endsWith("EXPREQ") || pMethod.toUpperCase().endsWith(
				"EXPRSP"));
		iIsRequest = (pMethod.toUpperCase().endsWith("REQ"));
		iIsSimple = pMethod.toUpperCase().startsWith("SIMPLE");
	}

	public void setCIMVersion(String pCimVersion) {
		this.iCimVersion = pCimVersion;
	}

	public void setDTDVersion(String pDtdVersion) {
		this.iDtdVersion = pDtdVersion;
	}

	public void setIsRequest(boolean pValue) {
		this.iIsRequest = pValue;
	}

	public String getId() {
		return iId;
	}

	public String getProtocolVersion() {
		return iProtocolVersion;
	}
}
