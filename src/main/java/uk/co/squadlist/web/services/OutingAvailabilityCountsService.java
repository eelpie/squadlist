package uk.co.squadlist.web.services;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicLongMap;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.model.AvailabilityOption;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.views.DateHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class OutingAvailabilityCountsService {

	public Map<String, Map<String, Long>> buildOutingAvailabilityCounts(List<OutingWithSquadAvailability> squadOutings) throws JsonParseException, JsonMappingException, HttpFetchException, IOException {
		final Map<String, Map<String, Long>> results = Maps.newHashMap();
		
		for (OutingWithSquadAvailability outingWithAvailability : squadOutings) {		
			final AtomicLongMap<String> counts = AtomicLongMap.create();			
			final Map<String, AvailabilityOption> membersAvailabilityForThisOuting = outingWithAvailability.getAvailability();
			for (AvailabilityOption availabilityOption : membersAvailabilityForThisOuting.values()) {
				if (availabilityOption != null && availabilityOption.getColour() != null) {	// Should the API persist these?
					counts.addAndGet(availabilityOption.getColour(), 1);
				}
			}
			results.put(outingWithAvailability.getOuting().getId(), counts.asMap());
		}
		return results;
	}

	public int getPendingOutingsCountFor(String memberId, SquadlistApi api) {
		int pendingCount = 0;
		for (OutingAvailability outingAvailability : api.getAvailabilityFor(memberId, 
				DateHelper.startOfCurrentOutingPeriod().toDate(), DateHelper.oneYearFromNow().toDate())) {
			if (outingAvailability.getAvailabilityOption() == null) {
				pendingCount++;
			}
		}		 
		return pendingCount;
	}
	
}
