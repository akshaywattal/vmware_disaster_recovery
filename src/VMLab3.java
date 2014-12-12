
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.net.URL;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;


public class VMLab3 
{
	static Properties prop;
	public static void main(String[] args) throws Exception
	{	
	    if(args.length!=1)
	    {
	      System.out.println("Operations: 1) Power On 2) Power Off 3) Clone 4) Cold Migrate");
	      System.out.println("Usage: java VMLab3 <operation_to_perform>");
	      System.exit(0);
	    }
	    
	    GetPropertyValue getPropertyValue= new GetPropertyValue();
	    prop = getPropertyValue.getProps();
	    
	    	
		if (args[0].equals("1")) {
			// Read from properties file
			String vHostURL =prop.getProperty("vHost");
			String username = prop.getProperty("vHostUsername");
			String password = prop.getProperty("vHostPassword");
			boolean ignoreCert = Boolean.parseBoolean(prop.getProperty("vHostIgnoreCert"));
			String name = prop.getProperty("vHostName");
			
			ServiceInstance si = new ServiceInstance(new URL (vHostURL), username, password, ignoreCert);
			Folder rootFolder = si.getRootFolder();
			
			ManagedEntity[] mes = rootFolder.getChildEntity();
			
			for(int i=0; i<mes.length; i++)
			{
				if(mes[i] instanceof Datacenter)
				{
					Datacenter dc = (Datacenter) mes[i];
					Folder vmFolder = dc.getVmFolder();
					ManagedEntity[] vms = vmFolder.getChildEntity();
					
					for(int j=0; j<vms.length; j++)
					{
						if(vms[j] instanceof VirtualMachine)
						{
							VirtualMachine vm = (VirtualMachine) vms[j];
							System.out.println((vm.getName()));
							VirtualMachineSummary summary = (VirtualMachineSummary) (vm.getSummary());
							System.out.println(summary.toString());
							VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();
							if(vmri.getPowerState() == VirtualMachinePowerState.poweredOff
									&& name.equals(vm.getName()))
								{
									Task task = vm.powerOnVM_Task(null);
									task.waitForMe();
									System.out.println("vm:" + vm.getName() + " powered on.");
								}
						}
					}
				}
			}
			si.getServerConnection().logout();
		}
		
		if (args[0].equals("2")) {
			// Read from properties file
			String vHostURL =prop.getProperty("vHost");
			String username = prop.getProperty("vHostUsername");
			String password = prop.getProperty("vHostPassword");
			boolean ignoreCert = Boolean.parseBoolean(prop.getProperty("vHostIgnoreCert"));
			String name = prop.getProperty("vHostName");
			
			ServiceInstance si = new ServiceInstance(new URL(vHostURL), username, password, ignoreCert);
			Folder rootFolder = si.getRootFolder();
			
			ManagedEntity[] mes = rootFolder.getChildEntity();
			
			for(int i=0; i<mes.length; i++)
			{
				if(mes[i] instanceof Datacenter)
				{
					Datacenter dc = (Datacenter) mes[i];
					Folder vmFolder = dc.getVmFolder();
					ManagedEntity[] vms = vmFolder.getChildEntity();
					
					for(int j=0; j<vms.length; j++)
					{
						if(vms[j] instanceof VirtualMachine)
						{
							VirtualMachine vm = (VirtualMachine) vms[j];
							System.out.println((vm.getName()));
							VirtualMachineSummary summary = (VirtualMachineSummary) (vm.getSummary());
							System.out.println(summary.toString());
							VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();
							if(vmri.getPowerState() == VirtualMachinePowerState.poweredOn
									&& name.equals(vm.getName()))
								{
									Task task = vm.powerOffVM_Task();
									task.waitForMe();
									System.out.println("vm:" + vm.getName() + " powered off.");
								}
						}
					}
				}
			}
			si.getServerConnection().logout();
		}
		
		if (args[0].equals("3")) {
			// Read from properties file
			String vServerURL =prop.getProperty("vServer");
			String username = prop.getProperty("vServerUsername");
			String password = prop.getProperty("vServerPassword");
			boolean ignoreCert = Boolean.parseBoolean(prop.getProperty("vServerIgnoreCert"));
			
			String vmname = prop.getProperty("VMName");
		    String cloneName = prop.getProperty("VMCloneName");

		    ServiceInstance si = new ServiceInstance(
		        new URL(vServerURL), username, password, ignoreCert);

		    Folder rootFolder = si.getRootFolder();
		    VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
		        rootFolder).searchManagedEntity(
		            "VirtualMachine", vmname);

		    if(vm==null)
		    {
		      System.out.println("No VM " + vmname + " found");
		      si.getServerConnection().logout();
		      return;
		    }

		    VirtualMachineCloneSpec cloneSpec = 
		      new VirtualMachineCloneSpec();
		    cloneSpec.setLocation(new VirtualMachineRelocateSpec());
		    cloneSpec.setPowerOn(false);
		    cloneSpec.setTemplate(false);

		    Task task = vm.cloneVM_Task((Folder) vm.getParent(), 
		        cloneName, cloneSpec);
		    System.out.println("Launching the VM clone task. " +
		    		"Please wait ...");

		    String status = task.waitForMe();
		    if(status==Task.SUCCESS)
		    {
		      System.out.println("VM got cloned successfully.");
		    }
		    else
		    {
		      System.out.println("Failure -: VM cannot be cloned");
		    }
			
		}
		
		if (args[0].equals("4")) {
			// Read from properties file
			String vServerURL =prop.getProperty("vServer");
			String username = prop.getProperty("vServerUsername");
			String password = prop.getProperty("vServerPassword");
			boolean ignoreCert = Boolean.parseBoolean(prop.getProperty("vServerIgnoreCert"));
			
			String vmname = prop.getProperty("VMName");
			String newHostName = prop.getProperty("vHostNameMigration");

			    ServiceInstance si = new ServiceInstance(
			        new URL(vServerURL), username, password, ignoreCert);

			    Folder rootFolder = si.getRootFolder();
			    VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
			        rootFolder).searchManagedEntity(
			            "VirtualMachine", vmname);
			    HostSystem newHost = (HostSystem) new InventoryNavigator(
			        rootFolder).searchManagedEntity(
			            "HostSystem", newHostName);
			    ComputeResource cr = (ComputeResource) newHost.getParent();
			    
			    String[] checks = new String[] {"cpu", "software"};

			    Task task = vm.migrateVM_Task(cr.getResourcePool(), newHost,
			        VirtualMachineMovePriority.highPriority, 
			        VirtualMachinePowerState.poweredOff);
			  
			    if(task.waitForMe()==Task.SUCCESS)
			    {
			      System.out.println("Migrated!");
			    }
			    else
			    {
			      System.out.println("Migration failed!");
			      TaskInfo info = task.getTaskInfo();
			      System.out.println(info.getError().getFault());
			    }
			    si.getServerConnection().logout();
			  }
	}
		
}