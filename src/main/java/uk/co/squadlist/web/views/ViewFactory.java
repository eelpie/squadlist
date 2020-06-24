package uk.co.squadlist.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.PreferredSquadService;

@Component
public class ViewFactory {

    private final LoggedInUserService loggedInUserService;
    private final OutingAvailabilityCountsService outingAvailabilityCountsService;
    private final PreferredSquadService preferredSquadService;
    private final GoverningBodyFactory governingBodyFactory;

    @Autowired
    public ViewFactory(LoggedInUserService loggedInUserService, OutingAvailabilityCountsService outingAvailabilityCountsService,
                       PreferredSquadService preferredSquadService, GoverningBodyFactory governingBodyFactory) {
        this.loggedInUserService = loggedInUserService;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.preferredSquadService = preferredSquadService;
        this.governingBodyFactory = governingBodyFactory;
    }

    public ModelAndView getViewForLoggedInUser(String templateName) throws SignedInMemberRequiredException, UnknownInstanceException {
        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final ModelAndView mv = new ModelAndView(templateName);
        mv.addObject("loggedInUser", loggedInUser.getId());
        final int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUser.getId(), loggedInUserApi);  // TODO should be a post handler?
        if (pendingOutingsCountFor > 0) {
            mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
        }

        final int memberDetailsProblems = governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance()).checkRegistrationNumber(loggedInUser.getRegistrationNumber()) != null ? 1 : 0;
        if (memberDetailsProblems > 0) {
            mv.addObject("memberDetailsProblems", memberDetailsProblems);
        }

        try {
            mv.addObject("preferredSquad", preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mv;
    }

}
