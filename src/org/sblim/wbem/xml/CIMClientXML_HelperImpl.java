/**
 * CIMClientXML_HelperImpl.java
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
 *   13521    2004-11-26  thschaef     XML Request Composition for static method call is wrong
 *   18075    2005-08-11  pineiro5     Can not use method CIMClient.invokeMethod
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1365086 	  2006-10-25  ebak	       Possible bug in createQualifier
 * 1610046    2006-12-18  lupusalex    Does not escape trailing spaces <KEYVALUE> 
 * 1610046    2007-01-10  lupusalex    Rework: Does not escape trailing spaces <KEYVALUE>
 * 1649611    2007-01-31  lupusalex    Interop issue: Quotes not escaped by client
 * 1676343    2007-02-08  lupusalex    Remove dependency from Xerces
 * 1732645    2007-06-07  ebak         Wrong reference building in METHODCALL request
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sblim.wbem.cim.CIMArgument;
import org.sblim.wbem.cim.CIMClass;
import org.sblim.wbem.cim.CIMDataType;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.cim.CIMProperty;
import org.sblim.wbem.cim.CIMQualifierType;
import org.sblim.wbem.cim.CIMValue;
import org.sblim.wbem.cimxml.CimXmlSerializer;
import org.sblim.wbem.client.indications.CIMError;
import org.sblim.wbem.client.operations.CIMAssociatorNamesOp;
import org.sblim.wbem.client.operations.CIMAssociatorsOp;
import org.sblim.wbem.client.operations.CIMCreateClassOp;
import org.sblim.wbem.client.operations.CIMCreateInstanceOp;
import org.sblim.wbem.client.operations.CIMCreateNameSpaceOp;
import org.sblim.wbem.client.operations.CIMCreateQualifierTypeOp;
import org.sblim.wbem.client.operations.CIMDeleteClassOp;
import org.sblim.wbem.client.operations.CIMDeleteInstanceOp;
import org.sblim.wbem.client.operations.CIMDeleteQualifierTypeOp;
import org.sblim.wbem.client.operations.CIMEnumClassNamesOp;
import org.sblim.wbem.client.operations.CIMEnumClassesOp;
import org.sblim.wbem.client.operations.CIMEnumInstanceNamesOp;
import org.sblim.wbem.client.operations.CIMEnumInstancesOp;
import org.sblim.wbem.client.operations.CIMEnumNameSpaceOp;
import org.sblim.wbem.client.operations.CIMEnumQualifierTypesOp;
import org.sblim.wbem.client.operations.CIMExecQueryOp;
import org.sblim.wbem.client.operations.CIMGetClassOp;
import org.sblim.wbem.client.operations.CIMGetInstanceOp;
import org.sblim.wbem.client.operations.CIMGetPropertyOp;
import org.sblim.wbem.client.operations.CIMGetQualifierTypeOp;
import org.sblim.wbem.client.operations.CIMInvokeMethodOp;
import org.sblim.wbem.client.operations.CIMOperation;
import org.sblim.wbem.client.operations.CIMReferenceNamesOp;
import org.sblim.wbem.client.operations.CIMReferencesOp;
import org.sblim.wbem.client.operations.CIMSetClassOp;
import org.sblim.wbem.client.operations.CIMSetInstanceOp;
import org.sblim.wbem.client.operations.CIMSetPropertyOp;
import org.sblim.wbem.client.operations.CIMSetQualifierTypeOp;
import org.sblim.wbem.util.SessionProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CIMClientXML_HelperImpl {

	private static final String CLASSNAME = "org.sblim.wbem.xml.CIMClientXML_HelperImpl";

	private static final String VERSION = "1.0";

	private static final String ASSOCIATOR_NAMES = "AssociatorNames";

	private int iOutputMsgCount = 0;

	private DocumentBuilder iBuilder;

	private Logger iLogger = null;

	public CIMClientXML_HelperImpl() throws ParserConfigurationException {
		iLogger = SessionProperties.getGlobalProperties().getLogger();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		iBuilder = factory.newDocumentBuilder();
	}

	public DocumentBuilder getDocumentBuilder() {
		return iBuilder;
	}

	public Document newDocument() {
		return iBuilder.newDocument();
	}

	public Document parse(InputSource is) throws IOException, SAXException {
		if (is == null) throw new IllegalArgumentException("null inputstream argument");
		return iBuilder.parse(is);
	}

	public static void serialize(OutputStream os, Document doc) throws IOException {
		CimXmlSerializer.serialize(os, doc, false);
	}

	public static void dumpDocument(Document doc) throws IOException {
		OutputStream stream = SessionProperties.getGlobalProperties().getDebugOutputStream();
		if (stream == null) { return; }
		stream.write("<--- request begin -----\n".getBytes());
		CimXmlSerializer.serialize(stream, doc, true);
		stream.write("---- request end ------>\n".getBytes());
	}

	public Element createCIMMessage(Document doc, Element requestE) {
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, "createCIMMessage", new Object[] { doc, requestE });
		}
		Element cimE = CIMXMLBuilderImpl.createCIM(doc);
		Element messageE = CIMXMLBuilderImpl.createMESSAGE(doc, cimE, String
				.valueOf(iOutputMsgCount++), VERSION);
		if (requestE != null) {
			messageE.appendChild(requestE);
		}
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.exiting(CLASSNAME, "createCIMMessage", messageE);
		}

		return messageE;
	}

	public Element createMultiReq(Document doc) {
		Element multireqE = CIMXMLBuilderImpl.createMULTIREQ(doc);
		return multireqE;
	}

	public Element associatorNames_request(Document doc, CIMObjectPath objectName,
			String assocClass, String resultClass, String role, String resultRole) {

		// obtain data
		String className = objectName.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");
		Vector keysV = objectName.getKeys();

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE,
				ASSOCIATOR_NAMES);
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, objectName);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ObjectName");
		Element instancenameE = CIMXMLBuilderImpl.createINSTANCENAME(doc, iparamvalueE, className);
		for (int i = 0; i < keysV.size(); i++) {
			CIMProperty p = (CIMProperty) keysV.elementAt(i);
			String pName = p.getName();
			CIMValue pValue = p.getValue();
			String pValueStr = pValue.getValue().toString();
			String pValueTypeStr = CIMXMLBuilderImpl.getTypeStr(pValue.getType());

			Element keybindingE = CIMXMLBuilderImpl.createKEYBINDING(doc, instancenameE, pName);
			CIMXMLBuilderImpl.createKEYVALUE(doc, keybindingE, pValueTypeStr, pValueStr);
		}
		if (assocClass != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "AssocClass");
			CIMXMLBuilderImpl.createCLASSNAME(doc, iparamvalueE, assocClass);
		}
		if (resultClass != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ResultClass");
			CIMXMLBuilderImpl.createCLASSNAME(doc, iparamvalueE, resultClass);
		}
		if (role != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "Role");
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, role);
		}
		if (resultRole != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ResultRole");
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, resultRole);
		}

		return simplereqE;
	}

	public Element associators_request(Document doc, CIMObjectPath objectName, String assocClass,
			String resultClass, String role, String resultRole, boolean includeQualifiers,
			boolean includeClassOrigin, String[] propertyList) {

		// obtain data
		String className = objectName.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");
		Vector keysV = objectName.getKeys();

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE, "Associators");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, objectName);
		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ObjectName");
		Element instancenameE = CIMXMLBuilderImpl.createINSTANCENAME(doc, iparamvalueE, className);
		for (int i = 0; i < keysV.size(); i++) {
			CIMProperty p = (CIMProperty) keysV.elementAt(i);
			String pName = p.getName();
			CIMValue pValue = p.getValue();
			String pValueStr = pValue.getValue().toString();
			String pValueTypeStr = CIMXMLBuilderImpl.getTypeStr(pValue.getType());

			Element keybindingE = CIMXMLBuilderImpl.createKEYBINDING(doc, instancenameE, pName);
			CIMXMLBuilderImpl.createKEYVALUE(doc, keybindingE, pValueTypeStr, pValueStr);
		}
		if (assocClass != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "AssocClass");
			CIMXMLBuilderImpl.createCLASSNAME(doc, iparamvalueE, assocClass);
		}
		if (resultClass != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ResultClass");
			CIMXMLBuilderImpl.createCLASSNAME(doc, iparamvalueE, resultClass);
		}
		if (role != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "Role");
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, role);
		}
		if (resultRole != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ResultRole");
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, resultRole);
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeQualifiers");
		if (includeQualifiers) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeClassOrigin");
		if (includeClassOrigin) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		if (propertyList != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "PropertyList"); // BB
			// fixed
			Element valuearrayE = CIMXMLBuilderImpl.createVALUEARRAY(doc, iparamvalueE);
			for (int i = 0; i < propertyList.length; i++) {
				CIMXMLBuilderImpl.createVALUE(doc, valuearrayE, propertyList[i]);
			}
		}

		return simplereqE;
	}

	public Element enumerateInstanceNames_request(Document doc, CIMObjectPath path) {
		// obtain data
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE,
				"EnumerateInstanceNames");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ClassName");
		CIMXMLBuilderImpl.createCLASSNAME(doc, iparamvalueE, className);

		return simplereqE;
	}

	public Element enumerateInstances_request(Document doc, CIMObjectPath path,
			boolean deepInheritance, boolean localOnly, boolean includeQualifiers,
			boolean includeClassOrigin, String[] propertyList) {

		// obtain data
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE,
				"EnumerateInstances");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ClassName");
		CIMXMLBuilderImpl.createCLASSNAME(doc, iparamvalueE, className);
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "LocalOnly");
		if (localOnly) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "DeepInheritance");
		if (deepInheritance) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeQualifiers");
		if (includeQualifiers) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeClassOrigin");
		if (includeClassOrigin) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		if (propertyList != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "PropertyList");
			Element valuearrayE = CIMXMLBuilderImpl.createVALUEARRAY(doc, iparamvalueE);
			for (int i = 0; i < propertyList.length; i++) {
				CIMXMLBuilderImpl.createVALUE(doc, valuearrayE, propertyList[i]);
			}
		}
		return simplereqE;
	}

	public Element getInstance_request(Document doc, CIMObjectPath name, boolean localOnly,
			boolean includeQualifiers, boolean includeClassOrigin, String[] propertyList) {

		// obtain data
		String className = name.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE, "GetInstance");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, name);

		// TODO clean up this code
		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE,
				"InstanceName");
		CIMXMLBuilderImpl.createINSTANCENAME(doc, iparamvalueE, name);

		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "LocalOnly");
		if (localOnly) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeQualifiers");
		if (includeQualifiers) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeClassOrigin");
		if (includeClassOrigin) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		if (propertyList != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "PropertyList");
			Element valuearrayE = CIMXMLBuilderImpl.createVALUEARRAY(doc, iparamvalueE);
			for (int i = 0; i < propertyList.length; i++) {
				CIMXMLBuilderImpl.createVALUE(doc, valuearrayE, propertyList[i]);
			}
		}

		return simplereqE;
	}

	public Element deleteInstance_request(Document doc, CIMObjectPath name) {

		// obtain data
		String className = name.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE,
				"DeleteInstance");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, name);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE,
				"InstanceName");
		CIMXMLBuilderImpl.createINSTANCENAME(doc, iparamvalueE, name);

		return simplereqE;
	}

	public Element getClass_request(Document doc, CIMObjectPath name, boolean localOnly,
			boolean includeQualifiers, boolean includeClassOrigin, String[] propertyList) {

		// obtain data
		String className = name.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE, "GetClass");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, name);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ClassName");
		CIMXMLBuilderImpl.createCLASSNAME(doc, iparamvalueE, className);
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "LocalOnly");
		if (localOnly) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeQualifiers");
		if (includeQualifiers) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeClassOrigin");
		if (includeClassOrigin) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		if (propertyList != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "PropertyList");
			Element valuearrayE = CIMXMLBuilderImpl.createVALUEARRAY(doc, iparamvalueE);
			for (int i = 0; i < propertyList.length; i++) {
				CIMXMLBuilderImpl.createVALUE(doc, valuearrayE, propertyList[i]);
			}
		}

		return simplereqE;
	}

	public Element createInstance_request(Document doc, CIMObjectPath name, CIMInstance instance) {

		String className = instance.getObjectPath().getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE,
				"CreateInstance");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, name);
		Element iparamvalueE = CIMXMLBuilderImpl
				.createIPARAMVALUE(doc, imethodcallE, "NewInstance");

		CIMXMLBuilderImpl.createINSTANCE(doc, iparamvalueE, instance);

		return simplereqE;
	}

	public Element invokeMethod_request(Document doc, CIMObjectPath name, String methodName,
			Vector inArgs) {

		// obtain data
		String className = name.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");
		Vector keysV = name.getKeys();

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element methodcallE = CIMXMLBuilderImpl.createMETHODCALL(doc, simplereqE, methodName, null);

		// 13521
		Element localpathE = null;
		if (keysV.size() > 0) {
			localpathE = CIMXMLBuilderImpl.createLOCALINSTANCEPATH(doc, methodcallE);
			CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, localpathE, name); // 13521
			CIMXMLBuilderImpl.createINSTANCENAME(doc, localpathE, name); // 13521
		} else {
			localpathE = CIMXMLBuilderImpl.createLOCALCLASSPATH(doc, methodcallE, name);
		}

		// TODO clean up this code
		if (inArgs != null) {
			for (int i = 0; i < inArgs.size(); i++) {
				CIMArgument arg = (CIMArgument) inArgs.elementAt(i);
				if (arg == null) continue;

				CIMValue argValue = arg.getValue();
				CIMDataType argType = arg.getType();
				String argName = arg.getName();

				if (argValue != null && argValue.isArrayValue()) { // BB fix
					if (argType.getType() == CIMDataType.REFERENCE_ARRAY) {
						Element valuerefarrayParamvalueE = CIMXMLBuilderImpl.createPARAMVALUE(doc,
								methodcallE, argName, null);
						// valuerefarrayParamvalueE.setAttribute("PARAMTYPE",
						// "");
						Vector valueV = (Vector) argValue.getValue();
						if (valueV != null) {
							Element valuerefarrayE = CIMXMLBuilderImpl.createVALUEREFARRAY(doc,
									valuerefarrayParamvalueE);
							for (int j = 0; j < valueV.size(); j++) {
								CIMObjectPath refOP = (CIMObjectPath) valueV.elementAt(j);

								CIMXMLBuilderImpl.createVALUEREFERENCE(doc, valuerefarrayE, refOP);
							}
						}
					} else {
						Element valuearrayParamvalueE = CIMXMLBuilderImpl.createPARAMVALUE(doc,
								methodcallE, argName, null);
						String paramtypeStr = CIMXMLBuilderImpl.getTypeStr(argType);
						valuearrayParamvalueE.setAttribute("PARAMTYPE", paramtypeStr);
						Vector valueV = (Vector) argValue.getValue();
						if (valueV != null) {
							Element valuearrayE = CIMXMLBuilderImpl.createVALUEARRAY(doc,
									valuearrayParamvalueE);
							for (int j = 0; j < valueV.size(); j++) {
								CIMXMLBuilderImpl.createVALUE(doc, valuearrayE, valueV.elementAt(j)
										.toString());
							}
						}
					}
				} else {
					if (argType != null && argType.getType() == CIMDataType.REFERENCE) { // BB
						// fixed
						CIMObjectPath refOP = (CIMObjectPath) argValue.getValue();

						Element refParamvalueE = CIMXMLBuilderImpl.createPARAMVALUE(doc,
								methodcallE, argName, null);
						refParamvalueE.setAttribute("PARAMTYPE", "reference");

						if (refOP != null) {
							String refClassName = refOP.getObjectName();
							Vector refKeysV = refOP.getKeys();

							Element refValuereferenceE = CIMXMLBuilderImpl.createVALUEREFERENCE(
									doc, refParamvalueE);
							Element refLocalinstancepathE = CIMXMLBuilderImpl
									.createLOCALINSTANCEPATH(doc, refValuereferenceE);
							CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, refLocalinstancepathE,
									name);

							Element refInstancenameE = CIMXMLBuilderImpl.createINSTANCENAME(doc,
									refLocalinstancepathE, refClassName);
							for (int j = 0; j < refKeysV.size(); j++) {
								CIMProperty p = (CIMProperty) refKeysV.elementAt(j);
								String pName = p.getName();
								CIMValue propertyValue = p.getValue();
								if (propertyValue.getValue() != null) {
									// ebak: 1732645 - reference types requires different treating
									CIMDataType propType = propertyValue.getType();
									Element refKeybindingE =
										CIMXMLBuilderImpl.createKEYBINDING(
											doc, refInstancenameE, pName
										);
									if (propType.getType()==CIMDataType.REFERENCE) {
										CIMXMLBuilderImpl.createVALUE(
											doc, refKeybindingE, propertyValue
										);
									} else {
										String propertyValueStr = propertyValue.getValue().toString();
										String propertyValueTypeStr = CIMXMLBuilderImpl
												.getTypeStr(propertyValue.getType());
										CIMXMLBuilderImpl.createKEYVALUE(doc, refKeybindingE,
												propertyValueTypeStr, propertyValueStr);
									}
								}
							}
						}
					} else {
						Element paramvalueE = CIMXMLBuilderImpl.createPARAMVALUE(doc, methodcallE,
								argName, null);
						// BB begin
						Object v = null;
						v = (argValue != null) ? argValue.getValue() : null;
						if (v != null) {
							String paramtypeStr = CIMXMLBuilderImpl.getTypeStr(argType);
							paramvalueE.setAttribute("PARAMTYPE", paramtypeStr);
							CIMXMLBuilderImpl.createVALUE(doc, paramvalueE, v.toString());
						}// BB end
					}
				}
			}
		}

		return simplereqE;
	}

	// public CIMResponse createIndication_request(Document doc) throws
	// CIMXMLParseException, CIMException {
	// Element rootE = doc.getDocumentElement();
	// CIMResponse response = (CIMResponse)xmlParser.parseCIM(rootE);
	// response.checkError();
	// return response;
	// // Vector v = (Vector)response.getFirstReturnValue();
	// //
	// // //TODO: Should we return the whole list of instances or just the first
	// instance?
	// // //TODO: return the whole vector of indications
	// // if (v.size() > 0)
	// // return (CIMInstance)v.elementAt(0);
	// // else
	// // return null;
	// }

	public Document createIndication_response(CIMError error) {

		// CIMXMLBuilderImpl.create XML
		Document doc = iBuilder.newDocument();
		Element cimE = CIMXMLBuilderImpl.createCIM(doc);
		Element messageE = CIMXMLBuilderImpl.createMESSAGE(doc, cimE, String
				.valueOf(iOutputMsgCount++), "1.0");
		Element simpleexprspE = CIMXMLBuilderImpl.createSIMPLEEXPRSP(doc, messageE);
		Element expmethodresponseE = CIMXMLBuilderImpl.createEXPMETHODRESPONSE(doc, simpleexprspE,
				"ExportIndication");
		if (error == null) {
			CIMXMLBuilderImpl.createIRETURNVALUE(doc, expmethodresponseE);
		} else {
			CIMXMLBuilderImpl.createERROR(doc, expmethodresponseE, error);
		}
		// Element
		return doc;
	}

	public Element createClass_request(Document doc, CIMObjectPath path, CIMClass cc) {
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE, "CreateClass");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "NewClass");

		CIMXMLBuilderImpl.createCLASS(doc, iparamvalueE, cc);

		return simplereqE;
	}

	public Element getQualifier_request(Document doc, CIMObjectPath path, String qt) {
		// obtain data
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE, "GetQualifier");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE,
				"QualifierName");
		CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, path.getObjectName());

		return simplereqE;
	}

	public Element createQualifierType_request(Document doc, CIMObjectPath path, CIMQualifierType qt) {
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");
		// TODO Proposal: There is no need to require the path to be a class
		// path for this operation. Remove the above two lines.

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE,
				"SetQualifier"); // ebak: 1365086 Possible bug in createQualifier
		// TODO Bug: As per DSP0200 and DSP0201, the name of the IMETHODCALL for
		// this must be "SetQualifier". Suggest to test this before changing.
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE,
				"QualifierDeclaration");
		CIMXMLBuilderImpl.createQUALIFIER_DECLARATION(doc, iparamvalueE, qt);

		return simplereqE;
	}

	public Element deleteClass_request(Document doc, CIMObjectPath path) {
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE, "DeleteClass");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ClassName");

		CIMXMLBuilderImpl.createOBJECTNAME(doc, iparamvalueE, path);

		return simplereqE;
	}

	public Element deleteQualifierType_request(Document doc, CIMObjectPath path) {
		// obtain data
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE,
				"DeleteQualifier");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE,
				"QualifierName");
		CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, path.getObjectName());
		return simplereqE;
	}

	public Element enumerateClasses_request(Document doc, CIMObjectPath path,
			boolean deepInheritance, boolean localOnly, boolean includeQualifiers,
			boolean includeClassOrigin) {

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE,
				"EnumerateClasses");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE;
		if (path != null && path.getObjectName() != null
				&& path.getObjectName().trim().length() != 0) {
			String className = path.getObjectName();
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ClassName");
			CIMXMLBuilderImpl.createCLASSNAME(doc, iparamvalueE, className);
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "LocalOnly");
		if (localOnly) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "DeepInheritance");
		if (deepInheritance) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}

		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeQualifiers");
		if (includeQualifiers) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeClassOrigin");
		if (includeClassOrigin) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		return simplereqE;
	}

	public Element enumerateClassNames_request(Document doc, CIMObjectPath path,
			boolean deepInheritance) {

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE,
				"EnumerateClassNames");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE;

		if (path != null && path.getObjectName() != null
				&& path.getObjectName().trim().length() != 0) {
			String className = path.getObjectName();
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ClassName");
			CIMXMLBuilderImpl.createCLASSNAME(doc, iparamvalueE, className);
		}

		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "DeepInheritance");
		if (deepInheritance) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}

		return simplereqE;
	}

	public Element getProperty_request(Document doc, CIMObjectPath path, String propertyName) {
		// obtain data
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE, "GetProperty");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE,
				"InstanceName");
		CIMXMLBuilderImpl.createINSTANCENAME(doc, iparamvalueE, path);

		if (propertyName != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "PropertyName");
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, propertyName);
		}

		return simplereqE;
	}

	public Element referenceNames_request(Document doc, CIMObjectPath path, String resultClass,
			String role) {
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE,
				"ReferenceNames");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ObjectName");
		CIMXMLBuilderImpl.createOBJECTNAME(doc, iparamvalueE, path);

		if (resultClass != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ResultClass");
			CIMXMLBuilderImpl.createCLASSNAME(doc, iparamvalueE, resultClass);
		}
		if (role != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "Role");
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, role);
		}

		return simplereqE;
	}

	public Element references_request(Document doc,

	CIMObjectPath path, String resultClass, String role, boolean includeQualifiers,
			boolean includeClassOrigin, String[] propertyList) {
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE, "References");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE;

		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ObjectName");
		CIMXMLBuilderImpl.createOBJECTNAME(doc, iparamvalueE, path);

		if (resultClass != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "ResultClass");
			CIMXMLBuilderImpl.createCLASSNAME(doc, iparamvalueE, resultClass);
		}
		if (role != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "Role");
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, role);
		}

		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeQualifiers");
		if (includeQualifiers) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeClassOrigin");
		if (includeClassOrigin) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		if (propertyList != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "PropertyList");
			Element valuearrayE = CIMXMLBuilderImpl.createVALUEARRAY(doc, iparamvalueE);
			for (int i = 0; i < propertyList.length; i++) {
				CIMXMLBuilderImpl.createVALUE(doc, valuearrayE, propertyList[i]);
			}
		}

		return simplereqE;
	}

	public Element setClass_request(Document doc, CIMObjectPath path, CIMClass cc) {
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE, "ModifyClass");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE,
				"ModifiedClass");
		CIMXMLBuilderImpl.createCLASS(doc, iparamvalueE, cc);

		return simplereqE;
	}

	public Element setInstance_request(Document doc, CIMObjectPath path, CIMInstance ci,
			boolean includeQualifiers, String[] propertyList) {
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE,
				"ModifyInstance");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE,
				"ModifiedInstance");
		CIMXMLBuilderImpl.createVALUENAMEDINSTANCE(doc, iparamvalueE, path, ci);

		iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "IncludeQualifiers");
		if (includeQualifiers) {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "true");
		} else {
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, "false");
		}
		if (propertyList != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "PropertyList");
			Element valuearrayE = CIMXMLBuilderImpl.createVALUEARRAY(doc, iparamvalueE);
			for (int i = 0; i < propertyList.length; i++) {
				CIMXMLBuilderImpl.createVALUE(doc, valuearrayE, propertyList[i]);
			}
		}

		return simplereqE;
	}

	public Element setProperty_request(Document doc, CIMObjectPath path, String propertyName,
			CIMValue newValue) {
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE, "SetProperty");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE,
				"InstanceName");
		CIMXMLBuilderImpl.createINSTANCENAME(doc, iparamvalueE, path);

		if (propertyName != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "PropertyName");
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, propertyName);
		}

		if (newValue != null) {
			iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "NewValue");
			CIMXMLBuilderImpl.createVALUE(doc, iparamvalueE, newValue);
		}

		return simplereqE;
	}

	public Element setQualifierType_request(Document doc, CIMObjectPath path, CIMQualifierType qt) {
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");
		// TODO Proposal: There is no need to require the path to be a class
		// path for this operation. Remove the above two lines.

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE, "SetQualifier");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element iparamvalueE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE,
				"QualifierDeclaration");
		CIMXMLBuilderImpl.createQUALIFIER_DECLARATION(doc, iparamvalueE, qt);

		return simplereqE;
	}

	public Element enumQualifierTypes_request(Document doc, CIMObjectPath path) {
		String className = path.getObjectName();
		if (className == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null class name");
		// TODO Proposal: There is no need to require the path to be a class
		// path for this operation. Remove the above two lines.

		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE,
				"EnumerateQualifiers");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		return simplereqE;
	}

	public Element execQuery_request(Document doc, CIMObjectPath path, String query,
			String queryLanguage) {
		Element simplereqE = CIMXMLBuilderImpl.createSIMPLEREQ(doc);
		Element imethodcallE = CIMXMLBuilderImpl.createIMETHODCALL(doc, simplereqE, "ExecQuery");
		CIMXMLBuilderImpl.createLOCALNAMESPACEPATH(doc, imethodcallE, path);

		Element querylanguageE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE,
				"QueryLanguage");
		CIMXMLBuilderImpl.createVALUE(doc, querylanguageE, queryLanguage);

		Element queryE = CIMXMLBuilderImpl.createIPARAMVALUE(doc, imethodcallE, "Query");
		CIMXMLBuilderImpl.createVALUE(doc, queryE, query);

		return simplereqE;
	}

	public Element performBatchOperation_request(Document doc, Vector operations)
			throws CIMException {

		Element messageE = createCIMMessage(doc, null);
		if (operations.size() > 1) {
			Element multireqE = createMultiReq(doc);
			messageE.appendChild(multireqE);
			messageE = multireqE;
		}
		int i = 0;
		Iterator iter = operations.iterator();
		while (iter.hasNext()) {
			CIMOperation op = (CIMOperation) iter.next();
			try {
				Element requestE = null;

				if (op instanceof CIMAssociatorsOp) {
					CIMAssociatorsOp associatorsOp = (CIMAssociatorsOp) op;
					requestE = associators_request(doc, associatorsOp.getObjectName(),
							associatorsOp.getAssocClass(), associatorsOp.getResultClass(),
							associatorsOp.getRole(), associatorsOp.getResultRole(), associatorsOp
									.isIncludeQualifiers(), associatorsOp.isIncludeClassOrigin(),
							associatorsOp.getPropertyList());
				} else if (op instanceof CIMAssociatorNamesOp) {
					CIMAssociatorNamesOp associatorNamesOp = (CIMAssociatorNamesOp) op;
					requestE = associatorNames_request(doc, associatorNamesOp.getObjectName(),
							associatorNamesOp.getAssocClass(), associatorNamesOp.getResultClass(),
							associatorNamesOp.getRole(), associatorNamesOp.getResultRole());
				} else if (op instanceof CIMCreateClassOp) {
					CIMCreateClassOp createClassOp = (CIMCreateClassOp) op;
					requestE = createClass_request(doc, createClassOp.getObjectName(),
							createClassOp.getCimClass());
				} else if (op instanceof CIMCreateInstanceOp) {
					CIMCreateInstanceOp createInstanceOp = (CIMCreateInstanceOp) op;
					requestE = createInstance_request(doc, createInstanceOp.getObjectName(),
							createInstanceOp.getInstance());
				} else if (op instanceof CIMCreateNameSpaceOp) {
					CIMCreateNameSpaceOp createNameSpaceOp = (CIMCreateNameSpaceOp) op;

					String namespace = createNameSpaceOp.getNameSpace().getNameSpace();
					int j = namespace.lastIndexOf('/');

					if (j < 0) throw new CIMException(CIMException.CIM_ERR_NOT_FOUND,
							"Invalid namespace. Must contain at least /");
					String parentNs = namespace.substring(0, j);
					namespace = namespace.substring(j + 1);

					CIMInstance inst = new CIMInstance();
					inst.setClassName("CIM_NameSpace");
					CIMProperty prop = new CIMProperty("NameSpace");
					prop.setValue(new CIMValue(namespace, CIMDataType
							.getPredefinedType(CIMDataType.STRING)));
					Vector v = new Vector();
					v.add(prop);
					inst.setProperties(v);
					CIMObjectPath object = new CIMObjectPath(null, parentNs);

					requestE = createInstance_request(doc, object, inst);
				} else if (op instanceof CIMCreateQualifierTypeOp) {
					CIMCreateQualifierTypeOp createQualifierTypeOp = (CIMCreateQualifierTypeOp) op;
					requestE = createQualifierType_request(doc, createQualifierTypeOp
							.getObjectName(), createQualifierTypeOp.getQualifierType());
				} else if (op instanceof CIMDeleteClassOp) {
					CIMDeleteClassOp deleteClassOp = (CIMDeleteClassOp) op;
					requestE = deleteClass_request(doc, deleteClassOp.getObjectName());
				} else if (op instanceof CIMDeleteInstanceOp) {
					CIMDeleteInstanceOp deleteInstanceOp = (CIMDeleteInstanceOp) op;
					requestE = deleteClass_request(doc, deleteInstanceOp.getObjectName());
				} else if (op instanceof CIMDeleteQualifierTypeOp) {
					CIMDeleteQualifierTypeOp deleteQualifierTypeOp = (CIMDeleteQualifierTypeOp) op;
					requestE = deleteClass_request(doc, deleteQualifierTypeOp.getObjectName());
				} else if (op instanceof CIMEnumClassesOp) {
					CIMEnumClassesOp enumClassesOp = (CIMEnumClassesOp) op;
					requestE = enumerateClasses_request(doc, enumClassesOp.getObjectName(),
							enumClassesOp.isDeep(), enumClassesOp.isLocalOnly(), enumClassesOp
									.isIncludeQualifiers(), enumClassesOp.isIncludeClassOrigin());
				} else if (op instanceof CIMEnumClassNamesOp) {
					CIMEnumClassNamesOp enumClassNamesOp = (CIMEnumClassNamesOp) op;
					requestE = enumerateClassNames_request(doc, enumClassNamesOp.getObjectName(),
							enumClassNamesOp.isDeep());
				} else if (op instanceof CIMEnumInstanceNamesOp) {
					CIMEnumInstanceNamesOp enumInstanceNamesOp = (CIMEnumInstanceNamesOp) op;
					requestE = enumerateInstanceNames_request(doc, enumInstanceNamesOp
							.getObjectName());
				} else if (op instanceof CIMEnumInstancesOp) {
					CIMEnumInstancesOp enumInstancesOp = (CIMEnumInstancesOp) op;
					requestE = enumerateInstances_request(doc, enumInstancesOp.getObjectName(),
							enumInstancesOp.isDeep(), enumInstancesOp.isLocalOnly(),
							enumInstancesOp.isIncludeQualifiers(), enumInstancesOp
									.isIncludeClassOrigin(), enumInstancesOp.getPropertyList());
				} else if (op instanceof CIMEnumNameSpaceOp) {
					CIMEnumNameSpaceOp enumNameSpaceOp = (CIMEnumNameSpaceOp) op;
					enumNameSpaceOp.getObjectName().setObjectName("CIM_NameSpace");

					requestE = enumerateInstanceNames_request(doc, enumNameSpaceOp.getObjectName());
				} else if (op instanceof CIMEnumQualifierTypesOp) {
					CIMEnumQualifierTypesOp enumQualifierTypesOp = (CIMEnumQualifierTypesOp) op;
					requestE = enumQualifierTypes_request(doc, enumQualifierTypesOp.getObjectName());
				} else if (op instanceof CIMExecQueryOp) {
					CIMExecQueryOp execQueryOp = (CIMExecQueryOp) op;
					requestE = execQuery_request(doc, execQueryOp.getObjectName(), execQueryOp
							.getQuery(), execQueryOp.getQueryLanguage());
				} else if (op instanceof CIMGetPropertyOp) {
					CIMGetPropertyOp getPropertyOp = (CIMGetPropertyOp) op;
					requestE = getInstance_request(doc, getPropertyOp.getObjectName(), false,
							false, false, new String[] { getPropertyOp.getPropertyName() });

				} else if (op instanceof CIMGetClassOp) {
					CIMGetClassOp getClassOp = (CIMGetClassOp) op;
					requestE = getClass_request(doc, getClassOp.getObjectName(), getClassOp
							.isLocalOnly(), getClassOp.isIncludeQualifiers(), getClassOp
							.isIncludeClassOrigin(), getClassOp.getPropertyList());
				} else if (op instanceof CIMGetInstanceOp) {
					CIMGetInstanceOp getInstanceOp = (CIMGetInstanceOp) op;
					requestE = getInstance_request(doc, getInstanceOp.getObjectName(),
							getInstanceOp.isLocalOnly(), getInstanceOp.isIncludeQualifiers(),
							getInstanceOp.isIncludeClassOrigin(), getInstanceOp.getPropertyList());
				} else if (op instanceof CIMGetQualifierTypeOp) {
					CIMGetQualifierTypeOp getQualifierTypeOp = (CIMGetQualifierTypeOp) op;
					requestE = getQualifier_request(doc, getQualifierTypeOp.getObjectName(),
							getQualifierTypeOp.getQualifierType());
				} else if (op instanceof CIMInvokeMethodOp) {
					CIMInvokeMethodOp invokeMethodOp = (CIMInvokeMethodOp) op;
					requestE = invokeMethod_request(doc, invokeMethodOp.getObjectName(),
							invokeMethodOp.getMethodCall(), invokeMethodOp.getInParams());
				} else if (op instanceof CIMReferenceNamesOp) {
					CIMReferenceNamesOp referenceNamesOp = (CIMReferenceNamesOp) op;
					requestE = referenceNames_request(doc, referenceNamesOp.getObjectName(),
							referenceNamesOp.getResultClass(), referenceNamesOp.getResultRole());
				} else if (op instanceof CIMReferencesOp) {
					CIMReferencesOp referencesOp = (CIMReferencesOp) op;
					requestE = references_request(doc, referencesOp.getObjectName(), referencesOp
							.getResultClass(), referencesOp.getRole(), referencesOp
							.isIncludeQualifiers(), referencesOp.isIncludeClassOrigin(),
							referencesOp.getPropertyList());
				} else if (op instanceof CIMSetClassOp) {
					CIMSetClassOp setClassOp = (CIMSetClassOp) op;
					requestE = setClass_request(doc, setClassOp.getObjectName(), setClassOp
							.getCimClass());
				} else if (op instanceof CIMSetInstanceOp) {
					CIMSetInstanceOp setInstanceOp = (CIMSetInstanceOp) op;
					requestE = setInstance_request(doc, setInstanceOp.getObjectName(),
							setInstanceOp.getInstance(), setInstanceOp.isIncludeQualifiers(),
							setInstanceOp.getPropertyList());
				} else if (op instanceof CIMSetPropertyOp) {
					CIMSetPropertyOp setPropertyOp = (CIMSetPropertyOp) op;
					requestE = setProperty_request(doc, setPropertyOp.getObjectName(),
							setPropertyOp.getPropertyName(), setPropertyOp.getCimValue());
				} else if (op instanceof CIMSetQualifierTypeOp) {
					CIMSetQualifierTypeOp setQualifierTypeOp = (CIMSetQualifierTypeOp) op;
					requestE = setQualifierType_request(doc, setQualifierTypeOp.getObjectName(),
							setQualifierTypeOp.getQualifierType());
				}
				if (requestE == null) throw new CIMException(
						CIMException.CIM_ERR_INVALID_PARAMETER, "Illegal batch operation number ("
								+ i + ") " + op.getClass());
				messageE.appendChild(requestE);
			} catch (CIMException e) {
				throw e;
			} catch (Exception e) {
				throw new CIMException(CIMException.CIM_ERR_FAILED, "At batch operation (" + i
						+ ')', e);
			}
			i++;
		}
		return messageE;
	}
}
