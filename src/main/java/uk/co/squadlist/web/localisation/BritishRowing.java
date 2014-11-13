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
		statusMaximumPoints.put("Intermediate 3", 2);
		statusMaximumPoints.put("Intermediate 2", 4);
		statusMaximumPoints.put("Intermediate 1", 6);
		statusMaximumPoints.put("Senior", 9);
		statusMaximumPoints.put("Elite", Integer.MAX_VALUE);
		
		mastersMinimumAges = Maps.newLinkedHashMap();
		mastersMinimumAges.put("Masters J", 80);
		mastersMinimumAges.put("Masters I", 75);
		mastersMinimumAges.put("Masters H", 70);
		mastersMinimumAges.put("Masters G", 65);
		mastersMinimumAges.put("Masters F", 60);
		mastersMinimumAges.put("Masters E", 55);
		mastersMinimumAges.put("Masters D", 50);
		mastersMinimumAges.put("Masters C", 43);
		mastersMinimumAges.put("Masters B", 36);
		mastersMinimumAges.put("Masters A", 27);
	}
	
	@Override
	public String getName() {
		return "British Rowing";
	}
	
	public List<String> getPointsOptions() {
		return POINTS_OPTIONS;
	}
	
	public Map<String, Integer> getStatusPoints() {
		return statusMaximumPoints;
	}

	public Map<String, Integer> getAgeGrades() {
		return mastersMinimumAges;
	}

	@Override
	public String getRowingStatus(String rowingPoints) {		
		return determineStatusFromCurrentPoints(parsePoints(rowingPoints), 1);
	}
	
	@Override
	public String getRowingStatus(String totalRowingPoints, int crewSize) {
		return determineStatusFromCurrentPoints(parsePoints(totalRowingPoints), crewSize);
	}
	
	@Override
	public String getScullingStatus(String scullingPoints) {
		return determineStatusFromCurrentPoints(parsePoints(scullingPoints), 1);
	}
	
	@Override
	public String getScullingStatus(String totalScullingPoints, int crewSize) {
		return determineStatusFromCurrentPoints(parsePoints(totalScullingPoints), crewSize);
	}
	
	@Override
	public int getEffectiveAge(Date dateOfBirth) {
		final LocalDate localDateOfBirth = new LocalDate(dateOfBirth);		
		final LocalDate localEndOfYear = new LocalDate(new DateTime().withDate(DateTime.now().getYear(), 12, 31).toDate());
		
		final Years age = Years.yearsBetween(localDateOfBirth, localEndOfYear);
		return age.getYears();
	}
	
	@Override
	public String getAgeGrade(int age) {
		/* TODO The age restriction is the lower limit for the average age of the crew (excluding coxswain), 
		 * measured in whole years attained during the current "calendar" year 
		 * i.e. if you are 40 on 1 June 2010 then you would be deemed to be 40 (or Masters B) 
		 * for all events between 1 Jan and 31 Dec 2010.
		 */
		 
		for (String mastersGrade : mastersMinimumAges.keySet()) {
			if (age >= mastersMinimumAges.get(mastersGrade)) {
				return mastersGrade;
			}
		}
		
		return null;
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

	private String determineStatusFromCurrentPoints(final int p, int crewSize) {
		for (String status : statusMaximumPoints.keySet()) {
			if (p <= statusMaximumPoints.get(status) * crewSize) {
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

}