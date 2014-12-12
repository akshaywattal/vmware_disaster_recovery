/**
 * CIMParameter.java
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

import java.io.Serializable;

public class CIMParameter extends CIMQualifiableElement implements Cloneable, Serializable {

	private static final long serialVersionUID = -4122732409934594730L;

	private CIMDataType iType;

	/**
	 * Creates an object of CIMParameter.
	 */
	public CIMParameter() {
		super();
	}

	/**
	 * Creates an object of CIMParameter with the specified name.
	 * 
	 * @param pName
	 *            The parameter's name
	 */
	public CIMParameter(String pName) {
		super(pName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		CIMParameter that = new CIMParameter(iName);
		that.iType = (CIMDataType) iType.clone();

		for (int i = 0; i < iQualifiers.size(); i++) {
			that.iQualifiers.add(((CIMQualifier) iQualifiers.elementAt(i)).clone());
		}
		return that;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return iName.hashCode() + ((iType != null) ? iType.hashCode() : 0);
	}

	/**
	 * Returns the size of this object. A value of CIMDataType.SIZE_SINGLE is
	 * returned for single elements, CIMDataType.SIZE_UNLIMITED is returned for
	 * variable length arrays, and zero or a positive number is returned for a
	 * fixed length array.
	 * 
	 * @return The size
	 */
	public int getSize() {
		if (iType != null) return iType.getSize();
		return CIMDataType.SIZE_SINGLE;
	}

	/**
	 * Returns the CIM data type of this object
	 * 
	 * @return The type
	 */
	public CIMDataType getType() {
		return iType;
	}

	/**
	 * Sets the size of this object
	 * 
	 * @deprecated Using this method can produce inconsitency and break
	 *             invariants
	 * @param size
	 *            The size
	 */
	public void setSize(int size) {}

	/**
	 * Specifies the CIMDataType for this object.
	 * 
	 * @param pType
	 *            The type
	 * @throws IllegalArgumentException
	 *             if the specified argument is null.
	 */
	public void setType(CIMDataType pType) {
		if (pType == null) throw new IllegalArgumentException("null data type argument");
		iType = pType;
	}

	/**
	 * Returns the MOF representation of this object.
	 * 
	 * @return The MOF representation
	 */
	public String toMOF() {
		StringBuffer buff = new StringBuffer();
		if (iQualifiers.size() > 0) {
			buff.append(vectorToMOFString(iQualifiers, false, 1));
			buff.append("\n\t\t");
		}
		buff.append(iType);
		buff.append(" ");
		buff.append(iName);

		// if (type != null && type.isArrayType()) {
		// buff.append("[]");
		// }
		return buff.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return toMOF();
	}
}
