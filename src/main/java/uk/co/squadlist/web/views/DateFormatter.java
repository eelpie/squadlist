package uk.co.squadlist.web.views;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.Context;

@Component
public class DateFormatter {
	
	private final Context context;
	
	@Autowired
	public DateFormatter(Context context) {
		this.context = context;
	}
	
	public String timeSince(Date date) {
		return getDateFormatter().timeSince(date);
	}
	
	public String month(String month) {
		final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);
		return monthDateTime.toString("MMM yyyy");
	}
	
	public String dayMonthTime(Date date) {
		return new DateTime(date, DateTimeZone.forID(context.getTimeZone())).toString("EEE dd MMM kk:mm");
	}
	
	public String dayMonthYear(Date date) {
		return getDateFormatter().dayMonthYear(date);
	}

	public String dayMonthYearTime(Date date) {
		return getDateFormatter().dayMonthYearTime(date);
	}

	public String fullMonthYear(Date date) {
		return getDateFormatter().fullMonthYear(date);
	}
	
	private uk.co.eelpieconsulting.common.dates.DateFormatter getDateFormatter() {
		return new uk.co.eelpieconsulting.common.dates.DateFormatter(context.getTimeZone());
	}
	
}