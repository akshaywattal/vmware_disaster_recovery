/**
 * Utils.java
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
 * 1657901    2007-02-12  ebak         Performance issues
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

/*
 * Created on Sep 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sblim.wbem.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.util.SessionProperties;

/**
 * @author Roberto
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Utils {
	public static void addSorted(Vector vec, Comparable o) {
		// ebak: possible performance improvement
		int i=Collections.binarySearch(vec, o);
		if (i>=0) {
			Logger logger = SessionProperties.getGlobalProperties().getLogger();
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "adding a property that already exists"+o);
			}
			return;
		}
		// i= -insPoint - 1
		// insPoint=-1-i
		i=-1-i;
		vec.insertElementAt(o, i);
	}
	
	public static void addSorted(Vector vec1, Vector vec2) {
		Iterator iter = vec2.iterator();
		while (iter.hasNext()) {
			Comparable comp = (Comparable)iter.next();
			Utils.addSorted(vec1, comp);				
		}
	}
	
	public static boolean hasValue(Vector vec, Comparable o) {
		Iterator iter = vec.iterator();
		while (iter.hasNext()) {
			Comparable local = (Comparable)iter.next();
			int comparison = local.compareTo(o);
			if (comparison == 0) return true;
		}
		return false;
	}
}
