package uk.co.squadlist.web.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.InvalidSquadException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.display.DisplayOuting;
import uk.co.squadlist.web.model.forms.SquadDetails;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Controller
public class SquadsController {
	
	private static Logger log = Logger.getLogger(SquadsController.class);
	
	private SquadlistApi api;
	private LoggedInUserService loggedInUserService;
	private UrlBuilder urlBuilder;
	private InstanceConfig instanceConfig;
	
	@Autowired
	public SquadsController(SquadlistApi api, LoggedInUserService loggedInUserService, UrlBuilder urlBuilder, InstanceConfig instanceConfig) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.urlBuilder = urlBuilder;
		this.instanceConfig = instanceConfig;
	}
	
	@RequestMapping("/squad/{id}")
    public ModelAndView index(@PathVariable String id) throws Exception {
		final ModelAndView mv = new ModelAndView("squad");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(instanceConfig.getInstance()));
		mv.addObject("squad", api.getSquad(instanceConfig.getInstance(), id));
    	mv.addObject("members", api.getSquadMembers(instanceConfig.getInstance(), id));
    	mv.addObject("outings", api.getSquadOutings(instanceConfig.getInstance(), id, DateHelper.startOfCurrentOutingPeriod().toDate(), DateHelper.endOfCurrentOutingPeriod().toDate()));
    	return mv;
    }
	
	@RequestMapping(value="/squad/new", method=RequestMethod.GET)
    public ModelAndView newSquad(@ModelAttribute("squad") SquadDetails squadDetails) throws Exception {    	
		return renderNewSquadForm();
    }

	@RequestMapping(value="/squad/new", method=RequestMethod.POST)
    public ModelAndView newSquadSubmit(@Valid @ModelAttribute("squad") SquadDetails squadDetails, BindingResult result) {
		if (result.hasErrors()) {
			return renderNewSquadForm();
		}
		
		try {
			Squad newSquad = api.createSquad(instanceConfig.getInstance(), squadDetails.getName());
			final ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.squadUrl(newSquad)));
			return mv;
			
		} catch (InvalidSquadException e) {
			log.info("Invalid squad");
			result.rejectValue("name", null, "squad name is already in use");	         
			return renderNewSquadForm();
		}
    }
		
	@RequestMapping("/squad/{id}/availability")
    public ModelAndView availability(@PathVariable String id,
    		@RequestParam(value = "month", required = false) String month) throws Exception {
    	ModelAndView mv = new ModelAndView("squadAvailability");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(instanceConfig.getInstance()));
    	
		mv.addObject("squad", api.getSquad(instanceConfig.getInstance(), id));
		
    	final List<Member> members = api.getSquadMembers(instanceConfig.getInstance(), id);
		mv.addObject("members", members);
    	
		if (members.isEmpty()) {
			return mv;			
		}
		
    	Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
    	Date endDate = DateHelper.endOfCurrentOutingPeriod().toDate();
		if (month != null) {
    		final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);	// TODO Can be moved to spring?
    		startDate = monthDateTime.toDate();
    		endDate = monthDateTime.plusMonths(1).toDate();
    	}
		
    	final List<Outing> outings = Lists.newArrayList();
    	final List<OutingWithSquadAvailability> squadAvailability = api.getSquadAvailability(instanceConfig.getInstance(), id, startDate, endDate);
    	final Map<String, String> allAvailability = decorateOutingsWithMembersAvailability(squadAvailability, outings);
    	
    	mv.addObject("outings", makeDisplayObjectsFor(outings));    	
    	mv.addObject("availability", allAvailability);
		mv.addObject("outingMonths", api.getSquadOutingMonths(instanceConfig.getInstance(), id));
		return mv;		
    }

	private Map<String, String> decorateOutingsWithMembersAvailability(final List<OutingWithSquadAvailability> squadAvailability, final List<Outing> outings) {
		Map<String, String> allAvailability = Maps.newHashMap();
    	for (OutingWithSquadAvailability outingWithSquadAvailability : squadAvailability) {
    		outings.add(outingWithSquadAvailability.getOuting());
			final Map<String, String> outingAvailability = outingWithSquadAvailability.getAvailability();
			for (String member : outingAvailability.keySet()) {
				allAvailability.put(outingWithSquadAvailability.getOuting().getId() + "-" + member, outingAvailability.get(member));				
			}
		}
		return allAvailability;
	}
	
	@RequestMapping("/squad/{id}/contacts")
    public ModelAndView contacts(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadContacts");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(instanceConfig.getInstance()));

		mv.addObject("squad", api.getSquad(instanceConfig.getInstance(), id));
    	mv.addObject("members", api.getSquadMembers(instanceConfig.getInstance(), id));
    	return mv;
    }
	
	@RequestMapping("/squad/{id}/entrydetails")
    public ModelAndView entrydetails(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadEntryDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(instanceConfig.getInstance()));

		mv.addObject("squad", api.getSquad(instanceConfig.getInstance(), id));
    	mv.addObject("members", api.getSquadMembers(instanceConfig.getInstance(), id));
    	return mv;
    }
	
	@RequestMapping("/squad/{id}/outings")
	public ModelAndView outings(@PathVariable String id,
			@RequestParam(value = "month", required = false) String month) throws Exception {
    	final Squad squad = api.getSquad(instanceConfig.getInstance(), id);
    	Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
    	Date endDate = DateHelper.endOfCurrentOutingPeriod().toDate();
    	
    	if (month != null) {
    		final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);	// TODO Can be moved to spring?
    		startDate = monthDateTime.toDate();
    		endDate = monthDateTime.plusMonths(1).toDate();
    	}
    	
    	final ModelAndView mv = new ModelAndView("squadOutings");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());		
		mv.addObject("squad", squad);
		mv.addObject("outings", makeDisplayObjectsFor(api.getSquadOutings(instanceConfig.getInstance(), id, startDate, endDate)));
		mv.addObject("outingMonths", api.getSquadOutingMonths(instanceConfig.getInstance(), id));
    	return mv;
    }
	
	private ModelAndView renderNewSquadForm() {
		ModelAndView mv = new ModelAndView("newSquad");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
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
