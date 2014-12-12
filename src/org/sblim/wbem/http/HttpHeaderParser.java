/**
 * HttpHeaderParser.java
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

import java.util.Vector;

public class HttpHeaderParser {

	String iRaw;

	Vector iNameValuePair;

	public HttpHeaderParser(String s) {
		iRaw = s;
		iNameValuePair = new Vector();
		if (iRaw != null) {
			iRaw = iRaw.trim();
			char charArray[] = iRaw.toCharArray();
			int startPosValue = 0;
			int currentPos = 0;
			boolean isName = true;
			boolean withinQuote = false;
			int len = charArray.length;
			String nvp[] = new String[2];
			do {
				char c = charArray[currentPos];
				if (c == '=') {
					nvp[0] = (new String(charArray, startPosValue, currentPos - startPosValue))
							.toLowerCase();
					isName = false;
					startPosValue = ++currentPos;
				} else if (c == '"') {
					if (withinQuote) {
						nvp[1] = new String(charArray, startPosValue, currentPos - startPosValue);
						iNameValuePair.add(nvp);
						nvp = new String[2];
						withinQuote = false;
						while (++currentPos < len // ignore spaces and
													// preceding comma
								&& (charArray[currentPos] == ' ' || charArray[currentPos] == ',' || charArray[currentPos] == ';'))
							;
						isName = true;
						startPosValue = currentPos;
					} else {
						withinQuote = true;
						startPosValue = ++currentPos;
					}
				} else if (c == ' ' || c == ',' || c == ';') {
					if (withinQuote) {
						currentPos++;
					} else {
						if (isName) {
							nvp[0] = (new String(charArray, startPosValue, currentPos
									- startPosValue)).toLowerCase();
							iNameValuePair.add(nvp);
							nvp = new String[2];
						} else {
							nvp[1] = new String(charArray, startPosValue, currentPos
									- startPosValue);
							iNameValuePair.add(nvp);
							nvp = new String[2];
						}

						while (++currentPos < len // ignore spaces and
													// preceding comma
								&& (charArray[currentPos] == ' ' || charArray[currentPos] == ',' || charArray[currentPos] == ';'))
							;
						isName = true;
						startPosValue = currentPos;
					}
				} else {
					currentPos++;
				}
			} while (currentPos < len);

			if (--currentPos > startPosValue) {
				if (!isName) {
					if (charArray[currentPos] == '"') {
						nvp[1] = new String(charArray, startPosValue, currentPos - startPosValue);
						iNameValuePair.add(nvp);
						nvp = new String[2];
					} else {
						nvp[1] = new String(charArray, startPosValue,
								(currentPos - startPosValue) + 1);
						iNameValuePair.add(nvp);
						nvp = new String[2];
					}
				} else {
					nvp[0] = (new String(charArray, startPosValue, (currentPos - startPosValue) + 1))
							.toLowerCase();
				}
				iNameValuePair.add(nvp);
			} else if (currentPos == startPosValue) {
				if (!isName) {
					if (charArray[currentPos] == '"') {
						nvp[1] = String.valueOf(charArray[currentPos - 1]);
						iNameValuePair.add(nvp);
						nvp = new String[2];
					} else {
						nvp[1] = String.valueOf(charArray[currentPos]);
						iNameValuePair.add(nvp);
						nvp = new String[2];
					}
				} else {
					nvp[0] = String.valueOf(charArray[currentPos]).toLowerCase();
				}
				iNameValuePair.add(nvp);
			}
		}
	}

	public String findKey(int i) {
		if (i < 0 || i > iNameValuePair.size()) return null;
		return ((String[]) iNameValuePair.elementAt(i))[0];
	}

	public String findValue(int i) {
		if (i < 0 || i > iNameValuePair.size()) return null;
		return ((String[]) iNameValuePair.elementAt(i))[1];
	}

	public String findValue(String s) {
		return findValue(s, null);
	}

	public String findValue(String name, String defaultValue) {
		if (name == null) return defaultValue;
		name.toLowerCase();
		for (int i = 0; i < iNameValuePair.size(); i++) {
			if (((String[]) iNameValuePair.elementAt(i))[0] == null) return defaultValue;
			if (name.equals(((String[]) iNameValuePair.elementAt(i))[0])) return ((String[]) iNameValuePair
					.elementAt(i))[1];
		}
		return defaultValue;
	}

	public int findInt(String name, int defaultValue) {
		try {
			return Integer.parseInt(findValue(name, String.valueOf(defaultValue)));
		} catch (Throwable throwable) {
			return defaultValue;
		}
	}

	public String toString() {
		return "raw:" + iRaw;
	}
}
