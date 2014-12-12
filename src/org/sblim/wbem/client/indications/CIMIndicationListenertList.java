/**
 * CIMIndicationListenertList.java
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
package org.sblim.wbem.client.indications;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Maintains a list of CIMListeners. This class must be used with
 * CIMIndicationHandler, CIMEventDispatcher classes, which provide the necesary
 * framework to handle CIMIndications.
 * 
 */
public class CIMIndicationListenertList {

	protected Hashtable iTableId = new Hashtable();

	public CIMIndicationListenertList() {}

	public synchronized void addListener(CIMListener pListener) {
		addListener(pListener, String.valueOf(pListener.hashCode()));
	}

	private synchronized void addListener(CIMListener pListener, String pId) {

		Vector listenerList = (Vector) iTableId.get(pId);
		if (listenerList == null) {
			listenerList = new Vector();
			iTableId.put(pId, listenerList);
		}
		listenerList.add(pListener);
	}

	public synchronized void removeListener(CIMListener pListener) {
		String id = String.valueOf(pListener.hashCode());
		String.valueOf(pListener.hashCode());
		Vector listenerList = (Vector) iTableId.get(id);
		if (listenerList != null) {
			listenerList.remove(pListener);
		}
	}

	public synchronized Vector getListeners() {
		return getListeners(null);
	}

	public synchronized Vector getListeners(String pId) {
		if (pId == null) pId = "";
		Vector listenerList = (Vector) iTableId.get(pId);
		if (listenerList == null) {
			if (iTableId.size() == 1) {
				listenerList = new Vector();
				Enumeration enumeration = iTableId.elements();
				while (enumeration.hasMoreElements()) {
					listenerList.addAll((Vector) enumeration.nextElement());
				}
				return listenerList;
			}
			return new Vector(0);
		}

		return new Vector(listenerList);
	}
}
