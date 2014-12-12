import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;


public class AvailabilityManager {
	// Distributed Static HashMap
	static HashMap<String,String> ipListMap;
	static HashMap<String,String> ipListMapvHost;
	static HashMap<String,String> ipListProvisionMap;
	
	// Get list of IP
	public HashMap<String, String> getIPList(Properties prop) throws RemoteException, MalformedURLException {
		// Declaring Hash Map for Storing IP Address of VMs
		HashMap<String,String> ipMap = new HashMap<String,String>();
		
		// Service Instance
		ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
				prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
		Folder rootFolder = si.getRootFolder();
		
		System.out.println("\n============ Virtual Machines ============");
		ManagedEntity[] vms = new InventoryNavigator(rootFolder).searchManagedEntities(
				new String[][] { {"VirtualMachine", "name" }, }, true);
		for(int i=0; i<vms.length; i++)
		{
			System.out.println("vm["+i+"]=" + vms[i].getName());
			VirtualMachine vmm = (VirtualMachine) vms[i];
			ipMap.put(vms[i].getName(), vmm.getSummary().getGuest().getIpAddress());
			System.out.println("IP Address=" + vmm.getSummary().getGuest().getIpAddress() + "\n");
		}
		
		si.getServerConnection().logout();
		
		return ipMap;
	}
	
	public HashMap<String, String> getResourcePoolVMList(Properties prop) throws RemoteException, MalformedURLException {
		// Declaring Hash Map for Storing IP Address of VMs
		HashMap<String,String> ipMap = new HashMap<String,String>();
		
		// Service Instance
		ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenterAdmin")), prop.getProperty("vCenterAdminUsername"), 
				prop.getProperty("vCenterAdminPassword"), Boolean.parseBoolean(prop.getProperty("vCenterAdminIgnoreCert")));
		Folder rootFolder = si.getRootFolder();
		
		System.out.println("\n============ Virtual Machines ============");
		ResourcePool rps = (ResourcePool)new InventoryNavigator(rootFolder).searchManagedEntity("ResourcePool", "Team01_vHOSTS");  
        VirtualMachine[] vms = rps.getVMs();
        
		for(int i=0; i<vms.length; i++)
		{
			System.out.println("vm["+i+"]=" + vms[i].getName());
			VirtualMachine vmm = (VirtualMachine) vms[i];
			ipMap.put(vms[i].getName(), vms[i].getName());
		}
		
		si.getServerConnection().logout();
		
		return ipMap;
	}

	public static void main(String[] args) throws RemoteException, MalformedURLException, Exception {
		
		// Create an Instance of AvailabilityManager
		AvailabilityManager availabilityManager = new AvailabilityManager();
		
		// Initializing classes
		PropertyManager getPropertyValue= new PropertyManager();
		Thread d = null;
		Thread s = null;
		
		ipListProvisionMap = new HashMap<String,String>();
		
		Properties prop = null;
		while(true) {
			// Get properties
			prop = getPropertyValue.getProps();
			
			// Get IP List
			ipListMap = new HashMap<String,String>();
			ipListMap = availabilityManager.getIPList(prop);
			
			ipListMapvHost = new HashMap<String,String>();
			ipListMapvHost = availabilityManager.getResourcePoolVMList(prop);
			
			 // Iterate over IP List, Ignore VMs that have Null IP
			 Iterator it2 = ipListMapvHost.entrySet().iterator();
				    while (it2.hasNext()) {
				        Map.Entry ip2 = (Map.Entry)it2.next();
				        System.out.println(ip2.getKey() + " = " + ip2.getValue());
				        
				        String hostName;
				        if (!String.valueOf(ip2.getValue()).equals("null")) {
							hostName = ip2.getKey().toString();
				        	
				        	// Start Snapshot Manager of each VM
//				        	new SnapShotManagerVHost(hostName).start();
						}
				         
//				       it.remove(); // avoids a ConcurrentModificationException
					}
			
			// Iterate over IP List, Ignore VMs that have Null IP
			Iterator it = ipListMap.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry ip = (Map.Entry)it.next();
			        System.out.println(ip.getKey() + " = " + ip.getValue());
			        
			        String hostName;
			        if (!String.valueOf(ip.getValue()).equals("null")) {
						hostName = ip.getKey().toString();
						
						if(!ipListProvisionMap.containsKey(hostName))
							ipListProvisionMap.put(hostName,"NP");
								        	
						// Check if already provisioned or not
						if(ipListProvisionMap.get(hostName).equals("NP")) {
							// Start Snapshot Manager of each VM
//				        	s = new SnapShotManager(hostName);
//				        	s.start();
//							
							// Start Health Manager of each VM
				        	d = new HealthManager(hostName);
				        	d.start();	
				        	ipListProvisionMap.put(hostName,"P");
						}
		        	
					}
//			       it.remove(); // avoids a ConcurrentModificationException
				}
				
			Thread.sleep(Integer.parseInt(prop.getProperty("MonitorIntervalMain")));
		}
		}
		    
	}


