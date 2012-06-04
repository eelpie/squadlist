package uk.co.squadlist.web.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.HttpFetchException;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.display.DisplayOutingAvailability;

@Controller
public class MyOutingsController {
	
	private LoggedInUserService loggedInUserService;
	private SquadlistApi api;
	
	@Autowired
	public MyOutingsController(LoggedInUserService loggedInUserService, SquadlistApi api) {
		this.loggedInUserService = loggedInUserService;
		this.api = api;
	}
	
	@RequestMapping("/")
    public ModelAndView members() throws Exception {
    	ModelAndView mv = new ModelAndView("myOutings");
    	final String loggedInUser = loggedInUserService.getLoggedInUser();
		mv.addObject("loggedInUser", loggedInUser);
    	mv.addObject("outings", makeDisplayObjectsFor(api.getAvailabilityFor(loggedInUser)));
    	return mv;
    }
	
	private List<DisplayOutingAvailability> makeDisplayObjectsFor(List<OutingAvailability> availabilityFor) throws JsonParseException, JsonMappingException, IOException, HttpFetchException {
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
