/**
 * CIMMethod.java
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

import java.util.Iterator;
import java.util.Vector;

import org.sblim.wbem.util.Utils;

/**
 * This class is not thread safe.
 */
public class CIMMethod extends CIMQualifiableElement implements Cloneable {

	private static final long serialVersionUID = -5943564359433173083L;

	private String iOriginClass = null;

	private String iOverridingMethod = null;

	private Vector iParameters = new Vector(0);

	private CIMDataType iType;

	private boolean iPropagated = false;

	/**
	 * Creates and instantiates a CIM method.
	 */
	public CIMMethod() {
		super();
	}

	/**
	 * Creates and instantiates a CIM Method with the specified name.
	 * 
	 * @param name
	 *            the name for the method.
	 */
	public CIMMethod(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 * 
	 * Returns an object which is a copy of the current CIM Method. This methods
	 * invokes the clone(boolean includeQualifiers, boolean includeClassOrigin)
	 * with both arguments as true, including indicates that the new CIM Method
	 * must include any qualifier information and classorigin from this
	 * CIMMethod.
	 */
	public Object clone() {
		return clone(true, true);
	}

	/**
	 * Returns an object which is a copy of this CIM Method.
	 * 
	 * @param pIncludeQualifier
	 *            a boolean that is true if the new object must contain
	 *            qualifier information from the current method, otherwise
	 *            false.
	 * @param pIncludeClassOrigin
	 *            a boolean that is true if the new object must contain
	 *            classorigin information from the current method, otherwise
	 *            false.
	 * @return returns an object which is a copy of this CIMMethod
	 */
	public Object clone(boolean pIncludeQualifier, boolean pIncludeClassOrigin) {
		CIMMethod that = new CIMMethod(iName);

		if (pIncludeQualifier) {
			Iterator iter = iQualifiers.iterator();
			while (iter.hasNext()) {
				CIMQualifier qual = (CIMQualifier) iter.next();
				that.iQualifiers.add(qual.clone());
			}
		}

		if (pIncludeClassOrigin) {
			that.iOriginClass = this.iOriginClass;
		}
		that.iOverridingMethod = this.iOverridingMethod;
		that.iType = (CIMDataType) this.iType.clone();
		that.iPropagated = this.iPropagated;

		Iterator iter = iParameters.iterator();
		while (iter.hasNext()) {
			CIMParameter param = (CIMParameter) iter.next();
			that.iParameters.add(param.clone());
		}
		return that;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return ((iOriginClass != null) ? iOriginClass.hashCode() : 0) * 31
				+ ((iOverridingMethod != null) ? iOverridingMethod.hashCode() : 0) * 37
				+ ((iPropagated ? 1 : 0)) * 23 + ((iQualifiers.hashCode()))
				+ (iParameters.hashCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 
	 * Takes a CIMMethod and returns true if it is equal to this CIMMethod.
	 * Otherwise, it returns false. Two methods are defined to be equal if the
	 * number of parameters is the same and all the parameter are equal, the
	 * number of qualifiers are the same and all the qualifiers are equal,
	 * (according to the equal comparison of the CIMQualifier which is case
	 * insensitive for the names), originClass, name , and isPropagated property
	 * of both methods are the same.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof CIMMethod)) return false;
		CIMMethod that = (CIMMethod) o;

		if (!this.iParameters.equals(that.iParameters)
				|| !this.iQualifiers.equals(that.iQualifiers)
				|| this.iPropagated != that.iPropagated) return false;

		if (!(this.iOverridingMethod == null ? that.iOverridingMethod == null
				: this.iOverridingMethod.equalsIgnoreCase(that.iOverridingMethod))) return false;

		if (!(this.iOriginClass == null ? that.iOriginClass == null : this.iOriginClass
				.equalsIgnoreCase(that.iOriginClass))) return false;

		return this.iName.equalsIgnoreCase(that.iName);
	}

	/**
	 * Get the name of the origin class for this method.
	 * 
	 * @return the name of the origin class for this method.
	 */
	public String getOriginClass() {
		return iOriginClass;
	}

	/**
	 * Gets the name of the overriding method for this method.
	 * 
	 * @return the name of the overriding method.
	 */

	public String getOverridingMethod() {
		return iOverridingMethod;
	}

	/**
	 * Returns a list of CIMParameters for this method. For performance reasons
	 * the method returns a vector which provides direct access to the
	 * parameters of the method. It is important to note that any modification
	 * to this vector may affect the consistency of the CIMObject therefore,
	 * should be avoided. The application should deside when the vector needs to
	 * be cloned or not to preserve a consistent internal state.
	 * 
	 * @return a vector of CIMParameter for this method.
	 */
	public Vector getParameters() {
		return iParameters;
	}

	/**
	 * Identify if the current method was propagated without modifications from
	 * the underlying subclass (respectively, Class).
	 * 
	 * @return true if the method was propagated, false if it was not.
	 */
	public boolean isPropagated() {
		return this.iPropagated;
	}

	/**
	 * Returns the size of the value returned by the method described by the
	 * CIMDataType. For single data types elements, a SINGLE_SIZE is returned.
	 * For arrays data type elements, the size of the array is returned or
	 * SIZE_UNLIMITED if the array does not specify the size If the data type is
	 * not present, a SINGLE_SIZE is returned.
	 * 
	 * @return the size of the method described by the CIMDataType
	 */
	public int getSize() {
		if (iType != null) return iType.getSize();
		return CIMDataType.SIZE_SINGLE;
	}

	/**
	 * Returns the data type of the method.
	 * 
	 * @return null if the method data type is <i>void</i>
	 */

	public CIMDataType getType() {
		return iType;
	}

	/**
	 * Specifies the name of the class where this method was defined.
	 * 
	 * @param pOriginClass
	 *            name of the class where this method was defined.
	 */
	public void setOriginClass(String pOriginClass) {
		this.iOriginClass = pOriginClass;
	}

	/**
	 * Set the name of the overriding method for this method.
	 * 
	 * @param pOverridingMethod
	 *            the name of the overriding method. A null parameter value is
	 *            accepted.
	 */
	public void setOverridingMethod(String pOverridingMethod) {
		this.iOverridingMethod = pOverridingMethod;
	}

	/**
	 * Adds a the specified parameter to the method.
	 * 
	 * @param pParameter
	 *            specifies the parameter to be added.
	 * @throws IllegalArgumentException
	 *             if the parameter is null
	 */
	public void addParameter(CIMParameter pParameter) {
		if (pParameter == null) throw new IllegalArgumentException("null parameter argument");
		Utils.addSorted(iParameters, pParameter);
	}

	/**
	 * Specify the new set of parameters for the method. If parameters is null,
	 * all the parameters are removed from the method.
	 * 
	 * @param pParameters
	 *            specifies a vector containing the new set of parameters
	 */
	public void setParameters(Vector pParameters) {
		if (pParameters == null) this.iParameters.setSize(0);
		else {
			Utils.addSorted(this.iParameters, pParameters);
		}
	}

	/**
	 * Specifies if the current method is propagated from a parent class.
	 * 
	 * @param pIsPropagated
	 */
	public void setPropagated(boolean pIsPropagated) {
		this.iPropagated = pIsPropagated;
	}

	/**
	 * Removes the specified property from the method. Returns normally if the
	 * methods does not exists.
	 * 
	 * @param pParameterName
	 * @throws IllegalArgumentException
	 *             if the parameterName is null.
	 */
	public void removeProperty(String pParameterName) {
		if (pParameterName == null) throw new IllegalArgumentException(
				"null parameterName argument");
		Iterator iter = iParameters.iterator();
		while (iter.hasNext()) {
			CIMParameter param = (CIMParameter) iter.next();
			if (param.getName().equals(pParameterName)) {
				iter.remove();
				return;
			}
		}
	}

	/**
	 * Set the size of the type returned by the method.
	 * 
	 * @deprecated the current method has been deprecated because the size of
	 *             the data type is unmutable. To change the size of the return
	 *             type, a new data type must be specified by invoking the
	 *             <b>setType</b> method
	 * @param pSize -
	 *            the size assigned to the method's return type
	 * @throws IllegalArgumentException
	 *             if the specified size is not a valid size.
	 */
	public void setSize(int pSize) {}

	/**
	 * Sets the data type of the returned value for the method. Setting the
	 * method type to <i>null</i> indicates that the method is void.
	 * 
	 * @param pType
	 *            specifie the return value data type
	 */
	public void setType(CIMDataType pType) {
		this.iType = pType;
	}

	/**
	 * Returns the MOF representation of the method.
	 * 
	 * @return a string representation in MOF
	 */
	public String toMOF() {
		StringBuffer buf = new StringBuffer();

		if (iName != null && iName.length() > 0) {
			buf.append(vectorToMOFString(iQualifiers, false, 1));
			buf.append("\n\t");
			if (iType != null) buf.append(iType);
			else buf.append("void");
			buf.append(" ");
			buf.append(iName);
			buf.append("(\n");
			buf.append(vectorToMOFString(iParameters, true, 1, 0, true));
			buf.append("\t);");
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

	}
}
