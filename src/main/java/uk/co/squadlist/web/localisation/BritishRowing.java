package uk.co.squadlist.web.localisation;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class BritishRowing implements GoverningBody {

	private static final Pattern VALID_REGISTRATION_NUMBER = Pattern.compile("\\d{6}[G|S|J|U|C|B]\\d{7}", Pattern.CASE_INSENSITIVE);	// TODO check with BR about valid codes
	private static final List<String> POINTS_OPTIONS = Lists.newArrayList("N", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");	// TODO N is deprecated - move to 0
	
	private final Map<String, Integer> statusMaximumPoints;
	private final Map<String, Integer> mastersMinimumAges;

	public BritishRowing() {
		statusMaximumPoints = Maps.newLinkedHashMap();
		statusMaximumPoints.put("Novice", 0);
		statusMaximumPoints.put("Intermediate 3", 4);
		statusMaximumPoints.put("Intermediate 2", 8);
		statusMaximumPoints.put("Intermediate 1", 12);
		statusMaximumPoints.put("Senior", 18);
		statusMaximumPoints.put("Elite", Integer.MAX_VALUE);
		
		mastersMinimumAges = Maps.newLinkedHashMap();
		mastersMinimumAges.put("J", 80);
		mastersMinimumAges.put("I", 75);
		mastersMinimumAges.put("H", 70);
		mastersMinimumAges.put("G", 65);
		mastersMinimumAges.put("F", 60);
		mastersMinimumAges.put("E", 55);
		mastersMinimumAges.put("D", 50);
		mastersMinimumAges.put("C", 43);
		mastersMinimumAges.put("B", 36);
		mastersMinimumAges.put("A", 27);
	}
	
	@Override
	public String getName() {
		return "British Rowing";
	}
	
	public List<String> getPointsOptions() {
		return POINTS_OPTIONS;
	}

	@Override
	public String getRowingStatus(String rowingPoints) {		
		return determineStatusFromCurrentPoints(parsePoints(rowingPoints));
	}
	
	@Override
	public String getScullingStatus(String scullingPoints) {
		return determineStatusFromCurrentPoints(parsePoints(scullingPoints));
	}
	
	@Override
	public String getAgeGrade(Date dateOfBirth) {
		final LocalDate localDateOfBirth = new LocalDate(dateOfBirth);
		final Years age = Years.yearsBetween(localDateOfBirth, LocalDate.now());	// TODO push to date formatter?
		
		for (String mastersGrade : mastersMinimumAges.keySet()) {
			if (age.getYears() >= mastersMinimumAges.get(mastersGrade)) {
				return "Masters " + mastersGrade;
			}
		}
		
		return null;
	}
	
	private String determineStatusFromCurrentPoints(final int p) {
		for (String status : statusMaximumPoints.keySet()) {
			if (p <= statusMaximumPoints.get(status)) {
				return status;
			}
 		}
		return null;
	}
	
	private int parsePoints(String points) {
		if (Strings.isNullOrEmpty(points)) {
			return 0;
		}
		if (points.equals("N")) {	// TODO N is deprecated
			return 0;
		}
		return Integer.parseInt(points);
	}

	@Override
	public String checkRegistrationNumber(String registrationNumber) {
		if (Strings.isNullOrEmpty(registrationNumber)) {
			return null;
		}
		
		if (VALID_REGISTRATION_NUMBER.matcher(registrationNumber).matches()) {			
			final String dateString = registrationNumber.substring(0, 6);
			
			final DateTime parse = ISODateTimeFormat.basicDate().parseDateTime(dateString + "01");			
			final DateTime endOfRegistrationDate = parse.plusMonths(1);			
			if (endOfRegistrationDate.isBefore(DateTime.now())) {
				return "Expired registration";
			}			
			return null;
		}
		return "Not in the expected British Rowing format";	
	}
	
}
