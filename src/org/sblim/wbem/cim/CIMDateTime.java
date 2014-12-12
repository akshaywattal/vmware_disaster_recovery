/**
 * CIMDateTime.java
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
 * 1455939    2006/05/15  lupusalex    CIMDataTime does not handle microsecond
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1960934    2008-05-09  blaschke-oss CIMDateTime(Calendar) does not respect DST
 * 2023050    2008-07-23  raman_arora  DateTime object incorrect for microseconds
 * 2219646    2008-11-17  blaschke-oss Fix / clean code to remove compiler warnings
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
*/
 

package org.sblim.wbem.cim;

import java.io.Serializable;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sblim.wbem.util.SessionProperties;

public class CIMDateTime implements Serializable, Cloneable {

	private static final long serialVersionUID = -5756487333233101157L;

	protected String iDateString;

	protected Calendar iCalendar = null;

	protected boolean iInterval = false;

	protected int iYear;

	protected int iMonth;

	protected int iDay;

	protected int iHour;

	protected int iMinute;

	protected int iSecond;

	protected int iMillisecond;

	protected int iMicrosecond;

	protected int iOffsetMinute;

	protected int iPrecision = FULL_PRECISION;

	public static final int YEAR_PRECISION = 0;

	public static final int MONTH_PRECISION = 1;

	public static final int DAY_PRECISION = 2;

	public static final int HOUR_PRECISION = 3;

	public static final int MINUTE_PRECISION = 4;

	public static final int SECOND_PRECISION = 5;

	public static final int MILLI_ONE_DIGIT_PRECISION = 6;

	public static final int MILLI_TWO_DIGIT_PRECISION = 7;

	public static final int MILLI_THREE_DIGIT_PRECISION = 8;

	public static final int MILLI_FOUR_DIGIT_PRECISION = 9;

	public static final int MILLI_FIVE_DIGIT_PRECISION = 10;

	public static final int MILLI_SIX_DIGIT_PRECISION = 11;

	public static final int FULL_PRECISION = 12;

	/**
	 * Construct an object of a CIMDateTime given a calendar.
	 * 
	 * @param pCalendar
	 *            The calendar
	 * @deprecated this constructor was marked as deprecated because the dual
	 *             nature of this object. This object was used to represent
	 *             point in time, as well as intervals. Instead of this
	 *             constructor applications are encourage to use corresponding
	 *             objects such as CIMDateTime, or CIMSimpleDateTime.
	 */
	public CIMDateTime(Calendar pCalendar) {
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
	 * Creates an object of a CIMDateTime.
	 * 
	 * @param pDateString
	 *            A string representation of a datetime
	 * @deprecated this constructor was marked as deprecated because the dual
	 *             nature of this object. This object was used to represent
	 *             point in time, as well as intervals. Instead of this
	 *             constructor applications are encourage to use corresponding
	 *             objects such as CIMTimeInterval, or CIMSimpleDateTime.
	 */
	public CIMDateTime(String pDateString) {
		char sign = pDateString.charAt(21);
		if (sign == ':') iInterval = true;
		valueOf(this, pDateString);
	}

	protected CIMDateTime() {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return clone(new CIMDateTime());
	}

	protected CIMDateTime clone(CIMDateTime pClone) {
		pClone.iCalendar = iCalendar;
		pClone.iYear = iYear;
		pClone.iMonth = iMonth;
		pClone.iDay = iDay;
		pClone.iHour = iHour;
		pClone.iMinute = iMinute;
		pClone.iSecond = iSecond;
		pClone.iMillisecond = iMillisecond;
		pClone.iMicrosecond = iMicrosecond;
		pClone.iPrecision = iPrecision;
		return pClone;
	}

	/**
	 * Creates an CIMDateTime object from the specified String argument. This
	 * method returns a CIMSimpleDateTime or a CIMDateTime, depending on what
	 * type of CIMDataType is denoted by the string argument.
	 * 
	 * @param pDateString
	 *            The date string to parse
	 * @return a CIMDateTime or CIMSimpleDateTime object
	 */
	public static CIMDateTime valueOf(String pDateString) {
		CIMDateTime datetime = null;
		char sign = pDateString.charAt(21);

		if (sign == ':') {
			datetime = new CIMDateTime();
			datetime.iInterval = true;
		} else {
			datetime = new CIMSimpleDateTime();
		}
		return valueOf(datetime, pDateString);
	}

	protected static CIMDateTime valueOf(CIMDateTime pDatetime, String pDateString) {
		try {
			if (pDateString == null)
				throw new IllegalArgumentException("null calendar argument");
			
			char sign = pDateString.charAt(21);
			pDatetime.iDateString = pDateString; // keep the original

			// format yyyymmddhhmmss.mmmmmmsutc
			if (pDateString.length() != 25) throw new IllegalArgumentException(
					"Invalid length of cim date type: " + pDateString);

			// TODO make sure that the precision for each field
			if (!pDatetime.iInterval) {
				pDatetime.iPrecision = FULL_PRECISION;

				String _year = pDateString.substring(0, 4);
				if (_year.indexOf('*') > -1) pDatetime.iPrecision = YEAR_PRECISION;
				else pDatetime.iYear = Integer.parseInt(_year);

				String _month = pDateString.substring(4, 6);
				if (_month.indexOf('*') > -1) {
					if (pDatetime.iPrecision == FULL_PRECISION) pDatetime.iPrecision = MONTH_PRECISION;
				} else pDatetime.iMonth = Integer.parseInt(_month);

				String _day = pDateString.substring(6, 8);
				if (_day.indexOf('*') > -1) {
					if (pDatetime.iPrecision == FULL_PRECISION) pDatetime.iPrecision = DAY_PRECISION;
				} else pDatetime.iDay = Integer.parseInt(_day);
			} else {
				String _day = pDateString.substring(0, 8);
				if (_day.indexOf('*') > -1) {
					if (pDatetime.iPrecision == FULL_PRECISION) pDatetime.iPrecision = DAY_PRECISION;
				} else pDatetime.iDay = Integer.parseInt(_day);
			}

			String _hour = pDateString.substring(8, 10);
			if (_hour.indexOf('*') > -1) {
				if (pDatetime.iPrecision == FULL_PRECISION) pDatetime.iPrecision = HOUR_PRECISION;
			} else pDatetime.iHour = Integer.parseInt(_hour);

			String _minute = pDateString.substring(10, 12);
			if (_minute.indexOf('*') > -1) {
				if (pDatetime.iPrecision == FULL_PRECISION) pDatetime.iPrecision = MINUTE_PRECISION;
			} else pDatetime.iMinute = Integer.parseInt(_minute);

			String _second = pDateString.substring(12, 14);
			if (_second.indexOf('*') > -1) {
				if (pDatetime.iPrecision == FULL_PRECISION) pDatetime.iPrecision = SECOND_PRECISION;
			} else pDatetime.iSecond = Integer.parseInt(_second);

			String _microsecond = pDateString.substring(15, 21);
			int i = _microsecond.indexOf('*');
			if (i > -1) {
				if (pDatetime.iPrecision == FULL_PRECISION)
					pDatetime.iPrecision = i + MILLI_ONE_DIGIT_PRECISION;
				pDatetime.iMillisecond = Integer.parseInt(_microsecond.replace('*', '0')) / 1000;
				pDatetime.iMicrosecond = Integer.parseInt(_microsecond.replace('*', '0')) % 1000;
			} else {
				pDatetime.iMillisecond = Integer.parseInt(_microsecond) / 1000;
				pDatetime.iMicrosecond = Integer.parseInt(_microsecond) % 1000;
			}

			pDatetime.iOffsetMinute = Integer.parseInt(pDateString.substring(22, 25));
			if (sign != ':') {
				if (sign == '-') pDatetime.iOffsetMinute = -pDatetime.iOffsetMinute;
				else if (sign != '+') throw new IllegalArgumentException("Invalid cim date type:"
						+ pDateString);

			} else {
				if (pDatetime.iOffsetMinute > 0) throw new IllegalArgumentException(
						"Invalid offset minute of cim date time: " + pDateString);
			}
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			Logger logger = SessionProperties.getGlobalProperties().getLogger();
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "error while parsing CIMDateTime value: " + pDateString,
						e);
			}
			throw new IllegalArgumentException("Invalid cim date type: " + pDateString);
		}

		return pDatetime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {

		int hashvalue = 31;

		if (iInterval) return getCalendar().hashCode();

		hashvalue *= iDay;
		hashvalue *= iHour;
		hashvalue *= iMinute;
		hashvalue *= iHour;
		hashvalue *= iSecond;
		hashvalue *= iMillisecond;
		hashvalue *= iMicrosecond;
		hashvalue *= iPrecision;

		return hashvalue;
	}

	/**
	 * Determines if the current date time occurs after the date type from the
	 * specified object. Comparison of CIMSimpleDateTime and CIMIntervalTime are
	 * meaningless, for this reason always return false.
	 * 
	 * @param pThat
	 *            The datetime to compare with
	 * @return <code>true</code> if <code>this</code> occurs after
	 *         <code>pThat</code>, <code>false</code> otherwise
	 */
	public boolean after(CIMDateTime pThat) {
		if (pThat == null) throw new IllegalArgumentException("null calendar argument");
		if (iInterval || pThat.iInterval) return false;

		return getCalendar().after(pThat.getCalendar());
	}

	/**
	 * Determines if the current date time ocurrs after the date type from the
	 * specified object.
	 * 
	 * @param pThat
	 *            The datetime to compare with
	 * @return <code>true</code> if <code>this</code> occurs before
	 *         <code>pThat</code>, <code>false</code> otherwise
	 */

	public boolean before(CIMDateTime pThat) {
		if (pThat == null) throw new IllegalArgumentException("null calendar argument");
		if (iInterval || pThat.iInterval) return false;

		return getCalendar().before(pThat.getCalendar());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CIMDateTime)) return false;
		if (o == this) return true;
		CIMDateTime that = (CIMDateTime) o;
		if (iInterval == that.iInterval) {
			if (!iInterval) return this.getCalendar().equals(that.getCalendar());
			return (iDay == that.iDay && iHour == that.iHour
					&& iSecond == that.iSecond && iMillisecond == that.iMillisecond);
		}
		return false;
	}

	/**
	 * Determines if the current object is represents an interval.
	 * 
	 * @return returns true if the datetime is an interval, otherwise returns
	 *         false.
	 */
	public boolean isInterval() {
		return iInterval;
	}

	/**
	 * Returns a calendar representation of this object. If the object represent
	 * an interval, this method returns null.
	 * 
	 * @return The calendar representation
	 */
	public Calendar getCalendar() {
		if (iCalendar == null && !iInterval) {

			TimeZone timezone = TimeZone.getDefault();
			timezone.setRawOffset(iOffsetMinute * 60 * 1000);

			iCalendar = Calendar.getInstance(timezone);

			// calendar.setTimeZone(calendar.getTimeZone());

			iCalendar.set(iYear, (iMonth - 1) + iCalendar.getMinimum(Calendar.MONTH), (iDay - 1)
					+ iCalendar.getMinimum(Calendar.DAY_OF_MONTH), (iHour)
					+ iCalendar.getMinimum(Calendar.HOUR_OF_DAY), (iMinute)
					+ iCalendar.getMinimum(Calendar.MINUTE), (iSecond)
					+ iCalendar.getMinimum(Calendar.SECOND));
			iCalendar.set(Calendar.MILLISECOND, iMillisecond);

			// calendar.set(Calendar.DST_OFFSET, offsetminute*60*1000);
		}
		return (Calendar) iCalendar.clone();
	}

	/**
	 * Returns the MOF representation of this object.
	 * 
	 * @return The MOF representation
	 */
	public String toMOF() {
		StringBuffer buf = new StringBuffer();
		if (!iInterval) {
			if (isYearPrecision()) buf.append(zeroPadding(iYear, 4));
			else buf.append(starPadding(4));

			if (isMonthPrecision()) buf.append(zeroPadding(iMonth, 2));
			else buf.append(starPadding(2));

			if (isDayPrecision()) buf.append(zeroPadding(iDay, 2));
			else buf.append(starPadding(2));
		} else {
			if (isDayPrecision()) buf.append(zeroPadding(iDay, 8));
			else buf.append(starPadding(8));
		}

		if (isHourPrecision()) buf.append(zeroPadding(iHour, 2));
		else buf.append(starPadding(2));

		if (isMinutePrecision()) buf.append(zeroPadding(iMinute, 2));
		else buf.append(starPadding(2));

		if (isSecondPrecision()) buf.append(zeroPadding(iSecond, 2));
		else buf.append(starPadding(2));

		buf.append('.');
		// as per DMTF the format is : yyyyMMddHHmmss.mmmmmmsutc 
		// mmmmmm = total number of microseconds
		int microsecodsPrecision = getMicrosecodsPrecision();
		if (microsecodsPrecision < 6) {
			// need to get total number of microseconds
			// make sure millisecond and/or microsecond string is 3 digit
			String millis = zeroPadding(iMillisecond, 3);
			String micros = zeroPadding(iMicrosecond, 3);
			int totalMicros = Integer.parseInt(millis + micros);
			for (int n = 0; n < 6 - microsecodsPrecision; n++) {
				totalMicros /= 10;
			}
			if (microsecodsPrecision > 0)
				buf.append(zeroPadding(totalMicros, microsecodsPrecision));
			buf.append(starPadding(6 - microsecodsPrecision));
		} else {
			buf.append(zeroPadding(iMillisecond, 3));
			buf.append(zeroPadding(iMicrosecond, 3));
		}

		if (iInterval) {
			buf.append(":000");
		} else {
			if (iOffsetMinute > 0) {
				buf.append('+');
				buf.append(zeroPadding(iOffsetMinute, 3));
			} else {
				buf.append('-');
				buf.append(zeroPadding(-iOffsetMinute, 3));
			}
		}

		return buf.toString();
	}

	protected String starPadding(int pDigits) {
		if (pDigits > 0) return "**********".substring(0, pDigits);
		return "";
	}

	protected String zeroPadding(int pValue, int pDigits) {
		String str = String.valueOf(pValue);
		if (pDigits - str.length() > 0) return "00000000000".substring(0, pDigits - str.length())
				+ str;
		return str;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return toMOF();
	}

	/**
	 * Gets the number of milliseconds specified for this CIMDateTime.
	 * 
	 * @return The milliseconds
	 */
	public int getMillisecond() {
		return iMillisecond;
	}

	/**
	 * Gets the number of microseconds specified for this CIMDateTime.
	 * 
	 * @return The microsenconds
	 */
	public int getMicrosecond() {
		return iMicrosecond;
	}

	/**
	 * Gets the number of minutes specified for this CIMDateTime.
	 * 
	 * @return The minutes
	 */
	public int getMinute() {
		return iMinute;
	}

	/**
	 * Gets the number of days specified for this CIMDateTime.
	 * 
	 * @return The day
	 */
	public int getDay() {
		return iDay;
	}

	/**
	 * Gets the number of hours specified for this CIMDateTime.
	 * 
	 * @return The hours
	 */
	public int getHour() {
		return iHour;
	}

	/**
	 * Gets the number of offset minutes specified for this CIMDateTime.
	 * 
	 * @return The offset minutes
	 */
	public int getOffsetMinute() {
		return iOffsetMinute;
	}

	/**
	 * Gets the number of seconds specified for this CIMDateTime.
	 * 
	 * @return The seconds
	 */
	public int getSeconds() {
		return iSecond;
	}

	/**
	 * Specifies the number of hours for this CIMDateTime.
	 * 
	 * @param pHour
	 *            The hour
	 */
	public void setHour(int pHour) {
		if (pHour < 0 || pHour > 23) throw new IllegalArgumentException(
				"invalid value of hour [1-24]:" + pHour);
		iCalendar = null;
		iHour = pHour;
	}

	/**
	 * Gets the number of milliseconds specified this CIMDateTime.
	 * 
	 * @param pMillisecond
	 *            The milliseconds
	 * 
	 */
	public void setMilliseconds(int pMillisecond) {
		if (pMillisecond < 0 || pMillisecond > 999) throw new IllegalArgumentException(
				"invalid value of millisecond [0-999]:" + pMillisecond);
		iCalendar = null;
		iMillisecond = pMillisecond;
	}

	/**
	 * Gets the number of microseconds specified this CIMDateTime.
	 * 
	 * @param pMicrosecond
	 *            The microsenconds
	 */
	public void setMicroseconds(int pMicrosecond) {
		if (pMicrosecond < 0 || pMicrosecond > 999) throw new IllegalArgumentException(
				"invalid value of microsecond [0-999]:" + pMicrosecond);
		iCalendar = null;
		iMicrosecond = pMicrosecond;
	}

	/**
	 * Specifies the number of minutes for this CIMDateTime.
	 * 
	 * @param pMinute
	 *            The minutes
	 */
	public void setMinute(int pMinute) {
		if (pMinute < 0 || pMinute > 59) throw new IllegalArgumentException(
				"invalid value of minute:" + pMinute);
		iCalendar = null;
		iMinute = pMinute;
	}

	/**
	 * Sets the number of offset minutes associated with this CIMDateTime.
	 * 
	 * @param pOffsetMinutes
	 *            an integer value which species the number of offset minutes.
	 * @throws IllegalArgumentException
	 *             if the number of minutes is out of a valid range (-999,999)
	 */
	public void setOffsetMinute(int pOffsetMinutes) {
		if (pOffsetMinutes < -999 || pOffsetMinutes > 999) throw new IllegalArgumentException(
				"invalid value of offset minute:" + pOffsetMinutes);
		iCalendar = null;
		iOffsetMinute = pOffsetMinutes;
	}

	/**
	 * Sets the number of seconds for this CIMDateTime.
	 * 
	 * @param pSecond
	 *            integer value which species the number of seconds.
	 * @throws IllegalArgumentException
	 *             if the number of seconds is out of a valid range.
	 */
	public void setSecond(int pSecond) {
		if (pSecond < 0 || pSecond > 59) throw new IllegalArgumentException(
				"invalid value of second:" + pSecond);
		iCalendar = null;
		iSecond = pSecond;
	}

	/**
	 * Determines if this CIMDateTime has day precision.
	 * 
	 * @return true if this CIMDateTime has day precision, otherwise returns
	 *         false.
	 */
	public boolean isDayPrecision() {
		return iPrecision > DAY_PRECISION;
	}

	/**
	 * Determines if this CIMDateTime has hour precision.
	 * 
	 * @return true if this CIMDateTime has hour precision, otherwise returns
	 *         false.
	 */
	public boolean isHourPrecision() {
		return iPrecision > HOUR_PRECISION;
	}

	/**
	 * Returns the number of precision digits defined for microseconds. For
	 * instance, "19981207102059.10****-000" has 2 digits of precision.
	 * "19981207102059.1*****-000" has 1 digit of precision, and
	 * "199812071020**.******-000" has 0 digits of precision.
	 * 
	 * @return an integer representing the number of precision digits for the
	 *         milliseconds.
	 */
	public int getMicrosecodsPrecision() {
		if (iPrecision > SECOND_PRECISION)
			return iPrecision - MILLI_ONE_DIGIT_PRECISION;
		return 0;
	}

	/**
	 * Determines if this CIMDateTime has minute precision.
	 * 
	 * @return true if this CIMDateTime has month precision, otherwise returns
	 *         false.
	 */
	public boolean isMinutePrecision() {
		return iPrecision > MINUTE_PRECISION;
	}

	/**
	 * Determines if this CIMDateTime has month precision.
	 * 
	 * @return true if this CIMDateTime has month precision, otherwise returns
	 *         false.
	 */
	public boolean isMonthPrecision() {
		return iPrecision > MONTH_PRECISION;
	}

	/**
	 * Determines if this CIMDateTime has minute precision.
	 * 
	 * @return true if this CIMDateTime has month precision, otherwise returns
	 *         false.
	 */
	public boolean isSecondPrecision() {
		return iPrecision > SECOND_PRECISION;
	}

	/**
	 * Determines if this CIMDateTime has year precision.
	 * 
	 * @return true if this CIMDateTime has year precision, otherwise returns
	 *         false.
	 */
	public boolean isYearPrecision() {
		return iPrecision > YEAR_PRECISION;
	}

	/**
	 * Specifies the precision level for this CIMDateTime. The default value for
	 * this object is FULL_PRECISION.
	 * 
	 * @param pPrecision
	 *            specifies the precision level for this object.
	 */
	public void setPrecision(int pPrecision) {
		if (pPrecision < 0 || pPrecision > FULL_PRECISION) throw new IllegalArgumentException(
				"precision argument out of range [0-11]:" + pPrecision);
		iPrecision = pPrecision;
	}

	/**
	 * Gets the precision level of this CIMDateTime.
	 * 
	 * @return an integer value representing the precision level for this
	 *         object.
	 */
	public int getPrecision() {
		return iPrecision;
	}

	public static void main(String[] args) {

		// CIMDateTime date = new CIMDateTime("199812101235**.******+000");
		// CIMDateTime date2 = new CIMDateTime("19980525133015.000000+000");
		// CIMDateTime date3 = new CIMDateTime("20040105130342.000000-000");
		// System.out.println("date3:"+date3+"\n");
		// Calendar cal = date3.getCalendar();
		// System.out.println("originalInstallDate with format="+
		// DateFormat.getTimeInstance(DateFormat.FULL
		// ).format(cal.getTime())+"\n");
		//
		// TimeZone timezone = TimeZone.getDefault();
		// timezone.setRawOffset(60*60*1000);
		//
		// cal.setTimeZone(timezone);
		// System.out.println("originalInstallDate with format="+
		// DateFormat.getTimeInstance(DateFormat.FULL
		// ).format(cal.getTime())+"\n");
		// CIMDateTime date5 = new CIMDateTime(cal);
		//		
		//		
		// System.out.println("date5:"+date5);
		// System.out.println("date5:"+date5.getCalendar());
		//		
		//		
		// System.out.println("date3:"+date3);
		// System.out.println("date3:"+date3.getCalendar());
		// CIMDateTime date4 = new CIMDateTime(date3.getCalendar());
		// System.out.println("date4:"+date4);
		// System.out.println("date4:"+date4.getCalendar());
		// // System.out.println(date2);
		// Calendar cal = date2.getCalendar();
		// System.out.println(cal);
		// date2 = new CIMDateTime(cal);
		// System.out.println(date2);
		// cal = date2.getCalendar();
		// System.out.println(cal);
		// date2 = new CIMDateTime(cal);
		// System.out.println(date2);
		// date2 = new CIMDateTime("20041123125809.000000+060");
		// CIMDateTime date = CIMDateTime.valueOf("199812101235**.******+000");
		// CIMDateTime date = new CIMDateTime(Calendar.getInstance());
		// Calendar cal = Calendar.getInstance();
		// CIMDateTime date = new CIMDateTime();
		// date.setYear(2004);
		// date.setDay(1);
		// date.setHour(8);
		// date.setMinute(8);
		// date.setSecond(59);
		// date.setMillisecond(123456);
		// date.setPrecision(MILLI_THREE_DIGIT_PRECISION);
		//		
		// date.setInterval(true);
		// System.out.println(date);
		// date.setInterval(false);
		// date.setPrecision(HOUR_PRECISION);
		// date.setOffsetMinute(100);
		// System.out.println(date2);
		// System.out.println(date2.getCalendar());
		// CIMDateTime("0000012312****.******:000");
		System.out.println(new CIMDateTime("12345678901234.567890:000"));
		System.out.println(new CIMDateTime("12345678901234.56789*:000"));
		System.out.println(new CIMDateTime("12345678901234.5678**:000"));
		System.out.println(new CIMDateTime("12345678901234.567***:000"));
		System.out.println(new CIMDateTime("12345678901234.56****:000"));
		System.out.println(new CIMDateTime("12345678901234.5*****:000"));
		System.out.println(new CIMDateTime("12345678901234.******:000"));
		System.out.println(new CIMDateTime("123456789012**.******:000"));
		System.out.println(new CIMDateTime("1234567890****.******:000"));
		System.out.println(new CIMDateTime("12345678******.******:000"));
		System.out.println(new CIMDateTime("1234**********.******+000"));
		System.out.println(new CIMDateTime("12*45678901234.567890+100"));
	}
}
