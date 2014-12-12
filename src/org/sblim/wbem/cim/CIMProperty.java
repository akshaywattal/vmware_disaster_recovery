/**
 * CIMProperty.java
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
 *   17953    2005-08-10  pineiro5     CIMClient does a case sensitive retrieve of property names
 * 1535756    2006-08-08  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.cim;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

public class CIMProperty extends CIMQualifiableElement implements Cloneable, Serializable {

	private static final long serialVersionUID = 418624401577884269L;

	private CIMValue iValue;

	private String iOriginClass = null;

	private String iOverridingProperty;

	private CIMDataType iType;

	private boolean iPropagated = false;

	/**
	 * Creates an CIMProperty object.
	 * 
	 */
	public CIMProperty() {
		this(EMPTY);
	}

	/**
	 * Creates a CIMProperty object with the specified name.
	 * 
	 * @param pName
	 *            The property's name
	 */
	public CIMProperty(String pName) {
		this(pName, null);
	}

	/**
	 * Creates a CIMProperty with the specified name and CIMValue.
	 * 
	 * @param pName
	 *            The property's name
	 * @param pValue
	 *            The property's value
	 */
	public CIMProperty(String pName, CIMValue pValue) {
		super(pName);
		iValue = pValue;
		if (pValue != null) {
			iType = pValue.getType();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return clone(true, true);
	}

	/**
	 * Creates a replica of this object. Only if the includeQualifier arguments
	 * is true, the qualifiers of the CIMProperty will be copied. Only if
	 * includeClassOrigin is true, the classorigin information will be passed to
	 * the new object.
	 * 
	 * @param pIncludeQualifier
	 *            if <code>true</code>, the qualifiers of the CIMProperty
	 *            will be copied
	 * @param pIncludeClassOrigin
	 *            if <code>true</code>, the classorigin information will be
	 *            copied
	 * @return The replica
	 */
	public Object clone(boolean pIncludeQualifier, boolean pIncludeClassOrigin) {
		CIMProperty that = new CIMProperty(); // TODO should the value be
		// cloned?
		that.iName = iName;
		if (iValue != null) that.iValue = (CIMValue) iValue.clone();
		if (pIncludeClassOrigin) that.iOriginClass = iOriginClass;
		that.iOverridingProperty = iOverridingProperty;
		that.iPropagated = iPropagated;

		if (iType != null) that.iType = (CIMDataType) iType.clone();

		if (pIncludeQualifier) {
			for (int i = 0; i < iQualifiers.size(); i++) {
				that.iQualifiers.add(((CIMQualifier) iQualifiers.elementAt(i)).clone());
			}
		}
		return that;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof CIMProperty)) return false;

		CIMProperty that = (CIMProperty) obj;
		if (!(iOriginClass == null ? that.iOriginClass == null : iOriginClass
				.equalsIgnoreCase(that.iOriginClass))) return false;

		if (!(iOverridingProperty == null ? that.iOverridingProperty == null
				: iOverridingProperty.equals(that.iOverridingProperty))) return false;

		if (iPropagated != that.iPropagated) return false;

		if (!iQualifiers.equals(that.iQualifiers)) return false;

		if (!(iType == null ? that.iType == null : iType.equals(that.iType))) return false;

		if (!(iValue == null ? that.iValue == null : iValue.equals(that.iValue))) return false;

		return iName.equalsIgnoreCase(that.iName);
	}

	/**
	 * Returns the origin class for this object. The origin class specifies the
	 * CIMClass that introduces this property.
	 * 
	 * @return The originating class
	 */
	public String getOriginClass() {
		return iOriginClass;
	}

	/**
	 * Returns the name of the property that this CIMProperty overrides. Returns
	 * null if this CIMProperty does not override any property.
	 * 
	 * @return The overridden property
	 */
	public String getOverridingProperty() {
		return iOverridingProperty;
	}

	/**
	 * Returns the size of this property.
	 * 
	 * @return The size
	 */
	public int getSize() {
		if (iValue != null) return iValue.getSize();
		if (iType != null) return iType.getSize();

		return CIMDataType.SIZE_SINGLE;
	}

	/**
	 * Returns the CIMValue associated with this CIMProperty.
	 * 
	 * @return The value
	 */
	public CIMValue getValue() {
		return iValue;
	}

	/**
	 * Returns the CIMDataType associated with this CIMProperty.
	 * 
	 * @return The type
	 */
	public CIMDataType getType() {
		return (iType != null) ? iType : (iValue != null ? iValue.getType() : null);
	}

	/**
	 * Determines if this CIMProperty contains the <i>Key</i> qualifier.
	 * 
	 * @return <code>true</code> if the property is a key property
	 */
	public boolean isKey() {
		Iterator iter = iQualifiers.iterator();
		while (iter.hasNext()) {
			CIMQualifier qualifier = (CIMQualifier) iter.next();

			if (qualifier.getName().equalsIgnoreCase("Key")
					&& (qualifier.getValue() == null || CIMValue.TRUE.equals(qualifier.getValue()))) return true;
		}
		return false;
	}

	/**
	 * Determines if this CIMProperty is propagated.
	 * 
	 * @return <code>true</code> if the property is propagated
	 */
	public boolean isPropagated() {
		return iPropagated;
	}

	/**
	 * Determines if this CIMProperty is a reference data type.
	 * 
	 * @return <code>true</code> if the property is a reference
	 */
	public boolean isReference() {
		if (iType != null) return iType.isReferenceType();
		else if (iValue != null && iValue.getType() != null) return iValue.getType()
				.isReferenceType();
		else return false; // should we throw an exception ?

	}

	/**
	 * Removes the qualifier with the specified name from this CIMProperty.
	 * 
	 * @param pName
	 *            The qualifier's name
	 * @return <code>true</code> if the qualifier was successfully removed,
	 *         otherwise returns <code>false</code>.
	 */
	public boolean removeQualifier(String pName) {
		if (pName == null) throw new IllegalArgumentException("null qualifier name exception");

		Iterator iter = iQualifiers.iterator();
		while (iter.hasNext()) {
			CIMQualifier qualifier = (CIMQualifier) iter.next();
			if (qualifier.getName().equalsIgnoreCase(pName)) {
				iter.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds/removes the key qualifier
	 * 
	 * @param pKey
	 *            if <code>true</code> the key qualifier is added, otherwise
	 *            it's removed
	 */
	public void setKey(boolean pKey) {
		if (pKey) addQualifier(new CIMQualifier("Key"));
		else removeQualifier("Key");
	}

	/**
	 * Specifies the overriding property for this CIMProperty.
	 * 
	 * @param pNewOverridingProperty
	 *            The overriding property
	 */
	public void setOverridingProperty(String pNewOverridingProperty) {
		iOverridingProperty = pNewOverridingProperty;
	}

	/**
	 * Specifies the origin class for this CIMProperty.
	 * 
	 * @param pOriginClass
	 *            The origin class
	 */
	public void setOriginClass(String pOriginClass) {
		iOriginClass = pOriginClass;
	}

	/**
	 * Sets the propagated value to the specified value.
	 * 
	 * @param pValue
	 *            The propageted value
	 */
	public void setPropagated(boolean pValue) {
		iPropagated = pValue;
	}

	/**
	 * Specifies the size of this property.
	 * 
	 * @deprecated the size of the property is bounded to the CIMDataType, which
	 *             is unmutable. Applications are encourage to specify the
	 *             CIMDataType with the appopiated size.
	 * @param pSize
	 *            The size
	 */
	public void setSize(int pSize) {
	// if (size < CIMDataType.SIZE_UNLIMITED) throw new
	// IllegalArgumentException("invalid size range: "+size);
	// throw new RuntimeException("Not implemented");

	}

	/**
	 * Sets the CIMDataType for this CIMProperty.
	 * 
	 * @param pType
	 *            The type
	 * @throws IllegalArgumentException
	 *             if the current property already has a CIMValue assigned to it
	 *             and the new CIMDataType does not matches the value's data
	 *             type.
	 */
	public void setType(CIMDataType pType) {
		if (iValue == null) iType = pType;
		else {
			if ((iValue.getType() != null) && iValue.getType().getType() != pType.getType()) throw new IllegalArgumentException(
					"Specified data type does not match data type of argument value");
			//	
		}
	}

	/**
	 * Updates the CIMQualifier value from the CIMProperty with the specified
	 * CIMQualifier.
	 * 
	 * @param pQualifier
	 *            The qualifier
	 * @return The new qualifier
	 * @throws CIMException
	 *             if a CIMQualifier with the same name does not exists.
	 */
	public CIMQualifier setQualifier(CIMQualifier pQualifier) throws CIMException {
		if (pQualifier == null) throw new IllegalArgumentException("null qualifier argument");

		// TODO verify it's a valid qualifier, if it is a key, it must not be
		// array type

		Iterator iter = iQualifiers.iterator();
		while (iter.hasNext()) {
			CIMQualifier q = (CIMQualifier) iter.next();
			if (q.getName().equalsIgnoreCase(pQualifier.getName())) {
				q.setValue(pQualifier.getValue());
				return q;
			}
		}
		throw new CIMException(CIMException.CIM_ERR_NOT_FOUND);
	}

	/**
	 * Sets the value of this CIMProperty to the specified CIMProperty.
	 * 
	 * @param pValue
	 *            The value
	 * @throws IllegalArgumentException
	 *             if the specified CIMValues's type does not match current
	 *             CIMDataType
	 */
	public void setValue(CIMValue pValue) {

		if (iType != null && pValue != null && pValue.getType().getType() != iType.getType()) { throw new IllegalArgumentException(
				"value type does not match property type"); }
		iValue = pValue;

		if (iType == null && pValue != null) {
			iType = pValue.getType();
		}
	}

	/**
	 * Returns the MOF representation of this CIMObject.
	 * 
	 * @return The MOF representation
	 */
	public String toMOF() {
		StringBuffer buf = new StringBuffer();

		if (iName != null && iName.length() > 0)

		if (iQualifiers.size() > 0) {
			buf.append(vectorToMOFString(iQualifiers, false, 1));
			buf.append('\n');
		}
		buf.append('\t');

		if (iType != null) buf.append(iType.getStringType());
		else buf.append("null");
		buf.append(' ');

		buf.append(iName);
		if (iType != null && iType.isArrayType()) {
			if (iType.getSize() > -1 && iType.getType() != CIMDataType.REFERENCE_ARRAY) {
				buf.append("[");
				buf.append(iType.getSize());
				buf.append("]");
			} else buf.append("[]");
		}

		if (iValue != null) {
			buf.append(" = ");
			if (iValue == null) {
				buf.append("null");
			} else if (iValue.getType() != null && iValue.getType().isArrayType()) {
				buf.append('{');
				buf.append(vectorToMOFString((Vector) iValue.getValue(), false, 0, 0, true));
				buf.append('}');
			} else buf.append(iValue);
		}
		buf.append(";\n");
		return buf.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return toMOF();
	}
}
