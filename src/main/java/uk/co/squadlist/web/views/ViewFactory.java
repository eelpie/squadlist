package uk.co.squadlist.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.OutingAvailability;

@Component
public class ViewFactory {
	
	private final LoggedInUserService loggedInUserService;
	private final InstanceSpecificApiClient api;
	
	@Autowired
	public ViewFactory(LoggedInUserService loggedInUserService, InstanceSpecificApiClient api) {
		this.loggedInUserService = loggedInUserService;
		this.api = api;
	}

	public ModelAndView getView(String templateName) {
		final String loggedInUser = loggedInUserService.getLoggedInUser();
		
		final ModelAndView mv = new ModelAndView(templateName);
		mv.addObject("loggedInUser", loggedInUser);    	
    	int pendingOutingsCountFor = getPendingOutingsCountFor(loggedInUser);
    	if (pendingOutingsCountFor > 0) {
    		mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
    	}
		return mv;
	}

	private int getPendingOutingsCountFor(String loggedInUser) {	// TODO duplication with my outings view
		int pendingCount = 0;
		for (OutingAvailability outingAvailability : api.getAvailabilityFor(loggedInUser, DateHelper.startOfCurrentOutingPeriod().toDate(), DateHelper.endOfCurrentOutingPeriod().toDate())) {
			if (outingAvailability.getAvailability() == null) {
				pendingCount++;
			}			
		}		 
		return pendingCount;
	}

}
