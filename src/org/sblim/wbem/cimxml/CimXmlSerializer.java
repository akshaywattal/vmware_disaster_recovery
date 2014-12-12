/**
 * CimXmlSerializer.java
 * 
 * (C) Copyright IBM Corp. 2007, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author: Alexander Wolf-Reber, IBM, a.wolf-reber@de.ibm.com
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1676343    2007-02-08  lupusalex    Remove dependency from Xerces
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.cimxml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Class CimXmlSerializer implements a XML serializer for DOM documents that is
 * specialized for CIM-XML. It might not be used as a generel purpose serializer
 * since it doesn't support any DOM or XML features not required by CIM-XML.
 * 
 */
public class CimXmlSerializer {

	/**
	 * Class XmlWriter implements a writer on an output stream that escapes XML
	 * values according to the CIM-XML requirements.
	 * 
	 */
	private static class XmlWriter {

		private BufferedWriter iWriter;

		/**
		 * Ctor.
		 * 
		 * @param pOut
		 *            The output stream the serialized document is written to
		 * @param pCharsetName
		 *            The encoding the use for the output stream
		 */
		public XmlWriter(OutputStream pOut, String pCharsetName) {
			iWriter = new BufferedWriter(new OutputStreamWriter(pOut, Charset.forName(pCharsetName)
					.newEncoder()));
		}

		/**
		 * Writes text to the stream
		 * 
		 * @param pText
		 *            The text
		 * @throws IOException
		 */
		public void write(String pText) throws IOException {
			if (pText != null) iWriter.write(pText);
		}

		/**
		 * Closes the stream
		 * 
		 * @throws IOException
		 */
		public void close() throws IOException {
			iWriter.close();
		}

		/**
		 * Flushes the buffer to the stream
		 * 
		 * @throws IOException
		 */
		public void flush() throws IOException {
			iWriter.flush();
		}

		/**
		 * Writes a XML value (either attribute or text node). The value is
		 * escaped as follows:<br />
		 * <br />
		 * <table border="1">
		 * <tr>
		 * <th>char</th>
		 * <th>result</th>
		 * </tr>
		 * <tr>
		 * <td align="center">&lt; space</td>
		 * <td>&amp;#xnn;</td>
		 * </tr>
		 * <tr>
		 * <td align="center">&gt; ~</td>
		 * <td>unchanged (UTF-8)</td>
		 * </tr>
		 * <tr>
		 * <td align="center">space</td>
		 * <td>unchanged or &amp;#x20;<br />
		 * (First leading, last trailing and every other space are escaped)</td>
		 * </tr>
		 * <tr>
		 * <td align="center">&lt;</td>
		 * <td>&amp;lt;</td>
		 * </tr>
		 * <tr>
		 * <td align="center">&gt;</td>
		 * <td>&amp;gt;</td>
		 * </tr>
		 * <tr>
		 * <td align="center">&amp;</td>
		 * <td>&amp;amp;</td>
		 * </tr>
		 * <tr>
		 * <td align="center">"</td>
		 * <td>&amp;quot;</td>
		 * </tr>
		 * <tr>
		 * <td align="center">'</td>
		 * <td>&amp;apos;</td>
		 * <tr>
		 * <td align="center">other</td>
		 * <td>unchanged</td>
		 * </tr>
		 * </tr>
		 * </table>
		 * 
		 * @param pText
		 *            The text
		 * @throws IOException
		 */
		public void writeValue(final String pText) throws IOException {
			if (pText == null) { return; }
			boolean escapeSpace = true;
			final int oneBeforeLast = pText.length() - 2;
			for (int i = 0; i < pText.length(); ++i) {

				char currentChar = pText.charAt(i);
				boolean isSpace = false;

				if (isHighSurrogate(currentChar)) {
					if (i > oneBeforeLast || !isLowSurrogate(pText.charAt(i + 1))) { throw new IOException(
							"Illegal Unicode character"); }
					iWriter.write(pText, i++, 2);
				} else if (currentChar < ' ') {
					writeAsHex(currentChar);
				} else if (currentChar > '~') {
					iWriter.write(currentChar);
				} else {
					switch (currentChar) {
						case ' ':
							isSpace = true;
							if (escapeSpace) {
								writeAsHex(currentChar);
							} else {
								iWriter.write(currentChar);
							}
							break;
						case '<':
							iWriter.write("&lt;");
							break;
						case '>':
							iWriter.write("&gt;");
							break;
						case '&':
							iWriter.write("&amp;");
							break;
						case '"':
							iWriter.write("&quot;");
							break;
						case '\'':
							iWriter.write("&apos;");
							break;
						default:
							iWriter.write(currentChar);
					}
				}
				escapeSpace = (isSpace && !escapeSpace) || (i == oneBeforeLast);
			}
		}

		private void writeAsHex(char pChar) throws IOException {
			iWriter.write("&#x" + Integer.toHexString(pChar) + ";");
		}

		private boolean isHighSurrogate(char pChar) {
			return pChar >= UTF16_MIN_HIGH_SURROGATE
					&& pChar <= UTF16_MAX_HIGH_SURROGATE;
		}

		private boolean isLowSurrogate(char pChar) {
			return pChar >= UTF16_MIN_LOW_SURROGATE
					&& pChar <= UTF16_MAX_LOW_SURROGATE;
		}

	}

	/**
	 * UTF-8
	 */
	private static final String UTF8 = "UTF-8";

	/**
	 * The minimum value of a Unicode high-surrogate code unit in the UTF-16
	 * encoding.
	 */
	private static final char UTF16_MIN_HIGH_SURROGATE = '\uD800';

	/**
	 * The minimum value of a Unicode low-surrogate code unit in the UTF-16
	 * encoding.
	 */
	private static final char UTF16_MIN_LOW_SURROGATE = '\uDC00';

	/**
	 * The maximum value of a Unicode high-surrogate code unit in the UTF-16
	 * encoding.
	 */
	private static final char UTF16_MAX_HIGH_SURROGATE = '\uDBFF';

	/**
	 * The maximum value of a Unicode low-surrogate code unit in the UTF-16
	 * encoding.
	 */
	private static final char UTF16_MAX_LOW_SURROGATE = '\uDFFF';

	private boolean iPretty;

	private int iIndent = 0;

	private boolean iLastClosed = false;

	private CimXmlSerializer(boolean pPretty) {
		iPretty = pPretty;
	}

	/**
	 * Serializes a given DOM document as (CIM-)XML to a given output stream
	 * 
	 * @param pOS
	 *            The output stream
	 * @param pDoc
	 *            The document
	 * @param pPretty
	 *            If <code>true</code> the XML is nicely wrapped and indented,
	 *            otherwise it's all in one line
	 * @throws IOException
	 *             Whenever something goes wrong
	 */
	public static void serialize(OutputStream pOS, Document pDoc, boolean pPretty)
			throws IOException {

		try {
			XmlWriter writer = new XmlWriter(pOS, UTF8);
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			new CimXmlSerializer(pPretty).serializeNode(writer, pDoc.getDocumentElement());
			writer.flush();
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	private void serializeNode(XmlWriter pWriter, Node pNode) throws IOException {
		switch (pNode.getNodeType()) {
			case Node.ELEMENT_NODE:
				pWriter.write(indent());
				pWriter.write("<");
				pWriter.write(pNode.getNodeName());
				NamedNodeMap attributes = pNode.getAttributes();
				if (attributes != null) {
					for (int i = 0; i < attributes.getLength(); ++i) {
						pWriter.write(" ");
						serializeNode(pWriter, attributes.item(i));
					}
				}
				Node child = pNode.getFirstChild();
				if (child == null) {
					pWriter.write("/>");
					iLastClosed = true;
					break;
				}
				pWriter.write(">");
				++iIndent;
				iLastClosed = false;
				while (child != null) {
					serializeNode(pWriter, child);
					child = child.getNextSibling();
				}
				--iIndent;
				if (iLastClosed) {
					pWriter.write(indent());
				}
				pWriter.write("</");
				pWriter.write(pNode.getNodeName());
				pWriter.write(">");
				iLastClosed = true;
				break;
			case Node.ATTRIBUTE_NODE:
				pWriter.write(pNode.getNodeName());
				pWriter.write("=\"");
				pWriter.writeValue(pNode.getNodeValue());
				pWriter.write("\"");
				break;
			case Node.TEXT_NODE:
				pWriter.writeValue(pNode.getNodeValue());
		}
	}

	private String indent() {
		if (!iPretty) { return ""; }
		StringBuffer result = new StringBuffer();
		result.append('\n');
		for (int i = 0; i < iIndent; ++i) {
			result.append(' ');
		}
		return result.toString();
	}
}
