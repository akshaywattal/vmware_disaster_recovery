/**
 * (C) Copyright IBM Corp. 2006, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author : Endre Bak, ebak@de.ibm.com
 * 
 * Flag       Date        Prog         Description
 * -------------------------------------------------------------------------------
 * 1565892    2006-10-18  ebak         Make SBLIM client JSR48 compliant
 * 2003590    2008-06-30  blaschke-oss Change licensing from CPL to EPL
 * 2524131    2009-01-21  raman_arora  Upgrade client to JDK 1.5 (Phase 1)
 */

package org.sblim.cimclient.internal.util;

/**
 * Class Util is responsible for storing commonly used static methods.
 */
public class Util {

	/**
	 * Quotes the passed string.
	 * 
	 * @param pStr
	 * @return the quoted string
	 */
	public static String quote(String pStr) {
		StringBuffer dstBuf = new StringBuffer();
		dstBuf.append('"');
		for (int i = 0; i < pStr.length(); i++) {
			char ch = pStr.charAt(i);
			if (ch == '\\' || ch == '"') dstBuf.append('\\');
			dstBuf.append(ch);
		}
		dstBuf.append('"');
		return dstBuf.toString();
	}

}
