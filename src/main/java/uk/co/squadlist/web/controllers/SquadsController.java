package uk.co.squadlist.web.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.display.DisplayOuting;

@Controller
public class SquadsController {
	
	private SquadlistApi api;
	private LoggedInUserService loggedInUserService;
	
	@Autowired
	public SquadsController(SquadlistApi api, LoggedInUserService loggedInUserService) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
	}
	
	@RequestMapping("/squad/{id}/availability")
    public ModelAndView availability(@PathVariable int id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadAvailability");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		mv.addObject("squad", api.getSquad(id));
    	mv.addObject("members", api.getSquadMembers(id));
    	
    	final DateMidnight midnightYesterday = DateTime.now().minusDays(1).toDateMidnight();
		final List<OutingWithSquadAvailability> squadAvailability = api.getSquadAvailability(id, midnightYesterday.toDate());

    	List<Outing> outings = new ArrayList<Outing>();
    	Map<String, String> allAvailability = new HashMap<String, String>();
    	for (OutingWithSquadAvailability outingWithSquadAvailability : squadAvailability) {
    		outings.add(outingWithSquadAvailability.getOuting());
			final Map<String, String> outingAvailability = outingWithSquadAvailability.getAvailability();
			for (String member : outingAvailability.keySet()) {
				allAvailability.put(outingWithSquadAvailability.getOuting().getId() + "-" + member, outingAvailability.get(member));				
			}
		}
    	mv.addObject("outings", makeDisplayObjectsFor(outings));    	
    	mv.addObject("availability", allAvailability);
    	return mv;
    }
	
	@RequestMapping("/squad/{id}/contacts")
    public ModelAndView contacts(@PathVariable int id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadContacts");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());

		mv.addObject("squad", api.getSquad(id));
    	mv.addObject("members", api.getSquadMembers(id));
    	return mv;
    }
	
	@RequestMapping("/squad/{id}/entrydetails")
    public ModelAndView entrydetails(@PathVariable int id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadEntryDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());

		mv.addObject("squad", api.getSquad(id));
    	mv.addObject("members", api.getSquadMembers(id));
    	return mv;
    }
	
	@RequestMapping("/squad/{id}/outings")
    public ModelAndView outings(@PathVariable int id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadOutings");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());

		mv.addObject("squad", api.getSquad(id));
    	mv.addObject("outings", makeDisplayObjectsFor(api.getSquadOutings(id)));
    	return mv;
    }

	private List<DisplayOuting> makeDisplayObjectsFor(List<Outing> outings) throws JsonParseException, JsonMappingException, IOException, HttpFetchException {
		List<DisplayOuting> displayOutings = new ArrayList<DisplayOuting>();
		final Map<Integer, Squad> squadsMap = api.getSquadsMap();
		for (Outing outing : outings) {
			displayOutings.add(new DisplayOuting(outing.getId(), 
					outing.getSquad(), 
					squadsMap.get(outing.getSquad()).getName(), 
					outing.getDate()));
		}
		return displayOutings;
	}
	
}
