/**
 * IndicationSubscriptionListenerSample.java
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
 * @author: Kai Boerner, IBM, kai.boerner@us.ibm.com  
 * 
 * 
 * Change History
 * Flag       Date            Prog         Description
 *------------------------------------------------------------------------------- 
 *            2005-11-24      kboerner     Inital Creation
 * 1535756    2006-08-07      lupusalex    Make code warning free
 * 2807325    2009-06-22      blaschke-oss Change licensing from CPL to EPL
 */
package org.sblim.wbem.cimclient.sample;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Vector;

import org.sblim.wbem.cim.CIMClass;
import org.sblim.wbem.cim.CIMDataType;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.cim.CIMProperty;
import org.sblim.wbem.cim.CIMValue;
import org.sblim.wbem.cim.UnsignedInt16;
import org.sblim.wbem.client.CIMClient;
import org.sblim.wbem.client.PasswordCredential;
import org.sblim.wbem.client.UserPrincipal;
import org.sblim.wbem.client.indications.CIMEvent;
import org.sblim.wbem.client.indications.CIMEventDispatcher;
import org.sblim.wbem.client.indications.CIMIndicationHandler;
import org.sblim.wbem.client.indications.CIMIndicationListenertList;
import org.sblim.wbem.client.indications.CIMListener;
import org.sblim.wbem.http.HttpConnectionHandler;
import org.sblim.wbem.http.HttpServerConnection;

public class IndicationSubscriptionListenerSample implements CIMListener {

	public static void main(String args[]) {

		if (args.length != 4) {
			System.out
					.println("Usage: IndicationsubscriptionListenerSample <CIMOM_URL> <NAMESPACE> <USER> <PW>");
			return;
		}

		IndicationSubscriptionListenerSample ic = new IndicationSubscriptionListenerSample(args[0], args[1], args[2], args[3]);

		if (!ic.startServer()) {
			System.out.println("Listener not startet. End sample program");
			return;
		}
		System.out.println("Listener started....");
		try {

			try {
				System.out.println("Cleaning up subscriptions");
				ic.unsubscribe();
			} catch (Exception e) {}
			try {
				System.out.println("Subscribing");
				ic.subscribe();
				System.out.println("Subscribed");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Problem during subscribtion. Exiting.");
				return;
			}

			while (ic.indicationCounter < ic.maxIndications) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {}
			}
			System.out.println("Exiting.");

		} finally {
			ic.unsubscribe();
			ic.tearDown();
		}
	}

	private String cimAgentAddress;

	private HttpServerConnection httpServerConnection; 

	private int indicationCounter = 1; 

	private int indicationListenerPort = 5990;

	private String key = "IndicationTestClient";

	private int maxIndications = 50;

	private String namespace;

	private String pw;

	private String user;

	public IndicationSubscriptionListenerSample(String pCIMOM, String pNamespace, String pUser, String pPw) {
		cimAgentAddress = pCIMOM;
		namespace = pNamespace;
		user = pUser;
		pw = pPw;
	}

	public void indicationOccured(CIMEvent e) {

		CIMInstance indication = e.getIndication();
		System.out.println("Indication (" + indicationCounter + "):  Name ="
				+ indication.getClassName());
		Vector props = indication.getProperties();
		for (int i = 0; i < props.size(); ++i) {
			CIMProperty cp = (CIMProperty) props.elementAt(i);
			String value = (cp.getValue() != null) ? cp.getValue().toString() : "null";
			System.out.println("Indication (" + indicationCounter + "):  " + cp.getName() + " ="
					+ value);
		}
		indicationCounter++;
	}

	private CIMInstance buildIndicationFilterInstance(CIMClient pCimClient, String pFilterType,
			String pLocalIPAddress) {

		String mSystemHostName = pCimClient.getNameSpace().getHost();

		try {
			mSystemHostName = InetAddress.getByName(mSystemHostName).getCanonicalHostName();
		} catch (Exception e) {
			// No canonicalHostName() available, use IP Address
		}

		CIMClass mFilterClass = pCimClient.getClass(new CIMObjectPath("CIM_IndicationFilter"),
				false);
		CIMInstance filterInstance = mFilterClass.newInstance();
		String filterString = "SELECT * FROM " + pFilterType;

		filterInstance.setProperty("Query", new CIMValue(filterString, CIMDataType
				.getPredefinedType(CIMDataType.STRING)));
		filterInstance.setProperty("QueryLanguage", new CIMValue("WQL", CIMDataType
				.getPredefinedType(CIMDataType.STRING)));
		filterInstance.setProperty("SystemName", new CIMValue(mSystemHostName, CIMDataType
				.getPredefinedType(CIMDataType.STRING)));
		filterInstance.setProperty("Name", new CIMValue(pFilterType + " for " + key + " "
				+ pLocalIPAddress, CIMDataType.getPredefinedType(CIMDataType.STRING)));
		filterInstance.setProperty("Description", new CIMValue(key, CIMDataType
				.getPredefinedType(CIMDataType.STRING)));
		filterInstance.setProperty("CreationClassName", new CIMValue("CIM_IndicationFilter",
				CIMDataType.getPredefinedType(CIMDataType.STRING)));
		filterInstance.setProperty("SystemCreationClassName", new CIMValue("CIM_ComputerSystem",
				CIMDataType.getPredefinedType(CIMDataType.STRING)));
		filterInstance.setProperty("SourceNamespace", new CIMValue(namespace, CIMDataType
				.getPredefinedType(CIMDataType.STRING)));

		return filterInstance;
	}

	private CIMInstance buildIndicationHandlerInstance(CIMClient pCimClient,
			String pLocalIPAddress, int pLocalListenerPort, int pCimomID) {

		String mSystemHostName = pCimClient.getNameSpace().getHost();
		try {
			mSystemHostName = InetAddress.getByName(mSystemHostName).getCanonicalHostName();
		} catch (Exception e) {
			// No canonicalHostName() available, use IP Address
		}

		CIMClass mHandlerClass = pCimClient.getClass(new CIMObjectPath(
				"CIM_ListenerDestinationCIMXML"), false);
		CIMInstance destinationInst = mHandlerClass.newInstance();

		destinationInst
				.setProperty("CreationClassName", new CIMValue("CIM_ListenerDestinationCIMXML",
						CIMDataType.getPredefinedType(CIMDataType.STRING)));
		destinationInst.setProperty("SystemCreationClassName", new CIMValue("CIM_ComputerSystem",
				CIMDataType.getPredefinedType(CIMDataType.STRING)));
		destinationInst.setProperty("Name", new CIMValue("CIM_System for " + key + " "
				+ pLocalIPAddress, CIMDataType.getPredefinedType(CIMDataType.STRING)));
		destinationInst.setProperty("SystemName", new CIMValue(mSystemHostName, CIMDataType
				.getPredefinedType(CIMDataType.STRING)));
		destinationInst.setProperty("PersistenceType", new CIMValue(new UnsignedInt16(3),
				CIMDataType.getPredefinedType(CIMDataType.UINT16)));

		destinationInst.setProperty("Destination", new CIMValue("http://" + pLocalIPAddress + ":"
				+ pLocalListenerPort + "/" + pCimomID, CIMDataType
				.getPredefinedType(CIMDataType.STRING)));

		return destinationInst;
	}

	private CIMInstance buildIndicationSubscriptionInstance(CIMClient pCimClient,
			CIMObjectPath pHandlerCOP, CIMObjectPath pFilterCOP) {

		CIMClass mSubscriptionClass = pCimClient.getClass(new CIMObjectPath(
				"CIM_IndicationSubscription"), false);
		CIMInstance ssInst = mSubscriptionClass.newInstance();

		ssInst.setProperty("Filter", new CIMValue(pFilterCOP, CIMDataType
				.getPredefinedType(CIMDataType.REFERENCE)));
		ssInst.setProperty("Handler", new CIMValue(pHandlerCOP, CIMDataType
				.getPredefinedType(CIMDataType.REFERENCE)));
		ssInst.setProperty("OnFatalErrorPolicy", new CIMValue(new UnsignedInt16(2), CIMDataType
				.getPredefinedType(CIMDataType.UINT16)));

		return ssInst;
	}

	private CIMClient connect() {
		try {

			UserPrincipal userPr = new UserPrincipal(user);
			PasswordCredential pwCred = new PasswordCredential(pw.toCharArray());

			CIMNameSpace ns = new CIMNameSpace(cimAgentAddress, namespace);

			return new CIMClient(ns, userPr, pwCred);

		} catch (CIMException e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean startServer() {
		CIMIndicationListenertList indicationClient = new CIMIndicationListenertList();
		indicationClient.addListener(this);
		CIMEventDispatcher dispatcher = new CIMEventDispatcher(indicationClient);
		CIMIndicationHandler indicationHdlr = new CIMIndicationHandler(dispatcher);
		try {
			httpServerConnection = new HttpServerConnection(new HttpConnectionHandler(
					indicationHdlr), indicationListenerPort);
			httpServerConnection.setName("CIMListener - Http Server");
			httpServerConnection.start();
		} catch (Exception e) {
			dispatcher.kill();
		}
		return httpServerConnection != null;
	}

	private void subscribe() throws Exception {

		CIMClient cimClient = connect();

		try {

			CIMInstance mHandlerInstance = buildIndicationHandlerInstance(cimClient,
					httpServerConnection.getHostIP(), httpServerConnection.getPort(), 0);
			CIMInstance mFilterInstance1 = buildIndicationFilterInstance(cimClient,
					"CIM_InstCreation", httpServerConnection.getHostIP());
			CIMInstance mFilterInstance2 = buildIndicationFilterInstance(cimClient,
					"CIM_InstModification", httpServerConnection.getHostIP());
			CIMInstance mFilterInstance3 = buildIndicationFilterInstance(cimClient,
					"CIM_InstDeletion", httpServerConnection.getHostIP());
			CIMInstance mFilterInstance4 = buildIndicationFilterInstance(cimClient,
					"CIM_ProcessIndication", httpServerConnection.getHostIP());

			CIMObjectPath mIhPath = cimClient.createInstance(new CIMObjectPath(), mHandlerInstance);

			CIMObjectPath mFilterPath1 = cimClient.createInstance(new CIMObjectPath(),
					mFilterInstance1);
			CIMObjectPath mFilterPath2 = cimClient.createInstance(new CIMObjectPath(),
					mFilterInstance2);
			CIMObjectPath mFilterPath3 = cimClient.createInstance(new CIMObjectPath(),
					mFilterInstance3);
			CIMObjectPath mFilterPath4 = cimClient.createInstance(new CIMObjectPath(),
					mFilterInstance4);

			CIMInstance mSubscriptionInstance1 = buildIndicationSubscriptionInstance(cimClient,
					mIhPath, mFilterPath1);
			cimClient.createInstance(new CIMObjectPath(), mSubscriptionInstance1);

			CIMInstance mSubscriptionInstance2 = buildIndicationSubscriptionInstance(cimClient,
					mIhPath, mFilterPath2);
			cimClient.createInstance(new CIMObjectPath(), mSubscriptionInstance2);

			CIMInstance mSubscriptionInstance3 = buildIndicationSubscriptionInstance(cimClient,
					mIhPath, mFilterPath3);
			cimClient.createInstance(new CIMObjectPath(), mSubscriptionInstance3);

			CIMInstance mSubscriptionInstance4 = buildIndicationSubscriptionInstance(cimClient,
					mIhPath, mFilterPath4);
			cimClient.createInstance(new CIMObjectPath(), mSubscriptionInstance4);

		} finally {
			cimClient.close();
		}
	}

	private void tearDown() {
		try {
			httpServerConnection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void unsubscribe() {

		CIMClient cimClient = connect();

		try {

			try {
				CIMInstance mHandler = buildIndicationHandlerInstance(cimClient,
						httpServerConnection.getHostIP(), httpServerConnection.getPort(), 0);
				Vector mFilterCOPs = new Vector();
				Vector mSubscriptionCOPs = new Vector();

				try {
					Enumeration mFilterCOPsEnum = cimClient.associatorNames(mHandler
							.getObjectPath(), "CIM_IndicationSubscription", "CIM_IndicationFilter",
							null, null);
					while (mFilterCOPsEnum != null && mFilterCOPsEnum.hasMoreElements())
						mFilterCOPs.add(mFilterCOPsEnum.nextElement());

					Enumeration mSubscriptionCOPsEnum = cimClient.referenceNames(mHandler
							.getObjectPath());
					while (mSubscriptionCOPsEnum != null && mSubscriptionCOPsEnum.hasMoreElements())
						mSubscriptionCOPs.add(mSubscriptionCOPsEnum.nextElement());
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (mSubscriptionCOPs != null) for (int i = 0; i < mSubscriptionCOPs.size(); i++) {
					try {
						cimClient.deleteInstance((CIMObjectPath) mSubscriptionCOPs.elementAt(i));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				for (int i = 0; i < mFilterCOPs.size(); i++) {
					try {
						cimClient.deleteInstance((CIMObjectPath) mFilterCOPs.elementAt(i));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				CIMInstance mFilterInstance1 = buildIndicationFilterInstance(cimClient,
						"CIM_InstCreation", httpServerConnection.getHostIP());
				cimClient.deleteInstance(mFilterInstance1.getObjectPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				CIMInstance mFilterInstance2 = buildIndicationFilterInstance(cimClient,
						"CIM_InstModification", httpServerConnection.getHostIP());
				cimClient.deleteInstance(mFilterInstance2.getObjectPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				CIMInstance mFilterInstance3 = buildIndicationFilterInstance(cimClient,
						"CIM_InstDeletion", httpServerConnection.getHostIP());
				cimClient.deleteInstance(mFilterInstance3.getObjectPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				CIMInstance mFilterInstance4 = buildIndicationFilterInstance(cimClient,
						"CIM_ProcessIndication", httpServerConnection.getHostIP());
				cimClient.deleteInstance(mFilterInstance4.getObjectPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				// ebak: this instance should be deleted too
				CIMInstance mHandler = buildIndicationHandlerInstance(cimClient,
						httpServerConnection.getHostIP(), httpServerConnection.getPort(), 0);
				cimClient.deleteInstance(mHandler.getObjectPath());
			} catch (Exception e) {
				e.printStackTrace();
			}

		} finally {
			cimClient.close();
		}
	}

}// end of class

