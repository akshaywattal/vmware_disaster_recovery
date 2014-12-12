/**
 * CIMXMLParserImpl.java
 *
 * (C) Copyright IBM Corp. 2005, 2012
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author : Roberto Pineiro, IBM, roberto.pineiro@us.ibm.com  
 * @author : Chung-hao Tan, IBM, chungtan@us.ibm.com
 * 
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1464860    2006-05-15  lupusalex    No default value for VALUETYPE assumed
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1547910    2006-09-05  ebak         parseIMETHODCALL() CIMObjectPath parsing problem
 * 1547908    2006-09-05  ebak         parseKEYBINDING() creates incorrect reference type
 * 1545915    2006-09-05  ebak         Wrong parsing of IMETHODCALL request
 * 1660756    2007-03-02  ebak         Embedded object support
 * 1689085    2007-04-10  ebak         Embedded object enhancements for Pegasus
 * 1669961    2006-04-16  lupusalex    CIMTypedElement.getType() =>getDataType()
 * 1712656    2007-05-04  ebak         Correct type identification for SVC CIMOM
 * 1714878    2007-05-08  ebak         Empty string property values are parsed as nulls
 * 1737141    2007-06-18  ebak         Sync up with JSR48 evolution
 * 1769504	  2007-08-15  ebak         Type identification for VALUETYPE="numeric"
 * 1783288    2007-09-10  ebak         CIMClass.isAssociation() not working for retrieved classes.
 * 1820763    2007-10-29  ebak         Supporting the EmbeddedInstance qualifier
 * 1827728    2007-11-20  ebak         rework: embeddedInstances: attribute EmbeddedObject not set
 * 1848607    2007-12-11  ebak         Strict EmbeddedObject types
 * 1944875    2008-05-29  blaschke-oss Indications with embedded objects are not accepted
 * 2003590    2008-06-30  blaschke-oss Change licensing from CPL to EPL
 * 2210455    2008-10-30  blaschke-oss Enhance javadoc, fix potential null pointers
 * 2524131    2009-01-21  raman_arora  Upgrade client to JDK 1.5 (Phase 1)
 * 2531371    2009-02-10  raman_arora  Upgrade client to JDK 1.5 (Phase 2)
 * 2714989    2009-03-26  blaschke-oss Code cleanup from redundant null check et al
 * 2763216    2009-04-14  blaschke-oss Code cleanup: visible spelling/grammar errors
 * 2797550    2009-06-01  raman_arora  JSR48 compliance - add Java Generics
 * 2823494    2009-08-03  rgummada     Change Boolean constructor to static
 * 2860081    2009-09-17  raman_arora  Pull Enumeration Feature (DOM Parser)
 * 2957387    2010-03-03  blaschke-oss EmbededObject XML attribute must not be all uppercases
 * 3023143    2010-07-01  blaschke-oss CIMXMLParserImpl uses # constructor instead of valueOf
 * 3027479    2010-07-09  blaschke-oss Dead store to local variable
 * 3027615    2010-07-12  blaschke-oss Use CLASS_ARRAY_T instead of new CIMDataType(CLASS,0)
 * 3293248    2011-05-03  blaschke-oss Support for CIM_ERROR instances within ERROR
 * 3297028    2011-05-03  blaschke-oss Instances contain CIMClassProperty with DOM parser
 * 3411944    2011-09-20  blaschke-oss createJavaObject exception with hex uint
 * 3513353    2012-03-30  blaschke-oss TCK: CIMDataType arrays must have length >= 1
 * 3513349    2012-03-31  blaschke-oss TCK: CIMDataType must not accept null string
 * 3466280    2012-04-23  blaschke-oss get instance failure for CIM_IndicationSubscription
 * 3521119    2012-04-24  blaschke-oss JSR48 1.0.0: remove CIMObjectPath 2/3/4-parm ctors
 * 3526679    2012-05-14  blaschke-oss DOM parser ignores ERROR node CODE
 */

package org.sblim.cimclient.internal.cimxml;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;

import javax.cim.CIMArgument;
import javax.cim.CIMClass;
import javax.cim.CIMClassProperty;
import javax.cim.CIMDataType;
import javax.cim.CIMDateTime;
import javax.cim.CIMDateTimeAbsolute;
import javax.cim.CIMDateTimeInterval;
import javax.cim.CIMFlavor;
import javax.cim.CIMInstance;
import javax.cim.CIMMethod;
import javax.cim.CIMNamedElementInterface;
import javax.cim.CIMObjectPath;
import javax.cim.CIMParameter;
import javax.cim.CIMProperty;
import javax.cim.CIMQualifier;
import javax.cim.CIMQualifierType;
import javax.cim.CIMScope;
import javax.cim.UnsignedInteger16;
import javax.cim.UnsignedInteger32;
import javax.cim.UnsignedInteger64;
import javax.cim.UnsignedInteger8;
import javax.wbem.WBEMException;

import org.sblim.cimclient.internal.cim.CIMHelper;
import org.sblim.cimclient.internal.cim.CIMQualifiedElementInterfaceImpl;
import org.sblim.cimclient.internal.logging.LogAndTraceBroker;
import org.sblim.cimclient.internal.util.MOF;
import org.sblim.cimclient.internal.util.WBEMConfiguration;
import org.sblim.cimclient.internal.util.XMLHostStr;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * Class CIMXMLParserImpl is the main class of CIM-XML DOM parser.
 * 
 */
public class CIMXMLParserImpl {

	/*
	 * ebak: local object path - should be used by parseLOCALCLASSPATH(),
	 * parseLOCALINSTANCEPATH(), parseCLASSNAME(), parseINSTANCENAME(),
	 * parseCLASS() and parseINSTANCE()
	 */
	private static LocalPathBuilder cLocalPathBuilder = new LocalPathBuilder(null);

	/**
	 * setLocalObjectPath
	 * 
	 * @param pLocalOp
	 *            - empty fields of parsed objectpaths will be substituted by
	 *            fields coming from this parameter
	 */
	public static void setLocalObjectPath(CIMObjectPath pLocalOp) {
		cLocalPathBuilder = new LocalPathBuilder(pLocalOp);
	}

	/*
	 * / local object path
	 */

	/**
	 * parseCIM
	 * 
	 * @param pCimE
	 * @return CIMMessage
	 * @throws CIMXMLParseException
	 */
	public static CIMMessage parseCIM(Element pCimE) throws CIMXMLParseException {
		// <!ELEMENT CIM (MESSAGE|DECLARATION)>
		// <!ATTLIST CIM %CIMVERSION;%DTDVERSION;>
		Attr cim_cimversionA = (Attr) searchAttribute(pCimE, "CIMVERSION");
		String cimversion = cim_cimversionA.getNodeValue();

		Attr cim_dtdversionA = (Attr) searchAttribute(pCimE, "DTDVERSION");
		String dtdversion = cim_dtdversionA.getNodeValue();

		// MESSAGE
		Element messageE = (Element) searchFirstNode(pCimE, "MESSAGE");
		if (messageE != null) {
			CIMMessage message = parseMESSAGE(cimversion, dtdversion, messageE);
			message.setCIMVersion(cimversion);
			message.setDTDVersion(dtdversion);
			return message;
		}

		// TODO : DECLARATION

		throw new CIMXMLParseException();
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	// Value Elements
	// ////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * parseVALUE - supports the non-standard TYPE attribute
	 * 
	 * @param pValueE
	 * @return TypedValue, type is null if no TYPE attribute was found, the
	 *         value is always String, the caller method have to convert it.
	 * @throws CIMXMLParseException
	 */
	public static TypedValue parseVALUE(Element pValueE) throws CIMXMLParseException {
		// <! ELEMENT VALUE (#PCDATA)>

		String typeStr = attribute(pValueE, "TYPE");
		CIMDataType type = typeStr == null ? null : parseScalarTypeStr(typeStr);
		Text t = (Text) pValueE.getFirstChild();
		// ebak: empty VALUE element is parsed as empty String
		String valueStr;
		if (t != null) {
			String nodeValue = t.getNodeValue();
			valueStr = nodeValue == null ? "" : nodeValue;
		} else {
			valueStr = "";
		}
		return new TypedValue(type, valueStr);
	}

	/**
	 * parseVALUEARRAY - supports the non-standard TYPE attribute
	 * 
	 * @param pValueArrayE
	 * @return TypedValue, type is null if no TYPE attribute was found, the
	 *         value is always String[], the caller method have to convert it.
	 * @throws CIMXMLParseException
	 */
	@SuppressWarnings("null")
	public static TypedValue parseVALUEARRAY(Element pValueArrayE) throws CIMXMLParseException {
		// <! ELEMENT VALUE.ARRAY (VALUE*)>
		String typeStr = attribute(pValueArrayE, "TYPE");
		CIMDataType type = typeStr == null ? null : parseArrayTypeStr(typeStr);
		Element[] valueEA = searchNodes(pValueArrayE, "VALUE");
		String[] strA = new String[valueEA == null ? 0 : valueEA.length];
		for (int i = 0; i < strA.length; i++) {
			Element valueE = valueEA[i];
			String valueStr = (String) parseVALUE(valueE).getValue();
			strA[i] = valueStr;
		}
		return new TypedValue(type, strA);
	}

	/**
	 * parseVALUEREFERENCE
	 * 
	 * @param pValuereferenceE
	 * @return CIMObjectPath
	 * @throws CIMXMLParseException
	 */
	public static CIMObjectPath parseVALUEREFERENCE(Element pValuereferenceE)
			throws CIMXMLParseException {
		// <!ELEMENT VALUE.REFERENCE
		// (CLASSPATH|LOCALCLASSPATH|CLASSNAME|INSTANCEPATH|LOCALINSTANCEPATH|INSTANCENAME)>

		// CLASSPATH
		Element classpathE = (Element) searchFirstNode(pValuereferenceE, "CLASSPATH");
		if (classpathE != null) {
			CIMObjectPath op = parseCLASSPATH(classpathE);
			return op;
		}

		// LOCALCLASSPATH
		Element localclasspathE = (Element) searchFirstNode(pValuereferenceE, "LOCALCLASSPATH");
		if (localclasspathE != null) {
			CIMObjectPath op = parseLOCALCLASSPATH(localclasspathE);
			return op;
		}

		// CLASSNAME
		Element classnameE = (Element) searchFirstNode(pValuereferenceE, "CLASSNAME");
		if (classnameE != null) {
			CIMObjectPath op = parseCLASSNAME(classnameE);
			if (op != null && op.getNamespace() != null) {
				// LocalPathBuilder includes default namespace in CLASSNAME
				// elements, needs to be stripped
				op = new CIMObjectPath(op.getScheme(), op.getHost(), op.getPort(), null, op
						.getObjectName(), op.getKeys(), op.getXmlSchemaName());
			}
			return op;
		}

		// INSTANCEPATH
		Element instancepathE = (Element) searchFirstNode(pValuereferenceE, "INSTANCEPATH");
		if (instancepathE != null) {
			CIMObjectPath op = parseINSTANCEPATH(instancepathE);
			return op;
		}

		// LOCALINSTANCEPATH
		Element localinstancepathE = (Element) searchFirstNode(pValuereferenceE,
				"LOCALINSTANCEPATH");
		if (localinstancepathE != null) {
			CIMObjectPath op = parseLOCALINSTANCEPATH(localinstancepathE);
			return op;
		}

		// INSTANCENAME
		Element instancenameE = (Element) searchFirstNode(pValuereferenceE, "INSTANCENAME");
		if (instancenameE != null) {
			CIMObjectPath op = parseINSTANCENAME(instancenameE);
			if (op != null && op.getNamespace() != null) {
				// LocalPathBuilder includes default namespace in INSTANCENAME
				// elements, needs to be stripped
				op = new CIMObjectPath(op.getScheme(), op.getHost(), op.getPort(), null, op
						.getObjectName(), op.getKeys(), op.getXmlSchemaName());
			}
			return op;
		}

		throw new CIMXMLParseException();
	}

	/**
	 * parseVALUEREFARRAY
	 * 
	 * @param pValueRefArrayE
	 * @return CIMObjectPath[]
	 * @throws CIMXMLParseException
	 */
	@SuppressWarnings("null")
	public static CIMObjectPath[] parseVALUEREFARRAY(Element pValueRefArrayE)
			throws CIMXMLParseException {
		// <! ELEMENT VALUE.REFARRAY (VALUE.REFERENCE*)>
		Element[] valueRefElementA = searchNodes(pValueRefArrayE, "VALUE.REFERENCE");
		CIMObjectPath[] resA = new CIMObjectPath[valueRefElementA == null ? 0
				: valueRefElementA.length];
		for (int i = 0; i < resA.length; i++) {
			Element valueRefE = valueRefElementA[i];
			resA[i] = parseVALUEREFERENCE(valueRefE);
		}
		return resA;
	}

	/**
	 * parseVALUEOBJECT
	 * 
	 * @param pValueObjectE
	 * @return CIMNamedElementInterface (CIMClass|CIMInstance)
	 * @throws CIMXMLParseException
	 */
	public static CIMNamedElementInterface parseVALUEOBJECT(Element pValueObjectE)
			throws CIMXMLParseException {
		// <! ELEMENT VALUE.OBJECT (CLASS|INSTANCE)>

		// CLASS
		Element classE = (Element) searchFirstNode(pValueObjectE, "CLASS");
		if (classE != null) {
			CIMClass obj = parseCLASS(classE);
			return obj;
		}

		// INSTANCE
		Element instanceE = (Element) searchFirstNode(pValueObjectE, "INSTANCE");
		if (instanceE != null) {
			CIMInstance obj = parseINSTANCE(instanceE);
			return obj;
		}

		throw new CIMXMLParseException();
	}

	/**
	 * parseVALUENAMEDINSTANCE
	 * 
	 * @param pValueNamedInstanceE
	 * @return CIMInstance
	 * @throws CIMXMLParseException
	 */
	public static CIMInstance parseVALUENAMEDINSTANCE(Element pValueNamedInstanceE)
			throws CIMXMLParseException {
		// <! ELEMENT VALUE.NAMEDINSTANCE (INSTANCENAME,INSTANCE)>

		// INSTANCENAME
		Element instancenameE = (Element) searchFirstNode(pValueNamedInstanceE, "INSTANCENAME");
		if (instancenameE == null) { throw new CIMXMLParseException(); }
		CIMObjectPath op = parseINSTANCENAME(instancenameE);

		// INSTANCE
		Element instanceE = (Element) searchFirstNode(pValueNamedInstanceE, "INSTANCE");
		if (instanceE == null) { throw new CIMXMLParseException(); }
		CIMInstance inst = parseINSTANCE(instanceE, op); // BB mod
		return inst;
	}

	/**
	 * parseVALUEINSTANCEWITHPATH
	 * 
	 * @param pValueNamedInstanceE
	 * @return CIMInstance
	 * @throws CIMXMLParseException
	 */
	public static CIMInstance parseVALUEINSTANCEWITHPATH(Element pValueNamedInstanceE)
			throws CIMXMLParseException {
		// <! ELEMENT VALUE.INSTANCEWITHPATH (INSTANCEPATH,INSTANCE)>

		// INSTANCEPATH
		Element instancepathE = (Element) searchFirstNode(pValueNamedInstanceE, "INSTANCEPATH");
		if (instancepathE == null) { throw new CIMXMLParseException(); }
		CIMObjectPath op = parseINSTANCEPATH(instancepathE);

		// INSTANCE
		Element instanceE = (Element) searchFirstNode(pValueNamedInstanceE, "INSTANCE");
		if (instanceE == null) { throw new CIMXMLParseException(); }
		CIMInstance inst = parseINSTANCE(instanceE, op); // BB mod
		return inst;
	}

	/**
	 * parseVALUENAMEDOBJECT
	 * 
	 * @param pValueNamedObjectE
	 * @return CIMNamedElementInterface
	 * @throws CIMXMLParseException
	 */
	public static CIMNamedElementInterface parseVALUENAMEDOBJECT(Element pValueNamedObjectE)
			throws CIMXMLParseException {
		// <! ELEMENT VALUE.NAMEDOBJECT (CLASS|(INSTANCENAME,INSTANCE))>

		// CLASS
		Element classE = (Element) searchFirstNode(pValueNamedObjectE, "CLASS");
		if (classE != null) {
			CIMClass obj = parseCLASS(classE);
			return obj;
		}

		// INSTANCENAME
		Element instancenameE = (Element) searchFirstNode(pValueNamedObjectE, "INSTANCENAME");
		if (instancenameE != null) {
			CIMObjectPath op = parseINSTANCENAME(instancenameE);

			// INSTANCE
			Element instanceE = (Element) searchFirstNode(pValueNamedObjectE, "INSTANCE");
			if (instanceE == null) { throw new CIMXMLParseException(); }
			CIMInstance inst = parseINSTANCE(instanceE, op); // BB mod
			return inst;
		}

		throw new CIMXMLParseException();
	}

	/**
	 * parseVALUEOBJECTWITHPATH
	 * 
	 * @param pValueObjectWithPathE
	 * @return CIMNamedElementInterface
	 * @throws CIMXMLParseException
	 */
	public static CIMNamedElementInterface parseVALUEOBJECTWITHPATH(Element pValueObjectWithPathE)
			throws CIMXMLParseException {
		// <! ELEMENT VALUE.OBJECTWITHPATH
		// ((CLASSPATH,CLASS)|(INSTANCEPATH,INSTANCE))>

		// CLASSPATH
		Element classpathE = (Element) searchFirstNode(pValueObjectWithPathE, "CLASSPATH");
		if (classpathE != null) {
			// CIMObjectPath op = parseCLASSPATH(classpathE);

			// CLASS
			Element classE = (Element) searchFirstNode(pValueObjectWithPathE, "CLASS");
			if (classE == null) { throw new CIMXMLParseException(); }
			// TODO parse CLASS with ObjectPath
			CIMClass obj = parseCLASS(classE);
			return obj;
		}

		// INSTANCEPATH
		Element instancepathE = (Element) searchFirstNode(pValueObjectWithPathE, "INSTANCEPATH");
		if (instancepathE != null) {
			CIMObjectPath op = parseINSTANCEPATH(instancepathE);

			// INSTANCE
			Element instanceE = (Element) searchFirstNode(pValueObjectWithPathE, "INSTANCE");
			if (instanceE == null) { throw new CIMXMLParseException(); }
			CIMInstance inst = parseINSTANCE(instanceE, op); // BB mod
			return inst;
		}

		throw new CIMXMLParseException();
	}

	/**
	 * parseVALUEOBJECTWITHLOCALPATH
	 * 
	 * @param pValueObjectWithLocalPathE
	 * @return CIMNamedElementInterface
	 * @throws CIMXMLParseException
	 */
	public static CIMNamedElementInterface parseVALUEOBJECTWITHLOCALPATH(
			Element pValueObjectWithLocalPathE) throws CIMXMLParseException {
		// <! ELEMENT VALUE.OBJECTWITHPATH
		// ((LOCALCLASSPATH,CLASS)|(LOCALINSTANCEPATH,INSTANCE))>

		// LOCALCLASSPATH
		Element localclasspathE = (Element) searchFirstNode(pValueObjectWithLocalPathE,
				"LOCALCLASSPATH");
		if (localclasspathE != null) {
			// CIMObjectPath op = parseLOCALCLASSPATH(localclasspathE);

			// CLASS
			Element classE = (Element) searchFirstNode(pValueObjectWithLocalPathE, "CLASS");
			if (classE == null) { throw new CIMXMLParseException(); }
			// TODO parse class with path
			CIMClass obj = parseCLASS(classE);
			return obj;
		}

		// LOCALINSTANCEPATH
		Element localinstancepathE = (Element) searchFirstNode(pValueObjectWithLocalPathE,
				"LOCALINSTANCEPATH");
		if (localinstancepathE != null) {
			CIMObjectPath op = parseLOCALINSTANCEPATH(localinstancepathE);

			// INSTANCE
			Element instanceE = (Element) searchFirstNode(pValueObjectWithLocalPathE, "INSTANCE");
			if (instanceE == null) { throw new CIMXMLParseException(); }
			CIMInstance inst = parseINSTANCE(instanceE, op); // BB mod
			return inst;
		}

		throw new CIMXMLParseException();
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	// Naming and Location elements
	// ////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * parseNAMESPACEPATH
	 * 
	 * @param pNameSpacePathE
	 * @return CIMObjectPath
	 * @throws CIMXMLParseException
	 */
	public static CIMObjectPath parseNAMESPACEPATH(Element pNameSpacePathE)
			throws CIMXMLParseException {
		// <!ELEMENT NAMESPACEPATH (HOST,LOCALNAMESPACEPATH)>
		// HOST
		Element hostE = (Element) searchFirstNode(pNameSpacePathE, "HOST");
		if (hostE == null) { throw new CIMXMLParseException(); }
		XMLHostStr xmlHostStr = new XMLHostStr(parseHOST(hostE));
		// LOCALNAMESPACE
		Element localnamespacepathE = (Element) searchFirstNode(pNameSpacePathE,
				"LOCALNAMESPACEPATH");
		if (localnamespacepathE == null) { throw new CIMXMLParseException(); }
		String nameSpace = parseLOCALNAMESPACEPATH(localnamespacepathE);
		/*
		 * CIMObjectPath( String scheme, String host, String port, String
		 * namespace, String objectName, CIMProperty[] keys )
		 */
		return new CIMObjectPath(xmlHostStr.getProtocol(), xmlHostStr.getHost(), xmlHostStr
				.getPort(), nameSpace, null, null);
	}

	/**
	 * parseLOCALNAMESPACEPATH
	 * 
	 * @param pLocalNameSpaceE
	 * @return String
	 * @throws CIMXMLParseException
	 */
	public static String parseLOCALNAMESPACEPATH(Element pLocalNameSpaceE)
			throws CIMXMLParseException {
		// <!ELEMENT LOCALNAMESPACE (NAMESPACE+))>

		Element[] nameSpaceElementA = searchNodes(pLocalNameSpaceE, "NAMESPACE");
		if (nameSpaceElementA == null) {
			if (cLocalPathBuilder == null) throw new CIMXMLParseException(
					"NAMESPACE child element is mandatory for LOCALNAMESPACEPATH element!");
			return cLocalPathBuilder.getBasePath().getNamespace();
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nameSpaceElementA.length; i++) {
			Element namespaceE = nameSpaceElementA[i];
			String s = parseNAMESPACE(namespaceE);
			if (i > 0) sb.append("/" + s);
			else sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * parseHOST
	 * 
	 * @param pHostE
	 * @return String
	 */
	public static String parseHOST(Element pHostE) {
		// <!ELEMENT HOST (#PCDATA)>

		Text valueT = (Text) searchFirstChild(pHostE);
		String host = valueT.getNodeValue();
		return host;
	}

	/**
	 * parseNAMESPACE
	 * 
	 * @param pNameSpaceE
	 * @return String
	 */
	public static String parseNAMESPACE(Element pNameSpaceE) {
		// <!ELEMENT NAMESPACE EMPTY>
		// <!ATTLIST NAMESPACE %NAME;>

		Attr namespace_nameA = (Attr) searchAttribute(pNameSpaceE, "NAME");
		String n = namespace_nameA.getValue();
		return n;
	}

	/**
	 * parseCLASSPATH
	 * 
	 * @param pClassPathE
	 * @return CIMObjectPath
	 * @throws CIMXMLParseException
	 */
	public static CIMObjectPath parseCLASSPATH(Element pClassPathE) throws CIMXMLParseException {
		// <!ELEMENT CLASSPATH (NAMESPACEPATH,CLASSNAME)>

		// NAMESPACEPATH
		Element namespacepathE = (Element) searchFirstNode(pClassPathE, "NAMESPACEPATH");
		if (namespacepathE == null) { throw new CIMXMLParseException(); }
		CIMObjectPath nsPath = parseNAMESPACEPATH(namespacepathE);
		// CLASSNAME
		Element classnameE = (Element) searchFirstNode(pClassPathE, "CLASSNAME");
		if (classnameE == null) { throw new CIMXMLParseException(); }
		String className = parseClassName(classnameE);
		/*
		 * CIMObjectPath( String scheme, String host, String port, String
		 * namespace, String objectName, CIMProperty[] keys )
		 */
		return new CIMObjectPath(nsPath.getScheme(), nsPath.getHost(), nsPath.getPort(), nsPath
				.getNamespace(), className, null);
	}

	/**
	 * parseLOCALCLASSPATH
	 * 
	 * @param pClassPathE
	 * @return CIMObjectPath
	 * @throws CIMXMLParseException
	 */
	public static CIMObjectPath parseLOCALCLASSPATH(Element pClassPathE)
			throws CIMXMLParseException {
		// <!ELEMENT LOCALCLASSPATH (LOCALNAMESPACEPATH,CLASSNAME)>

		// NAMESPACEPATH
		Element localnamespacepathE = (Element) searchFirstNode(pClassPathE, "LOCALNAMESPACEPATH");
		if (localnamespacepathE == null) { throw new CIMXMLParseException(); }
		String nameSpace = parseLOCALNAMESPACEPATH(localnamespacepathE);

		// CLASSNAME
		Element classnameE = (Element) searchFirstNode(pClassPathE, "CLASSNAME");
		if (classnameE == null) { throw new CIMXMLParseException(); }
		CIMObjectPath op = parseCLASSNAME(classnameE);
		return cLocalPathBuilder.build(op.getObjectName(), nameSpace);
	}

	/**
	 * parseClassName
	 * 
	 * @param pClassNameE
	 * @return String
	 */
	public static String parseClassName(Element pClassNameE) {
		// <!ELEMENT CLASSNAME EMPTY>
		// <!ATTLIST CLASSNAME %NAME;>
		Attr classname_nameA = (Attr) searchAttribute(pClassNameE, "NAME");
		return classname_nameA.getNodeValue();
	}

	/**
	 * parseCLASSNAME
	 * 
	 * @param pClassNameE
	 * @return CIMObjectPath
	 */
	public static CIMObjectPath parseCLASSNAME(Element pClassNameE) {
		return cLocalPathBuilder.build(parseClassName(pClassNameE), null);
	}

	/**
	 * parseINSTANCEPATH
	 * 
	 * @param pInstancePathE
	 * @return CIMObjectPath
	 * @throws CIMXMLParseException
	 */
	public static CIMObjectPath parseINSTANCEPATH(Element pInstancePathE)
			throws CIMXMLParseException {
		// <!ELEMENT INSTANCEPATH (NAMESPACEPATH,INSTANCENAME)>

		// NAMESPACEPATH
		Element namespacepathE = (Element) searchFirstNode(pInstancePathE, "NAMESPACEPATH");
		if (namespacepathE == null) { throw new CIMXMLParseException(); }
		CIMObjectPath nsPath = parseNAMESPACEPATH(namespacepathE);
		// INSTANCENAME
		Element instancenameE = (Element) searchFirstNode(pInstancePathE, "INSTANCENAME");
		if (instancenameE == null) { throw new CIMXMLParseException(); }
		CIMObjectPath op = parseINSTANCENAME(instancenameE);
		// ebak: change host and namespace
		return new CIMObjectPath(nsPath.getScheme(), nsPath.getHost(), nsPath.getPort(), nsPath
				.getNamespace(), op.getObjectName(), op.getKeys());
	}

	/**
	 * parseLOCALINSTANCEPATH
	 * 
	 * @param pLocalInstancePathE
	 * @return CIMObjectPath
	 * @throws CIMXMLParseException
	 */
	public static CIMObjectPath parseLOCALINSTANCEPATH(Element pLocalInstancePathE)
			throws CIMXMLParseException {
		// <!ELEMENT LOCALINSTANCEPATH (LOCALNAMESPACEPATH,INSTANCENAME)>

		// LOCALNAMESPACEPATH
		Element localnamespacepathE = (Element) searchFirstNode(pLocalInstancePathE,
				"LOCALNAMESPACEPATH");
		if (localnamespacepathE == null) { throw new CIMXMLParseException(); }
		String nameSpace = parseLOCALNAMESPACEPATH(localnamespacepathE);

		// INSTANCENAME
		Element instancenameE = (Element) searchFirstNode(pLocalInstancePathE, "INSTANCENAME");
		if (instancenameE == null) { throw new CIMXMLParseException(); }
		CIMObjectPath op = parseINSTANCENAME(instancenameE);
		/*
		 * CIMObjectPath(String objectName, String namespace, CIMProperty[]
		 * keys)
		 */
		return cLocalPathBuilder.build(op.getObjectName(), nameSpace, op.getKeys());
	}

	/**
	 * parseINSTANCENAME
	 * 
	 * @param pInstanceNameE
	 * @return CIMObjectPath
	 * @throws CIMXMLParseException
	 */
	public static CIMObjectPath parseINSTANCENAME(Element pInstanceNameE)
			throws CIMXMLParseException {
		// <!ELEMENT INSTANCENAME (KEYBINDING*|KEYVALUE?|VALUE.REFERNCE?)>
		// <!ATTLIST INSTANCENAME %CLASSNAME;>
		Attr instance_classnameA = (Attr) searchAttribute(pInstanceNameE, "CLASSNAME");
		String opClassName = instance_classnameA.getNodeValue();

		// KEYBINDING
		Element[] keyBindingElementA = searchNodes(pInstanceNameE, "KEYBINDING");
		if (keyBindingElementA != null && keyBindingElementA.length > 0) {
			CIMProperty<?>[] keys = new CIMProperty[keyBindingElementA.length];
			for (int i = 0; i < keyBindingElementA.length; i++) {
				Element keybindingE = keyBindingElementA[i];
				keys[i] = parseKEYBINDING(keybindingE);
			}
			return cLocalPathBuilder.build(opClassName, null, keys);
		}

		return new CIMObjectPath(null, null, null, null, opClassName, null);
	}

	/**
	 * parseOBJECTPATH
	 * 
	 * @param pObjectPathE
	 * @return CIMObjectPath
	 * @throws CIMXMLParseException
	 */
	public static CIMObjectPath parseOBJECTPATH(Element pObjectPathE) throws CIMXMLParseException {
		// <!ELEMENT OBJECTPATH (INSTANCEPATH|CLASSPATH) >

		// INSTANCEPATH
		Element instancepathE = (Element) searchFirstNode(pObjectPathE, "INSTANCEPATH");
		if (instancepathE != null) {
			CIMObjectPath op = parseINSTANCEPATH(instancepathE);
			return op;
		}

		// CLASSPATH
		Element classpathE = (Element) searchFirstNode(pObjectPathE, "CLASSPATH");
		if (classpathE != null) {
			CIMObjectPath op = parseCLASSPATH(classpathE);
			return op;
		}

		throw new CIMXMLParseException();
	}

	/**
	 * parseKEYBINDING
	 * 
	 * @param pKeyBindingE
	 * @return CIMProperty
	 * @throws CIMXMLParseException
	 */
	public static CIMProperty<?> parseKEYBINDING(Element pKeyBindingE) throws CIMXMLParseException {
		// <!ELEMENT KEYBINDING (KEYVALUE|VALUE.REFERENCE) >
		// <!ATTLIST KEYBINDING %NAME;>

		Attr keybinding_nameA = (Attr) searchAttribute(pKeyBindingE, "NAME");
		String propName = keybinding_nameA.getValue();

		// KEYVALUE
		Element keyvalueE = (Element) searchFirstNode(pKeyBindingE, "KEYVALUE");
		if (keyvalueE != null) {
			TypedValue propTypedVal = parseKEYVALUE(keyvalueE);
			return new CIMProperty<Object>(propName, propTypedVal.getType(), propTypedVal
					.getValue(), true, false, null);
		}

		// VALUE.REFERENCE
		Element valuereferenceE = (Element) searchFirstNode(pKeyBindingE, "VALUE.REFERENCE");
		if (valuereferenceE != null) {
			CIMObjectPath op = parseVALUEREFERENCE(valuereferenceE);
			return new CIMProperty<CIMObjectPath>(propName, new CIMDataType(op.getObjectName()),
					op, true, false, null);
		}

		throw new CIMXMLParseException();
	}

	private static final TreeMap<String, CIMDataType> TYPESTR_MAP = new TreeMap<String, CIMDataType>(
			new Comparator<Object>() {

				public int compare(Object pO1, Object pO2) {
					return ((String) pO1).compareToIgnoreCase((String) pO2);
				}

			});

	static {
		TYPESTR_MAP.put(MOF.DT_UINT8, CIMDataType.UINT8_T);
		TYPESTR_MAP.put(MOF.DT_UINT16, CIMDataType.UINT16_T);
		TYPESTR_MAP.put(MOF.DT_UINT32, CIMDataType.UINT32_T);
		TYPESTR_MAP.put(MOF.DT_UINT64, CIMDataType.UINT64_T);
		TYPESTR_MAP.put(MOF.DT_SINT8, CIMDataType.SINT8_T);
		TYPESTR_MAP.put(MOF.DT_SINT16, CIMDataType.SINT16_T);
		TYPESTR_MAP.put(MOF.DT_SINT32, CIMDataType.SINT32_T);
		TYPESTR_MAP.put(MOF.DT_SINT64, CIMDataType.SINT64_T);
		TYPESTR_MAP.put(MOF.DT_REAL32, CIMDataType.REAL32_T);
		TYPESTR_MAP.put(MOF.DT_REAL64, CIMDataType.REAL64_T);
		TYPESTR_MAP.put(MOF.DT_CHAR16, CIMDataType.CHAR16_T);
		TYPESTR_MAP.put(MOF.DT_STR, CIMDataType.STRING_T);
		TYPESTR_MAP.put(MOF.DT_BOOL, CIMDataType.BOOLEAN_T);
		TYPESTR_MAP.put(MOF.DT_DATETIME, CIMDataType.DATETIME_T);
		// FIXME: ebak: What to do with those types which are not specified by
		// MOF's BNF?
		TYPESTR_MAP.put(MOF.INVALID, CIMDataType.INVALID_T);
		// FIXME: ebak: What is the string representation of OBJECT?
		TYPESTR_MAP.put("object", CIMDataType.OBJECT_T);
		TYPESTR_MAP.put(MOF.CLASS, CIMDataType.CLASS_T);
		TYPESTR_MAP.put(MOF.REFERENCE, new CIMDataType(""));
	}

	/**
	 * parseScalarTypeStr
	 * 
	 * @param pTypeStr
	 * @return CIMDataType
	 * @throws CIMXMLParseException
	 */
	public static CIMDataType parseScalarTypeStr(String pTypeStr) throws CIMXMLParseException {
		return parseTypeStr(pTypeStr, false);
	}

	/**
	 * parseArrayTypeStr
	 * 
	 * @param pTypeStr
	 * @return CIMDataType
	 * @throws CIMXMLParseException
	 */
	public static CIMDataType parseArrayTypeStr(String pTypeStr) throws CIMXMLParseException {
		return parseTypeStr(pTypeStr, true);
	}

	/**
	 * parseTypeStr
	 * 
	 * @param pTypeStr
	 * @param pArray
	 * @return CIMDataType
	 * @throws CIMXMLParseException
	 */
	public static CIMDataType parseTypeStr(String pTypeStr, boolean pArray)
			throws CIMXMLParseException {
		if (pTypeStr == null) return pArray ? CIMDataType.STRING_ARRAY_T : CIMDataType.STRING_T;
		CIMDataType type = TYPESTR_MAP.get(pTypeStr);
		if (type == null) throw new CIMXMLParseException("Unknown type string:" + pTypeStr);
		if (pArray) {
			if (type.getType() == CIMDataType.REFERENCE) return new CIMDataType(type
					.getRefClassName(), 0);
			return CIMHelper.UnboundedArrayDataType(type.getType());
		}
		return type;
	}

	/**
	 * parseKEYVALUE
	 * 
	 * @param pKeyValueE
	 * @return TypedValue
	 * @throws CIMXMLParseException
	 */
	public static TypedValue parseKEYVALUE(Element pKeyValueE) throws CIMXMLParseException {
		/*
		 * <!ELEMENT KEYVALUE (#PCDATA)> <!ATTLIST KEYVALUE VALUETYPE
		 * (string|boolean|numeric) 'string') %CIMType; #IMPLIED>
		 */
		// ebak: if TYPE attribute is included there is no need to deal with
		// VALUETYPE attribute
		String typeStr = attribute(pKeyValueE, "TYPE");

		Text valueT = (Text) searchFirstChild(pKeyValueE);
		String valueStr = valueT == null ? null : valueT.getNodeValue();

		if (typeStr == null) {
			String valueTypeStr = attribute(pKeyValueE, "VALUETYPE");
			ValueTypeHandler vtHandler = new ValueTypeHandler(valueTypeStr, valueStr);
			return new TypedValue(vtHandler.getType(), vtHandler.getValue());
		}
		Object value = valueStr == null ? "" : createJavaObject(typeStr, valueStr);
		return new TypedValue(parseScalarTypeStr(typeStr), value);
	}

	/**
	 * Class ValueTypeHandler determines the value and type of a KEYVALUE XML
	 * element, when the VALUETYPE attribute is provided instead of the exact
	 * TYPE attribute. There is a very similar code section in the PULL and SAX
	 * parser, but that section wasn't made common since in ideal case the DOM
	 * parser will be removed.
	 */
	private static class ValueTypeHandler {

		private CIMDataType iType;

		private Object iValue;

		/**
		 * 
		 * Ctor.
		 * 
		 * @param pValueTypeStr
		 * @param pValueStr
		 * @throws CIMXMLParseException
		 */
		public ValueTypeHandler(String pValueTypeStr, String pValueStr) throws CIMXMLParseException {

			if (pValueTypeStr == null) throw new CIMXMLParseException(
					"KEYVALUE node must have a VALUETYPE attribute!");

			if (pValueTypeStr.equals("numeric")) {
				if (!setUInt64(pValueStr) && !setSInt64(pValueStr) && !setReal64(pValueStr)) throw new CIMXMLParseException(
						"Unparseable \"number\" value: " + pValueStr + " !");
			} else if (pValueTypeStr.equals(MOF.DT_STR)) {
				if (!setDTAbsolute(pValueStr) && !setDTInterval(pValueStr)) {
					this.iValue = pValueStr;
					this.iType = CIMDataType.STRING_T;
				}
			} else if (pValueTypeStr.equals(MOF.DT_BOOL)) {
				if (!setBoolean(pValueStr)) throw new CIMXMLParseException(
						"Unparsable \"boolean\" value: " + pValueStr + " !");
			} else {
				throw new CIMXMLParseException("KEYVALUE node's VALUETYPE attribute must be "
						+ MOF.DT_STR + ", " + MOF.DT_BOOL + " or numeric! " + pValueStr
						+ " is not allowed!");
			}
		}

		/**
		 * 
		 * getType
		 * 
		 * @return CIMDataType
		 */
		public CIMDataType getType() {
			return this.iType;
		}

		/**
		 * 
		 * getValue
		 * 
		 * @return Object
		 */
		public Object getValue() {
			return this.iValue;
		}

		private boolean setUInt64(String pValue) {
			try {
				this.iValue = new UnsignedInteger64(pValue);
			} catch (NumberFormatException e) {
				return false;
			}
			this.iType = CIMDataType.UINT64_T;
			return true;
		}

		private boolean setSInt64(String pValue) {
			try {
				this.iValue = new Long(pValue);
			} catch (NumberFormatException e) {
				return false;
			}
			this.iType = CIMDataType.SINT64_T;
			return true;
		}

		private boolean setReal64(String pValue) {
			try {
				this.iValue = new Double(pValue);
			} catch (NumberFormatException e) {
				return false;
			}
			this.iType = CIMDataType.REAL64_T;
			return true;
		}

		private boolean setBoolean(String pValue) {
			try {
				this.iValue = Boolean.valueOf(pValue);
			} catch (NumberFormatException e) {
				return false;
			}
			this.iType = CIMDataType.BOOLEAN_T;
			return true;
		}

		private boolean setDTAbsolute(String pValue) {
			try {
				this.iValue = new CIMDateTimeAbsolute(pValue);
			} catch (IllegalArgumentException e) {
				return false;
			}
			this.iType = CIMDataType.DATETIME_T;
			return true;
		}

		private boolean setDTInterval(String pValue) {
			try {
				this.iValue = new CIMDateTimeInterval(pValue);
			} catch (IllegalArgumentException e) {
				return false;
			}
			this.iType = CIMDataType.DATETIME_T;
			return true;
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	// Object Definition Elements
	// ////////////////////////////////////////////////////////////////////////////////////////

	private static String attribute(Element pElement, String pName) {
		String attrib = pElement.getAttribute(pName);
		if (attrib == null || attrib.length() == 0) return null;
		return attrib;
	}

	/**
	 * parseCLASS
	 * 
	 * @param pClassE
	 * @return CIMClass
	 * @throws CIMXMLParseException
	 */
	public static CIMClass parseCLASS(Element pClassE) throws CIMXMLParseException {
		// <!ELEMENT CLASS (QUALIFIER*,
		// (PROPERTY|PROPERTY.ARRAY|PROPERTY.REFERENCE)*,METHOD*)>
		// <!ATTLIST CLASS %NAME;%SUPERCLASS;>

		Attr class_nameA = (Attr) searchAttribute(pClassE, "NAME");
		String name = class_nameA.getNodeValue();

		// Attr superclass_nameA = (Attr)searchAttribute(classE, "SUPERCLASS");
		// String supername = superclass_nameA.getNodeValue();

		String superClass = attribute(pClassE, "SUPERCLASS");

		// QUALIFIER
		CIMQualifier<?>[] qualis = parseQUALIFIERS(pClassE);

		// PROPERTY
		CIMClassProperty<?>[] props = parseCLASSPROPERTIES(pClassE);

		// METHOD
		Element[] methodElementA = searchNodes(pClassE, "METHOD");
		CIMMethod<?>[] methods;
		if (methodElementA != null) {
			methods = new CIMMethod[methodElementA.length];
			for (int i = 0; i < methodElementA.length; i++) {
				Element methodE = methodElementA[i];
				methods[i] = parseMETHOD(methodE);
			}
		} else {
			methods = null;
		}

		/*
		 * CIMClass( String name, String superclass, CIMQualifier[] qualifiers,
		 * CIMClassProperty[] props, CIMMethod[] methods )
		 * 
		 * return new CIMClass( name, superClass, qualis, props, methods );
		 * 
		 * This constructor can provide localPath info. CIMClass( CIMObjectPath
		 * path, String superclass, CIMQualifier[] qualifiers,
		 * CIMClassProperty[] props, CIMMethod[] pMethods, boolean
		 * pIsAssociation, boolean pIsKeyed )
		 */
		return new CIMClass(cLocalPathBuilder.build(name, null), superClass, qualis, props,
				methods, hasAssocQuali(qualis), hasKeyProp(props));
	}

	private static boolean hasAssocQuali(CIMQualifier<?>[] pQualis) {
		if (pQualis == null) return false;
		for (int i = 0; i < pQualis.length; i++) {
			CIMQualifier<?> quali = pQualis[i];
			if ("ASSOCIATION".equalsIgnoreCase(quali.getName())
					&& Boolean.TRUE.equals(quali.getValue())) return true;
		}
		return false;
	}

	private static boolean hasKeyProp(CIMProperty<?>[] pProps) {
		if (pProps == null) return false;
		for (int i = 0; i < pProps.length; i++)
			if (pProps[i].isKey()) return true;
		return false;
	}

	private static CIMParameter<?>[] parseParameters(Element pMethodE) throws CIMXMLParseException {
		// PARAMETER
		Vector<CIMParameter<Object>> paramV = new Vector<CIMParameter<Object>>();
		Element[] paramElementA = searchNodes(pMethodE, "PARAMETER");
		if (paramElementA != null) {
			for (int i = 0; i < paramElementA.length; i++) {
				Element parameterE = paramElementA[i];
				CIMParameter<Object> p = parsePARAMETER(parameterE);
				paramV.add(p);
			}
		}

		Element[] paramRefElementA = searchNodes(pMethodE, "PARAMETER.REFERENCE");
		if (paramRefElementA != null) {
			for (int i = 0; i < paramRefElementA.length; i++) {
				Element parameterE = paramRefElementA[i];
				CIMParameter<Object> p = parsePARAMETERREFERENCE(parameterE);
				paramV.add(p);
			}
		}

		Element[] paramArrayElementA = searchNodes(pMethodE, "PARAMETER.ARRAY");
		if (paramArrayElementA != null) {
			for (int i = 0; i < paramArrayElementA.length; i++) {
				Element parameterE = paramArrayElementA[i];
				CIMParameter<Object> p = parsePARAMETERARRAY(parameterE);
				paramV.add(p);
			}
		}

		Element[] paramRefArrayElementA = searchNodes(pMethodE, "PARAMETER.REFARRAY");
		if (paramRefArrayElementA != null) {
			for (int i = 0; i < paramRefArrayElementA.length; i++) {
				Element parameterE = paramRefArrayElementA[i];
				CIMParameter<Object> p = parsePARAMETERREFARRAY(parameterE);
				paramV.add(p);
			}
		}

		return paramV.toArray(new CIMParameter[0]);
	}

	/**
	 * parseMETHOD
	 * 
	 * @param pMethodE
	 * @return CIMMethod
	 * @throws CIMXMLParseException
	 */
	public static CIMMethod<Object> parseMETHOD(Element pMethodE) throws CIMXMLParseException {
		// <!ELEMENT METHOD
		// (QUALIFIER*,(PARAMETER|PARAMETER.REFERENCE|PARAMETER.ARRAY|PARAMETER.REFARRAY)*)>
		// <!ATTLIST METHOD
		// %CIMName;
		// %CIMType; #IMPLIED
		// %ClassOrigin;
		// %Propagated;>

		String name = attribute(pMethodE, "NAME");
		EmbObjHandler embObjHandler = new EmbObjHandler(pMethodE);
		CIMDataType type = embObjHandler.getType();
		if (type.isArray()) throw new CIMXMLParseException("Method's type cannot be Array!");
		String classOrigin = attribute(pMethodE, "CLASSORIGIN");
		String propagatedStr = pMethodE.getAttribute("PROPAGATED");
		boolean propagated = "true".equalsIgnoreCase(propagatedStr);

		// PARAMETERS
		CIMParameter<?>[] params = parseParameters(pMethodE);
		// CIMMethod(String name, CIMDataType type, CIMQualifier[] qualifiers,
		// CIMParameter[] parameters, boolean propagated, String originClass)
		return new CIMMethod<Object>(name, type, embObjHandler.getQualifiers(), params, propagated,
				classOrigin);
	}

	/**
	 * parsePARAMETER
	 * 
	 * @param pParamE
	 * @return CIMParameter
	 * @throws CIMXMLParseException
	 */
	public static CIMParameter<Object> parsePARAMETER(Element pParamE) throws CIMXMLParseException {
		String name = attribute(pParamE, "NAME");
		EmbObjHandler iEmbObjHandler = new EmbObjHandler(pParamE);
		// CIMParameter(String name, CIMDataType type, CIMQualifier[]
		// qualifiers)
		return new CIMParameter<Object>(name, iEmbObjHandler.getType(), iEmbObjHandler
				.getQualifiers());
	}

	/**
	 * parsePARAMETERREFERENCE
	 * 
	 * @param pParamE
	 * @return CIMParameter
	 * @throws CIMXMLParseException
	 */
	public static CIMParameter<Object> parsePARAMETERREFERENCE(Element pParamE)
			throws CIMXMLParseException {
		String name = attribute(pParamE, "NAME");
		String referenceClass = attribute(pParamE, "REFERENCECLASS");
		CIMDataType type = new CIMDataType(referenceClass);
		// QUALIFIER
		CIMQualifier<?>[] qualis = parseQUALIFIERS(pParamE);
		return new CIMParameter<Object>(name, type, qualis);
	}

	/**
	 * parsePARAMETERARRAY
	 * 
	 * @param pParamE
	 * @return CIMParameter
	 * @throws CIMXMLParseException
	 */
	public static CIMParameter<Object> parsePARAMETERARRAY(Element pParamE)
			throws CIMXMLParseException {
		String name = attribute(pParamE, "NAME");
		EmbObjHandler iEmbObjHandler = new EmbObjHandler(pParamE);
		return new CIMParameter<Object>(name, iEmbObjHandler.getArrayType(), iEmbObjHandler
				.getQualifiers());
	}

	/**
	 * parsePARAMETERREFARRAY
	 * 
	 * @param pParamE
	 * @return CIMParameter
	 * @throws CIMXMLParseException
	 */
	public static CIMParameter<Object> parsePARAMETERREFARRAY(Element pParamE)
			throws CIMXMLParseException {
		String name = attribute(pParamE, "NAME");

		String referenceClass = attribute(pParamE, "REFERENCECLASS");

		int arraySize = 0; // unlimited
		String arraySizeStr = pParamE.getAttribute("ARRAYSIZE");
		try {
			if (arraySizeStr.length() > 0) arraySize = Integer.parseInt(arraySizeStr);
		} catch (Exception e) {
			LogAndTraceBroker.getBroker().trace(Level.WARNING,
					"exception while parsing parameter array reference size", e);
		}

		CIMDataType type = new CIMDataType((referenceClass != null) ? referenceClass : "",
				arraySize);

		// QUALIFIER
		CIMQualifier<?>[] qualis = parseQUALIFIERS(pParamE);
		return new CIMParameter<Object>(name, type, qualis);
	}

	/**
	 * parseINSTANCE
	 * 
	 * @param pInstanceE
	 * @return CIMInstance
	 * @throws CIMXMLParseException
	 */
	public static CIMInstance parseINSTANCE(Element pInstanceE) throws CIMXMLParseException {
		return parseINSTANCE(pInstanceE, null);
	}

	/**
	 * parseINSTANCE
	 * 
	 * @param pInstanceE
	 * @param pObjPath
	 * @return CIMInstance
	 * @throws CIMXMLParseException
	 */
	public static CIMInstance parseINSTANCE(Element pInstanceE, CIMObjectPath pObjPath)
			throws CIMXMLParseException {
		// <!ELEMENT INSTANCE (QUALIFIER*,
		// (PROPERTY|PROPERTY.ARRAY|PROPERTY.REFERENCE)*)>
		// <!ATTLIST INSTANCE %CLASSNAME;>

		// BB mod CIMInstance inst = new CIMInstance();
		String className = attribute(pInstanceE, "CLASSNAME");
		// QUALIFIER
		// FIXME: in JSR48 CIMInstance doesn't have qualifiers
		// CIMQualifier[] qualis = parseQUALIFIERS(pInstanceE);
		// PROPERTY
		CIMProperty<?>[] props = parsePROPERTIES(pInstanceE);

		return new CIMInstance(pObjPath == null ? cLocalPathBuilder.build(className, null)
				: pObjPath, props);
	}

	/**
	 * ebak: Access to enclosing method parseQUALIFIERS(Element) from the type
	 * CIMXMLParserImpl is emulated by a synthetic accessor method. Increasing
	 * its visibility will improve your performance
	 * 
	 * @param pElement
	 * @return CIMQualifier[]
	 * @throws CIMXMLParseException
	 */
	public static CIMQualifier<?>[] parseQUALIFIERS(Element pElement) throws CIMXMLParseException {
		Element[] qualiElementA = searchNodes(pElement, "QUALIFIER");
		CIMQualifier<?>[] qualis;
		if (qualiElementA != null) {
			qualis = new CIMQualifier[qualiElementA.length];
			for (int i = 0; i < qualiElementA.length; i++) {
				Element qualifierE = qualiElementA[i];
				qualis[i] = parseQUALIFIER(qualifierE);
			}
		} else qualis = null;
		return qualis;
	}

	/**
	 * <pre>
	 *  ENTITY % QualifierFlavor &quot;
	 *  OVERRIDABLE    (true|false)  'true'
	 *  TOSUBCLASS     (true|false)  'true'
	 *  TOINSTANCE     (true|false)  'false'
	 *  TRANSLATABLE   (true|false)  'false'&quot;
	 * </pre>
	 * 
	 * @param pElement
	 * @return int - CIMFlavor bit mixture
	 */
	private static int parseFLAVORS(Element pElement) {
		int flavors = 0;
		if (!getBoolAttribute(pElement, "OVERRIDABLE", true)) flavors |= CIMFlavor.DISABLEOVERRIDE;
		if (!getBoolAttribute(pElement, "TOSUBCLASS", true)) flavors |= CIMFlavor.RESTRICTED;
		if (getBoolAttribute(pElement, "TRANSLATABLE", false)) flavors |= CIMFlavor.TRANSLATE;
		return flavors;
	}

	private static TypedValue parseSingleValue(Element pElement) throws CIMXMLParseException {
		return parseSingleValue(pElement, VALUE | VALUEA);
	}

	private static final int VALUE = 1, VALUEA = 2, VALUEREF = 4, VALUEREFA = 8;

	private static TypedValue parseSingleValue(Element pElement, int pMask)
			throws CIMXMLParseException {
		String typeStr = attribute(pElement, "TYPE");
		// ebak: there was an ESS fix in the base implementation
		if (typeStr == null) typeStr = attribute(pElement, "PARAMTYPE");
		CIMDataType type = null;
		Object value = null;
		if ((pMask & VALUE) > 0) {
			Element valueE = (Element) searchFirstNode(pElement, "VALUE");
			if (valueE != null) {
				TypedValue tVal = parseVALUE(valueE);
				String valueStr = (String) tVal.getValue();
				type = typeStr == null ? tVal.getType() : parseScalarTypeStr(typeStr);
				value = createJavaObject(type.toString(), valueStr);
			}
		}
		if (type == null && (pMask & VALUEREF) > 0) {
			// VALUE.REFERENCE
			Element valuereferenceE = (Element) searchFirstNode(pElement, "VALUE.REFERENCE");
			if (valuereferenceE != null) {
				CIMObjectPath op = parseVALUEREFERENCE(valuereferenceE);
				value = op;
				type = new CIMDataType(op.getObjectName());
			}
		}
		if (type == null && (pMask & VALUEA) > 0) {
			// VALUE.ARRAY
			Element valuearrayE = (Element) searchFirstNode(pElement, "VALUE.ARRAY");
			if (valuearrayE != null) {
				TypedValue tValA = parseVALUEARRAY(valuearrayE);
				type = typeStr == null ? tValA.getType() : parseArrayTypeStr(typeStr);
				String[] valStrA = (String[]) tValA.getValue();
				if (valStrA != null) {
					Object[] values = new Object[valStrA.length];
					for (int i = 0; i < valStrA.length; i++) {
						values[i] = createJavaObject(type.toString(), valStrA[i]);
					}
					value = values;
				}
			}
		}
		if (type == null && (pMask & VALUEREFA) > 0) {
			// VALUE.REFARRAY
			Element valueRefArrayE = (Element) searchFirstNode(pElement, "VALUE.REFARRAY");
			if (valueRefArrayE != null) {
				type = parseArrayTypeStr(typeStr);
				CIMObjectPath[] opA = parseVALUEREFARRAY(valueRefArrayE);
				value = opA;
			}
		}
		if (type == null) {
			if (value instanceof Object[]) type = CIMDataType.STRING_ARRAY_T;
			else if (value != null) type = CIMDataType.STRING_T;
			else type = CIMDataType.STRING_T; // /throw new
			// CIMXMLParseException("null
			// type with null value!");
		}
		return new TypedValue(type, value);
	}

	/**
	 * parseQUALIFIER
	 * 
	 * @param pQualifierE
	 * @return CIMQualifier
	 * @throws CIMXMLParseException
	 */
	public static CIMQualifier<?> parseQUALIFIER(Element pQualifierE) throws CIMXMLParseException {
		// <!ELEMENT QUALIFIER (VALUE|VALUE.ARRAY)>
		// <!ATTLIST QUALIFIER %NAME;%TYPE;%PROPAGATED;%QUALIFIERFLAVOR;>

		String name = attribute(pQualifierE, "NAME");
		boolean propagated = MOF.TRUE.equalsIgnoreCase(pQualifierE.getAttribute("PROPAGATED"));
		// FLAVORS
		int flavors = parseFLAVORS(pQualifierE);
		// VALUE
		TypedValue typedValue = parseSingleValue(pQualifierE);
		if (typedValue.getType() == null) throw new CIMXMLParseException(
				"Qualifier's type is null!");
		// CIMQualifier(String pName, CIMDataType pType, Object pValue, int
		// pFlavor)
		return new CIMQualifier<Object>(name, typedValue.getType(), typedValue.getValue(), flavors,
				propagated);
	}

	/**
	 * parseQUALIFIERDECLARATION
	 * 
	 * @param pQualifierTypeE
	 * @return CIMQualifierType
	 * @throws CIMXMLParseException
	 */
	public static CIMQualifierType<Object> parseQUALIFIERDECLARATION(Element pQualifierTypeE)
			throws CIMXMLParseException {
		// <!ELEMENT QUALIFIER.DECLARATION (SCOPE?,(VALUE|VALUE.ARRAY)?)>
		// <!ATTLIST QUALIFIER.DECLARATION
		// %CIMName;
		// %CIMType; #REQUIRED
		// ISARRAY (true|false) #IMPLIED
		// %ArraySize;
		// %QualifierFlavor;>

		String name = attribute(pQualifierTypeE, "NAME");

		// SCOPES
		// ebak: there should be only 1 scope node
		Element scopeElement = (Element) searchFirstNode(pQualifierTypeE, "SCOPE");
		int scopes = scopeElement == null ? 0 : parseSCOPES(scopeElement);
		// QUALIFIERS
		// CIMQualifier[] qualis = parseQUALIFIERS(pQualifierTypeE);

		// VALUE
		TypedValue typedValue = parseSingleValue(pQualifierTypeE);
		CIMDataType type = typedValue.getType();
		if (MOF.TRUE.equalsIgnoreCase(attribute(pQualifierTypeE, "ISARRAY"))) {
			String arraySizeStr = attribute(pQualifierTypeE, "ARRAYSIZE");
			if (arraySizeStr != null) {
				try {
					int arraySize = Integer.parseInt(arraySizeStr);
					type = new CIMDataType(type.getType(), arraySize);
				} catch (Exception e) {
					LogAndTraceBroker.getBroker().trace(Level.WARNING,
							"exception while parsing array size", e);
				}
			}
		}

		// FIXME: ebak: what about the flavors?
		return new CIMQualifierType<Object>(new CIMObjectPath(null, null, null, null, name, null),
				type, typedValue.getValue(), scopes, 0);
	}

	private static boolean hasTrueAttribute(Element pElement, String pName) {
		String valueStr = pElement.getAttribute(pName);
		return MOF.TRUE.equalsIgnoreCase(valueStr);
	}

	private static boolean getBoolAttribute(Element pElement, String pName, boolean pDefVal) {
		String val = pElement.getAttribute(pName);
		if (MOF.TRUE.equalsIgnoreCase(val)) return true;
		if (MOF.FALSE.equalsIgnoreCase(val)) return false;
		return pDefVal;
	}

	/**
	 * parseSCOPES
	 * 
	 * @param pScopeE
	 * @return int
	 */
	public static int parseSCOPES(Element pScopeE) {
		int scopes = 0;
		if (hasTrueAttribute(pScopeE, "CLASS")) scopes |= CIMScope.CLASS;
		if (hasTrueAttribute(pScopeE, "ASSOCIATION")) scopes |= CIMScope.ASSOCIATION;
		if (hasTrueAttribute(pScopeE, "REFERENCE")) scopes |= CIMScope.REFERENCE;
		if (hasTrueAttribute(pScopeE, "PROPERTY")) scopes |= CIMScope.PROPERTY;
		if (hasTrueAttribute(pScopeE, "METHOD")) scopes |= CIMScope.METHOD;
		if (hasTrueAttribute(pScopeE, "PARAMETER")) scopes |= CIMScope.PARAMETER;
		if (hasTrueAttribute(pScopeE, "INDICATION")) scopes |= CIMScope.INDICATION;
		return scopes;
	}

	private static Vector<CIMClassProperty<?>> parseClassPropsToVec(Element pElement)
			throws CIMXMLParseException {
		Element[] propElementA = searchNodes(pElement, "PROPERTY");
		Vector<CIMClassProperty<?>> propVec = new Vector<CIMClassProperty<?>>();
		if (propElementA != null) {
			for (int i = 0; i < propElementA.length; i++) {
				Element propertyE = propElementA[i];
				propVec.add(parseCLASSPROPERTY(propertyE));

			}
		}

		// PROPERTY.ARRAY
		Element[] propArrayElementA = searchNodes(pElement, "PROPERTY.ARRAY");
		if (propArrayElementA != null) {
			for (int i = 0; i < propArrayElementA.length; i++) {
				Element propertyarrayE = propArrayElementA[i];
				propVec.add(parseCLASSPROPERTYARRAY(propertyarrayE));
			}
		}

		// PROPERTY.REFERENCE
		Element[] propRefElementA = searchNodes(pElement, "PROPERTY.REFERENCE");
		if (propRefElementA != null) {
			for (int i = 0; i < propRefElementA.length; i++) {
				Element propertyreferenceE = propRefElementA[i];
				propVec.add(parseCLASSPROPERTYREFERENCE(propertyreferenceE));
			}
		}
		return propVec;
	}

	/**
	 * parsePROPERTIES
	 * 
	 * @param pElement
	 * @return CIMProperty[]
	 * @throws CIMXMLParseException
	 */
	public static CIMProperty<?>[] parsePROPERTIES(Element pElement) throws CIMXMLParseException {
		Vector<CIMClassProperty<?>> classPropVec = parseClassPropsToVec(pElement);
		// The following does not convert Vector<CIMClassProperty> to
		// CIMProperty[], it is still CIMClassProperty[]!!! You can treat it
		// like a CIMProperty (property.getValue(), etc.) but (property
		// instanceof CIMProperty) will return false!
		// return classPropVec.toArray(new CIMProperty[0]);

		int arraySize = classPropVec.size();
		CIMProperty<?>[] retA = new CIMProperty[arraySize];
		for (int i = 0; i < arraySize; i++) {
			CIMClassProperty<?> prop = classPropVec.get(i);
			retA[i] = new CIMProperty<Object>(prop.getName(), prop.getDataType(), prop.getValue(),
					prop.isKey(), prop.isPropagated(), prop.getOriginClass());
		}
		return retA;
	}

	/**
	 * parseCLASSPROPERTIES
	 * 
	 * @param pElement
	 * @return CIMClassProperty[]
	 * @throws CIMXMLParseException
	 */
	public static CIMClassProperty<?>[] parseCLASSPROPERTIES(Element pElement)
			throws CIMXMLParseException {
		Vector<CIMClassProperty<?>> classPropVec = parseClassPropsToVec(pElement);
		return classPropVec.toArray(new CIMClassProperty[0]);
	}

	/**
	 * parsePROPERTY
	 * 
	 * @param pPropertyE
	 * @return CIMProperty
	 * @throws CIMXMLParseException
	 */
	public static CIMProperty<?> parsePROPERTY(Element pPropertyE) throws CIMXMLParseException {
		return parseCLASSPROPERTY(pPropertyE);
	}

	private static class EmbObjHandler {

		private CIMQualifier<?>[] iQualiA;

		private boolean iHasEmbObjAttr, iHasEmbInstAttr, iHasEmbObjQuali, iHasEmbInstQuali, iKeyed;

		private CIMDataType iRawType, iType;

		private Object iRawValue, iValue;

		private Element iElement;

		private boolean iStrictParsing = WBEMConfiguration.getGlobalConfiguration()
				.strictEmbObjTypes();

		/**
		 * Ctor.
		 * 
		 * @param pElement
		 * @throws CIMXMLParseException
		 */
		public EmbObjHandler(Element pElement) throws CIMXMLParseException {
			this.iElement = pElement;
			handleQualis();
			handleAttribs();
			Element valueE = (Element) searchFirstNode(pElement, "VALUE");
			if (valueE != null) {
				TypedValue tv = parseVALUE(valueE);
				if (this.iRawType == null) this.iRawType = tv.getType();
				this.iRawValue = tv.getValue();
			} else {
				valueE = (Element) searchFirstNode(pElement, "VALUE.ARRAY");
				if (valueE != null) {
					TypedValue tv = parseVALUEARRAY(valueE);
					if (this.iRawType == null) this.iRawType = tv.getType();
					this.iRawValue = tv.getValue();
				}
			}
		}

		private void handleQualis() throws CIMXMLParseException {
			this.iQualiA = parseQUALIFIERS(this.iElement);
			if (this.iQualiA != null) {
				for (int idx = 0; idx < this.iQualiA.length; idx++) {
					String qualiName = this.iQualiA[idx].getName();
					if ("EmbeddedObject".equalsIgnoreCase(qualiName)) {
						this.iHasEmbObjQuali = true;
					} else if ("EMBEDDEDINSTANCE".equalsIgnoreCase(qualiName)) {
						this.iHasEmbInstQuali = true;
					} else if ("KEY".equalsIgnoreCase(qualiName)) {
						this.iKeyed = true;
					}
					if ((this.iHasEmbObjQuali || this.iHasEmbInstQuali) && this.iKeyed) return;
				}
			}
		}

		private void handleAttribs() throws CIMXMLParseException {
			String typeStr = this.iElement.getAttribute("TYPE");
			if (typeStr == null || typeStr.length() == 0) {
				typeStr = this.iElement.getAttribute("PARAMTYPE");
				if (typeStr == null || typeStr.length() == 0) {
					/*
					 * throw new CIMXMLParseException( iElement.getNodeName()+"
					 * must contain a TYPE or PARAMTYPE attribute!" );
					 */
					/*
					 * ebak: Workaround for SVC's TYPE attribute in VALUE and
					 * VALUE.ARRAY element.
					 */
					typeStr = null;
				}
			}
			// ebak: iRawType can be filled from contained VALUE or VALUE.ARRAY
			// element (SVC)
			if (typeStr != null) this.iRawType = parseScalarTypeStr(typeStr);

			// DSPs call for EmbeddedObject AND XML is case sensitive, BUT there
			// are probably CIMOMs out there that still use EMBEDDEDOBJECT
			String embObjAttrStr = this.iElement.getAttribute("EmbeddedObject");
			if (embObjAttrStr == null || embObjAttrStr.length() == 0) {
				embObjAttrStr = this.iElement.getAttribute("EMBEDDEDOBJECT");
			}
			if (embObjAttrStr == null || embObjAttrStr.length() == 0) {
				this.iHasEmbObjAttr = this.iHasEmbInstAttr = false;
			} else if ("object".equalsIgnoreCase(embObjAttrStr)) {
				this.iHasEmbObjAttr = true;
				this.iHasEmbInstAttr = false;
			} else if ("instance".equalsIgnoreCase(embObjAttrStr)) {
				this.iHasEmbObjAttr = false;
				this.iHasEmbInstAttr = true;
			} else throw new CIMXMLParseException("EmbeddedObject attribute cannot contain \""
					+ embObjAttrStr + "\" value!");
		}

		/**
		 * getQualifiers
		 * 
		 * @return CIMQualifier[]
		 * @throws CIMXMLParseException
		 */
		public CIMQualifier<?>[] getQualifiers() throws CIMXMLParseException {
			transform();
			CIMQualifiedElementInterfaceImpl qualiImpl = new CIMQualifiedElementInterfaceImpl(
					this.iQualiA, isKeyed(), this.iType.getType() == CIMDataType.STRING);
			return qualiImpl.getQualifiers();
		}

		/**
		 * isKeyed
		 * 
		 * @return boolean
		 */
		public boolean isKeyed() {
			return this.iKeyed;
		}

		/**
		 * isEmbeddedObject
		 * 
		 * @return boolean
		 */
		private boolean isEmbeddedObject() {
			return this.iHasEmbObjAttr || this.iHasEmbInstAttr || this.iHasEmbObjQuali
					|| this.iHasEmbInstQuali;
		}

		/**
		 * isEmbeddedClass
		 * 
		 * @return boolean
		 */
		private boolean isEmbeddedClass() {
			return this.iHasEmbObjAttr || this.iHasEmbObjQuali;
		}

		/**
		 * isEmbeddedInstance
		 * 
		 * @return boolean
		 */
		private boolean isEmbeddedInstance() {
			return this.iHasEmbInstAttr || this.iHasEmbInstQuali;
		}

		/**
		 * getType
		 * 
		 * @return CIMDataType
		 * @throws CIMXMLParseException
		 */
		public CIMDataType getType() throws CIMXMLParseException {
			transform();
			return this.iType;
		}

		/**
		 * getArrayType
		 * 
		 * @return CIMDataType
		 * @throws CIMXMLParseException
		 */
		public CIMDataType getArrayType() throws CIMXMLParseException {
			transform();
			return this.iType.isArray() ? this.iType : CIMHelper.UnboundedArrayDataType(this.iType
					.getType());
		}

		/**
		 * getValue
		 * 
		 * @return Object
		 * @throws CIMXMLParseException
		 */
		public Object getValue() throws CIMXMLParseException {
			transform();
			return this.iValue;
		}

		private void transform() throws CIMXMLParseException {
			if (this.iType != null) return;
			if (this.iRawValue == null) {
				if (isEmbeddedObject()) {
					if (this.iRawType != CIMDataType.STRING_T) throw new CIMXMLParseException(
							"Embedded Object CIM-XML element's type must be string. "
									+ this.iRawType + " is invalid!");
					if (this.iStrictParsing) {
						/*
						 * In case of strict parsing Object means CIMClass,
						 * Instance means CIMInstance. Pegasus 2.7.0 seems to
						 * handle it this way.
						 */
						this.iType = isEmbeddedInstance() ? CIMDataType.OBJECT_T
								: CIMDataType.CLASS_T;
					} else {
						/*
						 * for valueless EmbeddedObject="object" the type is
						 * undeterminable since Pegasus's CIMObject can contain
						 * both CIMClass and CIMInstance
						 */
						this.iType = isEmbeddedInstance() ? CIMDataType.OBJECT_T
								: CIMDataType.STRING_T;
					}
				} else {
					this.iType = this.iRawType;
				}
				this.iValue = null;
			} else {
				if (isEmbeddedObject()) {
					transformEmbObj();
				} else {
					transformNormObj();
				}
			}
		}

		private void transformEmbObj() throws CIMXMLParseException {
			if (this.iRawValue instanceof String) {
				this.iValue = parseXmlStr((String) this.iRawValue);
				this.iType = getCIMObjType(this.iValue);
			} else {
				this.iValue = parseXmlStrA((String[]) this.iRawValue);
				this.iType = getCIMObjAType((Object[]) this.iValue);
			}
			if (isEmbeddedInstance() && this.iType.getType() != CIMDataType.OBJECT) throw new CIMXMLParseException(
					this.iElement.getNodeName()
							+ " element is an EmbeddedInstance with non INSTANCE value. "
							+ "It's not valid!");
			if (isEmbeddedClass() && this.iType.getType() != CIMDataType.CLASS
					&& this.iType.getType() != CIMDataType.OBJECT) throw new CIMXMLParseException(
					this.iElement.getNodeName()
							+ " element is an EmbeddedObject with non CLASS/INSTANCE value. It's not valid!");
		}

		private void transformNormObj() throws CIMXMLParseException {
			if (this.iRawValue instanceof String) {
				this.iType = this.iRawType;
				this.iValue = createJavaObject(this.iType.toString(), (String) this.iRawValue);
			} else {
				String[] rawValueA = (String[]) this.iRawValue;
				String typeStr = this.iRawType.toString();
				Object[] objA = new Object[rawValueA.length];
				for (int i = 0; i < objA.length; i++)
					objA[i] = createJavaObject(typeStr, rawValueA[i]);
				this.iType = CIMHelper.UnboundedArrayDataType(this.iRawType.getType());
				this.iValue = objA;
			}
		}

		/**
		 * parseXmlStr
		 * 
		 * @param pXmlStr
		 * @return CIMClass or CIMInstance
		 * @throws CIMXMLParseException
		 */
		public static Object parseXmlStr(String pXmlStr) throws CIMXMLParseException {
			// ebak: workaround for null array elements -> parse empty EmbObj
			// string as null
			if (pXmlStr == null || pXmlStr.length() == 0) return null;
			try {
				CIMClientXML_HelperImpl builder = new CIMClientXML_HelperImpl();
				Document doc = builder.parse(new InputSource(new StringReader(pXmlStr)));
				return parseObject(doc.getDocumentElement());
			} catch (CIMXMLParseException e) {
				throw e;
			} catch (Exception e) {
				throw new CIMXMLParseException(e.getMessage() + "\npXmlStr=" + pXmlStr);
			}
		}

		/**
		 * parseXmlStrA
		 * 
		 * @param pXmlStrA
		 * @return Object[]
		 * @throws CIMXMLParseException
		 */
		public static Object[] parseXmlStrA(String[] pXmlStrA) throws CIMXMLParseException {
			if (pXmlStrA == null || pXmlStrA.length == 0) return null;
			Object[] objA = new Object[pXmlStrA.length];
			for (int i = 0; i < objA.length; i++) {
				String xmlStr = pXmlStrA[i];
				objA[i] = xmlStr == null ? null : parseXmlStr(xmlStr);
			}
			return objA;
		}

		/**
		 * getCIMObjType
		 * 
		 * @param pCIMObj
		 * @return CIMDataType
		 * @throws CIMXMLParseException
		 */
		public static CIMDataType getCIMObjType(Object pCIMObj) throws CIMXMLParseException {
			if (pCIMObj == null) throw new CIMXMLParseException(
					"cannot have null CIM object! (CIMClass or CIMInstance)");
			if (pCIMObj instanceof CIMInstance) return CIMDataType.OBJECT_T;
			if (pCIMObj instanceof CIMClass) return CIMDataType.CLASS_T;
			throw new CIMXMLParseException(pCIMObj.getClass().getName()
					+ " is not a CIM object! (CIMClass or CIMInstance)");
		}

		/**
		 * getCIMObjAType
		 * 
		 * @param pCIMObjA
		 * @return CIMDataType
		 * @throws CIMXMLParseException
		 */
		public static CIMDataType getCIMObjAType(Object[] pCIMObjA) throws CIMXMLParseException {
			if (pCIMObjA == null || pCIMObjA.length == 0) return CIMDataType.STRING_ARRAY_T;
			CIMDataType type = null;
			for (int i = 0; i < pCIMObjA.length; i++) {
				if (pCIMObjA[i] == null) continue;
				CIMDataType currType = getCIMObjType(pCIMObjA[i]);
				if (type == null) {
					type = currType;
				} else if (type != currType) { throw new CIMXMLParseException(
						"Embedded Object arrays with different types are not supported"); }
			}
			if (type == CIMDataType.OBJECT_T) return CIMDataType.OBJECT_ARRAY_T;
			if (type == CIMDataType.CLASS_T) return CIMDataType.CLASS_ARRAY_T;
			return CIMDataType.STRING_ARRAY_T;
		}

	}

	/**
	 * parseCLASSPROPERTY
	 * 
	 * @param pPropertyE
	 * @return CIMClassProperty
	 * @throws CIMXMLParseException
	 */
	public static CIMClassProperty<Object> parseCLASSPROPERTY(Element pPropertyE)
			throws CIMXMLParseException {
		// <!ELEMENT PROPERTY (QUALIFIER*,VALUE?)>
		// <!ATTLIST PROPERTY %NAME;%TYPE;%CLASSORIGIN;%PROPAGATED;>

		Attr property_nameA = (Attr) searchAttribute(pPropertyE, "NAME");
		String name = property_nameA.getNodeValue();

		String classOrigin = pPropertyE.getAttribute("CLASSORIGIN");
		if (classOrigin != null && classOrigin.length() == 0) classOrigin = null;

		String propagatedStr = pPropertyE.getAttribute("PROPAGATED");
		boolean propagated = MOF.TRUE.equalsIgnoreCase(propagatedStr);

		// QUALIFIER
		// ebak: EmbeddedObject support
		EmbObjHandler embObjHandler = new EmbObjHandler(pPropertyE);
		/*
		 * CIMClassProperty(String pName, CIMDataType pType, Object pValue,
		 * CIMQualifier[] pQualifiers, boolean pKey, boolean propagated, String
		 * originClass)
		 */
		return new CIMClassProperty<Object>(name, embObjHandler.getType(),
				embObjHandler.getValue(), embObjHandler.getQualifiers(), embObjHandler.isKeyed(),
				propagated, classOrigin);
	}

	/**
	 * parsePROPERTYARRAY
	 * 
	 * @param pPropertyArrayE
	 * @return CIMProperty
	 * @throws CIMXMLParseException
	 */
	public static CIMProperty<Object> parsePROPERTYARRAY(Element pPropertyArrayE)
			throws CIMXMLParseException {
		return parseCLASSPROPERTYARRAY(pPropertyArrayE);
	}

	/**
	 * parseCLASSPROPERTYARRAY
	 * 
	 * @param pPropArrayE
	 * @return CIMClassProperty
	 * @throws CIMXMLParseException
	 */
	public static CIMClassProperty<Object> parseCLASSPROPERTYARRAY(Element pPropArrayE)
			throws CIMXMLParseException {
		// <!ELEMENT PROPERTY.ARRAY (QUALIFIER*,VALUE.ARRAY?)>
		// <!ATTLIST PROPERTY.ARRAY
		// %NAME;%TYPE;%ARRAYSIZE;%CLASSORIGIN;%PROPAGATED;>

		String name = pPropArrayE.getAttribute("NAME");

		// String valueArraysizeStr = propertyarrayE.getAttribute("ARRAYSIZE");

		String classOrigin = pPropArrayE.getAttribute("CLASSORIGIN");
		if (classOrigin != null && classOrigin.length() == 0) classOrigin = null;

		String valuePropagatedStr = pPropArrayE.getAttribute("PROPAGATED");
		boolean propagated = (valuePropagatedStr != null && valuePropagatedStr
				.equalsIgnoreCase(MOF.TRUE));

		// QUALIFIER
		// ebak: EmbeddedObject support
		EmbObjHandler embObjHandler = new EmbObjHandler(pPropArrayE);
		return new CIMClassProperty<Object>(name, embObjHandler.getArrayType(), embObjHandler
				.getValue(), embObjHandler.getQualifiers(), embObjHandler.isKeyed(), propagated,
				classOrigin);
	}

	/**
	 * parsePROPERTYREFERENCE
	 * 
	 * @param pPropRefE
	 * @return CIMProperty
	 * @throws CIMXMLParseException
	 */
	public static CIMProperty<Object> parsePROPERTYREFERENCE(Element pPropRefE)
			throws CIMXMLParseException {
		return parseCLASSPROPERTYREFERENCE(pPropRefE);
	}

	/**
	 * parseCLASSPROPERTYREFERENCE
	 * 
	 * @param pPropRefE
	 * @return CIMClassProperty
	 * @throws CIMXMLParseException
	 */
	public static CIMClassProperty<Object> parseCLASSPROPERTYREFERENCE(Element pPropRefE)
			throws CIMXMLParseException {
		// <!ELEMENT PROPERTY.REFERENCE (QUALIFIER*,VALUE.REFERENCE?)>
		// <!ATTLIST PROPERTY.REFERENCE
		// %NAME;%REFERENCECLASS;%CLASSORIGIN;%PROPAGATED;>

		String name = pPropRefE.getAttribute("NAME");

		String classOrigin = pPropRefE.getAttribute("CLASSORIGIN");
		if (classOrigin != null && classOrigin.length() == 0) classOrigin = null;

		String referenceClass = pPropRefE.getAttribute("REFERENCECLASS");
		if (referenceClass != null && referenceClass.length() == 0) referenceClass = null;

		String propagatedStr = pPropRefE.getAttribute("PROPAGATED");
		boolean propagated = MOF.TRUE.equalsIgnoreCase(propagatedStr);

		// QUALIFIER
		CIMQualifier<?>[] qualis = parseQUALIFIERS(pPropRefE);

		CIMDataType type = new CIMDataType(referenceClass);
		// VALUE.REFERENCE
		Element valueRefE = (Element) searchFirstNode(pPropRefE, "VALUE.REFERENCE");
		Object value = valueRefE != null ? parseVALUEREFERENCE(valueRefE) : null;

		// return a CIMProperty without a CIMValue
		CIMQualifiedElementInterfaceImpl qualiImpl = new CIMQualifiedElementInterfaceImpl(qualis);
		return new CIMClassProperty<Object>(name, type, value, qualis, qualiImpl.isKeyed(),
				propagated, classOrigin);
	}

	/**
	 * parseMESSAGE
	 * 
	 * @param pCimVersion
	 * @param pDtdVersion
	 * @param pMessageE
	 * @return CIMMessage
	 * @throws CIMXMLParseException
	 */
	public static CIMMessage parseMESSAGE(String pCimVersion, String pDtdVersion, Element pMessageE)
			throws CIMXMLParseException {
		// <!ELEMENT MESSAGE
		// (SIMPLEREQ|MULTIREQ|SIMPLERSP|MULTIRSP|SIMPLEEXPREQ|MULTIEXPREQ|SIMPLEEXPRSP|MULTIEXPRSP)>
		// <!ATTLIST MESSAGE %ID;%PROTOCOLVERSION;>

		Attr idA = (Attr) searchAttribute(pMessageE, "ID");
		String id = idA.getNodeValue();
		// TODO
		if (pCimVersion.equals("2.0") && pDtdVersion.equals("2.0")) {

			// Attr message_idA = (Attr)searchAttribute(messageE, "ID");
			// String messadeID = message_idA.getNodeValue();

			// Attr message_protocolversionA = (Attr)searchAttribute(messageE,
			// "PROTOCOLVERSION");
			// String protocolversion = message_protocolversionA.getNodeValue();

			// SIMPLERSP
			Element simplerspE = (Element) searchFirstNode(pMessageE, "SIMPLERSP");
			if (simplerspE != null) {
				CIMResponse response = parseSIMPLERSP(simplerspE);
				response.setMethod("SIMPLERSP");
				response.setId(id);
				return response;
			}
			Element multirspE = (Element) searchFirstNode(pMessageE, "MULTIRSP");
			if (multirspE != null) {
				CIMResponse response = parseMULTIRSP(multirspE);
				response.setMethod("MULTIRSP");
				response.setId(id);
				return response;
			}
			// SIMPLEEXPREQ
			Element simpleexpreqE = (Element) searchFirstNode(pMessageE, "SIMPLEEXPREQ");
			if (simpleexpreqE != null) {
				CIMRequest request = parseSIMPLEEXPREQ(simpleexpreqE);
				request.setMethod("SIMPLEEXPREQ");
				request.setId(id);
				return request;
			}

			// MULTIEXPREQ
			Element multiexpreqE = (Element) searchFirstNode(pMessageE, "MULTIEXPREQ");
			if (multiexpreqE != null) {
				CIMRequest request = parseMULTIEXPREQ(multiexpreqE);
				request.setMethod("MULTIEXPREQ");
				request.setId(id);
				return request;
			}
			// SIMPLEEXPRSP
			// MULTIEXPRSP

			// SIMPLEREQ
			Element simplereqE = (Element) searchFirstNode(pMessageE, "SIMPLEREQ");
			if (simplereqE != null) {
				CIMRequest request = parseSIMPLEREQ(simplereqE);
				request.setMethod("SIMPLEREQ");
				request.setId(id);
				return request;
			}

			// MULTIREQ
			Element multireqE = (Element) searchFirstNode(pMessageE, "MULTIREQ");
			if (multireqE != null) {
				CIMRequest request = parseMULTIREQ(multireqE);
				request.setMethod("MULTIREQ");
				request.setId(id);
				return request;
			}
			throw new CIMXMLParseException();
		}
		// TODO, look for the specific error message in the spec
		throw new CIMXMLParseException("DTD not supported");
	}

	/**
	 * parsePARAMVALUE
	 * 
	 * @param pParamValueE
	 * @return CIMArgument
	 * @throws CIMXMLParseException
	 */
	public static CIMArgument<Object> parsePARAMVALUE(Element pParamValueE)
			throws CIMXMLParseException {
		// <!ELEMENT PARAMVALUE
		// (VALUE|VALUE.REFERENCE|VALUE.ARRAY|VALUE.REFARRAY)?>)
		// <!ATTLIST PARAMTYPE %NAME;%PARAMTYPE%>
		String name = attribute(pParamValueE, "NAME");
		// FIXME: ebak: The base implementation didn't contain VALUE.REFARRAY
		// handling. Why?
		// ebak: embedded object support, different parsing for VALUE and
		// VALUE.ARRAY sub elements
		if (searchFirstNode(pParamValueE, "VALUE.REFERENCE") != null
				|| searchFirstNode(pParamValueE, "VALUE.REFARRAY") != null) {
			TypedValue typedValue = parseSingleValue(pParamValueE, VALUEREF);
			CIMDataType type = typedValue.getType();
			Object value = typedValue.getValue();
			if (type == null) throw new CIMXMLParseException("Type is null!");
			return new CIMArgument<Object>(name, type, value);
		}
		EmbObjHandler embObjHandler = new EmbObjHandler(pParamValueE);
		return new CIMArgument<Object>(name, embObjHandler.getType(), embObjHandler.getValue());
	}

	/**
	 * parseIPARAMVALUE
	 * 
	 * @param pParamValueE
	 * @return CIMArgument
	 * @throws CIMXMLParseException
	 */
	public static CIMArgument<Object> parseIPARAMVALUE(Element pParamValueE)
			throws CIMXMLParseException {
		// <!ELEMENT IPARAMVALUE
		// (VALUE|VALUE.ARRAY|VALUE.REFERENCE|CLASSNAME|INSTANCENAME|QUALIFIER.DECLARATION|
		// CLASS|INSTANCE|VALUE.NAMEDINSTANCE)?>
		// <!ATTLIST IPARAMVALUE
		// %CIMName;>

		String name = attribute(pParamValueE, "NAME");
		// this can parse VALUE, VALUE.ARRAY and VALUE.REFERENCE
		TypedValue typedValue = parseSingleValue(pParamValueE, VALUE | VALUEA | VALUEREF);
		if (typedValue.getType() != null) { return new CIMArgument<Object>(name, typedValue
				.getType(), typedValue.getValue()); }
		// we can have different types too which cannot be handled by
		// parseSingleValue()

		// INSTANCENAME
		Element instNameE = (Element) searchFirstNode(pParamValueE, "INSTANCENAME");
		if (instNameE != null) {
			CIMObjectPath op = parseINSTANCENAME(instNameE);
			CIMDataType type = new CIMDataType(op.getObjectName());
			return new CIMArgument<Object>(name, type, op);
		}

		// QUALIFIER.DECLARATION
		Element qualiDeclarationE = (Element) searchFirstNode(pParamValueE, "QUALIFIER.DECLARATION");
		if (qualiDeclarationE != null) {
			CIMQualifierType<Object> qualiType = parseQUALIFIERDECLARATION(qualiDeclarationE);
			// FIXME: ebak: Does it surely have to be mapped to type:
			// REFERENCE????
			return new CIMArgument<Object>(name, new CIMDataType(qualiType.getName()), qualiType);
		}

		// CLASS
		Element classE = (Element) searchFirstNode(pParamValueE, "CLASS");
		if (classE != null) {
			CIMClass cl = parseCLASS(classE);
			// FIXME: ebak: Does it surely have to be mapped to type: REFERENCE?
			// Why not map to type: CLASS?
			return new CIMArgument<Object>(name, new CIMDataType(cl.getName()), cl);
		}

		// INSTANCE
		Element instanceE = (Element) searchFirstNode(pParamValueE, "INSTANCE");
		if (instanceE != null) {
			CIMInstance inst = parseINSTANCE(instanceE);
			// FIXME: ebak: Does it surely have to be mapped to type: REFERENCE?
			// Why not map to type: OBJECT?
			return new CIMArgument<Object>(name, new CIMDataType(inst.getClassName()), inst);
		}

		// VALUE.NAMEDINSTANCE
		Element valuenamedisntanceE = (Element) searchFirstNode(pParamValueE, "VALUE.NAMEDINSTANCE");
		if (valuenamedisntanceE != null) {
			CIMInstance inst = parseVALUENAMEDINSTANCE(valuenamedisntanceE);
			// FIXME: ebak: Does it surely have to be mapped to type: REFERENCE?
			// Why not map to type: OBJECT?
			return new CIMArgument<Object>(name, new CIMDataType(inst.getClassName()), inst);
		}

		return null;
	}

	/**
	 * parseSIMPLERSP
	 * 
	 * @param pSimpleRspE
	 * @return CIMResponse
	 * @throws CIMXMLParseException
	 */
	public static CIMResponse parseSIMPLERSP(Element pSimpleRspE) throws CIMXMLParseException {
		// <!ELEMENT SIMPLERSP (METHODRESPONSE|IMETHODRESPONSE)>

		// METHODRESPONSE
		Element methodresponseE = (Element) searchFirstNode(pSimpleRspE, "METHODRESPONSE");
		if (methodresponseE != null) {

		return parseMETHODRESPONSE(methodresponseE); }

		// IMETHODRESPONSE
		Element imethodresponseE = (Element) searchFirstNode(pSimpleRspE, "IMETHODRESPONSE");
		if (imethodresponseE != null) { return parseIMETHODRESPONSE(imethodresponseE); }

		throw new CIMXMLParseException();
	}

	/**
	 * parseMULTIRSP
	 * 
	 * @param pSimpleRspE
	 * @return CIMResponse
	 * @throws CIMXMLParseException
	 */
	public static CIMResponse parseMULTIRSP(Element pSimpleRspE) throws CIMXMLParseException {
		// <!ELEMENT MULTIRSP (SIMPLERSP,SIMPLERSP+)>

		Element[] multiRespElementA = searchNodes(pSimpleRspE, "SIMPLERSP");
		if (multiRespElementA != null && multiRespElementA.length > 0) {
			CIMResponse multiRsp = new CIMResponse();
			for (int i = 0; i < multiRespElementA.length; i++) {
				Element methodresponseE = multiRespElementA[i];
				CIMResponse rsp = parseSIMPLERSP(methodresponseE);
				rsp.setMethod("SIMPLERSP");
				multiRsp.addResponse(rsp);
			}
			return multiRsp;
		}

		throw new CIMXMLParseException("SIMPLRESP nodes aren't found!");
	}

	/**
	 * parseSIMPLEREQ
	 * 
	 * @param pSimpleReqE
	 * @return CIMRequest
	 * @throws CIMXMLParseException
	 */
	public static CIMRequest parseSIMPLEREQ(Element pSimpleReqE) throws CIMXMLParseException {
		// <!ELEMENT SIMPLEREQ (METHODCALL|IMETHODCALL)>

		// METHODCALL
		Element methodcallE = (Element) searchFirstNode(pSimpleReqE, "METHODCALL");
		if (methodcallE != null) { return parseMETHODCALL(methodcallE); }

		// IMETHODCALL
		Element imethodcallE = (Element) searchFirstNode(pSimpleReqE, "IMETHODCALL");
		if (imethodcallE != null) { return parseIMETHODCALL(imethodcallE); }

		throw new CIMXMLParseException("METHODCALL or IMETHODCALL nodes aren't found!");
	}

	/**
	 * parseMULTIREQ
	 * 
	 * @param pMultiReqE
	 * @return CIMRequest
	 * @throws CIMXMLParseException
	 */
	public static CIMRequest parseMULTIREQ(Element pMultiReqE) throws CIMXMLParseException {
		// <!ELEMENT MULTIREQ (SIMPLEREQ,SIMPLEREQ+)>

		Element[] methodReqElementA = searchNodes(pMultiReqE, "MULTIREQ");
		if (methodReqElementA != null && methodReqElementA.length > 0) {
			CIMRequest multiReq = new CIMRequest();
			for (int i = 0; i < methodReqElementA.length; i++) {
				Element methodrequestE = methodReqElementA[i];
				CIMRequest req = parseSIMPLEREQ(methodrequestE);
				req.setMethod("MULTIPREQ");

				multiReq.addRequest(req);
			}
			return multiReq;
		}
		throw new CIMXMLParseException("MULTIREQ nodes aren't found!");
	}

	/**
	 * parseMETHODCALL
	 * 
	 * @param pMethodCallE
	 * @return CIMRequest
	 * @throws CIMXMLParseException
	 */
	public static CIMRequest parseMETHODCALL(Element pMethodCallE) throws CIMXMLParseException {
		// <!ELEMENT METHODCALL
		// ((LOCALCLASSPATH|LOCALINSTANCEPATH),PARAMVALUE*)>
		// <!ATTLIST METHODCALL
		// %CIMName;>

		CIMRequest request = new CIMRequest();
		String methodname = pMethodCallE.getAttribute("CIMName");
		request.setMethodName(methodname);

		// EXPMETHODCALL
		boolean localclasspathFound = false;
		Element localclasspathE = (Element) searchFirstNode(pMethodCallE, "LOCALCLASSPATH");
		if (localclasspathE != null) {
			CIMObjectPath path = parseLOCALCLASSPATH(localclasspathE);

			request.setObjectPath(path);
			localclasspathFound = true;
		}

		Element localinstancepathE = (Element) searchFirstNode(pMethodCallE, "LOCALINSTANCEPATH");
		if (localinstancepathE != null) {
			CIMObjectPath path = parseLOCALINSTANCEPATH(localinstancepathE);

			request.setObjectPath(path);
		} else {
			if (!localclasspathFound) throw new CIMXMLParseException(
					"LOCALCLASSPATH or LOCALINSTANCEPATH not found!");
		}

		Element[] paramValueElementA = searchNodes(pMethodCallE, "PARAMVALUE");

		if (paramValueElementA != null && paramValueElementA.length > 0) {
			CIMArgument<?>[] argA = new CIMArgument[paramValueElementA.length];
			for (int i = 0; i < paramValueElementA.length; i++) {
				Element paramvalueE = paramValueElementA[i];
				argA[i] = parsePARAMVALUE(paramvalueE);
			}
			request.addParamValue(argA);
		}

		return request;
	}

	/**
	 * parseIMETHODCALL
	 * 
	 * @param pIMethodCallE
	 * @return CIMRequest
	 * @throws CIMXMLParseException
	 */
	public static CIMRequest parseIMETHODCALL(Element pIMethodCallE) throws CIMXMLParseException {
		// <!ELEMENT IMETHODCALL (LOCALNAMESPACEPATH,IPARAMVALUE*)>
		// <!ATTLIST IMETHODCALL
		// %CIMName;>

		CIMRequest request = new CIMRequest();
		String methodname = pIMethodCallE.getAttribute("NAME"); // ebak:
		// CIMName->NAME
		request.setMethodName(methodname);

		// METHODCALL
		Element localnamespacepathE = (Element) searchFirstNode(pIMethodCallE, "LOCALNAMESPACEPATH");
		if (localnamespacepathE != null) {
			String nameSpace = parseLOCALNAMESPACEPATH(localnamespacepathE);
			request.setNameSpace(nameSpace);
		}

		Element[] iParamValElementA = searchNodes(pIMethodCallE, "IPARAMVALUE"); // ebak:
		// PARAMVALUE->IPARAMVALUE

		if (iParamValElementA != null && iParamValElementA.length > 0) {
			CIMArgument<?>[] argA = new CIMArgument[iParamValElementA.length];
			for (int i = 0; i < iParamValElementA.length; i++) {
				Element paramvalueE = iParamValElementA[i];
				CIMArgument<Object> arg = parseIPARAMVALUE(paramvalueE);
				/*
				 * ebak: local nameSpacePath should be added to those reference
				 * arguments which don't contain namespace
				 */
				Object value = arg.getValue();
				if (value instanceof CIMObjectPath) {
					CIMObjectPath op = (CIMObjectPath) value;
					if (op.getNamespace() == null || op.getNamespace().length() == 0) {
						arg = new CIMArgument<Object>(arg.getName(), arg.getDataType(),
						// CIMObjectPath(String scheme, String host, String
								// port, String namespace, String objectName,
								// CIMProperty[] keys)
								new CIMObjectPath(op.getScheme(), op.getHost(), op.getPort(),
										request.getNameSpace(), op.getObjectName(), op.getKeys()));
					}
				}
				argA[i] = arg;
			}
			request.addParamValue(argA);
			return request;
		}

		throw new CIMXMLParseException("IPARAMVALUE node not found!");
	}

	/**
	 * parseSIMPLEEXPREQ
	 * 
	 * @param pSimpleExpReqE
	 * @return CIMRequest
	 * @throws CIMXMLParseException
	 */
	public static CIMRequest parseSIMPLEEXPREQ(Element pSimpleExpReqE) throws CIMXMLParseException {
		// <!ELEMENT SIMPLEEXPREQ (METHODRESPONSE)>

		// EXPMETHODCALL
		Element expmethodcallE = (Element) searchFirstNode(pSimpleExpReqE, "EXPMETHODCALL");
		if (expmethodcallE != null) { return parseEXPMETHODCALL(expmethodcallE); }

		throw new CIMXMLParseException("EXPMETHODCALL node not found!");
	}

	/**
	 * parseMULTIEXPREQ
	 * 
	 * @param pMultiExpReqE
	 * @return CIMRequest
	 * @throws CIMXMLParseException
	 */
	public static CIMRequest parseMULTIEXPREQ(Element pMultiExpReqE) throws CIMXMLParseException {
		// <!ELEMENT SIMPLEEXPREQ (METHODRESPONSE)>

		Element[] methodReqElementA = searchNodes(pMultiExpReqE, "SIMPLEEXREQ");
		if (methodReqElementA != null && methodReqElementA.length > 0) {
			CIMRequest multiReq = new CIMRequest();
			for (int i = 0; i < methodReqElementA.length; i++) {
				Element methodrequestE = methodReqElementA[i];
				CIMRequest req = parseSIMPLEEXPREQ(methodrequestE);
				req.setMethod("SIMPLEEXPREQ");

				multiReq.addRequest(req);
			}
			return multiReq;
		}
		throw new CIMXMLParseException("SIMPLEEXREQ node not found!");
	}

	/**
	 * parseEXPMETHODCALL
	 * 
	 * @param pExpMethodCallE
	 * @return CIMRequest
	 * @throws CIMXMLParseException
	 */
	public static CIMRequest parseEXPMETHODCALL(Element pExpMethodCallE)
			throws CIMXMLParseException {
		// <!ELEMENT EXPMETHODCALL (EXPPARAMVALUE*)>

		// EXPMETHODCALL
		CIMRequest request = new CIMRequest();
		Element[] paramValElementA = searchNodes(pExpMethodCallE, "EXPPARAMVALUE");
		Vector<CIMInstance> v = new Vector<CIMInstance>();
		if (paramValElementA != null) {
			for (int i = 0; i < paramValElementA.length; i++) {
				Element expparamvalueE = paramValElementA[i];
				CIMInstance inst = parseEXPPARAMVALUE(expparamvalueE);
				v.add(inst);
			}
		}
		request.addParamValue(v);
		return request;
	}

	/**
	 * parseEXPPARAMVALUE
	 * 
	 * @param pExpParamValueE
	 * @return CIMInstance
	 * @throws CIMXMLParseException
	 */
	public static CIMInstance parseEXPPARAMVALUE(Element pExpParamValueE)
			throws CIMXMLParseException {
		// <!ELEMENT EXPPARAMVALUE (INSTANCE)>
		// <!ATTLIST EXPPARAMVALUE
		// %CIMName;>
		// INSTANCE
		Element instanceE = (Element) searchFirstNode(pExpParamValueE, "INSTANCE");
		if (instanceE != null) {
			CIMInstance inst = parseINSTANCE(instanceE);
			return inst;
		}
		throw new CIMXMLParseException("INSTANCE node not found!");
	}

	/**
	 * parseMETHODRESPONSE
	 * 
	 * @param pMethodResponseE
	 * @return CIMResponse
	 * @throws CIMXMLParseException
	 */
	public static CIMResponse parseMETHODRESPONSE(Element pMethodResponseE)
			throws CIMXMLParseException {
		// <!ELEMENT METHODRESPONSE (ERROR|(RETURNVALUE?,PARAMVALUE*))>

		CIMResponse response = new CIMResponse();

		// ERROR
		Element errorE = (Element) searchFirstNode(pMethodResponseE, "ERROR");
		if (errorE != null) {
			WBEMException exception = parseERROR(errorE);
			response.setError(exception);
			return response;
		}

		// RETURNVALUE
		Element[] retValElementA = searchNodes(pMethodResponseE, "RETURNVALUE");
		if (retValElementA != null && retValElementA.length > 0) {
			Vector<Object> v = new Vector<Object>();
			for (int i = 0; i < retValElementA.length; i++) {
				Element returnvalueE = retValElementA[i];
				v.add(parseRETURNVALUE(returnvalueE));
			}
			response.setReturnValue(v);
		}

		// PARAMVALUE
		Element[] paramValElementA = searchNodes(pMethodResponseE, "PARAMVALUE");
		if (paramValElementA != null && paramValElementA.length > 0) {
			Vector<Object> v = new Vector<Object>();
			for (int i = 0; i < paramValElementA.length; i++) {
				Element paramvalueE = paramValElementA[i];
				CIMArgument<?> arg = parsePARAMVALUE(paramvalueE);
				v.add(arg);
			}
			response.addParamValue(v);
		}

		// PARAMVALUE (ESS fix)
		if (retValElementA != null && retValElementA.length > 0) {
			for (int i = 0; i < retValElementA.length; i++) {
				Element retValE = retValElementA[i];

				paramValElementA = searchNodes(retValE, "PARAMVALUE");
				if (paramValElementA != null && paramValElementA.length > 0) {
					Vector<CIMArgument<Object>> v = new Vector<CIMArgument<Object>>();
					for (int j = 0; j < paramValElementA.length; j++) {
						Element paramvalueE = paramValElementA[j];
						CIMArgument<Object> arg = parsePARAMVALUE(paramvalueE);
						v.add(arg);
					}
					response.addParamValue(v);
				}
			}
		}

		return response;
	}

	/**
	 * parseIMETHODRESPONSE
	 * 
	 * @param pIMethodResponseE
	 * @return CIMResponse
	 * @throws CIMXMLParseException
	 */
	public static CIMResponse parseIMETHODRESPONSE(Element pIMethodResponseE)
			throws CIMXMLParseException {
		// <!ELEMENT IMETHODRESPONSE (ERROR|(IRETURNVALUE?, PARAMVALUE*))>

		CIMResponse response = new CIMResponse();
		// ERROR
		Element errorE = (Element) searchFirstNode(pIMethodResponseE, "ERROR");
		if (errorE != null) {
			WBEMException exception = parseERROR(errorE);
			response.setError(exception);
			return response;
		}

		// IRETURNVALUE
		Element[] retValElementA = searchNodes(pIMethodResponseE, "IRETURNVALUE");
		if (retValElementA != null && retValElementA.length > 0) {
			for (int i = 0; i < retValElementA.length; i++) {
				Element ireturnvalueE = retValElementA[i];

				Vector<Object> rtnV = parseIRETURNVALUE(ireturnvalueE);
				response.setReturnValue(rtnV);
			}
		}

		// PARAMVALUE
		Element[] paramValElementA = searchNodes(pIMethodResponseE, "PARAMVALUE");
		if (paramValElementA != null && paramValElementA.length > 0) {
			Vector<Object> v = new Vector<Object>();
			for (int i = 0; i < paramValElementA.length; i++) {
				Element paramvalueE = paramValElementA[i];
				CIMArgument<?> arg = parsePARAMVALUE(paramvalueE);
				v.add(arg);
			}
			response.addParamValue(v);
		}

		return response;
	}

	/**
	 * parseERROR
	 * 
	 * @param pErrorE
	 * @return WBEMException
	 * @throws CIMXMLParseException
	 */
	public static WBEMException parseERROR(Element pErrorE) throws CIMXMLParseException {
		// <!ELEMENT ERROR (INSTANCE*)>
		// <!ATTLSIT ERROR %CODE;%DESCRIPTION;>

		Attr error_codeA = (Attr) searchAttribute(pErrorE, "CODE");
		String code = error_codeA.getNodeValue();
		int errorCode = 0;
		try {
			if (code.length() > 0) errorCode = Integer.parseInt(code);
		} catch (Exception e) {
			LogAndTraceBroker.getBroker().trace(Level.WARNING,
					"exception while parsing error code from XML", e);
			errorCode = WBEMException.CIM_ERR_FAILED;
		}
		Attr error_descriptionA = (Attr) searchAttribute(pErrorE, "DESCRIPTION");
		String description = "";
		if (error_descriptionA != null) {
			description = error_descriptionA.getNodeValue();
		}

		Vector<Object> rtnV = new Vector<Object>();

		// INSTANCE
		Element[] instElementA = searchNodes(pErrorE, "INSTANCE");
		if (instElementA != null && instElementA.length > 0) {
			for (int i = 0; i < instElementA.length; i++) {
				Element instanceE = instElementA[i];
				CIMInstance inst = parseINSTANCE(instanceE);
				rtnV.add(inst);
			}
		}

		// throw new CIMException(CIMException.getErrorName(errorCode),
		// description.substring(description.indexOf(':')+1));
		if (!rtnV.isEmpty()) return new WBEMException(errorCode, "ErrorCode:" + errorCode
				+ " description:" + description, rtnV.toArray(new CIMInstance[0]));
		return new WBEMException(errorCode, "ErrorCode:" + errorCode + " description:"
				+ description);
	}

	/**
	 * parseRETURNVALUE
	 * 
	 * @param pRetValE
	 * @return Object
	 * @throws CIMXMLParseException
	 */
	public static Object parseRETURNVALUE(Element pRetValE) throws CIMXMLParseException {
		// <!ELEMENT RETURNVALUE (VALUE|VALUE.REFERENCE)>
		// <!ATTLSIT RETURNVALUE %PARAMTYPE;>
		// ebak: embedded object support: different parsing of VALUE sub element
		if (searchFirstNode(pRetValE, "VALUE") != null) {
			EmbObjHandler embObjHandler = new EmbObjHandler(pRetValE);
			return embObjHandler.getValue();
		}
		TypedValue typedVal = parseSingleValue(pRetValE, VALUEREF);
		if (typedVal.getType() == null) throw new CIMXMLParseException(
				"VALUE or VALUE.REFERENCE nodes not found!");
		Object value = typedVal.getValue();
		return value;
	}

	/**
	 * parseIRETURNVALUE
	 * 
	 * @param pIRetValE
	 * @return Vector
	 * @throws CIMXMLParseException
	 */
	public static Vector<Object> parseIRETURNVALUE(Element pIRetValE) throws CIMXMLParseException {
		// <!ELEMENT IRETURNVALUE (CLASSNAME*|INSTANCENAME*|INSTANCEPATH*
		// |VALUE*|VALUE.OBJECTWITHPATH*|VALUE.OBJECTWITHLOCALPATH*|VALUE.OBJECT*
		// |OBJECTPATH*|QUALIFIER.DECLARATION*|VALUE.ARRAY|VALUE.REFERENCE*
		// |CLASS*|INSTANCE*|VALUE.NAMEDINSTANCE*|VALUE.INSTANCEWITHPATH)>

		Vector<Object> rtnV = new Vector<Object>();

		// CLASS
		Element[] classElementA = searchNodes(pIRetValE, "CLASS");
		if (classElementA != null && classElementA.length > 0) {
			for (int i = 0; i < classElementA.length; i++) {
				Element classE = classElementA[i];
				CIMClass c = parseCLASS(classE);
				rtnV.add(c);
			}
		}

		// INSTANCE
		Element[] instElementA = searchNodes(pIRetValE, "INSTANCE");
		if (instElementA != null && instElementA.length > 0) {
			for (int i = 0; i < instElementA.length; i++) {
				Element instanceE = instElementA[i];
				CIMInstance inst = parseINSTANCE(instanceE);
				rtnV.add(inst);
			}
		}

		// CLASSNAME
		Element[] classNameElementA = searchNodes(pIRetValE, "CLASSNAME");
		if (classNameElementA != null && classNameElementA.length > 0) {
			for (int i = 0; i < classNameElementA.length; i++) {
				Element classnameE = classNameElementA[i];
				CIMObjectPath op = parseCLASSNAME(classnameE);
				rtnV.add(op);
			}
		}

		// INSTANCENAME
		Element[] instNameElementA = searchNodes(pIRetValE, "INSTANCENAME");
		if (instNameElementA != null && instNameElementA.length > 0) {
			for (int i = 0; i < instNameElementA.length; i++) {
				Element instancenameE = instNameElementA[i];
				CIMObjectPath op = parseINSTANCENAME(instancenameE);
				rtnV.add(op);
			}
		}

		// INSTANCEPATH
		Element[] instpathElementA = searchNodes(pIRetValE, "INSTANCEPATH");
		if (instpathElementA != null && instpathElementA.length > 0) {
			for (int i = 0; i < instpathElementA.length; i++) {
				Element instancePathE = instpathElementA[i];
				CIMObjectPath op = parseINSTANCEPATH(instancePathE);
				rtnV.add(op);
			}
		}

		// OBJECTPATH
		Element[] objPathElementA = searchNodes(pIRetValE, "OBJECTPATH");
		if (objPathElementA != null && objPathElementA.length > 0) {
			for (int i = 0; i < objPathElementA.length; i++) {
				Element objectpathE = objPathElementA[i];
				CIMObjectPath op = parseOBJECTPATH(objectpathE);
				rtnV.add(op);
			}
		}

		// VALUE
		// Vector valueV = searchNodes(ireturnvalueE, "VALUE");
		// if (valueV != null) {
		// for (int i=0; i<valueV.size(); i++) {
		// Element valueE = (Element)valueV.elementAt(i);
		// String valueStr = parseVALUE(valueE);
		// // cannot construct a CIMValue without type information
		// }
		// }

		// VALUE.ARRAY
		// Vector valuearrayV = searchNodes(ireturnvalueE, "VALUE.ARRAY");
		// if (valuearrayV != null) {
		// for (int i=0; i<valuearrayV.size(); i++) {
		// Element valuearrayE = (Element)valuearrayV.elementAt(i);
		// // cannot construct a CIMValue without type information
		// }
		// }

		// VALUE.REFERENCE
		Element[] valRefElementA = searchNodes(pIRetValE, "VALUE.REFERENCE");
		if (valRefElementA != null && valRefElementA.length > 0) {
			for (int i = 0; i < valRefElementA.length; i++) {
				Element valuereferenceE = valRefElementA[i];
				CIMObjectPath op = parseVALUEREFERENCE(valuereferenceE);
				rtnV.add(op);
			}
		}

		// VALUE.OBJECT
		Element[] valObjElementA = searchNodes(pIRetValE, "VALUE.OBJECT");
		if (valObjElementA != null && valObjElementA.length > 0) {
			for (int i = 0; i < valObjElementA.length; i++) {
				Element valueobjectE = valObjElementA[i];
				CIMNamedElementInterface obj = parseVALUEOBJECT(valueobjectE);
				rtnV.add(obj);
			}
		}

		// VALUE.NAMEDINSTANCE
		Element[] valNamedInstElementA = searchNodes(pIRetValE, "VALUE.NAMEDINSTANCE");
		if (valNamedInstElementA != null && valNamedInstElementA.length > 0) {
			for (int i = 0; i < valNamedInstElementA.length; i++) {
				Element valuenamedisntanceE = valNamedInstElementA[i];
				CIMInstance inst = parseVALUENAMEDINSTANCE(valuenamedisntanceE);
				rtnV.add(inst);
			}
		}

		// VALUE.INSTANCEWITHPATH
		Element[] valInstWithPathElementA = searchNodes(pIRetValE, "VALUE.INSTANCEWITHPATH");
		if (valInstWithPathElementA != null && valInstWithPathElementA.length > 0) {
			for (int i = 0; i < valInstWithPathElementA.length; i++) {
				Element valueinstancewithpathE = valInstWithPathElementA[i];
				CIMInstance inst = parseVALUEINSTANCEWITHPATH(valueinstancewithpathE);
				rtnV.add(inst);
			}
		}

		// VALUE.OBJECTWITHPATH
		Element[] valObjWithPathElementA = searchNodes(pIRetValE, "VALUE.OBJECTWITHPATH");
		if (valObjWithPathElementA != null && valObjWithPathElementA.length > 0) {
			for (int i = 0; i < valObjWithPathElementA.length; i++) {
				Element valueobjectwithpathE = valObjWithPathElementA[i];
				CIMNamedElementInterface namedIF = parseVALUEOBJECTWITHPATH(valueobjectwithpathE);
				rtnV.add(namedIF);
			}
		}

		// VALUE.OBJECTWITHLOCALPATH
		Element[] valObjWithLocalPathElementA = searchNodes(pIRetValE, "VALUE.OBJECTWITHLOCALPATH");
		if (valObjWithLocalPathElementA != null && valObjWithLocalPathElementA.length > 0) {
			for (int i = 0; i < valObjWithLocalPathElementA.length; i++) {
				Element valueobjectwithlocalpathE = valObjWithLocalPathElementA[i];
				CIMNamedElementInterface namedIF = parseVALUEOBJECTWITHLOCALPATH(valueobjectwithlocalpathE);
				rtnV.add(namedIF);
			}
		}

		// QUALIFIER.DECLARATION
		Element[] qualiDeclElementA = searchNodes(pIRetValE, "QUALIFIER.DECLARATION");
		if (qualiDeclElementA != null && qualiDeclElementA.length > 0) {
			for (int i = 0; i < qualiDeclElementA.length; i++) {
				Element qualifierdeclarationE = qualiDeclElementA[i];
				CIMQualifierType<Object> o = parseQUALIFIERDECLARATION(qualifierdeclarationE);
				rtnV.add(o);
			}
		}
		return rtnV;
	}

	/**
	 * parseObject
	 * 
	 * @param pRootE
	 * @return Object
	 * @throws CIMXMLParseException
	 */
	public static Object parseObject(Element pRootE) throws CIMXMLParseException {
		Object o = null;
		String nodeName = pRootE.getNodeName();
		if (nodeName.equalsIgnoreCase("INSTANCE")) {
			o = parseINSTANCE(pRootE);
		} else if (nodeName.equalsIgnoreCase("VALUE.NAMEDINSTANCE")) {
			o = parseVALUENAMEDINSTANCE(pRootE);
		} else if (nodeName.equalsIgnoreCase("VALUE.NAMEDOBJECT")) {
			o = parseVALUENAMEDOBJECT(pRootE);
		} else if (nodeName.equalsIgnoreCase("VALUE.OBJECTWITHPATH")) {
			o = parseVALUEOBJECTWITHPATH(pRootE);
		} else if (nodeName.equalsIgnoreCase("VALUE.OBJECTWITHLOCALPATH")) {
			o = parseVALUEOBJECTWITHLOCALPATH(pRootE);
		} else if (nodeName.equalsIgnoreCase("CLASS")) {
			o = parseCLASS(pRootE);
		} else if (nodeName.equalsIgnoreCase("CLASSPATH")) {
			o = parseCLASSPATH(pRootE);
		} else if (nodeName.equalsIgnoreCase("LOCALCLASSPATH")) {
			o = parseLOCALCLASSPATH(pRootE);
		} else if (nodeName.equalsIgnoreCase("OBJECTPATH")) {
			o = parseOBJECTPATH(pRootE);
		} else if (nodeName.equalsIgnoreCase("CLASSNAME")) {
			o = parseCLASSNAME(pRootE);
		} else if (nodeName.equalsIgnoreCase("INSTANCEPATH")) {
			o = parseINSTANCEPATH(pRootE);
		} else if (nodeName.equalsIgnoreCase("LOCALINSTANCEPATH")) {
			o = parseLOCALINSTANCEPATH(pRootE);
		} else if (nodeName.equalsIgnoreCase("INSTANCENAME")) {
			o = parseINSTANCENAME(pRootE);
		} else if (nodeName.equalsIgnoreCase("QUALIFIER")) {
			o = parseQUALIFIER(pRootE);
		} else if (nodeName.equalsIgnoreCase("PROPERTY")) {
			o = parsePROPERTY(pRootE);
		} else if (nodeName.equalsIgnoreCase("PROPERTY.ARRAY")) {
			o = parsePROPERTYARRAY(pRootE);
		} else if (nodeName.equalsIgnoreCase("PROPERTY.REFERENCE")) {
			o = parsePROPERTYREFERENCE(pRootE);
		} else if (nodeName.equalsIgnoreCase("METHOD")) {
			o = parseMETHOD(pRootE);
		} else if (nodeName.equalsIgnoreCase("PARAMETER")) {
			o = parsePARAMETER(pRootE);
		} else if (nodeName.equalsIgnoreCase("PARAMETER.REFERENCE")) {
			o = parsePARAMETERREFERENCE(pRootE);
		} else if (nodeName.equalsIgnoreCase("PARAMETER.ARRAY")) {
			o = parsePARAMETERARRAY(pRootE);
		} else if (nodeName.equalsIgnoreCase("PARAMETER.REFARRAY")) {
			o = parsePARAMETERREFARRAY(pRootE);
		}
		return o;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * searchNodes
	 * 
	 * @param pParentE
	 * @param pTagName
	 * @return Element[]
	 */
	public static Element[] searchNodes(Element pParentE, String pTagName) {
		// return all of child nodes immediately below the parent
		// return null if not found
		NodeList nl = pParentE.getChildNodes();
		if (nl == null || nl.getLength() == 0) return null;
		Vector<Node> resElementV = new Vector<Node>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equals(pTagName)) {
				resElementV.add(n);
			}
		}
		return resElementV.toArray(new Element[0]);
	}

	/**
	 * searchFirstNode
	 * 
	 * @param pParentE
	 * @param pTagName
	 * @return Node
	 */
	public static Node searchFirstNode(Element pParentE, String pTagName) {
		// return the first node which matches to the specific name
		// return null if not found
		NodeList nl = pParentE.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equals(pTagName)) { return n; }
		}
		return null;
	}

	/**
	 * searchAttribute
	 * 
	 * @param pParentN
	 * @param pAttrName
	 * @return Node
	 */
	public static Node searchAttribute(Node pParentN, String pAttrName) {
		// return the attribute node with the specific name
		NamedNodeMap nnm = pParentN.getAttributes();
		return nnm.getNamedItem(pAttrName);
	}

	/**
	 * searchFirstChild
	 * 
	 * @param pParentE
	 * @return Node
	 */
	public static Node searchFirstChild(Element pParentE) {
		// return the first node which matches to the specific name
		// return null if not found
		return pParentE.getFirstChild();
	}

	/**
	 * createJavaObject
	 * 
	 * @param pTypeStr
	 * @param pValue
	 * @return Object
	 * @throws CIMXMLParseException
	 */
	public static Object createJavaObject(String pTypeStr, String pValue)
			throws CIMXMLParseException {
		// return a java object with the specific type
		if (pTypeStr == null) pTypeStr = MOF.DT_STR;
		if (MOF.NULL.equalsIgnoreCase(pTypeStr)) return null;
		Object o = null;
		CIMDataType cimType = parseTypeStr(pTypeStr, false);
		int radix = 10;

		if (pTypeStr.toLowerCase().startsWith("sint") || pTypeStr.toLowerCase().startsWith("uint")) {
			pValue = pValue.toLowerCase();
			if (pValue.startsWith("0x") || pValue.startsWith("+0x") || pValue.startsWith("-0x")) {
				radix = 16;
				if (pValue.startsWith("-")) pValue = "-" + pValue.substring(3);
				else pValue = pValue.substring(pValue.indexOf('x') + 1);
			}
		}

		switch (cimType.getType()) {
			case CIMDataType.UINT8:
				o = new UnsignedInteger8(Short.parseShort(pValue, radix));
				break;
			case CIMDataType.UINT16:
				o = new UnsignedInteger16(Integer.parseInt(pValue, radix));
				break;
			case CIMDataType.UINT32:
				o = new UnsignedInteger32(Long.parseLong(pValue, radix));
				break;
			case CIMDataType.UINT64:
				o = new UnsignedInteger64(new BigInteger(pValue, radix));
				break;
			case CIMDataType.SINT8:
				o = Byte.valueOf(pValue, radix);
				break;
			case CIMDataType.SINT16:
				o = Short.valueOf(pValue, radix);
				break;
			case CIMDataType.SINT32:
				o = Integer.valueOf(pValue, radix);
				break;
			case CIMDataType.SINT64:
				o = Long.valueOf(pValue, radix);
				break;
			case CIMDataType.STRING:
				o = pValue;
				break;
			case CIMDataType.BOOLEAN:
				o = Boolean.valueOf(pValue);
				break;
			case CIMDataType.REAL32:
				o = new Float(pValue);
				break;
			case CIMDataType.REAL64:
				o = new Double(pValue);
				break;
			case CIMDataType.DATETIME:
				o = getDateTime(pValue);
				break;
			case CIMDataType.REFERENCE:
				o = new CIMObjectPath(pValue);
				break;
			case CIMDataType.CHAR16:
				o = Character.valueOf(pValue.charAt(0));
				break;
			// case CIMDataType.OBJECT: o = new CIMInstance(); break; //TODO
			// case CIMDataType.CLASS: o = new CIMClass(value); break; //TODO
		}
		return o;
	}

	private static CIMDateTime getDateTime(String pValue) throws CIMXMLParseException {
		try {
			return new CIMDateTimeAbsolute(pValue);
		} catch (IllegalArgumentException eAbs) {
			try {
				return new CIMDateTimeInterval(pValue);
			} catch (IllegalArgumentException eInt) {
				throw new CIMXMLParseException("Failed to parse dateTime string: " + pValue + "!\n"
						+ "CIMDateTimeAbsolute parsing error:\n" + eAbs.getMessage() + "\n"
						+ "CIMDateTimeInterval parsing error:\n" + eInt.getMessage());
			}
		}
	}

}
