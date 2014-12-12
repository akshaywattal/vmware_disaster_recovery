/**
 * HttpConnectionHandler.java
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
 *   17931    2005-07-28  thschaef     Add InetAddress to CIM Event
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1649779    2007-02-01  lupusalex    Indication listener threads freeze
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */
package org.sblim.wbem.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.http.io.ASCIIPrintStream;
import org.sblim.wbem.util.SessionProperties;

public class HttpConnectionHandler {

	public static final int MAJOR_VERSION = 1;

	public static final int MINOR_VERSION = 1;

	HttpContentHandler iHandler;

	public HttpConnectionHandler(HttpContentHandler handler) {
		this.iHandler = handler;
	}

	public void handleConnection(Socket socket) {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
			do { // handle persistent connections
				MessageReader reader = new MessageReader(is);
				boolean persistent = reader.isPersistentConnectionSupported();
				persistent = false;
				boolean chunk = reader.isChunkSupported();

				HttpServerMethod readerMethod = reader.getMethod();
				if (readerMethod.getMethodName().equals("POST")
						|| readerMethod.getMethodName().equals("M-POST")) {
					// TODO: validate authorization

					MessageWriter writer = new MessageWriter(os, persistent, chunk);
					try {
						// 17931
						iHandler.handleContent(reader, writer, socket.getInetAddress());
						writer.setMethod(new HttpServerMethod(readerMethod.getMajorVersion(),
								readerMethod.getMinorVersion(), 200, "OK"));
					} catch (HttpException e) {
						writer.setMethod(new HttpServerMethod(readerMethod.getMajorVersion(),
								readerMethod.getMinorVersion(), e.getStatus(), e.getMessage()));
					} catch (Throwable t) {
						writer.setMethod(new HttpServerMethod(readerMethod.getMajorVersion(),
								readerMethod.getMinorVersion(), 501, "Not Implemented")); // TODO:
																							// define
																							// the
																							// correct
																							// error
																							// description
						writer.reset();
						// TODO: report a specific error
						writeError(writer.getOutputStream(), "error", "error");
					} finally {
						try {
							writer.close();
						} catch (IOException e) {
							Logger logger = SessionProperties.getGlobalProperties().getLogger();
							if (logger.isLoggable(Level.WARNING)) {
								logger.log(Level.WARNING,
										"exception while closing output stream from socket", e);
							}
						}
					}
				} else {
					persistent = false;
					MessageWriter writer = new MessageWriter(os, false, false);
					HttpHeader header = new HttpHeader();
					writer.setHeader(header);
					// header.addField("Connection", persistent?
					// "Keep-Alive","close");
					writer.setMethod(new HttpServerMethod(readerMethod.getMajorVersion(),
							readerMethod.getMinorVersion(), 501, "Not Implemented"));
					writeError(writer.getOutputStream(), "", "");
					try {
						writer.close();
					} catch (IOException e) {
						Logger logger = SessionProperties.getGlobalProperties().getLogger();
						if (logger.isLoggable(Level.WARNING)) {
							logger.log(Level.WARNING,
									"exception while closing output stream from socket", e);
						}
					}
				}

				if (!persistent) break;
				try {
					reader.close();
				} catch (IOException e) {
					// nothing has to be done here
					Logger logger = SessionProperties.getGlobalProperties().getLogger();
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING,
								"exception while closing input stream from socket", e);
					}
				}
			} while (true);
		} catch (IOException e) {
			// error while reading from the socket!!
			// Maybe the connection was closed by the client
			// Logger logger = GlobalProperties.getLogger();
			// if (logger.isLoggable(Level.WARNING)) {
			// logger.log(Level.WARNING, "error while reading from", e);
			// }
		}
		try {
			socket.close();
		} catch (IOException e) {
			// nothing has to be done here
			Logger logger = SessionProperties.getGlobalProperties().getLogger();
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "exception while closing socket", e);
			}
		}
	}

	public void close() {
		iHandler.close();
	}

	private void writeError(ASCIIPrintStream dos, String title, String body) {
		dos.print("<HTTP> <HEAD> <TITLE>" + title + "</TITLE></HEAD><BODY>" + body
				+ "</BODY></HTML>");

	}
}
