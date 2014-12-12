/**
 * HttpSocketFactory.java
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
 * 1498130    2006-05-31  lupusalex    Selection of xml parser on a per connection basis
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1558663    2006-09-14  lupusalex    Support custom socket factories in client connections
 * 1573723    2006-10-09  lupusalex    Selection of JSSE provider via properties file not feasible
 * 1573723    2006-10-30  lupusalex    rework: Selection of JSSE provider via properties file not feasible
 * 1815752    2007-10-18  ebak         TLS support
 * 1815752    2007-10-31  ebak         rework: TLS support
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.http;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.sblim.wbem.util.SessionProperties;

public class HttpSocketFactory {

	private static final String CLASSNAME = HttpSocketFactory.class.getName();

	private SSLSocketFactory iSslSocketFactory;

	private SocketFactory iDefaultSocketFactory;

	private ServerSocketFactory iDefaultServerSocketFactory;

	private SSLServerSocketFactory iSslServerSocketFactory;

	private SSLContext iSslContext;

	private static HttpSocketFactory iInstance = new HttpSocketFactory();

	Logger ilogger;

	private HttpSocketFactory() {
		ilogger = SessionProperties.getGlobalProperties().getLogger();
	}

	public static HttpSocketFactory getInstance() {
		return iInstance;
	}

	public ServerSocketFactory getServerSocketFactory(boolean ssl, SessionProperties properties) {
		if (ssl) return getSSLServerSocketFactory(properties);

		return getServerSocketFactory();
	}

	public SocketFactory getSocketFactory(String protocol, SessionProperties properties) {
		String methodName = "getSocketFactory(String)";
		SocketFactory socketFactory = properties.getSocketFactory();

		if (ilogger.isLoggable(Level.FINER)) {
			ilogger.entering(CLASSNAME, methodName, protocol);
		}

		if (socketFactory == null) {
			if (protocol.equalsIgnoreCase("https")) {
				socketFactory = getSSLSocketFactory(properties);
			} else if (protocol.equalsIgnoreCase("http")) {
				socketFactory = getSocketFactory(properties);
			}
		}

		if (ilogger.isLoggable(Level.FINER)) {
			ilogger.exiting(CLASSNAME, methodName, socketFactory);
		}
		return socketFactory;
	}

	private SSLContext getSSLContext(SessionProperties properties) {
		String methodName = "getSSLContext()";

		if (ilogger.isLoggable(Level.FINER)) {
			ilogger.entering(CLASSNAME, methodName);
		}

		KeyManager[] allKeys = new KeyManager[] { new TrustAllKeyManager() };

		String provider = properties.getJSSEProvider();
		
		ilogger.log(Level.INFO, "Loading JSSE provider:"+provider);
		
		String protocolHandler = properties.getJSSEProtocolHandler();
		if (protocolHandler != null) {
			System.setProperty("java.protocol.handler.pkgs", protocolHandler);
		 }

		Class providerClass;
		Provider securityProvider;

		try {
			providerClass = Class.forName(provider);
			securityProvider = (java.security.Provider) providerClass.newInstance();
			if (Security.getProvider(securityProvider.getName()) != null) {
				Security.removeProvider(securityProvider.getName());
			}
			Security.insertProviderAt(securityProvider, 1); 
		} catch (Exception e) {
			if (ilogger.isLoggable(Level.SEVERE)) {
				ilogger.log(Level.SEVERE, "Unexpected exception while initializing JSSE provider: "
						+ provider, e);
			}
			if (ilogger.isLoggable(Level.FINER)) {
				ilogger.exiting(CLASSNAME, methodName, null);
			}
			throw new RuntimeException(e);
		}

		String keyStorePath = properties.getKeystore();
		char[] keystorePassword = properties.getKeystorePassword();

		if (keystorePassword == null) {
			if (ilogger.isLoggable(Level.WARNING)) {
				ilogger.log(Level.WARNING, "Unassigned KeyStore password");
			}
		}
		if (keyStorePath == null) {
			if (ilogger.isLoggable(Level.WARNING)) {
				ilogger.log(Level.WARNING, "Unassigned keystore path ");
			}
		}

		if (ilogger.isLoggable(Level.FINER)) {
			ilogger.log(Level.FINER, "Loading keystore from :" + keyStorePath);
		}

		// System.setProperty("javax.net.debug",
		// "ssl,handshake,data,trustmanager");

		// System.out.println("trustStore:"+System.getProperty("javax.net.ssl.trustStore"));
		// System.setProperty("javax.net.ssl.trustStore", trustStore);

		try {
			KeyManager[] keyManager = allKeys;

			try {
				KeyStore keystore = KeyStore.getInstance(properties.getKeystoreType());
				keystore.load(new FileInputStream(keyStorePath), keystorePassword);

				String certificates = properties.getJSSECertificate();
				if (ilogger.isLoggable(Level.FINER)) {
					ilogger.log(Level.FINER, "Loading KeyManagerFactory with certificates from:"
							+ certificates);
				}

				KeyManagerFactory keymanagerfactory = KeyManagerFactory.getInstance(certificates);
				if (ilogger.isLoggable(Level.FINER)) {
					ilogger.log(Level.FINER, "Initializing KeyManagerFactory...");
				}
				keymanagerfactory.init(keystore, keystorePassword);
				if (ilogger.isLoggable(Level.FINER)) {
					ilogger.log(Level.FINER, "Getting KeyManager...");
				}
				keyManager = keymanagerfactory.getKeyManagers();

			} catch (FileNotFoundException e) {
				if (ilogger.isLoggable(Level.WARNING)) {
					ilogger.log(Level.WARNING, "Keystore file not found at location: "
							+ keyStorePath, e);
				}
				keyManager = allKeys;
			} catch (IOException e) {
				if (ilogger.isLoggable(Level.WARNING)) {
					ilogger.log(Level.WARNING, "exception while reading from keystore file", e);
				}
				keyManager = allKeys;
			} catch (CertificateException e) {
				if (ilogger.isLoggable(Level.WARNING)) {
					ilogger.log(Level.WARNING, "problems with certificates while loading keystore",
							e);
				}
				keyManager = allKeys;
			} catch (UnrecoverableKeyException e) {
				if (ilogger.isLoggable(Level.SEVERE)) {
					ilogger.log(Level.SEVERE, "Unexpected exception while loading keystore", e);
				}
				keyManager = allKeys;
			} catch (KeyStoreException e) {
				if (ilogger.isLoggable(Level.WARNING)) {
					ilogger.log(Level.WARNING, "Unexpected exception while loading keystore", e);
				}
				keyManager = allKeys;
			}

			if (keystorePassword != null) {
				for (int i = 0; i < keystorePassword.length; i++)
					keystorePassword[i] = 0;
			}

			// TrustManagerFactory trustmanagerfactory =
			// TrustManagerFactory.getInstance("IbmX509");
			// trustmanagerfactory.init(keystore);
			// TrustManager atrustmanager[] =
			// trustmanagerfactory.getTrustManagers();

			String protocol = properties.getJSSEProtocol();
			
			if (ilogger.isLoggable(Level.FINER)) {
				ilogger.log(Level.FINER, "Getting SSLContext instance for: "+protocol);
			}
			//			
			iSslContext = SSLContext.getInstance(protocol);
			// for (int i=0; i < atrustmanager.length; i++)
			// if (atrustmanager[i] instanceof X509TrustManager)
			// atrustmanager[i] = new DynamicTrustManager(keystore,
			// (X509TrustManager)atrustmanager[i], trustStore, storePass);
			//				
			if (ilogger.isLoggable(Level.FINER)) {
				ilogger.log(Level.FINER,
						"Initializing SSLContext with default certificate manager (TrustAll)...");
			}
			iSslContext.init(keyManager, new X509TrustManager[] { new TrustAllTrustManager() },
					null);

			if (ilogger.isLoggable(Level.FINER)) {
				ilogger.exiting(CLASSNAME, methodName, iSslContext);
			}

			return iSslContext;

		} catch (NoSuchAlgorithmException e) {
			if (ilogger.isLoggable(Level.SEVERE)) {
				ilogger.log(Level.SEVERE, "exception while initializing SSL context", e);
			}
			if (ilogger.isLoggable(Level.FINER)) {
				ilogger.exiting(CLASSNAME, methodName, null);
			}

			return null;

		} catch (KeyManagementException e) {
			if (ilogger.isLoggable(Level.SEVERE)) {
				ilogger.log(Level.SEVERE, "exception while initializing SSL context", e);
			}
			if (ilogger.isLoggable(Level.FINER)) {
				ilogger.exiting(CLASSNAME, methodName, null);
			}

			return null;

		}
	}

	protected synchronized SSLServerSocketFactory getSSLServerSocketFactory(
			SessionProperties properties) {
		String methodName = "getSSLServerSocketFactory()";
		SSLServerSocketFactory sssf = null;

		if (ilogger.isLoggable(Level.FINER)) {
			ilogger.entering(CLASSNAME, methodName);
		}

		if (iSslServerSocketFactory != null) {
			sssf = iSslServerSocketFactory;

		} else {
			if (iSslContext == null) {
				if (ilogger.isLoggable(Level.FINER)) {
					ilogger.log(Level.FINER, "Creating default SSLContext...");
				}
				iSslContext = getSSLContext(properties);
			}

			if (iSslContext != null) {
				sssf = iSslContext.getServerSocketFactory();
			}
		}

		if (ilogger.isLoggable(Level.FINER)) {
			ilogger.exiting(CLASSNAME, methodName, sssf);
		}

		return sssf;
	}

	protected synchronized SSLSocketFactory getSSLSocketFactory(SessionProperties properties) {
		String methodName = "getSSLSocketFactory()";
		SSLSocketFactory sslsf = null;

		if (ilogger.isLoggable(Level.FINER)) {
			ilogger.entering(CLASSNAME, methodName);
		}

		if (iSslSocketFactory != null) {
			sslsf = iSslSocketFactory;

		} else {

			if (iSslContext == null) {
				if (ilogger.isLoggable(Level.FINER)) {
					ilogger.log(Level.FINER, "Creating default SSLContext...");
				}
				iSslContext = getSSLContext(properties);
			}

			if (iSslContext != null) {
				if (ilogger.isLoggable(Level.FINER)) {
					ilogger.log(Level.FINER, "Getting socket factory from SSLContext");
				}
				sslsf = iSslContext.getSocketFactory();
			}
		}

		if (ilogger.isLoggable(Level.FINER)) {
			ilogger.exiting(CLASSNAME, methodName, sslsf);
		}

		return sslsf;
	}

	protected synchronized ServerSocketFactory getServerSocketFactory() {
		String methodName = "getServerSocketFactory()";

		if (ilogger.isLoggable(Level.FINER)) {
			ilogger.entering(CLASSNAME, "getServerSocketFactory");
		}

		if (iDefaultServerSocketFactory == null) {
			if (ilogger.isLoggable(Level.FINER)) {
				ilogger.log(Level.FINER, "Using default ServerSocketFactory");
			}
			iDefaultServerSocketFactory = ServerSocketFactory.getDefault();
		}

		if (ilogger.isLoggable(Level.FINER)) {
			ilogger.exiting(CLASSNAME, methodName, iDefaultServerSocketFactory);
		}

		return iDefaultServerSocketFactory;
	}

	protected synchronized SocketFactory getSocketFactory(SessionProperties properties) {
		String methodName = " getSocketFactory()";

		if (ilogger.isLoggable(Level.FINER)) {
			ilogger.entering(CLASSNAME, methodName);
		}

		if (iDefaultSocketFactory == null) {
			if (ilogger.isLoggable(Level.FINER)) {
				ilogger.log(Level.FINER, "Using default SocketFactory");
			}
			iDefaultSocketFactory = SocketFactory.getDefault();
		}

		if (ilogger.isLoggable(Level.FINER)) {
			ilogger.exiting(CLASSNAME, methodName, iDefaultSocketFactory);
		}
		return iDefaultSocketFactory;
	}

	public synchronized void setSocketFactory(SocketFactory factory) {
		if (factory == null) throw new IllegalArgumentException("Null socket factory");
		iDefaultSocketFactory = factory;
	}

	public synchronized void setSSLSocketFactory(SSLSocketFactory factory) {
		if (factory == null) throw new IllegalArgumentException("Null socket factory");
		iSslSocketFactory = factory;
	}

	public synchronized void setSSLServerSocketFactory(SSLServerSocketFactory factory) {
		if (factory == null) throw new IllegalArgumentException("Null socket factory");
		iSslServerSocketFactory = factory;
	}
}

class TrustAllTrustManager implements X509TrustManager {

	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}

class TrustEveryoneTrustManager1 implements X509TrustManager {

	X509TrustManager iDefaultManager;

	KeyStore iDefaultKeyStore;

	String iKeystoreFile;

	String iKeystorePassword;

	public TrustEveryoneTrustManager1(KeyStore keyStore, X509TrustManager trustManager,
			String keystoreFile, String keystorePassword) {
		iDefaultManager = trustManager;
		iDefaultKeyStore = keyStore;
		iKeystoreFile = keystoreFile;
		iKeystorePassword = keystorePassword;
	}

	public void checkClientTrusted(X509Certificate[] cert, String protocol)
			throws CertificateException {
	// System.out.println("PROTOCOL ClientTrusted:"+protocol);
	// if (cert != null) {
	// for (int i=0; i < cert.length; i++) {
	// System.out.println(cert[i]);
	// }
	// cert = getAcceptedIssuers();
	// for (int i=0; i < cert.length; i++) {
	// System.out.println(cert[i]);
	// }
	// }
	// try {
	// defaultManager.checkClientTrusted(cert, protocol);
	// } catch (Exception e) {
	// e.printStackTrace();
	// try {
	// // Create an empty keystore object
	// KeyStore keystore = KeyStore.getInstance("JKS");
	//
	// // Load the keystore contents
	// FileInputStream in = new FileInputStream(keystoreFile);
	// keystore.load(in, keystorePassword.toCharArray());
	// in.close();
	//
	// // Add the certificate
	// for (int i=0; i < cert.length; i++) {
	// defaultKeyStore.setCertificateEntry("cimclients", cert[i]);
	// }
	//	
	// // Save the new keystore contents
	// FileOutputStream out = new FileOutputStream(keystoreFile);
	// keystore.store(out, keystorePassword.toCharArray());
	// out.close();
	//
	// TrustManagerFactory trustmanagerfactory =
	// TrustManagerFactory.getInstance("IbmX509");
	// trustmanagerfactory.init(defaultKeyStore);
	// TrustManager atrustmanager[] = trustmanagerfactory.getTrustManagers();
	// for (int i=0; i < atrustmanager.length; i++) {
	// if (atrustmanager[i] instanceof X509TrustManager) {
	// defaultManager = (X509TrustManager)atrustmanager[i];
	// break;
	// }
	// }
	// } catch (java.security.cert.CertificateException ce) {
	// } catch (NoSuchAlgorithmException ce) {
	// } catch (FileNotFoundException ce) {
	// // Keystore does not exist
	// } catch (KeyStoreException ce) {
	// ce.printStackTrace();
	// } catch (Exception ce) {
	// }
	// try {
	// defaultManager.checkClientTrusted(cert, protocol);
	// }
	// catch (Exception ee) {
	// ee.printStackTrace();
	// }
	// }
	}

	public void checkServerTrusted(X509Certificate[] cert, String protocol)
			throws CertificateException {
	// System.out.println("PROTOCOL ClientTrusted:"+protocol);
	// if (cert != null) {
	// for (int i=0; i < cert.length; i++) {
	// System.out.println(cert[i]);
	// }
	// }
	// try {
	// defaultManager.checkServerTrusted(cert, protocol);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	}

	public X509Certificate[] getAcceptedIssuers() {
		// System.out.println("Getting all certificates");
		// return defaultManager.getAcceptedIssuers();
		return null;
	}
}

class TrustAllKeyManager implements X509KeyManager {

	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		return null;
	}

	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		return null;
	}

	public X509Certificate[] getCertificateChain(String alias) {
		return null;
	}

	public String[] getClientAliases(String keyType, Principal[] issuers) {
		return null;
	}

	public PrivateKey getPrivateKey(String alias) {
		return null;
	}

	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return null;
	}
}
