/**
 * CIMArgument.java
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
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.cim;

/**
 * A class used to denote the arguments passed to extrinsic method invocations.
 * 
 */
public class CIMArgument extends CIMQualifiableElement implements Cloneable {

	private static final long serialVersionUID = 828496901160110115L;

	private CIMValue iValue;

	private CIMDataType iType;

	/**
	 * Constructs an object representing a CIMArgument.
	 * 
	 */
	public CIMArgument() {
		super();
	}

	/**
	 * Construcs an object representing a CIMArgument with the specified name.
	 * 
	 * @param pName
	 *            the name for this CIMArgument
	 */
	public CIMArgument(String pName) {
		super(pName);
	}

	/**
	 * Constructs an object representing a CIMArgument with the specified name
	 * and the specified CIMValue.
	 * 
	 * @param pName
	 *            the name for this CIMArgument
	 * @param pValue
	 *            the value associated to this CIMArgument.
	 */
	public CIMArgument(String pName, CIMValue pValue) {
		super(pName);
		this.iValue = pValue;
		this.iType = (pValue != null) ? pValue.getType() : null;
	}

	public CIMArgument(String pName, CIMDataType pDataType) {
		super(pName);
		this.iValue = null;
		if (pDataType == null) throw new IllegalArgumentException(
				"Illegal argument - data type must not be null");
		this.iType = pDataType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return clone(true);
	}

	/**
	 * Creates a replica of this object. Simmiliar to clone() , but allows to
	 * create a replica without qualifiers.
	 * 
	 * @param pIncludeQualifier
	 *            determines of the resulting object must include qualifiers.
	 * @return a replica of this object.
	 * @see #clone()
	 */
	public Object clone(boolean pIncludeQualifier) {
		CIMArgument clone = new CIMArgument(iName);
		clone.iType = (CIMDataType) this.iType.clone();
		clone.iValue = (CIMValue) this.iValue.clone();

		if (pIncludeQualifier) {
			for (int i = 0; i < iQualifiers.size(); i++) {
				clone.iQualifiers.add(((CIMQualifier) iQualifiers.elementAt(i)).clone());
			}
		}
		return clone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof CIMArgument)) return false;
		CIMArgument that = (CIMArgument) o;

		if (!this.iQualifiers.equals(that.iQualifiers)) return false;

		if (!((this.iType == null && that.iType == null) || (this.iType != null && this.iType
				.equals(that.iType)))) return false;

		if (!((this.iValue == null && that.iValue == null) || (this.iValue != null && this.iValue
				.equals(that.iValue)))) return false;

		return this.iName.equalsIgnoreCase(that.iName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return iQualifiers.hashCode() + ((iType != null) ? iType.hashCode() : 0)
				+ ((iValue != null) ? iValue.hashCode() : 0) + iName.hashCode();
	}

	/**
	 * Gets the CIMValue associated with this CIMArgument.
	 * 
	 * @return the CIMValue for this argument.
	 */
	public CIMValue getValue() {
		return iValue;
	}

	/**
	 * Gets the CIMDataType associated with this argument.
	 * 
	 * @return CIMDataType representing the type for this argument
	 */
	public CIMDataType getType() {
		return iType;
	}

	/**
	 * Specifies the CIMDataType for this CIMArgument.
	 * 
	 * @param pType
	 */
	public void setType(CIMDataType pType) {
		if (iValue == null) this.iType = pType;
		else {
			if ((iValue.getType() != null) && iValue.getType().getType() != pType.getType()) throw new IllegalArgumentException(
					"Specified data type does not match data type of argument value");
		}
	}

	/**
	 * Specifies the CIMValue for this CIMArgument.
	 * 
	 * @param pValue
	 */
	public void setValue(CIMValue pValue) {
		if (iType != null && pValue != null && pValue.getType().getType() != iType.getType()) { throw new IllegalArgumentException(
				"value type does not match property type"); }
		this.iValue = pValue;

		if (iType == null && pValue != null) iType = pValue.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "CIMArgument(name=" + iName + ", value=" + iValue + ")";
	}
}
