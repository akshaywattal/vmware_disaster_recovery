/**
 * CIMRequest.java
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
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObjectPath;

public class CIMRequest extends CIMMessage {

	protected Vector iRequests = new Vector(0);

	protected Vector iParamValue = new Vector(0);

	protected String iMethodName;

	protected CIMObjectPath iPath;

	protected CIMNameSpace iNamespace;

	public CIMRequest() {

	}

	public CIMRequest(String cimVersion, String dtdVersion, String method) {
		super(cimVersion, dtdVersion, method);
	}

	// public void addParamValue(Vector v) {
	// paramValue.addAll(v);
	// }
	public void addParamValue(Object v) {
		if (v instanceof Vector) iParamValue.addAll((Vector) v);
		else iParamValue.add(v);
	}

	public void addRequest(CIMRequest request) {
		iRequests.add(request);
	}

	public String getMethodName() {
		return iMethodName;
	}

	public CIMNameSpace getNameSpace() {
		return iNamespace;
	}

	public CIMObjectPath getObjectPath() {
		return iPath;
	}

	public Vector getParamValue() {
		return iParamValue;
	}

	public void setMethodName(String methodName) {
		this.iMethodName = methodName;
	}

	public void setNameSpace(CIMNameSpace namespace) {
		this.iNamespace = namespace;
	}

	public void setObjectPath(CIMObjectPath path) {
		this.iPath = path;
	}
}
