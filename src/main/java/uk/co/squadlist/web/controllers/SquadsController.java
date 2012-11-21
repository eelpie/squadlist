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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.display.DisplayOuting;
import uk.co.squadlist.web.model.forms.SquadDetails;
import uk.co.squadlist.web.urls.UrlBuilder;

@Controller
public class SquadsController {
	
	private SquadlistApi api;
	private LoggedInUserService loggedInUserService;
	private UrlBuilder urlBuilder;
	
	@Autowired
	public SquadsController(SquadlistApi api, LoggedInUserService loggedInUserService, UrlBuilder urlBuilder) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.urlBuilder = urlBuilder;
	}
	
	@RequestMapping("/squad/{id}")
    public ModelAndView index(@PathVariable String id) throws Exception {
		final ModelAndView mv = new ModelAndView("squad");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(SquadlistApi.INSTANCE));
		mv.addObject("squad", api.getSquad(SquadlistApi.INSTANCE, id));
    	mv.addObject("members", api.getSquadMembers(SquadlistApi.INSTANCE, id));
    	mv.addObject("outings", api.getSquadOutings(SquadlistApi.INSTANCE, id));
    	return mv;
    }
	
	@RequestMapping(value="/squad/new", method=RequestMethod.GET)
    public ModelAndView newSquad(@ModelAttribute("squad") SquadDetails squadDetails) throws Exception {    	
		ModelAndView mv = new ModelAndView("newSquad");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		return mv;
    }
	
	@RequestMapping(value="/squad/new", method=RequestMethod.POST)
    public ModelAndView newSquadSubmit(@ModelAttribute("member") SquadDetails squadDetails) throws Exception {
		final Squad newSquad = api.createSquad(SquadlistApi.INSTANCE, squadDetails.getName());
		final ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.squadUrl(newSquad)));
		return mv;
    }
		
	@RequestMapping("/squad/{id}/availability")
    public ModelAndView availability(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadAvailability");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(SquadlistApi.INSTANCE));
    	
		mv.addObject("squad", api.getSquad(SquadlistApi.INSTANCE, id));
    	mv.addObject("members", api.getSquadMembers(SquadlistApi.INSTANCE, id));
    	
    	final DateMidnight midnightYesterday = DateTime.now().minusDays(1).toDateMidnight();
		final List<OutingWithSquadAvailability> squadAvailability = api.getSquadAvailability(SquadlistApi.INSTANCE, id, midnightYesterday.toDate(), midnightYesterday.plusWeeks(2).toDate());
		
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
    public ModelAndView contacts(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadContacts");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(SquadlistApi.INSTANCE));

		mv.addObject("squad", api.getSquad(SquadlistApi.INSTANCE, id));
    	mv.addObject("members", api.getSquadMembers(SquadlistApi.INSTANCE, id));
    	return mv;
    }
	
	@RequestMapping("/squad/{id}/entrydetails")
    public ModelAndView entrydetails(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadEntryDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(SquadlistApi.INSTANCE));

		mv.addObject("squad", api.getSquad(SquadlistApi.INSTANCE, id));
    	mv.addObject("members", api.getSquadMembers(SquadlistApi.INSTANCE, id));
    	return mv;
    }
	
	@RequestMapping("/squad/{id}/outings")
    public ModelAndView outings(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadOutings");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());

		mv.addObject("squad", api.getSquad(SquadlistApi.INSTANCE, id));
    	mv.addObject("outings", makeDisplayObjectsFor(api.getSquadOutings(SquadlistApi.INSTANCE, id)));
    	return mv;
    }

	private List<DisplayOuting> makeDisplayObjectsFor(List<Outing> outings) throws JsonParseException, JsonMappingException, IOException, HttpFetchException {
		List<DisplayOuting> displayOutings = new ArrayList<DisplayOuting>();
		for (Outing outing : outings) {
			displayOutings.add(new DisplayOuting(outing.getId(),
					outing.getSquad(),
					outing.getDate()));
		}
		return displayOutings;
	}
	
}
