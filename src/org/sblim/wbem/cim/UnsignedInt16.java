/**
 * UnsignedInt16.java
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
 * @author  Roberto Pineiro,    IBM,  roberto.pineiro@us.ibm.com  
 * @author  Chung-hao Tan,      IBM,  chungtan@us.ibm.com
 * @author  Thorsten Schaefer,  IBM,  thschaef@de.ibm.com 
 * @author  Wolfgantg Taphorn,  IBM,  taphorn@de.ibm.com
 *  
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1535756    2006-08-08  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.cim;

/**
 * Implements a 16-bit unsigned integer object.
 * 
 * The value range of an unsigned integer of 16-bit is '0 .. 65535'.
 */
public class UnsignedInt16 extends Number implements java.io.Serializable, Cloneable {

	private static final long serialVersionUID = -6300627291717069708L;

	private int iValue;

	/**
	 * The minimum value a unsigned integer of 16-bit value can be. Its value is
	 * '0'.
	 */
	public static int MIN_VALUE = 0;

	/**
	 * The maximum value a unsigned integer of 16-bit value can be. Its value is
	 * '65535'.
	 */
	public static int MAX_VALUE = (1 << 16) - 1;

	/**
	 * Constructs an unsigned 16-bit integer object for the specified byte
	 * value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt16(byte pValue) {
		if (pValue < MIN_VALUE || pValue > MAX_VALUE) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }

		this.iValue = pValue;
	}

	/**
	 * Constructs an unsigned 16-bit integer object for the specified short
	 * value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt16(short pValue) {
		if (pValue < MIN_VALUE || pValue > MAX_VALUE) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }

		this.iValue = pValue;
	}

	/**
	 * Constructs an unsigned 16-bit integer object for the specified int value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt16(int pValue) {
		if (pValue < MIN_VALUE || pValue > MAX_VALUE) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }

		this.iValue = pValue;
	}

	/**
	 * Constructs an unsigned 16-bit integer object for the specified String
	 * value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws NumberFormatException
	 *             If value contains non numeric values.
	 * @throws IllegalArgumentException
	 *             If value is not a null/empty string or if value does not fit
	 *             into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt16(String pValue) {
		if (pValue == null) {
			throw new IllegalArgumentException("null string argument");

		} else if (pValue.trim().length() == 0) { throw new IllegalArgumentException(
				"empty string argument"); }

		this.iValue = Integer.parseInt(pValue);

		if (this.iValue < MIN_VALUE || this.iValue > MAX_VALUE) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }
	}

	/**
	 * Constructs an unsigned 16-bit integer object for the specified
	 * UnsignedInt8 value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 */
	public UnsignedInt16(UnsignedInt8 pValue) {
		this.iValue = pValue.intValue();
	}

	/**
	 * Constructs an unsigned 16-bit integer object for the specified
	 * UnsignedInt16 value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 */
	public UnsignedInt16(UnsignedInt16 pValue) {
		this.iValue = pValue.intValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return Integer.toString(this.iValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof UnsignedInt16) { return this.iValue == (((UnsignedInt16) o).iValue); }
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#byteValue()
	 */
	public byte byteValue() {
		if (this.iValue < Byte.MIN_VALUE || this.iValue > Byte.MAX_VALUE) { throw new NumberFormatException(
				"Value range exceeded. The value " + iValue
						+ " is not in the range of a Byte value which is inbetween "
						+ Byte.MIN_VALUE + " .. " + Byte.MAX_VALUE); }

		return (byte) this.iValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#shortValue()
	 */
	public short shortValue() {
		if (this.iValue < Short.MIN_VALUE || this.iValue > Short.MAX_VALUE) { throw new NumberFormatException(
				"Value range exceeded. The value " + iValue
						+ " is not in the range of a Short value which is inbetween "
						+ Short.MIN_VALUE + " .. " + Short.MAX_VALUE); }

		return (short) iValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#intValue()
	 */
	public int intValue() {
		return iValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#longValue()
	 */
	public long longValue() {
		return iValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#floatValue()
	 */
	public float floatValue() {
		return iValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#doubleValue()
	 */
	public double doubleValue() {
		return iValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (new Integer(iValue)).hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return new UnsignedInt16(this.iValue);
	}
}
