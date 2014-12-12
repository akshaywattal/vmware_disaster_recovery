/**
 * (C) Copyright IBM Corp. 2006, 2012
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
 * 1686000    2007-04-20  ebak         modifyInstance() missing from WBEMClient
 * 1720707    2007-05-17  ebak         Conventional Node factory for CIM-XML SAX parser
 * 2003590    2008-06-30  blaschke-oss Change licensing from CPL to EPL
 * 2524131    2009-01-21  raman_arora  Upgrade client to JDK 1.5 (Phase 1)
 * 3511454    2012-03-27  blaschke-oss SAX nodes not reinitialized properly
 */

package org.sblim.cimclient.internal.cimxml.sax.node;

import org.sblim.cimclient.internal.cimxml.sax.SAXSession;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * ELEMENT SIMPLEREQ (IMETHODCALL | METHODCALL)
 */
public class SimpleReqNode extends AbstractMessageNode {

	private AbstractMethodCallNode iMethodCallNode;

	/**
	 * Ctor.
	 */
	public SimpleReqNode() {
		super(SIMPLEREQ);
	}

	public void addChild(Node pChild) {
		this.iMethodCallNode = (AbstractMethodCallNode) pChild;
	}

	/**
	 * @param pAttribs
	 * @param pSession
	 */
	@Override
	public void init(Attributes pAttribs, SAXSession pSession) {
		this.iMethodCallNode = null;
		// no attributes
	}

	/**
	 * @param pData
	 */
	@Override
	public void parseData(String pData) {
	// no data
	}

	/**
	 * @param pNodeNameEnum
	 */
	@Override
	public void testChild(String pNodeNameEnum) throws SAXException {
		if (this.iMethodCallNode != null) throw new SAXException(
				"SIMPLEREQ node can have only one child node!");
	}

	@Override
	public void testCompletness() throws SAXException {
		if (this.iMethodCallNode == null) throw new SAXException(
				"SIMPLEREQ node must have a child node!");
	}

	/**
	 * getAbstractMethodCallNode
	 * 
	 * @return AbstractMethodCallNode
	 */
	public AbstractMethodCallNode getAbstractMethodCallNode() {
		return this.iMethodCallNode;
	}

}
