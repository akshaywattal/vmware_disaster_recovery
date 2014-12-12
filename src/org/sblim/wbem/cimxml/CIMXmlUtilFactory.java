/**
 * CIMXmlUtilFactory.java
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
 * 1676343    2007-02-08  lupusalex    Remove dependency from Xerces
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.cimxml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sblim.wbem.cim.CIMClass;
import org.sblim.wbem.cim.CIMElement;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMMethod;
import org.sblim.wbem.cim.CIMParameter;
import org.sblim.wbem.cim.CIMProperty;
import org.sblim.wbem.cim.CIMQualifier;
import org.sblim.wbem.cim.CIMQualifierType;
import org.sblim.wbem.util.SessionProperties;
import org.sblim.wbem.xml.CIMXMLBuilderImpl;
import org.sblim.wbem.xml.CIMXMLParserImpl;
import org.sblim.wbem.xml.XMLDefaultHandlerImpl;
import org.sblim.wbem.xml.parser.XMLPullParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This is a factory class that create instances of CIMXmlUtil
 */
public class CIMXmlUtilFactory {

	/**
	 * Creates a CIMXmlUtil object.
	 * 
	 * @return a CIMXmlUtil object.
	 */
	public static CIMXmlUtil getCIMXmlUtil() {
		return new CIMXmlUtilImpl();
	}

	public static void main(String[] args) {
		String inst = "<VALUE.NAMEDINSTANCE> <INSTANCENAME CLASSNAME=\"McDATA_Fabric\"><KEYBINDING NAME=\"CreationClassName\"><KEYVALUE VALUETYPE=\"string\">McDATA_Fabric</KEYVALUE></KEYBINDING><KEYBINDING NAME=\"Name\"><KEYVALUE VALUETYPE=\"string\">1000080088A0CC24</KEYVALUE></KEYBINDING></INSTANCENAME><INSTANCE CLASSNAME=\"McDATA_Fabric\"> 	<PROPERTY NAME=\"CreationClassName\" PROPAGATED=\"true\" TYPE=\"string\"> <VALUE>McDATA_Fabric</VALUE></PROPERTY><PROPERTY NAME=\"Name\" PROPAGATED=\"true\" TYPE=\"string\"><VALUE>1000080088A0CC24</VALUE>  </PROPERTY> <PROPERTY NAME=\"ElementName\" PROPAGATED=\"true\" TYPE=\"string\"/>	</INSTANCE></VALUE.NAMEDINSTANCE>";
		CIMXmlUtil util = getCIMXmlUtil();
		try {
			SessionProperties.getGlobalProperties().setXmlParser(0);
			Object o = util.getCIMElement(inst);
			System.out.println(util.CIMElementToXml((CIMElement) o));
			System.out.println(o);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
}

/**
 * Class CIMXmlUtilImpl is the concrete implementation of the CIMXmlUtil
 * interface
 */
class CIMXmlUtilImpl implements CIMXmlUtil {

	private DocumentBuilder iBuilder;

	public CIMXmlUtilImpl() {}

	public synchronized String CIMElementToXml(CIMElement pElement) throws CIMException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			if (iBuilder == null) iBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
		Document doc = iBuilder.newDocument();
		Element elem = null;
		if (pElement instanceof CIMInstance) {
			elem = CIMXMLBuilderImpl.createINSTANCE(doc, null, (CIMInstance) pElement);
		} else if (pElement instanceof CIMClass) {
			elem = CIMXMLBuilderImpl.createCLASS(doc, null, (CIMClass) pElement);
		} else if (pElement instanceof CIMMethod) {
			elem = CIMXMLBuilderImpl.createMETHOD(doc, null, (CIMMethod) pElement, null);
		} else if (pElement instanceof CIMMethod) {
			elem = CIMXMLBuilderImpl.createPARAMETER(doc, null, (CIMParameter) pElement);
		} else if (pElement instanceof CIMProperty) {
			elem = CIMXMLBuilderImpl.createPROPERTY(doc, null, (CIMProperty) pElement);
		} else if (pElement instanceof CIMQualifier) {
			elem = CIMXMLBuilderImpl.createQUALIFIER(doc, null, (CIMQualifier) pElement);
		} else if (pElement instanceof CIMQualifierType) {
			elem = CIMXMLBuilderImpl.createQUALIFIER_DECLARATION(doc, null, (CIMQualifierType) pElement);
		}

		doc.appendChild(elem); // root element
		try {
			ByteArrayOutputStream byteOS = new ByteArrayOutputStream();

			CimXmlSerializer.serialize(byteOS, doc, true);
			return byteOS.toString();
		} catch (IOException e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	public synchronized CIMElement getCIMElement(String pXmlString) throws IOException, SAXException {

		int parser = SessionProperties.getGlobalProperties().getXmlParser();
		if (parser == SessionProperties.DOM_PARSER) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			try {
				if (iBuilder == null) iBuilder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new SAXException(e);
			}

			Document doc = iBuilder.parse(new InputSource(new StringReader(pXmlString)));
			Object o = CIMXMLParserImpl.parseObject(doc.getDocumentElement());
			if (o instanceof CIMElement) return (CIMElement) o;
			return null;
		}

		XMLDefaultHandlerImpl hndlr = new XMLDefaultHandlerImpl();

		if (parser == SessionProperties.SAX_PARSER) {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			try {
				SAXParser saxParser = factory.newSAXParser();
				saxParser.parse(new InputSource(new StringReader(pXmlString)), hndlr);
			} catch (Exception e) {
				Logger logger = SessionProperties.getGlobalProperties().getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "exception while parsing the XML with XML parser", e);
				}
			}

		} else if (parser == SessionProperties.PULL_PARSER) {
			XMLPullParser pullParser = new XMLPullParser(new StringReader(pXmlString));
			hndlr.parse(pullParser);
		}
		Vector o = hndlr.getObjects();
		if (o != null && o.size() > 0 && (o.elementAt(0) instanceof CIMElement)) { return (CIMElement) o
				.elementAt(0); }

		return null;
	}
}
