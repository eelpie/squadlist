package uk.co.squadlist.web.controllers;

import java.util.List;

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
public class ContactsController {
		
	private final InstanceSpecificApiClient api;
	private final LoggedInUserService loggedInUserService;
	private final PreferedSquadService preferedSquadService;
	
	@Autowired
	public ContactsController(InstanceSpecificApiClient api, LoggedInUserService loggedInUserService, PreferedSquadService preferedSquadService) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.preferedSquadService = preferedSquadService;
	}
	
	@RequestMapping("/contacts")
    public ModelAndView contacts(@RequestParam(required=false, value="squad") String squadId) throws Exception {
    	final ModelAndView mv = new ModelAndView("contacts");
    	mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	final List<Squad> allSquads = api.getSquads();
		mv.addObject("squads", allSquads);
    	if (!allSquads.isEmpty()) {
    		final Squad squadToShow = preferedSquadService.resolveSquad(squadId, loggedInUserService.getLoggedInUser());
    		mv.addObject("title", squadToShow.getName() + " contacts");
    		mv.addObject("squad", squadToShow);
    		mv.addObject("members", api.getSquadMembers(squadToShow.getId()));    		
    	}
    	return mv;
    }
	
}
