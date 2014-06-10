package uk.co.squadlist.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.PreferedSquadService;

@Component
public class ViewFactory {
	
	private final LoggedInUserService loggedInUserService;
	private final OutingAvailabilityCountsService outingAvailabilityCountsService;
	private final PreferedSquadService preferedSquadService;
	
	@Autowired
	public ViewFactory(LoggedInUserService loggedInUserService, OutingAvailabilityCountsService outingAvailabilityCountsService,
			PreferedSquadService preferedSquadService) {
		this.loggedInUserService = loggedInUserService;
		this.outingAvailabilityCountsService = outingAvailabilityCountsService;
		this.preferedSquadService = preferedSquadService;
	}

	public ModelAndView getView(String templateName) {
		final String loggedInUser = loggedInUserService.getLoggedInUser();
		
		final ModelAndView mv = new ModelAndView(templateName);
		mv.addObject("loggedInUser", loggedInUser);
		
    	int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUser);
    	if (pendingOutingsCountFor > 0) {
    		mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
    	}
    	
    	try {
    		mv.addObject("preferredSquad", preferedSquadService.resolvedPreferedSquad(loggedInUser));
    	} catch (Exception e) {
    		// TODO
    	}
		return mv;
	}
	
}
