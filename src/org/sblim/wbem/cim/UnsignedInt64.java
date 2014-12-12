/**
 * UnsignedInt64.java
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
 * 1487705    2006-05-29  taphorn      sblimCIMClient throws NumberFormatException in UnsignedInt64
 * 1535756    2006-08-08  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.cim;

import java.math.BigInteger;

/**
 * Implements a 64-bit unsigned integer object.
 * 
 * The value range of an unsigned integer of 64-bit is '0 ..
 * 18446744073709551615'.
 */
public class UnsignedInt64 extends Number implements java.io.Serializable, Cloneable {

	private static final long serialVersionUID = 4758276753882089604L;

	/**
	 * The minimum value a unsigned integer of 64-bit value can be. Its value is
	 * '0'.
	 */
	public static BigInteger MIN_VALUE = new BigInteger("0");

	/**
	 * The maximum value a unsigned integer of 64-bit value can be. Its value is
	 * '18446744073709551615'.
	 */
	public static BigInteger MAX_VALUE = (new BigInteger("1")).shiftLeft(64).subtract(
			new BigInteger("1"));

	private BigInteger iValue;

	/**
	 * Constructs an unsigned 64-bit integer object for the specified byte
	 * value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt64(byte pValue) {
		iValue = new BigInteger(Byte.toString(pValue));

		if (iValue.compareTo(MIN_VALUE) == (-1) || iValue.compareTo(MAX_VALUE) == 1) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }
	}

	/**
	 * Constructs an unsigned 64-bit integer object for the specified short
	 * value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt64(short pValue) {
		iValue = new BigInteger(Short.toString(pValue));

		if (iValue.compareTo(MIN_VALUE) == (-1) || iValue.compareTo(MAX_VALUE) == 1) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }
	}

	/**
	 * Constructs an unsigned 64-bit integer object for the specified int value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt64(int pValue) {
		iValue = new BigInteger(Integer.toString(pValue));

		if (iValue.compareTo(MIN_VALUE) == (-1) || iValue.compareTo(MAX_VALUE) == 1) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }
	}

	/**
	 * Constructs an unsigned 64-bit integer object for the specified long
	 * value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt64(long pValue) {
		iValue = new BigInteger(Long.toString(pValue));

		if (iValue.compareTo(MIN_VALUE) == (-1) || iValue.compareTo(MAX_VALUE) == 1) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }
	}

	/**
	 * Constructs an unsigned 64-bit integer object for the specified BigInteger
	 * value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt64(BigInteger pValue) {
		if (pValue == null) { throw new IllegalArgumentException("null bigInteger argument"); }

		iValue = new BigInteger(pValue.toString());

		if (iValue.compareTo(MIN_VALUE) < 0 || iValue.compareTo(MAX_VALUE) > 0) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }
	}

	/**
	 * Constructs an unsigned 64-bit integer object for the specified String
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
	public UnsignedInt64(String pValue) {
		if (pValue == null) {
			throw new IllegalArgumentException("null string argument");

		} else if (pValue.trim().length() == 0) { throw new IllegalArgumentException(
				"empty string argument"); }

		iValue = new BigInteger(pValue);

		if (iValue.compareTo(MIN_VALUE) < 0 || iValue.compareTo(MAX_VALUE) > 0) { throw new IllegalArgumentException(
				"Invalid value range. Value must be in a " + MIN_VALUE + ".." + MAX_VALUE
						+ " range"); }
	}

	/**
	 * Constructs an unsigned 64-bit integer object for the specified
	 * UnsignedInt8 value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt64(UnsignedInt8 pValue) {
		iValue = new BigInteger(pValue.toString());
	}

	/**
	 * Constructs an unsigned 64-bit integer object for the specified
	 * UnsignedInt16 value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt64(UnsignedInt16 pValue) {
		iValue = new BigInteger(pValue.toString());
	}

	/**
	 * Constructs an unsigned 64-bit integer object for the specified
	 * UnsignedInt32 value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt64(UnsignedInt32 pValue) {
		iValue = new BigInteger(pValue.toString());
	}

	/**
	 * Constructs an unsigned 64-bit integer object for the specified
	 * UnsignedInt64 value.
	 * 
	 * @param pValue
	 *            The value of the created object
	 * 
	 * @throws IllegalArgumentException
	 *             If value does not fit into the MIN_VALUE .. MAX_VALUE range.
	 */
	public UnsignedInt64(UnsignedInt64 pValue) {
		iValue = new BigInteger(pValue.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return iValue.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof UnsignedInt64) { return (iValue.compareTo((((UnsignedInt64) o)).iValue) == 0 ? true
				: false); }
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#byteValue()
	 */
	public byte byteValue() {
		if (iValue.compareTo(new BigInteger(Byte.toString(Byte.MAX_VALUE))) > 0) { throw new NumberFormatException(
				"Value range exceeded. The value " + iValue
						+ " is not in the range of a Byte value which is inbetween "
						+ Byte.MIN_VALUE + " .. " + Byte.MAX_VALUE); }

		return iValue.byteValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#shortValue()
	 */
	public short shortValue() {
		if (iValue.compareTo(new BigInteger(Short.toString(Short.MAX_VALUE))) > 0) { throw new NumberFormatException(
				"Value range exceeded. The value " + iValue
						+ " is not in the range of a Short value which is inbetween "
						+ Short.MIN_VALUE + " .. " + Short.MAX_VALUE); }

		return iValue.shortValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#intValue()
	 */
	public int intValue() {
		if (iValue.compareTo(new BigInteger(Integer.toString(Integer.MAX_VALUE))) > 0) { throw new NumberFormatException(
				"Value range exceeded. The value " + iValue
						+ " is not in the range of a Integer value which is inbetween "
						+ Integer.MIN_VALUE + " .. " + Integer.MAX_VALUE); }

		return iValue.intValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#longValue()
	 */
	public long longValue() {
		if (iValue.compareTo(new BigInteger(Long.toString(Long.MAX_VALUE))) > 0) { throw new NumberFormatException(
				"Value range exceeded. The value " + iValue
						+ " is not in the range of a Long value which is inbetween "
						+ Long.MIN_VALUE + " .. " + Long.MAX_VALUE); }

		return iValue.longValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#floatValue()
	 */
	public float floatValue() {
		if (iValue.floatValue() == Float.NEGATIVE_INFINITY
				|| iValue.floatValue() == Float.POSITIVE_INFINITY) { throw new NumberFormatException(
				"Value range exceeded. The value " + iValue
						+ " is not in the range of a Float value which is inbetween "
						+ Float.MIN_VALUE + " .. " + Float.MAX_VALUE); }

		return iValue.floatValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#doubleValue()
	 */
	public double doubleValue() {
		if (iValue.doubleValue() == Double.NEGATIVE_INFINITY
				|| iValue.doubleValue() == Double.POSITIVE_INFINITY) { throw new NumberFormatException(
				"Value range exceeded. The value " + iValue
						+ " is not in the range of a Double value which is inbetween "
						+ Double.MIN_VALUE + " .. " + Double.MAX_VALUE); }

		return iValue.doubleValue();
	}

	/**
	 * Returns the value if this unsigned integer as a BigInteger.
	 * 
	 * @return This UnsignedInt64 in a BigInteger representation.
	 */
	public BigInteger bigIntValue() {
		return new BigInteger(iValue.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return iValue.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return new UnsignedInt64(iValue);
	}
}
