package uk.co.squadlist.web.views;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

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
		final List<String> years = Lists.newArrayList();
		final int yearOfEra = DateTime.now().getYearOfEra();
		for (int i = 0; i < 3; i++) {
			years.add(Integer.toString(yearOfEra + i));
		}
		return years;
	}
	
	public static List<String> getMonths() {
		final List<String> days = Lists.newArrayList();
		for (int i = 1; i <= 12; i++) {
			DateTime month = new DateTime(1970, i, 1, 0, 0, 0);
			//days.add(month.toString("MMM"));
			days.add(Integer.toString(i));
		}
		return days;
	}
	
	public static List<String> getHours() {
		final List<String> hours = Lists.newArrayList();
		for (int i = 1; i <= 12; i = i + 1) {
			hours.add(Integer.toString(i));
		}
		return hours;
	}
	
	public static List<String> getMinutes() {
		final List<String> minutes = Lists.newArrayList();
		for (int i = 0; i < 60; i = i + 15) {
			minutes.add(Integer.toString(i));
		}
		return minutes;
	}
	
}
