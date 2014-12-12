import java.net.URL;
import java.util.Properties;

import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;


public class SnapShotManager extends Thread {
	public SnapShotManager(String vmHostName) {
		super(vmHostName);
	}
	
	// Function to create Snapshot of VM
	public void createSnapshot(String vmHostName, Properties prop) throws Exception {
		// Service Instance
		ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
				prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
		Folder rootFolder = si.getRootFolder();
		
		// Search for the VM with that particular vm HostName
		VirtualMachine virtualmachine = (VirtualMachine) new InventoryNavigator(
		        rootFolder).searchManagedEntity(
		            "VirtualMachine", vmHostName);
		
		 if(virtualmachine!=null) {
			 Task task = virtualmachine.createSnapshot_Task(
					 vmHostName.concat("_snapshot"), "Snapshot created for the VM " + vmHostName, false, false);
			      if(task.waitForMe()==Task.SUCCESS)
			      {
			        System.out.println("Snapshot was created.");
			      }
		 }
		 
		 // Closing connection
		 si.getServerConnection().logout();
	}
	
	// Function to delete Snapshot of VM
	public void deleteSnapshot(String vmHostName, Properties prop) throws Exception {
			// Service Instance
			ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
					prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
			Folder rootFolder = si.getRootFolder();
			
			// Search for the VM with that particular vm HostName
			VirtualMachine virtualmachine = (VirtualMachine) new InventoryNavigator(
			        rootFolder).searchManagedEntity(
			            "VirtualMachine", vmHostName);
			
			 if(virtualmachine!=null) {
				 Task task = virtualmachine.removeAllSnapshots_Task();
				      if(task.waitForMe()==Task.SUCCESS)
				      {
				        System.out.println("All previous Snapshots were removed.");
				      }
			 }
			 
			 // Closing connection
			 si.getServerConnection().logout();
		}
	
	public void run() {
		// Variables for properties
		Properties prop = null;
		PropertyManager getPropertyValue= new PropertyManager();
	
		while (true) {
			try {
					// Fetch properties
					prop = getPropertyValue.getProps();
					
					// Remove previous snapshot
					deleteSnapshot(getName(),prop);
				
					// Create new snapshot
					createSnapshot(getName(),prop);
					
					Thread.sleep(Integer.parseInt(prop.getProperty("SnapshotInterval")));
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
}

