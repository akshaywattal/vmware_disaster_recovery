/**
 * Challenge.java
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

package org.sblim.wbem.http;

import java.util.Vector;

/**
 * @author Roberto
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Challenge {
	
	public String scheme;
	public HttpHeader params;
	
	
	private Challenge() {
	}
	
	public HttpHeader getParams() {
		return params;
	}
	
	public String getScheme() {
		return scheme;
	}
	public String getRealm() {
		return params.getField("realm");
	}
	
	public static Challenge[] parseChallenge(String line) throws HttpParseException {
		Vector challeges = new Vector();
		if (line == null || line.length() == 0) throw new IllegalArgumentException("Invalid challenge");
		Challenge challenge = new Challenge();
		challenge.params = new HttpHeader();
		
		char buf[] = line.toCharArray();
		try {
			int start = 0, end = 0;
			while (true) {
				start = skipSpaces(buf, start);
				end = findEndOfToken(buf, start);
				String scheme = line.substring(start, end);
				challenge.scheme = scheme;
				start = end;
				boolean skipComma = true; 
				while (true) {
					if (!skipComma) {
						start = skipSpaces(buf, start);
						if (start >= buf.length || buf[start] != ',') {
							break;
						} 
					}
					start = skipSpaces(buf, start+1);
					if (start >= buf.length) break;

					end = findEndOfToken(buf, start);
					String paramname = line.substring(start, end);
					start = end;
					if (start >= buf.length) break;
					
					start = skipSpaces(buf, start);
					if (start >= buf.length) break;

					if (buf[start] != '=') throw new HttpParseException("Invalid challenge");
					if (start+1 >= buf.length) break;
					start = skipSpaces(buf, start+1);

					if (start >= buf.length) break;
					end = findEndOfToken(buf, start);
					String value = line.substring(start, end);
					start = end;
					
					if (value.startsWith("\"") && value.endsWith("\"") && value.length()>1)
						challenge.params.addField(paramname, value.substring(1,value.length()-1));
					else
						challenge.params.addField(paramname, value);
					skipComma = false;
				}
//				if (challenge.params.getField("realm") == null)
//					challenge.params.addField("realm", "");

				challeges.add(challenge);				
				if (start >= buf.length) break;
			}	
					
		} catch (HttpParseException e) {
			throw e;
		} catch (Exception e) {
			throw new HttpParseException("Invalid challenge");
		}
		
		return (Challenge[]) challeges.toArray(new Challenge[challeges.size()]);
	}
	
	private static int skipSpaces(char[] buf, int pos) {
		while (pos < buf.length && Character.isSpaceChar(buf[pos])) 
			pos++;
		return pos;
	}

	private static int findEndOfToken(char[] buf, int pos) {
		if (buf[pos] == '\"') {
			do {
				pos++;
			} while (buf[pos] != '\"' && pos < buf.length);
			pos++;
		}
		else {
			while (pos < buf.length && (!Character.isSpaceChar(buf[pos]) && !(buf[pos]== ',') && !(buf[pos] == '='))) 
				pos++;
		}
		return pos;
	}
	
}