/**
 * InputSource.java
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

package org.sblim.wbem.xml.parser;

import java.io.InputStream;
import java.io.Reader;

public class InputSource {

//	private String iPublicId;
//
//	private String iSystemId;

	private InputStream iByteStream;

	private String iEncoding;

	private Reader iCharacterStream;

	public InputSource() {}

	public InputSource(Reader characterStream) {
		iCharacterStream = characterStream;
	}

	public InputSource(InputStream byteStream) {
		iByteStream = byteStream;
	}

	public void setCharacterStream(Reader characterStream) {
		iCharacterStream = characterStream;
	}

	public void setEncoding(String encoding) {
		iEncoding = encoding;
	}

	public String getEncoding() {
		return iEncoding;
	}

	public Reader getCharacterStream() {
		return iCharacterStream;
	}

	public InputStream getByteStream() {
		return iByteStream;
	}
}
