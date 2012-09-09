package uk.co.squadlist.web.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.display.DisplayOutingAvailability;

@Component
public class DisplayObjectFactory {
	
	public List<DisplayOutingAvailability> makeDisplayObjectsFor(List<OutingAvailability> availabilityFor) throws JsonParseException, JsonMappingException, IOException, HttpFetchException {
		List<DisplayOutingAvailability> displayOutingAvailabilities = new ArrayList<DisplayOutingAvailability>();
		for (OutingAvailability outingAvailability : availabilityFor) {
			displayOutingAvailabilities.add(new DisplayOutingAvailability(outingAvailability.getOuting().getId(), 
					outingAvailability.getOuting().getSquad(), 
					outingAvailability.getOuting().getDate(),
					outingAvailability.getAvailability()));
		}
		return displayOutingAvailabilities;
	}
		
}
