package uk.co.squadlist.web.services;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicLongMap;
import org.springframework.stereotype.Component;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.AvailabilityOption;
import uk.co.squadlist.model.swagger.OutingWithAvailability;
import uk.co.squadlist.model.swagger.OutingWithSquadAvailability;
import uk.co.squadlist.web.views.DateHelper;

import java.util.List;
import java.util.Map;

@Component
public class OutingAvailabilityCountsService {

	public Map<String, Map<String, Long>> buildOutingAvailabilityCounts(List<OutingWithSquadAvailability> squadOutings) {
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

	public int getPendingOutingsCountFor(String memberId, DefaultApi swaggerApiClientForLoggedInUser) throws ApiException {
		int pendingCount = 0;

		List<OutingWithAvailability> memberAvailability = swaggerApiClientForLoggedInUser.getMemberAvailability(memberId, DateHelper.startOfCurrentOutingPeriod(), DateHelper.oneMonthFromNow());
		for (OutingWithAvailability outingAvailability : memberAvailability) {
			if (outingAvailability.getAvailabilityOption() == null) {
				pendingCount++;
			}
		}		 
		return pendingCount;
	}
	
}
