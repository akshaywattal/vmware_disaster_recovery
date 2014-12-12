/**
 * CIMObjectPath.java
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
 * Flag      Date        Prog         Description
 *------------------------------------------------------------------------------- 
 *   17953   2005-08-10  pineiro5     CIMClient does a case sensitive retrieve of property names
 *   18076   2005-08-11  pineiro5     localhost incorrectly added to CIMObjectPath
 * 1535756   2006-08-08  lupusalex    Make code warning free
 * 2219646   2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325   2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.cim;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.util.CharUtils;
import org.sblim.wbem.util.SessionProperties;

/**
 * Points to the specified CIM Class or CIM Instance. The object path is a
 * reference to a CIM Object, denoted by a host, namespace, object name, and
 * keys (if the object is an instance). The namespace is handle in an absolute
 * form with respect to the default namespace define for the CIMClient at the
 * moment of its construction.
 * 
 * root/lsissi:LSISSI_StorageSystem.Name="TheComputer",CreationClassName="LSISSI_StorageSystem"
 * 
 * The namespace associated with it is <i>root/lsissi</i>. The classname
 * associated with the object path is <i>LSISSI_StorageSystem</i> This object
 * is uniquely identified by two key property values: Name="TheComputer"
 * CreationClassName="LSISSI_StorageSystem"
 * 
 * Note: host information is maintained, but is upto the application to resolve
 * its address.
 * 
 */
public class CIMObjectPath implements Serializable, Cloneable {

	private static final long serialVersionUID = 2827390267133787271L;

	private static final String EMPTY = "";

	private String iClassName;

	private String iHost;

	private String iNamespace;

	private Vector iKeys = new Vector();

	protected String iToString;

	/**
	 * Constructs a CIMObjectPath.
	 * 
	 */
	public CIMObjectPath() {
		this(EMPTY, EMPTY);
	}

	/**
	 * Constructs a CIMObjectPath pointing to the specified ClassName.
	 * 
	 * @param pClassName
	 */
	public CIMObjectPath(String pClassName) {
		this(pClassName, EMPTY);
	}

	/**
	 * Constructs a CIMObjectPath with the specified ClassName, on the defined
	 * namespace.
	 * 
	 * @param pClassName
	 *            a String representing the CIM ClassName.
	 * @param pNamespace
	 *            a String which represents the namespace. Only forward slashes
	 *            ('/') are allowed on the namespace. The namespace should not
	 *            start or end with a '/', it will be automatically removed. As
	 *            a result a namespace like "/root/cimv2", will result on
	 *            "root/cimv2", and "/" would be translated into an empty
	 *            namespace ("")
	 * 
	 * Empty namespaces are handled as null namespace. Which in turn are
	 * replaced by the default namespace (assigned to the CIM client during its
	 * contruction) when operations are performed on objects with null
	 * namespaces.
	 * 
	 * @throws CIMException
	 *             CIM_ERR_INVALID_PARAMETER if a namespace is constructed with
	 *             '\'.
	 */
	public CIMObjectPath(String pClassName, String pNamespace) throws CIMException {
		iClassName = pClassName;
		setNamespace(pNamespace);
	}

	/**
	 * Constructs a CIMObjectPath with the specified CIM ClassName and the set
	 * of key values. A null namespace is assigned to this object.
	 * 
	 * @param pClassName
	 *            a String representing the ClassName.
	 * @param pKeyValuePairs
	 *            a Vector containing CIMProperties objects representing the
	 *            keys for this CIMObjectPath.
	 */
	public CIMObjectPath(String pClassName, Vector pKeyValuePairs) {
		this(pClassName, EMPTY);

		if (pKeyValuePairs == null) throw new IllegalArgumentException("null keys argument");
		setKeys(pKeyValuePairs);
	}

	protected void setNamespace(String pNamespace) {
		if (pNamespace != null) {
			if (pNamespace.indexOf('\\') > -1) throw new CIMException(
					CIMException.CIM_ERR_INVALID_PARAMETER,
					"namespace should not contain '\\' character");
			boolean tryAgain = false;
			do {
				tryAgain = false;
				if (pNamespace.startsWith("/")) {
					pNamespace = pNamespace.substring(1);
					tryAgain = true;
				}
				if (pNamespace.endsWith("/")) {
					pNamespace = pNamespace.substring(0, pNamespace.length() - 1);
					tryAgain = true;
				}
			} while (tryAgain);
			iNamespace = pNamespace;
		} else iNamespace = "";
	}

	/**
	 * Add a new key to the property list
	 * 
	 * @param pPropertyName
	 * @param pValue
	 */
	public void addKey(String pPropertyName, CIMValue pValue) {
		if (pPropertyName == null) throw new IllegalArgumentException("null property name argument");

		addKey(new CIMProperty(pPropertyName, pValue));
	}

	/**
	 * Adds the specified CIMProperty object as part of the keys.
	 * 
	 * @param pProperty
	 */
	public void addKey(CIMProperty pProperty) {
		if (pProperty == null) throw new IllegalArgumentException("null property name argument");

		try {
			CIMQualifier key = new CIMQualifier("Key");
			pProperty.addQualifier(key);
		} catch (Exception e) {
			Logger logger = SessionProperties.getGlobalProperties().getLogger();
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "exception while adding key property", e);
			}
		}
		Iterator iter = iKeys.iterator();
		int i = 0;
		while (iter.hasNext()) {
			CIMProperty key = (CIMProperty) iter.next();
			String name = key.getName();
			if (name.equalsIgnoreCase(pProperty.getName())) return;
			if (name.compareTo(pProperty.getName()) > 0) {
				iKeys.insertElementAt(pProperty, i);
				iToString = null;
				return;
			}
			i++;
		}
		iKeys.add(pProperty);
		iToString = null;
	}

	/**
	 * Removes all the key from this CIMObjectPath.
	 * 
	 */
	public void removeAllKeys() {
		iKeys.setSize(0);
		iToString = null;
	}

	/**
	 * Removes the specified key from this CIMObjectPath.
	 * 
	 * @param pPropertyName
	 *            a String representing the key name.
	 */
	public void removeKey(String pPropertyName) {
		Iterator iter = iKeys.iterator();
		while (iter.hasNext()) {
			CIMProperty key = (CIMProperty) iter.next();
			if (key.getName().equalsIgnoreCase(pPropertyName)) {
				iter.remove();
				iToString = null;
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		CIMObjectPath that = new CIMObjectPath();
		that.iNamespace = iNamespace;
		that.iClassName = iClassName;
		that.iHost = iHost;

		if (iKeys != null) {
			Iterator iter = iKeys.iterator();
			while (iter.hasNext()) {
				that.iKeys.add(((CIMProperty) iter.next()).clone());
			}
		}
		return that;
	}

	/**
	 * Returns a CIMObjectPath from the given string.
	 * 
	 * @param pObjectPath
	 *            a string with the following format
	 *            //hostname/namespace_level1/namespace_level2/ClassName.key1="value1"[,key2="value2"] *
	 *            for example
	 *            //localhost/root/cimv2/CIM_ComputerSystem.Name="MyComputer"
	 *            Note that values can represent CIM ObjectPath as well. Key
	 *            values are represented
	 * @return The object path
	 * @deprecated
	 */
	public static CIMObjectPath parse(String pObjectPath) {
		// TODO parse objectPath
		throw new RuntimeException("not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 
	 * Compares this object with the specified object. If the object passed is
	 * an CIMObjectPath, this method returns true only if the classname, the
	 * namespace are the same, both objectpath contains the same number of
	 * properties, and each property has the same value assigned to it. This
	 * method does not test for CIM qualifiers assigned to properties. It does
	 * not include hostname information during the comparation.
	 * 
	 * This methods produces the same results a equals(true). Note that the
	 * hostname is excluded from the comparison.
	 */
	public boolean equals(Object obj) {
		return equals(obj, true);
	}

	/**
	 * Compares this object with the specified object for equality.
	 * 
	 * @param pObject
	 *            The other object
	 * @param pIgnoreClassOrigin
	 *            if <code>true</code> the class origin is ignored
	 * @return <code>true</code> if pObject is equal to <code>this</code>,
	 *         <code>false</code> otherwise
	 */
	public boolean equals(Object pObject, boolean pIgnoreClassOrigin) {
		if (!(pObject instanceof CIMObjectPath)) return false;
		if (pObject == this) return true;
		CIMObjectPath that = (CIMObjectPath) pObject;

		// className should not be null
		if (!(iClassName == null ? that.iClassName == null : iClassName
				.equalsIgnoreCase(that.iClassName))) return false;

		if (!(iNamespace == null ? that.iNamespace == null : iNamespace
				.equalsIgnoreCase(that.iNamespace))) return false;

		/*
		 * What should we do if the CIMOM exposes one external IP and internally
		 * uses another. The request contains the global IP address used by
		 * client outside the firewall, but the CIMOM uses the internal IP
		 * address to generate the responses
		 */
		// if ( !((this.host != null && this.host.equals(that.host))
		// || (this.host == null && that.host == null)) )
		// return false;
		if (that.iKeys.size() != iKeys.size()) return false;
		/*
		 * We do not have to look for the property since both properties arrays
		 * must be sorted in ascending order.
		 */
		for (int i = 0; i < that.iKeys.size(); i++) {
			CIMProperty thatProperty = (CIMProperty) that.iKeys.elementAt(i);
			CIMProperty thisProperty = (CIMProperty) iKeys.elementAt(i);
			if (!thatProperty.getName().equalsIgnoreCase(thisProperty.getName())) { return false; }
			if (!pIgnoreClassOrigin
					&& !(thisProperty.getOriginClass() == null ? thatProperty.getOriginClass() == null
							: thatProperty.getOriginClass().equalsIgnoreCase(
									thisProperty.getOriginClass()))) return false;
			CIMValue thatValue = thatProperty.getValue();
			CIMValue thisValue = thisProperty.getValue();
			if (!(thisValue == null ? thisValue == null : thisValue.equals(thatValue))) return false;
		}
		return true;
	}

	/**
	 * Returns the host of the current object path.
	 * 
	 * @return The host
	 */
	public String getHost() {
		return iHost;
	}

	/**
	 * Returns a keyed CIMProperty with the specified name.
	 * 
	 * @param pPropertyName
	 *            The name of the key property
	 * @return The key property
	 */
	public CIMProperty getKey(String pPropertyName) {
		if (pPropertyName == null) return null;

		Iterator iterator = iKeys.iterator();
		while (iterator.hasNext()) {
			CIMProperty key = (CIMProperty) iterator.next();

			if (key.getName().equalsIgnoreCase(pPropertyName)) return key;
		}
		return null;
	}

	/**
	 * Returns an vector with all the key properties from the CIMObjectPath. For
	 * performance reasons, this method returns the Vector object internally
	 * used. The application MUST deside when this object and the objects
	 * CIMProperties need to be cloned to prevend an sinconsistent state.
	 * 
	 * @return The key vector
	 */
	public Vector getKeys() {
		return iKeys;
	}

	/**
	 * Returns the namespace from this CIMObjectPath.
	 * 
	 * @return The namespace
	 */
	public String getNameSpace() {
		return iNamespace;
	}

	/**
	 * Specifies the namespace for this CIMObjectPath.
	 * 
	 * @param pNamespace
	 *            The namespace
	 */
	public void setNameSpace(CIMNameSpace pNamespace) {
		if (pNamespace == null) {
			iNamespace = null;
			iHost = null;
		} else {
			iHost = pNamespace.getHost();
			setNamespace(pNamespace.getNameSpace());
		}
		iToString = null;
	}

	/**
	 * Returns the object name to which this object path points.The object name
	 * is a CIMClass name.
	 * 
	 * @return The object name
	 */
	public String getObjectName() {
		return iClassName;
	}

	/**
	 * Specifies the host name.
	 * 
	 * @param pHost
	 *            The host name
	 */
	public void setHost(String pHost) {
		iHost = pHost;
	}

	/**
	 * Specifies the namespace for this CIMObjectPath.
	 * 
	 * @param pNamespace
	 *            The namespace
	 */
	public void setNameSpace(String pNamespace) {
		// if (namespace == null) throw new IllegalArgumentException("null
		// namespace argument");

		setNamespace(pNamespace);
		iToString = null;
	}

	/**
	 * Specifies the object name for this CIMObjectPath.
	 * 
	 * @param pName
	 *            The object name
	 */
	public void setObjectName(String pName) {
		// if (className == null) throw new IllegalArgumentException("null
		// namespace argument");

		iClassName = pName;
		iToString = null;
	}

	/**
	 * Sets the key properties for this CIMObjectPath.
	 * 
	 * @param pKeys
	 *            a vector containing CIMProperties.
	 * @throws IllegalArgumentException
	 *             if any of the elements within the vector is not a CIMProperty
	 */
	public void setKeys(Vector pKeys) {
		if (pKeys == null) throw new IllegalArgumentException("null keys argument");

		try {
			Iterator iter = pKeys.iterator();
			while (iter.hasNext()) {
				CIMProperty prop = (CIMProperty) iter.next();
				addKey(prop);
			}
			iToString = null;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(
					"key vector contains must contains CIMProperties only");
		}
		// toString = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 * 
	 * Returns the string representation of an object path. An object path which
	 * denotes an instance is represented as follow: namespace:classname or
	 * namespace:classname.key1="value2",key2="value2",...,keyn="valuen" An
	 * object path which denotes a class is denoted as follow:
	 * namespace:classname or namespace:classname=@
	 * 
	 * The <i>namespace</i> must contains forward slash, but does not starts or
	 * ends with a forward slash. Note that this representation does not include
	 * host information.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		/*
		 * if (host != null) { if (!host.startsWith("//")) { sb.append("//"); }
		 * sb.append(host); }
		 */
		if (iNamespace != null && iNamespace.trim().length() != 0) {
			sb.append(iNamespace);
			sb.append(":");
		}
		if (iClassName != null) {
			sb.append(iClassName);
		}
		if (iKeys != null) {
			if (iKeys.size() > 0) {
				sb.append(".");
				boolean first = false;
				for (int i = 0; i < iKeys.size(); i++) {
					CIMProperty property = (CIMProperty) iKeys.elementAt(i);
					if (property == null) continue;

					CIMValue value = property.getValue();
					if (first) {
						sb.append(",");
					}
					first = true;
					String propertyName = property.getName();
					Object o = null;
					if (value != null && (o = value.getValue()) != null) {
						if (o instanceof String) sb.append(propertyName + "=\""
								+ CharUtils.escape((String) o) + "\"");
						else if (value.getValue() instanceof CIMObjectPath) sb.append(propertyName
								+ "=\"" + CharUtils.escape(o.toString()) + "\"");
						else sb.append(propertyName + "=" + o.toString());
					} else {
						sb.append(propertyName + "=null");
					}
				}
			} else {
				// sb.append("=@");
			}
		}
		iToString = sb.toString();
		return iToString;
	}

	/**
	 * Returns the string representation of an object path. An object path which
	 * denotes an instance is represented as follow: namespace:classname or
	 * namespace:classname.key1="value2",key2="value2",...,keyn="valuen" An
	 * object path which denotes a class is denoted as follow:
	 * namespace:classname or namespace:classname=@
	 * 
	 * The <i>namespace</i> must contains forward slash, but does not starts or
	 * ends with a forward slash. Note that this representation does not include
	 * host information.
	 * 
	 * @param pIncludeHostname
	 *            specifies if host information should be included
	 * @return The string representation
	 */
	public String toString(boolean pIncludeHostname) {
		StringBuffer sb = new StringBuffer();
		if (iHost != null) {
			if (!iHost.startsWith("//")) {
				sb.append("//");
			}
			sb.append(iHost);
			sb.append("/");
		} else {
			sb.append("///");
		}
		sb.append(toString());
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int keyHashCode = 0;
		Iterator iter = iKeys.iterator();
		while (iter.hasNext()) {
			CIMProperty prop = (CIMProperty) iter.next();
			int propHashCode = 0;
			propHashCode = prop.getName().hashCode() << 16 + ((prop.getValue() != null) ? prop
					.getValue().hashCode() : 0);

			keyHashCode = keyHashCode * 31 + propHashCode;
		}
		return ((iClassName != null) ? iClassName.toUpperCase().hashCode() : 0)
				+ ((iNamespace != null) ? iNamespace.hashCode() : 0) + keyHashCode;
	}

	public static void main(String[] args) {
		CIMObjectPath op1 = new CIMObjectPath("CIM_ComputerSystem", "/////");
		CIMObjectPath op2 = new CIMObjectPath("CIM_ComputerSystem", (String) null);

		System.out.println("Equals:" + op1.equals(op2));
		CIMProperty p1 = new CIMProperty("Name", new CIMValue(new Integer(1), CIMDataType
				.getPredefinedType(CIMDataType.SINT32)));
		CIMProperty p2 = new CIMProperty("Name", new CIMValue(new Integer(1), CIMDataType
				.getPredefinedType(CIMDataType.SINT32)));
		p1.addQualifier(new CIMQualifier("Key"));
		p2.addQualifier(new CIMQualifier("Overridable"));
		op1.addKey(p1);
		op2.addKey(p2);
		System.out.println("op1" + op1);
		System.out.println("op2" + op2);
		long initial = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {

			op1.equals(op2);
			// System.out.println("Equals:" +op1.equals(op2));
			op1.hashCode();
			// System.out.println("Hashcode op1:" +op1.hashCode());
			op2.hashCode();
			// System.out.println("Hashcode op2:" +op2.hashCode());
		}
		System.out.println("Elapse:" + ((System.currentTimeMillis() - initial) / 1000000.0));
	}
}
