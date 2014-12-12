/**
 * CIMEnumeration.java
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
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.client;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Provides the mechanisms to stream the response of CIMObjects, returned by the CIMOM.
 */
public interface CIMEnumeration extends Enumeration {
	
	/**
	 * Close the enumeration by throwing away any remaing xml document without parsing it,
	 * while keeping the connection available for future requests.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException ;
	
	/**
	 * Close the enumeration by throwing away any remaing xml document without parsing it. If 
	 * the force argument is true, then it forces to close the connection without receiving any
	 * of the remainding XML document from the CIMOM, otherwise reads the rest of the XML document
	 * without parsing it.
	 * 
	 * @param force
	 * @throws IOException
	 */
	public void close(boolean force) throws IOException;

	/**
	 * Fetch all the CIMObjects into memory. Preventing object loss when the CIMOM
	 * to close the connection because it timeout.
	 * @throws IOException
	 */
	public void fetchAll() throws IOException;

}
