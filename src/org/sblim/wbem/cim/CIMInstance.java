/**
 * CIMInstance.java
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

public class CIMInstance extends CIMObject implements Serializable {

	private static final long serialVersionUID = -1992307623410992121L;

	protected String iAlias;

	/**
	 * Constructs an object of a CIMInstance.
	 */
	public CIMInstance() {
		super();

		iObjectPath = new CIMObjectPath();
	}

	/**
	 * Constructs an object of a CIMInstance
	 * 
	 * @param pObjectPath
	 */
	public CIMInstance(CIMObjectPath pObjectPath) {
		super();
		if (pObjectPath == null) iObjectPath = new CIMObjectPath();
		else {
			iObjectPath = (CIMObjectPath) pObjectPath.clone();
			iName = iObjectPath.getObjectName();
			if (this.iName == null) throw new IllegalArgumentException("null class name");

			Vector keys = pObjectPath.getKeys();
			Iterator iter = keys.iterator();
			while (iter.hasNext()) {
				addProperty((CIMProperty) iter.next());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		CIMInstance that = new CIMInstance();
		that.iName = iName;
		that.iAlias = iAlias;
		that.iObjectPath = (CIMObjectPath) iObjectPath.clone();

		Iterator iter = iQualifiers.iterator();
		while (iter.hasNext()) {
			CIMQualifier qual = (CIMQualifier) iter.next();
			that.iQualifiers.add(qual.clone());
		}

		iter = this.iAllProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty prop = (CIMProperty) iter.next();
			that.addProperty((CIMProperty) prop.clone());
		}
		return that;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof CIMInstance)) return false;
		CIMInstance that = (CIMInstance) o;

		if (!iQualifiers.equals(that.iQualifiers) || !iAllProperties.equals(that.iAllProperties)) return false;

		if (!(this.iAlias == null ? that.iAlias == null : this.iAlias.equalsIgnoreCase(that.iAlias))) return false;

		return iName.equalsIgnoreCase(that.iName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return iName.hashCode() + iQualifiers.hashCode() + iAllProperties.hashCode()
				+ ((iAlias != null) ? iAlias.hashCode() : 0);
	}

	/**
	 * Returns the alias name for the instance
	 * 
	 * @return string which represents the alias for the instance, or null if it
	 *         hasn't beed specified
	 */
	public String getAlias() {
		return iAlias;
	}

	/**
	 * Returns the class name of the instance.
	 * 
	 * @return string which defines the class name for the instance.
	 */
	public String getClassName() {
		return iName;
	}

	/**
	 * Returns a list of key properties in this CIMInstance.
	 * 
	 * @return a vector which provides direct access to the key properties of
	 *         this instance.
	 */
	public Vector getKeyValuePairs() {
		return getKeys();
	}

	/**
	 * Returns the class name of this instance.
	 * 
	 * @deprecated instances don't have names. However current implementation
	 *             provides access to the class name of the instance.
	 * @return The name
	 */
	public String getName() {
		return iName;
	}

	/**
	 * Gets the CIMObjectsPath for this CIMInstance. Note that this method
	 * returns the internal representation of the objectpath. It is upto the
	 * application to deside when the objectpath must be cloned to maintain
	 * consistency.
	 * 
	 * @return The CIM object path
	 */
	public CIMObjectPath getObjectPath() {
		iObjectPath.setKeys(getKeyValuePairs());

		return iObjectPath;
	}

	/**
	 * Specifies the CIMObjectPath for this CIMInstance.
	 * 
	 * @param pObjectPath
	 *            The object path
	 * @throws IllegalArgumentException
	 *             if a null object path is specified
	 */
	public void setObjectPath(CIMObjectPath pObjectPath) {
		if (pObjectPath == null) throw new IllegalArgumentException("null objectpath argument");

		this.iName = pObjectPath.getObjectName();
		this.iObjectPath.setObjectName(this.iName);
		this.iObjectPath.setHost(pObjectPath.getHost());
		this.iObjectPath.setNameSpace(pObjectPath.getNameSpace());

		Vector keys = pObjectPath.getKeys();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			CIMProperty key = (CIMProperty) iter.next();
			CIMProperty localProp;
			if ((localProp = getProperty(key)) == null) {
				addProperty(key);
			} else {
				localProp.addQualifier(new CIMQualifier("Key"));
				if (localProp.getType().getType() == CIMDataType.REFERENCE) {
					setProperty(key.getName(), key.getValue());
				}
			}
		}
	}

	/**
	 * Specifies the alias for this instance.
	 * 
	 * @param pAliasName
	 *            The alias
	 */
	public void setAlias(String pAliasName) {
		this.iAlias = pAliasName;
	}

	/**
	 * Specifies the classname for this CIMInstance.
	 * 
	 * @param pClassname
	 *            The class name
	 * @throws IllegalArgumentException
	 *             for
	 */
	public void setClassName(String pClassname) {
		if (pClassname == null) throw new IllegalArgumentException("null classname argument");

		this.iObjectPath.setObjectName(pClassname);
		this.iName = pClassname;
	}

	/**
	 * Specifies the name for this CIMInstance.
	 * 
	 * @param pName
	 *            The name
	 */
	public void setName(String pName) {
		if (pName == null) throw new IllegalArgumentException("null name argument");

		this.iName = pName;
		this.iObjectPath.setObjectName(pName);
	}

	/**
	 * Returns the MOF representation of this CIMInstance.
	 * 
	 * @return The MOF representation
	 */
	public String toMOF() {
		StringBuffer buffer = new StringBuffer();

		if (iQualifiers.size() > 0) {
			buffer.append("\n\t");
			buffer.append(vectorToMOFString(iQualifiers, false, 1));
			buffer.append("\n");
		}
		buffer.append("instance of ");
		buffer.append(getClassName());
		buffer.append(" ");
		if (iAlias != null && iAlias.length() > 0) {
			buffer.append("as space");
			buffer.append(iAlias);
			buffer.append(" ");
		}
		buffer.append("{\n");

		if (iAllProperties.size() > 0) {
			buffer.append(vectorToMOFString(iAllProperties, true, 0, 0, false));
			buffer.append(" ");
		}

		buffer.append("};");

		return buffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return toMOF();
	}

	/**
	 * Updates the specified CIMProperty. If the property does not exists, add
	 * the new property to this CIMInstance, otherwise update the value,
	 * qualifiers, overridingProperty, and OriginClass attributes.
	 * 
	 * @param pProperty
	 *            The property
	 */
	public void updateProperty(CIMProperty pProperty) {
		if (pProperty == null) throw new IllegalArgumentException("null property argument");

		Iterator iter = iAllProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty prop = (CIMProperty) iter.next();

			if (prop.getName().equals(pProperty.getName())) {
				prop.setValue(pProperty.getValue());

				Iterator iter2 = pProperty.getQualifiers().iterator();
				while (iter2.hasNext()) {
					CIMQualifier qual = (CIMQualifier) iter2.next();
					prop.addQualifier(qual);
				}
				prop.setOverridingProperty(pProperty.getOverridingProperty());
				prop.setOriginClass(pProperty.getOriginClass());
				return;
			}
		}
		addProperty(pProperty);
	}

	/**
	 * Updates the CIMProperty value. Updates the CIMProperty's values which
	 * matches the specified CIMProperty name. If the CIMProperty does not
	 * exists, a new property will be added.
	 * 
	 * @param pProperty
	 *            The property
	 * @return true if the CIMProperty was succesfully updated, or false if a
	 *         new property was added.
	 */
	public boolean updatePropertyValue(CIMProperty pProperty) {
		if (pProperty == null) throw new IllegalArgumentException("null property argument");

		Iterator iter = iAllProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty prop = (CIMProperty) iter.next();

			if (prop.getName().equals(pProperty.getName())) {
				prop.setValue(pProperty.getValue());
				return true;
			}
		}
		Utils.addSorted(iAllProperties, pProperty);
		return false;
	}

	/**
	 * Updates the properties of the current CIMInstance with the vector of
	 * CIMProperty objects.
	 * 
	 * @param pProperties
	 *            A property vector
	 */
	public void updatePropertyValue(Vector pProperties) {
		if (pProperties == null) throw new IllegalArgumentException("null values argument");
		Iterator iter = pProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty property = (CIMProperty) iter.next();
			updatePropertyValue(property);
			return;
		}
	}

	public static void main(String[] args) {
		CIMObjectPath op = new CIMObjectPath("MyTestClass");
		CIMProperty p1 = new CIMProperty("KeyedProperty");
		p1.addQualifier(new CIMQualifier("Key"));
		op.addKey(p1);

		CIMProperty p2 = new CIMProperty("KeyedProperty");
		p2.setValue(new CIMValue("Value1", CIMDataType.getPredefinedType(CIMDataType.STRING)));

		p2.addQualifier(new CIMQualifier("Key"));
		p2.addQualifier(new CIMQualifier("Overridable"));

		System.out.println("CIMInstance(ObjectPath)");
		CIMInstance instance = new CIMInstance(op);
		System.out.println(instance);
		System.out.println("updateProperty()");
		instance.updateProperty(p2);
		System.out.println(instance);

		System.out.println("setName()");
		instance.setName("NewClassName");
		System.out.println(instance);

		System.out.println("getObjectPath()");
		System.out.println(instance.getObjectPath());
		System.out.println("hashCode()");
		System.out.println(instance.hashCode());
		System.out.println("clone()");
		CIMInstance instance2 = (CIMInstance) instance.clone();
		System.out.println(instance2);
		System.out.println("instance.equals(instance2)");
		System.out.println(instance2.equals(instance));
		System.out.println("instance.addQualifier(new CIMQualifier(\"Association\"))");
		instance2.addQualifier(new CIMQualifier("Association"));
		System.out.println("instance.equals(instance2)");
		System.out.println(instance2.equals(instance));
	}
}
