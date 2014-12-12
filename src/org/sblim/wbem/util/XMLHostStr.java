/**
 * XMLHostStr.java
 * 
 * (C) Copyright IBM Corp. 2006, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author: Endre Bak, ebak@de.ibm.com
 * 
 * Flag       Date        Prog         Description
 * -------------------------------------------------------------------------------
 * 1832439    2007-11-19  ebak         rework: less strict parsing for IPv6 hostnames
 * 1954059    2008-05-16  blaschke-oss wrong path in CIMNameSpace URI
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */


package org.sblim.wbem.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * XMLHostStr helps to convert XML HOST element style string into JSR48 style 
 * protocol, host and port strings. 
 */
public class XMLHostStr {
	
	private String iHostStr, iProtocol, iHost, iPort, iFile;
	
	/*
	 * [ protocol "://"] host [ ":" port ]
	 * 
	 * hosts:
	 *   bela@anything.org
	 *   172.20.8.1
	 *   [ffe8::e391:13a3]
	 * 
	 */
	
	/**
	 * Ctor.
	 */
	public XMLHostStr() { /* default constructor */ }
	
	/**
	 * Ctor.
	 * @param pXMLHostStr - like https://anything.org:5989
	 */
	public XMLHostStr(String pXMLHostStr) {
		set(pXMLHostStr);
	}
	
	/**
	 * set
	 * @param pXMLHostStr - like https://anything.org:5989
	 */
	public void set(String pXMLHostStr) {
		if (isIPv6Literal(pXMLHostStr)) {
			iProtocol = iPort = null;
			iHost = pXMLHostStr;
		} else {
			iHostStr = pXMLHostStr;
			iProtocol = parseProtocol();
			iPort = parsePort();
			iHost = iHostStr;
		}
		
		/*
		 * If port missing, look for possible file (https://anything.org/file);
		 * if port present, iFile set in parsePort()
		 */
		if (iPort == null) {
			int pos = iHost.indexOf('/');
			if (pos > 0) { 
				iFile = iHost.substring(pos+1);
				iHost = iHost.substring(0, pos);
			}
		}
	}
	
	/**
	 * getProtocol
	 * @return String
	 */
	public String getProtocol() { return iProtocol; }
	
	/**
	 * getHost
	 * @return String
	 */
	public String getHost() { return iHost; }
	
	/**
	 * getPort
	 * @return String
	 */
	public String getPort() { return iPort; }
	
	/**
	 * getFile
	 * @return String
	 */
	public String getFile() { return iFile; }
	
	public String toString() {
		return "protocol:"+getProtocol()+", host:"+getHost()+", port:"+getPort()+", file:"+getFile();
	}
	
	private static boolean isIPv6Literal(String pStr) {
		if (pStr == null || pStr.length() == 0) return false;
		int numOfDoubleColons = 0;
		int colonCnt = 0;
		int numOfNumbers = 0;
		int digitCnt = 0;
		for(int i=0; i<pStr.length(); i++) {
			char ch = pStr.charAt(i);
			if (Character.digit(ch, 16) >= 0) {
				if ((i == 0 && colonCnt == 1) || digitCnt >= 4) return false;
				++digitCnt;
				colonCnt = 0;
			} else if (ch == ':') {
				if ((i == pStr.length()-1 && colonCnt == 0) || colonCnt >= 2) return false;
				++colonCnt;
				if (colonCnt == 2) {
					if (numOfDoubleColons > 0 || numOfNumbers >= 8) return false;
					++numOfDoubleColons;
				}
				if (digitCnt > 0) {
					digitCnt = 0;
					if (numOfNumbers >= 8) return false;
					++numOfNumbers;
				}
			} else {
				return false;
			}
		}
		if (digitCnt > 0) ++numOfNumbers;
		if ((numOfNumbers == 8 && numOfDoubleColons > 0) || numOfNumbers > 8) return false;
		return true;
	}
	
	/*
	 * [ protocol "://"] host [ ":" port ]
	 * 
	 * hosts:
	 *   bela@anything.org
	 *   172.20.8.1
	 *   [ffe8::e391:13a3]
	 * 
	 */
	
	private static final String PR_SEP = "://";
	
	private String parseProtocol() {
		int pos = iHostStr.indexOf(PR_SEP);
		if (pos < 0) return null;
		String protocol = iHostStr.substring(0, pos);
		iHostStr = iHostStr.substring(pos+PR_SEP.length());
		return protocol;
	}
	
	private static final Pattern PORT_PAT = Pattern.compile("^(.+):([0-9]+)(\\/.+)?$"); 
	
	private String parsePort() {
		Matcher m = PORT_PAT.matcher(iHostStr);
		if (!m.matches()) return null;
		iHostStr = m.group(1);
		iFile = m.group(3);
		return m.group(2);
	}
	
}