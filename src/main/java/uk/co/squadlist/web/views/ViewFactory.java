package uk.co.squadlist.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.Member;
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

    public ModelAndView getViewForLoggedInUser(String templateName, Member loggedInUser) throws SignedInMemberRequiredException, UnknownInstanceException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final ModelAndView mv = new ModelAndView(templateName);
        try {
            mv.addObject("preferredSquad", preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads()));    // TODO this is questionable; push to navitems?
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mv;
    }

    public ModelAndView redirectionTo(String url) {
        RedirectView redirectView = new RedirectView(url);
        redirectView.setExposeModelAttributes(false);
        return new ModelAndView(redirectView);
    }

}
