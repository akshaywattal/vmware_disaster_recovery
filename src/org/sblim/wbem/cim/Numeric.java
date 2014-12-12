/**
 * Numeric.java
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
 * @author: Wolfgang Taphorn, IBM, taphorn@de.ibm.com  
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1535756    2006-08-08  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 * 
 */
package org.sblim.wbem.cim;

import java.math.BigInteger;

/**
 * 'numeric' properties are send out and retrieved by the CIM Client when the
 * CIMDataType of a Key property is one of the following: 
 *  - uint8, uint16, uint32, uint64
 *  - int8, int16, int32, int64
 *  - real32, real64 
 *
 * Is this the case, the Key Property, which is handled in the CIMObjectPath
 * is not of one of the CIMDataTypes, it is the XML data type 'numeric'.
 * 
 * So this class implements a data type which can deal with such 'numeric' Key
 * properties.
 * 
 */
public class Numeric extends BigInteger {

	private static final long serialVersionUID = -3203341205730162292L;

	/**
	 * Base constructor.
	 * 
	 * This 'Base constructor' can be used to create a 'numeric' property. The
	 * value of the created object can afterwards be consumed as needed.
	 *  
	 * @param value The value of the 'numeric' key property.
	 * @see BigInteger#BigInteger(java.lang.String)
	 */
	public Numeric(String value) {
		super(value);
	}
}
