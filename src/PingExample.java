import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.tempuri.* ;

public class PingExample
{
   
    public static void main( String[] args ) throws IOException
    {
//       System.out.println( "Ping Host: " + "T01-VM01-Ubuntu02" ) ;
//       Service service = new Service();
//       ServiceSoap port = service.getServiceSoap(); 
//       String result = port.pingHost( "130.65.133.162" ) ;
//       System.out.println( "Ping Result: " + result ) ;]
    	
    	String hostIpAddress="180.65.132.182";
		String resultLine;
		String pingResult = " ";
		Boolean pingResponse = false;
		String pingCommand;
    	
		
		System.out.println("The IP address of VM is" + hostIpAddress);
		pingCommand = "ping " + hostIpAddress;
		
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
		if (hostIpAddress != null) 
		{
			if (pingResult.contains("100% loss")) 
			{
				System.out.println("The Virtual Machine is not responding!");
				pingResponse = false;
			} else 
			{
				System.out.println("The VM is live. Pinging on the IP: " + hostIpAddress);
				pingResponse = true;
			}
		}
		else
		{
			System.out.println("The IP of VM is:"+hostIpAddress);
			pingResponse=false;
		}
//    	 String ipAddress = "130.65.132.132";
//    	 InetAddress inet = InetAddress.getByName(ipAddress);
//
//    	    System.out.println("Sending Ping Request to " + ipAddress);
//    	    System.out.println(inet.isReachable(5000) ? "Host is reachable" : "Host is NOT reachable");

    }
}
