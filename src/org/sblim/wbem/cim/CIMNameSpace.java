/**
 * CIMNameSpace.java
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
 * Flag     Date         Prog         Description
 *------------------------------------------------------------------------------- 
 *   17997  2005-08-04   thschaef     Log right class (this)
 *   18076  2005-08-11   pineiro5     localhost incorrectly added to CIMObjectPath
 * 1535756  2006-08-08   lupusalex    Make code warning free
 * 1816503  2007-10-19   ebak         IPv6 support 
 * 1824094  2007-11-05   ebak         CIMNameSpace(String pURI) doesn't handle namespace properly
 * 1832439  2007-11-15   ebak         less strict parsing for IPv6 hostnames
 * 1832439  2007-11-19   ebak         rework: less strict parsing for IPv6 hostnames
 * 1954059  2008-05-16   blaschke-oss wrong path in CIMNameSpace URI
 * 2219646  2008-11-17   blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325  2009-06-22   blaschke-oss Change licensing from CPL to EPL
 */


package org.sblim.wbem.cim;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.util.SessionProperties;
import org.sblim.wbem.util.XMLHostStr;

public class CIMNameSpace implements Serializable, Cloneable {

	private static final long serialVersionUID = 4090304642015551161L;

	private static final String HTTP = "http";

	// private static final String HTTPS = "https";

	private static final String DEFAULT_PROTOCOL = HTTP;

	private static final String DEFAULT_HOST = null;

	private static final int DEFAULT_PORT = 5988;

	private static final String DEFAULT_NAMESPACE = "root/cimv2";
	
	private static final String DEFAULT_FILE = "/cimom";

	public static final char NAMESPACE_SEPARATOR = '/';

	private String iProtocol, iHost, iFile, iNameSpace;
	
	private int iPort;
	
	//private URI iUri;

	/**
	 * Constructs and instantiates a default CIM namespace name.
	 * 
	 * @throws CIMException
	 *             CIM_ERR_FAILED - if a malformed URL is constructed from the
	 *             specified parameters
	 */
	public CIMNameSpace() throws CIMException {
		iProtocol = DEFAULT_PROTOCOL;
		iHost = DEFAULT_HOST;
		iPort = DEFAULT_PORT;
		iFile = DEFAULT_FILE;
		iNameSpace = DEFAULT_NAMESPACE;
	}

	/**
	 * Constructs a CIM namespace, pointing to a specified host or URI
	 * (protocol://hostname[:port]/file), i.e.
	 * "https://47.11.8.15:5989/root/cimv2"
	 * 
	 * @param pURI
	 *            The URI String
	 * @throws CIMException
	 */
	public CIMNameSpace(String pURI) throws CIMException {
		this(pURI, null);
	}

	/**
	 * Constructs an object which represents a CIMNameSpace.
	 * 
	 * @param pHost
	 *            host= protocol://hostname[:port]/file host=
	 *            "https://myhostname/" host= "http://myhostname:5988/cimom"
	 *            host= "myhostname"
	 * @param pNamespace
	 *            a string which represents the namespace in the CIM Object
	 *            Manager. According to the spec it should not starts or ends
	 *            with '/'. This are examples of valid namespace. namespace=
	 *            "root/cimv2" namespace= "root"
	 * @throws IllegalArgumentException
	 *             if the
	 * @throws CIMException
	 *             CIM_ERR_INVALID_PARAMETER - if the URI is malformed
	 */
	public CIMNameSpace(String pHost, String pNamespace) throws CIMException{
		if (pHost == null) throw new CIMException(
			CIMException.CIM_ERR_INVALID_PARAMETER, "Null hostName is not allowed!" 
		);
		XMLHostStr xmlHostStr = new XMLHostStr(pHost);
		String prot = xmlHostStr.getProtocol();
		String host = xmlHostStr.getHost();
		String port = xmlHostStr.getPort();
		String file = xmlHostStr.getFile();
		iProtocol = prot == null ? DEFAULT_PROTOCOL : prot;
		iHost = host == null ? DEFAULT_HOST : host;
		iPort = port == null ? DEFAULT_PORT : Integer.parseInt(port);
		iFile = file == null ? DEFAULT_FILE : file;
		iNameSpace = pNamespace == null ? DEFAULT_NAMESPACE : pNamespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		CIMNameSpace that = new CIMNameSpace();
		that.iProtocol = iProtocol;
		that.iHost = iHost;
		that.iFile = iFile;
		that.iNameSpace = iNameSpace;
		that.iPort = iPort;
		return that;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 
	 * Compares a namespace with the given object. It is important to notice
	 * that the hostname must be exactly the same (ignoring case). The IP
	 * address of the hostname name is not resolved.
	 * 
	 * @return true if the namespace value for this namespace is equal to the
	 * namespace value for the given object and both URI matches.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof CIMNameSpace)) return false;
		CIMNameSpace that = (CIMNameSpace) o;

		return
			equals(iProtocol, that.iProtocol) &&
			equals(iHost, that.iHost) &&
			equals(iFile, that.iFile) &&
			equals(iNameSpace, that.iNameSpace) &&
			iPort == that.iPort;
	}
	
	private static final boolean equals(String pA, String pB) {
		return (pA == null ? pB == null : pA.equalsIgnoreCase(pB));
	}

	/**
	 * Returns the hostname of the CIMNameSpace
	 * 
	 * @return a string which represents the hostname of the NameSpace
	 */
	public String getHost() {
		return iHost;
	}

	/**
	 * Returns the URI representation of the CIMNameSpace.
	 * 
	 * @return The URI
	 * @throws CIMException
	 *             malformed URI exception
	 */
	public URI getHostURI() throws CIMException {
		/*
		 * URI(
		 *   String scheme, String userInfo, String host, int port, String path,
		 *   String query, String fragment
		 * ) 
		 */ 
		String path = iFile;
		if (path.charAt(0)!='/') path = '/'+path;
		try {
			return
				new URI(iProtocol, null, iHost, iPort, path, null, null);
		} catch (URISyntaxException e) {
			throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER, e);
		}
	}

	/**
	 * Returns the URL representation of the CIMNameSpace.
	 * 
	 * @return The URL
	 * @throws CIMException
	 *             malformed URL exception
	 * @see java.net.URI#toURL()
	 */
	public URL getHostURL() throws CIMException {
		try {
			return getHostURI().toURL();
		} catch (Throwable e) {
			Logger logger = SessionProperties.getGlobalProperties().getLogger();
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "invalid URI format", e);
			}
			try {
				return new URL(getScheme(), getHost(), getPort(), getNameSpace());
			} catch (Throwable n) {
				// url = null;

				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "invalid URI format", e);
				}
				throw new CIMException(CIMException.CIM_ERR_FAILED, n, "malformed URI");
			}
		}
	}

	/**
	 * Returns the namespace fragment of the CIMNameSpace. This fragment does
	 * not include any information related to a host. (i.e. "myhost:root/cimv2",
	 * this method return just the "root/cimv2"
	 * 
	 * @return The namespace
	 */
	public String getNameSpace() {
		return iNameSpace;
	}

	/**
	 * Returns the scheme specified on the URI.
	 * 
	 * @return The scheme
	 */
	public String getScheme() {
		return iProtocol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Creates a CIMNameSpace object from the specified URI. This method behaves
	 * as new CIMNameSpace(p).
	 * 
	 * @param pURI
	 *            The URI string
	 * @return The namespace
	 * @throws CIMException
	 * 
	 * @deprecated The CIMNameSpace(String) constructor must be used instead of
	 *             this method.
	 */
	public static CIMNameSpace parse(String pURI) throws CIMException {
		return new CIMNameSpace(pURI);
	}

	/**
	 * Specifies the port for this CIMNameSpace object.
	 * 
	 * @param pPort
	 */
	public void setPort(int pPort) {
		if (pPort < 0) throw new IllegalArgumentException("invalid port number argument: " + pPort);
		iPort = pPort;
	}

	/**
	 * Returns the port defined for this namespace by the URI. If no port is
	 * defined, then the default port is used.
	 * 
	 * @return The port
	 */
	public int getPort() {
		return iPort;
	}

	/**
	 * Specifies the host for this CIMNameSpace object.
	 * 
	 * @param pHost
	 */
	public void setHost(String pHost) {
		XMLHostStr  xmlHostStr = new XMLHostStr(pHost);
		String prot = xmlHostStr.getProtocol();
		if (prot != null) iProtocol = prot;
		iHost = xmlHostStr.getHost();
		String port = xmlHostStr.getPort();
		if (port != null)iPort = Integer.parseInt(port); 
		iFile = xmlHostStr.getFile();
	}

	/**
	 * Specifies the namespace for this CIMNameSpace object. The namespace
	 * should not start or end with a '/', it will be automatically removed. As
	 * a result a namespace like "/root/cimv2", will result on "root/cimv2", and
	 * "/" would be translated into an empty namespace ("")
	 * 
	 * @param pNamespace
	 *            The namespace
	 * @throws CIMException
	 */
	public void setNameSpace(String pNamespace) throws CIMException {
		if (pNamespace == null) throw new IllegalArgumentException("null namespace argument");
		iNameSpace = pNamespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "//"+((iHost==null)?"":iHost)+'/'+((iNameSpace == null)?"":iNameSpace)+"/";
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * for testing purposes only
	 */
/* 	public static void main(String[] args) {
		CIMNameSpace ns;
		String[][] parms = new String[][] { 
				{ "test.host.com", null }, 
				{ "https://test.host.com", null }, 
				{ "test.host.com:1234", null }, 
				{ "https://test.host.com:1234", null }, 
				{ "test.host.com/filename", null }, 
				{ "https://test.host.com/filename", null }, 
				{ "test.host.com:1234/filename", null }, 
				{ "https://test.host.com:1234/filename", null }, 
				{ "test.host.com", "name/space" }, 
				{ "https://test.host.com", "name/space" }, 
				{ "test.host.com:1234", "name/space" }, 
				{ "https://test.host.com:1234", "name/space" }, 
				{ "test.host.com/filename", "name/space" }, 
				{ "https://test.host.com/filename", "name/space" }, 
				{ "test.host.com:1234/filename", "name/space" }, 
				{ "https://test.host.com:1234/filename", "name/space" },
				{ "12.34.56.78", null }, 
				{ "https://12.34.56.78", null }, 
				{ "12.34.56.78:1234", null }, 
				{ "https://12.34.56.78:1234", null }, 
				{ "12.34.56.78/filename", null }, 
				{ "https://12.34.56.78/filename", null }, 
				{ "12.34.56.78:1234/filename", null }, 
				{ "https://12.34.56.78:1234/filename", null }, 
				{ "12.34.56.78", "name/space" }, 
				{ "https://12.34.56.78", "name/space" }, 
				{ "12.34.56.78:1234", "name/space" }, 
				{ "https://12.34.56.78:1234", "name/space" }, 
				{ "12.34.56.78/filename", "name/space" }, 
				{ "https://12.34.56.78/filename", "name/space" }, 
				{ "12.34.56.78:1234/filename", "name/space" }, 
				{ "https://12.34.56.78:1234/filename", "name/space" },
				{ "12.34.56.78", null }, 
				{ "https://1234:5678:9abc::def", null }, 
				{ "[1234:5678:9abc::def]:1234", null }, 
				{ "https://[1234:5678:9abc::def]:1234", null }, 
				{ "1234:5678:9abc::def/filename", null }, 
				{ "https://1234:5678:9abc::def/filename", null }, 
				{ "[1234:5678:9abc::def]:1234/filename", null }, 
				{ "https://[1234:5678:9abc::def]:1234/filename", null }, 
				{ "1234:5678:9abc::def", "name/space" }, 
				{ "https://1234:5678:9abc::def", "name/space" }, 
				{ "[1234:5678:9abc::def]:1234", "name/space" }, 
				{ "https://[1234:5678:9abc::def]:1234", "name/space" }, 
				{ "1234:5678:9abc::def/filename", "name/space" }, 
				{ "https://1234:5678:9abc::def/filename", "name/space" }, 
				{ "[1234:5678:9abc::def]:1234/filename", "name/space" }, 
				{ "https://[1234:5678:9abc::def]:1234/filename", "name/space" }
		};
				
		System.out.println("CIMNameSpace()");
		try {
			ns = new CIMNameSpace();
			System.out.println("  toString(): " + ns.toString());
			System.out.println("  getNameSpace(): " + ns.getNameSpace());
			System.out.println("  getHostURI.toString(): " + ns.getHostURI().toString());
			System.out.println("  PASSED");
		} catch (CIMException ce) {
			System.out.println("  FAILED");
		}
		
		for(int parmsIdx = 0; parmsIdx < parms.length; parmsIdx++) {
			System.out.println("CIMNameSpace(" + parms[parmsIdx][0] + "," 
						+ parms[parmsIdx][1] + ")");
			try {
				ns = new CIMNameSpace(parms[parmsIdx][0], parms[parmsIdx][1]);
				System.out.println("  toString(): " + ns.toString());
				System.out.println("  getNameSpace(): " + ns.getNameSpace());
				System.out.println("  getHostURI.toString(): " + ns.getHostURI().toString());
				System.out.println("  PASSED");
			} catch (CIMException ce) {
				System.out.println("  FAILED");
			}
		}
		
		System.out.println("CIMNameSpace(null)");
		try {
			ns = new CIMNameSpace(null);
			System.out.println("  FAILED");
		} catch (CIMException ce) {
			System.out.println("  caught " + ce.toString());
			System.out.println("  PASSED");
		}
	} */
}
