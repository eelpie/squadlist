package uk.co.squadlist.web.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.AvailabilityOption;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.views.DateHelper;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicLongMap;

@Component
public class OutingAvailabilityCountsService {
	
	private final InstanceSpecificApiClient api;

	@Autowired
	public OutingAvailabilityCountsService(InstanceSpecificApiClient api) {
		this.api = api;
	}

	public Map<String, Map<String, Long>> buildOutingAvailabilityCounts(List<OutingWithSquadAvailability> squadOutings) throws JsonParseException, JsonMappingException, HttpFetchException, IOException {
		final Map<String, Map<String, Long>> results = Maps.newHashMap();
		
		for (OutingWithSquadAvailability outingWithAvailability : squadOutings) {		
			final AtomicLongMap<String> counts = AtomicLongMap.create();			
			final Map<String, AvailabilityOption> membersAvailabilityForThisOuting = outingWithAvailability.getAvailability();
			for (AvailabilityOption availabilityOption : membersAvailabilityForThisOuting.values()) {
				if (availabilityOption != null) {	// Should the API persist these?
					counts.addAndGet(availabilityOption.getColour(), 1);
				}
			}
			results.put(outingWithAvailability.getOuting().getId(), counts.asMap());
		}
		return results;
	}

	public int getPendingOutingsCountFor(String loggedInUser) {
		int pendingCount = 0;
		for (OutingAvailability outingAvailability : api.getAvailabilityFor(loggedInUser, 
				DateHelper.startOfCurrentOutingPeriod().toDate(), DateHelper.oneYearFromNow().toDate())) {
			if (outingAvailability.getAvailability() == null) {
				pendingCount++;
			}
		}		 
		return pendingCount;
	}
	
}
