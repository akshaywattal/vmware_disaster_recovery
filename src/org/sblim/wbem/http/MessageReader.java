/**
 * MessageReader.java
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
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-17  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.http.io.BoundedInputStream;
import org.sblim.wbem.http.io.ChunkedInputStream;
import org.sblim.wbem.http.io.PersistentInputStream;
import org.sblim.wbem.util.SessionProperties;

public class MessageReader {

	HttpHeader iHeader;

	HttpServerMethod iMethod;

	private boolean iChunked = false;

	private String iEncoding = "UTF-8";

	InputStream iContent;

	public MessageReader(InputStream reader) throws IOException, HttpException {
		iMethod = new HttpServerMethod(reader);
		iHeader = new HttpHeader(reader);

		// TODO: Add support for chunked, and bounded input stream
		String encoding = iHeader.getField("Transfer-Encoding");
		if ((encoding != null) && (encoding.toLowerCase().endsWith("chunked"))) {
			iChunked = true;
		}
		String length = iHeader.getField("Content-Length");
		int contentLength = -1;
		if (length != null) {
			try {
				contentLength = Integer.parseInt(length);
			} catch (Exception e) {
				contentLength = -1;

				Logger logger = SessionProperties.getGlobalProperties().getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "invalid content length format from the response", e);
				}
			}
		}
		String contentType = iHeader.getField("Content-Type");
		if (contentType != null) {
			try {
				HttpHeaderParser contentTypeHeader = new HttpHeaderParser(contentType);
				encoding = contentTypeHeader.findValue("charset");
			} catch (Exception e) {
				encoding = "UTF-8"; // TODO is this the default character
									// encoding?
				Logger logger = SessionProperties.getGlobalProperties().getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "invalid content type format from the response", e);
				}
			}
		}
		if (iChunked) {
			iContent = new ChunkedInputStream(new PersistentInputStream(reader,
					isPersistentConnectionSupported()));
		} else {
			iContent = new BoundedInputStream(new PersistentInputStream(reader,
					isPersistentConnectionSupported()), contentLength);
		}
	}

	public String getCharacterEncoding() {
		return iEncoding;
	}

	public HttpHeader getHeader() {
		return iHeader;
	}

	public HttpServerMethod getMethod() {
		return iMethod;
	}

	public InputStream getInputStream() {
		return iContent;
	}

	public boolean isPersistentConnectionSupported() {
		String conn = iHeader.getField("Connection");
		if (conn != null) {
			if (conn.equalsIgnoreCase("close")) return false;
			if (conn.equalsIgnoreCase("Keep-Alive")) return true;
		}

		return ((iMethod.getMajorVersion() >= 1) && (iMethod.getMinorVersion() >= 1));
	}

	public boolean isChunkSupported() {
		// TODO: make sure this is the correct way to test for chunk support
		if ((iMethod.getMajorVersion() >= 1) && (iMethod.getMinorVersion() >= 1)) {
			String TE;
			if ((TE = iHeader.getField("TE")) != null && (TE.equalsIgnoreCase("trailers"))) { return true; }
		}
		return false;
	}

	public void close() throws IOException {
		iContent.close();
	}
}
