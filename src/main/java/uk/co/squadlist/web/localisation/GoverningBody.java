package uk.co.squadlist.web.localisation;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public interface GoverningBody {

	public String getName();
	public List<String> getPointsOptions();

	public Integer getTotalPoints(List<String> rowingPoints);

	public String getRowingStatus(String rowingPoints);
	public String getRowingStatus(List<String> rowingPoints);

	public String getScullingStatus(String scullingPoints);
	public String getScullingStatus(List<String> scullingPoints);

	public int getEffectiveAge(DateTime dateOfBirth);
	public String getEffectiveAgeDescription();

	public String getAgeGrade(int age);
	public String getAgeGrade(DateTime dateOfBirth);

	public String checkRegistrationNumber(String registrationNumber);
	public String getStatusPointsReference();
	public Integer getEffectiveAge(List<DateTime> datesOfBirth);
	public List<Integer> getBoatSizes();
	public Map<Integer, String> getWeights();

	public Map<String, Integer> getStatusPoints();
	public Map<String, Integer> getAgeGrades();

}
