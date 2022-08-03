package uk.co.squadlist.web.views;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class DateFormatter {

    private final DateTimeZone timeZone;

    @Autowired
    public DateFormatter(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public String timeSince(Date date) {
        return getDateFormatter().timeSince(date);
    }
    public String timeSince(DateTime dateTime) {
        return timeSince(dateTime.toDate());
    }

    public String month(String month) {
        final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);
        return monthDateTime.toString("MMM yyyy");
    }

    public String dayMonthTime(Date date) {
        return new DateTime(date, timeZone).toString("EEE dd MMM kk:mm");
    }
    public String dayMonthTime(DateTime dateTime) {
        return dayMonthTime(dateTime.toDate());
    }

    public String dayMonthYear(Date date) {
        return getDateFormatter().dayMonthYear(date);
    }

    public String dayMonthYear(DateTime dateTime) {
        return dayMonthYear(dateTime.toDate());
    }


    public String dayMonthYearTime(Date date) {
        return getDateFormatter().dayMonthYearTime(date);
    }
    public String dayMonthYearTime(DateTime dateTime) {
        return dayMonthYearTime(dateTime.toDate());
    }

    public String fullMonthYear(Date date) {
        return getDateFormatter().fullMonthYear(date);
    }

    private uk.co.eelpieconsulting.common.dates.DateFormatter getDateFormatter() {
        return new uk.co.eelpieconsulting.common.dates.DateFormatter(timeZone);
    }

}