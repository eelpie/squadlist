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

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.InvalidSquadException;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.SquadDetails;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;

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
	
	@RequestMapping("/squad/{id}")
    public ModelAndView index(@PathVariable String id) throws Exception {
		final ModelAndView mv = new ModelAndView("squad");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(instanceConfig.getInstance()));
		mv.addObject("squad", api.getSquad(instanceConfig.getInstance(), id));
    	mv.addObject("members", api.getSquadMembers(instanceConfig.getInstance(), id));
    	mv.addObject("outings", api.getSquadOutings(instanceConfig.getInstance(), id, DateHelper.startOfCurrentOutingPeriod().toDate(), DateHelper.endOfCurrentOutingPeriod().toDate()));
    	return mv;
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
			Squad newSquad = api.createSquad(instanceConfig.getInstance(), squadDetails.getName());
			final ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.squadUrl(newSquad)));
			return mv;
			
		} catch (InvalidSquadException e) {
			log.info("Invalid squad");
			result.rejectValue("name", null, "squad name is already in use");	         
			return renderNewSquadForm();
		}
    }
	
	private ModelAndView renderNewSquadForm() {
		ModelAndView mv = new ModelAndView("newSquad");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		return mv;
	}
	
}
