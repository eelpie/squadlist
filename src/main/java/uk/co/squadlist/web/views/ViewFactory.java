package uk.co.squadlist.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;

@Component
public class ViewFactory {
	
	private final LoggedInUserService loggedInUserService;
	private final OutingAvailabilityCountsService outingAvailabilityCountsService;
	
	@Autowired
	public ViewFactory(LoggedInUserService loggedInUserService, OutingAvailabilityCountsService outingAvailabilityCountsService) {
		this.loggedInUserService = loggedInUserService;
		this.outingAvailabilityCountsService = outingAvailabilityCountsService;
	}

	public ModelAndView getView(String templateName) {
		final String loggedInUser = loggedInUserService.getLoggedInUser();
		
		final ModelAndView mv = new ModelAndView(templateName);
		mv.addObject("loggedInUser", loggedInUser);    	
    	int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUser);
    	if (pendingOutingsCountFor > 0) {
    		mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
    	}
		return mv;
	}
	
}
