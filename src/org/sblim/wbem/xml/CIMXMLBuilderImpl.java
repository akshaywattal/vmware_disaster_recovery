/**
 * CIMXMLBuilderImpl.java
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
 *------------------------------------------------------------------------------ 
 *   16627    2005-04-01  thschaef     Correct instantiation for Uint64 
 *   17459    2005-06-24  thschaef     catch null within createMethod method
 *   17459    2005-06-27  thschaef     further improvement to createMethod()
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1631407    2007-01-11  lupusalex    VALUE.REFERENCE doesn't handle references without namespace
 * 1656285    2007-02-12  ebak         IndicationHandler does not accept non-Integer message ID
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.xml;

import org.sblim.wbem.cim.*;
import org.sblim.wbem.client.indications.*;
import org.w3c.dom.*;
import java.math.BigInteger;
import java.util.*;

public class CIMXMLBuilderImpl {

	private static final int MAJOR_CIM_VERSION = 2;

	private static final int MINOR_CIM_VERSION = 0;

	private static final int MAJOR_DTD_VERSION = 2;

	private static final int MINOR_DTD_VERSION = 0;

	public static Element createCIM(Document doc) {
		// <!ELEMENT CIM (MESSAGE|DECLARATION)>
		// <!ATTLIST CIM %CIMVERSION;%DTDVERSION;>
		Element e = doc.createElement("CIM");
		e.setAttribute("CIMVERSION", MAJOR_CIM_VERSION + "." + MINOR_CIM_VERSION);
		// required
		e.setAttribute("DTDVERSION", MAJOR_DTD_VERSION + "." + MINOR_DTD_VERSION);
		// required
		doc.appendChild(e); // root element
		return e;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	// Value Elements
	// ////////////////////////////////////////////////////////////////////////////////////////

	public static Element createVALUE(Document doc, Element parentE) {
		// <! ELEMENT VALUE (#PCDATA)>
		Element e = doc.createElement("VALUE");
		parentE.appendChild(e);

		return e;
	}

	public static Element createVALUE(Document doc, Element parentE, String value) {
		// <! ELEMENT VALUE (#PCDATA)>
		Element e = doc.createElement("VALUE");
		parentE.appendChild(e);

		Text textE = doc.createTextNode(value);
		e.appendChild(textE);
		return e;
	}

	public static Element createVALUEARRAY(Document doc, Element parentE) {
		// <! ELEMENT VALUE.ARRAY (VALUE*)>
		Element e = doc.createElement("VALUE.ARRAY");
		parentE.appendChild(e);
		return e;
	}

	public static Element createVALUEREFERENCE(Document doc, Element parentE) {
		// <!ELEMENT VALUE.REFERENCE
		// (CLASSPATH|LOCALCLASSPATH|CLASSNAME|INSTANCEPATH|LOCALINSTANCEPATH|INSTANCENAME)>
		Element e = doc.createElement("VALUE.REFERENCE");
		parentE.appendChild(e);
		return e;
	}

	public static Element createVALUEREFARRAY(Document doc, Element parentE) {
		// <! ELEMENT VALUE.REFARRAY (VALUE.REFERENCE*)>
		Element e = doc.createElement("VALUE.REFARRAY");
		parentE.appendChild(e);
		return e;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	// Properties elements
	// ////////////////////////////////////////////////////////////////////////////////////////
	public static Element createPROPERTY(Document doc, Element parentE, String name, String type) {
		// <!ELEMENT PROPERTY (QUALIFIER*,VALUE?)>
		// <!ATTLIST PROPERTY
		// %CIMName;
		// %CIMType; #REQUIRED
		// %ClassOrigin;
		// %Propagated;>

		Element e = doc.createElement("PROPERTY");
		if (name != null) e.setAttribute("NAME", name);
		if (type != null) e.setAttribute("TYPE", type);
		// TODO: ClassOrigin, Propagated
		parentE.appendChild(e);
		return e;
	}

	public static Element createPROPERTYARRAY(Document doc, Element parentE, String name,
			String type) {
		// <!ELEMENT PROPERTY.ARRAY (QUALIFIER*,VALUE.ARRAY?)>
		// <!ATTLIST PROPERTY.ARRAY
		// %CIMName;
		// %CIMType; #REQUIRED
		// %ArraySize;
		// %ClassOrigin;
		// %Propagated;>

		Element e = doc.createElement("PROPERTY.ARRAY");
		if (name != null) e.setAttribute("NAME", name);
		if (type != null) e.setAttribute("TYPE", type);
		// TODO: ClassOrigin, Propagated
		parentE.appendChild(e);
		return e;
	}

	public static Element createPROPERTYREFERENCE(Document doc, Element parentE, String name,
			String referenceclass) {
		// <!ELEMENT PROPERTY.REFERENCE (QUALIFIER*,VALUE.REFERENCE?)>
		// <!ATTLIST PROPERTY.REFERENCE
		// %CIMName;
		// %ReferenceClass;
		// %ClassOrigin;
		// %Propagated;>

		Element e = doc.createElement("PROPERTY.REFERENCE");
		if (name != null) e.setAttribute("NAME", name);
		if (referenceclass != null) e.setAttribute("REFERENCECLASS", referenceclass);
		parentE.appendChild(e);
		return e;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	// Naming and Location elements
	// ////////////////////////////////////////////////////////////////////////////////////////

	public static Element createLOCALNAMESPACEPATH(Document doc, Element parentE) {
		// <!ELEMENT LOCALNAMESPACE (NAMESPACE+))>
		Element e = doc.createElement("LOCALNAMESPACEPATH");
		parentE.appendChild(e);
		return e;
	}

	public static Element createNAMESPACE(Document doc, Element parentE, String name) {
		// <!ELEMENT NAMESPACE EMPTY>
		// <!ATTLIST NAMESPACE %NAME;>
		Element e = doc.createElement("NAMESPACE");
		if (name != null) {
			e.setAttribute("NAME", name);
		}
		parentE.appendChild(e);
		return e;
	}

	public static Element createLOCALINSTANCEPATH(Document doc, Element parentE) {
		// <!ELEMENT INSTANCEPATH (NAMESPACEPATH,INSTANCENAME)>
		Element e = doc.createElement("LOCALINSTANCEPATH");
		parentE.appendChild(e);
		return e;
	}

	public static Element createCLASSNAME(Document doc, Element parentE, String name) {
		// <!ELEMENT CLASSNAME EMPTY>
		// <!ATTLIST CLASSNAME %NAME;>
		Element e = doc.createElement("CLASSNAME");
		if (name != null) {
			e.setAttribute("NAME", name);

			parentE.appendChild(e);
		}
		return e;
	}

	public static Element createCLASS(Document doc, Element parentE, String name, String superClass) {
		// <!ELEMENT CLASSNAME EMPTY>
		// <!ATTLIST CLASSNAME %NAME;>
		Element e = doc.createElement("CLASS");
		if (name != null) {
			e.setAttribute("NAME", name);

		}
		if (superClass != null && superClass.length() > 0) {
			e.setAttribute("SUPERCLASS", superClass);

		}
		if (parentE != null) parentE.appendChild(e);
		return e;
	}

	public static Element createINSTANCENAME(Document doc, Element parentE, String classname) {
		// <!ELEMENT INSTANCENAME (KEYBINDING*|KEYVALUE?|VALUE.REFERNCE?)>
		// <!ATTLIST INSTANCENAME %CLASSNAME;>
		Element e = doc.createElement("INSTANCENAME");
		if (classname != null) {
			e.setAttribute("CLASSNAME", classname);
		}
		parentE.appendChild(e);
		return e;
	}

	public static Element createKEYBINDING(Document doc, Element parentE, String name) {
		// <!ELEMENT KEYBINDING (KEYVALUE|VALUE.REFERENCE) >
		// <!ATTLIST KEYBINDING %NAME;>
		Element e = doc.createElement("KEYBINDING");
		if (name != null) {
			e.setAttribute("NAME", name);
		}
		parentE.appendChild(e);
		return e;
	}

	public static Element createKEYVALUE(Document doc, Element parentE, String valuetype,
			String value) {
		// <!ELEMENT KEYVALUE (#PCDATA)>
		// <!ATTLIST KEYVALUE VALUETYPE (string|boolean|numeric) 'string')
		Element e = doc.createElement("KEYVALUE");
		if (valuetype != null) {
			e.setAttribute("VALUETYPE", getOpTypeStr(CIMDataType.getDataType(valuetype, false)));
		} else {
			e.setAttribute("VALUETYPE", "string");
		}
		parentE.appendChild(e);

		Text valueE = doc.createTextNode(value);
		e.appendChild(valueE);
		return e;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	// Object Definition Elements
	// ////////////////////////////////////////////////////////////////////////////////////////

	public static Element createINSTANCE(Document doc, Element parentE, String classname) {
		// <!ELEMENT INSTANCE
		// (QUALIFIER*,(PROPERTY|PROPERTY.ARRAY|PROPERTY.REFERENCE)*)>
		// <!ATTLIST INSTANCE %ClassName;>

		Element e = doc.createElement("INSTANCE");
		if (classname != null) {
			e.setAttribute("CLASSNAME", classname);
		}
		if (parentE != null) parentE.appendChild(e);
		return e;
	}

	public static Element createQUALIFIER(Document doc, Element parentE, String name, String type) {
		// <!ELEMENT QUALIFIER (VALUE|VALUE.ARRAY)>
		// <!ATTLIST QUALIFIER
		// %CIMName;
		// %CIMType; #REQUIRED
		// %Propagated;
		// %QualifierFlavor;>

		Element e = doc.createElement("QUALIFIER");
		if (name != null) {
			e.setAttribute("NAME", name);
			// TODO: add additional attributes "propagated", "qualifierFlavod"
		}
		if (type != null) e.setAttribute("TYPE", type);
		parentE.appendChild(e);
		return e;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////
	// Message Elements
	// ///////////////////////////////////////////////////////////////////////////////////////////

	public static Element createMESSAGE(Document doc, Element parentE, String id,
			String protocolversion) {
		// <!ELEMENT MESSAGE
		// (SIMPLEREQ|MULTIREQ|SIMPLERSP|MULTIRSP|SIMPLEEXPREQ|MULTIEXPREQ|SIMPLEEXPRSP|MULTIEXPRSP)>
		// <!ATTLIST MESSAGE %ID;%PROTOCOLVERSION;>
		Element e = doc.createElement("MESSAGE");
		e.setAttribute("ID", id); // required
		e.setAttribute("PROTOCOLVERSION", protocolversion); // required
		parentE.appendChild(e);
		return e;
	}

	public static Element createSIMPLEREQ(Document doc, Element parentE) {
		// <!ELEMENT SIMPLEREQ (METHODCALL|IMETHODCALL)>
		Element e = doc.createElement("SIMPLEREQ");
		parentE.appendChild(e);
		return e;
	}

	public static Element createSIMPLEREQ(Document doc) {
		// <!ELEMENT SIMPLEREQ (METHODCALL|IMETHODCALL)>
		Element e = doc.createElement("SIMPLEREQ");
		return e;
	}

	public static Element createMULTIREQ(Document doc) {
		// <!ELEMENT MULTIREQ (SIMPLEREQ|SIMPLEREQ+)>
		Element e = doc.createElement("MULTIREQ");
		return e;
	}

	public static Element createMETHODCALL(Document doc, Element parentE, String name,
			String paramtype) {
		// <!ELEMENT METHODCALL
		// ((LOCALCLASSPATH|LOCALINSTANCEPATH),PARAMVALUE*)>
		// <!ATTLIST METHODCALL %NAME;%PARAMTYPE;>
		Element e = doc.createElement("METHODCALL");
		if (name != null) {
			e.setAttribute("NAME", name);
		}
		if (paramtype != null) {
			e.setAttribute("PARAMTYPE", paramtype);
		}
		parentE.appendChild(e);
		return e;
	}

	public static Element createPARAMVALUE(Document doc, Element parentE, String name,
			String paramtype) {
		// <!ELEMENT PARAMVALUE
		// (VALUE|VALUE.REFERENCE|VALUE.ARRAY|VALUE.REFARRAY)?>)
		// <!ATTLIST PARAMTYPE %NAME;%PARAMTYPE%>
		Element e = doc.createElement("PARAMVALUE");
		if (name != null) {
			e.setAttribute("NAME", name);
		}
		if (paramtype != null) {
			e.setAttribute("PARAMTYPE", paramtype);
		}
		parentE.appendChild(e);
		return e;
	}

	public static Element createSIMPLERSP(Document doc, Element parentE) {
		// <!ELEMENT SIMPLERSP (METHODRESPONSE|IMETHODRESPONSE)>
		Element e = doc.createElement("SIMPLERSP");

		parentE.appendChild(e);
		return e;
	}

	public static Element createSIMPLEEXPRSP(Document doc, Element parentE) {
		// <!ELEMENT SIMPLEEXPRSP (EXPMETHODRESPONSE) >
		Element e = doc.createElement("SIMPLEEXPRSP");

		parentE.appendChild(e);
		return e;
	}

	public static Element createMETHODRESPONSE(Document doc, Element parentE, String name) {
		// <!ELEMENT METHODRESPONSE (ERROR|(RETURNVALUE?,PARAMVALUE*))>
		// <!ATTLIST METHODRESPONSE
		// %CIMName;>

		// %CIMName;>
		Element e = doc.createElement("METHODRESPONSE");
		if (name != null) {
			e.setAttribute("NAME", name);
		}
		parentE.appendChild(e);
		return e;
	}

	public static Element createIMETHODRESPONSE(Document doc, Element parentE, String name) {
		// <!ELEMENT IMETHODRESPONSE (ERROR|IRETURNVALUE?)>
		// <!ATTLIST IMETHODRESPONSE
		// %CIMName;>
		// %CIMName;>
		Element e = doc.createElement("IMETHODRESPONSE");
		if (name != null) {
			e.setAttribute("NAME", name);
		}
		parentE.appendChild(e);
		return e;
	}

	public static Element createEXPMETHODRESPONSE(Document doc, Element parentE, String name) {
		// <!ELEMENT EXPMETHODRESPONSE (ERROR|IRETURNVALUE?)>
		// <!ATTLIST EXPMETHODRESPONSE
		// %CIMName;>
		Element e = doc.createElement("EXPMETHODRESPONSE");
		if (name != null) {
			e.setAttribute("NAME", name);
		}
		parentE.appendChild(e);
		return e;
	}

	public static Element createIRETURNVALUE(Document doc, Element parentE) {
		// <!ELEMENT IRETURNVALUE
		// (CLASSNAME*|INSTANCENAME*|VALUE*|VALUE.OBJECTWITHPATH*|VALUE.OBJECTWITHLOCALPATH*
		// VALUE.OBJECT*|OBJECTPATH*|QUALIFIER.DECLARATION*|VALUE.ARRAY?|VALUE.REFERENCE?|
		// CLASS*|INSTANCE*|VALUE.NAMEDINSTANCE*)>
		Element e = doc.createElement("IRETURNVALUE");
		parentE.appendChild(e);
		return e;
	}

	public static Element createIMETHODCALL(Document doc, Element parentE, String name) {
		// <!ELEMENT IMETHODCALL (LOCALNAMESPACEPATH,IPARAMVALUE*)>
		// <!ATTLIST IMETHODCALL %NAME;>
		Element e = doc.createElement("IMETHODCALL");
		if (name != null) {
			e.setAttribute("NAME", name);
		}
		parentE.appendChild(e);
		return e;
	}

	public static Element createIPARAMVALUE(Document doc, Element parentE, String name) {
		// <!ELEMENT PARAMVALUE
		// (VALUE|VALUE.REFERENCE|VALUE.ARRAY|VALUE.NAMEDINSTANCE
		// |CLASSNAME|INSTANCENAME|QUALIFIER.DECLARATION|CLASS|INSTANCE|?>)
		// <!ATTLIST PARAMTYPE %NAME;>
		Element e = doc.createElement("IPARAMVALUE");
		if (name != null) {
			e.setAttribute("NAME", name);
		}
		parentE.appendChild(e);
		return e;
	}

	public static Element createERROR(Document doc, Element parentE, CIMError error) {
		// <!ELEMENT ERROR EMPTY>
		// <!ATTLIST ERROR
		// CODE CDATA #REQUIRED
		// DESCRIPTION CDATA #IMPLIED>
		Element e = doc.createElement("ERROR");
		int code;
		if ((code = error.getCode()) > 0) {
			e.setAttribute("CODE", Integer.toString(code));
		}
		String description = error.getDescription();
		if (description != null) {
			e.setAttribute("DESCRIPTION", description);
		}

		parentE.appendChild(e);
		return e;
	}

	public static Element createQUALIFIER_DECLARATION(Document doc, Element parentE,
			CIMQualifierType qualifiertype) {
		// <!ELEMENT QUALIFIER.DECLARATION (SCOPE?,(VALUE|VALUE.ARRAY)?)>
		// <!ATTLIST QUALIFIER.DECLARATION
		// %CIMName;
		// %CIMType; #REQUIRED
		// ISARRAY (true|false) #IMPLIED
		// %ArraySize;
		// %QualifierFlavor;>

		String pValueTypeStr = getTypeStr(qualifiertype.getType());
		Element qualifierdeclarationE = doc.createElement("QUALIFIER.DECLARATION");
		qualifierdeclarationE.setAttribute("NAME", qualifiertype.getName());
		qualifierdeclarationE.setAttribute("TYPE", pValueTypeStr);
		qualifierdeclarationE.setAttribute("ISARRAY", qualifiertype.isArrayValue() ? "true"
				: "false");

		Vector flavors = qualifiertype.getFlavor();
		for (int n = 0; n < flavors.size(); n++) {
			CIMFlavor flavor = (CIMFlavor) flavors.elementAt(n);
			if (flavor.equals(CIMFlavor.getFlavor(CIMFlavor.TRANSLATE))) {
				qualifierdeclarationE.setAttribute("TRANSLATABLE", "true");
			} else if (flavor.equals(CIMFlavor.getFlavor(CIMFlavor.DISABLEOVERRIDE))) {
				qualifierdeclarationE.setAttribute("OVERRIDABLE", "false");
			} else if (flavor.equals(CIMFlavor.getFlavor(CIMFlavor.RESTRICTED))) {
				qualifierdeclarationE.setAttribute("TOSUBCLASS", "true");
			}
		}

		Vector scopes = qualifiertype.getScope();
		if (scopes.size() > 0) {
			Element scopeE = doc.createElement("SCOPE");
			if (qualifiertype.hasScope(CIMScope.getScope(CIMScope.CLASS))) scopeE.setAttribute(
					"CLASS", "true");
			if (qualifiertype.hasScope(CIMScope.getScope(CIMScope.ASSOCIATION))) scopeE
					.setAttribute("ASSOCIATION", "true");
			if (qualifiertype.hasScope(CIMScope.getScope(CIMScope.REFERENCE))) scopeE.setAttribute(
					"REFERENCE", "true");
			if (qualifiertype.hasScope(CIMScope.getScope(CIMScope.PROPERTY))) scopeE.setAttribute(
					"PROPERTY", "true");
			if (qualifiertype.hasScope(CIMScope.getScope(CIMScope.METHOD))) scopeE.setAttribute(
					"METHOD", "true");
			if (qualifiertype.hasScope(CIMScope.getScope(CIMScope.PARAMETER))) scopeE.setAttribute(
					"PARAMETER", "true");
			if (qualifiertype.hasScope(CIMScope.getScope(CIMScope.INDICATION))) scopeE
					.setAttribute("INDICATION", "true");
			qualifierdeclarationE.appendChild(scopeE);
		}

		if (qualifiertype.hasDefaultValue() && qualifiertype.getDefaultValue().getValue() != null) {
			createVALUE(doc, qualifierdeclarationE, qualifiertype.getDefaultValue());
		}

		parentE.appendChild(qualifierdeclarationE);
		return qualifierdeclarationE;
	}

	public static Element createQUALIFIER(Document doc, Element parentE, CIMQualifier qualifier) {
		// <!ELEMENT QUALIFIER (VALUE|VALUE.ARRAY)>
		// <!ATTLIST QUALIFIER
		// %CIMName;
		// %CIMType; #REQUIRED
		// %Propagated;
		// %QualifierFlavor;>

		CIMValue value = qualifier.getValue();
		if (value == null) return null;

		Element qualifierE = createQUALIFIER(doc, parentE, qualifier.getName(), getTypeStr(value
				.getType()));

		if (qualifier.isPropagated()) {
			qualifierE.setAttribute("PROPAGATED", "true");
		}
		Vector flavors = qualifier.getFlavor();
		for (int n = 0; n < flavors.size(); n++) {
			CIMFlavor flavor = (CIMFlavor) flavors.elementAt(n);
			if (flavor.equals(CIMFlavor.getFlavor(CIMFlavor.TRANSLATE))) {
				qualifierE.setAttribute("TRANSLATABLE", "true");
			} else if (flavor.equals(CIMFlavor.getFlavor(CIMFlavor.DISABLEOVERRIDE))) {
				qualifierE.setAttribute("OVERRIDABLE", "false");
			} else if (flavor.equals(CIMFlavor.getFlavor(CIMFlavor.RESTRICTED))) {
				qualifierE.setAttribute("TOSUBCLASS", "false");
			}
		}

		createVALUE(doc, qualifierE, value);

		parentE.appendChild(qualifierE);
		return qualifierE;
	}

	public static void createQUALIFIERS(Document doc, Element parentE, Vector qualifiersV) {
		for (int i = 0; i < qualifiersV.size(); i++) {
			CIMQualifier qualifier = (CIMQualifier) qualifiersV.elementAt(i);
			createQUALIFIER(doc, parentE, qualifier);
		}
	}

	public static void createPROPERTIES(Document doc, Element parentE, Vector properties) {
		Iterator iter = properties.iterator();
		while (iter.hasNext()) {
			CIMProperty property = (CIMProperty) iter.next();
			createPROPERTY(doc, parentE, property);
		}
	}

	public static Element createPROPERTY(Document doc, Element parentE, CIMProperty property) {
		CIMDataType propertyDataType = property.getType();
		String pValueTypeStr = getTypeStr(propertyDataType);
		CIMValue pValue = property.getValue();

		Element propertyE;
		if (propertyDataType.isArrayType()) {
			propertyE = createPROPERTYARRAY(doc, parentE, property.getName(), pValueTypeStr);
		} else if (propertyDataType.isReferenceType()) {
			propertyE = createPROPERTYREFERENCE(doc, parentE, property.getName(), propertyDataType
					.getRefClassName());

		} else {
			propertyE = createPROPERTY(doc, parentE, property.getName(), pValueTypeStr);
		}

		String classorigin = property.getOriginClass();
		if (classorigin != null && classorigin.length() > 0) propertyE.setAttribute("CLASSORIGIN",
				classorigin);

		if (property.isPropagated()) propertyE.setAttribute("PROPAGATED", "true");

		createQUALIFIERS(doc, propertyE, property.getQualifiers());

		if (pValue != null) createVALUE(doc, propertyE, pValue);

		return propertyE;
	}

	public static Element createVALUEARRAY(Document doc, Element parentE, Vector vect) {
		Element valuearrayE = createVALUEARRAY(doc, parentE);

		Iterator iter = vect.iterator();
		while (iter.hasNext()) {
			CIMValue value = new CIMValue(iter.next());
			createVALUE(doc, valuearrayE, value);
		}
		parentE.appendChild(valuearrayE);
		return valuearrayE;
	}

	public static Element createVALUE(Document doc, Element parentE, CIMValue argValue) {
		if (argValue == null || argValue.getValue() == null) return null;

		Element valueE = null;
		if (argValue.isArrayValue()) {
			valueE = createVALUEARRAY(doc, parentE, (Vector) argValue.getValue());
		} else {
			Object obj = argValue.getValue();
			CIMDataType type = argValue.getType();
			if (type != null && type.getType() == CIMDataType.REFERENCE) {
				valueE = createVALUEREFERENCE(doc, parentE);

				CIMObjectPath op = (CIMObjectPath) obj;
				Vector keys = op.getKeys();
				if (op.getHost() == null || "".equals(op.getHost())) {
					if (op.getNameSpace()==null || "".equals(op.getNameSpace())) {
						if (keys.size() > 0) {
							createINSTANCENAME(doc, valueE, op);
						} else {
							createCLASSNAME(doc, valueE, op.getObjectName());
						}
					} else {
						if (keys.size() > 0) {
							createLOCALINSTANCEPATH(doc, valueE, op);
						} else {
							createLOCALCLASSPATH(doc, valueE, op);
						}
					}
				} else {
					if (keys.size() > 0) {
						createINSTANCEPATH(doc, valueE, op);
					} else {
						createCLASSPATH(doc, valueE, op);
					}
				}
				// createOBJECTPATH(doc, valueE, (CIMObjectPath) obj);
			} else {
				if (obj instanceof CIMInstance) {
					valueE = createVALUE(doc, parentE);
					createINSTANCE(doc, valueE, (CIMInstance) obj);
				} else if (obj instanceof CIMClass) {
					valueE = createVALUE(doc, parentE);
					createCLASS(doc, valueE, (CIMClass) obj);
				} else {
					createVALUE(doc, parentE, argValue.getValue().toString());
				}
			}
		}

		return valueE;
	}

	public static Element createIPARAMVALUE(Document doc, Element parentE, CIMValue argValue) {
		if (argValue == null || argValue.getValue() == null) return null;

		Element iparamvalueE = createIPARAMVALUE(doc, parentE, "NewValue");

		createVALUE(doc, iparamvalueE, argValue);

		parentE.appendChild(iparamvalueE);
		return iparamvalueE;
	}

	public static Element createINSTANCE(Document doc, Element parentE, CIMInstance instance) {
		// <!ELEMENT INSTANCE
		// (QUALIFIER*,(PROPERTY|PROPERTY.ARRAY|PROPERTY.REFERENCE)*)>
		// <!ATTLIST INSTANCE
		// %ClassName;>

		String className = instance.getObjectPath().getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_FAILED,
				"null class name");

		Element instanceE = createINSTANCE(doc, parentE, className);

		createQUALIFIERS(doc, instanceE, instance.getQualifiers());
		createPROPERTIES(doc, instanceE, instance.getAllProperties());

		if (parentE != null) parentE.appendChild(instanceE);

		return instanceE;
	}

	public static Element createOBJECTPATH(Document doc, Element parentE, CIMObjectPath path) {
		Element objectpathE = doc.createElement("OBJECTPATH");

		Vector keys = path.getKeys();
		if (keys.size() > 0) {
			createINSTANCEPATH(doc, objectpathE, path);
		} else {
			createCLASSPATH(doc, objectpathE, path);
		}

		parentE.appendChild(objectpathE);
		return objectpathE;
	}

	public static Element createOBJECTNAME(Document doc, Element parentE, CIMObjectPath path) {

		Vector keys = path.getKeys();
		if (keys.size() > 0) { return createINSTANCENAME(doc, parentE, path); }
		if (path.getObjectName() == null) throw new CIMException(
				CIMException.CIM_ERR_INVALID_PARAMETER, "null class name");
		return createCLASSNAME(doc, parentE, path.getObjectName());
	}

	public static Element createLOCALINSTANCEPATH(Document doc, Element parentE, CIMObjectPath path) {
		Element localinstancepathE = doc.createElement("LOCALINSTANCEPATH");

		createLOCALNAMESPACEPATH(doc, localinstancepathE, path);
		createINSTANCENAME(doc, localinstancepathE, path);

		parentE.appendChild(localinstancepathE);
		return localinstancepathE;
	}

	public static Element createLOCALCLASSPATH(Document doc, Element parentE, CIMObjectPath path) {
		Element localinstancepathE = doc.createElement("LOCALCLASSPATH");

		createLOCALNAMESPACEPATH(doc, localinstancepathE, path);

		if (path.getObjectName() == null) throw new CIMException(
				CIMException.CIM_ERR_INVALID_PARAMETER, "null class name");
		createCLASSNAME(doc, localinstancepathE, path.getObjectName());

		parentE.appendChild(localinstancepathE);
		return localinstancepathE;
	}

	public static Element createLOCALOBJECTPATH(Document doc, Element parentE, CIMObjectPath path) {

		Vector keys = path.getKeys();
		if (keys.size() > 0) { return createLOCALINSTANCEPATH(doc, parentE, path); }
		return createLOCALCLASSPATH(doc, parentE, path);
	}

	public static Element createVALUEREFERENCE(Document doc, Element parentE, CIMObjectPath path) {
		Element objectpathE = doc.createElement("VALUE.REFERENCE");

		String ns = path.getNameSpace();
		if (path.getHost() == null || "".equals(path.getHost().trim())) {
			if (path.getNameSpace() == null || "".equals(ns.trim())) {
				createINSTANCENAME(doc, objectpathE, path);
			} else {
				createLOCALOBJECTPATH(doc, objectpathE, path);
			}
		} else {
			Vector keys = path.getKeys();
			if (keys.size() > 0) {
				createINSTANCEPATH(doc, objectpathE, path);
			} else {
				createCLASSPATH(doc, objectpathE, path);
			}
		}

		parentE.appendChild(objectpathE);
		return objectpathE;
	}

	public static Element createINSTANCENAME(Document doc, Element parentE, CIMObjectPath instanceOP) {

		Element instancenameE = doc.createElement("INSTANCENAME");
		String classname = instanceOP.getObjectName();
		if (classname != null) {
			instancenameE.setAttribute("CLASSNAME", classname);
		}

		Vector keysV = instanceOP.getKeys();
		for (int ii = 0; ii < keysV.size(); ii++) {
			CIMProperty p = (CIMProperty) keysV.elementAt(ii);
			String pName = p.getName();
			CIMValue pValue = p.getValue();
			// if (_pValue == null) TODO what happend when a KeyBinding has a
			// null property value?
			if (pValue == null) {
				CIMDataType type = p.getType();
				switch (type.getType()) {
					case CIMDataType.STRING:
						pValue = new CIMValue("", type);
						break;
					case CIMDataType.UINT8:
						pValue = new CIMValue(new UnsignedInt8((short) 0), type);
						break;
					case CIMDataType.UINT16:
						pValue = new CIMValue(new UnsignedInt16(0), type);
						break;
					case CIMDataType.UINT32:
						pValue = new CIMValue(new UnsignedInt32(0), type);
						break;
					case CIMDataType.UINT64: // 16627
						pValue = new CIMValue(new UnsignedInt64(BigInteger.valueOf(0)), type);
						break;
					case CIMDataType.SINT8:
						pValue = new CIMValue(new Byte((byte) 0), type);
						break;
					case CIMDataType.SINT16:
						pValue = new CIMValue(new Short((short) 0), type);
						break;
					case CIMDataType.SINT32:
						pValue = new CIMValue(new Integer(0), type);
						break;
					case CIMDataType.SINT64:
						pValue = new CIMValue(new Long(0), type);
						break;
					case CIMDataType.NUMERIC:
						pValue = new CIMValue(new UnsignedInt64(BigInteger.valueOf(0)), type);
						break;
					default:
						throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
								"Null CIM Property value" + p);
				}
			}

			Element keybindingE = createKEYBINDING(doc, instancenameE, pName);
			if (pValue.getType().isReferenceType()) {
				CIMObjectPath refOP = (CIMObjectPath) pValue.getValue();

				createVALUEREFERENCE(doc, keybindingE, refOP);
			} else {
				String valueStr = pValue.getValue().toString();
				String valueTypeStr = getTypeStr(pValue.getType());
				createKEYVALUE(doc, keybindingE, valueTypeStr, valueStr);
			}
		}

		parentE.appendChild(instancenameE);
		return instancenameE;
	}

	public static Element createCLASSPATH(Document doc, Element parentE, CIMObjectPath path) {
		Element classpathE = doc.createElement("CLASSPATH");

		createCLASSNAME(doc, classpathE, path.getNameSpace());

		parentE.appendChild(classpathE);
		return classpathE;
	}

	public static void createPARAMETERS(Document doc, Element parentE, Vector parameters) {
		Iterator iter = parameters.iterator();
		while (iter.hasNext()) {
			CIMParameter parameter = (CIMParameter) iter.next();
			createPARAMETER(doc, parentE, parameter);
		}
	}

	public static Element createPARAMETER(Document doc, Element parentE, CIMParameter parameter) {
		Element parameterE = doc.createElement("PARAMETER");

		CIMDataType cimDataType = parameter.getType();
		if (cimDataType != null) {
			if (cimDataType.isArrayType()) {
				if (cimDataType.isReferenceType()) {
					parameterE = doc.createElement("PARAMETER.REFARRAY");
					parameterE.setAttribute("NAME", parameter.getName());
					parameterE.setAttribute("REFERENCECLASS", cimDataType.getRefClassName());

					createQUALIFIERS(doc, parameterE, parameter.getQualifiers());
				} else {
					parameterE = doc.createElement("PARAMETER.ARRAY");
					parameterE.setAttribute("NAME", parameter.getName());

					String datatype = getTypeStr(parameter.getType());
					if (datatype != null && datatype.length() != 0) {
						parameterE.setAttribute("TYPE", datatype);
					}
					parameterE.setAttribute("REFERENCECLASS", cimDataType.getRefClassName());

					createQUALIFIERS(doc, parameterE, parameter.getQualifiers());
				}

				parentE.appendChild(parameterE);
				return parameterE;
			}
			if (cimDataType.isReferenceType()) {
				parameterE = doc.createElement("PARAMETER.REFERENCE");
				parameterE.setAttribute("NAME", parameter.getName());
				parameterE.setAttribute("REFERENCECLASS", cimDataType.getRefClassName());

				createQUALIFIERS(doc, parameterE, parameter.getQualifiers());

				parentE.appendChild(parameterE);
				return parameterE;
			}
			String datatype = getTypeStr(parameter.getType());
			if (datatype != null && datatype.length() != 0) {
				parameterE.setAttribute("TYPE", datatype);
			}
		}
		parameterE.setAttribute("NAME", parameter.getName());
		createQUALIFIERS(doc, parameterE, parameter.getQualifiers());

		parentE.appendChild(parameterE);
		return parameterE;
	}

	public static void createMETHODS(Document doc, Element parentE, Vector methods, String classname) {
		Iterator iter = methods.iterator();
		while (iter.hasNext()) {
			CIMMethod method = (CIMMethod) iter.next();
			createMETHOD(doc, parentE, method, classname);
		}
	}

	public static Element createMETHOD(Document doc, Element parentE, CIMMethod method,
			String classname) {
		Element methodE = doc.createElement("METHOD");

		methodE.setAttribute("NAME", method.getName());
		String datatype = getTypeStr(method.getType());
		methodE.setAttribute("TYPE", datatype);
		String classorigin = method.getOriginClass();

		if (classorigin != null && classorigin.length() != 0) methodE.setAttribute("CLASSORIGIN",
				classorigin);

		// 17459
		if (classorigin != null && classorigin.equalsIgnoreCase(classname)) {
			methodE.setAttribute("PROPAGATED", "true");
		} else {
			methodE.setAttribute("PROPAGATED", "false");
		}
		createQUALIFIERS(doc, methodE, method.getQualifiers());
		createPARAMETERS(doc, methodE, method.getParameters());

		parentE.appendChild(methodE);
		return methodE;
	}

	public static Element createCLASS(Document doc, Element parentE, CIMClass clazz) {

		Element classE = createCLASS(doc, parentE, clazz.getName(), clazz.getSuperClass());

		createQUALIFIERS(doc, classE, clazz.getQualifiers());
		createPROPERTIES(doc, classE, clazz.getProperties());
		createMETHODS(doc, classE, clazz.getMethods(), clazz.getName());

		// parentE.appendChild(classE);
		return classE;
	}

	public static Element createHOST(Document doc, Element parentE, String host) {
		Element hostE = doc.createElement("HOST");

		Text hostT = doc.createTextNode(host);
		hostE.appendChild(hostT);

		parentE.appendChild(hostE);
		return hostE;
	}

	public static Element createNAMESPACEPATH(Document doc, Element parentE, CIMObjectPath path) {
		Element namespacepathE = doc.createElement("NAMESPACEPATH");

		String host = path.getHost();
		if (host == null) {
			// try {
			// host = InetAddress.getLocalHost().getHostAddress();
			// } catch (Exception e) {
			// throw new CIMXMLBuilderException("Error while resolving
			// hostname");
			// }
			host = "unassigned-hostname";
		}
		createHOST(doc, namespacepathE, host);
		createLOCALNAMESPACEPATH(doc, namespacepathE, path);

		parentE.appendChild(namespacepathE);
		return namespacepathE;
	}

	public static Element createINSTANCEPATH(Document doc, Element parentE, CIMObjectPath path) {
		Element instancepathE = doc.createElement("INSTANCEPATH");

		createNAMESPACEPATH(doc, instancepathE, path);
		createINSTANCENAME(doc, instancepathE, path);
		parentE.appendChild(instancepathE);
		return instancepathE;
	}

	public static Element createVALUENAMEDINSTANCE(Document doc, Element parentE,
			CIMObjectPath path, CIMInstance inst) {
		Element valuenamedinstanceE = doc.createElement("VALUE.NAMEDINSTANCE");
		createINSTANCENAME(doc, valuenamedinstanceE, path);
		createINSTANCE(doc, valuenamedinstanceE, inst);

		parentE.appendChild(valuenamedinstanceE);
		return valuenamedinstanceE;
	}

	public static Element createVALUENAMEDINSTANCE(Document doc, Element parentE, CIMInstance inst) {
		Element valuenamedinstanceE = doc.createElement("VALUE.NAMEDINSTANCE");
		createINSTANCENAME(doc, valuenamedinstanceE, inst.getObjectPath());
		createINSTANCE(doc, valuenamedinstanceE, inst);

		parentE.appendChild(valuenamedinstanceE);
		return valuenamedinstanceE;
	}

	public static Element createVALUEOBJECTWITHPATH(Document doc, Element parentE, Object obj,
			CIMNameSpace ns) {
		Element valueobjectwithpathE = doc.createElement("VALUE.OBJECTWITHPATH");
		if (obj instanceof CIMClass) {
			CIMClass clazz = (CIMClass) obj;

			if (clazz.getObjectPath().getNameSpace() == null
					|| clazz.getObjectPath().getNameSpace().length() == 0) clazz.getObjectPath()
					.setNameSpace(ns);
			createCLASSPATH(doc, valueobjectwithpathE, clazz.getObjectPath());
			createCLASS(doc, valueobjectwithpathE, clazz);
		} else if (obj instanceof CIMInstance) {
			CIMInstance inst = (CIMInstance) obj;
			if (inst.getObjectPath().getNameSpace() == null
					|| inst.getObjectPath().getNameSpace().length() == 0) inst.getObjectPath()
					.setNameSpace(ns);

			createINSTANCEPATH(doc, valueobjectwithpathE, inst.getObjectPath());
			createINSTANCE(doc, valueobjectwithpathE, inst);
		}

		parentE.appendChild(valueobjectwithpathE);
		return valueobjectwithpathE;
	}

	public static Element createVALUEOBJECTWITHLOCALPATH(Document doc, Element parentE, Object obj,
			CIMNameSpace ns) {
		Element valueobjectwithpathE = doc.createElement("VALUE.OBJECTWITHLOCALPATH");
		if (obj instanceof CIMClass) {
			CIMClass clazz = (CIMClass) obj;

			if (clazz.getObjectPath().getNameSpace() == null
					|| clazz.getObjectPath().getNameSpace().length() == 0) clazz.getObjectPath()
					.setNameSpace(ns);
			createLOCALCLASSPATH(doc, valueobjectwithpathE, clazz.getObjectPath());
			createCLASS(doc, valueobjectwithpathE, clazz);
		} else if (obj instanceof CIMInstance) {
			CIMInstance inst = (CIMInstance) obj;
			if (inst.getObjectPath().getNameSpace() == null
					|| inst.getObjectPath().getNameSpace().length() == 0) inst.getObjectPath()
					.setNameSpace(ns);

			createLOCALINSTANCEPATH(doc, valueobjectwithpathE, inst.getObjectPath());
			createINSTANCE(doc, valueobjectwithpathE, inst);
		}

		parentE.appendChild(valueobjectwithpathE);
		return valueobjectwithpathE;
	}

	public static Element createIRETURNVALUE_ERROR(Document doc, Element parentE, CIMError error) {
		Element ireturnvalueE = doc.createElement("IRETURNVALUE");

		parentE.appendChild(ireturnvalueE);
		return ireturnvalueE;
	}

	public static Element createIRETURNVALUE_GETINSTANCE(Document doc, Element parentE,
			CIMInstance inst) {
		Element ireturnvalueE = doc.createElement("IRETURNVALUE");
		createINSTANCENAME(doc, ireturnvalueE, inst.getObjectPath());

		return ireturnvalueE;
	}

	public static Element createIRETURNVALUE_ASSOCIATORS_NAMES(Document doc, Element parentE,
			Vector resultSet) throws Exception {
		Element ireturnvalueE = doc.createElement("IRETURNVALUE");

		Iterator iter = resultSet.iterator();

		while (iter.hasNext()) {
			CIMObjectPath path = (CIMObjectPath) iter.next();

			if (path.getHost() == null || "".equals(path.getHost())) createLOCALOBJECTPATH(doc,
					ireturnvalueE, path);
			else createOBJECTPATH(doc, ireturnvalueE, path);
		}

		parentE.appendChild(ireturnvalueE);
		return ireturnvalueE;
	}

	public static Element createIRETURNVALUE_ASSOCIATORS(Document doc, Element parentE,
			Vector resultSet, CIMNameSpace ns) throws Exception {
		Element ireturnvalueE = doc.createElement("IRETURNVALUE");

		Iterator iter = resultSet.iterator();

		while (iter.hasNext()) {
			Object obj = iter.next();
			CIMObjectPath op = null;
			if (obj instanceof CIMClass) {
				op = ((CIMClass) obj).getObjectPath();
			} else if (obj instanceof CIMInstance) {
				op = ((CIMInstance) obj).getObjectPath();
			} else {
		        throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
		                        "object in result set neither class nor instance!");
			}
			if (op.getHost() == null || "".equals(op.getHost())) {
				createVALUEOBJECTWITHLOCALPATH(doc, ireturnvalueE, obj, ns);
			} else {
				createVALUEOBJECTWITHPATH(doc, ireturnvalueE, obj, ns);
			}
		}

		parentE.appendChild(ireturnvalueE);
		return ireturnvalueE;
	}

	public static Element createIRETURNVALUE_ENUMERATE_INSTANCENAME(Document doc, Element parentE,
			Vector resultSet, CIMNameSpace ns) throws Exception {
		Element ireturnvalueE = doc.createElement("IRETURNVALUE");

		Iterator iter = resultSet.iterator();

		while (iter.hasNext()) {
			Object obj = iter.next();
			CIMObjectPath op = null;
			if (obj instanceof CIMClass) {
				op = ((CIMClass) obj).getObjectPath();
			} else if (obj instanceof CIMInstance) {
				op = ((CIMInstance) obj).getObjectPath();
			} else {
		        throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
		                        "object in result set neither class nor instance!");
			}
			if (op.getHost() == null || "".equals(op.getHost())) {
				createVALUEOBJECTWITHLOCALPATH(doc, ireturnvalueE, obj, ns);
			} else {
				createVALUEOBJECTWITHPATH(doc, ireturnvalueE, obj, ns);
			}
		}

		parentE.appendChild(ireturnvalueE);
		return ireturnvalueE;
	}

	public static Element createIRETURNVALUE(Document doc, Element parentE, Vector resultSet) {
		Element ireturnvalueE = doc.createElement("IRETURNVALUE");
		if (resultSet.size() > 0) {
			Object obj = resultSet.elementAt(0);
			if (obj instanceof CIMClass) {
				Iterator iter = resultSet.iterator();
				while (iter.hasNext()) {
					CIMClass clazz = (CIMClass) iter.next();

					Element classnameE = doc.createElement("CLASSNAME");
					ireturnvalueE.appendChild(classnameE);
					if (clazz.getName() != null) classnameE.setAttribute("NAME", clazz.getName());
				}
			} else if (obj instanceof CIMInstance) {
				Iterator iter = resultSet.iterator();
				while (iter.hasNext()) {
					CIMInstance inst = (CIMInstance) iter.next();
					createVALUENAMEDINSTANCE(doc, ireturnvalueE, inst);
				}
			}
		}
		parentE.appendChild(ireturnvalueE);
		return ireturnvalueE;
	}

	public static Element createIRETURNVALUE_ENUMERATE_CLASSNAME(Document doc, Element parentE,
			Vector resultSet) {
		Element ireturnvalueE = doc.createElement("IRETURNVALUE");
		if (resultSet.size() > 0) {
			Iterator iter = resultSet.iterator();
			while (iter.hasNext()) {
				CIMClass clazz = (CIMClass) iter.next();

				Element classnameE = doc.createElement("CLASSNAME");
				ireturnvalueE.appendChild(classnameE);
				if (clazz.getName() != null) classnameE.setAttribute("NAME", clazz.getName());
			}
		}
		parentE.appendChild(ireturnvalueE);
		return ireturnvalueE;
	}

	// ebak: [ 1656285 ] IndicationHandler does not accept non-Integer message ID
	public static Element createIndication_response(Document doc, String ID, CIMError error) {

		// xmlBuilder.create XML
		Element cimE = createCIM(doc);
		Element messageE = createMESSAGE(doc, cimE, ID, "1.0");
		Element simpleexprspE = createSIMPLEEXPRSP(doc, messageE);
		Element expmethodresponseE = createEXPMETHODRESPONSE(doc, simpleexprspE, "ExportIndication");
		if (error == null) {
			createIRETURNVALUE(doc, expmethodresponseE);
		} else {
			createERROR(doc, expmethodresponseE, error);
		}
		// Element
		return cimE;
	}

	public static Element createIRETURNVALUE_ENUMERATE_INSTANCE(Document doc, Element parentE,
			Vector resultSet) {
		Element ireturnvalueE = doc.createElement("IRETURNVALUE");
		if (resultSet.size() > 0) {
			Iterator iter = resultSet.iterator();
			while (iter.hasNext()) {
				CIMInstance inst = (CIMInstance) iter.next();
				createVALUENAMEDINSTANCE(doc, ireturnvalueE, inst);
			}
		}
		parentE.appendChild(ireturnvalueE);
		return ireturnvalueE;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////

	public static String getTypeStr(CIMDataType type) {
		if (type == null) return "string";
		switch (type.getType()) {
			case CIMDataType.UINT8_ARRAY:
			case CIMDataType.UINT8:
				return "uint8";
			case CIMDataType.UINT16_ARRAY:
			case CIMDataType.UINT16:
				return "uint16";
			case CIMDataType.UINT32_ARRAY:
			case CIMDataType.UINT32:
				return "uint32";
			case CIMDataType.UINT64_ARRAY:
			case CIMDataType.UINT64:
				return "uint64";
			case CIMDataType.SINT8_ARRAY:
			case CIMDataType.SINT8:
				return "sint8";
			case CIMDataType.SINT16_ARRAY:
			case CIMDataType.SINT16:
				return "sint16";
			case CIMDataType.SINT32_ARRAY:
			case CIMDataType.SINT32:
				return "sint32";
			case CIMDataType.SINT64_ARRAY:
			case CIMDataType.SINT64:
				return "sint64";
			case CIMDataType.STRING_ARRAY:
			case CIMDataType.STRING:
				return "string";
			case CIMDataType.BOOLEAN_ARRAY:
			case CIMDataType.BOOLEAN:
				return "boolean";
			case CIMDataType.REAL32_ARRAY:
			case CIMDataType.REAL32:
				return "real32";
			case CIMDataType.REAL64_ARRAY:
			case CIMDataType.REAL64:
				return "real64";
			case CIMDataType.DATETIME_ARRAY:
			case CIMDataType.DATETIME:
				return "datetime";
			case CIMDataType.CHAR16_ARRAY:
			case CIMDataType.CHAR16:
				return "char16";
			case CIMDataType.NUMERIC:
				return "numeric";
			case CIMDataType.NULL:
				return "null";
			// case CIMDataType.REFERENCE : return getRefClassName() + " " +
			// REF;
			// case CIMDataType.REFERENCE_ARRAY:return getRefClassName() + "[] "
			// + REF;
			default:
				return "";
		}
	}

	public static String getOpTypeStr(CIMDataType type) {
		if (type == null) return "string";
		switch (type.getType()) {
			case CIMDataType.UINT8_ARRAY:
			case CIMDataType.UINT8:
				return "numeric";
			case CIMDataType.UINT16_ARRAY:
			case CIMDataType.UINT16:
				return "numeric";
			case CIMDataType.UINT32_ARRAY:
			case CIMDataType.UINT32:
				return "numeric";
			case CIMDataType.UINT64_ARRAY:
			case CIMDataType.UINT64:
				return "numeric";
			case CIMDataType.SINT8_ARRAY:
			case CIMDataType.SINT8:
				return "numeric";
			case CIMDataType.SINT16_ARRAY:
			case CIMDataType.SINT16:
				return "numeric";
			case CIMDataType.SINT32_ARRAY:
			case CIMDataType.SINT32:
				return "numeric";
			case CIMDataType.SINT64_ARRAY:
			case CIMDataType.SINT64:
				return "numeric";
			case CIMDataType.STRING_ARRAY:
			case CIMDataType.STRING:
				return "string";
			case CIMDataType.BOOLEAN_ARRAY:
			case CIMDataType.BOOLEAN:
				return "boolean";
			case CIMDataType.REAL32_ARRAY:
			case CIMDataType.REAL32:
				return "numeric";
			case CIMDataType.REAL64_ARRAY:
			case CIMDataType.REAL64:
				return "numeric";
			case CIMDataType.DATETIME_ARRAY:
			case CIMDataType.DATETIME:
				return "string";
			case CIMDataType.CHAR16_ARRAY:
			case CIMDataType.CHAR16:
				return "string";
			case CIMDataType.NUMERIC:
				return "numeric";
			case CIMDataType.NULL:
				return "string";
			default:
				return "string";
		}
	}

	public static Element createLOCALNAMESPACEPATH(Document doc, Element parentE, CIMObjectPath name) {
		if (name == null) return null;
		// TODO: name(ObjectPath) should not be null, should an exception be
		// thrown?
		// This assumes that the NameSpace does not consist exclusively of
		// empties like "////"

		Element localnamespacepathE = doc.createElement("LOCALNAMESPACEPATH");
		String ns = name.getNameSpace();
		if (ns != null && ns.length() > 0) {
			// TODO namespace should not be null, CIMClient should make sure it
			// is not null
			int next = 0, prev = 0;
			// int
			while ((next = ns.indexOf(CIMNameSpace.NAMESPACE_SEPARATOR, prev)) > -1) {
				String path = ns.substring(prev, next);
				if (path.length() > 0) {
					createNAMESPACE(doc, localnamespacepathE, path);
				}
				prev = next + 1;
			}
			if (prev < ns.length()) {
				createNAMESPACE(doc, localnamespacepathE, ns.substring(prev, ns.length()));
			}
		}
		parentE.appendChild(localnamespacepathE);

		return localnamespacepathE;
	}
}
