/**
 * CIMEnumerationImpl.java
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
 *   18045    2005-08-10  pineiro5     Some code clean up in multiple points
 * 1488924    2006-05-15  lupusalex    Intermittent connection loss
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1893499    2008-03-19  blaschke-oss no CIMExeption thrown for wrong namespace using SAXParser
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.http.HttpClient;
import org.sblim.wbem.util.SessionProperties;
import org.sblim.wbem.xml.CIMResponse;
import org.sblim.wbem.xml.XMLDefaultHandlerImpl;
import org.sblim.wbem.xml.parser.XMLPullParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Provides the mechanisms to stream the response of CIMObjects, returned by the
 * CIMOM.
 */
public class CIMEnumerationImpl implements CIMEnumeration {

	private static final String CLASSNAME = "org.sblim.wbem.client.CIMEnumerationImpl";

	private static final String EMPTY = "";

	private XMLPullParser iPullParser;

	private XMLDefaultHandlerImpl iXmlHandler;

	private CIMObjectPath iRequest;

	private CIMNameSpace iDefaultNamespace;

	private boolean iHasNext;

	private InputStreamReader iInputStream;

	private Enumeration iEnumeration;

	private boolean iUseSAX;

	private HttpClient iHttpClient;

	private Logger iLogger = SessionProperties.getGlobalProperties().getLogger();

	public CIMEnumerationImpl(Enumeration pEnumeration, CIMObjectPath pRequest,
			CIMNameSpace pDefaultNamespace) {
		iRequest = pRequest;
		iDefaultNamespace = pDefaultNamespace;
		iEnumeration = pEnumeration;
	}

	public CIMEnumerationImpl(XMLDefaultHandlerImpl pHandler, CIMObjectPath pRequest,
			CIMNameSpace pDefaultNamespace, InputStreamReader pStream, HttpClient pHttpClient,
			boolean pUseSAX) {
		String methodName = "CIMEnumerationImpl(XMLDefaultHandlerImpl, CIMObjectPath, CIMNameSpace, InputStreamReader, HttpClient, boolean)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new String[] { pRequest.toString(),
					(pDefaultNamespace != null) ? pDefaultNamespace.toString() : null,
					(pStream != null) ? pStream.toString() : null,
					(pHttpClient != null) ? pHttpClient.toString() : null,
					Boolean.toString(pUseSAX) });
		}

		iXmlHandler = pHandler;
		iRequest = pRequest;
		iDefaultNamespace = pDefaultNamespace;
		iInputStream = pStream;
		iUseSAX = pUseSAX;
		iHttpClient = pHttpClient;

		try {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger
						.log(Level.FINER, pUseSAX ? "Using SAX parser... "
								: "Using Pull parser... ");
			}

			if (pUseSAX) {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				saxParser.parse(new InputSource(pStream), iXmlHandler);
				pStream = null;
			} else {
				iPullParser = new XMLPullParser(pStream);
				iHasNext = next(iPullParser, iXmlHandler, false);
			}
			
			Vector o = iXmlHandler.getObjects();

			if (o != null && o.size() > 0) {
				CIMResponse response = (CIMResponse) o.elementAt(0);
				response.checkError();
			} else {
				CIMException ce = new CIMException(CIMException.CIM_ERR_FAILED,
						"inconsistent state due to error while parsing response document");
				throw ce;
			}
		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error processing a returned enumeration", e);
			}
			throw e;
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error processing a returned enumeration", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName, new String[] { pRequest.toString(),
						(pDefaultNamespace != null) ? pDefaultNamespace.toString() : null,
						(pStream != null) ? pStream.toString() : null,
						(pHttpClient != null) ? pHttpClient.toString() : null,
						Boolean.toString(pUseSAX) });
			}
		}
	}

	/**
	 * Determines if this enumeration contains more elements.
	 * 
	 * @return <code>true</code> if this enumeration contains more elements
	 */
	public boolean hasMoreElements() {
		String methodName = "hasMoreElements()";
		boolean result = false;
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new String[] {
					(iRequest != null) ? iRequest.toString() : null,
					(iDefaultNamespace != null) ? iDefaultNamespace.toString() : null,
					(iInputStream != null) ? iInputStream.toString() : null,
					(iHttpClient != null) ? iHttpClient.toString() : null,
					Boolean.toString(iUseSAX) });
		}
		try {
			if (iEnumeration == null) {
				if (iUseSAX) {
					Vector o = iXmlHandler.getObjects();
					if (o == null || o.size() == 0) {
						CIMException e = new CIMException(CIMException.CIM_ERR_FAILED,
								"inconsistent XML parser: could not create cim response");
						if (iLogger.isLoggable(Level.INFO)) {
							iLogger.log(Level.INFO,
									"inconsistent XML parser: could not create cim response", e);
						}
						throw e;
					}
					CIMResponse response = (CIMResponse) (o.elementAt(0));
					result = response.getFirstReturnValue().size() > 0;
					return result;

				}
				result = iHasNext;
				if (!iHasNext) {
					iInputStream = null;
				}
				return result;
			}
			result = iEnumeration.hasMoreElements();
			return result;

		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName, Boolean.valueOf(result));
			}
		}
	}

	/**
	 * Reads the next object from the stream.
	 * 
	 * @return The next object
	 */
	public Object nextElement() {
		String methodName = "nextElement()";
		Object resultObj = null;
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName);
		}
		try {
			if (iEnumeration == null) {
				Vector objects = iXmlHandler.getObjects();

				if (objects.size() == 0) {
					CIMException e = new CIMException(CIMException.CIM_ERR_FAILED,
							"inconsistent XML parser: could not create cim response");
					throw e;
				}

				CIMResponse response = (CIMResponse) objects.elementAt(0);
				response.checkError();
				Vector v = response.getFirstReturnValue();

				if (v != null && v.size() > 0) {
					resultObj = v.remove(0);
					CIMClientXML.fixResult(iRequest, resultObj, iDefaultNamespace);
					try {
						if (v.size() == 0 && !iUseSAX) {
							iHasNext = next(iPullParser, iXmlHandler, false);
						} else {
							iHasNext = true;
						}
					} catch (Exception e) {
						if (iLogger.isLoggable(Level.WARNING)) {
							iLogger
									.log(
											Level.WARNING,
											"Unexpected exception while parsing XML document from the input stream",
											e);
						}
						iHasNext = false;
					}
					if (!iHasNext) {
						iInputStream = null;
					}

					return resultObj;
				}

				return resultObj;

			}
			resultObj = iEnumeration.nextElement();
			CIMClientXML.fixResult(iRequest, resultObj, iDefaultNamespace);

			return resultObj;

		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.WARNING)) {
				iLogger.log(Level.WARNING,
						"Error occured while reading the next enumration element", e);
			}
			throw e;
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.WARNING)) {
				iLogger.log(Level.WARNING,
						"Error occured while reading the next enumration element", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);

		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName, resultObj);
			}
		}
	}

	/**
	 * Close the enumeration by throwing away any remaing xml document without
	 * parsing it, while keeping the connection available for future requests.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		close(false);
	}

	/**
	 * Close the enumeration by throwing away any remaing xml document without
	 * parsing it. If the force argument is true, then it forces to close the
	 * connection without receiving any of the remainding XML document from the
	 * CIMOM, otherwise reads the rest of the XML document without parsing it.
	 * 
	 * @param pForce
	 *            If <code>true</code>, then the connection is closed without
	 *            receiving any of the remainding XML document from the CIMOM
	 * @throws IOException
	 */
	public void close(boolean pForce) throws IOException {
		String methodName = "close(boolean)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, Boolean.valueOf(pForce));
		}
		try {
			if (iInputStream != null) {
				if (pForce) {
					iHttpClient.streamFinished(false);
				} else {
					if (iEnumeration == null && iInputStream != null) iInputStream.close();
				}
				iInputStream = null;
			}
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.WARNING)) {
				iLogger.log(Level.WARNING,
						"Error occured during closing the stream of enumeration", e);
			}

		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName);
			}
		}
	}

	/**
	 * Fetch all the CIMObjects into memory. Preventing object loss when the
	 * CIMOM to close the connection because it timeout.
	 * 
	 * @throws IOException
	 */
	public void fetchAll() throws IOException {
		String methodName = "fetchAll()";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName);
		}
		try {
			next(iPullParser, iXmlHandler, true);

		} catch (SAXException e) {
			CIMException ee = new CIMException(CIMException.CIM_ERR_FAILED, e);
			if (iLogger.isLoggable(Level.WARNING)) {
				iLogger.log(Level.WARNING,
						"Error occured during fetching all enumeration elements", ee);
			}
			throw ee;

		} finally {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.exiting(CLASSNAME, methodName);
			}
		}
	}

	private boolean next(XMLPullParser pReader, DefaultHandler pHandler, boolean pFetchAll)
			throws IOException, SAXException {
		while (pReader.hasNext()) {
			int event = pReader.next();
			switch (event) {
				case XMLPullParser.START_ELEMENT:
					pHandler.startElement(EMPTY, EMPTY, pReader.getElementName(), pReader
							.getAttributes());
					break;
				case XMLPullParser.END_ELEMENT:
					pHandler.endElement(EMPTY, EMPTY, pReader.getElementName());

					if (!pFetchAll) {
						String lastElementName = null;
						if (pReader.getElementNames().size() > 0) lastElementName = (String) pReader
								.getElementNames().lastElement();

						if (lastElementName != null
								&& lastElementName.equalsIgnoreCase("IRETURNVALUE")) { return true; }
					}
					break;
				case XMLPullParser.CHARACTERS:
					char[] buf = pReader.getText().toCharArray();
					pHandler.characters(buf, 0, buf.length);
					break;
				case XMLPullParser.END_DOCUMENT:
					return false;
			}
		}
		return false;
	}

	public void finalize() {
		String methodName = "finalize()";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName);
		}

		try {
			if (iInputStream != null) close(true);
		} catch (IOException e) {
			if (iLogger.isLoggable(Level.WARNING)) {
				iLogger.log(Level.WARNING, "Error occured during cleaning up the enumeration", e);
			}
		}

		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.exiting(CLASSNAME, methodName);
		}
	}
}
