import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Properties;

import com.vmware.vim25.Action;
import com.vmware.vim25.AlarmAction;
import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmState;
import com.vmware.vim25.AlarmTriggeringAction;
import com.vmware.vim25.EventAlarmExpression;
import com.vmware.vim25.GroupAlarmAction;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.MethodAction;
import com.vmware.vim25.MethodActionArgument;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;


public class AlarmManager {
	
	// Out of Box, not using
	static StateAlarmExpression createStateAlarmExpression()
	{
		StateAlarmExpression expression = 
				new StateAlarmExpression();
		expression.setType("VirtualMachine");
		expression.setStatePath("runtime.powerState");
		expression.setOperator(StateAlarmOperator.isEqual);
		expression.setRed("poweredOff");
		return expression;
	}
	
	// Added function to create alarm event for Power Off
	static EventAlarmExpression createEventAlarmExpression()
	{
		EventAlarmExpression expression = 
				new EventAlarmExpression();
		expression.setEventType("VmPoweredOffEvent");
		expression.setObjectType("VirtualMachine");;
		expression.setStatus(ManagedEntityStatus.yellow);
		return expression;
	}
	
	// Out of Box, not using
	static MethodAction createPowerOnAction() 
	{
		MethodAction action = new MethodAction();
		action.setName("PowerOnVM_Task");
		MethodActionArgument argument = new MethodActionArgument();
		argument.setValue(null);
		action.setArgument(new MethodActionArgument[] { argument });
		return action;
	}
	
	// Out of Box, not using
	static AlarmTriggeringAction createAlarmTriggerAction(
		      Action action) 
		  {
		    AlarmTriggeringAction alarmAction = 
		      new AlarmTriggeringAction();
		    alarmAction.setYellow2red(true);
		    alarmAction.setAction(action);
		    return alarmAction;
		  }
	
	public static boolean getAlarmStatus(String vmHostName, Properties prop) throws Exception{
		// Service Instance
		ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
				prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
		Folder rootFolder = si.getRootFolder();
		
		com.vmware.vim25.mo.AlarmManager alMgr = si.getAlarmManager();
		VirtualMachine virtualmachine;
		boolean alarmStatus = false;
		
		try {
			virtualmachine = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmHostName);
//			AlarmState[] aState = alMgr.getAlarmState(virtualmachine);
			AlarmState[] aState = virtualmachine.getTriggeredAlarmState();

			if (aState!=null) {
				for (AlarmState alarmState : aState) {
					System.out.println(alarmState.entity.val);
					if( alarmState.overallStatus.name().equals("yellow")){
						System.out.println("Alarm: User has switched off the VM");
						// Return true if graceful shutdown
						alarmStatus=true;
					 }	 
					}
			}
			}
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return alarmStatus;	
	}
	
	// Create an alarm
	public void createAlarm(String vmHostName, Properties prop) throws Exception{
		// Service Instance
		ServiceInstance si = new ServiceInstance(new URL(prop.getProperty("vCenter")), prop.getProperty("vCenterUsername"), 
				prop.getProperty("vCenterPassword"), Boolean.parseBoolean(prop.getProperty("vCenterIgnoreCert")));
		Folder rootFolder = si.getRootFolder();
		
		VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmHostName);

		com.vmware.vim25.mo.AlarmManager alarmMgr = si.getAlarmManager();
		AlarmSpec spec = new AlarmSpec();
		
		// Event alarm expression
		EventAlarmExpression expression = createEventAlarmExpression();
		AlarmAction methodAction = createAlarmTriggerAction(createPowerOnAction());
		GroupAlarmAction gaa = new GroupAlarmAction();

		gaa.setAction(new AlarmAction[]{ methodAction});
//		spec.setAction(gaa);
		spec.setExpression(expression);
		spec.setName("VmPowerOffEventAlarm-" + vmHostName);
		spec.setDescription("Monitor VM power off by user");
		spec.setEnabled(true);    

		AlarmSetting as = new AlarmSetting();
		as.setReportingFrequency(0); //as often as possible
		as.setToleranceRange(0);

		spec.setSetting(as);
		alarmMgr.createAlarm(vm, spec);
		
		// Closing connection
		si.getServerConnection().logout();
	}
}
