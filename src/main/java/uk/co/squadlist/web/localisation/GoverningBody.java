package uk.co.squadlist.web.localisation;

import java.util.Date;
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

	public int getEffectiveAge(Date dateOfBirth);
	public String getEffectiveAgeDescription();

	public String getAgeGrade(int age);
	public String getAgeGrade(Date dateOfBirth);

	public String checkRegistrationNumber(String registrationNumber);
	public String getStatusPointsReference();
	public Integer getEffectiveAge(List<Date> datesOfBirth);
	public List<Integer> getBoatSizes();
	public Map<Integer, String> getWeights();

}
