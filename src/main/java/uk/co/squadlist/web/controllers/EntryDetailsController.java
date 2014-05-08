package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;

import com.google.common.base.Strings;

@Controller
public class EntryDetailsController {
		
	private final SquadlistApi api;
	private final LoggedInUserService loggedInUserService;
	private final InstanceConfig instanceConfig;
	private final PreferedSquadService preferedSquadService;
	
	@Autowired
	public EntryDetailsController(SquadlistApi api, LoggedInUserService loggedInUserService,
			InstanceConfig instanceConfig, PreferedSquadService preferedSquadService) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.instanceConfig = instanceConfig;
		this.preferedSquadService = preferedSquadService;
	}
	
	@RequestMapping("/entrydetails")
    public ModelAndView entrydetails(@RequestParam(required=false, value="squad") String squadId) throws Exception {
    	final ModelAndView mv = new ModelAndView("entryDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(instanceConfig.getInstance()));
    	
    	final Squad squadToShow = resolveSquad(squadId);
		mv.addObject("squad", squadToShow);
    	mv.addObject("members", api.getSquadMembers(instanceConfig.getInstance(), squadToShow.getId()));
    	return mv;
    }
	
	private Squad resolveSquad(String squadId) {
    	if(!Strings.isNullOrEmpty(squadId)) {
    		return api.getSquad(instanceConfig.getInstance(), squadId);
    	}    	
    	return preferedSquadService.resolvedPreferedSquad(loggedInUserService.getLoggedInUser());
	}
}
