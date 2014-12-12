/**
 * GlobalProperties.java
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
 * Change History
 * Flag       Date        Prog         Description
 * ------------------------------------------------------------------------------- 
 * 1498130    2006-05-31  lupusalex    Selection of xml parser on a per connection basis
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */
package org.sblim.wbem.util;

import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to configuration properties which can affect the behavior of the CIM Client,
 * including control over logs/tracing info, connection pool handling, connection timeout,
 * JSSE providers, among others.
 * This class provides static access to the global properties singleton of the SessionProperties 
 * class. It exists for backward compatiblity reasons only.
 * @deprecated Use the global properties instance offered by the SessionProperties class instead
 * @see org.sblim.wbem.util.SessionProperties#getGlobalProperties()
 */
public class GlobalProperties extends Properties {
	
	/**
	 * Uid for Serializable interface
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = -7241833060222303299L;

	private GlobalProperties() {
		super();
	}
 
	public static final int DOM_PARSER = SessionProperties.DOM_PARSER;
	public static final int SAX_PARSER = SessionProperties.SAX_PARSER;
	public static final int PULL_PARSER = SessionProperties.PULL_PARSER;
	
	/**
	 * @see SessionProperties#getBooleanProperty(String, boolean)
	 */
	public static boolean getBooleanProperty(String pKey, boolean pDefaultValue) {
		return SessionProperties.getGlobalProperties().getBooleanProperty(pKey, pDefaultValue);
	}

	/**
	 * @see SessionProperties#getConnectionPoolSize()
	 */
	public static int getConnectionPoolSize() {
		return SessionProperties.getGlobalProperties().getConnectionPoolSize();
	}

	/**
	 * @see SessionProperties#getContentLength()
	 */
	public static int getContentLength() {
		return SessionProperties.getGlobalProperties().getContentLength();
	}

	/**
	 * @see SessionProperties#getDebugOutputStream()
	 */
	public static OutputStream getDebugOutputStream() {
		return SessionProperties.getGlobalProperties().getDebugOutputStream();
	}

	/**
	 * @see SessionProperties#getDefaultCredentials()
	 */
	public static String getDefaultCredentials() {
		return SessionProperties.getGlobalProperties().getDefaultCredentials();
	}

	/**
	 * @see SessionProperties#getDefaultPrincipal()
	 */
	public static String getDefaultPrincipal() {
		return SessionProperties.getGlobalProperties().getDefaultPrincipal();
	}

	/**
	 * @see SessionProperties#getHttpTimeOut()
	 */
	public static int getHttpTimeOut() {
		return SessionProperties.getGlobalProperties().getHttpTimeOut();
	}

	/**
	 * @see SessionProperties#getIntProperty(String, int)
	 */
	public static int getIntProperty(String pKey, int pDefaultValue) {
		return SessionProperties.getGlobalProperties().getIntProperty(pKey, pDefaultValue);
	}

	/**
	 * @see SessionProperties#getJSSECertificate()
	 */
	public static String getJSSECertificate() {
		return SessionProperties.getGlobalProperties().getJSSECertificate();
	}

	/**
	 * @see SessionProperties#getJSSEProvider()
	 */
	public static String getJSSEProvider() {
		return SessionProperties.getGlobalProperties().getJSSEProvider();
	}

	/**
	 * @see SessionProperties#getKeystore()
	 */
	public static String getKeystore() {
		return SessionProperties.getGlobalProperties().getKeystore();
	}

	/**
	 * @see SessionProperties#getKeystorePassword()
	 */
	public static char[] getKeystorePassword() {
		return SessionProperties.getGlobalProperties().getKeystorePassword();
	}

	/**
	 * @see SessionProperties#getLevelProperty(String, Level)
	 */
	public static Level getLevelProperty(String pKey, Level pDefaultValue) {
		return SessionProperties.getGlobalProperties().getLevelProperty(pKey, pDefaultValue);
	}

	/**
	 * @see SessionProperties#getLogger()
	 */
	public static Logger getLogger() {
		return SessionProperties.getGlobalProperties().getLogger();
	}

	/**
	 * @see SessionProperties#getLoggerConsoleLevel()
	 */
	public static Level getLoggerConsoleLevel() {
		return SessionProperties.getGlobalProperties().getLoggerConsoleLevel();
	}

	/**
	 * @see SessionProperties#getLoggerFileLevel()
	 */
	public static Level getLoggerFileLevel() {
		return SessionProperties.getGlobalProperties().getLoggerFileLevel();
	}

	/**
	 * @see SessionProperties#getLoggerNameSpace()
	 */
	public static String getLoggerNameSpace() {
		return SessionProperties.getGlobalProperties().getLoggerNameSpace();
	}

	/**
	 * @see SessionProperties#getLoggerOutputFile()
	 */
	public static String getLoggerOutputFile() {
		return SessionProperties.getGlobalProperties().getLoggerOutputFile();
	}

	/**
	 * @see SessionProperties#getRetriesNumber()
	 */
	public static int getRetriesNumber() {
		return SessionProperties.getGlobalProperties().getRetriesNumber();
	}

	/**
	 * @see SessionProperties#getStringProperty(String, String)
	 */
	public static String getStringProperty(String pKey, String pDefaultValue) {
		return SessionProperties.getGlobalProperties().getStringProperty(pKey, pDefaultValue);
	}

	/**
	 * @see SessionProperties#getTruststore_password()
	 */
	public static String getTruststore_password() {
		return SessionProperties.getGlobalProperties().getTruststore_password();
	}

	/**
	 * @see SessionProperties#getTruststore()
	 */
	public static String getTruststore() {
		return SessionProperties.getGlobalProperties().getTruststore();
	}

	/**
	 * @see SessionProperties#getTruststorePassword()
	 */
	public static char[] getTruststorePassword() {
		return SessionProperties.getGlobalProperties().getTruststorePassword();
	}

	/**
	 * @see SessionProperties#getXmlParser()
	 */
	public static int getXmlParser() {
		return SessionProperties.getGlobalProperties().getXmlParser();
	}

	/**
	 * @see SessionProperties#isContentLengthRetryEnabled()
	 */
	public static boolean isContentLengthRetryEnabled() {
		return SessionProperties.getGlobalProperties().isContentLengthRetryEnabled();
	}

	/**
	 * @see SessionProperties#isCredentialsDefaultEnabled()
	 */
	public static boolean isCredentialsDefaultEnabled() {
		return SessionProperties.getGlobalProperties().isCredentialsDefaultEnabled();
	}

	/**
	 * @see SessionProperties#isDebugHttpConnectionPool()
	 */
	public static boolean isDebugHttpConnectionPool() {
		return SessionProperties.getGlobalProperties().isDebugHttpConnectionPool();
	}

	/**
	 * @see SessionProperties#isDebugInputStream()
	 */
	public static boolean isDebugInputStream() {
		return SessionProperties.getGlobalProperties().isDebugInputStream();
	}

	/**
	 * @see SessionProperties#isDebugXMLInput()
	 */
	public static boolean isDebugXMLInput() {
		return SessionProperties.getGlobalProperties().isDebugXMLInput();
	}

	/**
	 * @see SessionProperties#isDebugXMLOutput()
	 */
	public static boolean isDebugXMLOutput() {
		return SessionProperties.getGlobalProperties().isDebugXMLOutput();
	}

	/**
	 * @see SessionProperties#isLoggingEnabled()
	 */
	public static boolean isLoggingEnabled() {
		return SessionProperties.getGlobalProperties().isLoggingEnabled();
	}

	/**
	 * @see SessionProperties#loadProperties()
	 */
	public static boolean loadProperties() {
		return SessionProperties.getGlobalProperties().loadProperties();
	}

	/**
	 * @see SessionProperties#loadProperties(Properties)
	 */
	public static boolean loadProperties(Properties properties) {
		return SessionProperties.getGlobalProperties().loadProperties(properties);
	}

	/**
	 * @see SessionProperties#loadProperties(String)
	 */
	public static boolean loadProperties(String propertyFile) {
		return SessionProperties.getGlobalProperties().loadProperties(propertyFile);
	}

	/**
	 * @see SessionProperties#setConnectionPoolSize(int)
	 */
	public static void setConnectionPoolSize(int poolSize) {
		SessionProperties.getGlobalProperties().setConnectionPoolSize(poolSize);
	}

	/**
	 * @see SessionProperties#setContentLength(int)
	 */
	public static void setContentLength(int pContentLength) {
		SessionProperties.getGlobalProperties().setContentLength(pContentLength);
	}

	/**
	 * @see SessionProperties#setContentLengthRetry(boolean)
	 */
	public static void setContentLengthRetry(boolean pEnableContentCheck) {
		SessionProperties.getGlobalProperties().setContentLengthRetry(pEnableContentCheck);
	}

	/**
	 * @see SessionProperties#setCredentialsDefaultEnabled(boolean)
	 */
	public static void setCredentialsDefaultEnabled(boolean pCredentialsDefaultEnabled) {
		SessionProperties.getGlobalProperties().setCredentialsDefaultEnabled(pCredentialsDefaultEnabled);
	}

	/**
	 * @see SessionProperties#setDebugHttpConnectionPool(boolean)
	 */
	public static void setDebugHttpConnectionPool(boolean pEnableDebugHTTPPool) {
		SessionProperties.getGlobalProperties().setDebugHttpConnectionPool(pEnableDebugHTTPPool);
	}

	/**
	 * @see SessionProperties#setDebugInputStream(boolean)
	 */
	public static void setDebugInputStream(boolean pEnableDebugInputStream) {
		SessionProperties.getGlobalProperties().setDebugInputStream(pEnableDebugInputStream);
	}

	/**
	 * @see SessionProperties#setDebugOutputStream(OutputStream)
	 */
	public static void setDebugOutputStream(OutputStream pOutputStream) {
		SessionProperties.getGlobalProperties().setDebugOutputStream(pOutputStream);
	}

	/**
	 * @see SessionProperties#setDebugXMLInput(boolean)
	 */
	public static void setDebugXMLInput(boolean pEnableDebugXMLInput) {
		SessionProperties.getGlobalProperties().setDebugXMLInput(pEnableDebugXMLInput);
	}

	/**
	 * @see SessionProperties#setDebugXMLOutput(boolean)
	 */
	public static void setDebugXMLOutput(boolean pEnableDebugXMLOutput) {
		SessionProperties.getGlobalProperties().setDebugXMLOutput(pEnableDebugXMLOutput);
	}

	/**
	 * @see SessionProperties#setDefaultCredentials(String)
	 */
	public static void setDefaultCredentials(String pCredentialsPassword) {
		SessionProperties.getGlobalProperties().setDefaultCredentials(pCredentialsPassword);
	}

	/**
	 * @see SessionProperties#setDefaultPrincipal(String)
	 */
	public static void setDefaultPrincipal(String pCredentialsUser) {
		SessionProperties.getGlobalProperties().setDefaultPrincipal(pCredentialsUser);
	}

	/**
	 * @see SessionProperties#setHttpTimeOut(int)
	 */
	public static void setHttpTimeOut(int pTimeOut) {
		SessionProperties.getGlobalProperties().setHttpTimeOut(pTimeOut);
	}

	/**
	 * @see SessionProperties#setJSSECertificate(String)
	 */
	public static void setJSSECertificate(String pJsseCertificateMgr) {
		SessionProperties.getGlobalProperties().setJSSECertificate(pJsseCertificateMgr);
	}

	/**
	 * @see SessionProperties#setJSSEProvider(String)
	 */
	public static void setJSSEProvider(String providerClassName) {
		SessionProperties.getGlobalProperties().setJSSEProvider(providerClassName);
	}

	/**
	 * @see SessionProperties#setKeystore_password(char[])
	 */
	public static void setKeystore_password(char[] pNewPassword) {
		SessionProperties.getGlobalProperties().setKeystore_password(pNewPassword);
	}

	/**
	 * @see SessionProperties#setKeystore(String)
	 */
	public static void setKeystore(String pKeystore) {
		SessionProperties.getGlobalProperties().setKeystore(pKeystore);
	}

	/**
	 * @see SessionProperties#setLogger(Logger)
	 */
	public static void setLogger(Logger pNewLogger) {
		SessionProperties.getGlobalProperties().setLogger(pNewLogger);
	}

	/**
	 * @see SessionProperties#setLoggerConsoleLevel(Level)
	 */
	public static void setLoggerConsoleLevel(Level pLoggerConsoleLevel) {
		SessionProperties.getGlobalProperties().setLoggerConsoleLevel(pLoggerConsoleLevel);
	}

	/**
	 * @see SessionProperties#setLoggerFileLevel(Level)
	 */
	public static void setLoggerFileLevel(Level pLoggerFileLevel) {
		SessionProperties.getGlobalProperties().setLoggerFileLevel(pLoggerFileLevel);
	}

	/**
	 * @see SessionProperties#setLoggerNameSpace(String)
	 */
	public static void setLoggerNameSpace(String pLoggerNameSpace) {
		SessionProperties.getGlobalProperties().setLoggerNameSpace(pLoggerNameSpace);
	}

	/**
	 * @see SessionProperties#setLoggerOutputFile(String)
	 */
	public static void setLoggerOutputFile(String pLoggerOutputFile) {
		SessionProperties.getGlobalProperties().setLoggerOutputFile(pLoggerOutputFile);
	}

	/**
	 * @see SessionProperties#setLoggingEnabled(boolean)
	 */
	public static void setLoggingEnabled(boolean pEnableLogging) {
		SessionProperties.getGlobalProperties().setLoggingEnabled(pEnableLogging);
	}

	/**
	 * @see SessionProperties#setRetriesNumber(int)
	 */
	public static void setRetriesNumber(int pRetries) {
		SessionProperties.getGlobalProperties().setRetriesNumber(pRetries);
	}

	/**
	 * @see SessionProperties#setTruststore_password(char[])
	 */
	public static void setTruststore_password(char[] pNewPassword) {
		SessionProperties.getGlobalProperties().setTruststore_password(pNewPassword);
	}

	/**
	 * @see SessionProperties#setTruststore_password(String)
	 */
	public static void setTruststore_password(String pNewPassword) {
		SessionProperties.getGlobalProperties().setTruststore_password(pNewPassword);
	}

	/**
	 * @see SessionProperties#setTruststore(String)
	 */
	public static void setTruststore(String pTruststore) {
		SessionProperties.getGlobalProperties().setTruststore(pTruststore);
	}

	/**
	 * @see SessionProperties#setXmlParser(int)
	 */
	public static void setXmlParser(int parsingMode) {
		SessionProperties.getGlobalProperties().setXmlParser(parsingMode);
	}

	public static void main(String[] args) {
		GlobalProperties.loadProperties(".//cim.defaults");
		char[] pass = GlobalProperties.getTruststorePassword();
		System.out.println("Password:"+((pass != null)?new String(pass):"null"));
		GlobalProperties.setTruststore_password("password".toCharArray());
		pass = GlobalProperties.getTruststorePassword();
		System.out.println("Password:"+((pass != null)?new String(pass):"null"));
		GlobalProperties.setTruststore_password((String)null);
		pass = GlobalProperties.getTruststorePassword();
		System.out.println("Password:"+((pass != null)?new String(pass):"null"));
		GlobalProperties.setTruststore_password((char[])null);
		pass = GlobalProperties.getTruststorePassword();
		System.out.println("Password:"+((pass != null)?new String(pass):"null"));
	}
}
