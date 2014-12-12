/**
 * CIMEventDispatcher.java
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
 * 1498927    2006-06-01  lupusalex    Fill gaps in logging coverage
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1649779    2007-02-01  lupusalex    Indication listener threads freeze
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */
package org.sblim.wbem.client.indications;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sblim.wbem.util.SessionProperties;

public class CIMEventDispatcher extends Thread {

	protected Vector iEventQueue = new Vector();

	protected CIMIndicationListenertList iClient = null;

	private boolean iAlive = true;

	/**
	 * Construct a CIMEventDispatcher object which distributes CIMEvents to the
	 * appropiate CIMListener.
	 * 
	 * @param pClient
	 */
	public CIMEventDispatcher(CIMIndicationListenertList pClient) {
		this.iClient = pClient;
		setDaemon(true);
		setName("CIM EventDispatcher");
		start();
	}

	/**
	 * Propagates the CIMEvent to the event consumers.
	 * 
	 * @param pEvent
	 */
	public synchronized void dispatchEvent(CIMEvent pEvent) {

		Logger logger = SessionProperties.getGlobalProperties().getLogger();
		if (pEvent != null) {
			iEventQueue.add(pEvent);
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Added CIMEvent (id=" + pEvent.getID() + " to the queue ("
						+ iEventQueue.size() + " elements total)");
			}
			this.notify();
		} else if (logger.isLoggable(Level.WARNING)) {
			logger.log(Level.WARNING, "CIMEvent to dispatch was null");
		}
	}

	/**
	 * Starts the dispatching engine of the CIMEventDispatcher.
	 * 
	 */
	public synchronized void startup() {
		iAlive = true;
		start();
	}

	/**
	 * Stops the dispatching of events.
	 */
	public synchronized void kill() {
		iAlive = false;
		notify();
	}

	public synchronized void close() {
		kill();
	}

	private synchronized CIMEvent getEvent() {
		CIMEvent event = null;
		Logger logger = SessionProperties.getGlobalProperties().getLogger();
		while (event == null) {
			try {
				if (iEventQueue.size() == 0) wait();
			} catch (InterruptedException e) {}
			if (!iAlive) break;
			if (iEventQueue.size() > 0) {
				event = (CIMEvent) iEventQueue.remove(0);
				if (logger.isLoggable(Level.FINE)) {
					logger.log(Level.FINE, "Removed CIMEvent (id=" + event.getID()
							+ "from the queue (" + iEventQueue.size() + " elements left)");
				}
			}
		}
		return event;
	}

	public void run() {
		Logger logger = SessionProperties.getGlobalProperties().getLogger();
		CIMEvent event;
		do {
			event = getEvent();
			if (!iAlive) break;

			Vector currentListeners = iClient.getListeners(event.getID());
			int total = currentListeners.size();
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Processing CIMEvent (id=" + event.getID() + ")");
			}
			Vector timePerHandler = new Vector();
			long totalTime = System.currentTimeMillis();
			for (int i = 0; i < total; i++) {
				long totalTimePerHandle = System.currentTimeMillis();
				CIMListener listener = (CIMListener) currentListeners.elementAt(i);
				try {
					listener.indicationOccured(event);
				} catch (Exception e) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Exception caught in listener ("
								+ listener.getClass().getName() + ") while processing CIMEvent", e);
					}
				}
				timePerHandler.add(new Long(System.currentTimeMillis() - totalTimePerHandle));
			}
			totalTime = System.currentTimeMillis() - totalTime;
			if (logger.isLoggable(Level.FINER)) {
				logger.log(Level.FINER, "Total time to process the event by all handlers "
						+ totalTime + "ms, TimePerHandle=" + timePerHandler.toString());
			}
		} while (true);
		// clean up
	}
}
