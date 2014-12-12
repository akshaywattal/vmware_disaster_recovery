/**
 * CIMClass.java
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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import org.sblim.wbem.util.Utils;

/**
 * Defines a Java object that represents a CIM Class. A CIM Class provides the
 * class definition for creating CIM Instances.
 */
public class CIMClass extends CIMObject implements Serializable, Cloneable {

	private static final long serialVersionUID = 117915892431935944L;

	private String iSuperclass = null;

	private Vector iAllMethods = new Vector(0);

	/**
	 * Constructs an object of a CIMClass.
	 * 
	 */
	public CIMClass() {
		super();
	}

	/**
	 * Construct an object of a CIMClass with the specified name.
	 * 
	 * @param pClassName
	 */
	public CIMClass(String pClassName) {
		super(pClassName);
		iObjectPath = new CIMObjectPath(pClassName);
	}

	/**
	 * Construct an object of a CIMClass pointing to the specified
	 * CIMObjectPath. Initialize internal object path with the information
	 * provided (namespace, hostname, and object name)
	 * 
	 * @param pObjectPath
	 * @throws IllegalArgumentException
	 *             if the object name of the objectpath or the object path are
	 *             null
	 */
	public CIMClass(CIMObjectPath pObjectPath) {
		if (pObjectPath == null) throw new IllegalArgumentException("null object path");

		this.iName = pObjectPath.getObjectName();
		if (this.iName == null) throw new IllegalArgumentException("null class name");

		this.iObjectPath = new CIMObjectPath(this.iName);
		this.iObjectPath.setHost(pObjectPath.getHost());
		this.iObjectPath.setNameSpace(pObjectPath.getNameSpace());
	}

	/**
	 * Add a CIMMethod to this class.
	 * 
	 * @param pMethod
	 *            defines a CIMMethod to be added to the class. If the method
	 *            already exists, nothing happend.
	 * @throws IllegalArgumentException
	 *             if the specified method is null.
	 */
	public void addMethod(CIMMethod pMethod) {
		if (pMethod == null) throw new IllegalArgumentException("null method argument");

		if (getAllMethods(pMethod) == null) Utils.addSorted(iAllMethods, pMethod);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		CIMClass that = new CIMClass(this.iName);

		that.iSuperclass = this.iSuperclass;

		Iterator iter = iQualifiers.iterator();
		while (iter.hasNext()) {
			that.iQualifiers.add(((CIMQualifier) iter.next()).clone());
		}

		iter = this.iAllProperties.iterator();
		while (iter.hasNext()) {
			that.addProperty((CIMProperty) ((CIMProperty) iter.next()).clone());
		}

		iter = this.iAllMethods.iterator();
		while (iter.hasNext()) {
			that.addMethod((CIMMethod) ((CIMMethod) iter.next()).clone());
		}

		return that;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return iAllProperties.hashCode() + iAllMethods.hashCode() + iQualifiers.hashCode()
				+ ((iSuperclass != null) ? iSuperclass.hashCode() : 0) + iName.hashCode();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof CIMClass)) return false;
		CIMClass that = (CIMClass) o;

		if (!this.iAllProperties.equals(that.iAllProperties)
				|| !this.iAllMethods.equals(that.iAllMethods)
				|| !this.iQualifiers.equals(that.iQualifiers)
				|| !iObjectPath.equals(that.iObjectPath)) return false;

		if (!(this.iSuperclass == null ? that.iSuperclass == null : this.iSuperclass
				.equalsIgnoreCase(that.iSuperclass))) return false;

		return (this.iName == null ? that.iName == null : this.iName.equalsIgnoreCase(that.iName));
	}

	/**
	 * Gets a method corresponding to the specified name
	 * 
	 * @param pMethod
	 *            A CIMMethod with the name to look for
	 * @return The method
	 */
	protected CIMMethod getMethod(CIMMethod pMethod) {
		Iterator iter = iAllMethods.iterator();
		while (iter.hasNext()) {
			CIMMethod method = (CIMMethod) iter.next();
			if (method.getName().equalsIgnoreCase(pMethod.getName())) { return method; }
		}
		return null;
	}

	protected CIMMethod getAllMethods(CIMMethod pMethod) {
		Iterator iter = iAllMethods.iterator();
		while (iter.hasNext()) {
			CIMMethod method = (CIMMethod) iter.next();
			if (method.getName().equalsIgnoreCase(pMethod.getName())) { return method; }
		}
		return null;
	}

	/**
	 * Creates a replica of this CIMClass only with the properties specified in
	 * the propertyList argument.
	 * 
	 * @param pPropertyList
	 *            An array with string of property names. (case sencitive names)
	 * @return The replica
	 */
	public CIMClass filterProperties(String[] pPropertyList) {
		CIMClass that = new CIMClass(iName);

		that.iSuperclass = this.iSuperclass;

		Iterator iter = iQualifiers.iterator();
		while (iter.hasNext()) {
			that.iQualifiers.add(((CIMQualifier) iter.next()).clone());
		}

		iter = this.iAllProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty property = (CIMProperty) ((CIMProperty) iter.next()).clone();
			String propName = property.getName();
			for (int i = 0; i < pPropertyList.length; i++) {
				if (propName.equalsIgnoreCase(pPropertyList[i])) {
					that.addProperty(property);
					break;
				}
			}
		}

		iter = this.iAllMethods.iterator();
		while (iter.hasNext()) {
			that.addMethod((CIMMethod) ((CIMMethod) iter.next()).clone());
		}

		return that;
	}

	/**
	 * Return all the methods for this class, including all the methods
	 * inherited from superclasses.
	 * 
	 * @return A Vector containin all CIMMethods from this class. For
	 *         performance reasons, the returned vector points to the internal
	 *         data structure that maintains the methods. Modifications on this
	 *         object may result in an inconsistent state of the CIMClass.
	 *         Applications MUST deside when this obect has to be cloned.
	 */
	public Vector getAllMethods() {
		return iAllMethods;
	}

	/**
	 * Returns a CIMMethod with the specified name. If a method with the
	 * specified name (case sensitive) does not exists, then returns null. For
	 * performance reasons, this method returns the internal data structure that
	 * is used to by the CIMClass. Modifications on this object may result in
	 * state of the CIMClass or CIMMethod. Applications MUST deside when this
	 * object has to be cloned.
	 * 
	 * @param pName
	 *            The method's name
	 * 
	 * @return null if the speficied method is not found or does not exists.
	 */
	public CIMMethod getMethod(String pName) {
		int delimiter;
		if (pName == null) return null;

		if ((delimiter = pName.indexOf('.')) > -1) { return getMethod(pName
				.substring(delimiter + 1), pName.substring(0, delimiter)); }

		Iterator iter = iAllMethods.iterator();
		while (iter.hasNext()) {
			CIMMethod method = (CIMMethod) iter.next();
			if (method.getName().equalsIgnoreCase(pName)) {
				String overridingMethod = method.getOverridingMethod();
				if (overridingMethod != null) {
					if ((delimiter = overridingMethod.indexOf('.')) > -1) { return getMethod(
							overridingMethod.substring(delimiter + 1), overridingMethod.substring(
									0, delimiter)); }
					return getMethod(overridingMethod);
				}
				return method;
			}
		}
		return null;
	}

	/**
	 * Returns a CIMMethod with the specified name for the given class origin.
	 * If a method with the specified name (case sensitive) does not exists,
	 * then returns null. The class origin may be "<i>superclass.overridingmethod</i>"
	 * or "</i>overridingmethod</i>", or null. For performance reasons, this
	 * method returns the internal data structure that is used to by the
	 * CIMClass. Modifications on this object may result in state of the
	 * CIMClass or CIMMethod. Applications MUST deside when this object has to
	 * be cloned.
	 * 
	 * @param pName
	 *            The method's name
	 * @param pOriginClass
	 *            The class origin
	 * 
	 * @return null if the speficied method is not found or does not exists.
	 */

	public CIMMethod getMethod(String pName, String pOriginClass) {
		if (pName == null) return null;

		if (pOriginClass == null || pOriginClass.length() == 0) return getMethod(pName);

		Iterator iter = iAllMethods.iterator();
		while (iter.hasNext()) {
			CIMMethod method = (CIMMethod) iter.next();
			if (method.getName().equalsIgnoreCase(pName)
					&& method.getOriginClass().equalsIgnoreCase(pOriginClass)) {

				String overridingMethod = method.getOverridingMethod();
				if (overridingMethod != null) {
					int delimiter;
					if ((delimiter = overridingMethod.indexOf('.')) > -1) { return getMethod(
							overridingMethod.substring(delimiter + 1), overridingMethod.substring(
									0, delimiter)); }
					return getMethod(overridingMethod);
				}
				return method;
			}
		}
		return null;
	}

	/**
	 * Returns a vector containing all the CIMMethods defined by this CIMClass,
	 * without including the methods inherited by any super classes. For
	 * performance reasons, this method returns a vector that contains
	 * CIMMethods which are internally used by the CIMClass. Modifications on
	 * the CIMMethods objects may result in state of the CIMClass or CIMMethod.
	 * Applications MUST deside when this object has to be cloned.
	 * 
	 * @return a vector of CIMMethods.
	 */
	public Vector getMethods() {
		Vector methods = new Vector();
		Iterator iter = iAllMethods.iterator();
		while (iter.hasNext()) {
			CIMMethod method = (CIMMethod) iter.next();
			if (method.getOriginClass() == null) methods.add(method);
		}
		return methods;
	}

	/**
	 * Returns the name of the super class. Return null if this class does not
	 * has a super class.
	 * 
	 * @return a string which defines the super class of this class.
	 */
	public String getSuperClass() {
		return this.iSuperclass;
	}

	/**
	 * Determines if this CIMClass contains the <i>Association</i> qualifier.
	 * 
	 * @return true if this class contains the <i>association</i> qualifier,
	 *         otherwise false.
	 */
	public boolean isAssociation() {
		Iterator iter = iQualifiers.iterator();
		while (iter.hasNext()) {
			CIMQualifier qualifier = (CIMQualifier) iter.next();
			if (qualifier.getName().equalsIgnoreCase("Association")
					&& (qualifier.getValue() == null || CIMValue.TRUE.equals(qualifier.getValue()))) return true;
		}
		return false;
	}

	/**
	 * Determines if this CIMClass contains any keyed CIMProperty.
	 * 
	 * @return true if this CIMClass contains a keyed CIMProperty, otherwise
	 *         returns false.
	 */
	public boolean isKeyed() {
		Iterator iter = iAllProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty property = (CIMProperty) iter.next();
			if (property.isKey()) return true;
		}
		return false;
	}

	/**
	 * Creates a replica of this CIMClass which only contains the objects local
	 * CIMProperties and CIMMethods. A local CIMProperty or CIMMethod are those
	 * that the original CIMClass is this object.
	 * 
	 * @return The replica
	 */
	public CIMClass localElements() {
		CIMClass that = new CIMClass(iName);
		that.setObjectPath((CIMObjectPath) this.getObjectPath().clone());
		that.iSuperclass = this.iSuperclass;

		Iterator iter = iAllMethods.iterator();
		while (iter.hasNext()) {
			CIMMethod method = (CIMMethod) iter.next();

			if (method.getOriginClass().equalsIgnoreCase(this.iName)) that
					.addMethod((CIMMethod) method.clone()); // needs to be
			// cloned?
		}

		iter = iAllProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty property = (CIMProperty) iter.next();

			if (property.getOriginClass().equalsIgnoreCase(this.iName)) that
					.addProperty((CIMProperty) property.clone()); // needs to
			// be
			// cloned?
		}

		iter = iQualifiers.iterator();
		while (iter.hasNext()) {
			CIMQualifier qualifiers = (CIMQualifier) iter.next();

			that.iQualifiers.add(qualifiers.clone()); // needs to
			// be
			// cloned?
		}
		return that;
	}

	/**
	 * Creates an CIMInstance object based on this CIMClass. This CIMInstance
	 * may be used later to create a CIMInstance remotely at the CIMOM with the
	 * CIMClient.createInstance() method.
	 * 
	 * @return The new instance
	 */
	public CIMInstance newInstance() {
		CIMInstance inst = new CIMInstance();
		inst.setName(getName());

		// properties
		Vector newProperties = new Vector();
		for (int i = 0; i < iAllProperties.size(); i++) {
			CIMProperty property = (CIMProperty) iAllProperties.elementAt(i);
			newProperties.add(property.clone());
		}
		inst.setProperties(newProperties);

		Vector newQualifiers = new Vector();
		for (int i = 0; i < iQualifiers.size(); i++) {
			CIMQualifier qualifier = (CIMQualifier) iQualifiers.elementAt(i);
			newQualifiers.add(qualifier.clone());
		}
		inst.setQualifiers(newQualifiers);
		return inst;
	}

	/**
	 * Returns the number of properties in this class.
	 * 
	 * @return The property count
	 */
	public int numberOfProperties() {
		return getProperties().size();
	}

	/**
	 * Returns the number of qualifiers in this class.
	 * 
	 * @return The qualifier count
	 */
	public int numberOfQualifiers() {
		return iQualifiers.size();
	}

	/**
	 * Sets the super class for this class. If null is passed means that the
	 * class does not have any super class.
	 * 
	 * @param pClass
	 */
	public void setSuperClass(String pClass) {
		this.iSuperclass = pClass;
	}

	/**
	 * Adds or removes the Association qualifier from this CIMClass. Depending
	 * on the case, this method will add if the specified parameter is true,
	 * otherwise remove the association qualifier from this CIMClass.
	 * 
	 * @param pValue
	 */
	public void setIsAssociation(boolean pValue) {
		CIMQualifier associationQualifier = getQualifier("Association");
		if (associationQualifier == null && pValue) {
			Utils.addSorted(this.iQualifiers, new CIMQualifier("Association"));
		} else if (associationQualifier != null && !pValue) {
			removeQualifier(associationQualifier.getName());
		}
	}

	/**
	 * Specified when this CIM Class has key properties or not.
	 * 
	 * @deprecated this method may lead to confusion. Instead of this method
	 *             application are encourage to remove/add the Key qualifier
	 *             manually.
	 * @param pValue
	 */
	public void setIsKeyed(boolean pValue) {
	// this.keyed = keyed;
	}

	/**
	 * Assigns the specified vector with CIMMethod to this CIMClass. If the
	 * method argument is null, all the CIMMethods from this CIMClass will be
	 * removed.
	 * 
	 * @param pMethods
	 */
	public void setMethods(Vector pMethods) {
		if (pMethods == null) {
			this.iAllMethods.setSize(0);
		} else {
			this.iAllMethods.setSize(0);

			Utils.addSorted(this.iAllMethods, pMethods);
		}
	}

	/**
	 * Sets the name for this CIMClass.
	 * 
	 * @param pName
	 *            The name
	 */
	public void setName(String pName) {
		if (pName == null) throw new IllegalArgumentException("null name argument");

		super.setName(pName);
		if (this.iObjectPath == null) {
			this.iObjectPath = new CIMObjectPath(pName);
		} else this.iObjectPath.setObjectName(pName);
	}

	/**
	 * Returns the CIMObjectPath from this object. For performance reasons, this
	 * method returns the internal structure used by the CIMClass. The
	 * application is responsable to deside when it must may a copy of the
	 * returned object to prevent inconsiste state.
	 * 
	 * @return A CIMObjectPath pointing to the class.
	 */
	public CIMObjectPath getObjectPath() {
		return iObjectPath; // cloned?
	}

	/**
	 * Sets the CIMObjectPath for the current CIMClass.
	 * 
	 * @param pObjectPath
	 */
	public void setObjectPath(CIMObjectPath pObjectPath) {
		if (pObjectPath == null) throw new IllegalArgumentException("null objectpath argument");

		if (this.iObjectPath == null) {
			this.iObjectPath = new CIMObjectPath();
		}

		this.iObjectPath.setHost(pObjectPath.getHost());
		this.iObjectPath.setNameSpace(pObjectPath.getNameSpace());
		this.setName(pObjectPath.getObjectName());
	}

	/**
	 * Returns the MOF representation of this object.
	 * 
	 * @return A string containig the MOF representation
	 */
	public String toMOF() {
		StringBuffer buf = new StringBuffer();
		buf.append(vectorToMOFString(iQualifiers, false, 1));
		buf.append("\nclass ");
		buf.append(getName());
		if (iSuperclass != null && iSuperclass.length() > 0) {
			buf.append(" : ");
			buf.append(iSuperclass);
		}
		buf.append(" {\n");
		if (iAllProperties.size() > 0) {
			buf.append(vectorToMOFString(iAllProperties, true, 0, 0, false));
			buf.append("\n");
		}
		if (iAllMethods.size() > 0) {
			buf.append(vectorToMOFString(iAllMethods, true, 1, 0, false));
		}
		buf.append("};");

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

	public static void main(String args[]) {
		CIMClass cl1 = new CIMClass("CIM_ComputerSystem");
		CIMMethod m1 = new CIMMethod("AddProperty");
		m1.addQualifier(new CIMQualifier("Overridable"));
		m1.setType(new CIMDataType(CIMDataType.SINT32, 32));
		CIMParameter p1 = new CIMParameter("Arg1");
		p1.addQualifier(new CIMQualifier("IN"));
		m1.addParameter(p1);

		System.out.println(cl1);
	}
}
