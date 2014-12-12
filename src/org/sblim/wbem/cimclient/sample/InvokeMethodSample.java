/**
 * InvokeMethodSample.java
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
import java.util.Vector;

import org.sblim.wbem.cim.CIMArgument;
import org.sblim.wbem.cim.CIMDataType;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.cim.CIMValue;
import org.sblim.wbem.cim.UnsignedInt32;
import org.sblim.wbem.client.CIMClient;
import org.sblim.wbem.client.PasswordCredential;
import org.sblim.wbem.client.UserPrincipal;

/**
 * @author thschaef
 */
public class InvokeMethodSample {

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
			
			// *******************************************************
			// 4. Obtain instance COP of CIM_AccountManagementService
			// *******************************************************	
			CIMObjectPath amsClassCOp = new CIMObjectPath("CIM_AccountManagementService");	
			
			System.out.println(" Obtain instance of CIM_AccountManagementService");					
			Enumeration amsEnum = cimClient.enumerateInstanceNames(amsClassCOp);			
			
			CIMObjectPath amsCOP = null;
			while (amsEnum.hasMoreElements()) {
				amsCOP = (CIMObjectPath) amsEnum.nextElement();
				break;
			}
			
			// *******************************************************
			// 5. Invoke method CIM_AccountManagementService.CreateAccount()
			// *******************************************************	
			
			System.out.println(" Prepare method invokation...");
			// INPUT Parameter
			Vector inputVec = new Vector();
			CIMArgument userID       = new CIMArgument();
			CIMArgument password     = new CIMArgument();
			CIMArgument globalRole   = new CIMArgument();
	
	        userID.setName("UserID");
			userID.setValue(new CIMValue("dummy_ID2", CIMDataType.getPredefinedType(CIMDataType.STRING)));
	
			password.setName("Password");
			password.setValue(new CIMValue("dummy", CIMDataType.getPredefinedType(CIMDataType.STRING)));
	
			globalRole.setName("GlobalRole");
			globalRole.setValue(new CIMValue("Monitor", CIMDataType.getPredefinedType(CIMDataType.STRING)));
			
			System.out.println(" Input parameters > UserID: "+userID.getValue()+" Password: "+password.getValue()+" GlobalRole: "+globalRole.getValue()+")");
	
			inputVec.add(userID);
			inputVec.add(password);
			inputVec.add(globalRole);			
	
	        // OUTPUT Parameter
			// Need to create an array of exactly the size of the expected amount of returned values
			Vector outputVec = new Vector();
	
			System.out.println(" Invoking method > CIM_AccountManagementService.CreateAccount("+userID.getValue()+","+password.getValue()+","+globalRole.getValue());
			CIMValue result = cimClient.invokeMethod(amsCOP,"CreateAccount",inputVec,outputVec);
	
			if (((UnsignedInt32)result.getValue()).intValue() == 0) {
				if (outputVec.size() == 1) {
					CIMValue newAccountV = (CIMValue) outputVec.elementAt(0);
						
					System.out.println("  >> Successfully created new account <<");
					System.out.println("  COP :"+ newAccountV.getValue().toString());
				} else {
					System.out.println("  >> F A I L E D <<");
				    System.out.println("  Number Output Parameters: "+outputVec.size());	
				}
			} else {
				System.out.println("  >> F A I L E D <<");
			    System.out.println("  Return Code (RC): "+result.getValue().toString());				
			}	
			
			System.out.println("============= DONE ================");
	
			cimClient.close();
	
		} catch (CIMException e) {			
			e.printStackTrace();
		}
	
	}
	
	public static void main(String[] args) {
		
		InvokeMethodSample iss = new InvokeMethodSample();
		iss.test();
		
		
	}
}
