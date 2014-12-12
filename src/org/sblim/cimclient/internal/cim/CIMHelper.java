/**
 * (C) Copyright IBM Corp. 2006, 2012
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author : Alexander Wolf-Reber, IBM, a.wolf-reber@de.ibm.com
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1565892    2006-12-06  lupusalex    Make SBLIM client JSR48 compliant
 * 2003590    2008-06-30  blaschke-oss Change licensing from CPL to EPL
 * 2524131    2009-01-21  raman_arora  Upgrade client to JDK 1.5 (Phase 1)
 * 2964463    2010-03-08  blaschke-oss WBEMClient.initialize() throws wrong exception
 * 2942520    2010-03-08  blaschke-oss IPv6 link local address with scope_id including a dot not supported
 * 3513353    2012-03-30  blaschke-oss TCK: CIMDataType arrays must have length >= 1
 * 3513349    2012-03-31  blaschke-oss TCK: CIMDataType must not accept null string
 */

package org.sblim.cimclient.internal.cim;

import java.net.URI;
import java.net.URISyntaxException;

import javax.cim.CIMDataType;
import javax.cim.CIMObjectPath;

import org.sblim.cimclient.internal.util.WBEMConstants;

/**
 * Class CIMHelper provides convenience methods that are missing from the
 * official JSR48 API
 * 
 */
public abstract class CIMHelper {

	private CIMHelper() {
	// no instances
	}

	/**
	 * Creates a URI of a CIMOM from a given CIM object path, adding default
	 * port if port not parsable.
	 * 
	 * @param pPath
	 *            The CIM object path.
	 * @return The URI.
	 * @throws URISyntaxException
	 */
	public static URI createCimomUri(CIMObjectPath pPath) throws URISyntaxException {
		String scheme = pPath.getScheme();
		String host = pPath.getHost();
		int port = WBEMConstants.DEFAULT_WBEM_PORT;
		try {
			port = Integer.parseInt(pPath.getPort());
		} catch (NumberFormatException e) {
			// stuck with default port
		}
		return new URI(scheme, null, host, port, WBEMConstants.CIMOM_PATH, null, null);
	}

	/**
	 * Creates a URI of a CIMOM from a given URI, adding default port if port
	 * not specified.
	 * 
	 * @param pUri
	 *            The URI.
	 * @return The URI.
	 * @throws URISyntaxException
	 */
	public static URI createCimomUri(URI pUri) throws URISyntaxException {
		String scheme = pUri.getScheme();
		String host = pUri.getHost();
		int port = pUri.getPort();
		if (port == -1) {
			// stuck with default port
			port = WBEMConstants.DEFAULT_WBEM_PORT;
		}
		return new URI(scheme, null, host, port, WBEMConstants.CIMOM_PATH, null, null);
	}

	private static CIMDataType CIMScalarDataTypes[] = {
	/* 00 */CIMDataType.UINT8_T,
	/* 01 */CIMDataType.SINT8_T,
	/* 02 */CIMDataType.UINT16_T,
	/* 03 */CIMDataType.SINT16_T,
	/* 04 */CIMDataType.UINT32_T,
	/* 05 */CIMDataType.SINT32_T,
	/* 06 */CIMDataType.UINT64_T,
	/* 07 */CIMDataType.SINT64_T,
	/* 08 */CIMDataType.STRING_T,
	/* 09 */CIMDataType.BOOLEAN_T,
	/* 10 */CIMDataType.REAL32_T,
	/* 11 */CIMDataType.REAL64_T,
	/* 12 */CIMDataType.DATETIME_T,
	/* 13 */CIMDataType.CHAR16_T,
	/* 14 */new CIMDataType(""),
	/* 15 */CIMDataType.OBJECT_T,
	/* 16 */null,
	/* 17 */CIMDataType.CLASS_T };

	/**
	 * Returns the CIMDataType of a scalar of the specified data type. This
	 * should be used in lieu of "new CIMDataType(pType)" which is not supported
	 * by the JSR48 standard.
	 * 
	 * @param pType
	 *            Data type.
	 * @return CIMDataType corresponding to data type.
	 */
	public static CIMDataType ScalarDataType(int pType) {
		if (pType < 0 || pType >= CIMScalarDataTypes.length) return null;
		return CIMScalarDataTypes[pType];
	}

	private static CIMDataType CIMArrayDataTypes[] = {
	/* 00 */CIMDataType.UINT8_ARRAY_T,
	/* 01 */CIMDataType.SINT8_ARRAY_T,
	/* 02 */CIMDataType.UINT16_ARRAY_T,
	/* 03 */CIMDataType.SINT16_ARRAY_T,
	/* 04 */CIMDataType.UINT32_ARRAY_T,
	/* 05 */CIMDataType.SINT32_ARRAY_T,
	/* 06 */CIMDataType.UINT64_ARRAY_T,
	/* 07 */CIMDataType.SINT64_ARRAY_T,
	/* 08 */CIMDataType.STRING_ARRAY_T,
	/* 09 */CIMDataType.BOOLEAN_ARRAY_T,
	/* 10 */CIMDataType.REAL32_ARRAY_T,
	/* 11 */CIMDataType.REAL64_ARRAY_T,
	/* 12 */CIMDataType.DATETIME_ARRAY_T,
	/* 13 */CIMDataType.CHAR16_ARRAY_T,
	/* 14 */new CIMDataType("", 0),
	/* 15 */CIMDataType.OBJECT_ARRAY_T,
	/* 16 */null,
	/* 17 */CIMDataType.CLASS_ARRAY_T };

	/**
	 * Returns the CIMDataType of an unbounded array of the specified data type.
	 * This should be used in lieu of "new CIMDataType(pType,0)" which is not
	 * supported by the JSR48 standard.
	 * 
	 * @param pType
	 *            Data type.
	 * @return CIMDataType corresponding to data type.
	 */
	public static CIMDataType UnboundedArrayDataType(int pType) {
		if (pType < 0 || pType >= CIMArrayDataTypes.length) return null;
		return CIMArrayDataTypes[pType];
	}
}
