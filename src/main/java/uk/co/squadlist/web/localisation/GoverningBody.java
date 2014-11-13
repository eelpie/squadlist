package uk.co.squadlist.web.localisation;

import java.util.Date;
import java.util.List;

public interface GoverningBody {

	public String getName();
	public List<String> getPointsOptions();
	public String getRowingStatus(String points);
	public String getScullingStatus(String points);
	public int getEffectiveAge(Date dateOfBirth);
	public String getAgeGrade(int age);
	public String checkRegistrationNumber(String registrationNumber);
	public String getRowingStatus(String totalRowingPoints, int crewSize);
	public String getScullingStatus(String totalScullingPoints, int crewSize);
	public String getStatusPointsReference();
	public Integer getEffectiveAge(List<Date> datesOfBirth);
	
}
