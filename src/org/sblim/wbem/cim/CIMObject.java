/**
 * CIMObject.java
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

public abstract class CIMObject extends CIMQualifiableElement {

	protected CIMObjectPath iObjectPath;

	protected Vector iAllProperties = new Vector(0);

	protected CIMObject() {
		super();
	}

	protected CIMObject(String pName) {
		super(pName);
	}

	/**
	 * Gets the specified CIMProperty, if the property exists.
	 * 
	 * @param pPropertyName
	 *            the name of the CIMProperty, with the following format
	 *            "propName" or "propName.orignClass".
	 * @return null if the property does not exists, otherwise returns the
	 *         CIMProperty.
	 */
	public CIMProperty getProperty(String pPropertyName) {
		int delimiter;
		if (pPropertyName == null) throw new IllegalArgumentException("null property name argument");

		if ((delimiter = pPropertyName.indexOf('.')) > -1) { return getProperty(pPropertyName
				.substring(delimiter + 1), pPropertyName.substring(0, delimiter)); }

		Iterator iter = iAllProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty property = (CIMProperty) iter.next();
			if (property.getName().equalsIgnoreCase(pPropertyName)) {
				String overridingProperty = property.getOverridingProperty();
				if (overridingProperty != null) {
					int delimeter;
					if ((delimeter = overridingProperty.indexOf('.')) > -1) return getProperty(
							overridingProperty.substring(delimeter + 1), overridingProperty
									.substring(0, delimeter));
					return getProperty(overridingProperty);
				}
				return property;
			}
		}
		return null;
	}

	/**
	 * Gets the CIMProperty from the current CIMElement, if the property exits.
	 * 
	 * @param pName
	 *            a string which specifies the property name.
	 * @param pOriginClass
	 *            a String which specifies the originClass.
	 * @return null if the property exits, otherwise returns the CIMProperty.
	 */
	public CIMProperty getProperty(String pName, String pOriginClass) {
		if (pName == null) return null;

		if (pOriginClass == null || pOriginClass.length() == 0) return getProperty(pName);

		Iterator iter = iAllProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty property = (CIMProperty) iter.next();
			if (property.getName().equalsIgnoreCase(pName)
					&& property.getOriginClass().equalsIgnoreCase(pOriginClass)) {

				String overridingProperty = property.getOverridingProperty();
				if (overridingProperty != null) {
					int delimeter;
					if ((delimeter = overridingProperty.indexOf('.')) > -1) return getProperty(
							overridingProperty.substring(delimeter + 1), overridingProperty
									.substring(0, delimeter));
					return getProperty(overridingProperty);
				}
				return property;
			}
		}
		return null;
	}

	protected CIMProperty getProperty(CIMProperty pProperty) {

		Iterator iter = iAllProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty property = (CIMProperty) iter.next();
			if (property.getName().equalsIgnoreCase(pProperty.getName())) { return property; }
		}
		return null;
	}

	/**
	 * Returns a list of CIMProperties for this CIMObject. For performance
	 * reasons the method returns a vector which provides direct access to the
	 * properties of the object. It is important to note that any modification
	 * to this vector may affect the consistency of the CIMMethod therefore,
	 * should be avoided. The application should decide when the vector needs to
	 * be cloned or not to preserve a consistent internal state.
	 * 
	 * @return a vector of CIMProperties for this CIMObject.
	 */
	public Vector getProperties() {
		Vector properties = new Vector();
		Iterator iter = iAllProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty property = (CIMProperty) iter.next();
			if (property.getOriginClass() == null) properties.add(property);
		}
		return properties;
	}

	/**
	 * Returns a list of CIMProperties for this CIMObject. For performance
	 * reasons the method returns the internal property vector of the object. It
	 * is important to note that any modification to this vector may affect the
	 * consistency of the CIMMethod therefore, should be avoided. The
	 * application should decide when the vector needs to be cloned or not to
	 * preserve a consistent internal state.
	 * 
	 * @return internal vector of CIMProperties for this CIMObject.
	 */
	public Vector getAllProperties() {
		return iAllProperties;
	}

	/**
	 * Adds a property to the specified object. If the property already exits in
	 * the object, the property will not be modified. No exception is thrown.
	 * 
	 * @param pProperty
	 *            property to be added
	 */
	public void addProperty(CIMProperty pProperty) {
		if (pProperty == null) throw new IllegalArgumentException("null property argument");
		Utils.addSorted(iAllProperties, pProperty);
	}

	/**
	 * Replace the current properties from the object and add the new set of
	 * properties defined by the argument. If any property is repeated, only the
	 * first instance of it will be added.
	 * 
	 * @param pProperties
	 */
	public void setProperties(Vector pProperties) {
		if (pProperties == null) {
			this.iAllProperties.setSize(0);
		} else {
			this.iAllProperties.setSize(0);

			Utils.addSorted(this.iAllProperties, pProperties);
		}
	}

	/**
	 * Sets the value for the property, if the property already exists,
	 * otherwise create a new property with the specified name, and sets assigns
	 * the especified value to it.
	 * 
	 * @param pPropertyName
	 *            A string specifing the property name.
	 * @param pValue
	 *            CIMValue that will be assigned to the property. It may be
	 *            null.
	 * @return if the property already exists, returns the modified property,
	 *         otherwise returns the new property that was created.
	 * @throws IllegalArgumentException
	 *             if the propertyName is null
	 */
	public CIMProperty setProperty(String pPropertyName, CIMValue pValue) {
		if (pPropertyName == null) throw new IllegalArgumentException("null property name argument");
		CIMProperty property = getProperty(pPropertyName);
		if (property == null) {
			property = new CIMProperty(pPropertyName, pValue);
			addProperty(property);
		} else property.setValue(pValue);

		return property;
	}

	/**
	 * Remove the specified property from the object. If the object does not
	 * contains the specified.
	 * 
	 * @param propertyName
	 *            The property's name
	 * @return The removed property
	 * @throws IllegalArgumentException
	 *             if the property name is null
	 */
	public CIMProperty removeProperty(String propertyName) {
		if (propertyName == null) throw new IllegalArgumentException("null property name argument");

		Iterator iter = iAllProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty prop = (CIMProperty) iter.next();

			if (prop.getName().equalsIgnoreCase(propertyName)) {
				iter.remove();
				return prop;
			}
		}
		return null;
	}

	/**
	 * Returns a list of key properties in this CIMObject.
	 * 
	 * @return a vector of cloned key properties.
	 */
	public Vector getKeys() {
		Vector keys = new Vector();
		Iterator iter = iAllProperties.iterator();
		while (iter.hasNext()) {
			CIMProperty property = (CIMProperty) iter.next();

			if (property.isKey()) Utils.addSorted(keys, (CIMProperty) property.clone());
		}
		return keys;
	}

	/**
	 * Gets the CIMObjectPath from this CIM Object.
	 * 
	 * @return The CIM object path
	 */
	abstract CIMObjectPath getObjectPath();

	/**
	 * Specifies the CIMObjectPath for this CIM Object.
	 * 
	 * @param pObjectPath
	 *            The object path
	 */
	abstract public void setObjectPath(CIMObjectPath pObjectPath);
}
