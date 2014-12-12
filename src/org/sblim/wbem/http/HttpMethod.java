/**
 * HttpMethod.java
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
 *   12917    2004-11-11  thschaef     HTTP Header parsing does not work for UNIX line delimitor
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.http;

import java.io.IOException;
import java.io.InputStream;

public class HttpMethod {

	public static String readLine(InputStream inputstream) throws IOException {

		if (inputstream == null) return null;
		
		char buffer[] = new char[16];
		int used = 0;
		int prevChar;
		boolean flag = true;

		for (; (prevChar = inputstream.read()) >= 0; buffer[used++] = (char) prevChar) {

			flag = false;

			// @12917 - thschaef
			// see http://www.w3.org/Protocols/HTTP/AsImplemented.html for
			// details
			// Lines shall be delimited by an optional carriage return followed
			// by a mandatory line feed chararcter.
			// The client should not assume that the carriage return will be
			// present. Lines may be of any length.
			// Well-behaved servers should retrict line length to 80 characters
			// excluding the CR LF pair.

			// if (prevChar == 13) continue;
			if (prevChar == 10) break;

			if (used >= buffer.length) {
				char tmp[] = new char[buffer.length << 1];
				System.arraycopy(buffer, 0, tmp, 0, used);
				buffer = tmp;
			}
		}
		if (flag) throw new IOException("Unexpected EOF");

		for (; used > 0 && buffer[used - 1] <= ' '; used--)
			;

		return String.copyValueOf(buffer, 0, used);
	}
}
