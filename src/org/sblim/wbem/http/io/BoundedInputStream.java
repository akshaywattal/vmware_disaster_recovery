/**
 * BoundedInputStream.java
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
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.http.io;

import java.io.*;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class BoundedInputStream extends FilterInputStream {
	protected long maxLen, used;
	protected boolean closed = false;
	public BoundedInputStream(InputStream is) {
		this(is,-1);
	}
	public BoundedInputStream(InputStream is, long len) {
		super(is);
		this.maxLen = len;
		this.used = 0;
		this.in = is;
	}
	public int read() throws IOException {
		if (maxLen > -1) {
			if (used >= maxLen) return -1;
			
			int value = in.read();
			if (value > -1)	used++;
			return value;
		}
		return in.read();
	}
	public int read(byte buf[]) throws IOException {
		return read(buf, 0, buf.length);
	}
	public int read(byte buf[], int off, int len) throws IOException {
		if (closed) throw new IOException("I/O error - the stream is closed");
		
		if (maxLen > -1) {
			if (used >= maxLen) return -1;
			
			long min = ((used+len)>maxLen)? maxLen-used:len;
			int total = in.read(buf, off, (int)min);
			if (total > -1) used += total;
			return total;
		}
		return in.read(buf, off, len);
	}
	public long skip(long len) throws IOException {
		if (maxLen > -1) {
			if (len >= 0) {
				long min = ((used+len)>maxLen)? maxLen-used:len;
				long total = in.skip(min);
				if (total > -1) {
					used += total;
				}
				return total;
			}
			return -1;
		}
		return in.skip(len);
	}
	public int available() throws IOException {
		if (maxLen > -1) {
			return (int)(maxLen-used);
		}
		return in.available();
	}
	public synchronized void close() throws IOException {
		if (maxLen > -1) {
			if (!closed) {
				byte[] buf = new byte[512];
				while (read(buf, 0, buf.length) > -1) {				
				}
				closed = true;
			}
//			else
//				throw new IOException();
		} else {
			in.close();
		}
	}
}
