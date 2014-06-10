package uk.co.squadlist.web.localisation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class BritishRowing implements GoverningBody {

	private static final List<String> POINTS_OPTIONS = Lists.newArrayList("N", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
	
	@Override
	public String getName() {
		return "British Rowing";
	}
	
	public List<String> getPointsOptions() {
		return POINTS_OPTIONS;
	}

}
