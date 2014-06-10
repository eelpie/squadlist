package uk.co.squadlist.web.localisation;

import java.util.Date;
import java.util.List;

public interface GoverningBody {

	public String getName();
	public List<String> getPointsOptions();
	public String getRowingStatus(String points);
	public String getScullingStatus(String points);
	public String getAgeGrade(Date dateOfBirth);
	
}
