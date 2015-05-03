package uk.co.squadlist.web.views;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

@Component
public class DateFormatter {

	private static DateTimeZone timeZone = DateTimeZone.forID("Europe/London");	// TODO needs to be aware of instance timezone
	
	private uk.co.eelpieconsulting.common.dates.DateFormatter dateFormatter;
	
	public DateFormatter() {
		this.dateFormatter = new uk.co.eelpieconsulting.common.dates.DateFormatter(timeZone);
	}
	
	public String timeSince(Date date) {
		return dateFormatter.timeSince(date);
	}
	
	public String month(String month) {
		final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);
		return monthDateTime.toString("MMM yyyy");
	}
	
	public String dayMonthTime(Date date) {
		return new DateTime(date, timeZone).toString("EEE dd MMM kk:mm");
	}

	public String dayMonthYear(Date date) {
		return dateFormatter.dayMonthYear(date);
	}

	public String dayMonthYearTime(Date date) {
		return dateFormatter.dayMonthYearTime(date);
	}

	public String fullMonthYear(Date date) {
		return fullMonthYear(date);
	}

}
