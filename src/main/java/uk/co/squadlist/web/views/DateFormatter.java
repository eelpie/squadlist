package uk.co.squadlist.web.views;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;

@Component
public class DateFormatter {

	private static DateTimeZone timeZone = DateTimeZone.forID("Europe/London");	// TODO needs to be aware of instance timezone
	
	private final InstanceSpecificApiClient api;
	
	@Autowired
	public DateFormatter(InstanceSpecificApiClient api) {
		this.api = api;
	}
	
	public String timeSince(Date date) {
		return getDateFormatter().timeSince(date);
	}
	
	public String month(String month) {
		final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);
		return monthDateTime.toString("MMM yyyy");
	}
	
	public String dayMonthTime(Date date) {
		return new DateTime(date, timeZone).toString("EEE dd MMM kk:mm");
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
		try {
			return new uk.co.eelpieconsulting.common.dates.DateFormatter(api.getInstance().getTimeZone());
		} catch (UnknownInstanceException e) {
			throw new RuntimeException(e);
		}
	}

}