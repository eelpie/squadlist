package uk.co.squadlist.web.views;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class DateHelper {

	public static List<String> getDays() {
		final List<String> days = Lists.newArrayList();
		for (int i = 1; i <= 31; i++) {
			days.add(Integer.toString(i));
		}
		return days;
	}
	
	public static List<String> getYears() {
		return listYears(DateTime.now().getYearOfEra(), 3);
	}

	public static List<String> getDateOfBirthYears() {
		return listYears(DateTime.now().getYearOfEra() - 100, 100);
	}
	
	public static Map<Integer, String> getMonths() {
		final Map<Integer, String> months = Maps.newTreeMap();
		for (int i = 1; i <= 12; i++) {		
			months.put(i, new DateTime(1970, i, 1, 0, 0, 0).toString("MMM"));
		}
		return months;
	}
	
	public static List<String> getHours() {
		return IntStream.rangeClosed(1, 23).boxed().map(i -> i.toString()).collect(Collectors.toList());
	}
	
	public static List<String> getMinutes() {
		final List<String> minutes = Lists.newArrayList();
		for (int i = 0; i < 60; i = i + 15) {
			minutes.add(Integer.toString(i));
		}
		return minutes;
	}

	public static LocalDateTime defaultOutingStartDateTime() {
		return new DateTime(DateTime.now().toDateMidnight()).plusDays(1).plusHours(8).toLocalDateTime();
	}

	public static DateTime startOfCurrentOutingPeriod() {
		return midnightYesterday().toDateTime();
	}
	
	public static DateTime oneYearFromNow() {
		return DateTime.now().plusYears(1);
	}
	
	public static DateTime endOfCurrentOutingPeriod() {
		return startOfCurrentOutingPeriod().plusWeeks(2);
	}
	
	private static DateMidnight midnightYesterday() {
		return DateTime.now().minusDays(1).toDateMidnight();
	}
	
	private static List<String> listYears(int firstYear, int numberToShow) {
		final List<String> years = Lists.newArrayList();
		for (int i = 0; i < numberToShow; i++) {
			years.add(Integer.toString(firstYear + i));
		}
		return years;
	}
	
}