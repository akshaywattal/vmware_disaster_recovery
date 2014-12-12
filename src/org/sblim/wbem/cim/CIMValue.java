/**
 * CIMValue.java
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
 * 1535756    2006-08-08  lupusalex    Make code warning free
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.cim;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import org.sblim.wbem.util.CharUtils;

public class CIMValue implements Serializable, Cloneable {

	private static final long serialVersionUID = -1019257998440613738L;

	private Object iValue;

	private CIMDataType iType;

	public static final CIMValue FALSE = new CIMValue(Boolean.FALSE, CIMDataType.getPredefinedType(CIMDataType.BOOLEAN));

	public static final CIMValue TRUE = new CIMValue(Boolean.TRUE, CIMDataType.getPredefinedType(CIMDataType.BOOLEAN));

	/**
	 * Construct an object of a CIMValue. Other constructors are preferred than
	 * this one, because it may not determine the appropriate value type of the
	 * object passed as argument.
	 * 
	 * @param pValue
	 *            The value
	 * @deprecated this constructor may lead to confusion when the data type of
	 *             the specified argument can not be determined, such as the
	 *             case of null arguments.
	 */
	public CIMValue(Object pValue) {
		if (pValue instanceof Object[]) {
			pValue = new Vector(Arrays.asList((Object[]) pValue));
		}
		if (pValue instanceof Vector) {
			int dataType = CIMDataType.findType(pValue);

			this.iType = CIMDataType.getPredefinedType(dataType);

			Vector newValue = (Vector) pValue;
			Iterator iter = newValue.iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (dataType != CIMDataType.findArrayType(CIMDataType.findType(obj))) throw new IllegalArgumentException(
						"Invalid data type. The array contains elements that do not match arrays type");
			}
			this.iValue = newValue;
		} else {
			int dataType = CIMDataType.findType(pValue);
			if (dataType == CIMDataType.REFERENCE) { // handle the special
				// case of the REFERENCE
				this.iType = new CIMDataType(((CIMObjectPath) pValue).getObjectName());
			} else this.iType = new CIMDataType(dataType);
			this.iValue = pValue;
		}
	}

	/**
	 * Construct an object of a cim value, using the specified data type.
	 * 
	 * @param pValue
	 *            the value object contained by this CIMValue.
	 * @param pDataType
	 *            the data type for this CIMValue.
	 * @throws IllegalArgumentException
	 */
	public CIMValue(Object pValue, CIMDataType pDataType) {
		if (pValue instanceof Object[]) {
			pValue = new Vector(Arrays.asList((Object[]) pValue));

			if (pValue != null) {
				if (pDataType != null
						&& (!pDataType.isArrayType() || (pDataType.getType() == CIMDataType.INVALID))) throw new IllegalArgumentException(
						"Invalid argument type. Array type expected");
				Iterator iter = ((Vector) pValue).iterator();
				while (iter.hasNext()) {
					Object obj = iter.next();
					int elementType = CIMDataType.findArrayType(CIMDataType.findType(obj));
					if (!CIMDataType.isTypeCompatible(obj, pDataType)
							&& elementType != CIMDataType.NULL) throw new IllegalArgumentException(
							"Invalid element type. One of the elements"
									+ " of the array does not match the data type or a compatible data type of the array");
				}
				this.iValue = pValue;
			}

		} else {
			if (pValue != null) {
				if ((pValue instanceof Vector) || (pDataType != null && pDataType.isArrayType())) throw new IllegalArgumentException(
						"value type does not match the specified data type");
			}

			if (pDataType != null
					&& pValue != null
					&& (!CIMDataType.isTypeCompatible(pValue, pDataType) || pDataType.getType() == CIMDataType.INVALID)) throw new IllegalArgumentException(
					"Invalid argument type. The value contains an element that does not match the data type or a compatible data type."+
					"\ndata type:"+pDataType.toString()+
					"\nclassName of value:"+pValue.getClass().getName());

			this.iValue = pValue;
		}
		this.iType = pDataType;
	}

	/**
	 * Construct an object of cim value for a given vector, and the specified
	 * data type.
	 * 
	 * @param pValue
	 *            A vector of objects
	 * @param pDataType
	 *            An array data type
	 * @throws IllegalArgumentException
	 *             if the elements from the vector does not match the specified
	 *             data type.
	 */
	public CIMValue(Vector pValue, CIMDataType pDataType) {

		if (pValue != null) {
			if (!(pDataType != null && pDataType.isArrayType())) throw new IllegalArgumentException(
					"Array data type expected");
		}

		if (pValue != null) {
			if (pDataType != null
					&& (!pDataType.isArrayType() || (pDataType.getType() == CIMDataType.INVALID))) throw new IllegalArgumentException(
					"Invalid argument type. Array type expected");

			Vector newValue = pValue;
			Iterator iter = newValue.iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				int elementType = CIMDataType.findArrayType(CIMDataType.findType(obj));
				if (!CIMDataType.isTypeCompatible(obj, pDataType)
						&& elementType != CIMDataType.NULL) throw new IllegalArgumentException(
						"Invalid element type. One of the elements"
								+ " of the array does not match the data type or a compatible data type of the array");
			}
			this.iValue = newValue;
		}

		this.iType = pDataType;
	}

	protected CIMValue() {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		CIMValue that = new CIMValue();

		if (this.iValue instanceof Vector) that.iValue = ((Vector) iValue).clone();
		else that.iValue = iValue;

		if (this.iType != null) that.iType = (CIMDataType) this.iType.clone();
		return that;
	}

	/**
	 * Determines if the object argument passed to the method is contained as
	 * part of the vector or if the object argument is equivalent to the current
	 * value.
	 * 
	 * @param pValue
	 *            The value
	 * @return <code>true</code> if pValue is contained in this cim value,
	 *         <code>false</code> otherwise
	 */
	public boolean contains(Object pValue) {
		if (pValue == null) return false;

		if (iValue != null) {
			if (iValue instanceof Vector) return ((Vector) iValue).contains(pValue);
			return iValue.equals(pValue);
		}
		return false;
	}

	/**
	 * Determine no object has been assigned to the CIMValue. This is if the
	 * value == null && the value type != null.
	 * 
	 * @return <code>true</code> if value is equal to <code>null</code> and
	 *         data type is not null.
	 */
	public boolean isEmpty() {
		return (iValue == null && iType != null);
	}

	/**
	 * Determines if the current value is null.
	 * 
	 * @return <code>true</code> if the current value is equal to
	 *         <code>null</code>, otherwise returns <code>false</code>.
	 */
	public boolean isNull() {
		return iValue == null;
	}

	/**
	 * Determines if the current value is null.
	 * 
	 * @return <code>true</code> if the current value is equal to
	 *         <code>null</code>, otherwise returns <code>false</code>.
	 */
	public boolean isNullValue() {
		return isNull();
	}

	/**
	 * Returns the size of the current value. If the value is a vector type, the
	 * size of the vector. If the value is equals to null, and a data type is
	 * provided returns the size of the data type, otherwise returns a
	 * SIZE_SINGLE value.
	 * 
	 * @return The size
	 */
	public int getSize() {
		if (iValue instanceof Vector) {
			return ((Vector) iValue).size();
		} else if (iType != null) { return iType.getSize(); }
		return CIMDataType.SIZE_SINGLE;
	}

	/**
	 * Return the size of the current value. If the value is a vector type,
	 * returns the size of the vector. If the value is equals to null, and a
	 * data type is provided returns the size of the data type, otherwise
	 * returns a SIZE_SINGLE value.
	 * 
	 * @deprecated use getSize instead of size()
	 * @return The size
	 */
	public int size() {
		return getSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null) return false;

		if (!(obj instanceof CIMValue)) return false;
		CIMValue that = (CIMValue) obj;

		if (!(this.iType == null ? that.iType == null : this.iType.equals(that.iType))) return false;

		return (this.iValue == null ? that.iValue == null : this.iValue.equals(that.iValue));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return ((iValue != null) ? iValue.hashCode() : 0) << 16 + ((iType != null) ? iType
				.hashCode() : 0);
	}

	/**
	 * Return the data type for this object.
	 * 
	 * @return The type
	 */
	public CIMDataType getType() {
		return iType;
	}

	/**
	 * Returns the value assigned to this object.
	 * 
	 * @return The value
	 */
	public Object getValue() {
		return iValue;
	}

	/**
	 * Determines if the current value is an array type.
	 * 
	 * @return <code>true</code> if the value is an array, <code>false</code>
	 *         otherwise
	 */
	public boolean isArrayValue() {
		return (iValue instanceof Vector) || iType.isArrayType();
	}

	/**
	 * Returns the MOF representation of this element.
	 * 
	 * @return The MOF representation
	 */
	public String toMOF() {
		StringBuffer buf = new StringBuffer();

		if (iValue != null) {
			if (iType != null && iType.isArrayType()) buf.append(CIMElement.vectorToMOFString(
					((Vector) iValue), false, 1));
			else {
				if (iValue instanceof String || iValue instanceof CIMObjectPath) {
					buf.append('\"');
					buf.append(CharUtils.escape(iValue.toString()));
					buf.append('\"');
				} else if (iValue instanceof Character) {
					buf.append('\'');
					buf.append(CharUtils.escape(iValue.toString()));
					buf.append('\'');
				} else {
					buf.append(String.valueOf(iValue));
				}
			}
		} else {
			buf.append("null");
		}
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return toMOF();
	}

	public static void main(String[] args) {
		CIMValue value1 = new CIMValue((Object) null, CIMDataType
				.getPredefinedType(CIMDataType.CHAR16));
		CIMValue value2 = new CIMValue("string");
		System.out.println(value1);
		System.out.println(value2);
		System.out.println("equals() = " + value1.equals(value2));
		String[] strArray = new String[2];
		strArray[0] = "hello";
		strArray[1] = "bye";
		CIMValue value3 = new CIMValue(strArray, CIMDataType
				.getPredefinedType(CIMDataType.STRING_ARRAY));
		System.out.println(value3);
	}
}
