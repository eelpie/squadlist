package uk.co.squadlist.web.controllers;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.InvalidSquadException;
import uk.co.squadlist.web.model.forms.SquadDetails;
import uk.co.squadlist.web.urls.UrlBuilder;

@Controller
public class SquadsController {
	
	private final static Logger log = Logger.getLogger(SquadsController.class);
	
	private final SquadlistApi api;
	private final LoggedInUserService loggedInUserService;
	private final UrlBuilder urlBuilder;
	private final InstanceConfig instanceConfig;
	
	@Autowired
	public SquadsController(SquadlistApi api, LoggedInUserService loggedInUserService, UrlBuilder urlBuilder, InstanceConfig instanceConfig) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.urlBuilder = urlBuilder;
		this.instanceConfig = instanceConfig;
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
			api.createSquad(instanceConfig.getInstance(), squadDetails.getName());
			
			return new ModelAndView(new RedirectView(urlBuilder.adminUrl()));
			
		} catch (InvalidSquadException e) {
			log.info("Invalid squad");
			result.rejectValue("name", null, "squad name is already in use");	         
			return renderNewSquadForm();
		}
    }
	
	private ModelAndView renderNewSquadForm() {
		return new ModelAndView("newSquad").addObject("loggedInUser", loggedInUserService.getLoggedInUser());
	}
	
}
