import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.Properties;

import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.HostListSummary;
import com.vmware.vim25.HostOvercommittedEvent;
import com.vmware.vim25.HostResignatureRescanResult;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostSystemConnectionState;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;


public class HealthManager extends Thread {
	public HealthManager(String vmHostName) {
		super(vmHostName);
	}
	
	// Function to perform the Ping Test operation on VM
	public boolean pingTest (String ip, Properties prop) throws IOException {
		InetAddress inet;
		
		inet = InetAddress.getByName(ip);
		System.out.println("Sending Ping Request to " + ip);
		
		if(inet.isReachable(Integer.parseInt(prop.getProperty("PingWaitTime")))) {
			return true;
		} else {
			return false;
		}	
	}
	
	// Function to perform the Ping Test operation on vHost
	public boolean pingTestvHost (String ip) throws IOException {
		// Variables for ping test
		String resultLine;
		String pingResult = " ";
		String pingCommand;
	    	
		System.out.println("Sending Ping Request to " + ip);
		pingCommand = "ping " + ip;
		
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(pingCommand);

		BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(
				pr.getInputStream()));
		while ((resultLine = inputBuffer.readLine()) != null) 
		{
			 System.out.println(resultLine);
			pingResult += resultLine;
		}
		inputBuffer.close();
		
		if (ip != null) 
		{
			if (pingResult.contains("100% loss")) 
			{
				return false;
			} else 
			{
				return true;
			}
		}else {
			return false;
		}
	}
	
	// Function to gather the statistics of the VM
	public void gatherStats(String vmHostName, Properties prop) throws Exception {
		// Service Instance
		ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
				prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
		Folder rootFolder = si.getRootFolder();
		
		// Search for the VM with that particular vm HostName
		VirtualMachine virtualmachine = (VirtualMachine) new InventoryNavigator(
		        rootFolder).searchManagedEntity(
		            "VirtualMachine", vmHostName);
		
		// Configuration for VM Statistics
		VirtualMachineConfigInfo vmcapabilityonfiginfo = virtualmachine.getConfig();
        VirtualMachineRuntimeInfo vmri = virtualmachine.getRuntime();

		System.out.println("-----------------VM STATS START----------------------");
		System.out.println("VM NAME: " + virtualmachine.getName());
		System.out.println("The VM Guest is: " +virtualmachine.getGuest().getIpAddress());
		System.out.println("The Virtual Machine's Parent is: " +virtualmachine.getParent());
		System.out.println("");
		
		System.out.println("GuestOS is: " + vmcapabilityonfiginfo.getGuestFullName());
		System.out.println("GuestID is: " + vmcapabilityonfiginfo.getGuestId());
		System.out.println("GuestName is: " + vmcapabilityonfiginfo.getName());
		System.out.println("");
		
        // Memory, CPU & I/O Statistics
		System.out.println("Connection State of VM: " + vmri.getConnectionState());
        System.out.println("Power State of VM: " + vmri.getPowerState());
        System.out.println("Boot Time: " + vmri.getBootTime());
        System.out.println("Max CPU Usage: " + vmri.getMaxCpuUsage());
        System.out.println("Max Memory Usage: " + vmri.getMaxMemoryUsage());
        System.out.println("Guest CPU Usage: " + virtualmachine.getSummary().getQuickStats().getOverallCpuUsage());   
        System.out.println("Guest Memory Usage: " + virtualmachine.getSummary().getQuickStats().getGuestMemoryUsage()); 
		System.out.println("-----------------VM STATS END----------------------");
		
		// Close Connection
		si.getServerConnection().logout();
	}
	
	// Function to provision VM using screenshot
	public boolean provisionVM(String vmHostName, Properties prop) throws Exception {
		// Variables for status
		boolean provisioned = false;
		
		// Service Instance
		ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
				prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
		Folder rootFolder = si.getRootFolder();
				
		// Search for the VM with that particular vm HostName
		VirtualMachine virtualmachine = (VirtualMachine) new InventoryNavigator(
				 rootFolder).searchManagedEntity(
				        "VirtualMachine", vmHostName);
		
		 VirtualMachineCloneSpec cloneSpec = 
			      new VirtualMachineCloneSpec();
			    cloneSpec.setLocation(new VirtualMachineRelocateSpec());
			    cloneSpec.setPowerOn(true);
			    cloneSpec.setTemplate(false);
			    cloneSpec.setSnapshot(virtualmachine.getCurrentSnapShot().getMOR());

			    Task task = virtualmachine.cloneVM_Task((Folder) virtualmachine.getParent(), 
			    		vmHostName + "_recovered", cloneSpec);
			    System.out.println("Launching the VM recovery task. " +
			    		"Please wait ...");

			    String status = task.waitForMe();
			    if(status==Task.SUCCESS)
			    {
			      provisioned = true;
			    }
			    else
			    {
			      System.out.println("Failure -: VM " + vmHostName + " cannot be recovered");
			      provisioned = false;
			    }
			    
		// Close Connection
		si.getServerConnection().logout();
		
		return provisioned;
	}
	
	// Return the IP Address/Name of vHost
	public String findVMvHostMapping(String vmHostName, Properties prop) throws Exception {
		// Variable to store vHost
		String vHostIP = null;
		HostSystem hostsystemobj;
		
		// Service Instance
		ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
				prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
		Folder rootFolder = si.getRootFolder();
		
		// Search for the VM with that particular vHost
		ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities(
				new String[][] { {"HostSystem", "name" }, }, true);
		for(ManagedEntity managedEntity : hosts)
		{
			hostsystemobj = (HostSystem) managedEntity;
			VirtualMachine[] vmsd =  hostsystemobj.getVms();
			
			for (ManagedEntity managedEntityVM : vmsd) {
				if (managedEntityVM.getName().equals(vmHostName)) {
					vHostIP = managedEntity.getName();
					break;
				}
			}
			
		}
		
		// Close Connection
		si.getServerConnection().logout();
		
		return vHostIP;
	}
	
	// Function to test if vHost is alive or not
	public boolean testVHost(String vmHostName, Properties prop) throws Exception{
		// Variable to store vHost
		String vHostIP = null;
		boolean pingResult;
		boolean connected = false;
		boolean testVHost = false;
		HostSystem hostsystemobj = null;
		
		// Get vHost IP
		vHostIP = findVMvHostMapping(vmHostName,prop);
		
		// Service Instance
		ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
						prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
		Folder rootFolder = si.getRootFolder();
		
		// Get vHost
		hostsystemobj = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity(
				"HostSystem", vHostIP);
		
		// Check 1: Perform ping test on vHost
		pingResult = pingTestvHost(vHostIP);
		
		// Check 2: Verify connection state
		if(hostsystemobj.getRuntime().getConnectionState() == HostSystemConnectionState.disconnected) {
			connected = false;
		}else{
			connected = true;
		}
		
		if (connected && pingResult)
			testVHost = true;
		else 
			testVHost = false;
		
		// Close Connection
		si.getServerConnection().logout();
			
		return testVHost;
	}
	
	// Function to test if vHost is connected
	public boolean testVHostConnection(String vmHostName, Properties prop) throws Exception{ 
		// Variable to store vHost
		String vHostIP = null;
		boolean connected = false;
		HostSystem hostsystemobj = null;
		
		// Get vHost IP
		vHostIP = findVMvHostMapping(vmHostName,prop);
		
		// Service Instance
		ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
						prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
		Folder rootFolder = si.getRootFolder();
		
		// Get vHost
		hostsystemobj = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity(
				"HostSystem", vHostIP);
		
		// Check 2: Verify connection state
		if(hostsystemobj.getRuntime().getConnectionState() == HostSystemConnectionState.disconnected) {
			connected = false;
		}else{
			connected = true;
		}
		
		// Close Connection
		si.getServerConnection().logout();
			
		return connected;
		
	}
	
	
	// Function to find another live vHost and provision VM
	public boolean provisionOnLivevHost(String vmHostName, Properties prop) throws Exception {
			// Variable to store vHost
			HostSystem hostsystemobj = null;
			boolean pingResult = false;
			boolean provisioned = false;
			int poolIndex = 0;
					
			// Service Instance
			ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
					prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
			Folder rootFolder = si.getRootFolder();
			
			// Get the dead vm configurations
			VirtualMachine virtualmachine = (VirtualMachine) new InventoryNavigator(
					 rootFolder).searchManagedEntity(
					        "VirtualMachine", vmHostName);
			Datastore[] dataStore = virtualmachine.getDatastores();
	        ManagedEntity[] rps = new InventoryNavigator(rootFolder).searchManagedEntities(new String[][] {{"ResourcePool", "name" }, }, true);  
			
			// Search for another alive vHost
			ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities(
					new String[][] { {"HostSystem", "name" }, }, true);
			for(ManagedEntity managedEntity : hosts)
			{
				hostsystemobj = (HostSystem) managedEntity;
				// Perform ping test on vHost
				pingResult = pingTestvHost(hostsystemobj.getName());
				
					if (pingResult == true) {
						break;
					}
					poolIndex ++;
			}
			
			if (pingResult == true) {
				// Recover vm on the alive found vHost
				VirtualMachineRelocateSpec relocateSpec = new VirtualMachineRelocateSpec();
		        relocateSpec.setDatastore(dataStore[0].getMOR());
		        relocateSpec.setHost(hostsystemobj.getMOR());
		        relocateSpec.setPool(rps[poolIndex].getMOR());
		        
				 VirtualMachineCloneSpec cloneSpec = 
					      new VirtualMachineCloneSpec();
					    cloneSpec.setLocation(relocateSpec);
					    cloneSpec.setPowerOn(true);
					    cloneSpec.setTemplate(false);
					    cloneSpec.setSnapshot(virtualmachine.getCurrentSnapShot().getMOR());
	
					    Task task = virtualmachine.cloneVM_Task((Folder) virtualmachine.getParent(), 
					    		vmHostName + "_recovered", cloneSpec);
					    System.out.println("Launching the VM recovery task. " +
					    		"Please wait ...");
	
					    String status = task.waitForMe();
					    if(status==Task.SUCCESS)
					    {
					      provisioned = true;
					    }
					    else
					    {
					      System.out.println("Failure -: VM " + vmHostName + " cannot be recovered");
					    }
					    
				// Close Connection
				si.getServerConnection().logout();
			}
			
			return provisioned;
	}
	
	// Function to try to make vHost alive for provisioning
	public boolean provisionvHost(String vmHostName, Properties prop)  {
		// Variable to store vHost
		HostSystem hostsystemobj = null;
		boolean provisionedvHost = false;
		boolean provisioned = false;
		String vHostIP;
		
		try {
				// Get vHost IP
				vHostIP = findVMvHostMapping(vmHostName,prop);
				
				// Connect to admin
				ServiceInstance siAdmin = new ServiceInstance(new URL(prop.getProperty("vCenterAdmin")), prop.getProperty("vCenterAdminUsername"), 
						prop.getProperty("vCenterAdminPassword"), Boolean.parseBoolean(prop.getProperty("vCenterAdminIgnoreCert")));
				Folder rootFolderAdmin = siAdmin.getRootFolder();
				
				ResourcePool rpsAdmin = (ResourcePool)new InventoryNavigator(rootFolderAdmin).searchManagedEntity("ResourcePool", prop.getProperty("vCenterAdminRP"));  
		        VirtualMachine[] vms = rpsAdmin.getVMs();
		        VirtualMachine oneVM = null;
		        
				for(int i=0; i<vms.length; i++)
				{
					if(vms[i].getName().contains(vHostIP.substring(7)))
					{ oneVM = (VirtualMachine)vms[i]; 
						  break; }
				}
				
				Datastore[] dataStore = oneVM.getDatastores();
		        new InventoryNavigator(rootFolderAdmin).searchManagedEntities(new String[][] {{"ResourcePool", "name" }, }, true);  
		        hostsystemobj = (HostSystem) new InventoryNavigator(rootFolderAdmin).searchManagedEntity(
						"HostSystem",  prop.getProperty("vCenterAdminHost"));
		         
				// Recover vHost
		        VirtualMachineRelocateSpec relocateSpec = new VirtualMachineRelocateSpec();
		        relocateSpec.setDatastore(dataStore[0].getMOR());
		        relocateSpec.setHost(hostsystemobj.getMOR());
		        relocateSpec.setPool(rpsAdmin.getMOR());
		        
				VirtualMachineCloneSpec cloneSpec = 
					      new VirtualMachineCloneSpec();
					    cloneSpec.setLocation(relocateSpec);
					    cloneSpec.setPowerOn(true);
					    cloneSpec.setTemplate(false);
					    cloneSpec.setSnapshot(oneVM.getCurrentSnapShot().getMOR());
	
					    Task task = oneVM.cloneVM_Task((Folder) oneVM.getParent(), 
					    		oneVM.getName() + "_recovered", cloneSpec);
					    System.out.println("Launching the VM recovery task. " +
					    		"Please wait ...");
	
					    String status = task.waitForMe();
					    if(status==Task.SUCCESS)
					    {	
					    	oneVM.powerOffVM_Task();
					    	Task destroyTask = oneVM.destroy_Task();
					    	String destroyStatus = destroyTask.waitForMe();
					        if(destroyStatus==Task.SUCCESS)
						    {
					        	// Do nothing
						    }else {
						    	System.out.println("vHost " + oneVM.getName() + " has been recovered and provisioned. Please delete stale vHost manually");
						    }
					        
					      provisionedvHost = true;
					      
					      // Close the admin connection
					      siAdmin.getServerConnection().logout();
					    }
					    else
					    {
					      System.out.println("Failure -: VM " + oneVM.getName() + " cannot be recovered");
					    }
					    
					 if (provisionedvHost == true) {
						// Service Instance
						 ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
									prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
						 Folder rootFolder = si.getRootFolder();
							
						// Get datacenter configuration
						Datacenter dataCenter = (Datacenter)new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", prop.getProperty("DataCenter"));  
							
						// 	Configuration for Host Specification
						HostConnectSpec hSpec = new HostConnectSpec();
							hSpec.setHostName(vHostIP);
							hSpec.setUserName(prop.getProperty("vHostUsername"));
							hSpec.setPassword(prop.getProperty("vHostPassword"));
							hSpec.setForce(true);
							hSpec.setSslThumbprint(prop.getProperty("vStandByHostSSL"));
						
						// Get the stale vHost and re-connect it
						HostSystem hostsystemobj2 = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity(
									"HostSystem", vHostIP);
						
						Task reconnectTask = hostsystemobj2.reconnectHost_Task(hSpec);

				    	String reconnectStatus = reconnectTask.waitForMe();
				        if(reconnectStatus==Task.SUCCESS)
					    {
				        	provisioned = true;
					    }else {
					    	provisioned = false;
					    }
					 }
		} catch(Exception e) {
			e.printStackTrace();
		}
				return provisioned;
	}
	
	// Function to find another live vHost and provision VM using Migration
	public boolean provisionOnLivevHostMigrate(String vmHostName, Properties prop) throws Exception {
				// Variable to store vHost
				HostSystem hostsystemobj = null;
				boolean pingResult = false;
				boolean provisioned = false;
						
				// Service Instance
				ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
						prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
				Folder rootFolder = si.getRootFolder();
				
				// Get the dead vm configurations
				VirtualMachine virtualmachine = (VirtualMachine) new InventoryNavigator(
						 rootFolder).searchManagedEntity(
						        "VirtualMachine", vmHostName);
				
				// Search for another alive vHost
				ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities(
						new String[][] { {"HostSystem", "name" }, }, true);
				for(ManagedEntity managedEntity : hosts)
				{
					hostsystemobj = (HostSystem) managedEntity;
					// Perform ping test on vHost
					pingResult = pingTestvHost(hostsystemobj.getName());
					
						if (pingResult == true) {
							break;
						}
				}
				
				if (pingResult == true) {
					// Recover vm on the alive found vHost
					ComputeResource cr = (ComputeResource) hostsystemobj.getParent();
					    
					Task task = virtualmachine.migrateVM_Task(cr.getResourcePool(), hostsystemobj,
					        VirtualMachineMovePriority.highPriority, 
					        VirtualMachinePowerState.poweredOn);
					 
					 String status = task.waitForMe();
					    if(status==Task.SUCCESS)
					    {
					      provisioned = true;
					    }
					    else
					    {
					      System.out.println("Failure -: VM " + vmHostName + " cannot be recovered");
					    }
						    
					// Close Connection
					si.getServerConnection().logout();
				}
				
				return provisioned;
	}
	
	// Function to add a new vHost and provision VM
	public boolean addNewvHostAndProvision(String vmHostName, String hostIP, Properties prop) throws Exception {
				// Variable to store vHost
				HostSystem hostsystemobj = null;
				boolean provisioned = false;
				boolean vHostAdded = false;
				int poolIndex = 0;
						
				// Service Instance
				ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
						prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
				Folder rootFolder = si.getRootFolder();
				
				// Get datacenter configuration
				Datacenter dataCenter = (Datacenter)new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", prop.getProperty("DataCenter"));  
				
				// 	Configuration for Host Specification
				HostConnectSpec hSpec = new HostConnectSpec();
				hSpec.setHostName(hostIP);
				hSpec.setUserName(prop.getProperty("vHostUsername"));
				hSpec.setPassword(prop.getProperty("vHostPassword"));
				hSpec.setForce(true);
				hSpec.setSslThumbprint(prop.getProperty("vStandByHostSSL"));

				// Add new VM
				ComputeResourceConfigSpec compResSpec = new ComputeResourceConfigSpec();
				Task taskAddHost = dataCenter.getHostFolder().addStandaloneHost_Task(hSpec, compResSpec, true);
				
				if(taskAddHost.waitForTask()==Task.SUCCESS) {
					vHostAdded = true;
				    }
				else {
					vHostAdded = false;
				}
				
				if (vHostAdded == true) {
					// Get the dead vm configurations
					VirtualMachine virtualmachine = (VirtualMachine) new InventoryNavigator(
							 rootFolder).searchManagedEntity(
							        "VirtualMachine", vmHostName);
					Datastore[] dataStore = virtualmachine.getDatastores();
					ManagedEntity[] rps = new InventoryNavigator(rootFolder).searchManagedEntities(new String[][] {{"ResourcePool", "name" }, }, true);  
					
					// Search for index pool of newly added vHost
					ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities(
							new String[][] { {"HostSystem", "name" }, }, true);
					for(ManagedEntity managedEntity : hosts)
					{
						hostsystemobj = (HostSystem) managedEntity;
							if (hostsystemobj.getName().equals(hostIP)) {
								break;
							}
							poolIndex ++;
					}
					
					// Recover vm on the newly added vHost
					VirtualMachineRelocateSpec relocateSpec = new VirtualMachineRelocateSpec();
			        relocateSpec.setDatastore(dataStore[0].getMOR());
			        relocateSpec.setHost(hostsystemobj.getMOR());
			        relocateSpec.setPool(rps[poolIndex].getMOR());
			        
					 VirtualMachineCloneSpec cloneSpec = 
						      new VirtualMachineCloneSpec();
						    cloneSpec.setLocation(relocateSpec);
						    cloneSpec.setPowerOn(true);
						    cloneSpec.setTemplate(false);
						    cloneSpec.setSnapshot(virtualmachine.getCurrentSnapShot().getMOR());
		
						    Task task = virtualmachine.cloneVM_Task((Folder) virtualmachine.getParent(), 
						    		vmHostName + "_recovered", cloneSpec);
						    System.out.println("Launching the VM recovery task. " +
						    		"Please wait ...");
		
						    String status = task.waitForMe();
						    if(status==Task.SUCCESS)
						    {
						      provisioned = true;
						    }
						    else
						    {
						      System.out.println("Failure -: VM " + vmHostName + " cannot be recovered");
						    }
						    
					// Close Connection
					si.getServerConnection().logout();
					
				}
				
			return provisioned;
	}
	
	// Function to add a new vHost and provision VM
	public boolean addNewvHostAndProvisionMigrate(String vmHostName, String hostIP, Properties prop) throws Exception {
					// Variable to store vHost
					HostSystem hostsystemobj = null;
					boolean provisioned = false;
					boolean vHostAdded = false;
					
					// Service Instance
					ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
							prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
					Folder rootFolder = si.getRootFolder();
					
					// Get datacenter configuration
					Datacenter dataCenter = (Datacenter)new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", prop.getProperty("DataCenter"));  
					
					// 	Configuration for Host Specification
					HostConnectSpec hSpec = new HostConnectSpec();
					hSpec.setHostName(hostIP);
					hSpec.setUserName(prop.getProperty("vHostUsername"));
					hSpec.setPassword(prop.getProperty("vHostPassword"));
					hSpec.setForce(true);
					hSpec.setSslThumbprint(prop.getProperty("vStandByHostSSL"));

					// Add new VM
					ComputeResourceConfigSpec compResSpec = new ComputeResourceConfigSpec();
					Task taskAddHost = dataCenter.getHostFolder().addStandaloneHost_Task(hSpec, compResSpec, true);
					
					if(taskAddHost.waitForTask()==Task.SUCCESS) {
						vHostAdded = true;
					    }
					else {
						vHostAdded = false;
					}
					
					if (vHostAdded == true) {
						// Get the dead vm configurations
						VirtualMachine virtualmachine = (VirtualMachine) new InventoryNavigator(
								 rootFolder).searchManagedEntity(
								        "VirtualMachine", vmHostName);
						ComputeResource cr = (ComputeResource) hostsystemobj.getParent();
					    
						Task task = virtualmachine.migrateVM_Task(cr.getResourcePool(), hostsystemobj,
						        VirtualMachineMovePriority.highPriority, 
						        VirtualMachinePowerState.poweredOn);
						 
						String status = task.waitForMe();
						    if(status==Task.SUCCESS)
						    {
						      provisioned = true;
						    }
						    else
						    {
						      System.out.println("Failure -: VM " + vmHostName + " cannot be recovered");
						    }
							    
						// Close Connection
						si.getServerConnection().logout();
						
					}
					
				return provisioned;
		}
	
	// Function to delete VM
	public boolean deleteVM(String vmHostName, Properties prop) throws Exception{
			// Variable to store vHost
			boolean destroyed;
			
			// Service Instance
			ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
					prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
			Folder rootFolder = si.getRootFolder();
			
			// Get the dead vm configurations
			VirtualMachine virtualmachine = (VirtualMachine) new InventoryNavigator(
					 rootFolder).searchManagedEntity(
					        "VirtualMachine", vmHostName);
			
			Task task = virtualmachine.destroy_Task();

		    String status = task.waitForMe();
		    if(status==Task.SUCCESS)
		    {
		      destroyed = true;
		    }
		    else
		    {
			  destroyed = false;
		    }
		    
		    // Close Connection
		    si.getServerConnection().logout();
			
			return destroyed;
	}
	
	@SuppressWarnings("static-access")
	public void run() {	
		// Flags for different operations
		boolean alarmSet = false;
		boolean pingResult;
		boolean alarmStatus;
		boolean pingResultvHost;
		boolean vHostConnected;
		boolean provisionedVM;
		boolean makeAlivevHost;
		boolean provisionedAliveVHost;
		boolean provisionedOnAddVHost;
		
		// Initializing classes
		AlarmManager alarmManager = new AlarmManager();
		PropertyManager getPropertyValue= new PropertyManager();
		
		// Variable for properties
		Properties prop = null;
		while (true){
			try {	
					// Get properties
					prop = getPropertyValue.getProps();
					
					if (alarmSet == false) {
						alarmManager.createAlarm(getName(),prop);
						alarmSet=true;
					}
					
					// Display statistics for the VM
					gatherStats(getName(),prop);
					
					// Start the Ping Operation for VM- IP fetched from Distributed Hash Map
					AvailabilityManager av = new AvailabilityManager();
					pingResult = pingTestvHost(av.ipListMap.get(getName()));
					
					if(pingResult == true) {
						System.out.println("VM is reachable");
					}
					else {
						System.out.println("VM is NOT reachable!");
						
						// Case 1: Check if user had shutdown the VM - True for user shutdown, False for abrupt shutdown
						alarmStatus = alarmManager.getAlarmStatus(getName(),prop);
											
						if (alarmStatus == false) { 
							// Case 2: Check if vHost is Accessible - True if accessible, False for not accessible
							pingResultvHost = testVHost(getName(),prop);
							
							if (pingResultvHost == true) {
								System.out.println("vHost is reachable, provisioning of VM Starting...");
								
								// Provision the VM
								provisionedVM = provisionVM(getName(),prop);
								
								if (provisionedVM == true) {
									// Delete the stale VM
									if(deleteVM(getName(),prop))
										System.out.println("VM " + getName() + " has been recovered and provisioned");
									else
										System.out.println("VM " + getName() + " has been recovered and provisioned. Please delete stale VM manually");
								}
								else 
									System.out.println("VM " + getName() + " could not be recovered!");
	
							}
							else {
								System.out.println("vHost is NOT reachable, using alternate method for provisioning...");
																
								// Try to make vHost Alive using snapshot
								makeAlivevHost = provisionvHost(getName(),prop);
																
								// Get vHost Connection State for deciding Clone Snapshot / Migration
								vHostConnected = testVHostConnection(getName(),prop);
								
								// If the vHost is recovered and can be ping'ed
								if ( makeAlivevHost && vHostConnected) {
									System.out.println("vHost is reachable, provisioning of VM Starting...");
									
									// Provision the VM
									provisionedVM = provisionVM(getName(),prop);
									
									if (provisionedVM == true) {
										// Delete the stale VM
										if(deleteVM(getName(),prop))
											System.out.println("VM " + getName() + " has been recovered and provisioned");
										else
											System.out.println("VM " + getName() + " has been recovered and provisioned. Please delete stale VM manually");
									}
									else 
										System.out.println("VM " + getName() + " could not be recovered!");
									
								}
								
								// Clone Remidiation
								else if(vHostConnected == true) {
									// Case 3a: Find Another alive vHost and Provision
									provisionedAliveVHost = provisionOnLivevHost(getName(),prop);
									
									// Case 3b: If No vHost is alive, Add a new vHost
									if (provisionedAliveVHost == false) {
										provisionedOnAddVHost = addNewvHostAndProvision(getName(),prop.getProperty("vStandByHost"),prop);
										
										if (provisionedOnAddVHost == true) {
											// Delete the stale VM
											if(deleteVM(getName(),prop))
												System.out.println("VM " + getName() + " has been recovered and provisioned");
											else
												System.out.println("VM " + getName() + " has been recovered and provisioned. Please delete stale VM manually");
										}
										else {
											System.out.println("VM " + getName() + " could not be recovered!");
										}
									}
									else {
										// Delete the stale VM
										if(deleteVM(getName(),prop))
											System.out.println("VM " + getName() + " has been recovered and provisioned");
										else
											System.out.println("VM " + getName() + " has been recovered and provisioned. Please delete stale VM manually");
									}
								}
								
								// Migration Remidiation
								else {
									// Case 4a: Find Another alive vHost and Provision
									provisionedAliveVHost = provisionOnLivevHostMigrate(getName(),prop);
									
									// Case 4b: If No vHost is alive, Add a new vHost
									if (provisionedAliveVHost == false) {
										provisionedOnAddVHost = addNewvHostAndProvisionMigrate(getName(),prop.getProperty("vStandByHost"),prop);
										
										if (provisionedOnAddVHost == true) {
											System.out.println("VM " + getName() + " has been recovered and provisioned.");
											}
										else {
											System.out.println("VM " + getName() + " could not be recovered!");
										}
									}
									else {
											System.out.println("VM " + getName() + " has been recovered and provisioned.");
									}
								}
								
							}		
							
						}
						
					}
				
				// Monitoring Interval
				Thread.sleep(Integer.parseInt(prop.getProperty("MonitorInterval")));
				
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
			}
	}