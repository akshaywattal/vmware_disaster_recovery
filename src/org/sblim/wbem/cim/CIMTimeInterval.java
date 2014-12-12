/**
 * CIMTimeInterval.java
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
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 * 1535756    2006-08-08  lupusalex    Make code warning free
 * 2023050    2008-07-23  raman_arora  DateTime object incorrect for microseconds
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.cim;

public class CIMTimeInterval extends CIMDateTime {
	
	private static final long serialVersionUID = -5884792620447182579L;


	/**
	 * Construct an object of a CIMTimeInterval.
	 */
	public CIMTimeInterval() {
		iDateString= "00000000000000.000000:000";
		iInterval = true;
		iPrecision = FULL_PRECISION;
	}
	
	/**
	 * Constructs an object of CIMTimeInterval for a given String that defines a valid interval. 
	 * @param pTimeString The time interval string
	 */
	public CIMTimeInterval(String pTimeString) {
		iInterval = true;
		if (pTimeString.indexOf('+') > -1 || pTimeString.indexOf('-') > -1) throw new IllegalArgumentException("CIMTimeInterval must not be used to define points in time: "+pTimeString);
		valueOf(this, pTimeString);
	}
	
	/**
	 * Specifies the number of days associated with this interval of time.
	 * @param pDay A valid day of month (1<=pDay<=31)
	 */
	public void setDay(int pDay) {
		if (pDay < 1 || pDay > 31) throw new IllegalArgumentException("invalid value of offset minute:"+pDay);
		
		//TODO validate the day according to the month and year
		iCalendar = null;
		iDay = pDay;
	}

	protected static String convertSecondsToCIMInterval(int intervalInSeconds) {
		int seconds = intervalInSeconds % 60;
		String secondsStr = (seconds<10 ? "0" : "")+seconds;
		int minutes = (intervalInSeconds/60) % 60;
		String minutesStr = (minutes<10 ? "0" : "")+minutes;
		int hours = (intervalInSeconds/3600) % 24;
		String hoursStr = (hours<10 ? "0" : "")+hours;
		return "00000000"+hoursStr+minutesStr+secondsStr+".******:000";
	}

	
	public static void main(String[] args) {
//		CIMTimeInterval interval ;//= new CIMTimeInterval("0000012312****.******:000");
		System.out.println( new CIMTimeInterval("00000000000000.000000:000"));
		System.out.println( new CIMTimeInterval("12345678901234.567890:000"));
		System.out.println( new CIMTimeInterval("12345678901234.56789*:000"));
		System.out.println( new CIMTimeInterval("12345678901234.5678**:000"));
		System.out.println( new CIMTimeInterval("12345678901234.567***:000"));
		System.out.println( new CIMTimeInterval("12345678901234.56****:000"));
		System.out.println( new CIMTimeInterval("12345678901234.5*****:000"));
		System.out.println( new CIMTimeInterval("12345678901234.******:000"));
		System.out.println( new CIMTimeInterval("123456789012**.******:000"));
		System.out.println( new CIMTimeInterval("1234567890****.******:000"));
		System.out.println( new CIMTimeInterval("12345678******.******:000"));
		System.out.println( new CIMTimeInterval("1234567*******.******:000"));
	}
}
