/**
 * CIMDataType.java
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
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1657901    2007-02-12  ebak         Performance issues
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.cim;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Provides the basic interface to define CIM data type defined by the CIM
 * specification.
 * 
 */
public class CIMDataType implements Serializable, Cloneable {

	private static final long serialVersionUID = 8636134893815214662L;

	private final static int SINGLE_BASE = 0;

	private final static int ARRAY_BASE = 16;

	private final static int SPECIAL_BASE = 31;

	/**
	 * Invalid type.
	 */
	public final static int INVALID = -1;

	/**
	 * Unsigned 8-bit integer.
	 */
	public final static int UINT8 = 0 + SINGLE_BASE;

	/**
	 * Signed 8-bit integer.
	 */
	public final static int SINT8 = 1 + SINGLE_BASE;

	/**
	 * Unsigned 16-bit integer.
	 */
	public final static int UINT16 = 2 + SINGLE_BASE;

	/**
	 * Signed 16-bit integer.
	 */
	public final static int SINT16 = 3 + SINGLE_BASE;

	/**
	 * Unsigned 32-bit integer.
	 */
	public final static int UINT32 = 4 + SINGLE_BASE;

	/**
	 * Signed 32-bit integer.
	 */
	public final static int SINT32 = 5 + SINGLE_BASE;

	/**
	 * Unsigned 64-bit integer.
	 */
	public final static int UINT64 = 6 + SINGLE_BASE;

	/**
	 * Signed 64-bit integer.
	 */
	public final static int SINT64 = 7 + SINGLE_BASE;

	/**
	 * UCS String.
	 */
	public final static int STRING = 8 + SINGLE_BASE;

	/**
	 * Boolean.
	 */
	public final static int BOOLEAN = 9 + SINGLE_BASE;

	/**
	 * IEEE 4-byte floating point.
	 */
	public final static int REAL32 = 10 + SINGLE_BASE;

	/**
	 * IEEE 8-byte floating point.
	 */
	public final static int REAL64 = 11 + SINGLE_BASE;

	/**
	 * A string which defines a datetime value according the CIM specification.
	 */
	public final static int DATETIME = 12 + SINGLE_BASE;

	/**
	 * 16 bit UCS-2 character.
	 */
	public final static int CHAR16 = 13 + SINGLE_BASE;

	/**
	 * Reference type.
	 */
	public final static int REFERENCE = 14 + SINGLE_BASE;

	/**
	 * Numeric type for key properties
	 */
	public final static int NUMERIC = 15 + SINGLE_BASE;

	public final static int UINT8_ARRAY = UINT8 + ARRAY_BASE;

	public final static int SINT8_ARRAY = SINT8 + ARRAY_BASE;

	public final static int UINT16_ARRAY = UINT16 + ARRAY_BASE;

	public final static int SINT16_ARRAY = SINT16 + ARRAY_BASE;

	public final static int UINT32_ARRAY = UINT32 + ARRAY_BASE;

	public final static int SINT32_ARRAY = SINT32 + ARRAY_BASE;

	public final static int UINT64_ARRAY = UINT64 + ARRAY_BASE;

	public final static int SINT64_ARRAY = SINT64 + ARRAY_BASE;

	public final static int STRING_ARRAY = STRING + ARRAY_BASE;

	public final static int BOOLEAN_ARRAY = BOOLEAN + ARRAY_BASE;

	public final static int REAL32_ARRAY = REAL32 + ARRAY_BASE;;

	public final static int REAL64_ARRAY = REAL64 + ARRAY_BASE;;

	public final static int DATETIME_ARRAY = DATETIME + ARRAY_BASE;;

	public final static int CHAR16_ARRAY = CHAR16 + ARRAY_BASE;;

	public final static int REFERENCE_ARRAY = REFERENCE + ARRAY_BASE;;

	/**
	 * CIMInstance type.
	 */
	public final static int OBJECT = 0 + SPECIAL_BASE;

	/**
	 * Null type.
	 */
	public final static int NULL = 1 + SPECIAL_BASE;

	/**
	 * Class type.
	 */
	public final static int CLASS = 2 + SPECIAL_BASE;

	public final static int SIZE_SINGLE = -1;

	public final static int SIZE_UNLIMITED = -2;

	public final static int MIN_SINGLE_TYPE = UINT8;

	public final static int MAX_SINGLE_TYPE = NUMERIC;

	public final static int MIN_ARRAY_TYPE = UINT8_ARRAY;

	public final static int MAX_ARRAY_TYPE = REFERENCE_ARRAY;

	private final static int MAX_DATA_TYPE = CLASS;

	private static CIMDataType[] predefinedDataType = new CIMDataType[MAX_DATA_TYPE + 1];

	static {
		predefinedDataType[UINT8] = new CIMDataType(UINT8);
		predefinedDataType[UINT16] = new CIMDataType(UINT16);
		predefinedDataType[UINT32] = new CIMDataType(UINT32);
		predefinedDataType[UINT64] = new CIMDataType(UINT64);
		predefinedDataType[SINT8] = new CIMDataType(SINT8);
		predefinedDataType[SINT16] = new CIMDataType(SINT16);
		predefinedDataType[SINT32] = new CIMDataType(SINT32);
		predefinedDataType[SINT64] = new CIMDataType(SINT64);
		predefinedDataType[STRING] = new CIMDataType(STRING);
		predefinedDataType[CHAR16] = new CIMDataType(CHAR16);
		predefinedDataType[BOOLEAN] = new CIMDataType(BOOLEAN);
		predefinedDataType[REAL32] = new CIMDataType(REAL32);
		predefinedDataType[REAL64] = new CIMDataType(REAL64);
		predefinedDataType[DATETIME] = new CIMDataType(DATETIME);
		predefinedDataType[REFERENCE] = new CIMDataType(REFERENCE);
		predefinedDataType[NUMERIC] = new CIMDataType(NUMERIC);

		predefinedDataType[UINT8_ARRAY] = new CIMDataType(UINT8_ARRAY);
		predefinedDataType[UINT16_ARRAY] = new CIMDataType(UINT16_ARRAY);
		predefinedDataType[UINT32_ARRAY] = new CIMDataType(UINT32_ARRAY);
		predefinedDataType[UINT64_ARRAY] = new CIMDataType(UINT64_ARRAY);
		predefinedDataType[SINT8_ARRAY] = new CIMDataType(SINT8_ARRAY);
		predefinedDataType[SINT16_ARRAY] = new CIMDataType(SINT16_ARRAY);
		predefinedDataType[SINT32_ARRAY] = new CIMDataType(SINT32_ARRAY);
		predefinedDataType[SINT64_ARRAY] = new CIMDataType(SINT64_ARRAY);
		predefinedDataType[STRING_ARRAY] = new CIMDataType(STRING_ARRAY);
		predefinedDataType[CHAR16_ARRAY] = new CIMDataType(CHAR16_ARRAY);
		predefinedDataType[BOOLEAN_ARRAY] = new CIMDataType(BOOLEAN_ARRAY);
		predefinedDataType[REAL32_ARRAY] = new CIMDataType(REAL32_ARRAY);
		predefinedDataType[REAL64_ARRAY] = new CIMDataType(REAL64_ARRAY);
		predefinedDataType[DATETIME_ARRAY] = new CIMDataType(DATETIME_ARRAY);
		predefinedDataType[REFERENCE_ARRAY] = new CIMDataType(REFERENCE_ARRAY);

		predefinedDataType[OBJECT] = new CIMDataType(OBJECT);
		predefinedDataType[NULL] = new CIMDataType(NULL);
		predefinedDataType[CLASS] = new CIMDataType(CLASS);
	}

	public static final CIMDataType INVALID_DATATYPE = new CIMDataType(INVALID);

	public static final CIMDataType NULL_DATATYPE = new CIMDataType(NULL);

	private static final String EMPTY = "";

	private int type = INVALID;

	private int size = SIZE_SINGLE;

	private String referenceClass = EMPTY;

	/**
	 * Constructs an object of cim data type using the specified data type
	 * value. If the specified data type is an array type, assigns the size
	 * field to SIZE_UNLIMITED, otherwise assign it to SIZE_SINGLE.
	 * 
	 * @param pType
	 *            One of the type constants in this class
	 * @throws IllegalArgumentException
	 *             if the specified data type is invalid
	 */
	public CIMDataType(int pType) {
		setType(pType);
	}

	/**
	 * Constructs a cim data type with the specified type and size. A valid type
	 * value must be passed. This must be a value between MIN_SINGLE_TYPE and
	 * MAX_SINGLE_TYPE or between MIN_ARRAY_TYPE and MAX_ARRAY_TYPE.
	 * Additionally a valid size must be specified. A positive value or zero
	 * indicates that the array length is fixed. A value of SIZE_UNLIMITED means
	 * that the array is of variable size A value of SIZE_SINGLE is not
	 * meaningful.
	 * 
	 * @param pType
	 *            One of the type constants in this class
	 * @param pSize
	 *            The desirtd size SIZE_UNLIMITED, 0..MAX_INT_SIZE
	 * @throws IllegalArgumentException
	 *             if the data type is invalid or if the size is invalid.
	 */
	public CIMDataType(int pType, int pSize) {
		if (!(pType >= MIN_ARRAY_TYPE && pType <= MAX_ARRAY_TYPE)) {
			if (pType >= MIN_SINGLE_TYPE && pType <= MAX_SINGLE_TYPE) pType += ARRAY_BASE;
			else throw new IllegalArgumentException("Invalid data type:" + pType);
		}
		if (pSize < SIZE_UNLIMITED || pSize == SIZE_SINGLE) throw new IllegalArgumentException(
				"Invalid size of the data type:" + pSize);

		this.type = pType;
		this.size = pSize;
	}

	private CIMDataType() {}

	/**
	 * Construct a CIMDataType which is a references of the specified CIMClass.
	 * 
	 * @param pRefClassName
	 *            The referenced class' name
	 */
	public CIMDataType(String pRefClassName) {
		this.type = REFERENCE;
		this.size = SIZE_SINGLE;
		if (pRefClassName == null) this.referenceClass = EMPTY;
		else this.referenceClass = pRefClassName;
	}

	/**
	 * Construct a CIMDataType which is an array of references of the specified
	 * CIMClass.
	 * 
	 * @param pRefClassName
	 *            The referenced class' name
	 * @param pSize
	 *            The size of the array
	 */
	public CIMDataType(String pRefClassName, int pSize) {
		if (pSize < SIZE_UNLIMITED) throw new IllegalArgumentException("invalid size argument: "
				+ pSize);
		this.size = pSize;
		if (this.size >= 0 || this.size == SIZE_UNLIMITED) this.type = REFERENCE_ARRAY;
		else this.type = REFERENCE;
		if (pRefClassName == null) this.referenceClass = EMPTY;
		else this.referenceClass = pRefClassName;
	}

	/**
	 * Returns the class name pointed by this data type.
	 * 
	 * @return The referenced class' name
	 */
	public String getRefClassName() {
		return referenceClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;

		if (obj instanceof CIMDataType) {
			CIMDataType that = (CIMDataType) obj;

			if (this.type == that.type
					&& this.size == that.size
					&& (this.referenceClass == null ? that.referenceClass == null
							: this.referenceClass.equalsIgnoreCase(that.referenceClass))) return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return referenceClass.toUpperCase().hashCode() << 16 + size << 8 + type;
	}

	/**
	 * Returns the type of this object.
	 * 
	 * @return The type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the current type of the object. Additionally, modifies the size
	 * field to SIZE_UNLIMITED if specified data type is an array type,
	 * otherwise assigns it to SIZE_SINGLE
	 * 
	 * @param pType
	 *            The type
	 * @throws IllegalArgumentException
	 *             if the assigned data type is invalid
	 */
	public void setType(int pType) {
		if (pType == REFERENCE || pType == REFERENCE_ARRAY || pType == NULL) {
			// TODO what to do when is a reference, or null?
		}
		if (pType >= MIN_ARRAY_TYPE && pType <= MAX_ARRAY_TYPE) {
			this.size = SIZE_UNLIMITED;
		} else if (pType >= MIN_SINGLE_TYPE && pType <= MAX_SINGLE_TYPE) {
			this.size = SIZE_SINGLE;
		} else if (pType != INVALID && pType != NULL && pType != CLASS && pType != OBJECT) { throw new IllegalArgumentException(
				"Invalid data type"); }

		this.type = pType;
	}

	/**
	 * Returns the current size of the data type.
	 * 
	 * @return an integer which represents the size of the data type. A value of
	 *         zero or a positive value means that the data type is an array
	 *         type, and the value represent the size of the array. If the
	 *         returned value is SIZE_UNLIMITED, the data type represents an
	 *         unlimited size array, which means that the size has not been
	 *         specified. If the returned value is SIZE_SINGLE, the data type
	 *         represents a single element entity.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Returns an integer value that represents the data type for the specified
	 * object. (i.e. NULL for null values, UINT8 for Unsigned8 values, and so
	 * on). If the value type is undetermined, return INVALID type.
	 * 
	 * @param pValue
	 *            The value
	 * @return The CIM data type fitting the value
	 */
	public static int findType(Object pValue) {
		if (pValue == null) {
			return NULL;
		} else if (pValue instanceof Vector) {
			return findType((Vector) pValue);
		} else if (pValue instanceof UnsignedInt8) {
			return UINT8;
		} else if (pValue instanceof UnsignedInt16) {
			return UINT16;
		} else if (pValue instanceof UnsignedInt32) {
			return UINT32;
		} else if (pValue instanceof UnsignedInt64) {
			return UINT64;
		} else if (pValue instanceof Byte) {
			return SINT8;
		} else if (pValue instanceof Short) {
			return SINT16;
		} else if (pValue instanceof Integer) {
			return SINT32;
		} else if (pValue instanceof Long) {
			return SINT64;
		} else if (pValue instanceof String) {
			return STRING;
		} else if (pValue instanceof Boolean) {
			return BOOLEAN;
		} else if (pValue instanceof Float) {
			return REAL32;
		} else if (pValue instanceof Double) {
			return REAL64;
		} else if (pValue instanceof CIMDateTime) {
			return DATETIME;
		} else if (pValue instanceof CIMObjectPath) {
			return REFERENCE;
		} else if (pValue instanceof CIMInstance) {
			return OBJECT;
		} else if (pValue instanceof CIMClass) {
			return CLASS;
		} else if (pValue instanceof Character) {
			return CHAR16;
		} else if (pValue instanceof Numeric) { return NUMERIC; }
		return INVALID;
	}

	private static int findType(Vector pVector) {

		if (pVector == null) return NULL;

		Iterator iter = pVector.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj != null) return findArrayType(findType(obj));
		}
		return NULL;
	}
	
	private static class TypeFactory {
			
		private static HashMap cScalarTypeMap;
		private static HashMap cArrayTypeMap;
		
		private TypeFactory() {}
		
		private static void add(String pTypeStr, int pTypeCode) {
			cScalarTypeMap.put(pTypeStr, getPredefinedType(pTypeCode));
			if (pTypeCode >= MIN_SINGLE_TYPE && pTypeCode <= MAX_SINGLE_TYPE)
				cArrayTypeMap.put(pTypeStr, getPredefinedType(pTypeCode+ARRAY_BASE));
		}
		
		private static void initMaps() {
			if (cScalarTypeMap!=null) return;
			cScalarTypeMap=new HashMap();
			cArrayTypeMap=new HashMap();
			synchronized (cScalarTypeMap) {
				synchronized (cArrayTypeMap) {
					add("uint8", UINT8);
					add("uint16", UINT16);
					add("uint32", UINT32);
					add("uint64", UINT64);
					add("sint8", SINT8);
					add("sint16", SINT16);
					add("sint32", SINT32);
					add("sint64", SINT64);
					add("string", STRING);
					add("boolean", BOOLEAN);
					add("real32", REAL32);
					add("real64", REAL64);
					add("datetime", DATETIME);
					add("ref", REFERENCE);
					add("reference", REFERENCE);
					add("char16", CHAR16);
					add("numeric", NUMERIC);
				}
			}
		}
	
		public static CIMDataType getScalar(String pTypeStr) {
			initMaps();
			pTypeStr=pTypeStr.toLowerCase();
			CIMDataType type;
			synchronized (cScalarTypeMap) {
				type = (CIMDataType)cScalarTypeMap.get(pTypeStr);
			}
			if (type==null) return getPredefinedType(STRING);
			return (CIMDataType)type.clone();
		}
		
		public static CIMDataType getArray(String pTypeStr) {
			initMaps();
			pTypeStr=pTypeStr.toLowerCase();
			CIMDataType type;
			synchronized (cArrayTypeMap) {
				type = (CIMDataType)cArrayTypeMap.get(pTypeStr);
			}
			if (type==null) return getPredefinedType(STRING+ARRAY_BASE);
			return (CIMDataType)type.clone();
		}
		
	}
	
	
	/**
	 * Returns the corresponding CIMDataType object for the specified string type.
	 * Given a string representation of the data type (i.e. "uint16") returns the
	 * appropriate CIMDataType. The isArray argument determine if the resulting
	 * data type must be an array type or a single type element.
	 * @param typeStr
	 * @param isArray
	 * @return CIMDataType
	 */
	public static CIMDataType getDataType(String typeStr, boolean isArray) {
		return isArray ? TypeFactory.getArray(typeStr) : TypeFactory.getScalar(typeStr);
	}

	/**
	 * Return a predefined value for the CIMDataType. Applications are encourage
	 * to use this method instead of creating new CIMDataType objects. An
	 * exception to use this is when an array data type, with an specific size
	 * other than unlimited size, needs to be used.
	 * 
	 * @param pType
	 *            One of the data type constants in this class
	 * @return The data type
	 */
	public static CIMDataType getPredefinedType(int pType) {
		try {
			if (pType == INVALID) { return INVALID_DATATYPE; }
			return (CIMDataType) (predefinedDataType[pType].clone());

		} catch (Exception e) {
			return INVALID_DATATYPE;
		}
	}

	/**
	 * Defermines if the current data type is an array type.
	 * 
	 * @return <code>true</code> if this type is an array type,
	 *         <code>false</code> otherwise
	 */
	public boolean isArrayType() {
		return (type >= UINT8_ARRAY && type <= REFERENCE_ARRAY);
	}

	/**
	 * Defermines if the specified data type is is compatible with the data type
	 * of the specified value.
	 * 
	 * @param pValue
	 *            The value
	 * @param pDataType
	 *            The data type
	 * 
	 * @return <code>true</code>, if compatible, <code>false</code>otherwise
	 */
	public static boolean isTypeCompatible(Object pValue, CIMDataType pDataType) {
		boolean isCompatible = false;

		int valueType = findType(pValue);
		int valueDataType;
		if (pDataType.isArrayType()) {
			valueDataType = pDataType.getType() - ARRAY_BASE;
		} else {
			valueDataType = pDataType.getType();
		}

		switch (valueDataType) {
			case CIMDataType.UINT8:
				if ((valueType != CIMDataType.UINT8) || (valueType != CIMDataType.NUMERIC)) isCompatible = true;
				break;
			case CIMDataType.UINT16:
				if ((valueType == CIMDataType.UINT8) || (valueType != CIMDataType.UINT16)
						|| (valueType != CIMDataType.NUMERIC)) isCompatible = true;
				break;
			case CIMDataType.UINT32:
				if ((valueType == CIMDataType.UINT8) || (valueType == CIMDataType.UINT16)
						|| (valueType == CIMDataType.UINT32) || (valueType != CIMDataType.NUMERIC)) isCompatible = true;
				break;
			case CIMDataType.UINT64:
				if ((valueType == CIMDataType.UINT8) || (valueType == CIMDataType.UINT16)
						|| (valueType == CIMDataType.UINT32) || (valueType == CIMDataType.UINT64)
						|| (valueType != CIMDataType.NUMERIC)) isCompatible = true;
				break;
			case CIMDataType.SINT8:
				if ((valueType == CIMDataType.SINT8) || (valueType != CIMDataType.NUMERIC)) isCompatible = true;
				break;
			case CIMDataType.SINT16:
				if ((valueType == CIMDataType.SINT8) || (valueType == CIMDataType.SINT16)
						|| (valueType == CIMDataType.UINT8) || (valueType != CIMDataType.NUMERIC)) isCompatible = true;
				break;
			case CIMDataType.SINT32:
				if ((valueType == CIMDataType.SINT8) || (valueType == CIMDataType.SINT16)
						|| (valueType == CIMDataType.SINT32) || (valueType == CIMDataType.UINT8)
						|| (valueType == CIMDataType.UINT16) || (valueType != CIMDataType.NUMERIC)) isCompatible = true;
				break;
			case CIMDataType.SINT64:
				if ((valueType == CIMDataType.SINT8) || (valueType == CIMDataType.SINT16)
						|| (valueType == CIMDataType.SINT32) || (valueType == CIMDataType.SINT64)
						|| (valueType == CIMDataType.UINT8) || (valueType == CIMDataType.UINT16)
						|| (valueType == CIMDataType.UINT32) || (valueType != CIMDataType.NUMERIC)) isCompatible = true;
				break;
			case CIMDataType.STRING:
				if ((valueType == CIMDataType.STRING)) isCompatible = true;
				break;
			case CIMDataType.BOOLEAN:
				if ((valueType == CIMDataType.BOOLEAN)) isCompatible = true;
				break;
			case CIMDataType.REAL32:
				if ((valueType == CIMDataType.REAL32) || (valueType != CIMDataType.NUMERIC)) isCompatible = true;
				break;
			case CIMDataType.REAL64:
				if ((valueType == CIMDataType.REAL32) || (valueType != CIMDataType.REAL64)
						|| (valueType != CIMDataType.NUMERIC)) isCompatible = true;
				break;
			case CIMDataType.DATETIME:
				if ((valueType == CIMDataType.DATETIME) || (valueType == CIMDataType.STRING)) isCompatible = true;
				break;
			case CIMDataType.CHAR16:
				if ((valueType == CIMDataType.CHAR16) || (valueType == CIMDataType.STRING)) isCompatible = true;
				break;
			case CIMDataType.REFERENCE:
				if ((valueType == CIMDataType.REFERENCE)) isCompatible = true;
				break;
			case CIMDataType.OBJECT:
				if ((valueType == CIMDataType.OBJECT)) isCompatible = true;
				break;
			case CIMDataType.NUMERIC:
				if ((valueType == CIMDataType.NUMERIC) || (valueType == CIMDataType.UINT8)
						|| (valueType == CIMDataType.UINT16) || (valueType == CIMDataType.UINT32)
						|| (valueType == CIMDataType.UINT64) || (valueType == CIMDataType.SINT8)
						|| (valueType == CIMDataType.SINT16) || (valueType == CIMDataType.SINT32)
						|| (valueType == CIMDataType.SINT64) || (valueType == CIMDataType.REAL32)
						|| (valueType == CIMDataType.REAL64)) isCompatible = true;
				break;
		}

		return isCompatible;
	}

	/**
	 * Determines if the current data type is a reference type.
	 * 
	 * @return <code>true</code> if this data type is a reference type,
	 *         <code>false</code> otherwise
	 */
	public boolean isReferenceType() {
		return (type == REFERENCE_ARRAY || type == REFERENCE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		CIMDataType dt = new CIMDataType();
		dt.size = this.size;
		dt.type = this.type;
		dt.referenceClass = this.referenceClass;
		return dt;
	}

	/**
	 * Returns a value which represents the array type representation of a
	 * single type passed as an argument.
	 * 
	 * @param pSimpleType
	 *            The simple type
	 * @return returns the corresponding array element type, or INVALID if the
	 *         specified data type does not has a corresponding array type
	 *         element.
	 */
	public static int findArrayType(int pSimpleType) {
		if (pSimpleType >= UINT8 && pSimpleType <= REFERENCE) { // BB CHAR16) {
			return pSimpleType + ARRAY_BASE;
		}

		if (pSimpleType == NULL) return NULL;

		return INVALID;
	}

	/**
	 * Returns a value which represents the single type representation of an
	 * array type passed as an argument.
	 * 
	 * @param pArrayType
	 *            The array type
	 * @return returns the corresponding single element type, or INVALID if the
	 *         specified data type does not has a corresponding single type
	 *         element.
	 */
	public static int findSimpleType(int pArrayType) {
		if (pArrayType >= MIN_ARRAY_TYPE && pArrayType <= MAX_ARRAY_TYPE) { return pArrayType
				- ARRAY_BASE; }
		// TODO make sure this is valid
		if (pArrayType == NULL) return NULL;

		return INVALID;
	}

	/**
	 * Returns the string representation of this type.
	 * 
	 * @return The string representation
	 */
	public String getStringType() {
		String result;
		switch (type) {
			case CIMDataType.UINT8_ARRAY:
			case CIMDataType.UINT8:
				result = "uint8";
				break;
			case CIMDataType.UINT16_ARRAY:
			case CIMDataType.UINT16:
				result = "uint16";
				break;
			case CIMDataType.UINT32_ARRAY:
			case CIMDataType.UINT32:
				result = "uint32";
				break;
			case CIMDataType.UINT64_ARRAY:
			case CIMDataType.UINT64:
				result = "uint64";
				break;
			case CIMDataType.SINT8_ARRAY:
			case CIMDataType.SINT8:
				result = "sint8";
				break;
			case CIMDataType.SINT16_ARRAY:
			case CIMDataType.SINT16:
				result = "sint16";
				break;
			case CIMDataType.SINT32_ARRAY:
			case CIMDataType.SINT32:
				result = "sint32";
				break;
			case CIMDataType.SINT64_ARRAY:
			case CIMDataType.SINT64:
				result = "sint64";
				break;
			case CIMDataType.STRING_ARRAY:
			case CIMDataType.STRING:
				result = "string";
				break;
			case CIMDataType.BOOLEAN_ARRAY:
			case CIMDataType.BOOLEAN:
				result = "boolean";
				break;
			case CIMDataType.REAL32_ARRAY:
			case CIMDataType.REAL32:
				result = "real32";
				break;
			case CIMDataType.REAL64_ARRAY:
			case CIMDataType.REAL64:
				result = "real64";
				break;
			case CIMDataType.DATETIME_ARRAY:
			case CIMDataType.DATETIME:
				result = "datetime";
				break;
			case CIMDataType.CHAR16_ARRAY:
			case CIMDataType.CHAR16:
				result = "char16";
				break;
			case CIMDataType.NUMERIC:
				result = "numeric";
				break;
			case CIMDataType.NULL:
				result = "null";
				break;
			case CIMDataType.REFERENCE:
				result = getRefClassName() + " ref";
				break;
			case CIMDataType.REFERENCE_ARRAY:
				result = getRefClassName() + "[" + ((size > -1) ? String.valueOf(size) : "")
						+ "] ref";
				break;
			default:
				result = EMPTY;
		}
		return result;
	}

	/**
	 * Returns the MOF representation of this object.
	 * 
	 * @return Teh MOF representation
	 */
	public String toMOF() {
		String result = getStringType();
		if (isArrayType() && type != REFERENCE_ARRAY) {
			if (size > -1) result += "[" + size + "]";
			else result += "[]";
		}
		return result;
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
		CIMDataType type = new CIMDataType("CIM_ComputerSystem", 10);
		System.out.println(type);
		type = new CIMDataType("CIM_ComputerSystem", SIZE_SINGLE);
		System.out.println(type);
		type = new CIMDataType("CIM_ComputerSystem", SIZE_UNLIMITED);
		System.out.println(type);
		type = new CIMDataType("CIM_ComputerSystem", -10);
		System.out.println(type);
	}
}
