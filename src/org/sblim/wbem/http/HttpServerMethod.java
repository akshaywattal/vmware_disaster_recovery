/**
 * HttpServerMethod.java
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

public class HttpServerMethod extends HttpMethod {

	private String iMethodName;

	private String iFile;

	private String iProtocol;

	private int iMinor, iMajor;

	private int iStatus;

	private String iReason;

	public HttpServerMethod(int major, int minor, int status, String reason) {
		iMinor = minor;
		iMajor = major;
		iStatus = status;
		iReason = reason;
	}

	public HttpServerMethod(InputStream reader) throws IOException, HttpException {
		String line;

		do {
			line = readLine(reader);
		} while (line == null || line.length() == 0);
		int next = line.indexOf(' ');
		int prev = 0;
		if (next > -1) {
			iMethodName = line.substring(0, next).toUpperCase();
			if (iMethodName.equals("GET") && (line.indexOf(' ', next + 1) == -1)) { // Simple
																					// request
				iFile = line.substring(next + 1);
			} else { // FullRequest
				prev = next + 1;
				next = line.indexOf(' ', prev);
				iFile = line.substring(prev, next);

				prev = next + 1;
				iProtocol = line.substring(prev).toUpperCase();

				prev = iProtocol.indexOf('/');
				next = iProtocol.indexOf('.', prev + 1);
				try {
					iMajor = Integer.parseInt(iProtocol.substring(prev + 1, next));
					iMinor = Integer.parseInt(iProtocol.substring(next + 1));
				} catch (Exception e) {
					throw new HttpException(HttpURLConnection.HTTP_BAD_METHOD, "Bad method");
				}
			}
		} else throw new HttpException(HttpURLConnection.HTTP_BAD_METHOD, "Bad method");
	}

	public int getMajorVersion() {
		return iMajor;
	}

	public int getMinorVersion() {
		return iMinor;
	}

	public String getMethodName() {
		return iMethodName;
	}

	public String getFile() {
		return iFile;
	}

	public void write(ASCIIPrintStream dos) throws IOException {
		dos.print("HTTP/" + iMajor + "." + iMinor + " " + iStatus + " " + iReason + "\r\n");
	}
}
