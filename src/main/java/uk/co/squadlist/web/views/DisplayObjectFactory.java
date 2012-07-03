package uk.co.squadlist.web.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.exceptions.HttpFetchException;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.display.DisplayOutingAvailability;

@Component
public class DisplayObjectFactory {

	private SquadlistApi api;
	
	@Autowired
	public DisplayObjectFactory(SquadlistApi api) {
		this.api = api;
	}
	
	public List<DisplayOutingAvailability> makeDisplayObjectsFor(List<OutingAvailability> availabilityFor) throws JsonParseException, JsonMappingException, IOException, HttpFetchException {
		List<DisplayOutingAvailability> displayOutingAvailabilities = new ArrayList<DisplayOutingAvailability>();
		final Map<Integer, Squad> squadsMap = api.getSquadsMap();
		for (OutingAvailability outingAvailability : availabilityFor) {
			displayOutingAvailabilities.add(new DisplayOutingAvailability(outingAvailability.getOuting().getId(), 
					outingAvailability.getOuting().getSquad(), 
					squadsMap.get(outingAvailability.getOuting().getSquad()).getName(), 
					outingAvailability.getOuting().getDate(),
					outingAvailability.getAvailability()));
		}
		return displayOutingAvailabilities;
	}
		
}
