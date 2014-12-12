/**
 * CIMElement.java
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
 * 1657901    2007-02-12  ebak         Performance issues
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.cim;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import org.sblim.wbem.util.CharUtils;

public class CIMElement implements Serializable, Comparable {

	private static final long serialVersionUID = 8440203275058674221L;

	protected static final String[] OPENING_BRAKET = { "", "[", "(", "{" };

	protected static final String[] CLOSING_BRAKET = { "", "]", ")", "}" };

	protected static final String EMPTY = "";

	protected String iName;

	/**
	 * Default ctor.
	 */
	public CIMElement() {
		iName = EMPTY;
	}

	/**
	 * Constructs a CIMElement with a given name
	 * 
	 * @param pName
	 *            The name
	 */
	public CIMElement(String pName) {
		if (pName == null) throw new IllegalArgumentException("null element name argument");
		iName = pName;
	}

	/**
	 * Constructs a CIMElement with the name from a given CIMElement
	 * 
	 * @param pCimElement
	 *            The CIMElement
	 */
	public CIMElement(CIMElement pCimElement) {
		if (pCimElement == null) throw new IllegalArgumentException("null element argument");
		iName = pCimElement.iName;
	}

	/**
	 * Gets the name for this CIMElement.
	 * 
	 * @return The name
	 */
	public String getName() {
		return iName;
	}

	/**
	 * Specifies the name for this CIMElement.
	 * 
	 * @param pName
	 *            The name
	 * @throws IllegalArgumentException
	 *             if the specified name is null.
	 */
	public void setName(String pName) {
		if (pName == null) throw new IllegalArgumentException("null element name argument");
		iName = pName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CIMElement)) return false;
		return (iName.equalsIgnoreCase(((CIMElement) obj).iName));
	}

	/**
	 * Compares this object with the specified CIMElement.
	 * 
	 * @param pElement
	 *            The element to compare with
	 * @return true if string comparison of this object's name is less the
	 *         specified CIMElement's name .
	 */
	public boolean lessThan(CIMElement pElement) {
		if (pElement == null) throw new IllegalArgumentException("null element argument");

		return (iName.toUpperCase().compareTo(pElement.iName.toUpperCase()) < 0);
	}

	public CIMElement assign(CIMElement pNewElement) {
		if (pNewElement == null) throw new IllegalArgumentException("null element argument");

		iName = pNewElement.iName;
		return this;
	}

	protected static String vectorToMOFString(Vector pVector, boolean pLineFeed, int pSpacesPerTab) {
		return vectorToMOFString(pVector, pLineFeed, pSpacesPerTab, 1, true);
	}

	protected static String vectorToMOFString(Vector pVector) {
		return vectorToMOFString(pVector, true, 1, 1, true);
	}

	protected static void appendTab(StringBuffer pBuffer, int pSpacesPerTab) {
		while (pSpacesPerTab-- > 0)
			pBuffer.append('\t');
	}

	protected static String vectorToMOFString(Vector pVector, boolean pLineFeed, int pSpacesPerTab,
			int pBraketType, boolean pUseCommas) {
		StringBuffer buf = new StringBuffer();
		if (pVector != null) {
			appendTab(buf, pSpacesPerTab);
			buf.append(OPENING_BRAKET[pBraketType]);
			Iterator iter = pVector.iterator();
			int i = 0;
			while (iter.hasNext()) {
				if (i > 0) {
					if (pUseCommas) buf.append(',');
					if (pLineFeed) {
						buf.append('\n');
						appendTab(buf, pSpacesPerTab);
					}
					buf.append(' ');
				}
				Object o = iter.next();
				if (o != null) {
					if (o instanceof String || o instanceof CIMObjectPath) {
						buf.append('\"' + CharUtils.escape(o.toString()) + '\"');
					} else if (o instanceof Character) {
						buf.append('\'' + CharUtils.escape(o.toString()) + '\'');
					} else {
						buf.append(o.toString());
					}
				} else {
					buf.append("NULL");
				}
				i++;
			}
			if (pLineFeed) {
				buf.append('\n');
				// tabs(buf, tab);
			}
			buf.append(CLOSING_BRAKET[pBraketType]);
		}

		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (!(o instanceof CIMElement)) throw new IllegalArgumentException("Invalid object to compare with");
		return iName.compareToIgnoreCase(((CIMElement)o).iName);
	}
}
