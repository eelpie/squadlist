package uk.co.squadlist.web.services;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicLongMap;
import org.springframework.stereotype.Component;
import uk.co.squadlist.model.swagger.AvailabilityOption;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.views.DateHelper;

import java.util.List;
import java.util.Map;

@Component
public class OutingAvailabilityCountsService {

	public Map<String, Map<String, Long>> buildOutingAvailabilityCounts(List<uk.co.squadlist.model.swagger.OutingWithSquadAvailability> squadOutings) {
		final Map<String, Map<String, Long>> results = Maps.newHashMap();
		
		for (uk.co.squadlist.model.swagger.OutingWithSquadAvailability outingWithAvailability : squadOutings) {
			final AtomicLongMap<String> counts = AtomicLongMap.create();			
			final Map<String, uk.co.squadlist.model.swagger.AvailabilityOption> membersAvailabilityForThisOuting = outingWithAvailability.getAvailability();
			for (AvailabilityOption availabilityOption : membersAvailabilityForThisOuting.values()) {
				if (availabilityOption != null && availabilityOption.getColour() != null) {	// Should the API persist these?
					counts.addAndGet(availabilityOption.getColour(), 1);
				}
			}
			results.put(outingWithAvailability.getOuting().getId(), counts.asMap());
		}
		return results;
	}

	public int getPendingOutingsCountFor(String memberId, InstanceSpecificApiClient api) {
		int pendingCount = 0;
		for (OutingAvailability outingAvailability : api.getAvailabilityFor(memberId, 
				DateHelper.startOfCurrentOutingPeriod().toDate(), DateHelper.oneMonthFromNow().toDate())) {
			if (outingAvailability.getAvailabilityOption() == null) {
				pendingCount++;
			}
		}		 
		return pendingCount;
	}
	
}
