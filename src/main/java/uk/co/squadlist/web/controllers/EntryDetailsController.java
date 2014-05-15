package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;

@Controller
public class EntryDetailsController {
		
	private final InstanceSpecificApiClient api;
	private final LoggedInUserService loggedInUserService;
	private final PreferedSquadService preferedSquadService;
	
	@Autowired
	public EntryDetailsController(InstanceSpecificApiClient api, LoggedInUserService loggedInUserService, PreferedSquadService preferedSquadService) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.preferedSquadService = preferedSquadService;
	}
	
	@RequestMapping("/entrydetails")
    public ModelAndView entrydetails(@RequestParam(required=false, value="squad") String squadId) throws Exception {
    	final ModelAndView mv = new ModelAndView("entryDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads());
    	
    	final Squad squadToShow = preferedSquadService.resolveSquad(squadId, loggedInUserService.getLoggedInUser());
		mv.addObject("squad", squadToShow);
		mv.addObject("title", squadToShow.getName() + " entry details");
    	mv.addObject("members", api.getSquadMembers(squadToShow.getId()));
    	return mv;
    }
	
}
