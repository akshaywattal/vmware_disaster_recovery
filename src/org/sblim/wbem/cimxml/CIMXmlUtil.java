/**
 * CIMXmlUtil.java
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

package org.sblim.wbem.cimxml;

import java.io.IOException;


import org.sblim.wbem.cim.CIMElement;
import org.sblim.wbem.cim.CIMException;
import org.xml.sax.SAXException;

/**
 * This interface provides the methods used to convert CIMElements to CIM-XML representation,
 * and CIM-XML representation back to CIMElements. The implementation of this instance is
 * not thread safe.
 * @author Roberto
 */
public interface CIMXmlUtil {
	/**
	 * Returns a string representing the CIM-XML of a CIMElement.
	 * @param obj the element to be converted into XML
	 * @return the XML represented as String
	 * @throws CIMException On any unrecoverable error
	 */
	public String CIMElementToXml(CIMElement obj) throws CIMException;
	
	/**
	 * Construct a Java object representing the CIMElement from the XML representation.
	 * @param str XML representation of the CIMElement
	 * @return Java Object
	 * @throws CIMException if any CIM error occurs
	 * @throws IOException if any IO error occurs
	 * @throws SAXException if any parse error occurs
	 */
	public CIMElement getCIMElement(String str) throws CIMException, IOException, SAXException;
}
