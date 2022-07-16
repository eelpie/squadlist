package uk.co.squadlist.web.controllers.social;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.InvalidMemberException;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.urls.UrlBuilder;

@Controller
public class FacebookSigninController {

    private final UrlBuilder urlBuilder;
    private final LoggedInUserService loggedInUserService;
    private final FacebookLinkedAccountsService facebookLinkedAccountsService;

    @Autowired
    public FacebookSigninController(UrlBuilder urlBuilder,
                                    LoggedInUserService loggedInUserService,
                                    FacebookLinkedAccountsService facebookLinkedAccountsService) {
        this.urlBuilder = urlBuilder;
        this.loggedInUserService = loggedInUserService;
        this.facebookLinkedAccountsService = facebookLinkedAccountsService;
    }


    @RequestMapping(value = "/social/facebook/remove", method = RequestMethod.GET)
    public ModelAndView remove() throws UnknownMemberException, SignedInMemberRequiredException, InvalidMemberException {
        facebookLinkedAccountsService.removeLinkage(loggedInUserService.getLoggedInMember().getId());    // TODO Should tell Facebook to invalidate the token as well
        return redirectToSocialSettings();
    }

    private ModelAndView redirectToSocialSettings() {
        return new ModelAndView(new RedirectView(urlBuilder.socialMediaAccounts()));
    }

}