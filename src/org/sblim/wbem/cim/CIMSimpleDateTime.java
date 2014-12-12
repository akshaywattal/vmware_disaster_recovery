/**
 * CIMSimpleDateTime.java
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
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1960934    2008-05-09  blaschke-oss CIMDateTime(Calendar) does not respect DST
 * 1960994    2008-05-09  blaschke-oss CIMSimpleDateTime.setDay() TODO and bad exception info
 * 2023050    2008-07-23  raman_arora  DateTime object incorrect for microseconds
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.cim;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CIMSimpleDateTime extends CIMDateTime {

	private static final long serialVersionUID = -5855212374146014403L;

	/**
	 * Constructs an CIMDataTime used to define a point in time.
	 * 
	 */
	public CIMSimpleDateTime() {
		iDateString = "00000000000000.000000-000";
		iInterval = false;
		iPrecision = FULL_PRECISION;
	}

	/**
	 * Constructs a CIMSimpleDateTime object from the specified String defining
	 * an date.
	 * 
	 * @param pDateString
	 *            A date string
	 */
	public CIMSimpleDateTime(String pDateString) {
		if (pDateString.indexOf(':') > -1) throw new IllegalArgumentException(
				"simple date time must not be used to define intervals: " + pDateString);
		iInterval = false;
		valueOf(this, pDateString);
	}

	/**
	 * Constructs a CIMSimpleDateTime object from the given Calendar object.
	 * 
	 * @param pCalendar
	 *            A Calendar object
	 */
	public CIMSimpleDateTime(Calendar pCalendar) {
		if (pCalendar == null) throw new IllegalArgumentException("null calendar argument");
		Calendar calendar = (Calendar) pCalendar.clone();

		if (calendar.getTimeZone().inDaylightTime(calendar.getTime())) {
			iOffsetMinute = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / 60000;
		} else {
			iOffsetMinute = calendar.get(Calendar.ZONE_OFFSET) / 60000;
		}
		iYear = calendar.get(Calendar.YEAR);
		iMonth = calendar.get(Calendar.MONTH) - calendar.getMinimum(Calendar.MONTH) + 1;
		iDay = calendar.get(Calendar.DAY_OF_MONTH) - calendar.getMinimum(Calendar.DAY_OF_MONTH) + 1;
		iHour = calendar.get(Calendar.HOUR_OF_DAY) - calendar.getMinimum(Calendar.HOUR_OF_DAY);

		iMinute = calendar.get(Calendar.MINUTE) - calendar.getMinimum(Calendar.MINUTE);

		iSecond = calendar.get(Calendar.SECOND) - calendar.getMinimum(Calendar.SECOND);
		iMillisecond = calendar.get(Calendar.MILLISECOND)
				- calendar.getMinimum(Calendar.MILLISECOND);

		iPrecision = FULL_PRECISION;
	}

	/**
	 * Returns the month specified by this CIMDateTime.
	 * 
	 * @return The month
	 */
	public int getMonth() {
		return iMonth;
	}

	/**
	 * Returns the year specified by this CIMDateTime.
	 * 
	 * @return The year
	 */
	public int getYear() {
		return iYear;
	}

	/**
	 * Specifies the day of this CIMDateTime.
	 * 
	 * @param pDay
	 *            The day
	 */
	public void setDay(int pDay) {
		int maxday;
		switch (getMonth()) {
			case 2: /* February */
				GregorianCalendar gc = new GregorianCalendar();
				maxday = gc.isLeapYear(getYear()) ? 29 : 28;
				break;
				
			case 4: /* April */
			case 6: /* June */
			case 9: /* September */
			case 11: /* November */
				maxday = 30;
				break;
				
			default:
				maxday = 31;
				break;
		}
		if (pDay < 1 || pDay > maxday) throw new IllegalArgumentException(
				"invalid value of day [1-" + maxday + "] for month " + getMonth() + ":" + pDay);
		iCalendar = null;
		iDay = pDay;
	}

	/**
	 * Specifies the month for this CIMDateTime.
	 * 
	 * @param pMonth
	 *            The month
	 */
	public void setMonth(int pMonth) {
		if (pMonth < 1 || pMonth > 12) throw new IllegalArgumentException(
				"invalid value of month [1-12]:" + pMonth);
		iCalendar = null;
		iMonth = pMonth;
	}

	/**
	 * Specifies the year for this CIMDateTime.
	 * 
	 * @param pYear
	 *            The year
	 */
	public void setYear(int pYear) {
		iCalendar = null;
		iYear = pYear;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sblim.wbem.cim.CIMDateTime#clone()
	 */
	public Object clone() {
		return super.clone(new CIMSimpleDateTime());
	}

	public static void main(String[] args) {
		// Calendar cal = Calendar.getInstance();
		CIMSimpleDateTime date = new CIMSimpleDateTime("20040405130342.000000+060");
		System.out.println(date);
		System.out.println(date.getCalendar());
	}
}
