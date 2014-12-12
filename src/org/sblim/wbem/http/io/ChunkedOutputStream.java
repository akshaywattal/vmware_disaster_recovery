/**
 * ChunkedOutputStream.java
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

import java.io.*;

public class ChunkedOutputStream extends OutputStream {

	DataOutputStream iOs;

	byte[] iBuffer;

	int iUsed;

	public ChunkedOutputStream(OutputStream os, int buflen) {
		iOs = new DataOutputStream(os);
		iBuffer = new byte[buflen];
		iUsed = 0;
	}

	public void close() throws IOException {
		flush();
		iOs.writeChars("0\r\n");
		iOs.flush();
	}

	public void flush() throws IOException {
		if (iUsed > 0) {
			iOs.writeBytes(Integer.toHexString(iUsed) + "\r\n");
			iOs.write(iBuffer, 0, iUsed);
			iOs.writeBytes("\r\n");
			iOs.flush();
		}
		iUsed = 0;
	}

	public void write(byte source[], int offset, int len) throws IOException {
		int copied = 0;
		while (len > 0) {
			int total = (iBuffer.length - iUsed < len) ? (iBuffer.length - iUsed) : len;
			if (total > 0) {
				System.arraycopy(source, copied, iBuffer, iUsed, total);
				len -= total;
				iUsed += total;
				copied += total;
			}
			if (iUsed == iBuffer.length) flush();
		}
	}

	public void write(int i) throws IOException {
		if (iBuffer.length == iUsed) flush();
		iBuffer[iUsed++] = (byte) (0xFF & i);
	}

}
