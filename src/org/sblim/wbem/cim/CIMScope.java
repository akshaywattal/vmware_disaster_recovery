/**
 * CIMScope.java
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

/**
 * @author Roberto Pineiro
 * @author Chung-hao (Danny)
 * 
 */
public class CIMScope implements Cloneable, Comparable, Serializable {

	private static final long serialVersionUID = -8971815288035745263L;

	public static final int SCHEMA = 0;

	public static final int CLASS = 1;

	public static final int ASSOCIATION = 2;

	public static final int INDICATION = 3;

	public static final int PROPERTY = 4;

	public static final int REFERENCE = 5;

	public static final int METHOD = 6;

	public static final int PARAMETER = 7;

	public static final int ANY = 8;

	protected static final CIMScope[] SCOPES = new CIMScope[] { new CIMScope(SCHEMA),
			new CIMScope(CLASS), new CIMScope(ASSOCIATION), new CIMScope(INDICATION),
			new CIMScope(PROPERTY), new CIMScope(REFERENCE), new CIMScope(METHOD),
			new CIMScope(PARAMETER), new CIMScope(ANY) };

	private int iScope;

	/**
	 * Creates an object with the specified scope. Applications are encourage to
	 * use the getScope method instead of this constructor to obtain an instance
	 * of a CIMScope.
	 * 
	 * @param pScope One of the scope constants in this class
	 * @deprecated use getScope(int) instead
	 */
	public CIMScope(int pScope) {
		if (pScope < SCHEMA || pScope > ANY) throw new IllegalArgumentException("scope out of range");
		iScope = pScope;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CIMScope)) return false;
		return (iScope == ((CIMScope) obj).iScope);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return iScope;
	}

	/**
	 * Gets this scope's integer code
	 * @return The integer code
	 */
	public int getScope() {
		return iScope;
	}

	/**
	 * Returns an instance of the CIMScope given the specified argument.
	 * 
	 * @param pScopeCode
	 * @return The corresponding static scope instance
	 */
	public static CIMScope getScope(int pScopeCode) {
		if (pScopeCode < SCHEMA || pScopeCode > ANY) throw new IllegalArgumentException("scope out of range");

		return SCOPES[pScopeCode];
	}

	/**
	 * Returns the MOF representation of this object.
	 * 
	 * @return The MOF representation
	 */
	public String toMOF() {
		if (iScope == SCHEMA) return "Schema";
		if (iScope == CLASS) return "Class";
		if (iScope == ASSOCIATION) return "Association";
		if (iScope == INDICATION) return "Indication";
		if (iScope == PROPERTY) return "Property";
		if (iScope == REFERENCE) return "Reference";
		if (iScope == METHOD) return "Method";
		if (iScope == PARAMETER) return "Parameter";
		if (iScope == ANY) return "Any";
		return "INVALID SCOPE";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return toMOF();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (!(o instanceof CIMScope)) throw new IllegalArgumentException(
				"Invalid object to comparte with");

		return iScope - ((CIMScope) o).iScope;
	}
}
