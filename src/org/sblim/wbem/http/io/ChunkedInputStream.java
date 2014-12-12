/**
 * ChunkedInputStream.java
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
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 *   13799    2004-12-07  thschaef     Fixes on chunking
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1660568    2007-02-15  lupusalex    Chunking broken on SUN JRE
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */
package org.sblim.wbem.http.io;

import java.io.*;

import org.sblim.wbem.http.HttpHeader;
import org.sblim.wbem.http.HttpMethod;

public class ChunkedInputStream extends InputStream {

	private InputStream iIn;

	// private int iAvail = 0;
	private long iChunkSize = 0;

	private boolean iEof = false;

	private HttpHeader iTrailers = new HttpHeader();

	private boolean iClosed = false;

	private byte[] iTmp = new byte[1];

	public ChunkedInputStream(InputStream is) {
		iIn = is;
	}

	public synchronized int read() throws IOException {
		return (read(iTmp, 0, 1) > 0) ? (iTmp[0] & 0xFF) : -1;
	}

	public synchronized int read(byte[] buf, int off, int len) throws IOException {
		int total = 0;
		if (iEof || iClosed) return -1; // 13799 (return -1 if closed, not if
										// !closed)

		if (iChunkSize == 0) {
			String line = HttpMethod.readLine(iIn); // TODO read line using
													// valid character encoding

			if ("".equals(line)) // 13799 The chunked data is ending with
									// CRLF, so the first line read after it
									// results ""
			line = HttpMethod.readLine(iIn); // 13799 Except first chunk, the
												// above only read the CRLF !
												// TODO - get rid of ";*" suffix
			try {
				iChunkSize = Long.parseLong(line, 16);
			} catch (Exception e) {
				iEof = true;
				throw new IOException("Invalid chunk size");
			}
		}
		if (iChunkSize > 0) {
			total = iIn.read(buf, off, (iChunkSize < len) ? (int) iChunkSize : (int) len);
			if (total > 0) {
				iChunkSize -= total;
			}
			if (total == -1) throw new EOFException("Unexpected EOF");
		} else {
			// read trailer
			iEof = true;
			iTrailers = new HttpHeader(iIn);
		}
		return total > 0 ? total : -1;
	}

	public synchronized HttpHeader getTrailers() {
		return iTrailers;
	}

	public synchronized long skip(long total) throws IOException {
		byte[] tmp = new byte[(int) total];
		return read(tmp, 0, (int) total);
	}

	public synchronized int available() throws IOException {
		return (iEof ? 0 : (iChunkSize > 0 ? (int) iChunkSize : 1));
	}

	public void close() throws IOException {
		if (!iClosed) {
			iClosed = true;
			byte[] buf = new byte[512];
			while (read(buf, 0, buf.length) > -1) {

			}
			iIn.close();
		} else throw new IOException("Error while closing stream");
	}
}
