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
 * 1686000    2007-04-20  ebak         modifyInstance() missing from WBEMClient
 * 1720707    2007-05-17  ebak         Conventional Node factory for CIM-XML SAX parser
 * 2003590    2008-06-30  blaschke-oss Change licensing from CPL to EPL
 * 2524131    2009-01-21  raman_arora  Upgrade client to JDK 1.5 (Phase 1)
 */

package org.sblim.cimclient.internal.cimxml.sax.node;

import org.xml.sax.SAXException;

/**
 * ELEMENT METHODCALL ((LOCALINSTANCEPATH | LOCALCLASSPATH), PARAMVALUE*,
 * RESPONSEDESTINATION?) ATTLIST METHODCALL %CIMName;
 */
public class MethodCallNode extends AbstractMethodCallNode {

	/**
	 * Ctor.
	 */
	public MethodCallNode() {
		super(METHODCALL);
	}

	@Override
	protected void testSpecChild(String pNodeNameEnum) throws SAXException {
		if (pNodeNameEnum == LOCALCLASSPATH || pNodeNameEnum == LOCALINSTANCEPATH) {
			if (this.iPath != null) throw new SAXException(getNodeName()
					+ " node can have only one LOCALINSTANCEPATH or LOCALCLASSPATH child node!");
		} else if (pNodeNameEnum != PARAMVALUE) throw new SAXException(getNodeName()
				+ " node cannot have " + pNodeNameEnum + " child node!");
	}

}
