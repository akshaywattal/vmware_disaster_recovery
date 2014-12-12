/**
 * HttpClientPool.java
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
 * 1353168    2005-11-24  fiuczy       Possible NullPointerExcection in HttpClient.streamFinished()
 * 1498130    2006-05-31  lupusalex    Selection of xml parser on a per connection basis
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1646434    2007-01-28  lupusalex    CIMClient close() invalidates all it's enumerations
 * 1647159    2007-01-29  lupusalex    HttpClientPool runs out of HttpClients
 * 1702751    2007-04-18  lupusalex    Further leak prevention
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.http;

import java.net.URI;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.util.SessionProperties;

public class HttpClientPool {

	private Vector iAllConnections = new Vector(1);

	private Vector iAvailableConnections = new Vector(1);

	private SessionProperties iSessionProperties = SessionProperties.getGlobalProperties();

	private boolean iClosed = false;

	private int iPoolSize = 10;

	/**
	 * Returns the number of connections in this pool that are available/free
	 * for (re-)use.
	 * 
	 * @return number of available/free connections in pool
	 */
	public int getNumberOfAllConnections() {
		return iAllConnections.size();
	}

	/**
	 * Returns the number of all connections in this pool.
	 * 
	 * @return number of all connections in pool
	 */
	public int getNumberOfAvailableConnections() {
		return iAvailableConnections.size();
	}

	public SessionProperties getSessionProperties() {
		return iSessionProperties;
	}

	public void setSessionProperties(SessionProperties pSessionProperties) {
		iSessionProperties = (pSessionProperties != null) ? pSessionProperties : SessionProperties
				.getGlobalProperties();
		iPoolSize = iSessionProperties.getConnectionPoolSize();
	}

	/**
	 * Returns the available connections of this pool for a given
	 * URI&AuthorizationHandler
	 * 
	 * @param pUri
	 *            The uri
	 * @param pHandler
	 *            The authoriaztion handler
	 * @return A connection if one is available, <code>null</code> otherwise
	 */
	public synchronized HttpClient retrieveAvailableConnectionFromPool(URI pUri,
			AuthorizationHandler pHandler) {
		if (iClosed) { throw new IllegalStateException(
				"HttpClientPool is closed. Retrieve connection failed."); }
		Logger logger = SessionProperties.getGlobalProperties().getLogger();
		if (getNumberOfAvailableConnections() > 0) {
			if (logger.isLoggable(Level.FINER)) {
				logger.log(Level.FINER, "Reusing client (" + pUri.toString() + ", max: "
						+ getPoolSize() + ", current:" + getNumberOfAvailableConnections());
			}

			return (HttpClient) iAvailableConnections.remove(0);
		}

		if (logger.isLoggable(Level.FINER)) {
			logger.log(Level.FINER, "New client (" + pUri.toString() + ", max: " + getPoolSize()
					+ ", current:" + getNumberOfAvailableConnections());
		}
		HttpClient client = new HttpClient(pUri, this, pHandler);
		addConnectionToPool(client);
		return client;
	}

	/**
	 * Add the connection to the pool. Connection is added as available
	 * connection. Use method {@link #addConnectionToPool(HttpClient)} to add
	 * the connection without being available for reuse.
	 * 
	 * @param httpClient
	 *            connection that is to be added to the pool
	 * @return true if connection was added otherwise false
	 */
	public synchronized boolean returnAvailableConnectionToPool(HttpClient httpClient) {
		if (httpClient == null) { return false; }

		if (iClosed) {
			iAllConnections.remove(httpClient);
			httpClient.disconnect();
			return false;
		}

		if (iPoolSize > iAvailableConnections.size()) {
			if (!iAvailableConnections.contains(httpClient)) {
				// ensure that the connection exists in allConnections owned by
				// pool
				addConnectionToPool(httpClient);
				iAvailableConnections.add(httpClient);
				return true;
			}
		} else {
			SessionProperties.getGlobalProperties().getLogger().log(Level.FINER,
					"Http pool size reached, discarding client.");
			iAllConnections.remove(httpClient);
			iAvailableConnections.remove(httpClient);
			httpClient.disconnect();
		}
		return false; // connection was not added to pool
	}

	/**
	 * Add the connection to the pool, but does NOT add it as available
	 * connection. Use method
	 * {@link #returnAvailableConnectionToPool(HttpClient)} to also add the
	 * connection to the available connections.
	 * 
	 * @param httpClient
	 *            connection that is to be added to the pool
	 * @return true if connection was added otherwise false
	 */
	private boolean addConnectionToPool(HttpClient httpClient) {
		if (httpClient != null && !iAllConnections.contains(httpClient)) {
			// if connection is not in pool add it
			iAllConnections.add(httpClient);
			return true;
		}
		return false;
	}

	/**
	 * Removes a connection completly from the pool. The connection is not
	 * closed by this method, that has to be done by the caller.
	 * 
	 * @param httpClient
	 *            connection that is to be removed from the pool
	 * @return true if connection was removed otherwise false
	 */
	public synchronized boolean removeConnectionFromPool(HttpClient httpClient) {
		if (httpClient != null) {
			iAvailableConnections.remove(httpClient);
			if (iAllConnections.remove(httpClient)) { return true; }
			return false; // connection was not in pool!
		}
		return false; // no connection given
	}

	public synchronized void closePool(boolean pKeepActive) {
		iClosed = true;
		Iterator iter = pKeepActive ? iAvailableConnections.iterator() : iAllConnections.iterator();
		while (iter.hasNext()) {
			HttpClient httpClient = (HttpClient) iter.next();
			httpClient.disconnect();
		}
		if (pKeepActive) {
			iAllConnections.removeAll(iAvailableConnections);
		} else {
			iAllConnections.clear();
		}
		iAvailableConnections.clear();
	}

	protected void finalize() {
		closePool(true);
	}

	/**
	 * Returns poolSize
	 * 
	 * @return The value of poolSize.
	 */
	public int getPoolSize() {
		return iPoolSize;
	}

}
