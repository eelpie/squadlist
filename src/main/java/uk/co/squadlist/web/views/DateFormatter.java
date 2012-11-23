package uk.co.squadlist.web.views;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

@Component
public class DateFormatter extends uk.co.eelpieconsulting.common.dates.DateFormatter {

	public static String month(String month) {
		final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);
		return monthDateTime.toString("MMM yyyy");
	}
	
}
