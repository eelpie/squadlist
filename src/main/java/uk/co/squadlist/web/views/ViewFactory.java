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
    private final PreferredSquadService preferredSquadService;

    @Autowired
    public ViewFactory(LoggedInUserService loggedInUserService,
                       PreferredSquadService preferredSquadService) {
        this.loggedInUserService = loggedInUserService;
        this.preferredSquadService = preferredSquadService;
    }

    public ModelAndView getViewForLoggedInUser(String templateName) throws SignedInMemberRequiredException, UnknownInstanceException {
        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final ModelAndView mv = new ModelAndView(templateName);
        mv.addObject("loggedInUser", loggedInUser.getId());

        try {
            mv.addObject("preferredSquad", preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mv;
    }

}
