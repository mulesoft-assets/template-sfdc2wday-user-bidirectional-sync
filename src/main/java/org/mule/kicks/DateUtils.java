/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.kicks;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * The function of this class is provide date comparation an transformation
 * functionality.
 * 
 * @author damiansima
 */
public class DateUtils {

	/**
	 * The method will take any date and validate if it finish with "Z"
	 * indicating GMT 0 time zone. If so it will transform it to +00:00 offset.
	 * 
	 * If no it will return the same string.
	 * 
	 * @param date
	 *            string representing a date
	 * @return a string representing a date with the time zone with format
	 *         +HH:mm
	 */
	private static String reformatZuluTimeZoneToOffsetIfNecesary(String date) {
		String reformatedDate = "";

		if (date.charAt(date.length() - 1) == 'Z') {
			reformatedDate = date.substring(0, date.length() - 1);
			reformatedDate += "+00:00";
		} else {
			reformatedDate = date;
		}

		return reformatedDate;
	}

	/**
	 * Validate which date is older.
	 * 
	 * @param sfdcDate
	 *            a string representing a date
	 * @param workdayDate
	 *            a string representing a date
	 * @return true if the date A is after the date B
	 */
	public static boolean isAfter(String sfdcDate, String workdayDate) {
		Validate.notEmpty(sfdcDate, "The SFDC date should not be null or empty");
		Validate.notEmpty(workdayDate, "The Workday date should not be null or empty");

		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'");
		
		DateTime lastModifiedDateOfB = formatter.parseDateTime(workdayDate);

		return org.mule.templates.date.DateUtils.ISOStringDateToDateTime(sfdcDate).isAfter(lastModifiedDateOfB);
	}
}
