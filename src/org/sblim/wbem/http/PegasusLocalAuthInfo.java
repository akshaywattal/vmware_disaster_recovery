/**
 * PegasusLocalAuthInfo.java
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
 * 1516242    2006-07-05  lupusalex    Support of OpenPegasus local authentication
 * 1604329    2006-12-18  lupusalex    Fix OpenPegasus auth module
 * 1710050    2007-04-30  lupsualex    LocalAuth fails for z/OS Pegasus
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */
package org.sblim.wbem.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.util.SessionProperties;

/**
 * Implements OpenPegasus local authentication
 */
public class PegasusLocalAuthInfo extends AuthInfo {

	private static final String ISO_8859_1 = "ISO-8859-1";

	private static final String OS_NAME = "os.name";

	private static final String Z_OS = "z/OS";

	private boolean iChallenged = false;

	/**
	 * Default ctor.
	 */
	public PegasusLocalAuthInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.sblim.wbem.http.AuthInfo#updateAuthenticationInfo(org.sblim.wbem.http.Challenge, java.net.URI, java.lang.String)
	 */
	public void updateAuthenticationInfo(Challenge challenge, String authenticate, URI url,
			String requestMethod) throws NoSuchAlgorithmException {
		iChallenged  = true;
		iResponse = authenticate;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		if (iChallenged && iResponse!=null && iResponse.startsWith("Local ")) {
			
			try {
				
				String fileName = iResponse.substring(7, iResponse.length()-1);
				
				if (fileName.length() == 0) throw new IOException("No local auth file specified");

				File authorizationFile = new File(fileName);

				if (!authorizationFile.canRead())
					throw new IOException("Local auth file not accessible");

				BufferedReader in = Z_OS.equals(System.getProperty(OS_NAME)) ? new BufferedReader(
						new InputStreamReader(new FileInputStream(authorizationFile), ISO_8859_1))
						: new BufferedReader(new FileReader(authorizationFile));

				StringBuffer buffer = new StringBuffer();
				String line;

				while (true) {
					line = in.readLine();
					if (line == null)
						break;
					buffer.append(line);
				}
				in.close();

				StringBuffer header = new StringBuffer();
				header.append("Local \"");
				header.append(getCredentials().getUserName());
				header.append(':');
				header.append(fileName);
				header.append(':');
				header.append(buffer);
				header.append('"');

				return header.toString();
				
			} catch (IOException e) {
				Logger logger = SessionProperties.getGlobalProperties().getLogger();
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Could not read pegasus local auth file", e);
				}
			}
		} 

		return "Local \"" + getCredentials().getUserName() + "\"";
	}

	/* (non-Javadoc)
	 * @see org.sblim.wbem.http.AuthInfo#getHeaderFieldName()
	 */
	public String getHeaderFieldName() {
		return "PegasusAuthorization";
	}

	/* (non-Javadoc)
	 * @see org.sblim.wbem.http.AuthInfo#isSentOnFirstRequest()
	 */
	public boolean isSentOnFirstRequest() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sblim.wbem.http.AuthInfo#isKeptAlive()
	 */
	public boolean isKeptAlive() {
		return true;
	}

}
