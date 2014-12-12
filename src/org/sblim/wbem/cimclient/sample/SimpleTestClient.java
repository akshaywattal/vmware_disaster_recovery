/**
 * SimpleTestClient.java
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
 * Flag       Date            Prog         Description
 *------------------------------------------------------------------------------- 
 *   10423    2004-08-28      thschaef     Inital Creation
 * 2219646    2008-11-17      blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22      blaschke-oss Change licensing from CPL to EPL
 */ 

package org.sblim.wbem.cimclient.sample;

import java.util.Enumeration;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.client.CIMClient;
import org.sblim.wbem.client.PasswordCredential;
import org.sblim.wbem.client.UserPrincipal;

/**
 * @author thschaef
 */
public class SimpleTestClient {
	
	private void test() {
		try {
			
						
			String cimAgentAddress = "https://127.0.0.1:5989";
			String namespace       = "root/ibm";
			String user            = "youruser";
			String pw              = "yourpawo";
					
			System.out.println("===============================================");	
			System.out.println("= Starting: "+this.getClass().getName());
			System.out.println("= CIM Agent: "+cimAgentAddress);
			System.out.println("= Namespace: "+namespace);
			System.out.println("===============================================");	
			System.out.println();
			
			// *****************************
			// 1. Create user credentials
			// *****************************
			UserPrincipal userPr = new UserPrincipal(user);
			PasswordCredential pwCred = new PasswordCredential(pw.toCharArray());			
	
			// *****************************
			// 2. Set NameSpace
			// - URL is set like: http(s)://<IP>:Port
			// - Namespace does not need to be specified in COPs if set in this constuctor
			// - There is no server authentication being done. Thus: No need for a truststore
			// *****************************			
			CIMNameSpace ns = new CIMNameSpace(cimAgentAddress,namespace);
    
			// *****************************
			// 3. Create CIM Client
			// *****************************		    				
			CIMClient cimClient = new CIMClient(ns,userPr,pwCred);
	
			
			CIMObjectPath rpCOP = new CIMObjectPath("CIM_RegisteredProfile");
	
			System.out.println("Looking for children of CIM_RegisteredProfile");
			long enumerationStart = System.currentTimeMillis();	
			
			
			// *****************************
			// 4. Enumerate Instance Names
			// - Note that the method name in aligned to the CIM Operations over HTTP standard
			// *****************************	
			Enumeration rpEnm = cimClient.enumerateInstanceNames(rpCOP);
	
			long enumerationStop = System.currentTimeMillis();
			System.out.println("Enumeration completed in: "+(enumerationStop-enumerationStart)/1000+" sec.");
			System.out.println();
	
	
			// *****************************
		    // 5. Loop through all found children of RegisteredProfiles and
		    //    traverse its CIM_ElementConformsToProfile association
		    // *****************************			
			while (rpEnm.hasMoreElements()) {
		
				CIMObjectPath rp = (CIMObjectPath) rpEnm.nextElement();
				System.out.println(" Found: "+rp);
				
				System.out.println("   Traversing CIM_ElementConformsToProfile association...");
				long assocStart = System.currentTimeMillis();	
		
				Enumeration systemEnum = cimClient.associatorNames(rp,"CIM_ElementConformsToProfile",null,null,null);
				
				long assocStop = System.currentTimeMillis();
				System.out.println("   Association traversal completed in: "+(assocStop-assocStart)/1000+" sec.");
				System.out.println();
		
				while (systemEnum.hasMoreElements()) {
					CIMObjectPath system = (CIMObjectPath) systemEnum.nextElement();
					System.out.println("     Found: "+system);
				}
				
				System.out.println();
				System.out.println();
		
		
			}
	
	        System.out.println("============= DONE ================");
	
			cimClient.close();
	
	
	
		} catch (CIMException e) {			
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		
		SimpleTestClient stc = new SimpleTestClient();
		stc.test();
		
	}
}
