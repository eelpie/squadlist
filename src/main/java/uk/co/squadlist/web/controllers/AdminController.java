package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.urls.UrlBuilder;

@Controller
public class AdminController {
		
	private final InstanceSpecificApiClient api;
	private final LoggedInUserService loggedInUserService;
	
	@Autowired
	public AdminController(InstanceSpecificApiClient api, LoggedInUserService loggedInUserService, UrlBuilder urlBuilder) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
	}

	@RequestMapping(value="/admin", method=RequestMethod.GET)
    public ModelAndView member() throws Exception {
    	final ModelAndView mv = new ModelAndView("admin");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("members", api.getMembers());
    	mv.addObject("squads", api.getSquads());
    	mv.addObject("availabilityOptions", api.getAvailabilityOptions());
    	mv.addObject("title", "Admin");
    	return mv;
    }
	
}
