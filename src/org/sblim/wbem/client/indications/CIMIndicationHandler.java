/**
 * CIMIndicationHandler.java
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
 *   17931    2005-07-28  thschaef     Add InetAddress to CIM Event
 * 1438152    2006-05-15  lupusalex    Wrong message ID in ExportResponseMessage
 * 1498938    2006-06-01  lupusalex    Multiple events in single cim-xml request are not handled
 * 1498130    2006-05-31  lupusalex    Selection of xml parser on a per connection basis
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1656285    2007-02-12  ebak         IndicationHandler does not accept non-Integer message ID
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.client.indications;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.http.HttpContentHandler;
import org.sblim.wbem.http.HttpException;
import org.sblim.wbem.http.HttpHeader;
import org.sblim.wbem.http.HttpHeaderParser;
import org.sblim.wbem.http.MessageReader;
import org.sblim.wbem.http.MessageWriter;
import org.sblim.wbem.http.io.DebugInputStream;
import org.sblim.wbem.util.SessionProperties;
import org.sblim.wbem.xml.CIMClientXML_HelperImpl;
import org.sblim.wbem.xml.CIMRequest;
import org.sblim.wbem.xml.CIMXMLBuilderImpl;
import org.sblim.wbem.xml.CIMXMLParserImpl;
import org.sblim.wbem.xml.XMLDefaultHandlerImpl;
import org.sblim.wbem.xml.parser.XMLPullParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Handles the HTTP connections, providing the necesary interfaces for
 * CIMListener server.
 */
public class CIMIndicationHandler extends HttpContentHandler {

	private CIMEventDispatcher iDispatcher = null;

	private SessionProperties iSessionProperties = SessionProperties.getGlobalProperties();

	private int iMessageId = 0;

	public CIMIndicationHandler(CIMEventDispatcher pDispatcher) {
		this.iDispatcher = pDispatcher;
	}

	public CIMIndicationHandler(CIMEventDispatcher pDispatcher, SessionProperties pProperties) {
		this.iDispatcher = pDispatcher;
		this.iSessionProperties = (pProperties != null) ? pProperties : SessionProperties
				.getGlobalProperties();
	}

	public void close() {
		if (iDispatcher != null) iDispatcher.close();
	}

	public synchronized int getMsgID() {
		iMessageId++;
		if (iMessageId > 1000000) iMessageId = 0;
		return iMessageId;
	}

	public void handleContent(MessageReader pReader, MessageWriter pWriter, InetAddress pInetAddress)
			throws HttpException, IOException {
		CIMError error = null;
		// TODO validate CIMHeaders!
		HttpHeader inputHeader = pReader.getHeader();
		inputHeader = parseHeaders(inputHeader);

		String cimExport = inputHeader.getField("CIMExport");
		// String cimExportMethod = inputHeader.getField("CIMExportMethod");
		String cimOperation = inputHeader.getField("CIMOperation");
		String cimMethod = inputHeader.getField("CIMMethod");

		if (cimOperation != null && !"MethodCall".equalsIgnoreCase(cimOperation)) {
			pWriter.getHeader().addField("CIMError", "unsupported-operation");
			throw new HttpException(400, "Bad Request");
		}
		if (cimExport != null && !"MethodRequest".equalsIgnoreCase(cimExport)
				&& !"ExportMethodCall".equalsIgnoreCase(cimExport)) {
			pWriter.getHeader().addField("CIMError", "unsupported-operation");
			throw new HttpException(400, "Bad Request");
		}
		if (cimOperation == null && cimExport == null) {
			// TODO: verify the status returned by the server for this
			// situation, is not defined on the spec
			pWriter.getHeader().addField("CIMError", "unsupported-operation");
			throw new HttpException(400, "Bad Request");
		}

		CIMRequest request = null;
		CIMClientXML_HelperImpl xmlHelper = null;

		try {
			xmlHelper = new CIMClientXML_HelperImpl();
		} catch (ParserConfigurationException e1) {
			IOException e = new IOException("ParserConfigurationException");
			e.initCause(e1);
			throw e;
		}

		InputStream inputstream = null;
		if (iSessionProperties.isDebugInputStream()) inputstream = new DebugInputStream(pReader
				.getInputStream(), iSessionProperties.getDebugOutputStream());
		else inputstream = pReader.getInputStream();
		if (iSessionProperties.getXmlParser() == SessionProperties.DOM_PARSER) {

			Document doc = null;
			try {

				doc = xmlHelper.parse(new InputSource(new InputStreamReader(inputstream, pReader
						.getCharacterEncoding())));
				if (iSessionProperties.isDebugXMLInput()) CIMClientXML_HelperImpl.dumpDocument(doc); // debug

				Element rootE = doc.getDocumentElement();
				request = (CIMRequest) CIMXMLParserImpl.parseCIM(rootE);
			} catch (Exception e) {
				Logger logger = iSessionProperties.getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "exception while parsing the XML with DOM parser", e);
				}
				throw new HttpException(400, "Bad Request");
			}
		} else {
			XMLDefaultHandlerImpl hndlr = new XMLDefaultHandlerImpl(iSessionProperties
					.isDebugXMLInput());
			if (iSessionProperties.getXmlParser() == SessionProperties.SAX_PARSER) {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				try {
					SAXParser saxParser = factory.newSAXParser();
					saxParser.parse(new InputSource(new InputStreamReader(inputstream, pReader
							.getCharacterEncoding())), hndlr);
				} catch (Exception e) {
					Logger logger = iSessionProperties.getLogger();
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING,
								"exception while parsing the XML with XML parser", e);
					}
					throw new HttpException(400, "Bad Request");
				}

			} else {
				XMLPullParser pullParser = new XMLPullParser(new InputStreamReader(inputstream,
						pReader.getCharacterEncoding()));
				try {
					hndlr.parse(pullParser);
				} catch (Exception e) {
					Logger logger = iSessionProperties.getLogger();
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING,
								"exception while parsing the XML with PullBased parser", e);
					}
					throw new HttpException(400, "Bad Request");
				}
			}
			Vector o = hndlr.getObjects();
			request = (CIMRequest) o.elementAt(0);
		}
		if (request == null) throw new HttpException(400, "Bad Request");

		if ((cimExport == null && !cimMethod.equalsIgnoreCase("Indication"))
				|| !request.isCIMExport()) { throw new HttpException(400, "Bad Request"); }
		try {
			Vector paramValue = request.getParamValue();
			Iterator iter = paramValue.iterator();
			while (iter.hasNext()) {
				Object cimEvent = iter.next();
				if (cimEvent instanceof CIMInstance) {
					CIMInstance indicationInst = (CIMInstance) cimEvent;
					String id = pReader.getMethod().getFile();
					if (id != null && id.startsWith("/") && id.length() > 1
							&& !id.equalsIgnoreCase("/cimom")) {
						id = id.substring(1);
					} else {
						id = "0";
					}
					iDispatcher.dispatchEvent(new CIMEvent(indicationInst, id, pInetAddress));
				}
			}
		} catch (Exception e) {
			error = new CIMError(CIMException.getStatusCode(CIMException.CIM_ERR_FAILED));
		} catch (Throwable t) {
			error = new CIMError(new CIMException(CIMException.CIM_ERR_FAILED, t));
		}
		Document responseDoc = null;
		try {
			DocumentBuilder docBuilder = xmlHelper.getDocumentBuilder();
			responseDoc = docBuilder.newDocument();
			// ebak: [ 1656285 ] IndicationHandler does not accept non-Integer message ID
			CIMXMLBuilderImpl.createIndication_response(responseDoc, request.getId(), error);

		} catch (Exception e) {
			// TODO: check this error code, may not be appropiate
			throw new HttpException(400, "Bad Request");
		}
		CIMClientXML_HelperImpl.serialize(pWriter.getOutputStream(), responseDoc);
		pWriter.getHeader().addField("CIMExport", "MethodResponse");
	}

	private HttpHeader parseHeaders(HttpHeader pOriginalHeader) {
		String man = pOriginalHeader.getField("Man");
		String opt = pOriginalHeader.getField("Opt");
		HttpHeader headers = new HttpHeader();
		String ns = null;

		HttpHeaderParser manOptHeader = null;
		if (man != null && man.length() > 0) manOptHeader = new HttpHeaderParser(man);
		else if (opt != null && opt.length() > 0) manOptHeader = new HttpHeaderParser(opt);
		if (manOptHeader != null) ns = manOptHeader.findValue("ns");

		if (ns != null) {

			Iterator iter = pOriginalHeader.iterator();
			String key;
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				if (entry != null) {
					key = entry.getKey().toString();
					if (key.startsWith(ns + "-")) headers.addField(key.substring(3), entry
							.getValue().toString());
					else headers.addField(key, entry.getValue().toString());
				}
			}
		} else {
			headers = pOriginalHeader;
		}

		return headers;
	}

}
