/**
 * CIMQualifierType.java
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

import org.sblim.wbem.util.Utils;

public class CIMQualifierType extends CIMElement implements Cloneable, Comparable, Serializable {

	private static final long serialVersionUID = -2092769638867980096L;

	private Vector iFlavors = new Vector(0);

	private Vector iScopes = new Vector(0);

	private CIMDataType iType = null;

	private CIMValue iDefaultValue;

	/**
	 * Default ctor.
	 */
	public CIMQualifierType() {
		super();
	}

	/**
	 * Constructs a names qualifier.
	 * 
	 * @param pName
	 *            The name
	 */
	public CIMQualifierType(String pName) {
		super(pName);
	}

	/**
	 * Adds the specified CIMFlavor to this CIMQualifierType object
	 * 
	 * @param pFlavor
	 *            The flavor
	 * @throws IllegalArgumentException
	 *             if a null flavor is specified.
	 */
	public void addFlavor(CIMFlavor pFlavor) {
		if (pFlavor == null) throw new IllegalArgumentException("null flavor argument");
		Utils.addSorted(iFlavors, pFlavor);
	}

	/**
	 * Adds the specified CIMScope to this CIMQualifierType object
	 * 
	 * @param pScope
	 *            The scope
	 * @throws IllegalArgumentException
	 *             if a null scope is specified.
	 */
	public void addScope(CIMScope pScope) {
		if (pScope == null) throw new IllegalArgumentException("null scope argument");
		Utils.addSorted(iScopes, pScope);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		CIMQualifierType that = new CIMQualifierType(iName);
		that.iFlavors = (Vector) iFlavors.clone();
		that.iScopes = (Vector) iScopes.clone();

		if (iType != null) that.iType = (CIMDataType) iType.clone();

		if (iDefaultValue != null) that.iDefaultValue = (CIMValue) iDefaultValue.clone();

		return that;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof CIMQualifierType)) return false;

		CIMQualifierType that = (CIMQualifierType) o;
		if (!iFlavors.equals(that.iFlavors) || !iScopes.equals(that.iScopes)) return false;

		if (!((iType == null && that.iType == null) || (iType != null && iType.equals(that.iType)))) return false;

		if (!((iName == null && that.iName == null) || (iName != null && iName
				.equalsIgnoreCase(that.iName)))) return false;

		if (!((iDefaultValue == null && that.iDefaultValue == null) || (iDefaultValue != null && iDefaultValue
				.equals(that.iDefaultValue)))) return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return iFlavors.hashCode() << 24 + iScopes.hashCode() << 16 + ((iDefaultValue != null) ? iDefaultValue
				.hashCode()
				: 0) << 8 + ((iType != null) ? iType.hashCode() : 0);
	}

	/**
	 * Gets the default for this CIMQualifierType.
	 * 
	 * @return The default value
	 */
	public CIMValue getDefaultValue() {
		return iDefaultValue;
	}

	/**
	 * Returns a Vector containing all the CIMFlavors assigned to this
	 * CIMQualifierType.
	 * 
	 * @return A vector of flavors
	 */
	public Vector getFlavor() {
		return iFlavors;
	}

	/**
	 * Returns a Vector containing all the CIMScope assigned to this
	 * CIMQualifierType.
	 * 
	 * @return A vector of scopes
	 */
	public Vector getScope() {
		return iScopes;
	}

	/**
	 * Returns a CIMDataType associated to this CIMQualifierType.
	 * 
	 * @return The type
	 */
	public CIMDataType getType() {
		return iType;
	}

	/**
	 * Determines if this CIMQualifierType has a default value associated with
	 * it.
	 * 
	 * @return getDefaultValue()!=null
	 */
	public boolean hasDefaultValue() {
		return (iDefaultValue != null);
	}

	/**
	 * Determines if this CIMQualifierType has a CIMFlavor value associated with
	 * it.
	 * 
	 * @param pFlavor
	 *            The flavor
	 * 
	 * @return <code>true</code> if the given flavor is part of this qualifier
	 *         type
	 * 
	 */
	public boolean hasFlavor(CIMFlavor pFlavor) {
		if (pFlavor == null) throw new IllegalArgumentException("null flavor argument");

		return Utils.hasValue(iFlavors, pFlavor);
	}

	/**
	 * Determines if this CIMQualifierType has a CIMScope value associated with
	 * it.
	 * 
	 * @param pScope
	 *            The scope
	 * 
	 * @return <code>true</code> if the given scope is part of this qualifier
	 *         type
	 */
	public boolean hasScope(CIMScope pScope) {
		if (pScope == null) throw new IllegalArgumentException("null scope argument");

		Iterator iter = iScopes.iterator();
		while (iter.hasNext()) {
			CIMScope scp = (CIMScope) iter.next();
			if (scp.equals(pScope) || scp.getScope() == CIMScope.ANY) return true;
		}
		return false;
	}

	/**
	 * Determines if this CIMQualifierType's type is an array.
	 * 
	 * @return <code>true</code> if this qualifier's value is an array
	 */
	public boolean isArrayValue() {
		if (iType != null) return iType.isArrayType();
		else if (iDefaultValue != null && iDefaultValue.getType() != null) return iDefaultValue
				.getType().isArrayType();
		return false;
	}

	/**
	 * Assigns the default value for this CIMDataType.
	 * 
	 * @param pValue
	 *            The default value
	 */
	public void setDefaultValue(CIMValue pValue) {
		iDefaultValue = pValue;
	}

	/**
	 * Set the value type of this qualifier
	 * 
	 * @param pType
	 *            The type
	 * @throws IllegalArgumentException
	 *             if current CIMDataType has an associated default value hows
	 *             data type does not match the new specified data type.
	 */
	public void setType(CIMDataType pType) {
		// TODO check if current value is the same type of the new type
		iType = pType;
	}

	/**
	 * Returns the MOF representation of this object.
	 * 
	 * @return The MOF representation
	 */
	public String toMOF() {
		StringBuffer buf = new StringBuffer();

		buf.append("Qualifier ");
		buf.append(getName());
		buf.append(" : ");
		CIMDataType type = getType();
		if (type != null) buf.append(type);
		if (type != null && !type.isArrayType()) {
			buf.append(" = ");
			buf.append(getDefaultValue());
		}

		buf.append(",\n");
		if (iScopes.size() > 0) {
			buf.append("\tScope(");
			buf.append(vectorToMOFString(iScopes));
			buf.append(")");
		}

		if (iFlavors.size() > 0) {
			buf.append(",\n\tFlavor(");
			buf.append(vectorToMOFString(iFlavors));
			buf.append(")");
		}
		buf.append(";");

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (!(o instanceof CIMQualifierType)) throw new IllegalArgumentException(
				"Invalid object to compare with");

		return iName.toUpperCase().compareTo(((CIMQualifierType) o).iName.toUpperCase());
	}
}
