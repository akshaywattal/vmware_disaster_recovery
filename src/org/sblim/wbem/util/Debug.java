/**
 * Debug.java
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
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 *
 */
package org.sblim.wbem.util;

/**
 * @author tsquare
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Debug {
	
	public static final int LEVEL_TRACE = 0;
	public static final int LEVEL_INFO = 2;
	public static final int LEVEL_WARN = 4;
	public static final int LEVEL_ERROR = 6;
	public static final int LEVEL_PRINT = 8;


	private static int current_level = LEVEL_INFO;
	
	public static void func_start(String className, String methodName) {
		dump(LEVEL_TRACE, className, methodName, "func_start");
	}

	public static void func_end(String className, String methodName) {
		dump(LEVEL_TRACE, className, methodName, "func_end");
	}
	
	public static void trace(String className, String methodName, String s) {
		dump(LEVEL_TRACE, className, methodName, s);
	}	

	public static void info(String className, String methodName, String s) {
		dump(LEVEL_INFO, className, methodName, s);
	}	

	public static void warn(String className, String methodName, String s) {
		dump(LEVEL_WARN, className, methodName, s);
	}

	public static void error(String className, String methodName, String s) {
		dump(LEVEL_ERROR, className, methodName, s);
	}

	public static void print(String className, String methodName, String s) {
		dump(LEVEL_PRINT, className, methodName, s);
	}

	public static void dump(int level, String className, String methodName, String message) {
		if (level >= current_level) {
			System.out.println(className+"."+methodName+"(): "+message);
//			System.out.println(methodName+": "+message);
		}
	}

}
