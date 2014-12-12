/**
 * CIMEventDispatcher.java
 *
 * (C) Copyright IBM Corp. 2005, 2012
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author : Roberto Pineiro, IBM, roberto.pineiro@us.ibm.com  
 * @author : Chung-hao Tan, IBM, chungtan@us.ibm.com
 * 
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1498927    2006-06-01  lupusalex    Fill gaps in logging coverage
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 2003590    2008-06-30  blaschke-oss Change licensing from CPL to EPL
 * 2204488 	  2008-10-28  raman_arora  Fix code to remove compiler warnings
 * 2524131    2009-01-21  raman_arora  Upgrade client to JDK 1.5 (Phase 1)
 * 2531371    2009-02-10  raman_arora  Upgrade client to JDK 1.5 (Phase 2)
 * 3477087    2012-01-23  blaschke-oss Need Access to an Indication Sender's IP Address
 */
package org.sblim.cimclient.internal.wbem.indications;

import java.util.EventListener;
import java.util.LinkedList;
import java.util.logging.Level;

import javax.wbem.listener.IndicationListener;

import org.sblim.cimclient.IndicationListenerSBLIM;
import org.sblim.cimclient.internal.logging.LogAndTraceBroker;

/**
 * Class CIMEventDispatcher is required for indication handling.
 * 
 */
public class CIMEventDispatcher extends Thread {

	protected LinkedList<CIMEvent> iEventQueue = new LinkedList<CIMEvent>();

	protected EventListener iListener = null;

	private boolean iAlive = true;

	private LogAndTraceBroker iLogger = LogAndTraceBroker.getBroker();

	/**
	 * Construct a CIMEventDispatcher object which distributes CIMEvents to the
	 * appropriate CIMListener. The EventListener must be an instance of
	 * IndicationListener or IndicationListenerSBLIM.
	 * 
	 * @param pListener
	 *            The CIMListener (IndicationListener or
	 *            IndicationListenerSBLIM) which receives the CIMEvents to be
	 *            dispatched.
	 */
	public CIMEventDispatcher(EventListener pListener) {
		if (!(pListener instanceof IndicationListener)
				&& !(pListener instanceof IndicationListenerSBLIM)) throw new IllegalArgumentException(
				"Listener must be instance of IndicationListener or IndicationListenerSBLIM");
		this.iListener = pListener;
		setDaemon(true);
		setName("CIMEventDispatcher");
		start();
	}

	/**
	 * Propagates the CIMEvent to the event consumers.
	 * 
	 * @param pEvent
	 *            The CIMEvent to be dispatched.
	 */
	public synchronized void dispatchEvent(CIMEvent pEvent) {

		if (pEvent != null) {
			this.iEventQueue.add(pEvent);
			this.iLogger.trace(Level.FINE, "Added CIMEvent (id=" + pEvent.getID()
					+ " to the queue (" + this.iEventQueue.size() + " elements total)");
			notify();
		} else {
			this.iLogger.trace(Level.WARNING, "CIMEvent to dispatch was null");
		}
	}

	/**
	 * Starts the dispatching engine of the CIMEventDispatcher.
	 * 
	 */
	public synchronized void startup() {
		this.iAlive = true;
		start();
	}

	/**
	 * Stops the dispatching of events.
	 */
	public synchronized void kill() {
		this.iAlive = false;
		notify();
	}

	/**
	 * close
	 */
	public synchronized void close() {
		kill();
	}

	private synchronized CIMEvent getEvent() {
		CIMEvent event = null;
		while (event == null) {
			try {
				if (this.iEventQueue.size() == 0) wait();
			} catch (InterruptedException e) { /**/}
			if (!this.iAlive) break;
			if (this.iEventQueue.size() > 0) {
				event = this.iEventQueue.remove(0);
				this.iLogger.trace(Level.FINER, "Removed CIMEvent (id=" + event.getID()
						+ "from the queue (" + this.iEventQueue.size() + " elements left)");
			}
		}
		return event;
	}

	@Override
	public void run() {
		while (this.iAlive) {
			try {
				CIMEvent event = getEvent();
				if (event != null) {
					this.iLogger.trace(Level.FINER, "Processing CIMEvent (id=" + event.getID()
							+ ")");
					try {
						if (this.iListener instanceof IndicationListener) ((IndicationListener) this.iListener)
								.indicationOccured(event.getID(), event.getIndication());
						else // if instanceof IndicationListenerSBLIM)
						((IndicationListenerSBLIM) this.iListener).indicationOccured(event.getID(),
								event.getIndication(), event.getInetAddress());
					} catch (Throwable t) {
						this.iLogger.trace(Level.FINE, "Exception caught in listener ("
								+ this.iListener.getClass().getName()
								+ ") while processing CIMEvent", t);
					}
				}
			} catch (Throwable t) {
				this.iLogger.trace(Level.FINE, "Exception in event dispatcher loop", t);
			}
		}
	}
}
