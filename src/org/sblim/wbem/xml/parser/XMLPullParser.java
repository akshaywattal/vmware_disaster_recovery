/**
 * XMLPullParser.java
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
 *   18274    2005-09-12  fiuczy       String values get corrupted by CIM client
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.xml.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.util.SessionProperties;
import org.sblim.wbem.xml.CIMResponse;
import org.sblim.wbem.xml.XMLDefaultHandlerImpl;
import org.xml.sax.Attributes;

public class XMLPullParser {

	class CharString {

		private int iCount;

		private int iHash;

		private int iOffset;

		//private String iStr;

		private char[] iValue;

		public CharString(char[] value, int begin, int count) {
			this(value, begin, count, null);
		}

		public CharString(char[] value, int begin, int count, String str) {
			iValue = value;
			iOffset = begin;
			iCount = count;
			iHash = 0;
			//iStr = str;
		}

		public boolean equals(Object o) {
			if (!(o instanceof CharString)) return false;
			String s = (String) o;

			int pos = iOffset;
			for (int i = 0; i < iCount; i++) {
				if (iValue[pos] != s.charAt(i)) return false;
				pos++;
			}
			return true;
		}

		public int hashCode() {
			int h = iHash;
			if (h == 0) {
				int off = iOffset;
				char val[] = iValue;
				int len = iCount;

				for (int i = 0; i < len; i++) {
					h = 31 * h + val[off++];
				}
				iHash = h;
			}
			return h;
		}

		public void init(char[] value, int begin, int count, String str) {
			iValue = value;
			iOffset = begin;
			iCount = count;
			iHash = 0;
			//iStr = str;
		}

		public int quickHash() {
			int length = iValue.length;
			if (iHash == 0 && iValue != null && length > 0) {
				iHash = iValue[0] * 37 + iValue[length - 1] * 31 + iValue[length >> 1] * 7;
			}
			return iHash;
		}

	}

	class FastStringHash {

		int iHash;

		String iStr;

		public FastStringHash(String str, int hash) {
			iStr = str;
			iHash = hash;
		}

		public boolean equals(Object o) {
			if (!(o instanceof FastStringHash)) return false;

			return iHash == ((FastStringHash) o).iHash;
		}
	}

	class XMLAttributes implements Attributes {

		public int getIndex(String qName) {
			return iAttributeNames.indexOf(qName);
		}

		public int getIndex(String uri, String localName) {
			return 0;
		}

		public int getLength() {
			return iTotalAttributes;
		}

		public String getLocalName(int index) {
			return EMPTY;
		}

		public String getQName(int index) {
			return ((XMLAttributeValue) iAttributeNames.elementAt(index)).getText();
		}

		public String getType(int index) {
			return EMPTY;
		}

		public String getType(String qName) {
			return EMPTY;
		}

		public String getType(String uri, String localName) {
			return EMPTY;
		}

		public String getURI(int index) {
			return EMPTY;
		}

		public String getValue(int index) {
			return ((XMLAttributeValue) iAttributeValues.elementAt(index)).getText();
		}

		public String getValue(String qName) {
			return ((XMLAttributeValue) (iAttributeValues.elementAt(iAttributeNames.indexOf(qName))))
					.getText();
		}

		public String getValue(String uri, String localName) {
			return EMPTY;
		}
	}

	class XMLAttributeValue {

		int iCurrentPos;

		int iBegin, iLen;

		int iHash;

		String iText;

		private boolean iTranslate;

		public XMLAttributeValue(int begin, int len, boolean translate, int hash) {
			iBegin = begin;
			iLen = len;
			iTranslate = translate;
			iHash = hash;
		}

		public XMLAttributeValue(int begin, int len, int hash) {
			iBegin = begin;
			iLen = len;
			iTranslate = true;
			iHash = hash;
		}

		public String getText() {
			if (iText == null) {
				if (iTranslate) {
					try {
						// Integer hashKey = new Integer(hash);
						// text = (String)stringTable.get(hashKey);
						// text = null;
						// if (text == null) {
						iText = _getChars();
						// stringTable.put(hashKey, text);
						// } else {
						// // System.out.println("Found:"+text);
						// cnt++;
						// }
					} catch (Exception e) {
						Logger logger = SessionProperties.getGlobalProperties().getLogger();
						if (logger.isLoggable(Level.WARNING)) {
							logger.log(Level.WARNING, "exception while decoding CHARACTERS XML", e);
						}
						iText = new String(iBufferChar, iBegin, iLen);
					}
				} else {
					// Integer hashKey = new Integer(hash);
					// text = (String)stringTable.get(hashKey);
					// text = null;
					// if (text == null) {
					iText = new String(iBufferChar, iBegin, iLen);
					// stringTable.put(hashKey, text);
					// } else {
					// // System.out.println("Found:"+text);
					// cnt++;
					// }
				}
				iHash = 0;
			}
			return iText;
		}

		// public void setTranslate(boolean translate) {
		// this.translate = translate;
		// }

		public void init(int begin, int len) {
			iBegin = begin;
			iLen = len;
			iText = null;
		}

		public void setTranslate(boolean translate, int hash) {
			iTranslate = translate;
			iHash = hash;
		}

		public String toString() {
			return getText();
		}

		protected String _getChars() throws XMLPullParserException {
			StringBuffer attributeValue = new StringBuffer();
			int last = iBegin + iLen;
			char ch;
			// char prevCh = '\0'; //18274
			for (iCurrentPos = iBegin; iCurrentPos < last; iCurrentPos++) {
				ch = iBufferChar[iCurrentPos];
				if (ch == '&') {
					// try {
					// System.out.println(_currentPos);
					int ref = parseReference();

					if (ref > -1) attributeValue.append((char) ref);
					// }
					// catch (Exception e) {
					// e.printStackTrace();
					// }
					// } else if (ch == '\t'
					// || ch == '\r'
					// || ch == '\n') {
					//
					// if (ch != '\n' || prevCh != '\r'){
					// attributeValue.append(' ');
					// }
				} else {
					attributeValue.append(ch);
				}
				// prevCh = ch; //18274
			}
			return attributeValue.toString();
		}

		protected int parseReference() throws XMLPullParserException {
			iCurrentPos++;
			char ch1 = iBufferChar[iCurrentPos++];
			if (ch1 == '#') {
				ch1 = iBufferChar[iCurrentPos++];
				if (ch1 == 'x') {
					int value = 0;
					do {
						ch1 = iBufferChar[iCurrentPos++];
						if (ch1 >= '0' && ch1 <= '9') value = value * 16 + (ch1 - '0');
						else if (ch1 >= 'A' && ch1 <= 'F' || ch1 >= 'a' && ch1 <= 'z') value = value
								* 16 + (Character.toUpperCase(ch1) - 'A' + 10);
						else if (ch1 == ';') break;
						else throw new XMLPullParserException(
								"invalid character while parsing hex encoded number " + escape(ch1));
					} while (true);
					iCurrentPos--; // 18274
					return (char) value;
				}
				int value = 0;
				if (ch1 >= '0' && ch1 <= '9') {
					do {
						if (ch1 >= '0' && ch1 <= '9') {
							value = value * 10 + (ch1 - '0');
							ch1 = iBufferChar[iCurrentPos++];
						} else if (ch1 == ';') break;
						else throw new XMLPullParserException(
								"invalid character while parsing decinal encoded character: "
										+ escape(ch1));
					} while (true);
					iCurrentPos--; // 18274
					return (char) value;
				}
				throw new XMLPullParserException("invalid number format");
			}
			int startPos = iCurrentPos - 1;
			if (isValidStartElementNameChar(ch1)) {
				do {
					ch1 = iBufferChar[iCurrentPos++];
					if (ch1 == ';') break;
					if (!isValidElementNameChar(ch1)) throw new XMLPullParserException(
							"invalid reference character " + escape(ch1));
				} while (true);
			} else {
				throw new XMLPullParserException(
						"expected valid name start character for value reference");
			}
			iCurrentPos--;
			ch1 = iBufferChar[startPos];
			char ch2 = iBufferChar[startPos + 1];
			char ch3 = iBufferChar[startPos + 2];

			if (ch1 == 'l' && ch2 == 't' && ch3 == ';') {
				return '<';
			} else if (ch1 == 'g' && ch2 == 't' && ch3 == ';') {
				return '>';
			} else {
				char ch4 = iBufferChar[startPos + 3];
				if (ch1 == 'a' && ch2 == 'm' && ch3 == 'p' && ch4 == ';') { return '&'; }
				char ch5 = iBufferChar[startPos + 4];
				if (ch1 == 'a' && ch2 == 'p' && ch3 == 'o' && ch4 == 's' && ch5 == ';') {
					return '\'';
				} else if (ch1 == 'q' && ch2 == 'u' && ch3 == 'o' && ch4 == 't' && ch5 == ';') {
					return '\"';
				} else {
					// TODO return reference
				}
			}
			return -1;
		}
	}

	public static final int ATTRIBUTE = 10;

	public static final int CDATA = 12;

	public static final int CHARACTERS = 4;

	public static final int COMMENT = 5;

	public static final int DTD = 11;

	public static final String EMPTY = "";

	public static final int END_DOCUMENT = 8;

	public static final int END_ELEMENT = 2;

	public static final int ENTITY_DECLARATION = 15;

	public static final int ENTITY_REFERENCE = 9;

	public static final int NAMESPACE = 13;

	public static final int NOTATION_DECLARATION = 14;

	public static final int PROCESSING_INSTRUCTION = 3;

	public static final int SPACE = 6;

	public static final int START_DOCUMENT = 7;

	public static final int START_ELEMENT = 1;

	public static void main(String[] args) {
		XMLPullParser reader;
		try {
			File[] xmlFiles = XMLDefaultHandlerImpl.listAllXml(new File("c:\\code\\cimxml\\"));
			xmlFiles = new File[] { new File(
					"c:\\code\\cimxml\\Response_EnumerateInstances_Brocade_PortInSwitch.xml") };

			for (int i = 0; i < xmlFiles.length; i++) {
				XMLDefaultHandlerImpl parserHdlr = new XMLDefaultHandlerImpl(false);

				System.out.println("\n" + i + " - " + xmlFiles[i].getAbsolutePath());
				reader = new XMLPullParser(new InputStreamReader(new FileInputStream(xmlFiles[i])));

				try {
					while (next(reader, parserHdlr)) {
						Vector o = parserHdlr.getObjects();

						CIMResponse response = (CIMResponse) o.elementAt(0);
						try {
							response.checkError();
						} catch (Exception e) {}
						Vector v = response.getFirstReturnValue();
						if (v.size() > 0) {
							System.out.println(v.remove(0));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();

					parserHdlr = new XMLDefaultHandlerImpl(true);
					System.out.println("\n" + i + " - " + xmlFiles[i].getAbsolutePath());
					reader = new XMLPullParser(new InputStreamReader(new FileInputStream(
							xmlFiles[i])));

					while (reader.hasNext()) {
						int event = reader.next();
						switch (event) {
							case START_ELEMENT:
								parserHdlr.startElement(EMPTY, EMPTY, reader.getElementName(),
										reader.getAttributes());
								break;
							case END_ELEMENT:
								parserHdlr.endElement(EMPTY, EMPTY, reader.getElementName());
								break;
							case CHARACTERS:
								char[] buf = reader.getText().toCharArray();
								parserHdlr.characters(buf, 0, buf.length);
								break;
						}
					}
					CIMResponse response = (CIMResponse) parserHdlr.getObjects().elementAt(0);

					System.out.println("done\n" + response.getFirstReturnValue());

					e.printStackTrace();
					return;
				}

				/*
				 * while (reader.hasNext()) { int event = reader.next(); switch
				 * (event) { case START_ELEMENT: System.out.println(reader);
				 * break; case END_ELEMENT: System.out.println(reader); break;
				 * case CHARACTERS: System.out.println(reader); break; case
				 * END_DOCUMENT: System.out.println("<<END OF DOCUMENT>>");
				 * break; case START_DOCUMENT: System.out.println("<<START
				 * DOCUMENT>>"); break; case COMMENT: System.out.println("<<COMMENTS>>"); } }
				 */
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean next(XMLPullParser reader, XMLDefaultHandlerImpl parserHdlr)
			throws Exception {
		while (reader.hasNext()) {
			int event = reader.next();
			switch (event) {
				case START_ELEMENT:
					parserHdlr.startElement(EMPTY, EMPTY, reader.getElementName(), reader
							.getAttributes());
					break;
				case END_ELEMENT:
					parserHdlr.endElement(EMPTY, EMPTY, reader.getElementName());

					String lastElementName = null;
					if (reader.getElementNames().size() > 0) lastElementName = (String) reader
							.getElementNames().lastElement();

					if (lastElementName != null && lastElementName.equalsIgnoreCase("IRETURNVALUE")) { return true; }
					break;
				case CHARACTERS:
					char[] buf = reader.getText().toCharArray();
					parserHdlr.characters(buf, 0, buf.length);
					break;
				case END_DOCUMENT:
					return false;
			}
		}
		return false;
	}

	Vector iAttributeNames = new Vector();

	Attributes iAttributes;

	Vector iAttributeValues = new Vector();

	byte[] iBuffer;

	char[] iBufferChar = null;

	XMLAttributeValue iCharacters;

	boolean iClosingElementNamePending;

	int iColNumber = 1;

	// --------------------
	int iCurrentPosition = 0;

	int iCurrentState = 0;

	String iElementName;

	Vector iElementNames = new Vector();

	int iEndCharacters;

	int iFinish;

	// int cnt = 0;
	int iFinishChar = 0;

	Reader iInstream;

	int iLineNumber = 1;

	char iNextChar;

	boolean iSeenEpilog;

	boolean iSeenProlog;

	int iStart;

	int iStartCharacters;

	int iTotalAttributes;

	public XMLPullParser(Reader in) {
		iInstream = in;
		reset();
	}

	public void close() {

	}

	public Attributes getAttributes() {
		if (iCurrentState != START_ELEMENT) return null;

		if (iAttributes == null) {
			iAttributes = new XMLAttributes();
		}
		return iAttributes;
	}

	public String getElementName() {
		return iElementName;
	}

	public Vector getElementNames() {
		return iElementNames;
	}

	public int getLevel() {
		return iElementNames.size();
	}

	public String getText() {
		String result = null;
		;
		if (iCurrentState == CHARACTERS && iCharacters != null) { return iCharacters.getText(); }
		return result;
	}

	public boolean hasNext() {
		return (iCurrentState != END_DOCUMENT);
	}

	public int next() throws IOException {
		char ch;
		resetAttributes();
		ensureCapacity();
		if (iClosingElementNamePending) {
			iClosingElementNamePending = false;
			iElementNames.remove(iElementNames.size() - 1);
			iCurrentState = END_ELEMENT;
			return iCurrentState;
		}
		do {
			ch = (char) getNextCharCheckingEOF();
			if (ch == '<') {
				ch = (char) getNextChar();
				if (ch == '?') {
					if (iSeenProlog) { throw new XMLPullParserException(
							"The processing instruction target matching \"[xX][mM][lL]\" is not allowed."); }
					iSeenProlog = true;
					parsePI();
					ch = (char) getNextChar();
					ch = skipOptionalSpaces(ch);
					if (ch != '<') throw new XMLPullParserException(this,
							"Content is not allowed in prolog.");
					goBack();

					iCurrentState = START_DOCUMENT;
					return iCurrentState;
				} else if (ch == '!') {
					ch = (char) getNextChar();
					if (ch == '-') {
						parseComment();

						iCurrentState = COMMENT;
						return iCurrentState;
					} else if (ch == '[') {
						parseCDATA();
						iCurrentState = CHARACTERS;
						return iCurrentState;
					} else throw new XMLPullParserException(this, "unexpected char " + escape(ch));
				} else if (ch == '/') {
					parseEndElement();
					iCurrentState = END_ELEMENT;
					return iCurrentState;
				} else if (ch == '&') {
					parseUnknown();
				} else if (isValidStartElementNameChar(ch)) {
					if (!iSeenProlog) {
						iSeenProlog = true;
						iCurrentState = START_DOCUMENT;
						goBack();
						goBack();
						return iCurrentState;
					}
					parseStartElement(ch);
					iCurrentState = START_ELEMENT;
					return iCurrentState;
				} else {
					throw new XMLPullParserException(this, "unexpected char " + escape(ch));
				}
			} else {
				iStartCharacters = iCurrentPosition - 1;
				boolean amp = false;
				int hash = 0;
				// int n = 0;
				do {
					// n = (n+1)&15;
					// hash = hash*primes[n]+ch;

					ch = (char) getNextCharCheckingEOF();
					if (ch == (char) -1) {
						if (iElementNames.size() != 0) throw new XMLPullParserException(this,
								"unexpected EOF ");

						iCurrentState = END_DOCUMENT;
						return iCurrentState;
					} else if (ch == '\r' || ch == '\n') {} else {
						if (!isSpace(ch) && ch != '<' && iElementNames.size() == 0) {
							if (!iSeenProlog) throw new XMLPullParserException(this,
									"Content is not allowed in trailing section.");
							throw new XMLPullParserException(this,
									"Content is not allowed in trailing section.");
						}
					}
					amp = false;
					if (ch == '&') {
						amp = true;
						int i = parseReference();
						ch = (char) (i & 0xFFFF);
					}
				} while (ch != '<' || amp);
				iEndCharacters = iCurrentPosition;
				goBack();
				if (iElementNames.size() > 0) {
					if (iCharacters == null) iCharacters = new XMLAttributeValue(iStartCharacters,
							iEndCharacters - iStartCharacters - 1, hash);
					else {
						iCharacters.init(iStartCharacters, iEndCharacters - iStartCharacters - 1);
						iCharacters.setTranslate(true, hash);
					}

					iCurrentState = CHARACTERS;
					return iCurrentState;
				}
			}
		} while (true);
	}

	public void reset() {
		iSeenProlog = true;
		iCurrentState = 0;
		iClosingElementNamePending = false;
		iColNumber = 1;
		iLineNumber = 1;
		iElementName = null;
		iElementNames.setSize(0);
		iAttributeNames.setSize(0);
		iAttributeValues.setSize(0);
		iAttributes = null;

		iStartCharacters = 0;
		iEndCharacters = 0;

		iTotalAttributes = 0;
		iSeenProlog = false;
		iSeenEpilog = false;
	}

	public String toString() {
		switch (iCurrentState) {
			case START_ELEMENT: {
				String s = "START ELEM: <" + iElementName;
				if (iAttributeNames.size() > 0) {
					s += " ";
					for (int i = 0; i < iAttributeNames.size(); i++) {
						s += iAttributeNames.elementAt(i) + "=\"" + iAttributeValues.elementAt(i)
								+ "\" ";
					}
				}
				s += ">";
				return s;
			}
			case END_ELEMENT: {
				String s = "END ELEM: </" + iElementName + ">";
				return s;
			}
			case CHARACTERS: {
				return "CHARACTERS: \"" + getText(); // .replaceAll("\n",
				// "\\\\n").replaceAll("\r",
				// "\\\\r").replaceAll("\t",
				// "\\\\t")+"\"";
			}
		}
		return "UNKOWN";
	}

	protected char _getNextChar() {
		return (char) -1;
	}

	protected void addAttribute(int begName, int lenName, int begValue, int lenValue, int hashname,
			int hashvalue) {
		if (iAttributeNames.size() > iTotalAttributes) {
			XMLAttributeValue attribute = (XMLAttributeValue) iAttributeValues
					.elementAt(iTotalAttributes);
			XMLAttributeValue name = (XMLAttributeValue) iAttributeNames
					.elementAt(iTotalAttributes);
			iTotalAttributes++;
			attribute.init(begValue, lenValue);
			attribute.setTranslate(true, hashvalue);
			name.init(begName, lenName);
			name.setTranslate(false, hashname);
		} else {
			XMLAttributeValue attribute = new XMLAttributeValue(begValue, lenValue, hashvalue);
			XMLAttributeValue name = new XMLAttributeValue(begName, lenName, false, hashname);
			iTotalAttributes++;
			iAttributeNames.add(name);
			iAttributeValues.add(attribute);
		}
	}

	protected void ensureCapacity() {
		if (iBufferChar == null) iBufferChar = new char[1024];

		if (iCurrentPosition >= (8 * iBufferChar.length) / 10) {
			System.arraycopy(iBufferChar, iCurrentPosition, iBufferChar, 0, iFinishChar
					- iCurrentPosition);
			iFinishChar -= iCurrentPosition;
			iCurrentPosition = 0;
		}
	}

	protected String escape(char ch) {
		String result;
		if (ch == '\n') result = "\'\\n\'";
		if (ch == '\r') result = "\'\\r\'";
		if (ch == '\t') result = "\'\\t\'";
		if (ch == '\'') result = "\'\\'\'";
		if (ch > '\177' || ch < ' ') result = "\'\\u" + Integer.toHexString(ch) + "\'";
		else result = "\'" + ch + "\'";
		return result;
	}

	protected int getChar() throws IOException {
		if (iFinishChar <= iCurrentPosition) {
			if (iBufferChar == null) {
				iBufferChar = new char[1024];
			} else if (iFinishChar >= iBufferChar.length) {
				char[] tmp = iBufferChar;
				iBufferChar = new char[iBufferChar.length << 1];
				System.arraycopy(tmp, 0, iBufferChar, 0, tmp.length);
			}

			int total = iInstream.read(iBufferChar, iFinishChar, iBufferChar.length - iFinishChar);

			if (total <= 0) { return -1; }
			iFinishChar += total;
		}
		return iBufferChar[iCurrentPosition++];
	}

	protected int getNextChar() throws IOException {
		int ch;

		if (iFinishChar <= iCurrentPosition) {
			ch = getChar();
		} else ch = iBufferChar[iCurrentPosition++];

		if (ch == -1) throw new XMLPullParserException(this, "unexpected end of document");
		if (ch == '\n') {
			iLineNumber++;
			iColNumber = 1;
		} else iColNumber++;

		return (char) ch;
	}

	protected int getNextCharCheckingEOF() throws IOException {
		int ch;

		if (iFinishChar <= iCurrentPosition) {
			ch = getChar();
		} else ch = iBufferChar[iCurrentPosition++];

		if (ch == '\n') {
			iLineNumber++;
			iColNumber = 1;
		} else iColNumber++;

		return (char) ch;
	}

	protected void goBack() {
		iCurrentPosition--;
		if (iColNumber > 1) {
			iColNumber--;
		}
	}

	protected boolean isSpace(char ch) {
		return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
	}

	protected boolean isValidElementNameChar(char ch) {
		return (ch < 256 && (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch == '_'
				|| ch == ':' || ch == '-' || ch == '.' || ch >= '0' && ch <= '9' || ch == '\267'))
				|| ch >= '\300'
				&& ch <= '\u02FF'
				|| ch >= '\u0370'
				&& ch <= '\u037D'
				|| ch >= '\u0300'
				&& ch <= '\u036F'
				|| ch >= '\u037F'
				&& ch <= '\u2027'
				|| ch >= '\u202A' && ch <= '\u218F' || ch >= '\u2800' && ch <= '\uFFEF';
		// return isValidStartElementNameChar(ch)
		// || (ch < 256 && (ch == '-'
		// || ch == '.'
		// || ch >= '0' && ch <= '9'
		// || ch == '\267'))
		// || ch >= '\u0300' && ch <= '\u036F'
		// || ch >= '\u0400' && ch <= '\u2027'
		// || ch >= '\u202A' && ch <= '\u218F'
		// || ch >= '\u2800' && ch <= '\uFFEF';
	}

	protected boolean isValidStartElementNameChar(char ch) {
		return (ch < 256 && (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch == '_' || ch == ':'))
				|| (ch >= '\300' && ch <= '\u02FF' || ch >= '\u0370' && ch <= '\u037D'
						|| ch >= '\u037F' && ch <= '\u0400' || ch >= '\u0400' && ch <= '\u2027'
						|| ch >= '\u202A' && ch <= '\u218F' || ch >= '\u2800' && ch <= '\uFFEF');
	}

	protected void parseAttribute(char ch) throws IOException {
		int startAttributeName = iCurrentPosition - 1;
		int endAttributeName;
		int hash = 0;
		// int n = 0;
		do {
			// n = (n+1)&15;
			// hash = hash*primes[n]+ch;

			ch = (char) getNextChar();
		} while (isValidElementNameChar(ch));
		endAttributeName = iCurrentPosition;
		// String attributeName = new String(bufferChar, startAttributeName,
		// endAttributeName - startAttributeName-1);
		// attributeNames.add(attributeName);

		ch = skipOptionalSpaces(ch);

		if (ch != '=') throw new XMLPullParserException(this, "missing chacter \'=\'instead "
				+ escape(ch) + " was found ");

		ch = (char) getNextChar();
		ch = skipOptionalSpaces(ch);

		char delimiter;
		if (ch != '\"' && ch != '\'') throw new XMLPullParserException(this,
				"missing chacter \'\"\' or \'\'\' instead " + escape(ch) + " was found ");
		delimiter = ch;

		// StringBuffer attributeValue = new StringBuffer();
		int startAttributeValue = iCurrentPosition;
		char prevCh = '\0';
		int hashvalue = 0;
		// n = 0;
		do {
			// n = (n+1)&15;
			// hashvalue = hashvalue *primes[n] +ch;

			ch = (char) getNextChar();
			if (ch == delimiter) break;
			else if (ch == '<' || ch == '>') throw new XMLPullParserException(this,
					"illegal character " + escape(ch));
			else if (ch == '&') {
				int ref = parseReference();
				ch = (char) (ref & 0xffff);
				// attributeValue.append((char)ref);
			} else if (ch == '\t' || ch == '\r' || ch == '\n') {

				if (ch != '\n' || prevCh != '\r') {
					// attributeValue.append(' ');
				}
			} else {
				// attributeValue.append(ch);
			}
			prevCh = ch;
		} while (true);
		int endAttributeValue = iCurrentPosition;
		// attributeValues.add(attributeValue.toString());
		addAttribute(startAttributeName, endAttributeName - startAttributeName - 1,
				startAttributeValue, endAttributeValue - startAttributeValue - 1, hash, hashvalue);

		return;
	}

	protected int parseCDATA() throws IOException {
		char ch;
		ch = (char) getNextChar();
		if (ch != 'C') throw new XMLPullParserException("CDATA must start with \"<![CDATA[\".");
		ch = (char) getNextChar();
		if (ch != 'D') throw new XMLPullParserException("CDATA must start with \"<![CDATA[\".");
		ch = (char) getNextChar();
		if (ch != 'A') throw new XMLPullParserException("CDATA must start with \"<![CDATA[\".");
		ch = (char) getNextChar();
		if (ch != 'T') throw new XMLPullParserException("CDATA must start with \"<![CDATA[\".");
		ch = (char) getNextChar();
		if (ch != 'A') throw new XMLPullParserException("CDATA must start with \"<![CDATA[\".");
		ch = (char) getNextChar();
		if (ch != '[') throw new XMLPullParserException("CDATA must start with \"<![CDATA[\".");
		boolean braketFound = false;
		boolean doubleBraket = false;
		int startCharacter = iCurrentPosition;
		do {
			ch = (char) getNextCharCheckingEOF();
			if (ch == ']') {
				if (braketFound) doubleBraket = true;
				braketFound = true;
			} else if (ch == '>' && doubleBraket) {
				break;
			} else {
				braketFound = false;
				doubleBraket = false;
			}
			if (ch == (char) -1) throw new XMLPullParserException(
					"XML document structures must start and end within the same entity.");
		} while (true);

		int endCharacter = iCurrentPosition - 1;

		iCharacters.setTranslate(false, 0);
		iCharacters.init(startCharacter, endCharacter - startCharacter);

		return -1;
	}

	protected int parseComment() throws IOException {
		char ch;
		ch = (char) getNextChar();
		if (ch != '-') throw new XMLPullParserException("Comment must start with \"<!--\".");
		boolean dashFound = false;
		boolean doubleDash = false;
		do {
			ch = (char) getNextCharCheckingEOF();
			if (ch == '-') {
				if (dashFound) doubleDash = true;
				dashFound = true;
			} else if (ch == '>' && doubleDash) {
				break;
			} else {
				dashFound = false;
				doubleDash = false;
			}
			if (ch == (char) -1) throw new XMLPullParserException(
					"XML document structures must start and end within the same entity.");
		} while (true);

		// if (!seenProlog) {
		// ch = (char)getNextChar();
		// skipOptionalSpaces(ch);
		// goBack();
		// }
		return -1;
	}

	protected void parseEndElement() throws IOException {
		int startElementName;
		int endElementName;

		char ch;

		startElementName = iCurrentPosition;
		do {
			ch = (char) getNextChar();
		} while (isValidElementNameChar(ch));

		endElementName = iCurrentPosition;
		iElementName = new String(iBufferChar, startElementName, endElementName - startElementName
				- 1);

		if (!iElementNames.lastElement().equals(iElementName.toUpperCase())) throw new XMLPullParserException(
				this,
				"The content of elements must consist of well-formed character data or markup.");

		iElementNames.remove(iElementNames.size() - 1);

		ch = skipOptionalSpaces(ch);
		if (ch != '>') throw new XMLPullParserException(this, "\'=\' was expected, but \'"
				+ escape(ch) + "\' was found instead");

		if (iElementNames.size() == 0) iSeenEpilog = true;
	}

	protected int parsePI() throws IOException {
		char ch;
		ch = (char) getNextChar();
		boolean dashFound = false;
		do {
			ch = (char) getNextCharCheckingEOF();
			if (ch == '?') {
				dashFound = true;
			} else if (ch == '>' && dashFound) {
				break;
			} else {
				dashFound = false;
			}
			if (ch == (char) -1) throw new XMLPullParserException(
					"XML document structures must start and end within the same entity.");
		} while (true);

		return -1;
	}

	protected int parseReference() throws IOException {
		char ch1 = (char) getNextChar();
		if (ch1 == '#') {
			ch1 = (char) getNextChar();
			if (ch1 == 'x') {
				int value = 0;
				do {
					ch1 = (char) getNextChar();
					if (ch1 >= '0' && ch1 <= '9') value = value * 16 + (ch1 - '0');
					else if (ch1 >= 'A' && ch1 <= 'F' || ch1 >= 'a' && ch1 <= 'z') value = value
							* 16 + (Character.toUpperCase(ch1) - 'A' + 10);
					else if (ch1 == ';') break;
					else throw new XMLPullParserException(this,
							"invalid character while parsing hex encoded number " + escape(ch1));
				} while (true);
				return (char) value;
			}
			int value = 0;
			if (ch1 >= '0' && ch1 <= '9') {
				do {
					if (ch1 >= '0' && ch1 <= '9') {
						value = value * 10 + (ch1 - '0');
						ch1 = (char) getNextChar();
					} else if (ch1 == ';') break;
					else throw new XMLPullParserException(this,
							"invalid character while parsing decinal encoded character: "
									+ escape(ch1));
				} while (true);
				return (char) value;
			}
			throw new XMLPullParserException(this, "invalid number format");
		}
		int startPos = iCurrentPosition - 1;
		if (isValidStartElementNameChar(ch1)) {
			do {
				ch1 = (char) getNextChar();
				if (ch1 == ';') break;
				if (!isValidElementNameChar(ch1)) throw new XMLPullParserException(
						"invalid reference character " + escape(ch1));
			} while (true);
		} else {
			throw new XMLPullParserException(this,
					"expected valid name start character for value reference");
		}
		goBack();
		ch1 = iBufferChar[startPos];
		char ch2 = iBufferChar[startPos + 1];
		char ch3 = iBufferChar[startPos + 2];

		if (ch1 == 'l' && ch2 == 't' && ch3 == ';') {
			return '<';
		} else if (ch1 == 'g' && ch2 == 't' && ch3 == ';') {
			return '>';
		} else {
			char ch4 = iBufferChar[startPos + 3];
			if (ch1 == 'a' && ch2 == 'm' && ch3 == 'p' && ch4 == ';') { return '&'; }
			char ch5 = iBufferChar[startPos + 4];
			if (ch1 == 'a' && ch2 == 'p' && ch3 == 'o' && ch4 == 's' && ch5 == ';') {
				return '\'';
			} else if (ch1 == 'q' && ch2 == 'u' && ch3 == 'o' && ch4 == 't' && ch5 == ';') {
				return '\"';
			} else {
				// TODO return reference
			}
		}
		return -1;
	}

	protected int parseStartElement(char ch) throws IOException {
		int startElementName;
		int endElementName;

		resetAttributes();

		startElementName = iCurrentPosition - 1;

		do {
			// n = (n+1)&15;
			// hash = hash*primes[n]+ch;

			ch = (char) getNextChar();
		} while (isValidElementNameChar(ch));

		endElementName = iCurrentPosition;
		// Integer hashKey = new Integer(hash);
		// elementName = (String)stringTable.get(hashKey);
		// elementName = null;
		// if (elementName == null) {
		iElementName = new String(iBufferChar, startElementName, endElementName - startElementName
				- 1);
		// stringTable.put(hashKey, elementName);
		// } else {
		// // System.out.println("Found: "+elementName);
		// cnt++;
		// }
		iElementNames.add(iElementName.toUpperCase());

		do {
			ch = skipOptionalSpaces(ch);
			if (ch == '>') {
				break;
			} else if (ch == '/') {
				ch = (char) getNextChar();
				if (ch != '>') { throw new XMLPullParserException(this,
						"\'=\' was expected, but \'" + escape(ch) + "\' was found instead"); }
				iClosingElementNamePending = true;
			} else if (isValidStartElementNameChar(ch)) {
				parseAttribute(ch);
				ch = (char) getNextChar();
			} else throw new XMLPullParserException(this,
					"Element type \"CIM\" must be followed by either attribute specifications, \">\" or \"/>\".");

		} while (true);
		return -1;
	}

	protected void parseUnknown() throws IOException {
		char ch;
		do {
			ch = (char) getNextChar();
			if (ch == '<') throw new XMLPullParserException("The must end with a '>' delimiter.");
		} while (ch != '>');
	}

	protected void resetAttributes() {
		iTotalAttributes = 0;
		// attributeNames.setSize(0);
	}

	protected char skipOptionalSpaces(char ch) throws IOException {
		while (isSpace(ch)) {
			ch = (char) getNextChar();
		}
		return ch;
	}

	protected char skipRequiredSpaces(char ch) throws IOException {
		if (!isSpace(ch)) throw new XMLPullParserException(this, "space spected");
		do {
			ch = (char) getNextChar();
		} while (isSpace(ch));
		return ch;
	}

}
