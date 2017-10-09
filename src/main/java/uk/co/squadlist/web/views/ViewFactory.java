package uk.co.squadlist.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.localisation.BritishRowing;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.localisation.RowingIreland;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.PreferedSquadService;

@Component
public class ViewFactory {
	
	private final LoggedInUserService loggedInUserService;
	private final OutingAvailabilityCountsService outingAvailabilityCountsService;
	private final PreferedSquadService preferedSquadService;
	private final GoverningBodyFactory governingBodyFactory;

	@Autowired
	public ViewFactory(LoggedInUserService loggedInUserService, OutingAvailabilityCountsService outingAvailabilityCountsService,
			PreferedSquadService preferedSquadService, GoverningBodyFactory governingBodyFactory) {
		this.loggedInUserService = loggedInUserService;
		this.outingAvailabilityCountsService = outingAvailabilityCountsService;
		this.preferedSquadService = preferedSquadService;
		this.governingBodyFactory = governingBodyFactory;
	}

	public ModelAndView getViewForLoggedInUser(String templateName) {
		final Member loggedInUser = loggedInUserService.getLoggedInMember();
		
		final ModelAndView mv = new ModelAndView(templateName);
		mv.addObject("loggedInUser", loggedInUser.getId());
		
    	final int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUser.getId());	// TODO should be a post handler?
    	if (pendingOutingsCountFor > 0) {
    		mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
    	}

		final int memberDetailsProblems = governingBodyFactory.getGoverningBody().checkRegistrationNumber(loggedInUser.getRegistrationNumber()) != null ? 1 : 0;
    	if (memberDetailsProblems > 0) {
    		mv.addObject("memberDetailsProblems", memberDetailsProblems);
    	}
    	
    	try {
    		mv.addObject("preferredSquad", preferedSquadService.resolvedPreferedSquad(loggedInUser));
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
		return mv;
	}
	
}
