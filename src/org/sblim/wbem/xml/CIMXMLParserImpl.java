/**
 * CIMXMLParserImpl.java
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
 * 1464860    2006-05-15  lupusalex    No default value for VALUETYPE assumed
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1547910    2006-09-05  ebak         parseIMETHODCALL() CIMObjectPath parsing problem
 * 1547908    2006-09-05  ebak         parseKEYBINDING() creates incorrect reference type
 * 1545915    2006-09-05  ebak         Wrong parsing of IMETHODCALL request
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.xml;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.cim.CIMArgument;
import org.sblim.wbem.cim.CIMClass;
import org.sblim.wbem.cim.CIMDataType;
import org.sblim.wbem.cim.CIMDateTime;
import org.sblim.wbem.cim.CIMElement;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMFlavor;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMMethod;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.cim.CIMParameter;
import org.sblim.wbem.cim.CIMProperty;
import org.sblim.wbem.cim.CIMQualifier;
import org.sblim.wbem.cim.CIMQualifierType;
import org.sblim.wbem.cim.CIMScope;
import org.sblim.wbem.cim.CIMValue;
import org.sblim.wbem.cim.Numeric;
import org.sblim.wbem.cim.UnsignedInt16;
import org.sblim.wbem.cim.UnsignedInt32;
import org.sblim.wbem.cim.UnsignedInt64;
import org.sblim.wbem.cim.UnsignedInt8;
import org.sblim.wbem.util.SessionProperties;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

public class CIMXMLParserImpl {

	public static CIMMessage parseCIM(Element cimE) throws CIMXMLParseException {
		// <!ELEMENT CIM (MESSAGE|DECLARATION)>
		// <!ATTLIST CIM %CIMVERSION;%DTDVERSION;>
		Attr cim_cimversionA = (Attr)searchAttribute(cimE, "CIMVERSION");
		String cimversion = cim_cimversionA.getNodeValue();
		
		Attr cim_dtdversionA = (Attr)searchAttribute(cimE, "DTDVERSION");
		String dtdversion = cim_dtdversionA.getNodeValue();
		
		// MESSAGE
		Element messageE = (Element)searchFirstNode(cimE, "MESSAGE");
		if (messageE != null) {
			CIMMessage message = parseMESSAGE(cimversion, dtdversion, messageE);
			message.setCIMVersion(cimversion);
			message.setDTDVersion(dtdversion);
			return message;
		}

		// TODO : DECLARATION 
		
		throw new CIMXMLParseException();
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	// Value Elements
	//////////////////////////////////////////////////////////////////////////////////////////

	
	public static String parseVALUE(Element valueE) throws CIMXMLParseException {
		// <! ELEMENT VALUE (#PCDATA)>
		
		Text t = (Text)valueE.getFirstChild();
		if (t != null) {
			String valueStr = t.getNodeValue();
			return valueStr;
		}
		return null;
	}
	
	public static Vector parseVALUEARRAY(Element valuearrayE) throws CIMXMLParseException {
		// <! ELEMENT VALUE.ARRAY (VALUE*)>
		
		Vector v = new Vector();
		Vector valueV = searchNodes(valuearrayE, "VALUE");
		if (valueV != null) {
			for (int i=0; i<valueV.size(); i++) {
				Element valueE = (Element)valueV.elementAt(i);
				String valueStr = parseVALUE(valueE);
				v.add(valueStr);
			}
		}
		return v;
	}

	public static CIMObjectPath parseVALUEREFERENCE(Element valuereferenceE) throws CIMXMLParseException {
		// <!ELEMENT VALUE.REFERENCE (CLASSPATH|LOCALCLASSPATH|CLASSNAME|INSTANCEPATH|LOCALINSTANCEPATH|INSTANCENAME)>
		
		// CLASSPATH
		Element classpathE = (Element)searchFirstNode(valuereferenceE, "CLASSPATH");
		if (classpathE != null) {
			CIMObjectPath op = parseCLASSPATH(classpathE);
			return op;
		}

		// LOCALCLASSPATH
		Element localclasspathE = (Element)searchFirstNode(valuereferenceE, "LOCALCLASSPATH");
		if (localclasspathE != null) {
			CIMObjectPath op = parseLOCALCLASSPATH(localclasspathE);
			return op;
		}

		// CLASSNAME
		Element classnameE = (Element)searchFirstNode(valuereferenceE, "CLASSNAME");
		if (classnameE != null) {
			CIMObjectPath op = parseCLASSNAME(classnameE);
			return op;
		}

		// INSTANCEPATH
		Element instancepathE = (Element)searchFirstNode(valuereferenceE, "INSTANCEPATH");
		if (instancepathE != null) {
			CIMObjectPath op = parseINSTANCEPATH(instancepathE);
			return op;
		}
		
		// LOCALINSTANCEPATH
		Element localinstancepathE = (Element)searchFirstNode(valuereferenceE, "LOCALINSTANCEPATH");
		if (localinstancepathE != null) {
			CIMObjectPath op = parseLOCALINSTANCEPATH(localinstancepathE);
			return op;
		}

		// INSTANCENAME
		Element instancenameE = (Element)searchFirstNode(valuereferenceE, "INSTANCENAME");
		if (instancenameE != null) {
			CIMObjectPath op = parseINSTANCENAME(instancenameE);
			return op;
		}

		throw new CIMXMLParseException();
	}

	public static Vector parseVALUEREFARRAY(Element valuerefarrayE) throws CIMXMLParseException {
		// <! ELEMENT VALUE.REFARRAY (VALUE.REFERENCE*)>
		
		Vector v = new Vector();
		Vector valuereferenceV = searchNodes(valuerefarrayE, "VALUE.REFERENCE");
		for (int i=0; i<valuereferenceV.size(); i++) {
			Element valuereferenceE = (Element)valuereferenceV.elementAt(i);
			CIMObjectPath op = parseVALUEREFERENCE(valuereferenceE);
			v.add(new CIMValue(op, CIMDataType.getPredefinedType(CIMDataType.REFERENCE_ARRAY)));
		}
		return v;
	}

	public static CIMElement parseVALUEOBJECT(Element valueobjectE) throws CIMXMLParseException {
		// <! ELEMENT VALUE.OBJECT (CLASS|INSTANCE)>
		
		// CLASS
		Element classE = (Element)searchFirstNode(valueobjectE, "CLASS");
		if (classE != null) {
			CIMClass obj = parseCLASS(classE);
			return obj;
		}

		// INSTANCE
		Element instanceE = (Element)searchFirstNode(valueobjectE, "INSTANCE");
		if (instanceE != null) {
			CIMInstance obj = parseINSTANCE(instanceE);
			return obj;
		}

		throw new CIMXMLParseException();		
	}

	public static CIMElement parseVALUENAMEDINSTANCE(Element valuenamedinstanceE) throws CIMXMLParseException {
		// <! ELEMENT VALUE.NAMEDINSTANCE (INSTANCENAME,INSTANCE)>
		
		// INSTANCENAME
		Element instancenameE = (Element)searchFirstNode(valuenamedinstanceE, "INSTANCENAME");
		if (instancenameE == null) {
			throw new CIMXMLParseException();
		}
		CIMObjectPath op = parseINSTANCENAME(instancenameE);

		// INSTANCE
		Element instanceE = (Element)searchFirstNode(valuenamedinstanceE, "INSTANCE");
		if (instanceE == null) {
			throw new CIMXMLParseException();
		}
		CIMInstance inst = parseINSTANCE(instanceE, op); //BB mod
		return inst;
	}

	public static CIMElement parseVALUENAMEDOBJECT(Element valuenamedobjectE) throws CIMXMLParseException {
		// <! ELEMENT VALUE.NAMEDOBJECT (CLASS|(INSTANCENAME,INSTANCE))>
		
		// CLASS
		Element classE = (Element)searchFirstNode(valuenamedobjectE, "CLASS");
		if (classE != null) {
			CIMClass obj = parseCLASS(classE);
			return obj;
		}

		// INSTANCENAME
		Element instancenameE = (Element)searchFirstNode(valuenamedobjectE, "INSTANCENAME");
		if (instancenameE != null) {
			CIMObjectPath op = parseINSTANCENAME(instancenameE);

			// INSTANCE
			Element instanceE = (Element)searchFirstNode(valuenamedobjectE, "INSTANCE");
			if (instanceE == null) {
				throw new CIMXMLParseException();
			}
			CIMInstance inst = parseINSTANCE(instanceE, op); //BB mod
			return inst;
		}
		
		throw new CIMXMLParseException();
	}

	public static CIMElement parseVALUEOBJECTWITHPATH(Element valueobjectwithpathE) throws CIMXMLParseException {
		// <! ELEMENT VALUE.OBJECTWITHPATH ((CLASSPATH,CLASS)|(INSTANCEPATH,INSTANCE))>
		
		// CLASSPATH
		Element classpathE = (Element)searchFirstNode(valueobjectwithpathE, "CLASSPATH");
		if (classpathE != null) {
//			CIMObjectPath op = parseCLASSPATH(classpathE);
			
			// CLASS
			Element classE = (Element)searchFirstNode(valueobjectwithpathE, "CLASS");
			if (classE == null) {
				throw new CIMXMLParseException();
			}
			// TODO parse CLASS with ObjectPath
			CIMClass obj = parseCLASS(classE);
			return obj;
		}

		// INSTANCEPATH
		Element instancepathE = (Element)searchFirstNode(valueobjectwithpathE, "INSTANCEPATH");
		if (instancepathE != null) {
			CIMObjectPath op = parseINSTANCEPATH(instancepathE);

			// INSTANCE
			Element instanceE = (Element)searchFirstNode(valueobjectwithpathE, "INSTANCE");
			if (instanceE == null) {
				throw new CIMXMLParseException();
			}
			CIMInstance inst = parseINSTANCE(instanceE, op); //BB mod
			return inst;
		}
		
		throw new CIMXMLParseException();
	}

	public static CIMElement parseVALUEOBJECTWITHLOCALPATH(Element valueobjectwithlocalpathE) throws CIMXMLParseException {
		// <! ELEMENT VALUE.OBJECTWITHPATH ((LOCALCLASSPATH,CLASS)|(LOCALINSTANCEPATH,INSTANCE))>
		
		// LOCALCLASSPATH
		Element localclasspathE = (Element)searchFirstNode(valueobjectwithlocalpathE, "LOCALCLASSPATH");
		if (localclasspathE != null) {
//			CIMObjectPath op = parseLOCALCLASSPATH(localclasspathE);
			
			// CLASS
			Element classE = (Element)searchFirstNode(valueobjectwithlocalpathE, "CLASS");
			if (classE == null) {
				throw new CIMXMLParseException();
			}
			// TODO parse class with path
			CIMClass obj = parseCLASS(classE);
			return obj;
		}

		// LOCALINSTANCEPATH
		Element localinstancepathE = (Element)searchFirstNode(valueobjectwithlocalpathE, "LOCALINSTANCEPATH");
		if (localinstancepathE != null) {
			CIMObjectPath op = parseLOCALINSTANCEPATH(localinstancepathE);

			// INSTANCE
			Element instanceE = (Element)searchFirstNode(valueobjectwithlocalpathE, "INSTANCE");
			if (instanceE == null) {
				throw new CIMXMLParseException();
			}
			CIMInstance inst = parseINSTANCE(instanceE, op); //BB mod
			return inst;
		}
		
		throw new CIMXMLParseException();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Naming and Location elements
	//////////////////////////////////////////////////////////////////////////////////////////
	
	public static CIMNameSpace parseNAMESPACEPATH(Element namespacepathE) throws CIMXMLParseException {
		// <!ELEMENT NAMESPACEPATH (HOST,LOCALNAMESPACEPATH)>
		
		// HOST
		Element hostE = (Element)searchFirstNode(namespacepathE, "HOST");
		if (hostE == null) {
			throw new CIMXMLParseException();
		}
		String host = parseHOST(hostE);

		// LOCALNAMESPACE
		Element localnamespacepathE = (Element)searchFirstNode(namespacepathE, "LOCALNAMESPACEPATH");
		if (localnamespacepathE == null) {
			throw new CIMXMLParseException();
		}
		CIMNameSpace ns = parseLOCALNAMESPACEPATH(localnamespacepathE);
		ns.setHost(host);
		
		return ns;
	}
	
	public static CIMNameSpace parseLOCALNAMESPACEPATH(Element localnamespaceE) throws CIMXMLParseException {
		// <!ELEMENT LOCALNAMESPACE (NAMESPACE+))>
		
		Vector namespaceV = searchNodes(localnamespaceE, "NAMESPACE");
		if (namespaceV == null) {
			throw new CIMXMLParseException();
		}		
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<namespaceV.size(); i++) {
			Element namespaceE = (Element)namespaceV.elementAt(i);
			String s = parseNAMESPACE(namespaceE);
			if (i>0)
				sb.append("/"+s);
			else
				sb.append(s);
		}
		CIMNameSpace ns = null;
		ns = new CIMNameSpace();
		ns.setNameSpace(sb.toString());
		return ns;
	}

	public static String parseHOST(Element hostE) throws CIMXMLParseException {
		// <!ELEMENT HOST (#PCDATA)>
		
		Text valueT = (Text)searchFirstChild(hostE);
		String host = valueT.getNodeValue(); 
		return host;
	}

	public static String parseNAMESPACE(Element namespaceE) throws CIMXMLParseException {
		// <!ELEMENT NAMESPACE EMPTY>
		// <!ATTLIST NAMESPACE %NAME;>
		
		Attr namespace_nameA = (Attr)searchAttribute(namespaceE, "NAME");
		String n = namespace_nameA.getValue();
		return n;
	}

	public static CIMObjectPath parseCLASSPATH(Element classpathE) throws CIMXMLParseException {
		// <!ELEMENT CLASSPATH (NAMESPACEPATH,CLASSNAME)>
		
		// NAMESPACEPATH
		Element namespacepathE = (Element)searchFirstNode(classpathE, "NAMESPACEPATH");
		if (namespacepathE == null) {
			throw new CIMXMLParseException();
		}
		CIMNameSpace ns = parseNAMESPACEPATH(namespacepathE);

		// CLASSNAME
		Element classnameE = (Element)searchFirstNode(classpathE, "CLASSNAME");
		if (classnameE == null) {
			throw new CIMXMLParseException();
		}
		CIMObjectPath op = parseCLASSNAME(classnameE);

		op.setHost((ns.getHost()));
		op.setNameSpace(ns.getNameSpace());
		return op;
	}

	public static CIMObjectPath parseLOCALCLASSPATH(Element classpathE) throws CIMXMLParseException {
		// <!ELEMENT LOCALCLASSPATH (LOCALNAMESPACEPATH,CLASSNAME)>
		
		// NAMESPACEPATH
		Element localnamespacepathE = (Element)searchFirstNode(classpathE, "LOCALNAMESPACEPATH");
		if (localnamespacepathE == null) {
			throw new CIMXMLParseException();
		}
		CIMNameSpace ns = parseLOCALNAMESPACEPATH(localnamespacepathE);

		// CLASSNAME
		Element classnameE = (Element)searchFirstNode(classpathE, "CLASSNAME");
		if (classnameE == null) {
			throw new CIMXMLParseException();
		}
		CIMObjectPath op = parseCLASSNAME(classnameE);

		op.setHost((ns.getHost()));
		op.setNameSpace(ns.getNameSpace());
		return op;
	}

	public static CIMObjectPath parseCLASSNAME(Element classnameE) throws CIMXMLParseException {
		// <!ELEMENT CLASSNAME EMPTY>
		// <!ATTLIST CLASSNAME %NAME;>
		
		Attr classname_nameA = (Attr)searchAttribute(classnameE, "NAME");
		String opClassName = classname_nameA.getNodeValue();
		CIMObjectPath op = new CIMObjectPath();
		op.setObjectName(opClassName);
		return op;
	}

	public static CIMObjectPath parseINSTANCEPATH(Element instancepathE) throws CIMXMLParseException {
		// <!ELEMENT INSTANCEPATH (NAMESPACEPATH,INSTANCENAME)>
		
		// NAMESPACEPATH
		Element namespacepathE = (Element)searchFirstNode(instancepathE, "NAMESPACEPATH");
		if (namespacepathE == null) {
			throw new CIMXMLParseException();
		}
		CIMNameSpace ns = parseNAMESPACEPATH(namespacepathE);
	
		// INSTANCENAME
		Element instancenameE = (Element)searchFirstNode(instancepathE, "INSTANCENAME");
		if (instancenameE == null) {
			throw new CIMXMLParseException();
		}
		CIMObjectPath op = parseINSTANCENAME(instancenameE);
		op.setHost((ns.getHost()));
		op.setNameSpace(ns.getNameSpace());
		return op;
	}
	
	public static CIMObjectPath parseLOCALINSTANCEPATH(Element localinstancepathE) throws CIMXMLParseException {
		// <!ELEMENT LOCALINSTANCEPATH (LOCALNAMESPACEPATH,INSTANCENAME)>
		
		// LOCALNAMESPACEPATH
		Element localnamespacepathE = (Element)searchFirstNode(localinstancepathE, "LOCALNAMESPACEPATH");
		if (localnamespacepathE == null) {
			throw new CIMXMLParseException();
		}
		CIMNameSpace ns = parseLOCALNAMESPACEPATH(localnamespacepathE);
		
		// INSTANCENAME
		Element instancenameE = (Element)searchFirstNode(localinstancepathE, "INSTANCENAME");
		if (instancenameE == null) {
			throw new CIMXMLParseException();
		}
		CIMObjectPath op = parseINSTANCENAME(instancenameE);
		op.setHost((ns.getHost()));
		op.setNameSpace(ns.getNameSpace());
		return op;
	}

	public static CIMObjectPath parseINSTANCENAME(Element instancenameE) throws CIMXMLParseException {
		// <!ELEMENT INSTANCENAME (KEYBINDING*|KEYVALUE?|VALUE.REFERNCE?)>
		// <!ATTLIST INSTANCENAME %CLASSNAME;> 
		Attr instance_classnameA = (Attr)searchAttribute(instancenameE, "CLASSNAME");
		String opClassName = instance_classnameA.getNodeValue();
		CIMObjectPath op = new CIMObjectPath();
		op.setObjectName(opClassName);
		
		// KEYBINDING
		Vector keybindingV = searchNodes(instancenameE, "KEYBINDING");
		if (keybindingV != null) {
			for (int i=0; i<keybindingV.size(); i++) {
				Element keybindingE = (Element)keybindingV.elementAt(i);
				CIMProperty p = parseKEYBINDING(keybindingE);
				op.addKey(p.getName(), p.getValue());
			}
			return op;
		}
		
		// KEYVALUE
//		Element keyvalueE = (Element)searchFirstNode(instancenameE, "KEYVALUE");
//		if (keyvalueE != null) {
//			CIMValue pValue = parseKEYVALUE(keyvalueE);
//			// it doesn't make sense without a property name
//		}

		// VALUE.REFERENCE
//		Element valuereferenceE = (Element)searchFirstNode(instancenameE, "VALUE.REFERENCE");
//		if (valuereferenceE != null) {
//			CIMObjectPath ref = parseVALUEREFERENCE(valuereferenceE);
//			// it doesn't make sense without a property name
//		}
		
		// an empty OP without any property
		return op;
	}

	public static CIMObjectPath parseOBJECTPATH(Element objectpathE) throws CIMXMLParseException {
		// <!ELEMENT OBJECTPATH (INSTANCEPATH|CLASSPATH) >

		// INSTANCEPATH
		Element instancepathE = (Element)searchFirstNode(objectpathE, "INSTANCEPATH");
		if (instancepathE != null) {
			CIMObjectPath op = parseINSTANCEPATH(instancepathE);
			return op;
		}
		
		// CLASSPATH
		Element classpathE = (Element)searchFirstNode(objectpathE, "CLASSPATH");
		if (classpathE != null) {
			CIMObjectPath op = parseCLASSPATH(classpathE);
			return op;
		}

		throw new CIMXMLParseException();
	}

	public static CIMProperty parseKEYBINDING(Element keybindingE) throws CIMXMLParseException {
		// <!ELEMENT KEYBINDING (KEYVALUE|VALUE.REFERENCE) >
		// <!ATTLIST KEYBINDING %NAME;>
		
		Attr keybinding_nameA = (Attr)searchAttribute(keybindingE, "NAME");
		String pName = keybinding_nameA.getValue();
		CIMProperty p = new CIMProperty(pName);

		// KEYVALUE
		Element keyvalueE = (Element)searchFirstNode(keybindingE, "KEYVALUE");
		if (keyvalueE != null) {
			CIMValue pValue = parseKEYVALUE(keyvalueE);
			p.setValue(pValue);
			return p;
		}
		
		// VALUE.REFERENCE
		Element valuereferenceE = (Element)searchFirstNode(keybindingE, "VALUE.REFERENCE");
		if (valuereferenceE != null) {
			CIMObjectPath op = parseVALUEREFERENCE(valuereferenceE);
			/*
			 * ebak: the reference type should contain the name of the referenced class:
			 * CIMDataType.getPredefinedType(CIMDataType.REFERENCE) ->
			 * new CIMDataType(op.getObjectName())
			 */
			p.setValue(new CIMValue(op, new CIMDataType(op.getObjectName())));
			return p;
		}

		throw new CIMXMLParseException();
	}

	public static CIMValue parseKEYVALUE(Element keyvalueE) throws CIMXMLParseException {
		// <!ELEMENT KEYVALUE (#PCDATA)>
		// <!ATTLIST KEYVALUE VALUETYPE (string|boolean|numeric) 'string')
		 
		Attr keyvalue_valuetypeA = (Attr)searchAttribute(keyvalueE, "VALUETYPE");
		String objectType = "string"; //fix: default valuetype of keyvalue to string
		if (keyvalue_valuetypeA!=null) objectType = keyvalue_valuetypeA.getValue();
		Text valueT = (Text)searchFirstChild(keyvalueE);
		Object value = null;
		if (valueT != null) {
			String objectValue = valueT.getNodeValue();
			value = createJavaObject(objectType, objectValue);
		}
		return new CIMValue(value, CIMDataType.getDataType(objectType, false));
	}
	


	//////////////////////////////////////////////////////////////////////////////////////////
	// Object Definition Elements
	//////////////////////////////////////////////////////////////////////////////////////////

	public static CIMClass parseCLASS(Element classE) throws CIMXMLParseException {
		// <!ELEMENT CLASS (QUALIFIER*, (PROPERTY|PROPERTY.ARRAY|PROPERTY.REFERENCE)*,METHOD*)>
		// <!ATTLIST CLASS %NAME;%SUPERCLASS;>

		CIMClass c = new CIMClass();
		Attr class_nameA = (Attr)searchAttribute(classE, "NAME");
		String name = class_nameA.getNodeValue();
		c.setName(name);
		

//		Attr superclass_nameA = (Attr)searchAttribute(classE, "SUPERCLASS");
//		String supername = superclass_nameA.getNodeValue();

		String supername = classE.getAttribute("SUPERCLASS");
		if (supername != null && supername.length() > 0)
			c.setSuperClass(supername);

		// QUALIFIER
		Vector qualifierV = searchNodes(classE, "QUALIFIER");
		if (qualifierV != null) {
			Vector v = new Vector();
			for (int i=0; i<qualifierV.size(); i++) {
				Element qualifierE = (Element)qualifierV.elementAt(i);
				CIMQualifier q = parseQUALIFIER(qualifierE);
				v.add(q);
			}
			c.setQualifiers(v);
		}
		
		Vector v = new Vector();
		
		// PROPERTY
		Vector propertyV = searchNodes(classE, "PROPERTY");
		if (propertyV != null) {
			for (int i=0; i<propertyV.size(); i++) {
				Element propertyE = (Element)propertyV.elementAt(i);
				CIMProperty p = parsePROPERTY(propertyE);
				v.add(p);
			}
		}

		// PROPERTY.ARRAY
		Vector propertyarrayV = searchNodes(classE, "PROPERTY.ARRAY");
		if (propertyarrayV != null) {
			for (int i=0; i<propertyarrayV.size(); i++) {
				Element propertyarrayE = (Element)propertyarrayV.elementAt(i);
				CIMProperty p = parsePROPERTYARRAY(propertyarrayE);
				v.add(p);
			}
		}

		// PROPERTY.REFERENCE
		Vector propertyreferenceV = searchNodes(classE, "PROPERTY.REFERENCE");
		if (propertyreferenceV != null) {
			for (int i=0; i<propertyreferenceV.size(); i++) {
				Element propertyreferenceE = (Element)propertyreferenceV.elementAt(i);
				CIMProperty p = parsePROPERTYREFERENCE(propertyreferenceE);
				v.add(p);
			}
		}

		c.setProperties(v);
		
		// METHOD
		Vector methodV = searchNodes(classE, "METHOD");
		if (methodV != null) {
			Vector methods = new Vector();
			for (int i=0; i<methodV.size(); i++) {
				Element methodE = (Element)methodV.elementAt(i);
				CIMMethod p = parseMETHOD(methodE);
				methods.add(p);
			}
			c.setMethods(methods);
		}

		return c;
	}

	public static CIMMethod parseMETHOD(Element methodE) throws CIMXMLParseException {
//		<!ELEMENT METHOD (QUALIFIER*,(PARAMETER|PARAMETER.REFERENCE|PARAMETER.ARRAY|PARAMETER.REFARRAY)*)> 
//		<!ATTLIST METHOD  
//			 %CIMName; 
//			 %CIMType;          #IMPLIED  
//			 %ClassOrigin; 
//			 %Propagated;>
		CIMMethod method = new CIMMethod();
		
		String name = methodE.getAttribute("NAME");
		method.setName(name);
		
		String typeStr = methodE.getAttribute("TYPE");
		//TODO make sure that no array type is returned as a valid parameter
		CIMDataType type = CIMDataType.getDataType(typeStr, false);
		method.setType(type);

		String classorigin = methodE.getAttribute("CLASSORIGIN");
		if (classorigin != null && classorigin.length() > 0)
			method.setOriginClass(classorigin);

		String propagated = methodE.getAttribute("PROPAGATED");
		if ("true".equalsIgnoreCase(propagated))
			method.setPropagated(true);

		// QUALIFIER
		Vector qualifierV = searchNodes(methodE, "QUALIFIER");
		if (qualifierV != null) {
			Vector v = new Vector();
			for (int i=0; i<qualifierV.size(); i++) {
				Element qualifierE = (Element)qualifierV.elementAt(i);
				CIMQualifier q = parseQUALIFIER(qualifierE);
				v.add(q);
			}
			method.setQualifiers(v);
		}
		
		// PARAMETER
		Vector v = new Vector();
		Vector parameterV = searchNodes(methodE, "PARAMETER");
		if (parameterV != null) {
			for (int i=0; i<parameterV.size(); i++) {
				Element parameterE = (Element)parameterV.elementAt(i);
				CIMParameter p = parsePARAMETER(parameterE);
				v.add(p);
			}
		}

		Vector parameterreferenceV = searchNodes(methodE, "PARAMETER.REFERENCE");
		if (parameterreferenceV != null) {
			for (int i=0; i<parameterreferenceV.size(); i++) {
				Element parameterE = (Element)parameterreferenceV.elementAt(i);
				CIMParameter p = parsePARAMETERREFERENCE(parameterE);
				v.add(p);
			}
		}

		Vector parameterarrayV = searchNodes(methodE, "PARAMETER.ARRAY");
		if (parameterarrayV != null) {
			for (int i=0; i<parameterarrayV.size(); i++) {
				Element parameterE = (Element)parameterarrayV.elementAt(i);
				CIMParameter p = parsePARAMETERARRAY(parameterE);
				v.add(p);
			}
		}

		Vector parameterrefarrayV = searchNodes(methodE, "PARAMETER.REFARRAY");
		if (parameterrefarrayV != null) {
			for (int i=0; i<parameterrefarrayV.size(); i++) {
				Element parameterE = (Element)parameterrefarrayV.elementAt(i);
				CIMParameter p = parsePARAMETERREFARRAY(parameterE);
				v.add(p);
			}
		}
		
		method.setParameters(v);
		return method;
	}
	
	//TODO clean up the code of parsePARAMETER, parsePARAEMTERREFERENCE, parsePARAMETERARRAY and parsePARAMETERARRAYREF
	public static CIMParameter parsePARAMETER(Element parameterE) throws CIMXMLParseException {
		CIMParameter parameter = new CIMParameter();
		String name = parameterE.getAttribute("NAME");
		parameter.setName(name);

		String typeStr = parameterE.getAttribute("TYPE");
		//TODO make sure that no array type is returned as a valid parameter
		CIMDataType type = CIMDataType.getDataType(typeStr, false);
		parameter.setType(type);
		
		// QUALIFIER
		Vector qualifierV = searchNodes(parameterE, "QUALIFIER");
		if (qualifierV != null) {
			Vector v = new Vector();
			for (int i=0; i<qualifierV.size(); i++) {
				Element qualifierE = (Element)qualifierV.elementAt(i);
				CIMQualifier q = parseQUALIFIER(qualifierE);
				v.add(q);
			}
			parameter.setQualifiers(v);
		}
				
		return parameter;
	}

	//TODO clean up the code of parsePARAMETER, parsePARAEMTERREFERENCE, parsePARAMETERARRAY and parsePARAMETERARRAYREF
	public static CIMParameter parsePARAMETERREFERENCE(Element parameterE) throws CIMXMLParseException {
		CIMParameter parameter = new CIMParameter();
		String name = parameterE.getAttribute("NAME");
		parameter.setName(name);

		String referenceClass = parameterE.getAttribute("REFERENCECLASS");
		parameter.setType(new CIMDataType((referenceClass != null)?referenceClass:""));
		
		// QUALIFIER
		Vector qualifierV = searchNodes(parameterE, "QUALIFIER");
		if (qualifierV != null) {
			Vector v = new Vector();
			for (int i=0; i<qualifierV.size(); i++) {
				Element qualifierE = (Element)qualifierV.elementAt(i);
				CIMQualifier q = parseQUALIFIER(qualifierE);
				v.add(q);
			}
			parameter.setQualifiers(v);
		}
				
		return parameter;
	}

	//TODO clean up the code of parsePARAMETER, parsePARAEMTERREFERENCE, parsePARAMETERARRAY and parsePARAMETERARRAYREF
	public static CIMParameter parsePARAMETERARRAY(Element parameterE) throws CIMXMLParseException {
		CIMParameter parameter = new CIMParameter();
		String name = parameterE.getAttribute("NAME");
		parameter.setName(name);

		String typeStr = parameterE.getAttribute("TYPE");
		
		int arraySize = CIMDataType.SIZE_UNLIMITED;
		String arraySizeStr = parameterE.getAttribute("ARRAYSIZE");
		try {
			if (arraySizeStr.length() > 0)
				arraySize = Integer.parseInt(arraySizeStr);
		} catch (Exception e) {
			Logger logger = SessionProperties.getGlobalProperties().getLogger();
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "exception while reading parameter array size from the XML", e);
			}
		}

		CIMDataType type = CIMDataType.getDataType(typeStr, true);
		if (arraySize > CIMDataType.SIZE_SINGLE)
			type = new CIMDataType(type.getType(), arraySize);
	
		parameter.setType(type);				
		
		// QUALIFIER
		Vector qualifierV = searchNodes(parameterE, "QUALIFIER");
		if (qualifierV != null) {
			Vector v = new Vector();
			for (int i=0; i<qualifierV.size(); i++) {
				Element qualifierE = (Element)qualifierV.elementAt(i);
				CIMQualifier q = parseQUALIFIER(qualifierE);
				v.add(q);
			}
			parameter.setQualifiers(v);
		}
				
		return parameter;
	}

	//TODO clean up the code of parsePARAMETER, parsePARAEMTERREFERENCE, parsePARAMETERARRAY and parsePARAMETERARRAYREF
	public static CIMParameter parsePARAMETERREFARRAY(Element parameterE) throws CIMXMLParseException {
		CIMParameter parameter = new CIMParameter();
		String name = parameterE.getAttribute("NAME");
		parameter.setName(name);

		String referenceClass = parameterE.getAttribute("REFERENCECLASS");
		
		int arraySize = CIMDataType.SIZE_UNLIMITED;
		String arraySizeStr = parameterE.getAttribute("ARRAYSIZE");
		try {
			if (arraySizeStr.length() > 0)
				arraySize = Integer.parseInt(arraySizeStr);
		}
		catch (Exception e) {
			Logger logger = SessionProperties.getGlobalProperties().getLogger();
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "exeception while parsing parameter array reference size", e);
			}
		}

		CIMDataType type = new CIMDataType((referenceClass != null)?referenceClass:"", arraySize);
//		if (arraySize > CIMDataType.SIZE_SINGLE)
//			type = new CIMDataType(type.getType(), arraySize);
	
		parameter.setType(type);				
		
		// QUALIFIER
		Vector qualifierV = searchNodes(parameterE, "QUALIFIER");
		if (qualifierV != null) {
			Vector v = new Vector();
			for (int i=0; i<qualifierV.size(); i++) {
				Element qualifierE = (Element)qualifierV.elementAt(i);
				CIMQualifier q = parseQUALIFIER(qualifierE);
				v.add(q);
			}
			parameter.setQualifiers(v);
		}
				
		return parameter;
	}
	
//    public static static CIMInstance parseINSTANCE(Element instanceE) throws Exception {
    public static CIMInstance parseINSTANCE(Element instanceE) throws CIMXMLParseException {
		return parseINSTANCE(instanceE, null); //BB mod
	} //BB mod

//BB mod	public static CIMInstance parseINSTANCE(Element instanceE) throws CIMXMLParseException {
	public static CIMInstance parseINSTANCE(Element instanceE, CIMObjectPath op) throws CIMXMLParseException { //BB mod
		// <!ELEMENT INSTANCE (QUALIFIER*, (PROPERTY|PROPERTY.ARRAY|PROPERTY.REFERENCE)*)>
		// <!ATTLIST INSTANCE %CLASSNAME;>

//BB mod		CIMInstance inst = new CIMInstance();
		CIMInstance inst = new CIMInstance(op); //BB mod
		String className = instanceE.getAttribute("CLASSNAME");
 

		if (className != null && className.length() > 0)
			inst.setName(className);
		
		// QUALIFIER
		Vector qualifierV = searchNodes(instanceE, "QUALIFIER");
		if (qualifierV != null) {
			Vector v = new Vector();
			for (int i=0; i<qualifierV.size(); i++) {
				Element qualifierE = (Element)qualifierV.elementAt(i);
				CIMQualifier q = parseQUALIFIER(qualifierE);
				v.add(q);
			}
			inst.setQualifiers(v);
		}
		
		Vector v = new Vector();
		
		// PROPERTY
		Vector propertyV = searchNodes(instanceE, "PROPERTY");
		if (propertyV != null) {
			for (int i=0; i<propertyV.size(); i++) {
				Element propertyE = (Element)propertyV.elementAt(i);
				CIMProperty p = parsePROPERTY(propertyE);
				v.add(p);
			}
		}

		// PROPERTY.ARRAY
		Vector propertyarrayV = searchNodes(instanceE, "PROPERTY.ARRAY");
		if (propertyarrayV != null) {
			for (int i=0; i<propertyarrayV.size(); i++) {
				Element propertyarrayE = (Element)propertyarrayV.elementAt(i);
				CIMProperty p = parsePROPERTYARRAY(propertyarrayE);
				v.add(p);
			}
		}

		// PROPERTY.REFERENCE
		Vector propertyreferenceV = searchNodes(instanceE, "PROPERTY.REFERENCE");
		if (propertyreferenceV != null) {
			for (int i=0; i<propertyreferenceV.size(); i++) {
				Element propertyreferenceE = (Element)propertyreferenceV.elementAt(i);
				CIMProperty p = parsePROPERTYREFERENCE(propertyreferenceE);
				v.add(p);
			}
		}

		inst.setProperties(v);
		return inst;
	}
	
	public static CIMQualifier parseQUALIFIER(Element qualifierE) throws CIMXMLParseException {
		// <!ELEMENT QUALIFIER (VALUE|VALUE.ARRAY)>
		// <!ATTLIST QUALIFIER %NAME;%TYPE;%PROPAGATED;%QUALIFIERFLAVOR;>
		
		CIMQualifier q = new CIMQualifier();
		String name = qualifierE.getAttribute("NAME");
		q.setName(name);
		String valueTypeStr = qualifierE.getAttribute("TYPE");
		//

		String valuePropagatedStr = qualifierE.getAttribute("PROPAGATED");
		if (valuePropagatedStr != null && valuePropagatedStr.equalsIgnoreCase("true")) {
			q.setPropagated(true);
		}

		String valueOverridableStr = qualifierE.getAttribute("OVERRIDABLE");
		if (valueOverridableStr != null && valueOverridableStr.equalsIgnoreCase("true"))
			q.addFlavor(new CIMFlavor(CIMFlavor.ENABLEOVERRIDE));
		else
			q.addFlavor(new CIMFlavor(CIMFlavor.DISABLEOVERRIDE));
		
		String valueToSubClassStr = qualifierE.getAttribute("TOSUBCLASS");
		if (valueToSubClassStr != null && valueToSubClassStr.equalsIgnoreCase("true"))
			q.addFlavor(new CIMFlavor(CIMFlavor.TOSUBCLASS));
		else
			q.addFlavor(new CIMFlavor(CIMFlavor.RESTRICTED));
		
		String valueTranslatableStr = qualifierE.getAttribute("TRANSLATABLE");
		if (valueTranslatableStr != null && valueTranslatableStr.equalsIgnoreCase("true")) 
			q.addFlavor(new CIMFlavor(CIMFlavor.TRANSLATE));
		
		// VALUE
		Element valueE = (Element)searchFirstNode(qualifierE, "VALUE");
		if (valueE != null) {
			String valueStr = parseVALUE(valueE);
			Object o = createJavaObject(valueTypeStr, valueStr);
			CIMValue value = new CIMValue(o, CIMDataType.getDataType(valueTypeStr, false));
			q.setValue(value);
//			return q;
		}
		
		// VALUE.ARRAY
		Element valuearrayE = (Element)searchFirstNode(qualifierE, "VALUE.ARRAY");
		if (valuearrayE != null) {
			Vector v = parseVALUEARRAY(valuearrayE);
			Vector v2 = new Vector();
			for (int i=0; i<v.size(); i++) {
				String valueStr = (String)v.elementAt(i);
				Object o = createJavaObject(valueTypeStr, valueStr);
				v2.add(o);
			}
			CIMValue value = new CIMValue(v2, CIMDataType.getDataType(valueTypeStr, true));
			q.setValue(value);
//			return q;
		}
		return q;
//		throw new CIMXMLParseException();
	}
	
	public static CIMQualifierType parseQUALIFIERDECLARATION(Element qualifiertypeE) throws CIMXMLParseException {
//		<!ELEMENT QUALIFIER.DECLARATION (SCOPE?,(VALUE|VALUE.ARRAY)?)> 
//		<!ATTLIST QUALIFIER.DECLARATION  
//			 %CIMName; 
//			 %CIMType;                       #REQUIRED  
//			 ISARRAY        (true|false)     #IMPLIED 
//			 %ArraySize; 
//			 %QualifierFlavor;> 

		CIMQualifierType qualifiertype = new CIMQualifierType();
		String name = qualifiertypeE.getAttribute("NAME");
		qualifiertype.setName(name);
		String valueTypeStr = qualifiertypeE.getAttribute("TYPE");
		if (valueTypeStr == null || valueTypeStr.length()==0) throw new CIMXMLParseException();
		
		String isarrayStr = qualifiertypeE.getAttribute("ISARRAY");
		boolean isArray = (isarrayStr != null && isarrayStr.equalsIgnoreCase("true"))?true:false;
		int arraySize = CIMDataType.SIZE_UNLIMITED;
		
		String arraySizeStr = qualifiertypeE.getAttribute("ARRAYSIZE");
		try {
			if (arraySizeStr != null && arraySizeStr.length() > 0) {
				isArray = true;
				arraySize = Integer.parseInt(arraySizeStr);
			}
		} catch (Exception e) {
			Logger logger = SessionProperties.getGlobalProperties().getLogger();
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "exception while parsing array size", e);
			}
		}		
		
		CIMDataType valueType = CIMDataType.getDataType(valueTypeStr, isArray);
		if (isArray)
			valueType = new CIMDataType(valueType.getType(), arraySize);
		
		qualifiertype.setType(valueType);
		
		Vector scopeV = searchNodes(qualifiertypeE, "SCOPE");
		if (scopeV != null) {
			for (int i=0; i < scopeV.size(); i++) {
				Element scopeE = (Element)scopeV.elementAt(i);
				Vector scopeList = parseSCOPE(scopeE);
				Iterator iter = scopeList.iterator();
				while (iter.hasNext()) {
					CIMScope scope = (CIMScope)iter.next();
					qualifiertype.addScope(scope);						 
				}
			}
		}

		String valueOverridableStr = qualifiertypeE.getAttribute("OVERRIDABLE");
		if (valueOverridableStr != null && valueOverridableStr.equalsIgnoreCase("true"))
			qualifiertype.addFlavor(new CIMFlavor(CIMFlavor.ENABLEOVERRIDE));
		else
			qualifiertype.addFlavor(new CIMFlavor(CIMFlavor.DISABLEOVERRIDE));
		
		String valueToSubClassStr = qualifiertypeE.getAttribute("TOSUBCLASS");
		if (valueToSubClassStr != null && valueToSubClassStr.equalsIgnoreCase("true"))
			qualifiertype.addFlavor(new CIMFlavor(CIMFlavor.TOSUBCLASS));
		else
			qualifiertype.addFlavor(new CIMFlavor(CIMFlavor.RESTRICTED));
		
		String valueTranslatableStr = qualifiertypeE.getAttribute("TRANSLATABLE");
		if (valueTranslatableStr != null && valueTranslatableStr.equalsIgnoreCase("true")) 
			qualifiertype.addFlavor(new CIMFlavor(CIMFlavor.TRANSLATE));

		// VALUE
		Element valueE = (Element)searchFirstNode(qualifiertypeE, "VALUE");
		if (valueE != null) {
			String valueStr = parseVALUE(valueE);
			CIMValue value; 
			if (!valueStr.equals("NULL")) {
				Object o = createJavaObject(valueTypeStr, valueStr);
				value = new CIMValue(o, valueType);
			}
			else {
				value = new CIMValue(null, valueType);
			}
			qualifiertype.setDefaultValue(value);
		}

		// VALUE.ARRAY
		Element valuearrayE = (Element)searchFirstNode(qualifiertypeE, "VALUE.ARRAY");
		if (valuearrayE != null) {
			Vector v = parseVALUEARRAY(valuearrayE);
			Vector v2 = new Vector();
			for (int i=0; i<v.size(); i++) {
				String valueStr = (String)v.elementAt(i);
				Object o = null; 
				if (!valueStr.equals("NULL")) 
					o = createJavaObject(valueTypeStr, valueStr);
					
				v2.add(o);
			}
			CIMValue value = new CIMValue(v2, valueType);
			qualifiertype.setDefaultValue(value);
		}
		
		return qualifiertype;
	}
	
	
	public static Vector parseSCOPE(Element scopeE) throws CIMXMLParseException {
		Vector scopeV = new Vector();
		String clazz = scopeE.getAttribute("CLASS");
		if (clazz != null && clazz.equalsIgnoreCase("true"))
			scopeV.add(CIMScope.getScope(CIMScope.CLASS));
		String assoc = scopeE.getAttribute("ASSOCIATION");
		if (assoc != null && assoc.equalsIgnoreCase("true"))
			scopeV.add(CIMScope.getScope(CIMScope.ASSOCIATION));
		String ref = scopeE.getAttribute("REFERENCE");
		if (ref != null && ref.equalsIgnoreCase("true"))
			scopeV.add(CIMScope.getScope(CIMScope.REFERENCE));
		String prop = scopeE.getAttribute("PROPERTY");
		if (prop != null && prop.equalsIgnoreCase("true"))
			scopeV.add(CIMScope.getScope(CIMScope.PROPERTY));
		String method = scopeE.getAttribute("METHOD");
		if (method != null && method.equalsIgnoreCase("true"))
			scopeV.add(CIMScope.getScope(CIMScope.METHOD));
		String param = scopeE.getAttribute("PARAMETER");
		if (param != null && param.equalsIgnoreCase("true"))
			scopeV.add(CIMScope.getScope(CIMScope.PARAMETER));
		String indication = scopeE.getAttribute("INDICATION");
		if (indication  != null && indication.equalsIgnoreCase("true"))
			scopeV.add(CIMScope.getScope(CIMScope.INDICATION));
		
		return scopeV;
	}
	
	public static CIMProperty parsePROPERTY(Element propertyE) throws CIMXMLParseException {
		// <!ELEMENT PROPERTY (QUALIFIER*,VALUE?)>
		// <!ATTLIST PROPERTY %NAME;%TYPE;%CLASSORIGIN;%PROPAGATED;>
		
		CIMProperty p = new CIMProperty();
		Attr property_nameA = (Attr)searchAttribute(propertyE, "NAME");
		String name = property_nameA.getNodeValue();
		p.setName(name);
//		System.out.println("property.name="+name);	
		Attr property_typeA = (Attr)searchAttribute(propertyE, "TYPE");
		String valueTypeStr = property_typeA.getNodeValue();

		// TODO: make sure no array types are accepted as valid data types
		p.setType(CIMDataType.getDataType(valueTypeStr, false));


		String classoriginStr = propertyE.getAttribute("CLASSORIGIN");
		if (classoriginStr != null && classoriginStr.length()>0 )		
			p.setOriginClass(classoriginStr);

//		Attr property_propagatedA = (Attr)searchAttribute(propertyE, "PROPAGATED");
		String propagatedStr = propertyE.getAttribute("PROPAGATED");
		try {
			p.setPropagated(Boolean.getBoolean(propagatedStr));
		}
		catch (Exception e) {
		}
		
		boolean embedded = false;

		// check for embedded object attribute
//		Attr property_embeddedA = (Attr)searchAttribute(propertyE, 
//								"EMBEDDEDOBJECT");
		String embeddedobjectStr = propertyE.getAttribute("EMBEDDEDOBJECT");
		if (embeddedobjectStr != null && embeddedobjectStr.equalsIgnoreCase("object") ) {
		    embedded = true;
		}

		// QUALIFIER
		Vector qualifierV = searchNodes(propertyE, "QUALIFIER");
		if (qualifierV != null) {
			for (int i=0; i<qualifierV.size(); i++) {
				Element qualifierE = (Element)qualifierV.elementAt(i);
				CIMQualifier q = parseQUALIFIER(qualifierE);
				// probably superfluous but you never know...
				if (q.getName().equalsIgnoreCase("EmbeddedObject")) 
				    embedded = true;
				p.addQualifier(q);
			}
		}
		
		// VALUE
		Element valueE = (Element)searchFirstNode(propertyE, "VALUE");
		if (valueE != null) {
			String valueStr = parseVALUE(valueE);
			CIMValue value = null;
			if (embedded) {
			    p.setType(new CIMDataType(CIMDataType.OBJECT));

				try {
//					valueStr =  valueStr.replaceAll("&lt;", "<").
//								replaceAll("&amp;", "&").
//								replaceAll("&quot;", "\"").
//								replaceAll("&gt;", ">").
//								replaceAll("&apos;", "\'").
//								replaceAll("\\\\\"", "\"");
				    CIMClientXML_HelperImpl builder = new CIMClientXML_HelperImpl();
				    Document doc = builder.parse(new InputSource(new StringReader(valueStr)));
				    value = new CIMValue(parseObject(doc.getDocumentElement()),
							 new CIMDataType(CIMDataType.OBJECT));				    
				}
				catch (Exception e) {
					value = new CIMValue(createJavaObject(valueTypeStr, valueStr),
				    					CIMDataType.getDataType(valueTypeStr, false));
				    
				    Logger logger = SessionProperties.getGlobalProperties().getLogger();
				    if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, 
						   "parsePROPERTY exception while parsing embedded object from property",
						   e);
				    }
				}
			} else {
				Object o = createJavaObject(valueTypeStr, valueStr);
				value = new CIMValue(o, CIMDataType.getDataType(valueTypeStr, false));
			}
			p.setValue(value);
			return p;
		}

		// return a CIMProperty without a CIMValue
		return p;
	}
	
	public static CIMProperty parsePROPERTYARRAY(Element propertyarrayE) throws CIMXMLParseException {
		// <!ELEMENT PROPERTY.ARRAY (QUALIFIER*,VALUE.ARRAY?)>
		// <!ATTLIST PROPERTY.ARRAY %NAME;%TYPE;%ARRAYSIZE;%CLASSORIGIN;%PROPAGATED;>
		
		CIMProperty p = new CIMProperty();
		String name = propertyarrayE.getAttribute("NAME");
		p.setName(name);

		String valueTypeStr = propertyarrayE.getAttribute("TYPE");
		CIMDataType propertyType = CIMDataType.getDataType(valueTypeStr, true);
		p.setType(propertyType);
		
//		String valueArraysizeStr = propertyarrayE.getAttribute("ARRAYSIZE");
		
		String valueClassoriginStr = propertyarrayE.getAttribute("CLASSORIGIN");
		if (valueClassoriginStr != null && valueClassoriginStr.length()>0)
			p.setOriginClass(valueClassoriginStr);
		
		String valuePropagatedStr = propertyarrayE.getAttribute("PROPAGATED");
		if (valuePropagatedStr != null && valuePropagatedStr.equalsIgnoreCase("true"))
			p.setPropagated(true);
		
		// QUALIFIER
		Vector qualifierV = searchNodes(propertyarrayE, "QUALIFIER");
		if (qualifierV != null) {
			for (int i=0; i<qualifierV.size(); i++) {
				Element qualifierE = (Element)qualifierV.elementAt(i);
				CIMQualifier q = parseQUALIFIER(qualifierE);
				p.addQualifier(q);
			}
		}
		
		// VALUE.ARRAY
		Element valuearrayE = (Element)searchFirstNode(propertyarrayE, "VALUE.ARRAY");
		if (valuearrayE != null) {
			Vector v = parseVALUEARRAY(valuearrayE);
			Vector v2 = new Vector();
			for (int i=0; i<v.size(); i++) {
				String valueStr = (String)v.elementAt(i);
				Object o = createJavaObject(valueTypeStr, valueStr);
				v2.add(o);
			}
			CIMValue value = new CIMValue(v2, propertyType);
			p.setValue(value);
			return p;
		}

		// return a CIMProperty without a CIMValue
		return p;
	}
	
	public static CIMProperty parsePROPERTYREFERENCE(Element propertyreferenceE) throws CIMXMLParseException {
		// <!ELEMENT PROPERTY.REFERENCE (QUALIFIER*,VALUE.REFERENCE?)>
		// <!ATTLIST PROPERTY.REFERENCE %NAME;%REFERENCECLASS;%CLASSORIGIN;%PROPAGATED;>
		
		CIMProperty p = new CIMProperty();
		String name = propertyreferenceE.getAttribute("NAME");
		p.setName(name);

		String valueClassoriginStr = propertyreferenceE.getAttribute("CLASSORIGIN");
		if (valueClassoriginStr != null)
		p.setOriginClass(valueClassoriginStr);
		
		String valueReferenceClassStr = propertyreferenceE.getAttribute("REFERENCECLASS");
		if (valueReferenceClassStr != null)
			p.setType(new CIMDataType(valueReferenceClassStr));
			
		String valuePropagatedStr = propertyreferenceE.getAttribute("PROPAGATED");
		if (valuePropagatedStr != null && valuePropagatedStr.equalsIgnoreCase("true"))
			p.setPropagated(true);		
		// QUALIFIER
		Vector qualifierV = searchNodes(propertyreferenceE, "QUALIFIER");
		if (qualifierV != null) {
			for (int i=0; i<qualifierV.size(); i++) {
				Element qualifierE = (Element)qualifierV.elementAt(i);
				CIMQualifier q = parseQUALIFIER(qualifierE);
				p.addQualifier(q);
			}
		}
		
		// VALUE.REFERENCE
		Element valuereferenceE = (Element)searchFirstNode(propertyreferenceE, "VALUE.REFERENCE");
		if (valuereferenceE != null) {
			CIMObjectPath op = parseVALUEREFERENCE(valuereferenceE);
			p.setValue(new CIMValue(op, CIMDataType.getPredefinedType(CIMDataType.REFERENCE)));
			return p;
		}

		// return a CIMProperty without a CIMValue
		return p;
	}
	

	/////////////////////////////////////////////////////////////////////////////////////////////
	// Message Elements
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	public static CIMMessage parseMESSAGE(String cimversion, String dtdversion, Element messageE)  throws CIMXMLParseException {
		// <!ELEMENT MESSAGE (SIMPLEREQ|MULTIREQ|SIMPLERSP|MULTIRSP|SIMPLEEXPREQ|MULTIEXPREQ|SIMPLEEXPRSP|MULTIEXPRSP)>
		// <!ATTLIST MESSAGE %ID;%PROTOCOLVERSION;>
		
		// TODO 
		if (cimversion.equals("2.0") && dtdversion.equals("2.0")) {
			
//			Attr message_idA = (Attr)searchAttribute(messageE, "ID");
//			String messadeID = message_idA.getNodeValue();
			
//			Attr message_protocolversionA = (Attr)searchAttribute(messageE, "PROTOCOLVERSION");
//			String protocolversion = message_protocolversionA.getNodeValue();
			
			// SIMPLERSP
			Element simplerspE = (Element)searchFirstNode(messageE, "SIMPLERSP");
			if (simplerspE != null) {
				CIMResponse response = parseSIMPLERSP(simplerspE);
				response.setMethod("SIMPLERSP");
				return response;
			}
			Element multirspE = (Element)searchFirstNode(messageE, "MULTIRSP");
			if (multirspE != null) {
				CIMResponse response = parseMULTIRSP( multirspE);
				response.setMethod("MULTIRSP");
				return response;
			}
			// SIMPLEEXPREQ
			Element simpleexpreqE = (Element)searchFirstNode(messageE, "SIMPLEEXPREQ");
			if (simpleexpreqE != null) {
				CIMRequest request = parseSIMPLEEXPREQ( simpleexpreqE);
				request.setMethod("SIMPLEEXPREQ");
				return request;
			}

			// MULTIEXPREQ
			Element multiexpreqE = (Element)searchFirstNode(messageE, "MULTIEXPREQ");
			if (multiexpreqE != null) {
				CIMRequest request = parseMULTIEXPREQ( multiexpreqE);
				request.setMethod("MULTIEXPREQ");
				return request;
			}
			// SIMPLEEXPRSP
			// MULTIEXPRSP
			
			// SIMPLEREQ
			Element simplereqE = (Element)searchFirstNode(messageE, "SIMPLEREQ");
			if (simplereqE != null) {
				CIMRequest request = parseSIMPLEREQ(simplereqE);
				request.setMethod("SIMPLEREQ");
				return request;
			} 
		
			// MULTIREQ
			Element multireqE = (Element)searchFirstNode(messageE, "MULTIREQ");
			if (multireqE != null) {
				CIMRequest request = parseMULTIREQ( multireqE);
				request.setMethod("MULTIREQ");
				return request;
			}
			throw new CIMXMLParseException();
		}
		//TODO, look for the specific error message in the spec
		throw new CIMXMLParseException("DTD not supported");
	}

	public static CIMArgument parsePARAMVALUE(Element paramvalueE) throws CIMXMLParseException {
		// <!ELEMENT PARAMVALUE (VALUE|VALUE.REFERENCE|VALUE.ARRAY|VALUE.REFARRAY)?>)
		// <!ATTLIST PARAMTYPE %NAME;%PARAMTYPE%>
		
		Attr paramvalue_nameA = (Attr)searchAttribute(paramvalueE, "NAME");
		String name = null;
		if (paramvalue_nameA != null) {
			name = paramvalue_nameA.getNodeValue();
		}
		Attr paramvalue_paramtypeA = (Attr)searchAttribute(paramvalueE, "PARAMTYPE");
		String valueTypeStr = null;
		if (paramvalue_paramtypeA != null) {
			valueTypeStr = paramvalue_paramtypeA.getNodeValue();
		}
		
		// VALUE
		Element valueE = (Element)searchFirstNode(paramvalueE, "VALUE");
		if (valueE != null) {
			String valueStr = parseVALUE(valueE);
			
			// fix for ESS
			if (valueTypeStr == null) {
				Attr value_typeA = (Attr)searchAttribute(valueE, "TYPE");
				if (value_typeA != null) {
					valueTypeStr = value_typeA.getNodeValue();
				}
			}
			
			Object o = createJavaObject(valueTypeStr, valueStr);
			CIMArgument arg = new CIMArgument(name);
			arg.setValue(new CIMValue(o, CIMDataType.getDataType(valueTypeStr, false)));
			return arg;			
		}

		// VALUE.ARRAY
		Element valuearrayE = (Element)searchFirstNode(paramvalueE, "VALUE.ARRAY");
		if (valuearrayE != null) {
			Vector v = parseVALUEARRAY(valuearrayE);
			Vector v2 = new Vector();
			for (int i=0; i<v.size(); i++) {
				String valueStr = (String)v.elementAt(i);
				Object o = createJavaObject(valueTypeStr, valueStr);
				v2.add(o);
			}
			CIMArgument arg = new CIMArgument(name);
			arg.setValue(new CIMValue(v2, CIMDataType.getDataType(valueTypeStr, true)));
			return arg;			
		}

		// VALUE.REFERENCE
		Element valuereferenceE = (Element)searchFirstNode(paramvalueE, "VALUE.REFERENCE");
		if (valuereferenceE != null) {
			CIMObjectPath op = parseVALUEREFERENCE(valuereferenceE);
			CIMArgument arg = new CIMArgument(name);
			arg.setValue(new CIMValue(op, CIMDataType.getPredefinedType(CIMDataType.REFERENCE)));
			return arg;			
		}
		
		return null;
	}

	public static CIMArgument parseIPARAMVALUE(Element iparamvalueE) throws CIMXMLParseException {
//		<!ELEMENT IPARAMVALUE (VALUE|VALUE.ARRAY|VALUE.REFERENCE|CLASSNAME|INSTANCENAME|QUALIFIER.DECLARATION| 
//					  CLASS|INSTANCE|VALUE.NAMEDINSTANCE)?> 
//		<!ATTLIST IPARAMVALUE 
//			 %CIMName;> 

		Attr paramvalue_nameA = (Attr)searchAttribute(iparamvalueE, "NAME");
		String name = null;
		if (paramvalue_nameA != null) {
			name = paramvalue_nameA.getNodeValue();
		}
		
		// fix for ESS
		Attr paramvalue_paramtypeA = (Attr)searchAttribute(iparamvalueE, "PARAMTYPE");
		String valueTypeStr = null;
		if (paramvalue_paramtypeA != null) {
			valueTypeStr = paramvalue_paramtypeA.getNodeValue();
		}
		
		// VALUE
		Element valueE = (Element)searchFirstNode(iparamvalueE, "VALUE");
		if (valueE != null) {
			String valueStr = parseVALUE(valueE);
			
			// fix for ESS
			if (valueTypeStr == null) {
				Attr value_typeA = (Attr)searchAttribute(valueE, "TYPE");
				if (value_typeA != null) {
					valueTypeStr = value_typeA.getNodeValue();
				}
			}
			
			Object o = createJavaObject(valueTypeStr, valueStr);
			CIMArgument arg = new CIMArgument(name);
			arg.setValue(new CIMValue(o, CIMDataType.getDataType(valueTypeStr, false)));
			return arg;			
		}

		// VALUE.ARRAY
		Element valuearrayE = (Element)searchFirstNode(iparamvalueE, "VALUE.ARRAY");
		if (valuearrayE != null) {
			Vector v = parseVALUEARRAY(valuearrayE);
			Vector v2 = new Vector();
			for (int i=0; i<v.size(); i++) {
				String valueStr = (String)v.elementAt(i);
				Object o = createJavaObject(valueTypeStr, valueStr);
				v2.add(o);
			}
			CIMArgument arg = new CIMArgument(name);
			arg.setValue(new CIMValue(v2, CIMDataType.getDataType(valueTypeStr, true)));
			return arg;			
		}

		// VALUE.REFERENCE
		Element valuereferenceE = (Element)searchFirstNode(iparamvalueE, "VALUE.REFERENCE");
		if (valuereferenceE != null) {
			CIMObjectPath op = parseVALUEREFERENCE(valuereferenceE);
			CIMArgument arg = new CIMArgument(name);
			arg.setValue(new CIMValue(op, CIMDataType.getPredefinedType(CIMDataType.REFERENCE)));
			return arg;			
		}
		

		// CLASSNAME
		Element classnameE = (Element)searchFirstNode(iparamvalueE, "CLASSNAME");
		if (classnameE != null) {
			CIMObjectPath op = parseCLASSNAME(classnameE);
			CIMArgument arg = new CIMArgument(name);
			arg.setValue(new CIMValue(op, CIMDataType.getPredefinedType(CIMDataType.REFERENCE)));
			return arg;			
		}

		// INSTANCENAME
		Element instancenameE = (Element)searchFirstNode(iparamvalueE, "INSTANCENAME");
		if (instancenameE != null) {
			CIMObjectPath op = parseINSTANCENAME(instancenameE);
			CIMArgument arg = new CIMArgument(name);
			arg.setValue(new CIMValue(op, CIMDataType.getPredefinedType(CIMDataType.REFERENCE)));
			return arg;			
		}

		
		// QUALIFIER.DECLARATION
		Element qualifierdeclarationE = (Element)searchFirstNode(iparamvalueE, "QUALIFIER.DECLARATION");
		if (qualifierdeclarationE != null) {
			CIMQualifierType o = parseQUALIFIERDECLARATION(qualifierdeclarationE);
			CIMArgument arg = new CIMArgument(name);
			arg.setValue(new CIMValue(o, CIMDataType.getPredefinedType(CIMDataType.REFERENCE)));
			return arg;
		}
		
		// CLASS
		Element classE = (Element)searchFirstNode(iparamvalueE, "CLASS");
		if (classE != null) {
			CIMClass op = parseCLASS(classE);
			CIMArgument arg = new CIMArgument(name);
			arg.setValue(new CIMValue(op, CIMDataType.getPredefinedType(CIMDataType.REFERENCE)));
			return arg;			
		}
		
		// INSTANCE
		Element instanceE = (Element)searchFirstNode(iparamvalueE, "INSTANCE");
		if (instanceE != null) {
			CIMInstance op = parseINSTANCE(instanceE);
			CIMArgument arg = new CIMArgument(name);
			arg.setValue(new CIMValue(op, CIMDataType.getPredefinedType(CIMDataType.REFERENCE)));
			return arg;			
		}
		
		// VALUE.NAMEDINSTANCE
		Element valuenamedisntanceE = (Element)searchFirstNode(iparamvalueE, "VALUE.NAMEDINSTANCE");
		if (valuenamedisntanceE != null) {
			CIMElement o = parseVALUENAMEDINSTANCE(valuenamedisntanceE);
			CIMArgument arg = new CIMArgument(name);
			arg.setValue(new CIMValue(o, CIMDataType.getPredefinedType(CIMDataType.REFERENCE)));
			return arg;			
		}
		
		return null;
	}
	
	public static CIMResponse parseSIMPLERSP(Element simplerspE) throws CIMXMLParseException {
		// <!ELEMENT SIMPLERSP (METHODRESPONSE|IMETHODRESPONSE)>
		
		// METHODRESPONSE
		Element methodresponseE = (Element)searchFirstNode(simplerspE, "METHODRESPONSE");
		if (methodresponseE != null) {
			
			return parseMETHODRESPONSE( methodresponseE);
		}
		
		// IMETHODRESPONSE
		Element imethodresponseE = (Element)searchFirstNode(simplerspE, "IMETHODRESPONSE");
		if (imethodresponseE != null) {
			return parseIMETHODRESPONSE( imethodresponseE);
		}
		
		throw new CIMXMLParseException();
	}
		
	public static CIMResponse parseMULTIRSP(Element simplerspE) throws CIMXMLParseException {
		// <!ELEMENT MULTIRSP (SIMPLERSP,SIMPLERSP+)>
		
		Vector multiresponseV = searchNodes(simplerspE, "SIMPLERSP");
		if (multiresponseV != null) {
			CIMResponse multiRsp = new CIMResponse();
			for (int i = 0; i < multiresponseV.size(); i++) {
				Element methodresponseE = (Element)multiresponseV.elementAt(i);
				CIMResponse rsp = parseSIMPLERSP(methodresponseE);
				rsp.setMethod("SIMPLERSP");
				
				multiRsp.addResponse(rsp);
			}
			return multiRsp;
		}
		
		throw new CIMXMLParseException();
	}
	
	public static CIMRequest parseSIMPLEREQ(Element simplereqE) throws CIMXMLParseException {
		// <!ELEMENT SIMPLEREQ (METHODCALL|IMETHODCALL)>
		
		// METHODCALL
		Element methodcallE = (Element)searchFirstNode(simplereqE, "METHODCALL");
		if (methodcallE != null) {
			return parseMETHODCALL( methodcallE);
		}
		
		// IMETHODCALL
		Element imethodcallE = (Element)searchFirstNode(simplereqE, "IMETHODCALL");
		if (imethodcallE != null) {
			return parseIMETHODCALL(imethodcallE);
		}
		
		throw new CIMXMLParseException();
	}

	public static CIMRequest parseMULTIREQ(Element multireqE) throws CIMXMLParseException {
		// <!ELEMENT MULTIREQ (SIMPLEREQ,SIMPLEREQ+)>

		Vector methodrequestV = searchNodes(multireqE, "MULTIREQ");
		if (methodrequestV != null) {
			CIMRequest multiReq = new CIMRequest();
			for (int i = 0; i < methodrequestV.size(); i++) {
				Element methodrequestE = (Element)methodrequestV.elementAt(i);
				CIMRequest req = parseSIMPLEREQ(methodrequestE);
				req.setMethod("MULTIPREQ");
				
				multiReq.addRequest(req);
			}
			return multiReq;
		}		
		throw new CIMXMLParseException();
	}

	public static CIMRequest parseMETHODCALL(Element methodcallE) throws CIMXMLParseException {
//		<!ELEMENT METHODCALL ((LOCALCLASSPATH|LOCALINSTANCEPATH),PARAMVALUE*)> 
//		<!ATTLIST METHODCALL 
//			 %CIMName;>
		
		CIMRequest request = new CIMRequest();
		String methodname = methodcallE.getAttribute("CIMName");
		request.setMethodName(methodname);
		
		// EXPMETHODCALL
		boolean localclasspathFound = false;
		Element localclasspathE = (Element)searchFirstNode(methodcallE, "LOCALCLASSPATH");
		if (localclasspathE != null) {
			CIMObjectPath path = parseLOCALCLASSPATH(localclasspathE);

			request.setObjectPath(path);
			localclasspathFound = true;
		}
		
		Element localinstancepathE = (Element)searchFirstNode(methodcallE, "LOCALINSTANCEATH");
		if (localinstancepathE != null) {
			CIMObjectPath path = parseLOCALINSTANCEPATH(localinstancepathE);
		
			request.setObjectPath(path);
		}
		else {
			if (!localclasspathFound) throw new CIMXMLParseException();
		}
		
		Vector paramvalueV = searchNodes(methodcallE, "PARAMVALUE");
		
		if (paramvalueV != null) {
			Vector v = new Vector();
			for (int i=0; i<paramvalueV.size(); i++) {
				Element paramvalueE = (Element)paramvalueV.elementAt(i);
				CIMArgument arg = parsePARAMVALUE(paramvalueE);
				v.add(arg);
			}
			request.addParamValue(v);
		}
		
		return request;
//		throw new CIMXMLParseException();
	}
	public static CIMRequest parseIMETHODCALL(Element imethodcallE) throws CIMXMLParseException {
//		<!ELEMENT IMETHODCALL (LOCALNAMESPACEPATH,IPARAMVALUE*)> 
//		<!ATTLIST IMETHODCALL 
//			 %CIMName;>

		CIMRequest request = new CIMRequest();
		String methodname = imethodcallE.getAttribute("NAME"); // ebak: CIMName->NAME
		request.setMethodName(methodname);

		// METHODCALL
		Element localnamespacepathE = (Element)searchFirstNode(imethodcallE, "LOCALNAMESPACEPATH");
		if (localnamespacepathE != null) {
			CIMNameSpace namespace = parseLOCALNAMESPACEPATH(localnamespacepathE);
			request.setNameSpace(namespace);
		}
		
		Vector iparamvalueV = searchNodes(imethodcallE, "IPARAMVALUE"); // ebak: PARAMVALUE->IPARAMVALUE
		
		if (iparamvalueV != null) {
			Vector v = new Vector();
			for (int i=0; i<iparamvalueV.size(); i++) {
				Element paramvalueE = (Element)iparamvalueV.elementAt(i);
				CIMArgument arg = parseIPARAMVALUE(paramvalueE);
				/*
				 * ebak: local nameSpacePath should be added to those reference arguments which
				 * don't contain namespace
				 */
				Object value=arg.getValue().getValue();
				if (value instanceof CIMObjectPath) {
					CIMObjectPath op=(CIMObjectPath)value;
					if (op.getNameSpace()==null || op.getNameSpace().length()==0)
						op.setNameSpace(request.getNameSpace());
				}
				v.add(arg);
			}
			request.addParamValue(v);
			return request;
		}
				
		throw new CIMXMLParseException();
	}
	
	public static CIMRequest parseSIMPLEEXPREQ(Element simpleexpreqE) throws CIMXMLParseException {
		// <!ELEMENT SIMPLEEXPREQ (METHODRESPONSE)>
		
		// EXPMETHODCALL
		Element expmethodcallE = (Element)searchFirstNode(simpleexpreqE, "EXPMETHODCALL");
		if (expmethodcallE != null) {
			return parseEXPMETHODCALL(expmethodcallE);
		}
		
		throw new CIMXMLParseException();
	}
	
	public static CIMRequest parseMULTIEXPREQ(Element multiexpreqE) throws CIMXMLParseException {
		// <!ELEMENT SIMPLEEXPREQ (METHODRESPONSE)>
		
		Vector methodrequestV = searchNodes(multiexpreqE, "SIMPLEEXREQ");
		if (methodrequestV != null) {
			CIMRequest multiReq = new CIMRequest();
			for (int i = 0; i < methodrequestV.size(); i++) {
				Element methodrequestE = (Element)methodrequestV.elementAt(i);
				CIMRequest req = parseSIMPLEEXPREQ(methodrequestE);
				req.setMethod("SIMPLEEXPREQ");
				
				multiReq.addRequest(req);
			}
			return multiReq;
		}		
		throw new CIMXMLParseException();
	}

	public static CIMRequest parseEXPMETHODCALL(Element expmethodcallE) throws CIMXMLParseException {
		// <!ELEMENT EXPMETHODCALL (EXPPARAMVALUE*)>
		
		// EXPMETHODCALL
		CIMRequest request = new CIMRequest();
		Vector expparamvalueV = searchNodes(expmethodcallE, "EXPPARAMVALUE");
		Vector v = new Vector();
		if (expparamvalueV != null) {
			for (int i=0; i<expparamvalueV.size(); i++) {
				Element expparamvalueE = (Element)expparamvalueV.elementAt(i);
				CIMInstance inst = parseEXPPARAMVALUE(expparamvalueE);
				v.add(inst);
			}
		}
		
		request.addParamValue(v);
		return request;
	}

	public static CIMInstance parseEXPPARAMVALUE(Element expparamvalueE) throws CIMXMLParseException {
		//<!ELEMENT EXPPARAMVALUE (INSTANCE)> 
		//<!ATTLIST EXPPARAMVALUE  
		//	%CIMName;>
		// INSTANCE
		Element instanceE = (Element)searchFirstNode(expparamvalueE, "INSTANCE");
		if (instanceE != null) {
			CIMInstance inst = parseINSTANCE(instanceE);
						
			return inst;
		}
		
		throw new CIMXMLParseException();
	}

	public static CIMResponse parseMETHODRESPONSE(Element methodresponseE) throws CIMXMLParseException {
		// <!ELEMENT METHODRESPONSE (ERROR|(RETURNVALUE?,PARAMVALUE*))>

		CIMResponse response = new CIMResponse();		
		
		// ERROR
		Element errorE = (Element)searchFirstNode(methodresponseE, "ERROR");
		if (errorE != null) {
			CIMException exception = parseERROR(errorE);
			response.setError(exception);
			return response;
		}
		
		// RETURNVALUE
		Vector returnvalueV = searchNodes(methodresponseE, "RETURNVALUE");
		if (returnvalueV != null) {
			Vector v = new Vector();
			for (int i=0; i< returnvalueV.size(); i++) {
				Element returnvalueE = (Element)returnvalueV.elementAt(i); 
				CIMValue value = parseRETURNVALUE(returnvalueE);
				v.add(value);
			}
			response.setReturnValue(v);
		}
		
		// PARAMVALUE
		Vector paramvalueV = searchNodes(methodresponseE, "PARAMVALUE");
		if (paramvalueV != null) {
			Vector v = new Vector();
			for (int i=0; i<paramvalueV.size(); i++) {
				Element paramvalueE = (Element)paramvalueV.elementAt(i);
				CIMArgument arg = parsePARAMVALUE(paramvalueE);
				v.add(arg);
			}
			response.addParamValue(v);
		}

		// PARAMVALUE (ESS fix)
		if (returnvalueV != null) {
			Iterator iter = returnvalueV.iterator();
			while (iter.hasNext()) {
				Element returnvalueE = (Element)iter.next();
			
				paramvalueV = searchNodes(returnvalueE, "PARAMVALUE");
				if (paramvalueV != null) {
					Vector v = new Vector();
					for (int i=0; i<paramvalueV.size(); i++) {
						Element paramvalueE = (Element)paramvalueV.elementAt(i);
						CIMArgument arg = parsePARAMVALUE(paramvalueE);
						v.add(arg);
					}
					response.addParamValue(v);
				}
			}
		}
		
		return response;
	}

	public static CIMResponse parseIMETHODRESPONSE(Element imethodresponseE) throws CIMXMLParseException {
		// <!ELEMENT IMETHODRESPONSE (ERROR|(IRETURNVALUE?))>
		

		CIMResponse response = new CIMResponse();		
		// ERROR
		Element errorE = (Element)searchFirstNode(imethodresponseE, "ERROR");
		if (errorE != null) {
			CIMException  exception = parseERROR(errorE);
			response.setError(exception);
			return response;
		}
		
		// IRETURNVALUE
		Vector ireturnvalueV = searchNodes(imethodresponseE, "IRETURNVALUE");
		if (ireturnvalueV != null) {
			for(int i =0; i< ireturnvalueV.size(); i++) {
				Element ireturnvalueE = (Element)ireturnvalueV.elementAt(i);

				Vector rtnV = parseIRETURNVALUE(ireturnvalueE);
				response.setReturnValue(rtnV);
			}
		}
		
		
		return response;
	}
	
	public static CIMException parseERROR(Element errorE) throws CIMXMLParseException {
		// <!ELEMENT ERROR EMPTY>
		// <!ATTLSIT ERROR %CODE;%DESCRIPTION;>
		
		Attr error_codeA = (Attr)searchAttribute(errorE, "CODE");
		String code = error_codeA.getNodeValue();
		int errorCode = 0;
		try {
			if (code.length() > 0)
				errorCode = Integer.parseInt(code);
		} catch (Exception e) {
			Logger logger = SessionProperties.getGlobalProperties().getLogger();
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "exception while parsing error code from XML", e);
			}
		}
		Attr error_descriptionA = (Attr)searchAttribute(errorE, "DESCRIPTION");
		String description = "";
		if (error_descriptionA != null) {
			description = error_descriptionA.getNodeValue();
		}
		
		//throw new CIMException(CIMException.getErrorName(errorCode), description.substring(description.indexOf(':')+1)); 
		return new CIMException(CIMException.getStatusFromCode(errorCode), description);
	}

	public static CIMValue parseRETURNVALUE(Element returnvalueE) throws CIMXMLParseException {
		// <!ELEMENT RETURNVALUE (VALUE|VALUE.REFERENCE)>
		// <!ATTLSIT RETURNVALUE %PARAMTYPE;>
		
		Attr returnvalue_paramtypeA = (Attr)searchAttribute(returnvalueE, "PARAMTYPE");
		String valueTypeStr = null;
		if (returnvalue_paramtypeA != null) {
			valueTypeStr = returnvalue_paramtypeA.getNodeValue();
		}
		
		// VALUE
		Element valueE = (Element)searchFirstNode(returnvalueE, "VALUE");
		if (valueE != null) {
			String valueStr = parseVALUE(valueE);
			
			// fix for ESS
			if (valueTypeStr == null) {
				Attr value_typeA = (Attr)searchAttribute(valueE, "TYPE");
				if (value_typeA != null) {
					valueTypeStr = value_typeA.getNodeValue();
				}
			}
			
			Object o = createJavaObject(valueTypeStr, valueStr);
			return new CIMValue(o, CIMDataType.getPredefinedType(CIMDataType.REFERENCE));			
		}

		// VALUE.REFERENCE
		Element valuereferenceE = (Element)searchFirstNode(returnvalueE, "VALUE.REFERENCE");
		if (valuereferenceE != null) {
			CIMObjectPath op = parseVALUEREFERENCE(valuereferenceE);
			return new CIMValue(op, CIMDataType.getPredefinedType(CIMDataType.REFERENCE));			
		}
		
		throw new CIMXMLParseException();
	}

	public static Vector parseIRETURNVALUE(Element ireturnvalueE) throws CIMXMLParseException {
		// <!ELEMENT IRETURNVALUE (CLASSNAME*|INSTANCENAME*
		//		|VALUE*|VALUE.OBJECTWITHPATH*|VALUE.OBJECTWITHLOCALPATH*|VALUE.OBJECT*
		//		|OBJECTPATH*|QUALIFIER.DECLARATION*|VALUE.ARRAY|VALUE.REFERENCE*
		//		|CLASS*|INSTANCE*|VALUE.NAMEDINSTANCE*)>
		
		Vector rtnV = new Vector();

		// CLASS
		Vector classV = searchNodes(ireturnvalueE, "CLASS");
		if (classV != null) {
			for (int i=0; i<classV.size(); i++) {
				Element classE = (Element)classV.elementAt(i);
				CIMClass c = parseCLASS(classE);
				rtnV.add(c);
			}
		}

		// INSTANCE
		Vector instanceV = searchNodes(ireturnvalueE, "INSTANCE");
		if (instanceV != null) {
			for (int i=0; i<instanceV.size(); i++) {
				Element instanceE = (Element)instanceV.elementAt(i);
				CIMInstance inst = parseINSTANCE(instanceE);
				rtnV.add(inst);
			}
		}

		// CLASSNAME
		Vector classnameV = searchNodes(ireturnvalueE, "CLASSNAME");
		if (classnameV != null) {
			for (int i=0; i<classnameV.size(); i++) {
				Element classnameE = (Element)classnameV.elementAt(i);
				CIMObjectPath op = parseCLASSNAME(classnameE);
				rtnV.add(op);
			}
		}

		// INSTANCENAME
		Vector instancenameV = searchNodes(ireturnvalueE, "INSTANCENAME");
		if (instancenameV != null) {
			for (int i=0; i<instancenameV.size(); i++) {
				Element instancenameE = (Element)instancenameV.elementAt(i);
				CIMObjectPath op = parseINSTANCENAME(instancenameE);
				rtnV.add(op);
			}
		}
		
		// OBJECTPATH
		Vector objectpathV = searchNodes(ireturnvalueE, "OBJECTPATH");
		if (objectpathV != null) {
			for (int i=0; i<objectpathV.size(); i++) {
				Element objectpathE = (Element)objectpathV.elementAt(i);
				CIMObjectPath op = parseOBJECTPATH(objectpathE);
				rtnV.add(op);
			}
		}

		// VALUE
//		Vector valueV = searchNodes(ireturnvalueE, "VALUE");
//		if (valueV != null) {
//			for (int i=0; i<valueV.size(); i++) {
//				Element valueE = (Element)valueV.elementAt(i);
//				String valueStr = parseVALUE(valueE);
//				 // cannot construct a CIMValue without type information
//			}
//		}

		// VALUE.ARRAY
//		Vector valuearrayV = searchNodes(ireturnvalueE, "VALUE.ARRAY");
//		if (valuearrayV != null) {
//			for (int i=0; i<valuearrayV.size(); i++) {
//				Element valuearrayE = (Element)valuearrayV.elementAt(i);
//				// cannot construct a CIMValue without type information
//			}
//		}

		// VALUE.REFERENCE
		Vector valuereferenceV = searchNodes(ireturnvalueE, "VALUE.REFERENCE");
		if (valuereferenceV != null) {
			for (int i=0; i<valuereferenceV.size(); i++) {
				Element valuereferenceE = (Element)valuereferenceV.elementAt(i);
				CIMObjectPath op = parseVALUEREFERENCE(valuereferenceE);
				rtnV.add(op);
			}
		}

		// VALUE.OBJECT
		Vector valueobjectV = searchNodes(ireturnvalueE, "VALUE.OBJECT");
		if (valueobjectV != null) {
			for (int i=0; i<valueobjectV.size(); i++) {
				Element valueobjectE = (Element)valueobjectV.elementAt(i);
				CIMElement o = parseVALUEOBJECT(valueobjectE);
				rtnV.add(o);
			}
		}

		// VALUE.NAMEDINSTANCE
		Vector valuenamedinstanceV = searchNodes(ireturnvalueE, "VALUE.NAMEDINSTANCE");
		if (valuenamedinstanceV != null) {
			for (int i=0; i<valuenamedinstanceV.size(); i++) {
				Element valuenamedisntanceE = (Element)valuenamedinstanceV.elementAt(i);
				CIMElement o = parseVALUENAMEDINSTANCE(valuenamedisntanceE);
				rtnV.add(o);
			}
		}
		
		// VALUE.OBJECTWITHPATH
		Vector valueobjectwithpathV = searchNodes(ireturnvalueE, "VALUE.OBJECTWITHPATH");
		if (valueobjectwithpathV != null) {
			for (int i=0; i<valueobjectwithpathV.size(); i++) {
				Element valueobjectwithpathE = (Element)valueobjectwithpathV.elementAt(i);
				CIMElement o = parseVALUEOBJECTWITHPATH(valueobjectwithpathE);
				rtnV.add(o);
			}
		}

		// VALUE.OBJECTWITHLOCALPATH
		Vector valueobjectwithlocalpathV = searchNodes(ireturnvalueE, "VALUE.OBJECTWITHLOCALPATH");
		if (valueobjectwithlocalpathV != null) {
			for (int i=0; i<valueobjectwithlocalpathV.size(); i++) {
				Element valueobjectwithlocalpathE = (Element)valueobjectwithlocalpathV.elementAt(i);
				CIMElement o = parseVALUEOBJECTWITHLOCALPATH(valueobjectwithlocalpathE);
				rtnV.add(o);
			}
		}

		// QUALIFIER.DECLARATION
		Vector qualifierdeclarationV = searchNodes(ireturnvalueE, "QUALIFIER.DECLARATION");
		if (qualifierdeclarationV != null) {
			for (int i=0; i<qualifierdeclarationV.size(); i++) {
				Element qualifierdeclarationE = (Element)qualifierdeclarationV.elementAt(i);
				CIMQualifierType o = parseQUALIFIERDECLARATION(qualifierdeclarationE);
				rtnV.add(o);
			}
		}
		return rtnV;
	}
	
	public static Object parseObject(Element rootE) throws CIMXMLParseException {
		Object o = null;
		String nodeName = rootE.getNodeName();
		if (nodeName.equalsIgnoreCase("INSTANCE")) {
			o = parseINSTANCE(rootE);
		} else if (nodeName.equalsIgnoreCase("VALUE.NAMEDINSTANCE")) {
			o = parseVALUENAMEDINSTANCE(rootE);
		} else if (nodeName.equalsIgnoreCase("VALUE.NAMEDOBJECT")) {
			o = parseVALUENAMEDOBJECT(rootE);
		} else if (nodeName.equalsIgnoreCase("VALUE.OBJECTWITHPATH")) {
			o = parseVALUEOBJECTWITHPATH(rootE);			
		} else if (nodeName.equalsIgnoreCase("VALUE.OBJECTWITHLOCALPATH")) {
			o = parseVALUEOBJECTWITHLOCALPATH(rootE);			
		} else if (nodeName.equalsIgnoreCase("CLASS")) {
			o = parseCLASS(rootE);
		} else if (nodeName.equalsIgnoreCase("CLASSPATH")) {
			o = parseCLASSPATH(rootE);
		} else if (nodeName.equalsIgnoreCase("LOCALCLASSPATH")) {
			o = parseLOCALCLASSPATH(rootE);
		} else if (nodeName.equalsIgnoreCase("OBJECTPATH")) {
			o = parseOBJECTPATH(rootE);
		} else if (nodeName.equalsIgnoreCase("CLASSNAME")) {
			o = parseCLASSNAME(rootE);
		} else if (nodeName.equalsIgnoreCase("INSTANCEPATH")) {
			o = parseINSTANCEPATH(rootE);
		} else if (nodeName.equalsIgnoreCase("LOCALINSTANCEPATH")) {
			o = parseLOCALINSTANCEPATH(rootE);
		} else if (nodeName.equalsIgnoreCase("INSTANCENAME")) {
			o = parseINSTANCENAME(rootE);
		} else if (nodeName.equalsIgnoreCase("QUALIFIER")) {
			o = parseQUALIFIER(rootE);
		} else if (nodeName.equalsIgnoreCase("PROPERTY")) {
			o = parsePROPERTY(rootE);
		} else if (nodeName.equalsIgnoreCase("PROPERTY.ARRAY")) {
			o = parsePROPERTYARRAY(rootE);
		} else if (nodeName.equalsIgnoreCase("PROPERTY.REFERENCE")) {
			o = parsePROPERTYREFERENCE(rootE);
		} else if (nodeName.equalsIgnoreCase("METHOD")) {
			o = parseMETHOD(rootE);
		} else if (nodeName.equalsIgnoreCase("PARAMETER")) {
			o = parsePARAMETER(rootE);
		} else if (nodeName.equalsIgnoreCase("PARAMETER.REFERENCE")) {
			o = parsePARAMETERREFERENCE(rootE);
		} else if (nodeName.equalsIgnoreCase("PARAMETER.ARRAY")) {
			o = parsePARAMETERARRAY(rootE);
		} else if (nodeName.equalsIgnoreCase("PARAMETER.REFARRAY")) {
			o = parsePARAMETERREFARRAY(rootE);
		} else {
			o = null;
		}		
		return o;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////

	public static Vector searchNodes(Element parentE, String tagName) {
		// return all of child nodes immediately below the parent
		// return null if not found
		NodeList nl = parentE.getChildNodes();
		Vector v = new Vector();
		for (int i=0; i<nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equals(tagName)) {
				v.add(n);
			}
		}
		return (v.size() == 0)? null : v;
	}

	public static Node searchFirstNode(Element parentE, String tagName) {
		// return the first node which matches to the specific name
		// return null if not found
		NodeList nl = parentE.getChildNodes();
		for (int i=0; i<nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equals(tagName)) {
				return n;
			}
		}
		return null;
	}

	public static Node searchAttribute(Node parentN, String attrName) {
		// return the attribute node with the specific name
		NamedNodeMap nnm = parentN.getAttributes();
		return nnm.getNamedItem(attrName);
	}

	public static Node searchFirstChild(Element parentE) {
		// return the first node which matches to the specific name
		// return null if not found
		return parentE.getFirstChild();
	}

	public static Object createJavaObject(String typeStr, String value) {
		// return a java object with the specific type
		Object o = null;
		CIMDataType cimType = CIMDataType.getDataType(typeStr, false);
		int radix = 10;
		
		if (	typeStr.toLowerCase().startsWith("sint") || 
				typeStr.toLowerCase().startsWith("int")) {
			value = value.toLowerCase();
			if (value.startsWith("0x") ||
				value.startsWith("+0x") ||
				value.startsWith("-0x")) {
				radix = 16;
				if (value.startsWith("-"))
					value = "-"+value.substring(3);
				else 			
					value = value.substring(value.indexOf('x')+1);
			}
		}

		switch (cimType.getType()) {
			case CIMDataType.UINT8: o = new UnsignedInt8(Short.parseShort(value, radix)); break;
			case CIMDataType.UINT16: o = new UnsignedInt16(Integer.parseInt(value, radix)); break;
			case CIMDataType.UINT32: o = new UnsignedInt32(Long.parseLong(value, radix)); break;
			case CIMDataType.UINT64: o = new UnsignedInt64(new BigInteger(value, radix)); break;
			case CIMDataType.SINT8: o = Byte.valueOf(value, radix); break;
			case CIMDataType.SINT16: o = Short.valueOf(value, radix); break;
			case CIMDataType.SINT32: o = Integer.valueOf(value, radix); break;
			case CIMDataType.SINT64: o = Long.valueOf(value, radix); break;
			case CIMDataType.STRING: o = value; break;
			case CIMDataType.BOOLEAN: o = new Boolean(value); break;
			case CIMDataType.REAL32: o = new Float(value); break;
			case CIMDataType.REAL64: o = new Double(value); break;
			case CIMDataType.DATETIME: o = CIMDateTime.valueOf(value); break;
			case CIMDataType.REFERENCE: o = new CIMObjectPath(value); break;
			case CIMDataType.CHAR16 : o = new Character(value.charAt(0)); break;	
			case CIMDataType.NUMERIC : o = new Numeric(new BigInteger(value, radix).toString()); break;	
//			case CIMDataType.OBJECT: o = new CIMInstance(); break;				//TODO
//			case CIMDataType.CLASS: o = new CIMClass(value); break;				//TODO
		}
		return o;
	}

}
