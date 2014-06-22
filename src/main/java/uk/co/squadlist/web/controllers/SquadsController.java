package uk.co.squadlist.web.controllers;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.InvalidSquadException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.SquadDetails;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class SquadsController {
	
	private final static Logger log = Logger.getLogger(SquadsController.class);
	
	private final InstanceSpecificApiClient api;
	private final UrlBuilder urlBuilder;
	private final ViewFactory viewFactory;
	
	@Autowired
	public SquadsController(InstanceSpecificApiClient api, UrlBuilder urlBuilder, ViewFactory viewFactory) {
		this.api = api;	
		this.urlBuilder = urlBuilder;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping(value="/squad/new", method=RequestMethod.GET)
    public ModelAndView newSquad(@ModelAttribute("squadDetails") SquadDetails squadDetails) throws Exception {    	
		return renderNewSquadForm(new SquadDetails());
    }

	@RequestMapping(value="/squad/new", method=RequestMethod.POST)
    public ModelAndView newSquadSubmit(@Valid @ModelAttribute("squadDetails") SquadDetails squadDetails, BindingResult result) {
		if (result.hasErrors()) {
			return renderNewSquadForm(squadDetails);
		}
		
		try {
			api.createSquad(squadDetails.getName());
			
			return new ModelAndView(new RedirectView(urlBuilder.adminUrl()));
			
		} catch (InvalidSquadException e) {
			log.info("Invalid squad");
			result.rejectValue("name", null, "squad name is already in use");	         
			return renderNewSquadForm(squadDetails);
		}
    }
	
	@RequestMapping(value="/squad/{id}/edit", method=RequestMethod.GET)
	public ModelAndView editSquad(@PathVariable String id) throws UnknownSquadException {
		final Squad squad = api.getSquad(id);
		
		final SquadDetails squadDetails = new SquadDetails();
		squadDetails.setName(squad.getName());
		
		return renderEditSquadForm(squad, squadDetails);
	}
	
	@RequestMapping(value="/squad/{id}/edit", method=RequestMethod.POST)
    public ModelAndView editSquadSubmit(@PathVariable String id, @Valid @ModelAttribute("squadDetails") SquadDetails squadDetails, BindingResult result) throws UnknownSquadException {
		final Squad squad = api.getSquad(id);
		if (result.hasErrors()) {
			return renderEditSquadForm(squad, squadDetails);
		}
		
		squad.setName(squadDetails.getName());
		
		log.info("Updating squad: " + squad);
		api.updateSquad(squad);
		return new ModelAndView(new RedirectView(urlBuilder.adminUrl()));	
    }
	
	private ModelAndView renderNewSquadForm(SquadDetails squadDetails) {
		return viewFactory.getView("newSquad").addObject("squadDetails", squadDetails);
	}
	
	private ModelAndView renderEditSquadForm(final Squad squad, final SquadDetails squadDetails) {
		final ModelAndView mv = viewFactory.getView("editSquad");
		mv.addObject("squad", squad);
		mv.addObject("squadDetails", squadDetails);
		mv.addObject("squadMembers", api.getSquadMembers(squad.getId()));
		mv.addObject("availableMembers", api.getMembers());	// TODO diff
		return mv;
	}
	
}
