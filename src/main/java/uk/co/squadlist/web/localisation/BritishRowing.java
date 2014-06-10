package uk.co.squadlist.web.localisation;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class BritishRowing implements GoverningBody {

	private static final List<String> POINTS_OPTIONS = Lists.newArrayList("N", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");	// TODO N is deprecated - move to 0
	private final Map<String, Integer> statusMaximumPoints;
	
	public BritishRowing() {
		statusMaximumPoints = Maps.newLinkedHashMap();
		statusMaximumPoints.put("Novice", 0);
		statusMaximumPoints.put("Intermediate 3", 4);
		statusMaximumPoints.put("Intermediate 2", 8);
		statusMaximumPoints.put("Intermediate 1", 12);
		statusMaximumPoints.put("Senior", 18);
		statusMaximumPoints.put("Elite", Integer.MAX_VALUE);		
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
	
}
