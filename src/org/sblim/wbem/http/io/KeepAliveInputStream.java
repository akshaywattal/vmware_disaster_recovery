/**
 * KeepAliveInputStream.java
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

import org.sblim.wbem.http.HttpClient;

public class KeepAliveInputStream extends FilterInputStream {

	HttpClient iClient;

	public KeepAliveInputStream(InputStream is, HttpClient client) {
		super(is);
		iClient = client;
	}

	public int read() throws IOException {
		int i = super.read();
		if (i == -1 && iClient != null) {
			iClient.streamFinished();
			iClient = null;
		}
		return i;
	}

	public int read(byte buf[]) throws IOException {
		return read(buf, 0, buf.length);
	}

	public int read(byte buf[], int off, int len) throws IOException {
		int i = super.read(buf, off, len);
		if (i == -1 && iClient != null) {
			iClient.streamFinished();
			iClient = null;
		}
		return i;
	}

	public long skip(long len) throws IOException {
		long i = super.skip(len);

		if (i == -1 && iClient != null) {
			iClient.streamFinished();
			iClient = null;
		}
		return i;
	}

	public void close() throws IOException {
		super.close();
		if (iClient != null) {
			iClient.streamFinished();
			iClient = null;
		}
	}
}
