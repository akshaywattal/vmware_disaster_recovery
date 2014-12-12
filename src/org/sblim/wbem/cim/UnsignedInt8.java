/**
 * UnsignedInt8.java
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
 * @author: Roberto Pineiro,    IBM,  roberto.pineiro@us.ibm.com  
 * @author: Chung-hao Tan,      IBM,  chungtan@us.ibm.com
 * @author  Thorsten Schaefer,  IBM,  thschaef@de.ibm.com 
 * @author  Wolfgantg Taphorn,  IBM,  taphorn@de.ibm.com
 *
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1535756    2006-08-08  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 * 
 */

package org.sblim.wbem.cim;


/**
 * Implements a 8-bit unsigned integer object.
 * 
 * The value range of an unsigned integer of 8-bit is '0 .. 255'.
 */
public class UnsignedInt8 extends Number implements java.io.Serializable, Cloneable {
	
	private static final long serialVersionUID = 5547591341269280086L;

	/**
	 * The minimum value a unsigned integer of 8-bit value can be. Its value is '0'.
	 */
	public static short MIN_VALUE = 0;
	
	/**
	 * The maximum value a unsigned integer of 8-bit value can be. Its value is '255'.
	 */
	public static short MAX_VALUE = 255;
	
	
	private short iValue;
	
	/**
	 * Constructs an unsigned 8-bit integer object for the specified byte value.
	 * 
	 * @param pValue The value of the created object
	 *  
	 * @throws IllegalArgumentException If value does not fit into the 
	 *             MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt8(byte pValue) {
		if (pValue < MIN_VALUE || pValue > MAX_VALUE) { 
			throw new IllegalArgumentException("Invalid value range. Value must be in a " +MIN_VALUE+ ".." +MAX_VALUE+ " range");
		}
		
		this.iValue = pValue;
	}

	
	/**
	 * Constructs an unsigned 8-bit integer object for the specified short value.
	 * 
	 * @param pValue The value of the created object
	 *  
	 * @throws IllegalArgumentException If value does not fit into the 
	 *             MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt8(short pValue) {
		if (pValue < MIN_VALUE || pValue > MAX_VALUE) { 
			throw new IllegalArgumentException("Invalid value range. Value must be in a " +MIN_VALUE+ ".." +MAX_VALUE+ " range");
		}
		
		this.iValue = pValue;
	}

	
	/**
	 * Constructs an unsigned 8-bit integer object for the specified String value.
	 * 
	 * @param pValue The value of the created object
	 * 
	 * @throws NumberFormatException If value contains non numeric values. 
	 * @throws IllegalArgumentException If value is not a null/empty string or
	 *             if value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt8(String pValue) {
		if (pValue == null) {
			throw new IllegalArgumentException("null string argument");
			
		} else if (pValue.trim().length() == 0) {
			throw new IllegalArgumentException("empty string argument");
		}
		
		this.iValue = Short.parseShort(pValue);
		
		if (this.iValue < MIN_VALUE || this.iValue > MAX_VALUE) { 
			throw new IllegalArgumentException("Invalid value range. Value must be in a " +MIN_VALUE+ ".." +MAX_VALUE+ " range");
		}
	}
	
	
	/**
	 * Constructs an unsigned 8-bit integer object for the specified UnsignedInt8 value.
	 * 
	 * @param pValue The value of the created object
	 *  
	 */
	public UnsignedInt8(UnsignedInt8 pValue) {
		this.iValue = pValue.shortValue();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return Short.toString(this.iValue);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof UnsignedInt8) {
			return this.iValue == (((UnsignedInt8)o).iValue);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#byteValue()
	 */
	public byte byteValue() {
		if (this.iValue < Byte.MIN_VALUE || this.iValue > Byte.MAX_VALUE) { 
			throw new NumberFormatException("Value range exceeded. The value "+ iValue+" is not in the range of a Byte value which is inbetween "+Byte.MIN_VALUE+ " .. " + Byte.MAX_VALUE);
		}
		
		return Byte.parseByte(Short.toString(this.iValue));
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#shortValue()
	 */
	public short shortValue() {
		return iValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#intValue()
	 */
	public int intValue() {
		return iValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#longValue()
	 */
	public long longValue() {
		return iValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#floatValue()
	 */
	public float floatValue() {
		return iValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#doubleValue()
	 */
	public double doubleValue() {
		return iValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (new Short(iValue)).hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return new UnsignedInt8(this.iValue);
	}
}
