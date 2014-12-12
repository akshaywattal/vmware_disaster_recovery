/**
 * DebugInputStream.java
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
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */
package org.sblim.wbem.http.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DebugInputStream extends FilterInputStream {

	private byte[] iBuf;

	private boolean iBuffered;

	private int iCur = 0;

	private int iMaxLen = 0;

	private OutputStream iBut;

	public DebugInputStream(InputStream is, OutputStream os) {
		super(is);
		iBuf = new byte[512];
		iBuffered = false;
		iBut = os;
	}

	private void buffer() throws IOException {
		iBuffered = true;
		int total;
		while ((total = in.read(iBuf, iMaxLen, iBuf.length - iMaxLen)) > -1) {
			iMaxLen += total;
			if (iMaxLen == iBuf.length) {
				byte b[] = new byte[iBuf.length << 1];
				System.arraycopy(iBuf, 0, b, 0, iBuf.length);
				iBuf = b;
			}
		}
		if (iBut != null) {
			iBut.write("-----------------------------\n".getBytes());
			iBut.write("Dumping raw data from stream:\n".getBytes());
			iBut.write(iBuf, 0, iMaxLen);
			iBut.write("\n".getBytes());
			iBut.write("-----------------------------\n".getBytes());
		}
	}

	public synchronized int read() throws IOException {
		if (!iBuffered) buffer();

		if (iCur >= iMaxLen) return -1;
		return iBuf[iCur++];
	}

	public synchronized int read(byte b[], int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length)
				|| ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) { return 0; }

		int c = read();
		if (c == -1) { return -1; }
		b[off] = (byte) c;

		int i = 1;
		for (; i < len; i++) {
			c = read();
			if (c == -1) {
				break;
			}
			if (b != null) {
				b[off + i] = (byte) c;
			}
		}
		return i;
	}
}
