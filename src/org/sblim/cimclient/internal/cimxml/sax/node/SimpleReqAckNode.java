/**
 * (C) Copyright IBM Corp. 2006, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author : Endre Bak, ebak@de.ibm.com  
 * 
 * Flag       Date        Prog         Description
 * -------------------------------------------------------------------------------
 * 1565892    2006-12-04  ebak         Make SBLIM client JSR48 compliant
 * 1663270    2007-02-19  ebak         Minor performance problems
 * 1660756    2007-02-22  ebak         Embedded object support
 * 1720707    2007-05-17  ebak         Conventional Node factory for CIM-XML SAX parser
 * 2003590    2008-06-30  blaschke-oss Change licensing from CPL to EPL
 * 2524131    2009-01-21  raman_arora  Upgrade client to JDK 1.5 (Phase 1)
 */

package org.sblim.cimclient.internal.cimxml.sax.node;

import org.sblim.cimclient.internal.cimxml.sax.SAXSession;
import org.sblim.cimclient.internal.wbem.CIMError;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <pre>
 * ELEMENT SIMPLEREQACK (ERROR?)
 * ATTLIST SIMPLEREQACK
 *   INSTANCEID CDATA     #REQUIRED
 * </pre>
 */
public class SimpleReqAckNode extends Node {

	private String iInstanceID;

	// ERROR
	private CIMError iCIMError;

	/**
	 * Ctor.
	 */
	public SimpleReqAckNode() {
		super(SIMPLEREQACK);
	}

	@Override
	public void childParsed(Node pChild) {
		this.iCIMError = ((ErrorNode) pChild).getCIMError();
	}

	/**
	 * @param pSession
	 */
	@Override
	public void init(Attributes pAttribs, SAXSession pSession) throws SAXException {
		this.iCIMError = null;
		this.iInstanceID = pAttribs.getValue("INSTANCEID");
		if (this.iInstanceID == null) throw new SAXException(getNodeName()
				+ " node must have INSTANCEID attribute!");
	}

	/**
	 * @param pData
	 */
	@Override
	public void parseData(String pData) {
	// no data
	}

	@Override
	public void testChild(String pNodeNameEnum) throws SAXException {
		if (this.iCIMError != null) throw new SAXException(getNodeName()
				+ " node can have only one ERROR child node!");
		if (pNodeNameEnum != ERROR) throw new SAXException(getNodeName() + " node cannot have "
				+ pNodeNameEnum + " child node!");
	}

	@Override
	public void testCompletness() {
	// no mandatory child node
	}

}
