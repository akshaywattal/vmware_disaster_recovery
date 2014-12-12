/**
 * SessionProperties.java
 *
 * (C) Copyright IBM Corp. 2006, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author: Alexander Wolf-Reber, IBM, a.wolf-reber@de.ibm.com 
 * 
 * Change History
 * Flag       Date        Prog         Description
 * ------------------------------------------------------------------------------- 
 * 1498130    2006-05-31  lupusalex    Selection of xml parser on a per connection basis
 * 1516244    2006-07-10  ebak         GCJ support
 * 1498130    2006-07-17  lupusalex    review issues
 * 1558663    2006-09-14  lupusalex    Support custom socket factories in client connections
 * 1535793    2006-09-19  lupusalex    Fix&Integrate CIM&SLP configuration classes
 * 1573723    2006-10-09  lupusalex    Selection of JSSE provider via properties file not feasible
 * 1573723    2006-10-16  lupusalex    rework: Selection of JSSE provider via properties file not feasible
 * 1573723    2006-10-26  lupusalex    rework: Selection of JSSE provider via properties file not feasible
 * 1573723    2006-10-30  lupusalex    rework: Selection of JSSE provider via properties file not feasible
 * 1516242    2006-11-27  lupusalex    Support of OpenPegasus local authentication
 * 1815752    2007-10-18  ebak         TLS support
 * 1815752    2007-10-31  ebak         rework: TLS support
 * 1835847    2007-11-21  ebak         property for configuration file location
 * 2372679    2008-12-01  blaschke-oss Add property to control synchronized SSL handshaking
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */
package org.sblim.wbem.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.SocketFactory;

import org.sblim.wbem.http.PegasusLocalAuthInfo;
import org.sblim.wbem.http.WwwAuthInfo;

/**
 * Provides access to configuration properties which can affect the behavior of
 * the CIM Client, including control over logs/tracing info, connection pool
 * handling, connection timeout, JSSE providers, among others.
 * 
 */
public class SessionProperties extends Properties implements Cloneable {

	/**
	 * Uid for the Serializable interface
	 * 
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = -5658419976348651048L;

	/**
	 * Parsing mode 'pull parser'.
	 */
	public static final int PULL_PARSER = 0;

	/**
	 * Parsing moder 'SAX parser'.
	 */
	public static final int SAX_PARSER = 1;

	/**
	 * Parsing mode 'DOM parser'.
	 */
	public static final int DOM_PARSER = 2;

	/**
	 * Standard HTTP basic or digest authentication
	 */
	public static final String WWW_AUTHENTICATION = WwwAuthInfo.class.getName();

	/**
	 * OpenPegasus local authentication
	 */
	public static final String PEGASUS_LOCAL_AUTHENTICATION = PegasusLocalAuthInfo.class.getName();

	private static final String KEY_AUTHENTICATION = "http.authentication"; //$NON-NLS-1$	

	private static final String KEY_HTTP_TIME_OUT = "http.timeout"; //$NON-NLS-1$

	private static final String KEY_HTTP_CONNECTION_POOL_SIZE = "http.pool.size"; //$NON-NLS-1$

	private static final String KEY_TRUSTSTORE = "https.truststore.path"; //$NON-NLS-1$

	private static final String KEY_TRUSTSTORE_PASSWORD = "https.truststore.password"; //$NON-NLS-1$

	private static final String KEY_KEYSTORE = "https.keystore.path"; //$NON-NLS-1$

	private static final String KEY_KEYSTORE_TYPE = "https.keystore.type"; //$NON-NLS-1$

	private static final String KEY_KEYSTORE_PASSWORD = "https.keystore.password"; //$NON-NLS-1$

	private static final String KEY_JSSE_PROVIDER = "https.jsse.provider"; //$NON-NLS-1$
	
	private static final String KEY_JSSE_PROTOCOL_HANDLER	= "https.protocol.handler";
	
	private static final String KEY_JSSE_PROTOCOL = "https.protocol";

	private static final String KEY_JSSE_CERTIFICATE = "https.jsse.certificate"; //$NON-NLS-1$

	private static final String KEY_XML_PARSER = "xml.parser"; //$NON-NLS-1$

	private static final String KEY_DEBUG_HTTP_POOL = "debug.http.pool"; //$NON-NLS-1$

	private static final String KEY_DEBUG_ISTREAM = "debug.istream"; //$NON-NLS-1$

	private static final String KEY_DEBUG_ISTREAM_OUTPUT = "debug.istream.output"; //$NON-NLS-1$

	private static final String KEY_DEBUG_XML_INPUT = "debug.xml.input"; //$NON-NLS-1$

	private static final String KEY_DEBUG_XML_OUTPUT = "debug.xml.output"; //$NON-NLS-1$

	private static final String KEY_LOGGER = "logger"; //$NON-NLS-1$

	private static final String KEY_LOGGER_NAMESPACE = "logger.namespace"; //$NON-NLS-1$

	private static final String KEY_LOGGER_OUTPUT = "log.output"; //$NON-NLS-1$

	private static final String KEY_LOGGER_CONSOLE_LEVEL = "log.console.level"; //$NON-NLS-1$

	private static final String KEY_LOGGER_FILE_LEVEL = "log.file.level"; //$NON-NLS-1$

	private static final String KEY_CREDENTIALS_DEFAULT_ENABLED = "default.authorization.enabled"; //$NON-NLS-1$

	private static final String KEY_DEFAULT_PRINCIPAL = "default.principal"; //$NON-NLS-1$

	private static final String KEY_DEFAULT_CREDENTIAL = "default.credential"; //$NON-NLS-1$

	private static final String KEY_RETRIES_NUMBER = "retries.number"; //$NON-NLS-1$

	private static final String KEY_RETRIES_CONTENT_ENABLE = "retries.content.enable"; //$NON-NLS-1$

	private static final String KEY_RETRIES_CONTENT_LENGTH = "retries.content.lenght"; //$NON-NLS-1$
	
	private static final String KEY_CONFIGFILE = "sblim.wbem.configFile";
	
	private static final String KEY_SYNCHRONIZED_SSL_HANDSHAKE = "synchronized.ssl.handshake"; //$NON-NLS-1$

	private static final String FILE_CIMCLIENT_PROPERTIES = "cimclient.properties"; //$NON-NLS-1$

	private static final String FILE_DEFAULT_PROPERTIES = "cim.defaults"; //$NON-NLS-1$

	private static final String LINUX_SBLIM_CIM_CLIENT_PROPERTIES = "/etc/java/sblim-cim-client.properties"; //$NON-NLS-1$

	private static final int DEFAULT_HTTP_TIME_OUT = 0;

	private static final int DEFAULT_HTTP_CONNECTION_POOL_SIZE = 16;

	private static final String DEFAULT_TRUSTSTORE = "truststore"; //$NON-NLS-1$

	private static final char[] DEFAULT_TRUSTSTORE_PASSWORD = null;

	private static final String DEFAULT_KEYSTORE = "keystore"; //$NON-NLS-1$

	private static final String DEFAULT_KEYSTORE_TYPE = "JKS"; //$NON-NLS-1$

	private static final char[] DEFAULT_KEYSTORE_PASSWORD = null;

	private static final String DEFAULT_JSSE_PROVIDER = "com.ibm.jsse.IBMJSSEProvider"; //$NON-NLS-1$
	
	private static final String DEFAULT_JSSE_PROTOCOL = "SSL";

	private static final String DEFAULT_JSSE_CERTIFICATE = "IbmX509"; //$NON-NLS-1$

	private static final int DEFAULT_XML_PARSER = PULL_PARSER;

	private static final boolean DEFAULT_DEBUG_HTTP_POOL = false;

	private static final boolean DEFAULT_DEBUG_ISTREAM = false;

	private static final String DEFAULT_DEBUG_ISTREAM_OUTPUT = "stdout"; //$NON-NLS-1$

	private static final boolean DEFAULT_DEBUG_XML_INPUT = false;

	private static final boolean DEFAULT_DEBUG_XML_OUTPUT = false;

	private static final boolean DEFAULT_LOGGER = false;

	private static final String DEFAULT_LOGGER_NAMESPACE = "org.sblim.wbem.cimclient"; //$NON-NLS-1$

	private static final String DEFAULT_LOGGER_OUTPUT = "cimclient.log"; //$NON-NLS-1$

	private static final Level DEFAULT_LOGGER_CONSOLE_LEVEL = Level.WARNING;

	private static final Level DEFAULT_LOGGER_FILE_LEVEL = Level.WARNING;

	private static final boolean DEFAULT_CREDENTIALS_DEFAULT_ENABLED = false;

	private static final String DEFAULT_PRINCIPAL_VALUE = "default"; //$NON-NLS-1$

	private static final String DEFAULT_CREDENTIALS_VALUE = "default"; //$NON-NLS-1$

	private static final int DEFAULT_RETRIES_NUMBER = 1;

	private static final boolean DEFAULT_RETRIES_CONTENT_ENABLE = false;

	private static final int DEFAULT_RETRIES_CONTENT_LENGTH = 50;
	
	private static final String DEFAULT_AUTHENTICATION = WWW_AUTHENTICATION;
	
	private static final boolean DEFAULT_SYNCHRONIZED_SSL_HANDSHAKE = true;

	private int value_httpTimeOut;

	private int value_connectionPoolSize;

	private String value_truststore;

	private char[] value_truststore_password;

	private String value_keystore;

	private String value_keystore_type;

	private char[] value_keystore_password;

	private String value_jsseProvider;
	
	private String value_jsseProtocol;
	
	private String value_jsseProtocolHandler;

	private String value_jsseCertificate;

	private int value_xmlParser;

	private boolean value_debugHttpPool;

	private boolean value_debugInputStream;

	private OutputStream value_debugInputStreamOutput;

	private boolean value_debugXMLInput;

	private boolean value_debugXMLOutput;

	private boolean value_credentialsDefaultEnabled;

	private String value_defaultPrincipal;

	private String value_defaultCredentials;

	private int value_retries_number;

	private boolean value_retries_content_enable;

	private int value_retries_content_length;
	
	private String value_http_authentication;
	
	private boolean value_synchronized_ssl_handshake;

	private boolean isGlobal = false;

	private String propertyFile = null;

	private SocketFactory value_socketFactory = null;

	private static boolean value_logger_enabled;

	private static String value_loggerNameSpace;

	private static String value_loggerOutputFile;

	private static Level value_loggerFileLevel;

	private static Level value_loggerConsoleLevel;

	private static Logger logger;

	private static FileHandler fileHandler;

	private static ConsoleHandler consoleHandler;

	private static SessionProperties globalProperties;

	private static final HashMap defaultPropertyMap = new HashMap();

	private static void setDefault(String pKey, Object pValue) {
		defaultPropertyMap.put(pKey, pValue.toString());
	}

	private static void setDefault(String pKey, int pValue) {
		defaultPropertyMap.put(pKey, String.valueOf(pValue));
	}

	private static void setDefault(String pKey, boolean pValue) {
		defaultPropertyMap.put(pKey, String.valueOf(pValue));
	}

	static {

		setDefault(KEY_HTTP_TIME_OUT, DEFAULT_HTTP_TIME_OUT);
		setDefault(KEY_HTTP_CONNECTION_POOL_SIZE, DEFAULT_HTTP_CONNECTION_POOL_SIZE);
		setDefault(KEY_TRUSTSTORE, DEFAULT_TRUSTSTORE);
		setDefault(KEY_TRUSTSTORE_PASSWORD,
				DEFAULT_TRUSTSTORE_PASSWORD != null ? DEFAULT_TRUSTSTORE_PASSWORD.toString() : "");
		setDefault(KEY_KEYSTORE, DEFAULT_KEYSTORE);
		setDefault(KEY_KEYSTORE_TYPE, DEFAULT_KEYSTORE_TYPE);
		setDefault(KEY_KEYSTORE_PASSWORD,
				DEFAULT_KEYSTORE_PASSWORD != null ? DEFAULT_KEYSTORE_PASSWORD.toString() : "");
		setDefault(KEY_JSSE_PROVIDER, detectDefaultJsseProvider());
		setDefault(KEY_JSSE_CERTIFICATE, detectDefaultJsseCertificate());
		setDefault(KEY_JSSE_PROTOCOL, DEFAULT_JSSE_PROTOCOL);
		setDefault(KEY_XML_PARSER, DEFAULT_XML_PARSER);
		setDefault(KEY_DEBUG_HTTP_POOL, DEFAULT_DEBUG_HTTP_POOL);
		setDefault(KEY_DEBUG_ISTREAM, DEFAULT_DEBUG_ISTREAM);
		setDefault(KEY_DEBUG_ISTREAM_OUTPUT, DEFAULT_DEBUG_ISTREAM_OUTPUT);
		setDefault(KEY_DEBUG_XML_INPUT, DEFAULT_DEBUG_XML_INPUT);
		setDefault(KEY_DEBUG_XML_OUTPUT, DEFAULT_DEBUG_XML_OUTPUT);
		setDefault(KEY_LOGGER, DEFAULT_LOGGER);
		setDefault(KEY_LOGGER_NAMESPACE, DEFAULT_LOGGER_NAMESPACE);
		setDefault(KEY_LOGGER_OUTPUT, DEFAULT_LOGGER_OUTPUT);
		setDefault(KEY_LOGGER_CONSOLE_LEVEL, DEFAULT_LOGGER_CONSOLE_LEVEL.toString());
		setDefault(KEY_LOGGER_FILE_LEVEL, DEFAULT_LOGGER_FILE_LEVEL.toString());
		setDefault(KEY_CREDENTIALS_DEFAULT_ENABLED, DEFAULT_CREDENTIALS_DEFAULT_ENABLED);
		setDefault(KEY_DEFAULT_PRINCIPAL, DEFAULT_PRINCIPAL_VALUE);
		setDefault(KEY_DEFAULT_CREDENTIAL, DEFAULT_CREDENTIALS_VALUE);
		setDefault(KEY_RETRIES_NUMBER, DEFAULT_RETRIES_NUMBER);
		setDefault(KEY_RETRIES_CONTENT_ENABLE, DEFAULT_RETRIES_CONTENT_ENABLE);
		setDefault(KEY_RETRIES_CONTENT_LENGTH, DEFAULT_RETRIES_CONTENT_LENGTH);
		setDefault(KEY_AUTHENTICATION, DEFAULT_AUTHENTICATION);
		setDefault(KEY_SYNCHRONIZED_SSL_HANDSHAKE, DEFAULT_SYNCHRONIZED_SSL_HANDSHAKE);
		globalProperties = getEnvironmentDefaults();
		globalProperties.setGlobal(true);

	}

	/**
	 * Detects the JSSE provider with the highest priority currently installed
	 * in this JVM.
	 * 
	 * @return The class name of a java.security.Provider serving
	 *         "SSLContext.SSL" or SessionProperties.DEFAULT_JSSE_PROVIDER if
	 *         none was reported by the JVM.
	 */
	private static String detectDefaultJsseProvider() {
		Provider[] providers = Security.getProviders("SSLContext.SSL");
		if (providers.length > 0) { return providers[0].getClass().getName(); }
		return DEFAULT_JSSE_PROVIDER;
	}
	
	/**
	 * Detects the X509 certificate algorithm installed in this JVM.
	 * 
	 * @return The X509 certificate name or
	 *         SessionProperties.DEFAULT_JSSE_CERTIFICATE if none is reported by
	 *         the JVM.
	 */
	private static String detectDefaultJsseCertificate() {
		String certificate = Security.getProperty("ssl.KeyManagerFactory.algorithm");
		return certificate != null ? certificate : DEFAULT_JSSE_CERTIFICATE;
	}

	/**
	 * Returns the SessionProperties singleton instance representing the global
	 * properties. The global properties are applied whenever no session
	 * specific properties are specified. The global properties are identical to
	 * the environment defaults after startup, but may be modified by the
	 * application during runtime.
	 * 
	 * @return The global properties
	 */
	public static SessionProperties getGlobalProperties() {
		return globalProperties;
	}

	/**
	 * Returns the library default properties. These are the session properties
	 * that would be used if no property files was loaded and no modifications
	 * done by the application.
	 * 
	 * @return The library defaults
	 */
	public static SessionProperties getLibraryDefaults() {
		SessionProperties result = new SessionProperties();
		return result;
	}

	/**
	 * Returns the environment defaults. These are the session properties that
	 * are used after startup with no modifications done by the application. The
	 * properties file is applied.
	 * 
	 * @return The environment properties.
	 * @see #loadProperties()
	 */
	public static SessionProperties getEnvironmentDefaults() {
		SessionProperties result = new SessionProperties();
		result.loadProperties();
		return result;
	}

	/**
	 * Constructs a new SessionProperties instance with library defaults
	 * applied.
	 * 
	 * @see #getLibraryDefaults()
	 */
	public SessionProperties() {
		defaults = new Properties();
		defaults.putAll(defaultPropertyMap);
	}

	/**
	 * Sets the properties to the corresponding values.
	 * 
	 * This method is used to get the properties which have been loaded before
	 * read and set to corresponding values. These values can be accessed
	 * afterwards by their getter and setter methods.
	 * 
	 */
	private void setProperties() {

		value_httpTimeOut = getIntProperty(KEY_HTTP_TIME_OUT, DEFAULT_HTTP_TIME_OUT);
		if (value_httpTimeOut < 0) {
			if (logger != null && logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "The value for the key " + KEY_HTTP_TIME_OUT
						+ " is outside of the range. Using its default value.");
			} else {
				System.err.println("The value for the key " + KEY_HTTP_TIME_OUT
						+ " is outside of the range. Using its default value.");
			}
			value_httpTimeOut = DEFAULT_HTTP_TIME_OUT;
		}

		value_connectionPoolSize = getIntProperty(KEY_HTTP_CONNECTION_POOL_SIZE,
				DEFAULT_HTTP_CONNECTION_POOL_SIZE);
		if (value_connectionPoolSize < (-1)) {
			if (logger != null && logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "The value for the key " + KEY_HTTP_CONNECTION_POOL_SIZE
						+ " is outside of the range. Using its default value.");
			} else {
				System.err.println("The value for the key " + KEY_HTTP_CONNECTION_POOL_SIZE
						+ " is outside of the range. Using its default value.");
			}
			value_connectionPoolSize = DEFAULT_HTTP_CONNECTION_POOL_SIZE;
		}

		value_truststore = getStringProperty(KEY_TRUSTSTORE, DEFAULT_TRUSTSTORE);
		String truststorePass = getStringProperty(KEY_TRUSTSTORE_PASSWORD,
				DEFAULT_TRUSTSTORE_PASSWORD != null ? DEFAULT_TRUSTSTORE_PASSWORD.toString() : null);
		if (truststorePass != null) value_truststore_password = truststorePass.toCharArray();

		value_keystore = getStringProperty(KEY_KEYSTORE, DEFAULT_KEYSTORE);
		value_keystore_type = getStringProperty(KEY_KEYSTORE_TYPE, DEFAULT_KEYSTORE_TYPE);
		
		String keystorePass = getStringProperty(KEY_KEYSTORE_PASSWORD,
				DEFAULT_KEYSTORE_PASSWORD != null ? DEFAULT_KEYSTORE_PASSWORD.toString() : null);
		if (keystorePass != null) value_keystore_password = keystorePass.toCharArray();

		value_jsseProvider = getStringProperty(KEY_JSSE_PROVIDER, detectDefaultJsseProvider());
		value_jsseProtocol = getStringProperty(KEY_JSSE_PROTOCOL, DEFAULT_JSSE_PROTOCOL);
		value_jsseProtocolHandler = getStringProperty(KEY_JSSE_PROTOCOL_HANDLER, null);
		value_jsseCertificate = getStringProperty(KEY_JSSE_CERTIFICATE, detectDefaultJsseCertificate());

		value_xmlParser = getIntProperty(KEY_XML_PARSER, DEFAULT_XML_PARSER);
		if (value_xmlParser < 0 || value_xmlParser > 2) {
			if (logger != null && logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "The value for the key " + KEY_XML_PARSER
						+ " is outside of the range. Using its default value.");
			} else {
				System.err.println("The value for the key " + KEY_XML_PARSER
						+ " is outside of the range. Using its default value.");
			}
			value_xmlParser = DEFAULT_XML_PARSER;
		}

		value_debugHttpPool = getBooleanProperty(KEY_DEBUG_HTTP_POOL, DEFAULT_DEBUG_HTTP_POOL);
		value_debugInputStream = getBooleanProperty(KEY_DEBUG_ISTREAM, DEFAULT_DEBUG_ISTREAM);

		String dbgIStreamOutputStream = getStringProperty(KEY_DEBUG_ISTREAM_OUTPUT,
				DEFAULT_DEBUG_ISTREAM_OUTPUT);
		if ("stdout".equalsIgnoreCase(dbgIStreamOutputStream)) //$NON-NLS-1$
		value_debugInputStreamOutput = System.out;
		else if ("stderr".equalsIgnoreCase(dbgIStreamOutputStream)) //$NON-NLS-1$
		value_debugInputStreamOutput = System.err;
		else {
			try {
				value_debugInputStreamOutput = new FileOutputStream(
						new File(dbgIStreamOutputStream));
			} catch (IOException e) {
				value_debugInputStreamOutput = System.out;
			}
		}
		value_debugXMLInput = getBooleanProperty(KEY_DEBUG_XML_INPUT, DEFAULT_DEBUG_XML_INPUT);
		value_debugXMLOutput = getBooleanProperty(KEY_DEBUG_XML_OUTPUT, DEFAULT_DEBUG_XML_OUTPUT);

		value_credentialsDefaultEnabled = getBooleanProperty(KEY_CREDENTIALS_DEFAULT_ENABLED,
				DEFAULT_CREDENTIALS_DEFAULT_ENABLED);
		value_defaultPrincipal = getStringProperty(KEY_DEFAULT_PRINCIPAL, DEFAULT_PRINCIPAL_VALUE);
		value_defaultCredentials = getStringProperty(KEY_DEFAULT_CREDENTIAL,
				DEFAULT_CREDENTIALS_VALUE);

		value_retries_number = getIntProperty(KEY_RETRIES_NUMBER, DEFAULT_RETRIES_NUMBER);
		value_retries_content_enable = getBooleanProperty(KEY_RETRIES_CONTENT_ENABLE,
				DEFAULT_RETRIES_CONTENT_ENABLE);
		value_retries_content_length = getIntProperty(KEY_RETRIES_CONTENT_LENGTH,
				DEFAULT_RETRIES_CONTENT_LENGTH);
		
		value_http_authentication = getStringProperty(KEY_AUTHENTICATION, DEFAULT_AUTHENTICATION);
		value_synchronized_ssl_handshake = getBooleanProperty(KEY_SYNCHRONIZED_SSL_HANDSHAKE, 
				DEFAULT_SYNCHRONIZED_SSL_HANDSHAKE);
	}

	/**
	 * Sets the properties that are specific to the global properties instance.
	 * Also sets up the logging framework.
	 */
	private void setGlobalProperties() {
		value_logger_enabled = getBooleanProperty(KEY_LOGGER, DEFAULT_LOGGER);
		value_loggerNameSpace = getStringProperty(KEY_LOGGER_NAMESPACE, DEFAULT_LOGGER_NAMESPACE);
		value_loggerOutputFile = getStringProperty(KEY_LOGGER_OUTPUT, DEFAULT_LOGGER_OUTPUT);
		value_loggerConsoleLevel = getLevelProperty(KEY_LOGGER_CONSOLE_LEVEL,
				DEFAULT_LOGGER_CONSOLE_LEVEL);
		value_loggerFileLevel = getLevelProperty(KEY_LOGGER_FILE_LEVEL, DEFAULT_LOGGER_FILE_LEVEL);

		try {
			logger = Logger.getLogger(getLoggerNameSpace());
			setUpLoggingFramework();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Enumeration properties = super.propertyNames();
		while (properties.hasMoreElements()) {
			String name = (String) properties.nextElement();
			if (name.startsWith("net.slp.")) {
				System.setProperty(name, super.getProperty(name));
			}
		}
	}

	/**
	 * This method initializes the logging framework.
	 * 
	 * The logger will be equiped with the two handlers (Console and File) if
	 * the logging is enabled and the levels for the handlers are set properly.
	 */
	private void setUpLoggingFramework() {
		logger.setUseParentHandlers(false);

		if (isLoggingEnabled()) {
			if (consoleHandler == null) {
				consoleHandler = new ConsoleHandler();
				logger.addHandler(consoleHandler);
			}
			consoleHandler.setLevel(getLoggerConsoleLevel());

			logger.setLevel(Level.ALL);

			if (!value_loggerFileLevel.equals(Level.OFF)) {
				try {
					if (fileHandler == null) {
						fileHandler = new FileHandler(getLoggerOutputFile());
						logger.addHandler(fileHandler);
					}
					fileHandler.setLevel(getLoggerFileLevel());

				} catch (Exception e) {
					logger.log(Level.INFO, "Logging into file " + value_loggerOutputFile
							+ "could not be exhausted.");
				}
			}
		} else {
			Handler[] handlers = logger.getHandlers();
			for (int i = 0; i < handlers.length; i++) {
				handlers[i].setLevel(Level.OFF);
			}

			logger.setLevel(Level.OFF);
		}
	}

	/**
	 * Gets the string represetation of property.
	 * 
	 * @param key
	 *            a String representing the property name.
	 * @param defaultValue
	 *            default value used in case of an error ocurrs while reading
	 *            the value.
	 * @return The <code>String</code> value for the related key, otherwise
	 *         the submitted defaultValue.
	 */
	public String getStringProperty(String key, String defaultValue) {

		return getProperty(key, defaultValue);
	}

	/**
	 * Gets the integer represetation of property.
	 * 
	 * @param key
	 *            a String representing the property name.
	 * @param defaultValue
	 *            default value used in case of an error ocurrs while reading
	 *            the value.
	 * @return The <code>int</code> value for the related key, otherwise the
	 *         submitted devaultValue
	 */
	public int getIntProperty(String key, int defaultValue) {
		int value = defaultValue;
		String property = getProperty(key, Integer.toString(defaultValue));

		try {
			value = Integer.parseInt(property);

		} catch (Exception e) {
			if (logger != null && logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Unable to parse value of property " + key
						+ ". Using default value " + defaultValue, e);
			}
		}
		return value;
	}

	/**
	 * Gets the boolean representation of property.
	 * 
	 * @param key
	 *            a String representing the property name.
	 * @param defaultValue
	 *            default value used in case of an error ocurrs while reading
	 *            the value.
	 * @return The <code>boolean</code> value for the related key, otherwise
	 *         submitted defaultValue
	 */
	public boolean getBooleanProperty(String key, boolean defaultValue) {
		String property = getProperty(key, Boolean.toString(defaultValue));

		if (property.equalsIgnoreCase("true") //$NON-NLS-1$
				|| property.equalsIgnoreCase("yes") //$NON-NLS-1$
				|| property.equalsIgnoreCase("on")) { //$NON-NLS-1$
			return true;

		} else if (property.equalsIgnoreCase("false") //$NON-NLS-1$
				|| property.equalsIgnoreCase("no") //$NON-NLS-1$
				|| property.equalsIgnoreCase("off")) { //$NON-NLS-1$
			return false;
		}

		if (logger != null && logger.isLoggable(Level.WARNING)) {
			logger.log(Level.WARNING, "Unable to parse value of property " + key
					+ ". Using default value " + defaultValue);
		}

		return defaultValue;
	}

	/**
	 * Gets the boolean representation of property.
	 * 
	 * @param key
	 *            a String representing the property name.
	 * @param defaultValue
	 *            default value used in case of an error ocurrs while reading
	 *            the value.
	 * @return The <code>Level</code> value for the related key, othervide the
	 *         submitted defaultValue
	 */
	public Level getLevelProperty(String key, Level defaultValue) {
		String property = getProperty(key, defaultValue.getName());

		if (property == null || property.trim().length() == 0) return defaultValue;

		else if (property.equalsIgnoreCase(Level.OFF.getName())) return Level.OFF;

		else if (property.equalsIgnoreCase(Level.SEVERE.getName())) return Level.SEVERE;

		else if (property.equalsIgnoreCase(Level.WARNING.getName())) return Level.WARNING;

		else if (property.equalsIgnoreCase(Level.INFO.getName())) return Level.INFO;

		else if (property.equalsIgnoreCase(Level.CONFIG.getName())) return Level.CONFIG;

		else if (property.equalsIgnoreCase(Level.FINE.getName())) return Level.FINE;

		else if (property.equalsIgnoreCase(Level.FINER.getName())) return Level.FINER;

		else if (property.equalsIgnoreCase(Level.FINEST.getName())) return Level.FINEST;

		else if (property.equalsIgnoreCase(Level.ALL.getName())) return Level.ALL;

		if (logger != null && logger.isLoggable(Level.WARNING)) {
			logger.log(Level.WARNING, "Unable to parse value of property " + key
					+ ". Using default value " + defaultValue);
		}

		return defaultValue;
	}

	/**
	 * Gets the output stream used to dump some debugging information.
	 * 
	 * @return The currently used <code>OutputStream</code>
	 */
	public OutputStream getDebugOutputStream() {
		return value_debugInputStreamOutput;
	}

	/**
	 * Specifies the default output stream to dump debugging information,
	 * including the request/response xml documents.
	 * 
	 * @param outputStream
	 *            The <code>OutputStream</code> to use
	 */
	public void setDebugOutputStream(OutputStream outputStream) {
		setProperty(KEY_DEBUG_ISTREAM_OUTPUT, outputStream.toString());
		value_debugInputStreamOutput = outputStream;
	}

	/**
	 * Gets the path of current keystore.
	 * 
	 * @return The path of the current keystore
	 */
	public String getKeystore() {
		return value_keystore;
	}

	/**
	 * Specifies the path of the keystore.
	 * 
	 * @param keystore
	 *            The path to the keystore
	 */
	public void setKeystore(String keystore) {
		setProperty(KEY_KEYSTORE, keystore);
		value_keystore = keystore;
	}

	/**
	 * Gets the type of keystore.
	 * 
	 * @return The type of the keystore (e.g. "JKS", "PKCS12")
	 */
	public String getKeystoreType() {
		return value_keystore_type;
	}

	/**
	 * Specifies the type of the keystore.
	 * 
	 * @param type
	 *            The type of the keystore (e.g. "JKS", "PKCS12")
	 */
	public void setKeystoreType(String type) {
		setProperty(KEY_KEYSTORE_TYPE, type);
		value_keystore_type = type;
	}

	/**
	 * Gets the path of current truststore.
	 * 
	 * @return The path to the truststore
	 */
	public String getTruststore() {
		return value_truststore;
	}

	/**
	 * Specifies the path of the truststore.
	 * 
	 * @param truststore
	 *            The path to the truststore
	 */
	public void setTruststore(String truststore) {
		setProperty(KEY_TRUSTSTORE, truststore);
		value_truststore = truststore;
	}

	/**
	 * Gets the truststore password.
	 * 
	 * @return The password for the truststore
	 * @deprecated For security reasons instead of this method, char[]
	 *             getTruststorePassword() should be used.
	 */
	public String getTruststore_password() {
		if (value_truststore_password != null) return new String(value_truststore_password);
		return null;
	}

	/**
	 * Gets the truststore password. Note that this method returns a clone of
	 * the object which contains the password. Every single character from the
	 * object returned by this method must be reset to 0x00 in order to prevent
	 * access to the password by malicious programs.
	 * 
	 * @return The password for the truststore
	 */
	public char[] getTruststorePassword() {
		if (value_truststore_password != null) return (char[]) value_truststore_password.clone();
		return null;
	}

	/**
	 * Specifies the password for the truststore. A null password is used to
	 * reset the password.
	 * 
	 * @param newPassword
	 *            The new password for the truststore
	 * @deprecated For security reasons this method should not be used. Instead
	 *             setTruststore_password(char[] newPassword) should be used.
	 */
	public void setTruststore_password(String newPassword) {
		setProperty(KEY_TRUSTSTORE_PASSWORD, newPassword);
		// if (newPassword == null) throw new IllegalArgumentException("Null
		// newPassword argument");
		if (newPassword != null) {
			if (value_truststore_password != null) {
				for (int i = 0; i < value_truststore_password.length; i++) {
					value_truststore_password[i] = 0;
				}
			}
			value_truststore_password = newPassword.toCharArray();
		} else {
			if (value_truststore_password != null) {
				for (int i = 0; i < value_truststore_password.length; i++) {
					value_truststore_password[i] = 0;
				}
			}
			value_truststore_password = null;
		}
	}

	/**
	 * Specifies the password for the truststore. Note that the application is
	 * responsable of reseting the newPassword object in order to prevent
	 * malicious access to the password.
	 * 
	 * @param newPassword
	 *            The new password for the truststore
	 */
	public void setTruststore_password(char[] newPassword) {
		setProperty(KEY_TRUSTSTORE_PASSWORD, newPassword.toString());
		if (value_truststore_password != null) {
			for (int i = 0; i < value_truststore_password.length; i++) {
				value_truststore_password[i] = 0;
			}
		}
		if (newPassword != null) {
			value_truststore_password = new char[newPassword.length];
			for (int i = 0; i < value_truststore_password.length; i++) {
				value_truststore_password[i] = newPassword[i];
			}
		} else value_truststore_password = newPassword;
	}

	/**
	 * Gets the keystore password. Note that this method returns a clone of the
	 * object which contains the password. Every single character from the
	 * object returned by this method must be reset to 0x00 in order to prevent
	 * access to the password by malicious programs.
	 * 
	 * @return The password of the keystore
	 */
	public char[] getKeystorePassword() {
		if (value_keystore_password != null) return (char[]) value_keystore_password.clone();
		return null;
	}

	/**
	 * Specifies the password for the keystore. Note that the application is
	 * responsable of reseting the newPassword object in order to prevent
	 * malicious access to the password.
	 * 
	 * @param newPassword
	 *            The new password for the truststore
	 */
	public void setKeystore_password(char[] newPassword) {
		setProperty(KEY_KEYSTORE_PASSWORD, newPassword.toString());
		if (value_keystore_password != null) {
			for (int i = 0; i < value_keystore_password.length; i++) {
				value_keystore_password[i] = 0;
			}
		}
		if (newPassword != null) {
			value_keystore_password = new char[newPassword.length];
			for (int i = 0; i < value_keystore_password.length; i++) {
				value_keystore_password[i] = newPassword[i];
			}
		} else value_keystore_password = newPassword;
	}

	/**
	 * Gets the Xml Parsing mode.
	 * 
	 * @return A value of 0- pullbase, 1 - SAX, 2 - DOM
	 */
	public int getXmlParser() {
		return value_xmlParser;
	}

	/**
	 * Specifies the xml parsing mode.
	 * 
	 * @param parsingMode
	 *            An integer: 0 for pullbase, 1 for SAX, 2 for DOM.
	 * @throws IllegalArgumentException
	 *             if the parsingMode is not in the range from 0 .. 2.
	 */
	public void setXmlParser(int parsingMode) {
		if (parsingMode < 0 || parsingMode > 2) throw new IllegalArgumentException(
				"invalid time out value: " + parsingMode);

		setProperty(KEY_XML_PARSER, Integer.toString(parsingMode));
		value_xmlParser = parsingMode;
	}

	/**
	 * Gets the HTTP connection request time out (in milliseconds).
	 * 
	 * @return The time out for the HTTP connection
	 */
	public int getHttpTimeOut() {
		return value_httpTimeOut;
	}

	/**
	 * Specifies the time out for HTTP connections.
	 * 
	 * @param timeOut
	 *            The new time out for the HTTP connection
	 * @throws IllegalArgumentException
	 *             if the timeOut is not in the range from 0 ..
	 *             <code>Integer.MAX_VALUE</code>.
	 */
	public void setHttpTimeOut(int timeOut) {
		if (timeOut < 0) throw new IllegalArgumentException("invalid time out value: " + timeOut);

		setProperty(KEY_HTTP_TIME_OUT, Integer.toString(timeOut));
		value_httpTimeOut = timeOut;
	}

	/**
	 * Determines if the XML from CIMOM's response will be dumped into the
	 * default debugging output stream.
	 * 
	 * @return <code>true</code> if debuging of XML responses is enabled,
	 *         otherwise <code>false</code>
	 */
	public boolean isDebugXMLInput() {
		return value_debugXMLInput;
	}

	/**
	 * Specifies if the XML response from the CIMOM will be dumped into the
	 * default debugging output stream.
	 * 
	 * @param enableDebugXMLInput
	 *            The value to disable/enable debugging the XML responses
	 */
	public void setDebugXMLInput(boolean enableDebugXMLInput) {
		setProperty(KEY_DEBUG_XML_INPUT, Boolean.toString(enableDebugXMLInput));
		value_debugXMLInput = enableDebugXMLInput;
	}

	/**
	 * Determines if the XML from request will be dumped into the default
	 * debugging output stream.
	 * 
	 * @return <code>true</code> if debugging the XML requests is enabled,
	 *         otherwise <code>false</code>
	 */
	public boolean isDebugXMLOutput() {
		return value_debugXMLOutput;
	}

	/**
	 * Specifies if the XML from the request will be dumped into the default
	 * debugging output stream.
	 * 
	 * @param enableDebugXMLOutput
	 *            The value to disable/enable debugging the XML requests.
	 */
	public void setDebugXMLOutput(boolean enableDebugXMLOutput) {
		setProperty(KEY_DEBUG_XML_OUTPUT, Boolean.toString(enableDebugXMLOutput));
		value_debugXMLOutput = enableDebugXMLOutput;
	}

	/**
	 * Determines if the response from the CIMOM's (before parsing the XML) will
	 * be dumped into the default debugging output stream.
	 * 
	 * @return <code>true</code> if debugging the XML responses is enabled,
	 *         otherwise <code>false</code>
	 */
	public boolean isDebugInputStream() {
		return value_debugInputStream;
	}

	/**
	 * Specifies if the input stream must be dumped into the default debugging
	 * output stream.
	 * 
	 * @param enableDebugInputStream
	 *            The value to disable/enable dumping the the input stream to
	 *            the debugging output stream.
	 */
	public void setDebugInputStream(boolean enableDebugInputStream) {
		setProperty(KEY_DEBUG_ISTREAM, Boolean.toString(enableDebugInputStream));
		value_debugInputStream = enableDebugInputStream;
	}

	/**
	 * Gets the connection pool size. A value of -1 means all the connections
	 * will be reused when it is possible. Zero or a positive value indicates
	 * the maximum number of HTTP connections to be reused by the application.
	 * 
	 * @return The current size of possible connections in the connection pool.
	 */
	public int getConnectionPoolSize() {
		return value_connectionPoolSize;
	}

	/**
	 * Specifies the maximum number of connection pools to be maitained alive,
	 * and later will be reused for additional request.
	 * 
	 * @param poolSize
	 *            The new size of possible connections.
	 * @throws IllegalArgumentException
	 *             if the specified poolSize is not in the value range from -1 ..
	 *             Integer.MAX_VALUE.
	 */
	public void setConnectionPoolSize(int poolSize) {
		if (poolSize < -1) throw new IllegalArgumentException("invalid poolSize value: " + poolSize);

		setProperty(KEY_HTTP_CONNECTION_POOL_SIZE, Integer.toString(poolSize));
		value_connectionPoolSize = poolSize;
	}

	/**
	 * Determines if the Http Connection Pool debugging is enabled or not.
	 * 
	 * @deprecated This method is not used anywhere...
	 * 
	 * @return <code>true</code> if the debug ouput of the HTTP Connection
	 *         pool is enabled, otherwise <code>false</code>
	 */
	public boolean isDebugHttpConnectionPool() {
		return value_debugHttpPool;
	}

	/**
	 * Activates or deactivates the HttpConnectionPool debugging information.
	 * 
	 * @deprecated This method is never used at all...
	 * 
	 * @param enableDebugHTTPPool
	 *            The value to disable/enable writing debug information of the
	 *            HTTP Connection Pool.
	 */
	public void setDebugHttpConnectionPool(boolean enableDebugHTTPPool) {
		setProperty(KEY_DEBUG_HTTP_POOL, Boolean.toString(enableDebugHTTPPool));
		value_debugHttpPool = enableDebugHTTPPool;
	}

	/**
	 * Gets the complete class name for the JSSE provider.
	 * 
	 * @return The class name for the JSSE provider.
	 */
	public String getJSSEProvider() {
		return value_jsseProvider;
	}

	/**
	 * Specifies the complete class name for the JSSE provider.
	 * 
	 * @param providerClassName
	 *            The new class name for the JSSE provider.
	 */
	public void setJSSEProvider(String providerClassName) {
		setProperty(KEY_JSSE_PROVIDER, providerClassName);
		value_jsseProvider = providerClassName;
	}
	
	/**
	 * @return the JSSE Protocol
	 */
	public String getJSSEProtocol() {
		return value_jsseProtocol;
	}
	
	/**
	 * @param pProtocol
	 */
	public void setJSSEProtocol(String pProtocol) {
		setProperty(KEY_JSSE_PROTOCOL, pProtocol);
		value_jsseProtocol = pProtocol;
	}
	
 	/**
	 * @return The class name for JSSE protocol handler.
	 */
	public String getJSSEProtocolHandler() {
		return value_jsseProtocolHandler;
	}
	
	public void setJSSEProtocolHandler(String handlerClassName) {
		setProperty(KEY_JSSE_PROTOCOL_HANDLER, handlerClassName);
		value_jsseProtocolHandler = handlerClassName;
	}

	/**
	 * Gets the custom socket factory for client connections. This property is
	 * available at runtime only and cannot be set via the configuration file.
	 * 
	 * @return The custom socket factory for client connections.
	 */
	public SocketFactory getSocketFactory() {
		return value_socketFactory;
	}

	/**
	 * Specifies the custom socket factory for client connections. This property
	 * is available at runtime only and cannot be set via the configuration
	 * file.
	 * 
	 * @param factory
	 *            The custom socket factory. If <code>null</code> the client
	 *            will use javax.net.SocketFactory.getDefault() or
	 *            javax.net.ssl.SslSocketFactory.getDefault() factory
	 *            respectively. Note that the client doesn't do a consistency
	 *            check between the protocol and the custom socket factory.
	 */
	public void setSocketFactory(SocketFactory factory) {
		value_socketFactory = factory;
	}

	/**
	 * Gets the Certificate Managers name.
	 * 
	 * @return The certificate manager name
	 */
	public String getJSSECertificate() {
		return value_jsseCertificate;
	}

	/**
	 * Specifies the JSSE certificate manager used for https connection.
	 * 
	 * @param jsseCertificateMgr
	 *            The new JSSE certificate manager
	 */
	public void setJSSECertificate(String jsseCertificateMgr) {
		setProperty(KEY_JSSE_CERTIFICATE, jsseCertificateMgr);
		value_jsseCertificate = jsseCertificateMgr;
	}

	/**
	 * Gets the default logger for this application.
	 * 
	 * @return The current used <code>Logger</code>
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Allows applications to specify external <code>Logger<code> to be used
	 * by all the CIM clients. 
	 * Supported on the global properties instance only.
	 * 
	 * @param newLogger The new <code>Logger</code> to be used for logging
	 * @throws IllegalStateException When called on a non-global instance
	 * @see #isGlobal()
	 */
	public void setLogger(Logger newLogger) {
		if (!isGlobal()) throw new IllegalStateException(
				"Logger settings can be changed via the global properties instance only.");
		setProperty(KEY_LOGGER_NAMESPACE, logger.getName());
		setProperty(KEY_LOGGER, logger.getLevel().intValue() < Level.OFF.intValue() ? Boolean
				.toString(true) : Boolean.toString(false));
		logger = newLogger;
	}

	/**
	 * Determines if logging in general is enabled or not.
	 * 
	 * @return <code>true</code> if logging is enabled, otherwise
	 *         <code>false</code>
	 */
	public boolean isLoggingEnabled() {
		return value_logger_enabled;
	}

	/**
	 * Specifies if logging in general is enabled or not. Supported on the
	 * global properties instance only.
	 * 
	 * @param enableLogging
	 *            The value to disable/enable logging.
	 * @throws IllegalStateException
	 *             When called on a non-global instance
	 * @see #isGlobal()
	 */
	public void setLoggingEnabled(boolean enableLogging) {
		if (!isGlobal()) throw new IllegalStateException(
				"Logger settings can be changed via the global properties instance only.");
		setProperty(KEY_LOGGER, Boolean.toString(enableLogging));
		value_logger_enabled = enableLogging;
		setUpLoggingFramework();
	}

	/**
	 * Get the current log level of the console log handler.
	 * 
	 * @return The current log level of the console log handler
	 */
	public Level getLoggerConsoleLevel() {
		return value_loggerConsoleLevel;
	}

	/**
	 * Specifies a new log level for the console log handler. Supported on the
	 * global properties instance only.
	 * 
	 * @param loggerConsoleLevel
	 *            The new log level for the console log handler
	 * @throws IllegalStateException
	 *             When called on a non-global instance
	 * @see #isGlobal()
	 */
	public void setLoggerConsoleLevel(Level loggerConsoleLevel) {
		if (!isGlobal()) throw new IllegalStateException(
				"Logger settings can be changed via the global properties instance only.");
		setProperty(KEY_LOGGER_CONSOLE_LEVEL, loggerConsoleLevel.getName());
		value_loggerConsoleLevel = loggerConsoleLevel;
		setUpLoggingFramework();
	}

	/**
	 * Get the current log level of the file log handler.
	 * 
	 * @return The current log level of the file log handler
	 */
	public Level getLoggerFileLevel() {
		return value_loggerFileLevel;
	}

	/**
	 * Specifies a new log level for the file log handler Supported on the
	 * global properties instance only.
	 * 
	 * @param loggerFileLevel
	 *            The new log level for the file log handler.
	 * @throws IllegalStateException
	 *             When called on a non-global instance
	 * @see #isGlobal()
	 */
	public void setLoggerFileLevel(Level loggerFileLevel) {
		if (!isGlobal()) throw new IllegalStateException(
				"Logger settings can be changed via the global properties instance only.");
		setProperty(KEY_LOGGER_FILE_LEVEL, loggerFileLevel.getName());
		value_loggerFileLevel = loggerFileLevel;
		setUpLoggingFramework();
	}

	/**
	 * Get the current used namespace for the logger.
	 * 
	 * @return The current used namespace for the logger.
	 */
	public String getLoggerNameSpace() {
		return value_loggerNameSpace;
	}

	/**
	 * Specifies a new namespace for the logger. Supported on the global
	 * properties instance only.
	 * 
	 * @param loggerNameSpace
	 *            The new namespace used for the logger.
	 * @throws IllegalStateException
	 *             When called on a non-global instance
	 * @see #isGlobal()
	 */
	public void setLoggerNameSpace(String loggerNameSpace) {
		if (!isGlobal()) throw new IllegalStateException(
				"Logger settings can be changed via the global properties instance only.");
		setProperty(KEY_LOGGER_NAMESPACE, logger.getName());
		value_loggerNameSpace = loggerNameSpace;
		logger.removeHandler(consoleHandler);
		logger.removeHandler(fileHandler);
		consoleHandler = null;
		fileHandler = null;
		logger = Logger.getLogger(value_loggerNameSpace);
		setUpLoggingFramework();
	}

	/**
	 * Get the file name, log messages are written to.
	 * 
	 * @return The file name of the log file.
	 */
	public String getLoggerOutputFile() {
		return value_loggerOutputFile;
	}

	/**
	 * Specifies a new log file name Supported on the global properties instance
	 * only.
	 * 
	 * @param loggerOutputFile
	 * @throws IllegalStateException
	 *             When called on a non-global instance
	 * @see #isGlobal()
	 */
	public void setLoggerOutputFile(String loggerOutputFile) {
		if (!isGlobal()) throw new IllegalStateException(
				"Logger settings can be changed via the global properties instance only.");
		setProperty(KEY_LOGGER_OUTPUT, loggerOutputFile);
		value_loggerOutputFile = loggerOutputFile;
		logger.removeHandler(fileHandler);
		fileHandler = null;
		setUpLoggingFramework();
	}

	/**
	 * Determines if the usage of the default credentials is enabled or not.
	 * 
	 * @return <code>true</code> if the usage of the default credentials is
	 *         enabled, otherwise <code>false</code>
	 */
	public boolean isCredentialsDefaultEnabled() {
		return value_credentialsDefaultEnabled;
	}

	/**
	 * Specifies if the usage of the default credentials must be used or not.
	 * 
	 * @param credentialsDefaultEnabled
	 *            The value to disable/enable the usage of the default
	 *            credentials.
	 */
	public void setCredentialsDefaultEnabled(boolean credentialsDefaultEnabled) {
		setProperty(KEY_CREDENTIALS_DEFAULT_ENABLED, Boolean.toString(credentialsDefaultEnabled));
		value_credentialsDefaultEnabled = credentialsDefaultEnabled;
	}

	/**
	 * Get the current credentials used if the usage of default credentials is
	 * enabled. The default credentials is aka password.
	 * 
	 * @return The current credentials (password).
	 */
	public String getDefaultCredentials() {
		return value_defaultCredentials;
	}

	/**
	 * Specifies new default credentials to use if the usage of default
	 * credentials is enabled.
	 * 
	 * @param credentialsPassword
	 *            The new credential (password) to use.
	 */
	public void setDefaultCredentials(String credentialsPassword) {
		setProperty(KEY_DEFAULT_CREDENTIAL, credentialsPassword);
		value_defaultCredentials = credentialsPassword;
	}

	/**
	 * Get the current principal used if the usage of default credentials is
	 * enabled. The default principal is aka userID.
	 * 
	 * @return The current principal (userID).
	 */
	public String getDefaultPrincipal() {
		return value_defaultPrincipal;
	}

	/**
	 * Specifies new default principal to use if the usage of default
	 * credentials is enabled.
	 * 
	 * @param credentialsUser
	 *            The new principal (userID) to use.
	 */
	public void setDefaultPrincipal(String credentialsUser) {
		setProperty(KEY_DEFAULT_PRINCIPAL, credentialsUser);
		value_defaultPrincipal = credentialsUser;
	}

	/**
	 * Get the number of retries that is set for the transmission of the cimXML
	 * request.
	 * 
	 * @return The current number of retries.
	 */
	public int getRetriesNumber() {
		return value_retries_number;
	}

	/**
	 * Specifies the new number of retries that is set for the transmission of
	 * the cimXML request.
	 * 
	 * @param retries
	 *            The new number of retries.
	 */
	public void setRetriesNumber(int retries) {
		setProperty(KEY_RETRIES_NUMBER, Integer.toString(retries));
		value_retries_number = retries;
	}

	/**
	 * Returns a boolean that indicates whether or not the content lenght retry
	 * mechanism is enabled.
	 * 
	 * @return <code>true</code> if content length check is enabled, otherwise
	 *         <code>false</code>
	 */
	public boolean isContentLengthRetryEnabled() {
		return value_retries_content_enable;
	}

	/**
	 * Enables or disables the content length retry mechanism.
	 * 
	 * @param enableContentCheck
	 *            The value to disable/enable the content length retry
	 *            mechanism.
	 */
	public void setContentLengthRetry(boolean enableContentCheck) {
		setProperty(KEY_RETRIES_CONTENT_ENABLE, Boolean.toString(enableContentCheck));
		value_retries_content_enable = enableContentCheck;
	}

	/**
	 * Returns an integer that contains the content length that is being used
	 * for the retry mechanism.
	 * 
	 * @return The content length.
	 */
	public int getContentLength() {
		return value_retries_content_length;
	}

	/**
	 * Determines the content length that is supposed to be used during the
	 * retry mechanism.
	 * 
	 * @param contentLength
	 *            The content length.
	 */
	public void setContentLength(int contentLength) {
		setProperty(KEY_RETRIES_CONTENT_LENGTH, Integer.toString(contentLength));
		value_retries_content_length = contentLength;
	}

	/**
	 * Gets the HTTP authentication module 
	 * 
	 * @return the HTTP authentication module 
	 */
	public String getHttpAuthenticationModule() {
		return value_http_authentication;
	}

	/**
	 * Sets the HTTP authentication module 
	 * @param pAuthenticationModule The authentication module 
	 * 
	 * @see org.sblim.wbem.util.SessionProperties#WWW_AUTHENTICATION
	 * @see org.sblim.wbem.util.SessionProperties#PEGASUS_LOCAL_AUTHENTICATION
	 */
	public void setHttpAuthenticationModule(String pAuthenticationModule) {
		setProperty(KEY_AUTHENTICATION, pAuthenticationModule);
		value_http_authentication = pAuthenticationModule;
	}
	
	/**
	 * Returns a boolean that indicates whether or not SSL handshakes should
	 * be synchronized.
	 * 
	 * @return <code>true</code> if SSL handshakes should be synchronized, otherwise
	 *         <code>false</code>
	 */
	public boolean getSynchronizedSslHandshake() {
		return value_synchronized_ssl_handshake;
	}
	
	/**
	 * Enables or disables the synchronization of SSL handshakes
	 * @param enableSynchronizedSslHandshake
	 *            The value to disable/enable the SSL handshake synchronization
	 *            mechanism.
	 */
	public void setSynchronizedSslHandshake(boolean enableSynchronizedSslHandshake) {
		setProperty(KEY_SYNCHRONIZED_SSL_HANDSHAKE, Boolean.toString(enableSynchronizedSslHandshake));
		value_synchronized_ssl_handshake = enableSynchronizedSslHandshake;
	}
	
	/**
	 * Loads the properties from the specified properties.
	 * 
	 * @param properties
	 *            An instance of properties.
	 * @return <code>true</code> if the property file could be loaded
	 *         successfully, otherwise <code>false</code> is returned.
	 */
	public boolean loadProperties(Properties properties) {

		try {
			if (properties != null) {
				Enumeration keyEnumeration = properties.keys();

				while (keyEnumeration.hasMoreElements()) {
					Object keyProperty = keyEnumeration.nextElement();

					if (containsKey(keyProperty)) {
						setProperty(keyProperty.toString(), properties.getProperty(keyProperty
								.toString()));
					} else {
						put(keyProperty.toString(), properties.getProperty(keyProperty.toString()));
					}
				}
			} else {
				throw new IllegalArgumentException("null argument");
			}

		} finally {
			setProperties();
		}

		return true;
	}

	/**
	 * Loads the properties from the default property file. The default property
	 * file name is "cim.defaults". If this file does not exist, the method
	 * tries to load from "cimclient.properties". If this file also does not
	 * exist, the default values will be used.
	 * 
	 * @return <code>true</code> if the property file could be loaded
	 *         successfully, otherwise <code>false</code> is returned.
	 */
	public boolean loadProperties() {	
		File file = null;
		try {
			String confFile = System.getProperty(KEY_CONFIGFILE);
			if (confFile != null) {
				file = new File(confFile).getAbsoluteFile();
				if (file.exists()) {
					load(new FileInputStream(file));
					propertyFile = file.getAbsolutePath();
					return true;
				}
			}
			file = new File(FILE_DEFAULT_PROPERTIES).getAbsoluteFile();
			if (file.exists()) {
				load(new FileInputStream(file));
				propertyFile = FILE_DEFAULT_PROPERTIES;
				return true;
			}

			file = new File(FILE_CIMCLIENT_PROPERTIES).getAbsoluteFile();
			if (file.exists()) {
				load(new FileInputStream(file));
				propertyFile = FILE_CIMCLIENT_PROPERTIES;
				return true;
			}
			
			if (System.getProperty("os.name").equalsIgnoreCase("Linux")) {
				file = new File(LINUX_SBLIM_CIM_CLIENT_PROPERTIES).getAbsoluteFile();
				if (file.exists()) {
					load(new FileInputStream(file));
					propertyFile = LINUX_SBLIM_CIM_CLIENT_PROPERTIES;
					return true;
				}
			}

		} catch (Exception e) {
			System.err.println("Failed to load file " + file.getName());
			e.printStackTrace();

		} finally {
			setProperties();
		}

		return false;
	}

	/**
	 * Loads the properties from the specified file.
	 * 
	 * @param pPropertyFile
	 *            a String representing the file location.
	 * @return <code>true</code> if the property file could be loaded
	 *         successfully, otherwise <code>false</code> is returned.
	 */
	public boolean loadProperties(String pPropertyFile) {
		try {
			File file = new File(pPropertyFile).getAbsoluteFile();

			if (file.exists()) {
				load(new FileInputStream(file));
				propertyFile = pPropertyFile;
				return true;
			}

		} catch (Exception e) {
			if (logger != null && logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Failed to load file " + pPropertyFile);
			} else {
				System.err.println("Failed to load file " + pPropertyFile);
			}

		} finally {
			setProperties();
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public synchronized Object clone() {

		SessionProperties clone = new SessionProperties();
		clone.setConnectionPoolSize(getConnectionPoolSize());
		clone.setContentLength(getContentLength());
		clone.setContentLengthRetry(isContentLengthRetryEnabled());
		clone.setCredentialsDefaultEnabled(isCredentialsDefaultEnabled());
		clone.setDebugHttpConnectionPool(isDebugHttpConnectionPool());
		clone.setDebugInputStream(isDebugInputStream());
		clone.setDebugOutputStream(getDebugOutputStream());
		clone.setDebugXMLInput(isDebugXMLInput());
		clone.setDebugXMLOutput(isDebugXMLOutput());
		clone.setDefaultCredentials(getDefaultCredentials());
		clone.setDefaultCredentials(getDefaultCredentials());
		clone.setHttpTimeOut(getHttpTimeOut());
		clone.setJSSECertificate(getJSSECertificate());
		clone.setJSSEProvider(getJSSEProvider());
		clone.setKeystore(getKeystore());
		clone.setKeystore_password(getKeystorePassword());
		clone.setRetriesNumber(getRetriesNumber());
		clone.setTruststore(getTruststore());
		clone.setTruststore_password(getTruststorePassword());
		clone.setXmlParser(getXmlParser());
		clone.setSocketFactory(getSocketFactory());
		clone.setHttpAuthenticationModule(getHttpAuthenticationModule());
		clone.setSynchronizedSslHandshake(getSynchronizedSslHandshake());
		
		return clone;
	}

	/**
	 * Determines if these session properties are the global properties.
	 * 
	 * @return <code>true</code> if these session properties are the global
	 *         ones
	 */
	public boolean isGlobal() {
		return isGlobal;
	}

	/**
	 * Setting isGlobal to <code>true</code> makes this intance the global
	 * properties instance. It is the responsibility of the caller to ensure
	 * that there is only one global instance in the system.
	 * 
	 * @param pIsGlobal
	 *            The new value of isGlobal
	 */
	private void setGlobal(boolean pIsGlobal) {
		isGlobal = pIsGlobal;
		setGlobalProperties();
	}

	/**
	 * Gets the properties file that was applied (by loadProperties() or
	 * loadProperties(String) this SessionProperties instance. If multiple files
	 * have been applied this method returns the last one. If no properties file
	 * has been applied the method returns <code>null</code>.
	 * 
	 * @return The properties file
	 */
	public String getPropertyFile() {
		return propertyFile;
	}
	
}
