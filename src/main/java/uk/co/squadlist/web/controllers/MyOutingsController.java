package uk.co.squadlist.web.controllers;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class MyOutingsController {
		
	private final LoggedInUserService loggedInUserService;
	private final InstanceSpecificApiClient api;
	private final ViewFactory viewFactory;
	private final OutingAvailabilityCountsService outingAvailabilityCountsService;
	
	@Autowired
	public MyOutingsController(LoggedInUserService loggedInUserService, InstanceSpecificApiClient api, ViewFactory viewFactory,
			OutingAvailabilityCountsService outingAvailabilityCountsService) {
		this.loggedInUserService = loggedInUserService;
		this.api = api;
		this.viewFactory = viewFactory;
		this.outingAvailabilityCountsService = outingAvailabilityCountsService;
	}
	
	@RequestMapping("/")
    public ModelAndView outings() throws Exception {
    	final ModelAndView mv = viewFactory.getView("myOutings");
    	final String loggedInUser = loggedInUserService.getLoggedInUser();
		mv.addObject("member", api.getMemberDetails(loggedInUser));
		
		final Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
		final Date endDate = DateHelper.oneYearFromNow().toDate();
		
		mv.addObject("outings", api.getAvailabilityFor(loggedInUser, startDate, endDate));
		
		mv.addObject("title", "My outings");		
    	mv.addObject("availabilityOptions", api.getAvailabilityOptions());
    	return mv;
    }
	
	@RequestMapping("/myoutings/ajax")
    public ModelAndView ajax() throws Exception {
    	final ModelAndView mv = viewFactory.getView("myOutingsAjax");
    	int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUserService.getLoggedInUser());
    	if (pendingOutingsCountFor > 0) {
    		mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
    	}
    	return mv;
    }
	
}
