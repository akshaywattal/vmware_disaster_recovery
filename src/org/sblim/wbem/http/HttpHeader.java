/**
 * HttpHeader.java
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
 *   17970    2005-08-11  pineiro5     Logon from z/OS not possible
 * 1353138    2005-11-24  fiuczy       CIMObject element in HTTP header wrong encoded
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1516242    2006-11-27  lupusalex    Support of OpenPegasus local authentication
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.http;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.http.io.ASCIIPrintStream;
import org.sblim.wbem.util.SessionProperties;

public class HttpHeader {

	private static BitSet cDontNeedEncoding;

	private static final String HEX_STR = "0123456789ABCDEF";

	private static String cDfltEncName = null;

	static {
		cDontNeedEncoding = new BitSet(256);
		int i;
		for (i = 'a'; i <= 'z'; i++) {
			cDontNeedEncoding.set(i);
		}
		for (i = 'A'; i <= 'Z'; i++) {
			cDontNeedEncoding.set(i);
		}
		for (i = '0'; i <= '9'; i++) {
			cDontNeedEncoding.set(i);
		}
		cDontNeedEncoding.set(' ');
		cDontNeedEncoding.set('-');
		cDontNeedEncoding.set('_');
		cDontNeedEncoding.set('/');
		cDontNeedEncoding.set('.');
		cDontNeedEncoding.set('*');

		// dfltEncName = (String)AccessController.doPrivileged (
		// new GetPropertyAction("file.encoding")
		// );

	}

	private Hashtable iFields = new Hashtable();

	public HttpHeader() {}

	public HttpHeader(InputStream reader) throws IOException {
		String line = null;
		// TODO: this needs to be optimized!!!
		while (((line = HttpMethod.readLine(reader)) != null) && (line.length() > 0)) {
			// get the header
			try {
				int separator;
				if ((separator = line.indexOf(':')) > -1) {
					String header;
					String value;
					header = line.substring(0, separator);
					value = line.substring(separator + 1);
					// TODO validate header and value, they must not be empty
					// entries
					if (value.length() > 0 && value.startsWith(" ")) addField(header, value
							.substring(1));
					else addField(header, value);
				} else {
					Logger logger = SessionProperties.getGlobalProperties().getLogger();
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "invalid HTTP Header \"" + line + "\"");
					}
				}
			} catch (Exception e) {
				Logger logger = SessionProperties.getGlobalProperties().getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "exception while closing the socket");
				}
			}
			;
		}
		return;
	}

	public void addField(String header, String value) {
		if (header == null || header.length() == 0) return;

		if (value != null) {
			iFields.put(new HeaderEntry(header), value);
		} else {
			iFields.remove(new HeaderEntry(header));
		}
	}

	public void clear() {
		iFields.clear();
	}

	public Iterator iterator() {
		return iFields.entrySet().iterator();
	}

	public static HttpHeader parse(String line) {
		int prev = 0;
		int next = 0;
		HttpHeader header = new HttpHeader();
		if (line != null && line.length() > 0) {
			next = line.indexOf(',');
			while (next > -1) {
				String hdr = line.substring(prev, next);
				int separator = hdr.indexOf('=');
				if (separator > -1) {
					String key;
					String value;
					key = hdr.substring(0, separator);
					value = hdr.substring(separator + 1);

					header.addField(key, value);
				} else {
					// something goes wrong. no separator found
				}
				prev = next + 1;
				while (Character.isSpaceChar(line.charAt(prev)))
					prev++;
				next = line.indexOf(',', prev);
			}
			String hdr = line.substring(prev);
			int separator = hdr.indexOf('=');
			if (separator > -1) {
				header.addField(hdr.substring(0, separator), hdr.substring(separator + 1));
			}
		}

		return header;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		int i = 0;
		Iterator iterator = iFields.entrySet().iterator();
		while (iterator.hasNext()) {
			if (i++ > 0) buf.append(',');
			Map.Entry entry = (Map.Entry) iterator.next();
			buf.append(entry.getKey().toString());
			buf.append(": ");
			buf.append(entry.getValue().toString());
		}
		return buf.toString();
	}

	public void removeField(String header) {
		iFields.remove(new HeaderEntry(header));
	}

	public String getField(String header) {
		return (String) iFields.get(new HeaderEntry(header));
	}

	public void write(ASCIIPrintStream writer) throws IOException {

		Iterator iterator = iFields.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			writer.print(entry.getKey().toString());
			writer.print(": ");
			writer.print(entry.getValue().toString());
			writer.print("\r\n");
		}
		writer.print("\r\n");
	}

	public static synchronized String encode(byte[] byteArray) {
		String str = null;
		try {
			if (cDfltEncName == null) cDfltEncName = (String) AccessController
					.doPrivileged(new GetProperty("file.encoding"));
			str = encode(byteArray, cDfltEncName);
		} catch (UnsupportedEncodingException e) {}
		return str;
	}

	public static String encode(byte[] byteArray, String enc) throws UnsupportedEncodingException {

		int maxBytesPerChar = 10;
		ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
		new BufferedWriter(new OutputStreamWriter(buf, enc));
//      TODO: Looks like BufferedWriter is called just to produce the UnsupportedEncodingException. 
//		Is there a better way?

		StringBuffer out = new StringBuffer(byteArray.length);

		for (int i = 0; i < byteArray.length; i++) {
			int c = byteArray[i] & 0xFF;
			if (cDontNeedEncoding.get(c)) {
				if (c == ' ') {
					out.append("%20");
				} else {
					out.append((char) c);
				}
			} else {
				out.append('%');
				out.append(HEX_STR.charAt((c >> 4) & 0x0f));
				out.append(HEX_STR.charAt(c & 0x0f));
			}
		}

		return out.toString();
	}

	public static String encode(String s, String source, String dest)
			throws UnsupportedEncodingException {

		return encode(s.getBytes(source), dest);
	}

	class HeaderEntry {

		String iHeader;

		int iHashcode;

		public HeaderEntry(String header) {
			iHeader = header;
			iHashcode = header.toUpperCase().hashCode();
		}

		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof HeaderEntry)) return false;
			return iHeader.equalsIgnoreCase(((HeaderEntry) obj).iHeader);
		}

		public String toString() {
			return iHeader;
		}

		public int hashCode() {
			return iHashcode;
		}
	}

	static class GetProperty implements PrivilegedAction {

		String iPropertyName;

		GetProperty(String propertyName) {
			iPropertyName = propertyName;
		}

		public Object run() {
			return System.getProperty(iPropertyName);
		}
	}
}
