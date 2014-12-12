/**
 * HttpClientMethod.java
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

package org.sblim.wbem.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.sblim.wbem.http.io.ASCIIPrintStream;

public class HttpClientMethod extends HttpMethod {

	private boolean iIncomming = true;

	private String iHttpHeader;

	private int iMinor, iMajor;

	private int iStatus;

	private String iMethod;

	private String iRequest;

	private String iResponse;

	public HttpClientMethod(String method, String request, int major, int minor) {
		iMinor = minor;
		iMajor = major;
		iRequest = request;
		iMethod = method;
		iIncomming = false;
	}

	public HttpClientMethod(InputStream reader) throws IOException {
		String line = null;

		do {
			line = readLine(reader);
		} while (line == null || line.length() == 0);
		int rqt = line.indexOf(' ');
		int prev = 0;
		if (rqt > -1) { // Parse the header
			int next = rqt;
			iHttpHeader = line.substring(prev, next).toUpperCase();

			prev = iHttpHeader.indexOf('/');
			if (prev > 0 && iHttpHeader.substring(0, prev).equalsIgnoreCase("HTTP")) {
				next = iHttpHeader.indexOf('.', prev + 1);
				try {
					iMajor = Integer.parseInt(iHttpHeader.substring(prev + 1, next));
					iMinor = Integer.parseInt(iHttpHeader.substring(next + 1));
				} catch (Exception e) {
					throw new HttpException(HttpURLConnection.HTTP_BAD_METHOD, "Bad method");
				}
				prev = rqt;
				rqt = line.indexOf(' ', prev + 1);
				if (rqt > -1) {
					try {
						iStatus = Integer.parseInt(line.substring(prev + 1, rqt));
					} catch (Exception e) {
						throw new HttpException(HttpURLConnection.HTTP_BAD_METHOD, "Bad method");
					}
					iResponse = line.substring(rqt + 1);
					return;
				}
			} else throw new HttpException(HttpURLConnection.HTTP_BAD_METHOD, "Bad method");
		}
		throw new HttpException(HttpURLConnection.HTTP_BAD_METHOD, "Bad method");
	}

	public int getMajorVersion() {
		return iMajor;
	}

	public int getMinorVersion() {
		return iMinor;
	}

	public String getMethodName() {
		return iMethod;
	}

	public int getStatus() {
		return iStatus;
	}

	public void write(ASCIIPrintStream dos) throws IOException {
		dos.print(iMethod + " " + iRequest + " HTTP/" + iMajor + "." + iMinor + "\r\n");
	}

	public String getResponseMessage() {
		return iResponse;
	}

	public String toString() {
		if (iIncomming) { return iHttpHeader + " " + iStatus + " " + iResponse; }
		return iMethod + " " + iRequest + " HTTP/" + iMajor + "." + iMinor;
	}
}
