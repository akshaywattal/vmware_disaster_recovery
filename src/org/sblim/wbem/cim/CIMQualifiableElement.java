/**
 * CIMQualifiableElement.java
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
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */

package org.sblim.wbem.cim;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import org.sblim.wbem.util.Utils;

public abstract class CIMQualifiableElement extends CIMElement implements Serializable {

	protected Vector iQualifiers = new Vector(0);

	protected CIMQualifiableElement() {
		super();
	}

	protected CIMQualifiableElement(String pName) {
		super(pName);
	}

	/**
	 * Adds the specified qualifier to this CIM Element. If the qualifer already
	 * exits, nothing is changed.
	 * 
	 * @param pQualifier
	 *            The qualifier
	 */
	public void addQualifier(CIMQualifier pQualifier) {
		if (pQualifier == null) throw new IllegalArgumentException("null qualifier argument");

		if (getQualifier(pQualifier.getName()) == null) Utils.addSorted(iQualifiers, pQualifier);
	}

	/**
	 * Gets the specified qualfier, it exits on the CIM Element.
	 * 
	 * @param pName
	 *            The name of the qualifier.
	 * @return null if the qualifier does not exits, otherwise returns the
	 *         CIMQualifier.
	 */
	public CIMQualifier getQualifier(String pName) {
		if (pName == null) throw new IllegalArgumentException("null qualifier name argument");

		Iterator iter = iQualifiers.iterator();
		while (iter.hasNext()) {
			CIMQualifier qualifier = (CIMQualifier) iter.next();
			if (qualifier.getName().equalsIgnoreCase(pName)) { return qualifier; }
		}
		return null;
	}

	/**
	 * Replaces the CIMQualifiers for this CIMElement, with the specifed
	 * CIMQualifiers.
	 * 
	 * @param pQualifiers
	 *            a Vector of CIMQualifier objects.
	 */
	public void setQualifiers(Vector pQualifiers) {
		if (pQualifiers == null) this.iQualifiers.setSize(0);
		else {
			Utils.addSorted(this.iQualifiers, pQualifiers);
		}
	}

	/**
	 * Gets the list of the CIMQualifiers specified for this CIMElement.
	 * 
	 * @return a Vector of CIMQualifier objects.
	 */
	public Vector getQualifiers() {
		return iQualifiers;
	}

	/**
	 * Removes the specified qualifier.
	 * 
	 * @param pName
	 *            a String representing the CIMQualifier name.
	 * @return true if the qualifier was successfully removed, otherwise returns
	 *         false.
	 */
	public boolean removeQualifier(String pName) {
		if (pName == null) throw new IllegalArgumentException("null qualifier argument");

		Iterator iter = iQualifiers.iterator();
		while (iter.hasNext()) {
			CIMQualifier qual = (CIMQualifier) iter.next();
			if (qual.getName().equalsIgnoreCase(pName)) {
				iter.remove();
				return true;
			}
		}
		return false;
	}
}
