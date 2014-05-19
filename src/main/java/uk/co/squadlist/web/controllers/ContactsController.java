package uk.co.squadlist.web.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class ContactsController {
		
	private final InstanceSpecificApiClient api;
	private final PreferedSquadService preferedSquadService;
	private final ViewFactory viewFactory;
	
	@Autowired
	public ContactsController(InstanceSpecificApiClient api, PreferedSquadService preferedSquadService, ViewFactory viewFactory) {
		this.api = api;
		this.preferedSquadService = preferedSquadService;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping("/contacts")
    public ModelAndView contacts(@RequestParam(required=false, value="squad") String squadId) throws Exception {
    	final ModelAndView mv =  viewFactory.getView("contacts");
    	final List<Squad> allSquads = api.getSquads();
		mv.addObject("squads", allSquads);
    	if (!allSquads.isEmpty()) {
    		final Squad squadToShow = preferedSquadService.resolveSquad(squadId);
    		mv.addObject("title", squadToShow.getName() + " contacts");
    		mv.addObject("squad", squadToShow);
    		mv.addObject("members", api.getSquadMembers(squadToShow.getId()));    		
    	}
    	return mv;
    }
	
}
