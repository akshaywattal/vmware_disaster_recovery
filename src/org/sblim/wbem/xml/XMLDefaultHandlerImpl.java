/**
 * XMLDefaultHandlerImpl.java
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
 * Flag    Date         Prog         Description
 *------------------------------------------------------------------------------- 
 * 1464860 2006-05-15   lupusalex    No default value for VALUETYPE assumed
 * 1493639 2006-05-29   fiuczy       XML Parsing of PARAMETER.ARRAY wrong
 * 1528233 2006-07-25   lupusalex    Interoperability with Engenio CIMOM broken
 * 1535756 2006-08-07   lupusalex    Make code warning free
 * 1657901 2007-02-15   ebak         Performance issues
 * 1705776 2007-04-24   ebak         Regression: parseBigInteger() doesn't like zero
 * 1804532 2007-11-06   ebak         trace for both req/res should be traced in the same file
 * 2219646 2008-11-17   blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325 2009-06-17   blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.xml;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sblim.wbem.cim.CIMArgument;
import org.sblim.wbem.cim.CIMClass;
import org.sblim.wbem.cim.CIMDataType;
import org.sblim.wbem.cim.CIMDateTime;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMFlavor;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMMethod;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.cim.CIMParameter;
import org.sblim.wbem.cim.CIMProperty;
import org.sblim.wbem.cim.CIMQualifiableElement;
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
import org.sblim.wbem.xml.parser.XMLPullParser;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLDefaultHandlerImpl extends DefaultHandler {

	protected final static int CIM = "CIM".hashCode();

	protected final static int CLASS = "CLASS".hashCode();

	protected final static int CLASSNAME = "CLASSNAME".hashCode();

	protected final static int CLASSPATH = "CLASSPATH".hashCode();

	protected final static String EMPTY = "";

	protected final static int ERROR = "ERROR".hashCode();

	protected final static int EXPMETHODCALL = "EXPMETHODCALL".hashCode();

	protected final static int EXPPARAMVALUE = "EXPPARAMVALUE".hashCode();

	protected final static int HOST = "HOST".hashCode();

	protected final static int IMETHODRESPONSE = "IMETHODRESPONSE".hashCode();

	protected final static int INSTANCE = "INSTANCE".hashCode();

	protected final static int INSTANCENAME = "INSTANCENAME".hashCode();

	protected final static int INSTANCEPATH = "INSTANCEPATH".hashCode();

	protected final static int IPARAMVALUE = "IPARAMVALUE".hashCode();

	protected final static int IRETURNVALUE = "IRETURNVALUE".hashCode();

	protected final static int KEYBINDING = "KEYBINDING".hashCode();

	protected final static int KEYVALUE = "KEYVALUE".hashCode();

	protected final static int LOCALCLASSPATH = "LOCALCLASSPATH".hashCode();

	protected final static int LOCALINSTANCEPATH = "LOCALINSTANCEPATH".hashCode();

	protected final static int LOCALNAMESPACEPATH = "LOCALNAMESPACEPATH".hashCode();

	protected final static int MESSAGE = "MESSAGE".hashCode();

	protected final static int METHOD = "METHOD".hashCode();

	protected final static int METHODRESPONSE = "METHODRESPONSE".hashCode();

	protected final static int MULTIEXPREQ = "MULTIEXPREQ".hashCode();

	protected final static int MULTIEXPRSP = "MULTIEXPRSP".hashCode();

	protected final static int MULTIREQ = "MULTIREQ".hashCode();

	protected final static int MULTIRSP = "MULTIRSP".hashCode();

	protected final static int NAMESPACE = "NAMESPACE".hashCode();

	protected final static int NAMESPACEPATH = "NAMESPACEPATH".hashCode();

	protected final static int OBJECTPATH = "OBJECTPATH".hashCode();

	protected final static int PARAMETER = "PARAMETER".hashCode();

	protected final static int PARAMETER_ARRAY = "PARAMETER.ARRAY".hashCode();

	protected final static int PARAMETER_REFARRAY = "PARAMETER.REFARRAY".hashCode();

	protected final static int PARAMETER_REFERENCE = "PARAMETER.REFERENCE".hashCode();

	protected final static int PARAMVALUE = "PARAMVALUE".hashCode();

	protected final static int PROPERTY = "PROPERTY".hashCode();

	protected final static int PROPERTY_ARRAY = "PROPERTY.ARRAY".hashCode();

	protected final static int PROPERTY_REFERENCE = "PROPERTY.REFERENCE".hashCode();

	protected final static int QUALIFIER = "QUALIFIER".hashCode();

	protected final static int QUALIFIER_DECLARATION = "QUALIFIER.DECLARATION".hashCode();

	protected final static int RETURNVALUE = "RETURNVALUE".hashCode();

	protected final static int SCOPE = "SCOPE".hashCode();

	protected final static int SIMPLEEXPREQ = "SIMPLEEXPREQ".hashCode();

	protected final static int SIMPLEEXPRSP = "SIMPLEEXPRSP".hashCode();

	protected final static int SIMPLERSP = "SIMPLERSP".hashCode();

	protected final static int SIMPLREQ = "SIMPLREQ".hashCode();

	protected final static int VALUE = "VALUE".hashCode();

	protected final static int VALUE_ARRAY = "VALUE.ARRAY".hashCode();

	protected final static int VALUE_NAMEDINSTANCE = "VALUE.NAMEDINSTANCE".hashCode();

	protected final static int VALUE_NAMEDOBJECT = "VALUE_NAMEDOBJECT".hashCode();

	protected final static int VALUE_OBJECT = "VALUE.OBJECT".hashCode();

	protected final static int VALUE_OBJECTWITHLOCALPATH = "VALUE.OBJECTWITHLOCALPATH".hashCode();

	protected final static int VALUE_OBJECTWITHPATH = "VALUE.OBJECTWITHPATH".hashCode();

	protected final static int VALUE_REFARRAY = "VALUE.REFARRAY".hashCode();

	protected final static int VALUE_REFERENCE = "VALUE.REFERENCE".hashCode();

	private static final String FALSE = "false";

	private static Writer cOut = new PrintWriter(SessionProperties.getGlobalProperties().getDebugOutputStream());

	private static final String TRUE = "true";

	public static File[] listAllXml(File baseDir) {
		Vector result = new Vector();

		File[] allXmlFiles = baseDir.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				if (pathname.isFile() && pathname.getAbsolutePath().endsWith(".xml")
						&& pathname.getName().startsWith("Resp")) return true;
				return false;
			}
		});
		File[] allDirectories = baseDir.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				if (pathname.isDirectory()) return true;
				return false;
			}
		});
		result.addAll(Arrays.asList(allXmlFiles));
		for (int i = 0; i < allDirectories.length; i++) {
			File[] subFiles = listAllXml(allDirectories[i]);
			result.addAll(Arrays.asList(subFiles));
		}
		return (File[]) result.toArray(new File[result.size()]);
	}

	public static void main(String[] argv) {
		// Use an instance of ourselves as the SAX event handler
		XMLDefaultHandlerImpl handler = new XMLDefaultHandlerImpl();
		// Use the default (non-validating) parser
		SAXParserFactory factory = SAXParserFactory.newInstance();
		// Set up output stream
		try {
			cOut = new OutputStreamWriter(System.out, "UTF8");

			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			File[] xmlFiles = listAllXml(new File("c:\\code\\cimxml\\"));
			for (int i = 212; i < xmlFiles.length; i++) {
				System.out.println(i + " - " + xmlFiles[i].getCanonicalPath());
				try {
					handler.iDebug = false;
					saxParser.parse(xmlFiles[i], handler);

					CIMResponse response = (CIMResponse) handler.iObjects.elementAt(0);

					System.out.println("done\n" + response.getFirstReturnValue());
					handler.iObjects.removeAllElements();
				} catch (Throwable t) {
					try {
						handler.iDebug = true;
						saxParser.parse(xmlFiles[i], handler);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}

					t.printStackTrace();
				}
			}
			// saxParser.parse( new
			// File("c:\\code\\cimxml\\brocade\\Response_EnumerateInstances_Brocade_ZoneMembershipSettingDataInZoneAlias.xml"),
			// handler);
			// saxParser.parse( new
			// File("c:\\code\\cimxml\\brocade\\Response_EnumerateInstances_Brocade_Zone.xml"),
			// handler);
			// saxParser.parse( new
			// File("c:\\code\\cimxml\\brocade\\Response_EnumerateInstances_Brocade_SwitchHostedSANAccessPoint.xml"),
			// handler);

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	boolean iDebug = true;

	Vector iElementNames = new Vector();

	int iNn = 0;

	Vector iObjects = new Vector();

	Vector iTmpValues = new Vector();

	StringBuffer iValue = null;

	private int iIndentLevel = 0;

	private String iIndentString = "    "; // Amount to indent

	public XMLDefaultHandlerImpl() {
		iDebug = SessionProperties.getGlobalProperties().isDebugXMLInput();
	}

	public XMLDefaultHandlerImpl(boolean debug) {
		this.iDebug = debug;
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		String value = new String(ch, start, length);
		if (iDebug) {
			// nl();
			// emit("CHARS: ");

			// if (!value.trim().equals(EMPTY))
			emit(value);
			flush();
			// nl();
		}

		String eName = (String) iElementNames.lastElement();
		int tag = eName.hashCode();

		if (tag == KEYVALUE || tag == VALUE || tag == HOST) {
			if (this.iValue == null) this.iValue = new StringBuffer(value);
			else this.iValue.append(value);
		}
	}

	private static final Pattern BIN_PAT = Pattern.compile("^((?:\\+|-)?[01]+)[bB]$");
	/*
	 * ebak:  #1705776 Regression: parseBigInteger() doesn't like zero
	 * Allowing Octal pattern to match zeros seems to be the easiest change.
	 */
	private static final Pattern OCT_PAT = Pattern.compile("^(?:\\+|-)?0[0-7]*$");
	private static final Pattern DEC_PAT = Pattern.compile("^(?:\\+|-)?[1-9][0-9]*$");
	private static final Pattern HEX_PAT = Pattern.compile("^(\\+|-)?0[xX]([0-9a-fA-F]+)$");
	/*
	 * <pre>
	 * binaryValue		=	[ "+" | "-" ] 1*binaryDigit ( "b" | "B" )
	 * octalValue		=	[ "+" | "-" ] "0" 1*octalDigit
	 * decimalValue	=	[ "+" | "-" ] ( positiveDecimalDigit *decimalDigit | "0" )
	 * hexValue		=	[ "+" | "-" ] ( "0x" | "0X" ) 1*hexDigit
	 * </pre>
	 */
	private static BigInteger parseBigInteger(String pNumStr) {
		Matcher m = DEC_PAT.matcher(pNumStr);
		if (m.matches()) return new BigInteger(pNumStr);
		m = HEX_PAT.matcher(pNumStr);
		if (m.matches()) {
			String signStr = m.group(1);
			String hexDigits = m.group(2);
			String hexNumStr = signStr == null ? hexDigits : signStr + hexDigits;
			return new BigInteger(hexNumStr, 16);
		}
		m = OCT_PAT.matcher(pNumStr);
		if (m.matches()) return new BigInteger(pNumStr, 8);
		m = BIN_PAT.matcher(pNumStr);
		if (m.matches()) {
			String binDigits=m.group(1);
			return new BigInteger(binDigits, 2);
		}
		throw new NumberFormatException("'"+pNumStr+"' is a not handled number format!");
	}
	
	public Object createJavaObject(String typeStr, String value) {
		// return a java object with the specific type
		//typeStr=typeStr.toLowerCase();
		CIMDataType cimType = CIMDataType.getDataType(typeStr, false);
		Object o;
//		try {
		switch (cimType.getType()) {
			case CIMDataType.UINT8 :
				o = new UnsignedInt8(Short.decode(value).shortValue());
				break;
			case CIMDataType.UINT16 :
				o = new UnsignedInt16(Integer.decode(value).intValue());
				break;
			case CIMDataType.UINT32 :
				o = new UnsignedInt32(Long.decode(value).longValue());
				break;
			case CIMDataType.UINT64 :
				o = new UnsignedInt64(parseBigInteger(value));
				break;
			case CIMDataType.SINT8 :
				o = Byte.decode(value);
				break;
			case CIMDataType.SINT16 :
				o = Short.decode(value);
				break;
			case CIMDataType.SINT32 :
				o = Integer.decode(value);
				break;
			case CIMDataType.SINT64 :
				o = Long.decode(value);
				break;
			case CIMDataType.STRING :
				o = value;
				break;
			case CIMDataType.BOOLEAN :
				o = new Boolean(value);
				break;
			case CIMDataType.REAL32 :
				o = new Float(value);
				break;
			case CIMDataType.REAL64 :
				o = new Double(value);
				break;
			case CIMDataType.DATETIME :
				o = CIMDateTime.valueOf(value);
				break;
			case CIMDataType.REFERENCE :
				o = new CIMObjectPath(value);
				break;
			case CIMDataType.CHAR16 :
				o = new Character(value.charAt(0));
				break;
			case CIMDataType.NUMERIC :
				o = new Numeric(parseBigInteger(value).toString());
				break;
				//			case CIMDataType.OBJECT: o = new CIMInstance(); break;				//TODO
				//			case CIMDataType.CLASS: o = new CIMClass(value); break;				//TODO
			default:
				o = null;
		}
		return o;
//		}
//		catch (RuntimeException e) {
//			e.printStackTrace();
//			throw e;
//		}
	}

	public void endDocument() throws SAXException {
	// nl(); emit("END DOCUMENT");
	// try {
	// nl();
	// out.flush();
	// } catch (IOException e) {
	// throw new SAXException("I/O error", e);
	// }

	}

	public void endElement(String uri, String lName, String qName) throws SAXException {
		String eName = lName.toUpperCase(); // element name
		if (EMPTY.equals(eName)) eName = qName.toUpperCase(); // namespaceAware
		// = false

		if (iDebug) {
			// nl();
			// emit("END_ELM: ");
			emit("</" + eName + ">");
			flush();
			iIndentLevel--;
		}
		int tag = eName.hashCode();

		iElementNames.remove(iElementNames.size() - 1);

		if (tag == CIM) {} else if (tag == MESSAGE) {} else if (tag == MULTIRSP) {
			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMResponse
					&& iObjects.elementAt(iObjects.size() - 1) instanceof CIMResponse) {
				CIMResponse simplersp = (CIMResponse) pop();
				CIMResponse multirsp = (CIMResponse) iObjects.lastElement();
				multirsp.addResponse(simplersp);
			}
		} else if (tag == SIMPLERSP) {
			Object obj = null;
			if (iObjects.size() > 1) {
				obj = iObjects.elementAt(iObjects.size() - 2);
				CIMResponse response = (CIMResponse) pop();
				if (obj instanceof CIMResponse) {
					CIMResponse multirsp = (CIMResponse) obj;
					multirsp.addResponse(response);
				}
			}
		} else if (tag == MULTIEXPREQ) {
			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMRequest
					&& iObjects.elementAt(iObjects.size() - 1) instanceof CIMRequest) {
				CIMRequest simplereq = (CIMRequest) pop();
				CIMRequest multireq = (CIMRequest) iObjects.lastElement();
				multireq.addRequest(simplereq);
			}
		} else if (tag == SIMPLEEXPREQ) {
			Object obj = null;
			if (iObjects.size() > 1) {
				obj = iObjects.elementAt(iObjects.size() - 2);
				CIMRequest request = (CIMRequest) pop();
				if (obj instanceof CIMResponse) {
					CIMRequest multireq = (CIMRequest) obj;
					multireq.addRequest(request);
				}
			}
		} else if (tag == EXPMETHODCALL) {} else if (tag == EXPPARAMVALUE) {} else if (tag == IPARAMVALUE) {} else if (tag == IRETURNVALUE) {} else if (tag == ERROR) {
			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMException
					&& iObjects.elementAt(iObjects.size() - 2) instanceof CIMResponse) {
				CIMException e = (CIMException) pop();
				CIMResponse response = (CIMResponse) iObjects.lastElement();
				response.setError(e);

			}
			// CIMException e = (CIMException) objects.remove(objects.size()-1);
			//			
			// Object obj = objects.elementAt(objects.size()-1);
			// if (!(obj instanceof CIMResponse)) throw new
			// SAXException("Response tag expected");
			// CIMResponse response = (CIMResponse) obj;
		} else if (tag == KEYVALUE) {
			if (iObjects.size() > 0 && iTmpValues.size() > 0) {

				String valueType = (String) iTmpValues.remove(iTmpValues.size() - 1);

				CIMDataType type = CIMDataType.getDataType(valueType, false);
				Object o = null;
				try {
					try {
						o = createJavaObject(valueType, (iValue != null) ? iValue.toString()
								: EMPTY);
					} catch (NumberFormatException nfe) {
						/*
						 * Workaround for Engenio/WBEM-Solutions CIMOM that
						 * reports all keyvalues untruly as "numeric". We just
						 * try to reparse as string.
						 */
						valueType = "string";
						type = CIMDataType.getDataType(valueType, false);
						o = createJavaObject(valueType, (iValue != null) ? iValue.toString()
								: EMPTY);
					}
				} catch (Exception e) {
					throw new SAXException(e);
				}

				CIMValue value = new CIMValue(o, type);

				CIMProperty property;
				if (iObjects.lastElement() instanceof CIMProperty) {
					property = (CIMProperty) iObjects.lastElement();
				} else {
					property = new CIMProperty();
					iObjects.add(property);
				}
				property.setValue(value);

			} else {
				throw new SAXException("Inconsistent state of XML parser");
			}
			iValue = null;
		} else if (tag == KEYBINDING) {
			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMProperty
					&& iObjects.elementAt(iObjects.size() - 2) instanceof CIMObjectPath) {
				CIMProperty property = (CIMProperty) pop();
				CIMObjectPath objectModel = (CIMObjectPath) iObjects.lastElement();
				objectModel.addKey(property);
				// } else if (objects.size() > 1 && objects.lastElement()
				// instanceof CIMObjectPath
				// && objects.elementAt(objects.size()-2) instanceof
				// CIMProperty) {
				// CIMObjectPath objectModel = (CIMObjectPath)pop();
				// CIMProperty property = (CIMProperty)objects.lastElement();
				// property.setValue(new CIMValue(objectModel));
			}
		} else if (tag == QUALIFIER) {
			if (iTmpValues.size() > 0) iTmpValues.remove(iTmpValues.size() - 1);

			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMQualifier) {
				if (iObjects.elementAt(iObjects.size() - 2) instanceof CIMQualifiableElement) {
					CIMQualifier qualifier = (CIMQualifier) pop();
					CIMQualifiableElement qualifiable = (CIMQualifiableElement) iObjects
							.lastElement();

					qualifiable.addQualifier(qualifier);
				}
			}

		} else if (tag == HOST) {
			CIMNameSpace ns = null;

			String name = (iValue == null) ? "" : iValue.toString();
			if (name == null || name.length() == 0) throw new SAXException("missing hostname");

			if (!(iObjects.size() > 0) || !(iObjects.lastElement() instanceof CIMNameSpace)) {
				ns = new CIMNameSpace();
				iObjects.add(ns);

			} else {
				ns = (CIMNameSpace) iObjects.lastElement();
			}
			ns.setHost(name);
			iValue = null;
		} else if (tag == METHOD) {
			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMMethod) {
				if (iObjects.elementAt(iObjects.size() - 2) instanceof CIMClass) {
					CIMMethod method = (CIMMethod) iObjects.remove(iObjects.size() - 1);
					CIMClass clazz = (CIMClass) iObjects.lastElement();

					clazz.addMethod(method);
				}
			}
		} else if (tag == PARAMETER) {
			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMParameter) {
				if (iObjects.elementAt(iObjects.size() - 2) instanceof CIMMethod) {
					CIMParameter parameter = (CIMParameter) pop();
					CIMMethod method = (CIMMethod) iObjects.lastElement();

					method.addParameter(parameter);
				}
			}
		} else if (tag == PROPERTY || tag == PROPERTY_ARRAY || tag == PROPERTY_REFERENCE) {
			if (tag != PROPERTY_REFERENCE && iTmpValues.size() > 0) iTmpValues.remove(iTmpValues
					.size() - 1);

			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMProperty) {
				if (iObjects.elementAt(iObjects.size() - 2) instanceof CIMInstance) {
					CIMProperty property = (CIMProperty) iObjects.remove(iObjects.size() - 1);
					CIMInstance instance = (CIMInstance) iObjects.lastElement();

					/*
					 * ebak: 1657901 Performance issues
					 * CIMInstance.updatePropety() is replaced by CIMInstance.addProperty(),
					 * since check for property duplication is not necessary here. Any duplicated
					 * property is the bug of the CIMOM. 
					 */
					instance.addProperty(property);
				} else if (iObjects.elementAt(iObjects.size() - 2) instanceof CIMClass) {
					CIMProperty property = (CIMProperty) iObjects.remove(iObjects.size() - 1);
					CIMClass clazz = (CIMClass) iObjects.lastElement();

					clazz.addProperty(property);
				}
			}
		} else if (tag == CLASSPATH || tag == LOCALCLASSPATH || tag == INSTANCEPATH
				|| tag == LOCALINSTANCEPATH) {

			CIMObjectPath objectPath;
			if ((iObjects.lastElement() instanceof CIMObjectPath)
					&& (iObjects.elementAt(iObjects.size() - 2) instanceof CIMNameSpace)) {
				objectPath = (CIMObjectPath) pop();
				CIMNameSpace ns = (CIMNameSpace) pop();
				objectPath.setHost(ns.getHost());
				objectPath.setNameSpace(ns);

				iObjects.add(objectPath);
			}
		} else if (tag == VALUE || tag == VALUE_ARRAY || tag == VALUE_REFARRAY) {
			if ((tag == VALUE_ARRAY || tag == VALUE_REFARRAY) && iObjects.size() > 1
					&& iObjects.lastElement() instanceof Vector) {
				Vector valueArray = (Vector) pop();
				String valueType = (String) iTmpValues.lastElement();
				iObjects.add(new CIMValue(valueArray, CIMDataType.getDataType(valueType, true)));
			} else if (tag == VALUE) {
				// if (value == null) throw new SAXException("missing value");

				if (iTmpValues.size() > 0) {
					String valueType = (String) iTmpValues.lastElement();
					String valueStr = (iValue != null) ? iValue.toString() : EMPTY;
					if (valueType == null) {
						// TODO guess valuetype from valuestr
					}
					Object o = null;
					if (!((iObjects.lastElement() instanceof CIMQualifierType) && valueStr
							.equals("NULL"))) {
						// try {
						o = createJavaObject(valueType, valueStr);
						// } catch (Exception e) {
						// throw new SAXExcepstion(e);
						// }
					} else {
						// System.out.println("NULL found");
					}

					if (((String) iElementNames.lastElement()).equalsIgnoreCase("VALUE.ARRAY")) {
						Vector valueArray = (Vector) iObjects.lastElement();
						valueArray.add(o);
					} else {
						iObjects.add(new CIMValue(o, CIMDataType.getDataType(valueType, false)));
					}
					iValue = null;
				} else {
					throw new SAXException("Error while parsing value: value without type");
				}
			}

			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMValue) {
				if ((iObjects.elementAt(iObjects.size() - 2) instanceof CIMProperty)) {
					CIMValue value = (CIMValue) pop();
					CIMProperty property = (CIMProperty) iObjects.lastElement();

					property.setValue(value);

				} else if ((iObjects.elementAt(iObjects.size() - 2) instanceof CIMQualifier)) {
					CIMValue value = (CIMValue) pop();
					CIMQualifier qualifier = (CIMQualifier) iObjects.lastElement();

					qualifier.setValue(value);

				} else if ((iObjects.elementAt(iObjects.size() - 2) instanceof CIMArgument)) {
					CIMValue value = (CIMValue) pop();
					CIMArgument argument = (CIMArgument) iObjects.lastElement();

					argument.setValue(value);
				} else if ((iObjects.elementAt(iObjects.size() - 2) instanceof CIMQualifierType)) {
					CIMValue value = (CIMValue) pop();
					CIMQualifierType qualifierType = (CIMQualifierType) iObjects.lastElement();

					qualifierType.setDefaultValue(value);
				} else if ((iObjects.elementAt(iObjects.size() - 2) instanceof Vector)) {
					// CIMValue value = (CIMValue) pop();
					// Vector valueArray = (Vector) pop();
					throw new SAXException("not implemented");
				} else if ((iObjects.elementAt(iObjects.size() - 2) instanceof CIMResponse)) {
					Object result = pop();
					CIMResponse response = (CIMResponse) iObjects.lastElement();
					response.addReturnValue(result);
				}
			}

			if (iElementNames.size() > 1
					&& ((String) iElementNames.lastElement()).equalsIgnoreCase("IRETURNVALUE")
					&& iObjects.size() > 1
					&& iObjects.elementAt(iObjects.size() - 2) instanceof CIMResponse) {
				Object result = pop();
				CIMResponse response = (CIMResponse) iObjects.lastElement();
				response.addReturnValue(result);
			}
			// } else if (tag == VALUE_REFERENCE) {
			// if( objects.size() > 1 && objects.lastElement() instanceof
			// CIMObjectPath
			// && objects.elementAt(objects.size()-2) instanceof CIMProperty) {
			// CIMObjectPath objectModel = (CIMObjectPath)pop();
			// CIMProperty property = (CIMProperty)objects.lastElement();
			// property.setValue(new CIMValue(objectModel));
			// }
		} else if (tag == VALUE_NAMEDINSTANCE || tag == VALUE_NAMEDOBJECT) {
			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMInstance) {
				if ((iObjects.elementAt(iObjects.size() - 2) instanceof CIMObjectPath)) {
					CIMInstance instance = (CIMInstance) pop();
					CIMObjectPath objectpath = (CIMObjectPath) pop();

					instance.setObjectPath(objectpath);
					iObjects.add(instance);
				}
			}

			if (iElementNames.size() > 1
					&& ((String) iElementNames.lastElement()).equalsIgnoreCase("IRETURNVALUE")
					&& iObjects.size() > 1
					&& iObjects.elementAt(iObjects.size() - 2) instanceof CIMResponse) {
				Object result = pop();
				CIMResponse response = (CIMResponse) iObjects.lastElement();
				response.addReturnValue(result);
			}
			if (iElementNames.size() > 1
					&& ((String) iElementNames.lastElement()).equalsIgnoreCase("IRETURNVALUE")) {
				if (iObjects.size() > 2
						&& !(iObjects.elementAt(iObjects.size() - 2) instanceof CIMResponse)) throw new SAXException(
						"inconsistent state of XML parser");
			}
		} else if (tag == VALUE_OBJECTWITHPATH || tag == VALUE_OBJECTWITHLOCALPATH) {
			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMInstance) {
				if ((iObjects.elementAt(iObjects.size() - 2) instanceof CIMObjectPath)) {
					CIMInstance instance = (CIMInstance) pop();
					CIMObjectPath objectpath = (CIMObjectPath) pop();

					instance.setObjectPath(objectpath);
					iObjects.add(instance);
				}
			} else if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMClass) {
				if ((iObjects.elementAt(iObjects.size() - 2) instanceof CIMObjectPath)) {
					CIMClass clazz = (CIMClass) pop();
					CIMObjectPath objectpath = (CIMObjectPath) pop();

					clazz.setObjectPath(objectpath);
					iObjects.add(clazz);
				}
			}

			if (iElementNames.size() > 1
					&& ((String) iElementNames.lastElement()).equalsIgnoreCase("IRETURNVALUE")) {
				if (iObjects.size() > 2
						&& !(iObjects.elementAt(iObjects.size() - 2) instanceof CIMResponse)) throw new SAXException(
						"inconsistent CIMXML parser state");
			}

			if (iElementNames.size() > 1
					&& ((String) iElementNames.lastElement()).equalsIgnoreCase("IRETURNVALUE")
					&& iObjects.size() > 1
					&& iObjects.elementAt(iObjects.size() - 2) instanceof CIMResponse) {
				Object result = pop();
				CIMResponse response = (CIMResponse) iObjects.lastElement();
				response.addReturnValue(result);
			}
		} else if (tag == CLASSNAME || tag == INSTANCENAME || tag == VALUE_OBJECT
				|| tag == OBJECTPATH || tag == QUALIFIER_DECLARATION || tag == VALUE_REFERENCE
				|| tag == CLASS || tag == INSTANCE) {

			if (tag == VALUE_REFERENCE && iObjects.size() > 1
					&& iObjects.lastElement() instanceof CIMObjectPath) {
				CIMObjectPath valueReference = (CIMObjectPath) pop();

				// String elemName = (String) iElementNames.lastElement();
				if (/*
					 * (elemName.equalsIgnoreCase("KEYBINDING") ||
					 * elemName.equalsIgnoreCase("VALUE.REFERENCE") ||
					 * elemName.equalsIgnoreCase("PROPERTY.REFERENCE")) &&
					 */
				iObjects.lastElement() instanceof CIMProperty) {
					CIMProperty property = (CIMProperty) iObjects.lastElement();
					property.setValue(new CIMValue(valueReference, CIMDataType
							.getPredefinedType(CIMDataType.REFERENCE)));

				} else if (iObjects.lastElement() instanceof CIMArgument) {
					CIMArgument argument = (CIMArgument) iObjects.lastElement();

					argument.setValue(new CIMValue(valueReference, CIMDataType
							.getPredefinedType(CIMDataType.REFERENCE)));
				} else if (iObjects.lastElement() instanceof Vector) {
					Vector valueRefArray = (Vector) iObjects.lastElement();

					// valueRefArray.add(new CIMValue(valueReference,
					// CIMDataType.getPredefinedType(CIMDataType.REFERENCE)));
					valueRefArray.add(valueReference);
				} else {
					throw new SAXException("inconsistent XML parser state");
				}
			} else if (tag == QUALIFIER_DECLARATION) {
				iTmpValues.remove(iTmpValues.size() - 1);
			}

			if (iElementNames.size() > 1
					&& ((String) iElementNames.lastElement()).equalsIgnoreCase("IRETURNVALUE")) {
				if (iObjects.size() > 2
						&& !(iObjects.elementAt(iObjects.size() - 2) instanceof CIMResponse)) throw new SAXException(
						"inconsistent state of XML parser");
			}
			if (iElementNames.size() > 1 && iObjects.size() > 1) {
				if (iObjects.elementAt(iObjects.size() - 2) instanceof CIMResponse) {
					String elemName = (String) iElementNames.lastElement();

					if (elemName.equalsIgnoreCase("IRETURNVALUE")
							|| (elemName.equalsIgnoreCase("RETURNVALUE") && tag == VALUE_REFERENCE)) {
						Object result = pop();
						CIMResponse response = (CIMResponse) iObjects.lastElement();
						response.addReturnValue(result);
						// } else if (elemName.equalsIgnoreCase("PARAMVALUE")) {
						// Object result = pop();
						// CIMResponse response =
						// (CIMResponse)objects.lastElement();
						// response.addParamValue(result);
					}
				} else if (iObjects.elementAt(iObjects.size() - 2) instanceof CIMRequest) {
					Object result = pop();
					CIMRequest request = (CIMRequest) iObjects.lastElement();
					request.addParamValue(result);
				}
			}
		} else if (tag == SIMPLERSP) {
			if (iElementNames.size() > 1
					&& ((String) iElementNames.lastElement()).equalsIgnoreCase("MULTIRSP")
					&& iObjects.size() > 1 && iObjects.lastElement() instanceof CIMResponse) {
				CIMResponse simplersp = (CIMResponse) pop();
				CIMResponse multirsp = (CIMResponse) iObjects.lastElement();

				multirsp.addResponse(simplersp);
			}
		} else if (tag == SIMPLEEXPREQ) {
			if (iElementNames.size() > 1
					&& ((String) iElementNames.lastElement()).equalsIgnoreCase("MULTIEXPREQ")
					&& iObjects.size() > 1 && iObjects.lastElement() instanceof CIMRequest) {
				CIMRequest simplereq = (CIMRequest) pop();
				CIMRequest multireq = (CIMRequest) iObjects.lastElement();

				multireq.addRequest(simplereq);
			}
		} else if (tag == PARAMVALUE) {
			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMArgument
					&& iObjects.elementAt(iObjects.size() - 2) instanceof CIMResponse) {
				Object result = pop();
				CIMResponse response = (CIMResponse) iObjects.lastElement();
				response.addParamValue(result);
			}
		} else if (tag == PARAMETER_REFERENCE || tag == PARAMETER_ARRAY
				|| tag == PARAMETER_REFARRAY || tag == PARAMETER) {
			if (iObjects.size() > 1 && iObjects.lastElement() instanceof CIMParameter
					&& iObjects.elementAt(iObjects.size() - 2) instanceof CIMMethod) {
				CIMParameter parameter = (CIMParameter) pop();
				CIMMethod method = (CIMMethod) iObjects.lastElement();
				method.addParameter(parameter);
			}
		} else if (tag == RETURNVALUE) {
			// if (tmpValues.size() == 0)
			// throw new SAXException("inconsistent XML parser state: missing
			// return value type");
			if (iTmpValues.size() > 0) iTmpValues.remove(iTmpValues.size() - 1);
		} else if (tag == NAMESPACEPATH || tag == NAMESPACE || tag == LOCALNAMESPACEPATH
				|| tag == IMETHODRESPONSE || tag == METHODRESPONSE || tag == CIM || tag == MESSAGE
				|| tag == SIMPLERSP || tag == MULTIRSP || tag == SCOPE) {} else {
			throw new SAXException("Unexpected closing tag " + eName);
		}

	}

	public void endPrefixMapping(String prefix) throws SAXException {}

	public void error(SAXParseException exception) throws SAXException {
	// System.out.println("ERROR:"+exception);
	// exception.printStackTrace();
	}

	public void fatalError(SAXParseException exception) throws SAXException {
	// System.out.println("FATALERROR:"+exception);
	// exception.printStackTrace();
	}

	public Vector getObjects() {
		return iObjects;
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
	// System.out.println("IGNORABLE WHITESPACE:"+ch+", start:"+start+",
	// length:"+length);
	}

	public void notationDecl(String name, String publicId, String systemId) throws SAXException {}

	public boolean parse(XMLPullParser reader) throws IOException, SAXException {
		while (reader.hasNext()) {
			int event = reader.next();
			switch (event) {
				case XMLPullParser.START_ELEMENT:
					startElement(EMPTY, EMPTY, reader.getElementName(), reader.getAttributes());
					break;
				case XMLPullParser.END_ELEMENT:
					endElement(EMPTY, EMPTY, reader.getElementName());

					break;
				case XMLPullParser.CHARACTERS:
					char[] buf = reader.getText().toCharArray();
					characters(buf, 0, buf.length);
					break;
				case XMLPullParser.END_DOCUMENT:
			}
		}
		return true;
	}

	public void processingInstruction(String target, String data) throws SAXException {
	// System.out.println("PROCESSING INSTRUCTION:"+target+", "+data);

	}

	public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
		return null;
	}

	public void setDocumentLocator(Locator locator) {}

	public void skippedEntity(String name) throws SAXException {
	// System.out.println("SKIPPED ENTITY:"+name);
	}

	public void startDocument() throws SAXException {
		iIndentLevel = 0;
		// nl();
		// nl();
		// emit("START DOCUMENT");
		// nl();
		// emit("<?xml version='1.0' encoding='UTF-8'?>");
		// nl();

	}

	public void startElement(String uri, String lName, String qName, Attributes attrs)
			throws SAXException {
		iIndentLevel++;
		String eName = lName.toUpperCase(); // element name
		if (EMPTY.equals(eName)) eName = qName.toUpperCase(); // namespaceAware
		// = false
		int tag = eName.hashCode();

		if (iDebug) {
			emit("<" + eName + " ");
			// nl();
			// emit("ELEMENT:");
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if ("".equals(aName)) aName = attrs.getQName(i);
					// nl();
					// emit(" ATTR: ");
					emit(aName);
					emit("=\"");
					emit(attrs.getValue(i));
					emit("\" ");
				}
			}
			// if (attrs.getLength() > 0)
			// nl();
			emit(">");
			nl();
			flush();
		}

		iElementNames.add(eName);

		if (tag == CIM) {
			String cimversion = null;
			String dtdversion = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("CIMVERSION")) cimversion = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("DTDVERSION")) dtdversion = attrs.getValue(i);
				}
			}
			CIM cim = new CIM(cimversion, dtdversion);
			iObjects.add(cim);
		} else if (tag == MESSAGE) {
			String id = null;
			String protocolVersion = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("ID")) id = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("PROTOCOLVERSION")) protocolVersion = attrs
							.getValue(i);
				}
			}
			Message message = new Message(id, protocolVersion);
			iObjects.add(message);
		} else if (tag == MULTIRSP || tag == SIMPLERSP) {

			CIMResponse response = new CIMResponse();

			if ((iObjects.size() > 0) && iObjects.lastElement() instanceof Message) {
				Object message = pop();
				if (!(message instanceof Message)) throw new SAXException(
						"expected MESSAGE element name");
				Object cim = pop();
				if (!(cim instanceof CIM)) throw new SAXException("expected CIM element name");

				response.setMethod(eName);
				response.iCimVersion = ((CIM) cim).iCimVersion;
				response.iDtdVersion = ((CIM) cim).iDtdVersion;
				response.iId = ((Message) message).iId;
				response.iProtocolVersion = ((Message) message).iProtocolVersion;
			}

			iObjects.add(response);
		} else if (tag == SIMPLEEXPREQ || tag == MULTIEXPREQ) {
			CIMRequest request = new CIMRequest();
			if ((iObjects.size() > 0) && iObjects.lastElement() instanceof Message) {
				Object message = pop();
				if (!(message instanceof Message)) throw new SAXException(
						"expected MESSAGE element name");
				Object cim = pop();
				if (!(cim instanceof CIM)) throw new SAXException("expected CIM element name");

				request.setMethod(eName);
				request.iCimVersion = ((CIM) cim).iCimVersion;
				request.iDtdVersion = ((CIM) cim).iDtdVersion;
				request.iId = ((Message) message).iId;
				request.iProtocolVersion = ((Message) message).iProtocolVersion;
			}

			iObjects.add(request);

		} else if (tag == EXPMETHODCALL) {} else if (tag == EXPPARAMVALUE) {} else if (tag == IPARAMVALUE) {} else if (tag == IMETHODRESPONSE) {} else if (tag == METHODRESPONSE) {} else if (tag == IRETURNVALUE) {} else if (tag == ERROR) {

			String code = null;
			String description = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("CODE")) code = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("DESCRIPTION")) description = attrs.getValue(i);
				}
			}
			if (code == null || code.length() == 0) throw new SAXException("missing error code");
			if (description == null) description = EMPTY;

			int errorCode = 0;
			try {
				errorCode = Integer.parseInt(code);
			} catch (Exception e) {
				Logger logger = SessionProperties.getGlobalProperties().getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "exception while parsing error code", e);
				}
			}

			iObjects.add(new CIMException(CIMException.getStatusFromCode(errorCode),
					"Remote exception at the CIMOM:" + description));

		} else if (tag == INSTANCE) {
			CIMInstance inst = new CIMInstance();

			String classname = null;

			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("CLASSNAME")) classname = attrs.getValue(i);
				}
			}

			if (classname == null && classname.length() == 0) throw new SAXException(
					"missing instance's classname");

			inst.setClassName(classname);

			iObjects.add(inst);

		} else if (tag == CLASS) {
			CIMClass clazz = new CIMClass();

			String name = null;
			String superclass = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("NAME")) name = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("SUPERCLASS")) superclass = attrs.getValue(i);
				}
			}
			if (name == null) throw new SAXException("missing class name");
			clazz.setName(name);

			if (superclass != null && superclass.length() > 0) clazz.setSuperClass(superclass);

			iObjects.add(clazz);

		} else if (tag == METHOD) {
			CIMMethod method = new CIMMethod();

			String name = null;
			String type = null;
			String classorigin = null;
			String propagated = null;

			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("NAME")) name = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("TYPE")) type = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("CLASSORIGIN")) classorigin = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("PROPAGATED")) propagated = attrs.getValue(i);
				}
			}
			method.setName(name);

			if (type != null && type.length() > 0) method.setType(CIMDataType.getDataType(type,
					false));

			if (classorigin != null && classorigin.length() > 0) method.setOriginClass(classorigin);
			method.setPropagated(TRUE.equalsIgnoreCase(propagated));

			iObjects.add(method);
		} else if (tag == PARAMETER || tag == PARAMETER_ARRAY || tag == PARAMETER_REFERENCE
				|| tag == PARAMETER_REFARRAY) {

			CIMParameter parameter = new CIMParameter();
			String name = null;
			String type = null;
			String arraysize = null;
			String referenceclass = null;

			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("NAME")) name = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("TYPE")) type = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("ARRAYSIZE")) arraysize = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("REFERENCECLASS")) referenceclass = attrs
							.getValue(i);
				}
			}
			if (name == null) throw new SAXException("missing property name");
			parameter.setName(name);

			int arraySize = CIMDataType.SIZE_UNLIMITED;
			try {
				if (arraysize != null && arraysize.length() != 0) arraySize = Integer
						.parseInt(arraysize);
			} catch (Exception e) {
				Logger logger = SessionProperties.getGlobalProperties().getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "exception while parsing array size from parameter",
							e);
				}
			}

			if (tag == PARAMETER_REFERENCE || tag == PARAMETER_REFARRAY) {
				if (referenceclass == null) throw new SAXException("missing reference class");

				if (tag == PARAMETER_REFARRAY) {
					parameter.setType(new CIMDataType(referenceclass, arraySize));
				} else {
					parameter.setType(new CIMDataType(referenceclass));
				}
			} else {
				if (type == null) throw new SAXException("missing parameter's type");
				CIMDataType arrayType = CIMDataType.getDataType(type, tag == PARAMETER_ARRAY);
				if (arraySize > -1) parameter.setType(new CIMDataType(arrayType.getType(),
						arraySize));
				else parameter.setType(arrayType);
			}

			iObjects.add(parameter);
		} else if (tag == PROPERTY || tag == PROPERTY_ARRAY || tag == PROPERTY_REFERENCE) {
			CIMProperty property = new CIMProperty();
			String name = null;
			String type = null;
			String classorigin = null;
			String propagated = null;
			String arraysize = null;
			String referenceclass = null;

			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("NAME")) name = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("TYPE")) type = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("CLASSORIGIN")) classorigin = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("PROPAGATED")) propagated = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("ARRAYSIZE")) arraysize = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("REFERENCECLASS")) referenceclass = attrs
							.getValue(i);
				}
			}
			if (name == null) throw new SAXException("missing property name");
			property.setName(name);

			int arraySize = CIMDataType.SIZE_UNLIMITED;
			try {
				if (arraysize != null && arraysize.length() != 0) arraySize = Integer
						.parseInt(arraysize);
			} catch (Exception e) {
				Logger logger = SessionProperties.getGlobalProperties().getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "exception while parsing property's array size", e);
				}
			}

			if (tag != PROPERTY_REFERENCE) {
				if (type != null) {
					CIMDataType propType = CIMDataType.getDataType(type, tag == PROPERTY_ARRAY);
					if (tag == PROPERTY_ARRAY) {
						property.setType(new CIMDataType(propType.getType(), arraySize));
					} else {
						property.setType(propType);
					}
					iTmpValues.add(type);
				} else throw new SAXException("missing property's type");
			} else if (referenceclass != null && referenceclass.length() > 0) {
				property.setType(new CIMDataType(referenceclass));
			} // else
			// throw new SAXException("missing property's reference class");

			if (classorigin != null && classorigin.length() > 0) property
					.setOriginClass(classorigin);
			property.setPropagated(TRUE.equalsIgnoreCase(propagated));

			iObjects.add(property);
		} else if (tag == QUALIFIER) {
			CIMQualifier qualifier = new CIMQualifier();

			String name = null;
			String type = null;
			String propagated = null;
			String overridable = TRUE;
			String tosubclass = TRUE;
			String translatable = FALSE;
			// String overridable = null;
			// String tosubclass = null;
			// String translatable = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("NAME")) name = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("TYPE")) type = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("OVERRIDABLE")) overridable = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("PROPAGATED")) propagated = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("TOSUBCLASS")) tosubclass = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("TRANSLATABLE")) translatable = attrs.getValue(i);
				}
			}

			if (name == null || name.length() == 0) throw new SAXException("missing qualifier name");
			qualifier.setName(name);

			if (type == null || type.length() == 0) throw new SAXException("missing qualifier type");
			iTmpValues.add(type);

			qualifier.setPropagated(TRUE.equalsIgnoreCase(propagated));

			if (TRUE.equalsIgnoreCase(overridable)) qualifier.addFlavor(CIMFlavor
					.getFlavor(CIMFlavor.ENABLEOVERRIDE));
			else qualifier.addFlavor(CIMFlavor.getFlavor(CIMFlavor.DISABLEOVERRIDE));

			if (TRUE.equalsIgnoreCase(tosubclass)) qualifier.addFlavor(CIMFlavor
					.getFlavor(CIMFlavor.TOSUBCLASS));
			else qualifier.addFlavor(CIMFlavor.getFlavor(CIMFlavor.RESTRICTED));

			if (TRUE.equalsIgnoreCase(translatable)) qualifier.addFlavor(CIMFlavor
					.getFlavor(CIMFlavor.TRANSLATE));

			iObjects.add(qualifier);
			// VALUE
		} else if (tag == QUALIFIER_DECLARATION) {
			CIMQualifierType qualifierType = new CIMQualifierType();

			String name = null;
			String type = null;
			String overridable = TRUE;
			String tosubclass = TRUE;
			String translatable = FALSE;
			String isArray = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("NAME")) name = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("TYPE")) type = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("OVERRIDABLE")) overridable = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("TOSUBCLASS")) tosubclass = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("TRANSLATABLE")) translatable = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("ISARRAY")) isArray = attrs.getValue(i);
				}
			}

			if (name == null || name.length() == 0) throw new SAXException("missing qualifier name");
			qualifierType.setName(name);

			if (type == null || type.length() == 0) throw new SAXException("missing qualifier type");
			iTmpValues.add(type);

			if (isArray != null) {
				boolean isArrayType = TRUE.equalsIgnoreCase(isArray);
				qualifierType.setType(CIMDataType.getDataType(type, isArrayType));
			}

			if (TRUE.equalsIgnoreCase(overridable)) qualifierType.addFlavor(CIMFlavor
					.getFlavor(CIMFlavor.ENABLEOVERRIDE));
			else qualifierType.addFlavor(CIMFlavor.getFlavor(CIMFlavor.DISABLEOVERRIDE));

			if (TRUE.equalsIgnoreCase(tosubclass)) qualifierType.addFlavor(CIMFlavor
					.getFlavor(CIMFlavor.TOSUBCLASS));
			else qualifierType.addFlavor(CIMFlavor.getFlavor(CIMFlavor.RESTRICTED));

			if (TRUE.equalsIgnoreCase(translatable)) qualifierType.addFlavor(CIMFlavor
					.getFlavor(CIMFlavor.TRANSLATE));

			iObjects.add(qualifierType);
		} else if (tag == VALUE) {
			if (iTmpValues.size() > 0 && iTmpValues.lastElement() == null) {
				iTmpValues.remove(iTmpValues.size() - 1);

				String valueType = null;
				if (attrs != null) {
					for (int i = 0; i < attrs.getLength(); i++) {
						String aName = attrs.getLocalName(i); // Attr name
						if (EMPTY.equals(aName)) aName = attrs.getQName(i);

						if (aName.equalsIgnoreCase("TYPE")) valueType = attrs.getValue(i);
					}
				}
				iTmpValues.add(valueType);
			}
			// VALUE_ARRAY
		} else if (tag == VALUE_ARRAY || tag == VALUE_REFARRAY) {
			Vector value_array = new Vector();

			iObjects.add(value_array);
			// VALUE_NAMEDINSTANCE
		} else if (tag == VALUE_NAMEDINSTANCE || tag == VALUE_NAMEDOBJECT) {
			// HOST
		} else if (tag == HOST) {
			iValue = null;
			// NAMESPACEPATH
		} else if (tag == NAMESPACEPATH) {
			CIMNameSpace ns;

			if (!(iObjects.size() > 0) || !(iObjects.lastElement() instanceof CIMNameSpace)) {
				ns = new CIMNameSpace();
				ns.setNameSpace(EMPTY);
				iObjects.add(ns);
			}
			// LOCALNAMESPACEPATH
		} else if (tag == LOCALNAMESPACEPATH) {
			CIMNameSpace ns;

			if (!(iObjects.size() > 0) || !(iObjects.lastElement() instanceof CIMNameSpace)) {
				ns = new CIMNameSpace();
				ns.setNameSpace(EMPTY);

				iObjects.add(ns);
			}

		} else if (tag == NAMESPACE) {
			CIMNameSpace ns;

			String name = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("NAME")) name = attrs.getValue(i);
				}
			}

			if (name == null) throw new SAXException("missing name space");

			if ((iObjects.size() > 0) && (iObjects.lastElement() instanceof CIMNameSpace)) {
				ns = (CIMNameSpace) iObjects.lastElement();
				ns.setNameSpace((ns.getNameSpace().length() == 0) ? name
						: (ns.getNameSpace() + '/' + name));
			}

			// INSTANCENAME, CLASSNAME
		} else if (tag == INSTANCENAME || tag == CLASSNAME) {

			CIMObjectPath objectModel = new CIMObjectPath();

			String classname = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("CLASSNAME")) classname = attrs.getValue(i);
					if (aName.equalsIgnoreCase("NAME")) // Some buggy CIMOMs use
					// NAME instead
					classname = attrs.getValue(i); // os CLASSNAME
				}
			}
			objectModel.setObjectName(classname);

			iObjects.add(objectModel);
			// KEYBINDING
		} else if (tag == KEYBINDING) {
			CIMProperty property = new CIMProperty();

			String name = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("NAME")) name = attrs.getValue(i);
				}
			}
			property.setName(name);
			property.setPropagated(false);

			iObjects.add(property);

			// VALUE_REFERENCE
		} else if (tag == VALUE_REFERENCE) {
			// KEYVALUE
		} else if (tag == KEYVALUE) {
			iValue = null;

			String valueType = "string"; // fix: default valuetype of
			// keyvalue to string
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("VALUETYPE")) valueType = attrs.getValue(i);
				}
			}
			if (valueType == null || valueType.length() == 0) throw new SAXException(
					"missing key valuetype");
			iTmpValues.add(valueType);

			// INSTANCE
		} else if (tag == INSTANCEPATH || tag == OBJECTPATH || tag == VALUE_OBJECTWITHPATH
				|| tag == VALUE_OBJECTWITHLOCALPATH) {} else if (tag == PARAMVALUE) {

			CIMArgument argument = new CIMArgument();

			String name = null;
			String type = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("NAME")) name = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("PARAMTYPE")) type = attrs.getValue(i);
				}
			}
			argument.setName(name);

			// if (type == null)
			// type = EMPTY;
			iTmpValues.add(type);

			iObjects.add(argument);
		} else if (tag == QUALIFIER_DECLARATION) {

			CIMQualifierType qualifierType = new CIMQualifierType();

			String name = null;
			String type = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("NAME")) name = attrs.getValue(i);
					else if (aName.equalsIgnoreCase("PARAMTYPE")) type = attrs.getValue(i);
				}
			}
			qualifierType.setName(name);

			if (type == null) type = EMPTY;
			iTmpValues.add(type);

			iObjects.add(qualifierType);

		} else if (tag == SCOPE) {

			if (iObjects.size() > 0 && iObjects.lastElement() instanceof CIMQualifierType) {
				CIMQualifierType qualifierType = (CIMQualifierType) iObjects.lastElement();

				if (attrs != null) {
					for (int i = 0; i < attrs.getLength(); i++) {
						String aName = attrs.getLocalName(i); // Attr name
						if (EMPTY.equals(aName)) aName = attrs.getQName(i);

						if (aName.equalsIgnoreCase("CLASS") && TRUE.equalsIgnoreCase(attrs.getValue(i))) qualifierType
								.addScope(CIMScope.getScope(CIMScope.CLASS));
						else if (aName.equalsIgnoreCase("ASSOCIATION")) qualifierType.addScope(CIMScope
								.getScope(CIMScope.ASSOCIATION));
						else if (aName.equalsIgnoreCase("REFERENCE")) qualifierType.addScope(CIMScope
								.getScope(CIMScope.REFERENCE));
						else if (aName.equalsIgnoreCase("PROPERTY")) qualifierType.addScope(CIMScope
								.getScope(CIMScope.PROPERTY));
						else if (aName.equalsIgnoreCase("METHOD")) qualifierType.addScope(CIMScope
								.getScope(CIMScope.METHOD));
						else if (aName.equalsIgnoreCase("PARAMETER")) qualifierType.addScope(CIMScope
								.getScope(CIMScope.PARAMETER));
						else if (aName.equalsIgnoreCase("INDICATION")) qualifierType.addScope(CIMScope
								.getScope(CIMScope.INDICATION));
					}
				}
			}
		} else if (tag == RETURNVALUE) {
			String type = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (EMPTY.equals(aName)) aName = attrs.getQName(i);

					if (aName.equalsIgnoreCase("PARAMTYPE")) type = attrs.getValue(i);
				}
			}
			// if (type == null || type.length() == 0)
			// throw new SAXException("missing return value's type");

			iTmpValues.add(type);

		} else if (tag == CLASSPATH || tag == LOCALCLASSPATH || tag == LOCALINSTANCEPATH
				|| tag == VALUE_OBJECT) {} else {
			throw new SAXException("Unexpected opening tag: " + eName);
		}
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {}

	public String toString() {
		return iObjects.toString();
	}

	public void unparsedEntityDecl(String name, String publicId, String systemId,
			String notationName) throws SAXException {}

	public void warning(SAXParseException exception) throws SAXException {
	// System.out.println("WARNING:"+exception);
	// exception.printStackTrace();
	}

	protected Object last() {
		return iObjects.lastElement();
	}

	protected Object pop() {
		return iObjects.remove(iObjects.size() - 1);
	}

	private void emit(String s) throws SAXException {
		try {
			cOut.write(s);
		} catch (IOException e) {
			throw new SAXException("I/O error", e);
		}
	}

	private void flush() throws SAXException {
		try {
			cOut.flush();
		} catch (IOException e) {
			throw new SAXException("I/O error", e);
		}
	}

	private void nl() throws SAXException {
		String lineEnd = System.getProperty("line.separator");
		try {
			cOut.write(lineEnd);
			for (int i = 0; i < iIndentLevel; i++)
				cOut.write(iIndentString);

		} catch (IOException e) {
			throw new SAXException("I/O error", e);
		}
	}
}
