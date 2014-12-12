/**
 * (C) Copyright IBM Corp. 2005, 2010
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
 * Flag     Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1565892    2006-11-28  lupusalex    Make SBLIM client JSR48 compliant
 * 2003590    2008-06-30  blaschke-oss Change licensing from CPL to EPL
 * 2524131    2009-01-21  raman_arora  Upgrade client to JDK 1.5 (Phase 1)
 * 3027479    2010-07-09  blaschke-oss Dead store to local variable
 */

package org.sblim.cimclient.internal.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.sblim.cimclient.internal.http.io.BoundedInputStream;
import org.sblim.cimclient.internal.http.io.ChunkedInputStream;
import org.sblim.cimclient.internal.http.io.PersistentInputStream;
import org.sblim.cimclient.internal.logging.LogAndTraceBroker;

/**
 * Class MessageReader is responsible for reading http messages
 * 
 */
public class MessageReader {

	HttpHeader iHeader;

	HttpServerMethod iMethod;

	private boolean iChunked = false;

	private String iEncoding = "UTF-8";

	InputStream iContent;

	/**
	 * Ctor.
	 * 
	 * @param pStream
	 *            The input stream
	 * @throws IOException
	 * @throws HttpException
	 */
	public MessageReader(InputStream pStream) throws IOException, HttpException {
		this.iMethod = new HttpServerMethod(pStream);
		this.iHeader = new HttpHeader(pStream);

		// TODO: Add support for chunked, and bounded input stream
		String encoding = this.iHeader.getField("Transfer-Encoding");
		if ((encoding != null) && (encoding.toLowerCase().endsWith("chunked"))) {
			this.iChunked = true;
		}
		String length = this.iHeader.getField("Content-Length");
		int contentLength = -1;
		if (length != null) {
			try {
				contentLength = Integer.parseInt(length);
			} catch (Exception e) {
				contentLength = -1;

				LogAndTraceBroker logger = LogAndTraceBroker.getBroker();
				logger.trace(Level.FINER, "Exception while parsing http content-length", e);
			}
		}
		String contentType = this.iHeader.getField("Content-Type");
		if (contentType != null) {
			try {
				HttpHeaderParser contentTypeHeader = new HttpHeaderParser(contentType);
				encoding = contentTypeHeader.getValue("charset");
			} catch (Exception e) {
				encoding = "UTF-8"; // TODO is this the default character
				// encoding?
				LogAndTraceBroker logger = LogAndTraceBroker.getBroker();
				logger.trace(Level.FINER, "Exception while parsing http content-type", e);
			}
			this.iEncoding = encoding;
		}
		if (this.iChunked) {
			this.iContent = new ChunkedInputStream(new PersistentInputStream(pStream,
					isPersistentConnectionSupported()));
		} else {
			this.iContent = new BoundedInputStream(new PersistentInputStream(pStream,
					isPersistentConnectionSupported()), contentLength);
		}
	}

	/**
	 * Returns the character encoding
	 * 
	 * @return The character encoding
	 */
	public String getCharacterEncoding() {
		return this.iEncoding;
	}

	/**
	 * Returns the http header
	 * 
	 * @return The http header
	 */
	public HttpHeader getHeader() {
		return this.iHeader;
	}

	/**
	 * Returns the http server method
	 * 
	 * @return The http server method
	 */
	public HttpServerMethod getMethod() {
		return this.iMethod;
	}

	/**
	 * Returns the input stream
	 * 
	 * @return The input stream
	 */
	public InputStream getInputStream() {
		return this.iContent;
	}

	/**
	 * Returns the persistent connection support state
	 * 
	 * @return <code>true</code> if persistent connection is supported
	 */
	public boolean isPersistentConnectionSupported() {
		String conn = this.iHeader.getField("Connection");
		if (conn != null) {
			if (conn.equalsIgnoreCase("close")) return false;
			if (conn.equalsIgnoreCase("Keep-Alive")) return true;
		}

		return ((this.iMethod.getMajorVersion() >= 1) && (this.iMethod.getMinorVersion() >= 1));
	}

	/**
	 * Returns the chunking support state
	 * 
	 * @return <code>true</code> if chunking is supported
	 */
	public boolean isChunkSupported() {
		// TODO: make sure this is the correct way to test for chunk support
		if ((this.iMethod.getMajorVersion() >= 1) && (this.iMethod.getMinorVersion() >= 1)) {
			String TE;
			if ((TE = this.iHeader.getField("TE")) != null && (TE.equalsIgnoreCase("trailers"))) { return true; }
		}
		return false;
	}

	/**
	 * Closes the stream
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		this.iContent.close();
	}
}
