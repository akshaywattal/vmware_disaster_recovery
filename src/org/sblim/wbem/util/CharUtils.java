/**
 * CharUtils.java
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
package org.sblim.wbem.util;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CharUtils {
	public static String escape(String str) {

		char[] buf = str.toCharArray();
		char[] out = new char[buf.length*6+1];

		int size = buf.length;
		int pos = 0;
		for (int i = 0; i < size; i++) {
			char ch = buf[i];

			switch (ch) {
				case 0: continue;
				case '\n':
					out[pos++] = '\\';
					out[pos++] = 'n';
					break;
				case '\"':
					out[pos++] = '\\';
					out[pos++] = '"';
					break;
				case '\'':
					out[pos++] = '\\';
					out[pos++] = '\'';
					break;
				case '\f':
					out[pos++] = '\\';
					out[pos++] = 'f';
					break;
				case '\\':
					out[pos++] = '\\';
					out[pos++] = '\\';
					break;
				case '\r':
					out[pos++] = '\\';
					out[pos++] = 'r';
					break;
				case '\b':
					out[pos++] = '\\';
					out[pos++] = 'b';
					break;
				case '\t':	
					out[pos++] = '\\';
					out[pos++] = 't';
					break;
				default:
					if (ch < 0x20 || ch > 0x7E) {
						String hex = Integer.toString(ch, 16);
						int len = hex.length();
						out[pos++] = '\\';			
						out[pos++] = 'x';
						if (len < 4) out[pos++] = '0';
						if (len < 3) out[pos++] = '0';
						if (len < 2) out[pos++] = '0';
						for (int m=0; m<len; m++)
							out[pos++] = hex.charAt(m);
					}
					else {
						out[pos++] = ch;
					}
			}
		}
		return String.valueOf(out,0, pos);
	}
}
