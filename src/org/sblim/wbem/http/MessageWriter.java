/**
 * MessageWriter.java
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
 *   17620    2005-06-29  thschaef     eliminate ASCIIPrintStream1 in import statement       
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.sblim.wbem.http.io.ASCIIPrintStream;
import org.sblim.wbem.http.io.ChunkedOutputStream;
import org.sblim.wbem.http.io.PersistentOutputStream;

public class MessageWriter {

	HttpHeader iHeader = null;

	HttpServerMethod iMethod = null;

	HttpHeader iTrailer = null;

	boolean iChunked = false;

	boolean iPersistent = false;

	ASCIIPrintStream iRealOS;

	ASCIIPrintStream iClientOS;

	ByteArrayOutputStream iBufferedOS;

	public MessageWriter(OutputStream ros, boolean persistent, boolean chunked) {
		this.iRealOS = new ASCIIPrintStream(ros);
		this.iChunked = chunked;
		this.iPersistent = persistent;
		this.iBufferedOS = new ByteArrayOutputStream();
		if (chunked) {
			this.iClientOS = new ASCIIPrintStream(new ChunkedOutputStream(
					new PersistentOutputStream(iBufferedOS, persistent), 512));
		} else {
			this.iClientOS = new ASCIIPrintStream(new PersistentOutputStream(iBufferedOS,
					persistent));
		}
		iHeader = new HttpHeader();
		iMethod = new HttpServerMethod(HttpConnectionHandler.MAJOR_VERSION,
				HttpConnectionHandler.MINOR_VERSION, 200, "OK");
	}

	public void reset() {
		iBufferedOS.reset();
	}

	public void setHeader(HttpHeader header) {
		this.iHeader = header;
	}

	public void setMethod(HttpServerMethod method) {
		this.iMethod = method;
	}

	public HttpHeader getHeader() {
		return this.iHeader;
	}

	public HttpServerMethod getMethod() {
		return this.iMethod;
	}

	public ASCIIPrintStream getOutputStream() {
		return iClientOS;
	}

	public void close() throws IOException {
		iMethod.write(iRealOS);
		iRealOS.flush();
		if (!iChunked) iHeader.removeField("Transfer-Encoding");
		else iHeader.addField("Transfer-Encoding", "chunked");
		if (iPersistent) iHeader.addField("Connection", "Keep-alive");
		else iHeader.addField("Connection", "close");

		iHeader.addField("Content-Type", "application/xml;charset=\"utf-8\"");
		if (!iChunked) iHeader.addField("Content-length", Integer.toString(iBufferedOS.size()));
		iHeader.write(iRealOS);
		iRealOS.flush();
		iBufferedOS.writeTo(iRealOS);
		if (iChunked && (iTrailer != null)) {
			iTrailer.write(iRealOS);
		}
		iRealOS.flush();
	}

	public void setTrailer(HttpHeader trailer) {
		this.iTrailer = trailer;
	}
}
