package uk.co.squadlist.web.services;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.AvailabilityOption;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;

import com.google.common.collect.Maps;

@Component
public class OutingAvailabilityCountsService {
	
	private final InstanceSpecificApiClient api;
	
	@Autowired
	public OutingAvailabilityCountsService(InstanceSpecificApiClient api) {
		this.api = api;
	}
	
	public Map<String, Map<String, Integer>> buildOutingAvailabilityCounts(final Squad squad, Date startDate, Date endDate) throws JsonParseException, JsonMappingException, HttpFetchException, IOException {
		final Map<String, Map<String, Integer>> results = Maps.newHashMap();

		final List<AvailabilityOption> availabilityOptions = api.getAvailabilityOptions();
		final List<OutingWithSquadAvailability> squadOutings = api.getSquadAvailability(squad.getId(), startDate, endDate);
		for (OutingWithSquadAvailability outingWithAvailability : squadOutings) {	// TODO Someone (the API) needs to filter non current squad members out.
			final Map<String, Integer> counts = Maps.newTreeMap();			
			for(AvailabilityOption availabilityOption : availabilityOptions) {
				counts.put(availabilityOption.getColour(), 0);	// TODO there's a Guava map which doesn't need this
			}
			
			final Map<String, AvailabilityOption> membersAvailabilityForThisOuting = outingWithAvailability.getAvailability();
			for (AvailabilityOption availabilityOption : membersAvailabilityForThisOuting.values()) {
				if (availabilityOption != null) {	// TODO should api have explict nulls?
					int count = counts.get(availabilityOption.getColour());
					count = count + 1;
					counts.put(availabilityOption.getColour(), count);
				}
			}
			results.put(outingWithAvailability.getOuting().getId(), counts);
		}
		return results;
	}
	
}
