package uk.co.squadlist.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.PreferedSquadService;

@Component
public class ViewFactory {
	
	private final LoggedInUserService loggedInUserService;
	private final OutingAvailabilityCountsService outingAvailabilityCountsService;
	private final PreferedSquadService preferedSquadService;
	private final GoverningBody governingBody;
	
	@Autowired
	public ViewFactory(LoggedInUserService loggedInUserService, OutingAvailabilityCountsService outingAvailabilityCountsService,
			PreferedSquadService preferedSquadService, GoverningBody governingBody) {
		this.loggedInUserService = loggedInUserService;
		this.outingAvailabilityCountsService = outingAvailabilityCountsService;
		this.preferedSquadService = preferedSquadService;
		this.governingBody = governingBody;
	}

	public ModelAndView getView(String templateName) {
		final Member loggedInUser = loggedInUserService.getLoggedInMember();
		
		final ModelAndView mv = new ModelAndView(templateName);
		mv.addObject("loggedInUser", loggedInUser.getId());
		
    	final int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUser.getId());	// TODO should be a post handler?
    	if (pendingOutingsCountFor > 0) {
    		mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
    	}
    	
    	final int memberDetailsProblems = governingBody.checkRegistrationNumber(loggedInUser.getRegistrationNumber()) != null ? 1 : 0;
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
