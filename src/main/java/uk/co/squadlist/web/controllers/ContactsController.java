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
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class ContactsController {

	private static Logger log = Logger.getLogger(ContactsController.class);

	private InstanceSpecificApiClient api;
	private PreferredSquadService preferredSquadService;
	private ViewFactory viewFactory;
	private ContactsModelPopulator contactsModelPopulator;

	public ContactsController() {
	}

	@Autowired
	public ContactsController(InstanceSpecificApiClient api, PreferredSquadService preferredSquadService, ViewFactory viewFactory, ContactsModelPopulator contactsModelPopulator) {
		this.api = api;
		this.preferredSquadService = preferredSquadService;
		this.viewFactory = viewFactory;
		this.contactsModelPopulator = contactsModelPopulator;
	}

	@RequestMapping("/contacts")
    public ModelAndView contacts() throws Exception {
    	final ModelAndView mv =  viewFactory.getViewForLoggedInUser("contacts");
    	final List<Squad> allSquads = api.getSquads();
		mv.addObject("squads", allSquads);	// TODO leaves squad null on view
    	return mv;
    }

	@RequestMapping("/contacts/{squadId}")
    public ModelAndView squadContacts(@PathVariable String squadId) throws Exception {
		final Squad squadToShow = preferredSquadService.resolveSquad(squadId);

    	final ModelAndView mv =  viewFactory.getViewForLoggedInUser("contacts");
    	final List<Squad> allSquads = api.getSquads();
		mv.addObject("squads", allSquads);
    	if (!allSquads.isEmpty()) {
    		log.info("Squad to show: " + squadToShow);
    		contactsModelPopulator.populateModel(squadToShow, mv);
    	}
    	return mv;
    }

}
