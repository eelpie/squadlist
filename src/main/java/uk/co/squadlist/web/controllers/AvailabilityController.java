package uk.co.squadlist.web.controllers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.views.DateHelper;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

@Controller
public class AvailabilityController {
		
	private SquadlistApi api;
	private LoggedInUserService loggedInUserService;
	private InstanceConfig instanceConfig;
	private PreferedSquadService preferedSquadService;
	
	@Autowired
	public AvailabilityController(SquadlistApi api, LoggedInUserService loggedInUserService,
			InstanceConfig instanceConfig, PreferedSquadService preferedSquadService) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.instanceConfig = instanceConfig;
		this.preferedSquadService = preferedSquadService;
	}
	
	@RequestMapping("/availability")
    public ModelAndView availability(@RequestParam(required=false, value="squad") String squadId,
    		@RequestParam(value = "month", required = false) String month) throws Exception {
    	ModelAndView mv = new ModelAndView("availability");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(instanceConfig.getInstance()));
    	
    	final Squad squad = resolveSquad(squadId);

    	if (squad != null) {
			mv.addObject("squad", squad);
			mv.addObject("title", squad.getName() + " availability");
	    	mv.addObject("members", api.getSquadMembers(instanceConfig.getInstance(), squad.getId()));
	    	
			if (api.getSquadMembers(instanceConfig.getInstance(), squad.getId()).isEmpty()) {
				return mv;			
			}
			
	    	Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
	    	Date endDate = DateHelper.endOfCurrentOutingPeriod().toDate();
			if (month != null) {
	    		final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);	// TODO Can be moved to spring?
	    		startDate = monthDateTime.toDate();
	    		endDate = monthDateTime.plusMonths(1).toDate();
	    	}
			
	    	final List<OutingWithSquadAvailability> squadAvailability = api.getSquadAvailability(instanceConfig.getInstance(), squad.getId(), startDate, endDate);
	    	final List<Outing> outings = api.getSquadOutings(instanceConfig.getInstance(), squad.getId(), startDate, endDate);

	    	mv.addObject("availability", decorateOutingsWithMembersAvailability(squadAvailability, outings));
	    	mv.addObject("outings", outings);
			mv.addObject("outingMonths", api.getSquadOutingMonths(instanceConfig.getInstance(), squad.getId()));
    	}
		return mv;		
    }
	
	private Squad resolveSquad(String squadId) throws UnknownSquadException, UnknownMemberException {
    	if(!Strings.isNullOrEmpty(squadId)) {
    		return api.getSquad(instanceConfig.getInstance(), squadId);
    	}    	
    	return preferedSquadService.resolvedPreferedSquad(loggedInUserService.getLoggedInUser());
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
	
}
