/**
 * (C) Copyright IBM Corp. 2005, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * @author : Roberto Pineiro, IBM, roberto.pineiro@us.ibm.com  
 * @author : Chung-hao Tan, IBM, chungtan@us.ibm.com
 * 
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1565892    2006-11-28  lupusalex    Make SBLIM client JSR48 compliant
 * 2003590    2008-06-30  blaschke-oss Change licensing from CPL to EPL
 * 2524131    2009-01-21  raman_arora  Upgrade client to JDK 1.5 (Phase 1)
 * 2531371    2009-02-10  raman_arora  Upgrade client to JDK 1.5 (Phase 2) 
 */

package org.sblim.cimclient.internal.http;

import java.util.Vector;

/**
 * Class Challenge holds a http authentication challenge
 * 
 */
public class Challenge {

	private String iScheme;

	private HttpHeader iParams;

	private Challenge() {
	// hidden ctor
	}

	/**
	 * Returns the parameters
	 * 
	 * @return The parameters
	 */
	public HttpHeader getParams() {
		return this.iParams;
	}

	/**
	 * Returns the scheme
	 * 
	 * @return The scheme
	 */
	public String getScheme() {
		return this.iScheme;
	}

	/**
	 * Returns the realm
	 * 
	 * @return The realm
	 */
	public String getRealm() {
		return this.iParams.getField("realm");
	}

	/**
	 * Parses the challenge as received from the host
	 * 
	 * @param pLine
	 *            The challenge string
	 * @return The parsed challenge
	 * @throws HttpParseException
	 *             If the challenge string is ill-formed
	 */
	public static Challenge[] parseChallenge(String pLine) throws HttpParseException {
		Vector<Challenge> challeges = new Vector<Challenge>();
		if (pLine == null || pLine.length() == 0) throw new IllegalArgumentException(
				"Invalid challenge");
		Challenge challenge = new Challenge();
		challenge.iParams = new HttpHeader();

		char buf[] = pLine.toCharArray();
		try {
			int start = 0, end = 0;
			while (true) {
				start = skipSpaces(buf, start);
				end = findEndOfToken(buf, start);
				String scheme = pLine.substring(start, end);
				challenge.iScheme = scheme;
				start = end;
				boolean skipComma = true;
				while (true) {
					if (!skipComma) {
						start = skipSpaces(buf, start);
						if (start >= buf.length || buf[start] != ',') {
							break;
						}
					}
					start = skipSpaces(buf, start + 1);
					if (start >= buf.length) break;

					end = findEndOfToken(buf, start);
					String paramname = pLine.substring(start, end);
					start = end;
					if (start >= buf.length) break;

					start = skipSpaces(buf, start);
					if (start >= buf.length) break;

					if (buf[start] != '=') throw new HttpParseException("Invalid challenge");
					if (start + 1 >= buf.length) break;
					start = skipSpaces(buf, start + 1);

					if (start >= buf.length) break;
					end = findEndOfToken(buf, start);
					String value = pLine.substring(start, end);
					start = end;

					if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) challenge.iParams
							.addField(paramname, value.substring(1, value.length() - 1));
					else challenge.iParams.addField(paramname, value);
					skipComma = false;
				}
				// if (challenge.params.getField("realm") == null)
				// challenge.params.addField("realm", "");

				challeges.add(challenge);
				if (start >= buf.length) break;
			}

		} catch (HttpParseException e) {
			throw e;
		} catch (Exception e) {
			throw new HttpParseException("Invalid challenge");
		}

		return challeges.toArray(new Challenge[challeges.size()]);
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
		} else {
			while (pos < buf.length
					&& (!Character.isSpaceChar(buf[pos]) && !(buf[pos] == ',') && !(buf[pos] == '=')))
				pos++;
		}
		return pos;
	}

}
