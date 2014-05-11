package uk.co.squadlist.web.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;

import com.google.common.base.Strings;

@Controller
public class ContactsController {
		
	private final SquadlistApi api;
	private final LoggedInUserService loggedInUserService;
	private final InstanceConfig instanceConfig;
	private final PreferedSquadService preferedSquadService;
	
	@Autowired
	public ContactsController(SquadlistApi api, LoggedInUserService loggedInUserService, InstanceConfig instanceConfig, PreferedSquadService preferedSquadService) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.instanceConfig = instanceConfig;
		this.preferedSquadService = preferedSquadService;
	}
	
	@RequestMapping("/contacts")
    public ModelAndView contacts(@RequestParam(required=false, value="squad") String squadId) throws Exception {
		
    	final ModelAndView mv = new ModelAndView("contacts");
    	mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	final List<Squad> allSquads = api.getSquads(instanceConfig.getInstance());
		mv.addObject("squads", allSquads);
    	if (!allSquads.isEmpty()) {
    		final Squad squadToShow = resolveSquad(squadId);
    		mv.addObject("title", squadToShow.getName() + " contacts");
    		mv.addObject("squad", squadToShow);
    		mv.addObject("members", api.getSquadMembers(instanceConfig.getInstance(), squadToShow.getId()));    		
    	}
    	return mv;
    }

	private Squad resolveSquad(String squadId) throws UnknownSquadException, UnknownMemberException {
    	if(!Strings.isNullOrEmpty(squadId)) {
    		return api.getSquad(instanceConfig.getInstance(), squadId);
    	}    	
    	return preferedSquadService.resolvedPreferedSquad(loggedInUserService.getLoggedInUser());
	}
	
}
