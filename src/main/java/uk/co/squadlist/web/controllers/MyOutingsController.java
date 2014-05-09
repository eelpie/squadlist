package uk.co.squadlist.web.controllers;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.DisplayObjectFactory;

@Controller
public class MyOutingsController {
		
	private final LoggedInUserService loggedInUserService;
	private final SquadlistApi api;
	private final DisplayObjectFactory displayObjectFactory;
	private final InstanceConfig instanceConfig;
	
	@Autowired
	public MyOutingsController(LoggedInUserService loggedInUserService, SquadlistApi api, DisplayObjectFactory displayObjectFactory, InstanceConfig instanceConfig) {
		this.loggedInUserService = loggedInUserService;
		this.api = api;
		this.displayObjectFactory = displayObjectFactory;
		this.instanceConfig = instanceConfig;
	}
	
	@RequestMapping("/")
    public ModelAndView outings() throws Exception {
    	ModelAndView mv = new ModelAndView("myOutings");
    	final String loggedInUser = loggedInUserService.getLoggedInUser();
		mv.addObject("loggedInUser", loggedInUser);
    	
		mv.addObject("member", api.getMemberDetails(instanceConfig.getInstance(), loggedInUser));
		
		final Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
		final Date endDate = DateHelper.endOfCurrentOutingPeriod().toDate();
		
		mv.addObject("startDate", startDate);
		mv.addObject("endDate", endDate);
		
		mv.addObject("outings", displayObjectFactory.makeDisplayObjectsFor(api.getAvailabilityFor(instanceConfig.getInstance(), loggedInUser, startDate, endDate)));

		mv.addObject("heading", "My outings");		
    	mv.addObject("availabilityOptions", api.getAvailabilityOptions(instanceConfig.getInstance()));
    	return mv;
    }
	
}
