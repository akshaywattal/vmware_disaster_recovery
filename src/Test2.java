import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.HostConfigInfo;
import com.vmware.vim25.HostHardwareSummary;
import com.vmware.vim25.HostListSummary;
import com.vmware.vim25.HostListSummaryQuickStats;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfEntityMetricCSV;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfMetricSeriesCSV;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;


public class Test2 {

	public static void main(String[] args) {
		try {
			ServiceInstance si = new ServiceInstance(new URL("https://130.65.132.130/sdk"), "administrator", "12!@qwQW",true);
	        Folder rootFolder = si.getRootFolder();
	            
		    PerformanceManager perfMgr1 = si.getPerformanceManager();
		    
		    
	        ManagedEntity[] mehost = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
	        for (int i = 0; i < mehost.length; i++) {
	        	
	        	PerfProviderSummary pps = perfMgr1.queryPerfProviderSummary(mehost[i]);
	        	Integer counterId = 2;
	        	
	        	// set cpu metric
	    		PerfMetricId metricId = new PerfMetricId();
	    		metricId.setCounterId(counterId);
	    		metricId.setInstance("*");
	    		
	        	int refreshRate = pps.getRefreshRate();
	        	Calendar endTime = Calendar.getInstance();
	    		Calendar startTime = (Calendar) endTime.clone();
	    		startTime.add(Calendar.SECOND, -20);
	    		
	    		PerfQuerySpec qSpec = new PerfQuerySpec();
	    		qSpec.setEntity(mehost[i].getMOR());
	    		qSpec.setMetricId(new PerfMetricId[] {metricId});
	    		qSpec.setFormat("csv");
	    		qSpec.setIntervalId(new Integer(refreshRate));
	    		// set the time from which statistics are to be retrieved
	    		qSpec.setStartTime(startTime);
	    		qSpec.setEndTime(endTime);
	    		
	    		PerfEntityMetricBase[] pValues = perfMgr1
	    				.queryPerf(new PerfQuerySpec[] { qSpec });
	    		int total = 0;
	    		int n = 0;
	    		int avg = 0;
	    		if (pValues != null) {
	    			for (PerfEntityMetricBase pValue : pValues) {
	    				PerfEntityMetricCSV pem = (PerfEntityMetricCSV) pValue;
	    				PerfMetricSeriesCSV[] csvs = pem.getValue();
	    				for (PerfMetricSeriesCSV csv : csvs) {
	    					//System.out.println(csv.getValue());
	    					String[] samples = csv.getValue().split(",");
	    					
	    					for (String s: samples) {
	    						total += Integer.parseInt(s);
	    						n++;
	    					}
	    				}
	    			}
	    			avg = total/n;
	    		}
	    		System.out.println(avg);
	        	

	        	
	        }
		    


			}
			catch (Exception e){ e.printStackTrace();}

	}

}
