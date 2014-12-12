/**
 * HttpServerConnection.java
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
 * 1422316    2005-05-08  lupusalex    Disable delayed acknowledgement
 * 1483270    2006-05-15  lupusalex    Using Several Cim Clients cause an indication problem
 * 1498130    2006-05-31  lupusalex    Selection of xml parser on a per connection basis
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1649779    2007-02-01  lupusalex    Indication listener threads freeze
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.util.SessionProperties;
import org.sblim.wbem.util.ThreadPool;

/**
 * Class HttpServerConnection implements the outer shell of a HTTP server. It
 * accepts incoming connections and puts them in a queue to be serviced by an
 * independent thread
 * 
 */
public class HttpServerConnection implements Runnable {

	private int iPort;

	private ServerSocket iServerSocket;

	private HttpConnectionHandler iHandler;

	private HttpConnectionDispatcher iDispatcher;

	private boolean iClose = true;

	private String iServerName;

	private boolean iSsl;

	private Thread iRunner;

	private SessionProperties iSessionProperties;

	/**
	 * Ctor.
	 * 
	 * @param handler
	 *            The handler for incoming connections
	 * @param port
	 *            The port to listen on
	 * @param ssl
	 *            If <code>true</code> SSL is enabled
	 * @param properties
	 *            The configuration to use
	 * @throws IOException
	 *             On failure to create the server socket
	 */
	public HttpServerConnection(HttpConnectionHandler handler, int port, boolean ssl,
			SessionProperties properties) throws IOException {

		iPort = port;
		iHandler = handler;
		iSsl = ssl;
		iServerName = "HTTP" + (ssl ? "S Server" : " Server");
		iSessionProperties = (properties != null) ? properties : SessionProperties
				.getGlobalProperties();

		iServerSocket = HttpSocketFactory.getInstance().getServerSocketFactory(ssl,
				iSessionProperties).createServerSocket(port);
	}

	public HttpServerConnection(HttpConnectionHandler handler, int port, boolean ssl)
			throws IOException {
		this(handler, port, ssl, null);
	}

	public HttpServerConnection(HttpConnectionHandler handler, int port) throws IOException {
		this(handler, port, false, null);
	}

	/**
	 * Set the name of the thread
	 * 
	 * @param name
	 *            The name
	 */
	public void setName(String name) {
		if (iRunner != null) iRunner.setName(name);
	}

	/**
	 * Returns the port
	 * 
	 * @return The port
	 */
	public int getPort() {
		return iServerSocket.getLocalPort();
	}

	/**
	 * Returns the host ip
	 * 
	 * @return The host ip
	 * @throws UnknownHostException
	 */
	public String getHostIP() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}

	/**
	 * Returns if SSL is enanbled
	 * 
	 * @return <code>true</code> if SSL is enabled
	 */
	public boolean isSSL() {
		return iSsl;
	}

	/**
	 * Starts the server thread
	 */
	public void start() {
		if (iClose) {
			iClose = false;
			ThreadGroup group = new ThreadGroup("CIMListener on port " + String.valueOf(iPort));
			iDispatcher = new HttpConnectionDispatcher(group, iHandler, new ThreadPool(1, 16,
					group, "Handler "));
			iDispatcher.start();
			iRunner = new Thread(group, this, iServerName);
			iRunner.setDaemon(true);

			iRunner.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (!iClose) {
			try {
				Socket socket = iServerSocket.accept();
				try {
					socket.setKeepAlive(true);
					socket.setTcpNoDelay(true);
					socket.setSoTimeout(10000);
				} catch (IOException e) {
					Logger logger = SessionProperties.getGlobalProperties().getLogger();
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Exception while adjusting socket options", e);
					}
				}
				iDispatcher.dispatch(socket);
			} catch (Throwable t) {
				if (t instanceof SocketException && iClose) {
					break;
				}
				try {
					Logger logger = SessionProperties.getGlobalProperties().getLogger();
					if (logger.isLoggable(Level.SEVERE)) {
						logger.log(Level.SEVERE,
								"Exception while waiting for incoming http connections");
					}
				} catch (Throwable t2) {
					// just give up
				}
			}
		}
		
		// shutdown
		
		try {
			Logger logger = SessionProperties.getGlobalProperties().getLogger();
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Shutting down CIMListener on port "+iPort);
			}
		} catch (Throwable t) {
			// do nothing
		}
		try {
			// give the handlers a chance to process all already accepted
			// connections before complete shutdown
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// just ignore
		}
		try {
			iDispatcher.close();
		} catch (Exception e) {
			Logger logger = SessionProperties.getGlobalProperties().getLogger();
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Exception while closing http connection dispatcher", e);
			}
		}
		iDispatcher = null;
		iRunner = null;
	}

	/**
	 * Shuts down the server
	 */
	public void close() {
		if (!iClose) {
			iClose = true;
			try {
				iServerSocket.close();
				iServerSocket = null;
			} catch (Exception e) {
				Logger logger = SessionProperties.getGlobalProperties().getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Exception while closing server socket", e);
				}
			}
		}
	}

	/**
	 * Class HttpConnectionDispatcher is responsible for dispatching the
	 * incoming connections to the handlers. It doesn't execute the handler
	 * directly but creates a runnable that is submitted to a thread pool which
	 * takes care of execution.
	 * 
	 */
	private static class HttpConnectionDispatcher extends Thread {

		private List iConnectionPool = new LinkedList();

		private boolean iAlive = true;

		private HttpConnectionHandler iHandler;

		private ThreadPool iThreadPool;

		/**
		 * Ctor.
		 * 
		 * @param pGroup
		 *            The thread group to use for this thread and it's children
		 */
		public HttpConnectionDispatcher(ThreadGroup pGroup, HttpConnectionHandler pHandler,
				ThreadPool pPool) {
			super(pGroup, "Connection Dispatcher");
			setDaemon(true);
			iHandler = pHandler;
			iThreadPool = pPool;
		}

		/**
		 * Returns the connection pool
		 * 
		 * @return The connection pool
		 */
		public List getConnectionPool() {
			return iConnectionPool;
		}

		/**
		 * Dispatches a connection
		 * 
		 * @param pSocket
		 *            The socket of the conncetion
		 */
		public synchronized void dispatch(Socket pSocket) {
			iConnectionPool.add(pSocket);
			notify();
		}

		/**
		 * Gets the next pending connection
		 * 
		 * @return The socket of the conncection
		 */
		public synchronized Socket getConnection() {
			while (iConnectionPool.size() == 0) {
				try {
					wait();
				} catch (InterruptedException e) {
					// just ignore it, it is expected
				}
				if (!iAlive) return null;
			}
			Socket socket = null;
			if (iConnectionPool != null) socket = (Socket) iConnectionPool.remove(0);
			return socket;
		}

		public void run() {
			while (iAlive) {
				try {
					Socket socket = getConnection();
					if (socket != null) {
						iThreadPool.execute(new HttpServerWorker(iHandler, socket));
					}
				} catch (Throwable t) {
					try {
						Logger logger = SessionProperties.getGlobalProperties().getLogger();
						if (logger.isLoggable(Level.SEVERE)) {
							logger.log(Level.SEVERE,
									"Exception while submitting worker to thread pool", t);
						}
					} catch (Throwable t1) {
						// forget it
					}
				}
			}
			try {
				iHandler.close();
			} catch (Exception e) {
				Logger logger = SessionProperties.getGlobalProperties().getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Exception while closing http connection handler", e);
				}
			}
			try {
				iThreadPool.shutdown();
			} catch (Exception e) {
				Logger logger = SessionProperties.getGlobalProperties().getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Exception during shut down of thread pool", e);
				}
			}
		}

		public synchronized void close() {
			iAlive = false;
			notifyAll();
		}
	}
}
