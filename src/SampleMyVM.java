import CONFIG.*;
import java.net.URL;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * Write a description of class MyVM here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class SampleMyVM
{
    // instance variables - replace the example below with your own
    private String vmname ;
    private ServiceInstance si ;
    private VirtualMachine vm ;

    /**
     * Constructor for objects of class MyVM
     */
    public SampleMyVM( String vmname ) 
    {
        // initialise instance variables
        try {
            this.vmname = vmname ;
            this.si = new ServiceInstance(new URL(SJSULAB.getVmwareHostURL()), SJSULAB.getVmwareLogin(), SJSULAB.getVmwarePassword(), true);
            Folder rootFolder = si.getRootFolder();
            this.vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", this.vmname);
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }

        if( this.vm==null)
        {
            System.out.println("No VM " + vmname + " found");
            if ( this.si != null)
                this.si.getServerConnection().logout();
        }
    }

    /**
     * Destructor for objects of class MyVM
     */
    protected void finalize() throws Throwable
    {
        this.si.getServerConnection().logout(); //do finalization here
        super.finalize(); //not necessary if extending Object.
    } 

    /**
     * Power On the Virtual Machine
     */
    public void powerOn() 
    {
        try {
            System.out.println("command: powered on");
            Task task = vm.powerOnVM_Task(null);
            if(task.waitForMe()==Task.SUCCESS)
            {
                System.out.println(vmname + " powered on");
            }
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }
    }

    /**
     * Power Off the Virtual Machine
     */
    public void powerOff() 
    {
        try {
            System.out.println("command: powered off");
            Task task = vm.powerOffVM_Task();
            if(task.waitForMe()==Task.SUCCESS)
            {
                System.out.println(vmname + " powered off");
            }
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }
    }

     /**
     * Reset the Virtual Machine
     */

    public void reset() 
    {
        try {
            System.out.println("command: reset");
            Task task = vm.resetVM_Task();
            if(task.waitForMe()==Task.SUCCESS)
            {
                System.out.println(vmname + " reset");
            }
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }
    }


     /**
     * Suspend the Virtual Machine
     */
 
    public void suspend() 
    {
        try {
            System.out.println("command: suspend");
            Task task = vm.suspendVM_Task();
            if(task.waitForMe()==Task.SUCCESS)
            {
                System.out.println(vmname + " suspended");
            }
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }
    }

    

    /**
     *  Put VM & Guest OS on Standby
     */
    public void standBy() 
    {
        try {
            System.out.println("command: stand by");
            vm.standbyGuest();
            System.out.println(vmname + " guest OS stoodby");
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }
    }
    
    
    
}


