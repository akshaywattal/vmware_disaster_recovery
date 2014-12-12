/**
 * CIMVersion.java
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
 *   12794    2004-11-12  thschaef     Add version support
 * 1535756    2006-08-08  lupusalex    Make code warning free
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.cim;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CIMVersion {

	private static String PRODUCT_NAME = "n/a";

	private static String VERSION = "n/a";

	private static String COPYRIGHT = "n/a";

	private static String BUILDDATE = "n/a";

	private static String TIME = "n/a";

	static {
		Properties properties = new Properties();
		try {
			InputStream versionIS = CIMVersion.class
					.getResourceAsStream("/org/sblim/wbem/cim/version.txt");
			properties.load(versionIS);
			PRODUCT_NAME = properties.getProperty("PRODUCTNAME");
			VERSION = properties.getProperty("VERSION");
			COPYRIGHT = properties.getProperty("COPYRIGHT");
			BUILDDATE = properties.getProperty("BUILDDATE");
			TIME = properties.getProperty("TIME");
			versionIS.close();
		} catch (FileNotFoundException e) {
			System.out.println("Error: Could not open version.txt !");
		} catch (IOException e) {
			System.out.println("Error while reading version.txt");
		}
	}

	/**
	 * Gets the build date
	 * 
	 * @return The build date
	 */
	public static String getBuildDate() {
		return BUILDDATE;
	}

	/**
	 * Gets the copyright statement
	 * 
	 * @return THe copyright
	 */
	public static String getCopyright() {
		return COPYRIGHT;
	}

	/**
	 * Gets the product name
	 * 
	 * @return The product name
	 */
	public static String getProductName() {
		return PRODUCT_NAME;
	}

	/**
	 * Gets the version
	 * 
	 * @return The version
	 */
	public static String getVersion() {
		return VERSION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return (PRODUCT_NAME + "\n" + VERSION + "\n" + COPYRIGHT + "\n" + BUILDDATE + "\n" + TIME);
	}

	public static void main(String[] args) {
		System.out.println(new CIMVersion());
	}
}
