/**
 * CIMFlavor.java
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

public class CIMFlavor implements Serializable, Cloneable, Comparable {

	private static final long serialVersionUID = 87386188021225409L;

	public static final int ENABLEOVERRIDE = 0;

	public static final int DISABLEOVERRIDE = 1;

	public static final int RESTRICTED = 2;

	public static final int TOSUBCLASS = 3;

	public static final int TRANSLATE = 4;

	private int iFlavor;

	private static final CIMFlavor[] FLAVORS = new CIMFlavor[] { new CIMFlavor(ENABLEOVERRIDE),
			new CIMFlavor(DISABLEOVERRIDE), new CIMFlavor(RESTRICTED), new CIMFlavor(TOSUBCLASS),
			new CIMFlavor(TRANSLATE) };

	/**
	 * Construct an object CIMFlavor given the flavor type. Applications are
	 * encourage to use getFlavor(int) instead of this constructor.
	 * 
	 * @param pFlavor
	 */
	public CIMFlavor(int pFlavor) {
		if (pFlavor < ENABLEOVERRIDE || pFlavor > TRANSLATE) throw new IllegalArgumentException(
				"Flavor value out of range");

		iFlavor = pFlavor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CIMFlavor)) return false;

		return this.iFlavor == ((CIMFlavor) obj).iFlavor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return iFlavor;
	}

	/**
	 * Gets this object flavor type.
	 * 
	 * @return The flavor
	 */
	public int getFlavor() {
		return iFlavor;
	}

	/**
	 * Gets an object of the specified CIMFlavor.
	 * 
	 * @param flavor
	 *            A flavor constant from this class
	 * @return The corresponding flavor
	 */
	public static CIMFlavor getFlavor(int flavor) {
		if (flavor < ENABLEOVERRIDE || flavor > TRANSLATE) throw new IllegalArgumentException(
				"Flavor value out of range");

		return FLAVORS[flavor];
	}

	/**
	 * Returns the MOF representation of this object.
	 * 
	 * @return The MOF representation
	 */
	public String toMOF() {
		if (iFlavor == ENABLEOVERRIDE) return "EnableOverride";
		if (iFlavor == DISABLEOVERRIDE) return "DisableOverride";
		if (iFlavor == RESTRICTED) return "Restricted";
		if (iFlavor == TOSUBCLASS) return "ToSubclass";
		if (iFlavor == TRANSLATE) return "Translatable";
		return "INVALID FLAVOR";
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
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (!(o instanceof CIMFlavor)) throw new IllegalArgumentException(
				"Invalid object to comparte with");

		return iFlavor - ((CIMFlavor) o).iFlavor;
	}
}
