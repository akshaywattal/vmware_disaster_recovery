/**
 * UnsignedInt32.java
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
 *  
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1535756    2006-08-08  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.cim;

/**
 * Implements a 32-bit unsigned integer object.
 * 
 * The value range of an unsigned integer of 32-bit is '0 .. 4294967295'.
 */
public class UnsignedInt32 extends Number implements java.io.Serializable, Cloneable {

	private static final long serialVersionUID = -9063069511184239822L;

	/**
	 * The minimum value a unsigned integer of 32-bit value can be. Its value is
	 * '0'.
	 */
	public static long MIN_VALUE = 0;

	/**
	 * The maximum value a unsigned integer of 32-bit value can be. Its value is
	 * '4294967295L'.
	 */
	public static long MAX_VALUE = 4294967295L;

	private long iValue;

	/**
	 * Constructs an unsigned 32-bit integer object for the specified byte
	 * value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt32(byte pValue) {
		if (pValue < MIN_VALUE || pValue > MAX_VALUE) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }

		iValue = pValue;
	}

	/**
	 * Constructs an unsigned 32-bit integer object for the specified short
	 * value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt32(short pValue) {
		if (pValue < MIN_VALUE || pValue > MAX_VALUE) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }

		iValue = pValue;
	}

	/**
	 * Constructs an unsigned 32-bit integer object for the specified int value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt32(int pValue) {
		if (pValue < MIN_VALUE || pValue > MAX_VALUE) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }

		iValue = pValue;
	}

	/**
	 * Constructs an unsigned 32-bit integer object for the specified long
	 * value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt32(long pValue) {
		if (pValue < MIN_VALUE || pValue > MAX_VALUE) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }

		iValue = pValue;
	}

	/**
	 * Constructs an unsigned 32-bit integer object for the specified String
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
	public UnsignedInt32(String pValue) {
		if (pValue == null) {
			throw new IllegalArgumentException("null string argument");

		} else if (pValue.trim().length() == 0) { throw new IllegalArgumentException(
				"empty string argument"); }

		iValue = Long.parseLong(pValue);

		if (iValue < MIN_VALUE || iValue > MAX_VALUE) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }
	}

	/**
	 * Constructs an unsigned 32-bit integer object for the specified
	 * UnsignedInt8 value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt32(UnsignedInt8 pValue) {
		iValue = pValue.longValue();
	}

	/**
	 * Constructs an unsigned 32-bit integer object for the specified
	 * UnsignedInt16 value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt32(UnsignedInt16 pValue) {
		iValue = pValue.longValue();
	}

	/**
	 * Constructs an unsigned 32-bit integer object for the specified
	 * UnsignedInt32 value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt32(UnsignedInt32 pValue) {
		iValue = pValue.longValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return Long.toString(iValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof UnsignedInt32) { return iValue == (((UnsignedInt32) o).iValue); }
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#byteValue()
	 */
	public byte byteValue() {
		if (iValue < Byte.MIN_VALUE || iValue > Byte.MAX_VALUE) { throw new NumberFormatException(
				"Value range exceeded. The value " + iValue
						+ " is not in the range of a Byte value which is inbetween "
						+ Byte.MIN_VALUE + " .. " + Byte.MAX_VALUE); }

		return (byte) iValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#shortValue()
	 */
	public short shortValue() {
		if (iValue < Short.MIN_VALUE || iValue > Short.MAX_VALUE) { throw new NumberFormatException(
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
		if (iValue < Integer.MIN_VALUE || iValue > Integer.MAX_VALUE) { throw new NumberFormatException(
				"Value range exceeded. The value " + iValue
						+ " is not in the range of a Integer value which is inbetween "
						+ Integer.MIN_VALUE + " .. " + Integer.MAX_VALUE); }

		return (int) iValue;
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
		return (new Long(iValue)).hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return new UnsignedInt32(iValue);
	}
}
