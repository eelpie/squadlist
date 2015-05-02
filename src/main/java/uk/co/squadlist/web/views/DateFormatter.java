package uk.co.squadlist.web.views;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

// TODO needs to be aware of instance timezone
@Component
public class DateFormatter extends uk.co.eelpieconsulting.common.dates.DateFormatter {

	private static DateTimeZone timeZone = DateTimeZone.forID("Europe/London");

	public static String month(String month) {
		final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);
		return monthDateTime.toString("MMM yyyy");
	}

	public static String dayMonthTime(Date date) {
		return new DateTime(date, timeZone).toString("EEE dd MMM kk:mm");
	}

}
