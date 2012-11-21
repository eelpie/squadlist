package uk.co.squadlist.web.controllers;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.views.DisplayObjectFactory;

@Controller
public class MyOutingsController {
		
	private LoggedInUserService loggedInUserService;
	private SquadlistApi api;
	private DisplayObjectFactory displayObjectFactory;
	
	@Autowired
	public MyOutingsController(LoggedInUserService loggedInUserService, SquadlistApi api, DisplayObjectFactory displayObjectFactory) {
		this.loggedInUserService = loggedInUserService;
		this.api = api;
		this.displayObjectFactory = displayObjectFactory;
	}
	
	@RequestMapping("/")
    public ModelAndView outings() throws Exception {
    	ModelAndView mv = new ModelAndView("myOutings");
    	final String loggedInUser = loggedInUserService.getLoggedInUser();
		mv.addObject("loggedInUser", loggedInUser);
    	
		final DateMidnight midnightYesterday = DateTime.now().minusDays(1).toDateMidnight();
		mv.addObject("outings", displayObjectFactory.makeDisplayObjectsFor(api
				.getAvailabilityFor(SquadlistApi.INSTANCE, loggedInUser,
						midnightYesterday.toDate(), 
						midnightYesterday.plusWeeks(2).toDate())));
    	mv.addObject("availabilityOptions", api.getAvailabilityOptions(SquadlistApi.INSTANCE));
    	return mv;
    }
	
}
