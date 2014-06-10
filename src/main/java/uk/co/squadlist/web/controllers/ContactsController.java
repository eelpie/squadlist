package uk.co.squadlist.web.controllers;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class ContactsController {
		
	private static Logger log = Logger.getLogger(ContactsController.class);

	private InstanceSpecificApiClient api;
	private PreferedSquadService preferedSquadService;
	private ViewFactory viewFactory;
	private ContactsModelPopulator contactsModelPopulator;
	
	public ContactsController() {
	}
	
	@Autowired
	public ContactsController(InstanceSpecificApiClient api, PreferedSquadService preferedSquadService, ViewFactory viewFactory, ContactsModelPopulator contactsModelPopulator) {
		this.api = api;
		this.preferedSquadService = preferedSquadService;
		this.viewFactory = viewFactory;
		this.contactsModelPopulator = contactsModelPopulator;
	}
	
	@RequestMapping("/contacts/{squadId}")
    public ModelAndView contacts(@PathVariable String squadId) throws Exception {
		final Squad squadToShow = preferedSquadService.resolveSquad(squadId);
		
    	final ModelAndView mv =  viewFactory.getView("contacts");
    	final List<Squad> allSquads = api.getSquads();
		mv.addObject("squads", allSquads);
    	if (!allSquads.isEmpty()) {
    		log.info("Squad to show: " + squadToShow);
    		contactsModelPopulator.populateModel(squadToShow, mv);    		
    	}
    	return mv;
    }
	
}
