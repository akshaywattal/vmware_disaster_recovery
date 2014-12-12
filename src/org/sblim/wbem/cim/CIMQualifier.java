/**
 * CIMQualifier.java
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
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.cim;

import java.util.Vector;

import org.sblim.wbem.util.Utils;

public class CIMQualifier extends CIMElement implements Cloneable {

	private static final long serialVersionUID = -8512859782330327384L;

	private CIMValue iValue = null;

	private boolean iPropagated = false;

	private Vector iFlavors = new Vector(0);

	private CIMQualifierType iType;

	/**
	 * Default Ctor.
	 */
	public CIMQualifier() {
		super();
	}

	/**
	 * Ctor. Constructs a named qualifier.
	 * 
	 * @param pName
	 *            The qualifier's name
	 */
	public CIMQualifier(String pName) {
		super(pName);
	}

	/**
	 * Ctor. Constructs a named and typed qualifier.
	 * 
	 * @param pName
	 *            The qualifier's name
	 * @param pType
	 *            The qualifier's type
	 */
	public CIMQualifier(String pName, CIMQualifierType pType) {
		super(pName);
		setDefaults(pType);
	}

	/**
	 * Adds a flavor to this qualifier
	 * 
	 * @param pFlavor
	 *            The flavor
	 */
	public void addFlavor(CIMFlavor pFlavor) {
		if (pFlavor == null) throw new IllegalArgumentException("null flavor argument");

		Utils.addSorted(iFlavors, pFlavor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		CIMQualifier that = new CIMQualifier(iName);
		that.iPropagated = iPropagated;

		if (iType != null) that.iType = (CIMQualifierType) iType.clone();

		that.iFlavors = (Vector) iFlavors.clone();

		if (iValue != null) // BB mod
		that.iValue = (CIMValue) iValue.clone();

		return that;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof CIMQualifier)) return false;
		CIMQualifier that = (CIMQualifier) o;

		if (!(iValue == null ? that.iValue == null : iValue.equals(that.iValue))) return false;

		if (!(iType == null ? that.iType == null : iType.equals(that.iType))) return false;

		if (iPropagated != that.iPropagated || !iFlavors.equals(that.iFlavors)) return false;

		return iName.equalsIgnoreCase(that.iName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return ((iValue != null) ? iValue.hashCode() : 0) << 16 + ((iType != null) ? iType
				.hashCode() : 0) << 8 + (iFlavors.hashCode() + (iPropagated ? 1 : 0))
				+ iName.toUpperCase().hashCode();
	}

	public Vector getFlavor() {
		return iFlavors;
	}

	public CIMValue getValue() {
		if (iValue == null && iType != null) return iType.getDefaultValue();
		return iValue;
	}

	/**
	 * Checks if this qualifier has a given flavor set
	 * 
	 * @param pFlavor
	 *            The flavor
	 * @return <code>true</code> if the flavor is set, <code>false</code>
	 *         otherwise
	 */
	public boolean hasFlavor(CIMFlavor pFlavor) {
		if (pFlavor == null) throw new IllegalArgumentException("null flavor argument");

		return Utils.hasValue(iFlavors, pFlavor);
	}

	/**
	 * Checks if this qualifier has a value
	 * 
	 * @return getValue()!=null
	 */
	public boolean hasValue() {
		return (iValue != null);
	}

	/**
	 * Sets the default value and the flavor from the given qualifier type
	 * 
	 * @param pQualifierType
	 *            The qualifier type
	 */
	public void setDefaults(CIMQualifierType pQualifierType) {
		if (pQualifierType == null) throw new IllegalArgumentException(
				"null qualifier type argument");

		if (iValue == null && pQualifierType.hasDefaultValue()) {
			iValue = pQualifierType.getDefaultValue();
		}
		iFlavors = pQualifierType.getFlavor();
		iType = pQualifierType;
	}

	/**
	 * Sets this qualifiers value
	 * 
	 * @param pValue
	 *            The value
	 */
	public void setValue(CIMValue pValue) {
		iValue = pValue;
	}

	/**
	 * Sets the propagated value
	 * 
	 * @param pValue
	 *            <code>true</code> if this qualifier is propagated,
	 *            <code>false</code> otherwise
	 */
	public void setPropagated(boolean pValue) {
		iPropagated = pValue;
	}

	/**
	 * Checks if this qualifier is propagated
	 * 
	 * @return <code>true</code> if this qualifier is propagated,
	 *         <code>false</code> otherwise
	 */
	public boolean isPropagated() {
		return iPropagated;
	}

	/**
	 * Gets the MOF representation of this qualifier
	 * 
	 * @return The MOF representation
	 */
	public String toMOF() {
		StringBuffer buf = new StringBuffer();
		// TODO show flavor!
		buf.append(iName);
		if (iValue != null && iValue.getType() != null && iValue.getType().isArrayType()) {
			buf.append(CIMElement
					.vectorToMOFString(((Vector) iValue.getValue()), false, 0, 3, true));
		} else if (iValue != null) {
			buf.append(" ( ");
			buf.append(iValue);
			buf.append(" )");
		}
		return buf.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return toMOF();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (!(o instanceof CIMQualifier)) throw new IllegalArgumentException(
				"Invalid object to compare with");
		return iName.toUpperCase().compareTo(((CIMQualifier) o).iName.toUpperCase());
	}
}
