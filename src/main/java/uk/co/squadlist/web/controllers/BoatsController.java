package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class BoatsController {

	private ViewFactory viewFactory;
	private LoggedInUserService loggedInUserService;

	@Autowired
	public BoatsController(ViewFactory viewFactory, LoggedInUserService loggedInUserService) {
		this.viewFactory = viewFactory;
		this.loggedInUserService = loggedInUserService;
	}

	@RequestMapping("/boats/{id}")
	public ModelAndView outing(@PathVariable String id) throws Exception {
		InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
		return viewFactory.getViewForLoggedInUser("boat").addObject("boat", loggedInUserApi.getBoat(id));
	}

}
