/**
 * PasswordCredential.java
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
 *   14673    2005-01-26  thschaef     Fix setUserPassword() method
 * 1362792    2005-11-22  fiuczy       setUserPassword(char[]) does not clear old password
 * 1362783    2005-11-22  fiuczy       PasswordCredential.getUserPassword exposes char[] reference
 * 1362773    11/22/2005  fiuczy       Possible NullPointerException in PassworCredential(char[])
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.client;

import java.util.Arrays;

/**
 * Provides password based credential for a user login authentication. This class must be
 * used with the UserPrincipal interface, which contains the username and host name 
 * of the system for which the password is associeted to it.  
 */
public class PasswordCredential {
	
	private char[] iPassword;
	private String iHostname;
	

	/**
	 * Constructs an object of a PasswordCredential.
	 * Hostname and user password will both be null.
	 */
	public PasswordCredential() {
	}

	/**
	 * Constructs an object of a PasswordCredential with the specified password.
	 * @param pPassword
	 * @deprecated for security reason this class has been deprecated. 
	 * PasswordCredential(char[] password) constructor must be used instead of this 
	 * constructor.
	 */
	public PasswordCredential(String pPassword) {
		this.iPassword = pPassword.toCharArray();
	}

	/**
	 * Constructs an object of a PasswordCredential with the specified password.
	 * @param pPassword
	 */
	public PasswordCredential(char[] pPassword) {
		this.iPassword = null;
		if(pPassword!=null) {
			this.iPassword = new char[pPassword.length];
			System.arraycopy(pPassword, 0, this.iPassword,0, pPassword.length);
		}
	}
	
	/**
	 * Clears the user password from memory and 
	 * resets the password to null.
	 */
	public void clearUserPassword() {
		if (iPassword != null) {
			for (int i=0; i < iPassword.length; i++)
				iPassword[i] = 0;
		}
		this.iPassword = null;
	}
	
	/**
	 * Gets the host name.
	 * @return hostname
	 */
	public String getHostName() {
		return iHostname;
	}
	
	/**
	 * Gets a copy of the user password.
	 * @return user password
	 */
	public char[] getUserPassword() {
		if(iPassword!=null) {
			char[] passwordCopy = new char[iPassword.length];
			System.arraycopy(iPassword, 0, passwordCopy,0, iPassword.length);
			return passwordCopy;
		}
		return null;
	}
	
	/**
	 * Specifies the host name.
	 * @param pHostname New hostname to be set.
	 */
	public void setHostName(String pHostname) {
		this.iHostname = pHostname;		
	}
	
	/**
	 * Sets a new password for this credential object.
	 * Old password will be cleared!
	 * @param pPassword New password to be set.
	 */
	public void setUserPassword(char[] pPassword) { // 14673
		clearUserPassword(); // delete old password! #1362792
		if (pPassword != null) {
			this.iPassword = new char[pPassword.length];
			System.arraycopy(pPassword, 0, this.iPassword,0, pPassword.length);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof PasswordCredential) {
			PasswordCredential that = (PasswordCredential)o;
			if (!((this.iHostname == null && that.iHostname == null) ||
				(this.iHostname != null && this.iHostname.equals(that.iHostname))))
				return false; // hostname does not match
			return Arrays.equals(this.iPassword, that.iPassword);
		}
		return false; // object o is not of same class or null
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return ((iHostname != null)? iHostname.hashCode():0) + 
				((iPassword != null)? iPassword.hashCode():0);
	}
}
