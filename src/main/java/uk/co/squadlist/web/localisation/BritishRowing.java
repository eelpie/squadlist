package uk.co.squadlist.web.localisation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.joda.time.format.ISODateTimeFormat;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BritishRowing extends BaseGoverningBody implements GoverningBody {

	private static final Pattern VALID_REGISTRATION_NUMBER = Pattern.compile("\\d{6}[G|S|J|U|C|B|P]\\d{7}", Pattern.CASE_INSENSITIVE);	// TODO check with BR about valid codes
	private static final List<String> POINTS_OPTIONS = Lists.newArrayList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");

	private final Map<String, Integer> statusMaximumPoints;
	private final Map<String, Integer> mastersMinimumAges;

	public BritishRowing() {
		statusMaximumPoints = Maps.newLinkedHashMap();
		statusMaximumPoints.put("Novice", 0);
		statusMaximumPoints.put("Intermediate 3", 2);
		statusMaximumPoints.put("Intermediate 2", 4);
		statusMaximumPoints.put("Intermediate 1", 6);
		statusMaximumPoints.put("Senior", 9);
		statusMaximumPoints.put("Elite", null);

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
	public Integer getTotalPoints(List<String> rowingPoints) {
		Integer totalPoints = null;
		for (String points : rowingPoints) {
			if (!Strings.isNullOrEmpty(points)) {
				if (totalPoints == null) {
					totalPoints = 0;
				}
				totalPoints = totalPoints + parsePoints(points);
			} else {
				return null;
			}
		}
		return totalPoints;
	}

	@Override
	public String getRowingStatus(List<String> rowingPoints) {
		final Integer totalPoints = getTotalPoints(rowingPoints);
		if (totalPoints == null) {
			return null;
		}

		final int crewSize = rowingPoints.size();
		return determineStatusFromCurrentPoints(totalPoints, crewSize);
	}

	@Override
	public String getScullingStatus(List<String> scullingPoints) {
		return getRowingStatus(scullingPoints);
	}

	@Override
	public String getEffectiveAgeDescription() {
		return "Effective age is measured in whole years attained during the current calendar year.\n" +
				"ie. if you are 40 on 1 June 2010 then you would be deemed to be 40 (or Masters B) for all events between 1 Jan and 31 Dec 2010.";
	}

	@Override
	public int getEffectiveAge(DateTime dateOfBirth) {
		final LocalDate localDateOfBirth = new LocalDate(dateOfBirth);
		final LocalDate localEndOfYear = new LocalDate(new DateTime().withDate(DateTime.now().getYear(), 12, 31).toDate());

		final Years age = Years.yearsBetween(localDateOfBirth, localEndOfYear);
		return age.getYears();
	}

	@Override
	public String getAgeGrade(int age) {
		for (String mastersGrade : mastersMinimumAges.keySet()) {
			if (age >= mastersMinimumAges.get(mastersGrade)) {
				return mastersGrade;
			}
		}

		return null;
	}

	@Override
	public String getAgeGrade(DateTime dateOfBirth) {
		return getAgeGrade(getEffectiveAge(dateOfBirth));
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

	@Override
	public String getStatusPointsReference() {
		return "http://www.britishrowing.org/competing/points-status";
	}

	@Override
	public Integer getEffectiveAge(List<DateTime> datesOfBirth) {
		Integer youngestAge = null;
		for (DateTime dateOfBirth : datesOfBirth) {
			if (dateOfBirth == null) {
				return null;
			}

			int effectiveAge = getEffectiveAge(dateOfBirth);
			if (youngestAge == null || effectiveAge < youngestAge) {
				youngestAge = effectiveAge;
			}
		}
		return youngestAge;
	}

	@Override
	public String getRowingStatus(String rowingPoints) {
		return getRowingStatus(Lists.newArrayList(rowingPoints));
	}

	@Override
	public String getScullingStatus(String scullingPoints) {
		return getScullingStatus(Lists.newArrayList(scullingPoints));
	}

	private String determineStatusFromCurrentPoints(final int p, int crewSize) {
		for (String status : statusMaximumPoints.keySet()) {
			Integer maxPointsForStatus = statusMaximumPoints.get(status);
			if (maxPointsForStatus == null) {
				return status;
			}
			final int maxPointsForCrewAtThisStatus = maxPointsForStatus * crewSize;
			if (p <= maxPointsForCrewAtThisStatus) {
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