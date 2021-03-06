package uk.co.squadlist.web.controllers.social;

import com.google.common.base.Strings;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.scope.ScopeBuilder;
import com.restfb.types.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.InvalidMemberException;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.urls.UrlBuilder;

import java.io.IOException;

@Controller
public class FacebookSigninController {

    private final static Logger log = LogManager.getLogger(FacebookSigninController.class);

    private final UrlBuilder urlBuilder;
    private final FacebookOauthStateService facebookOauthStateService;
    private final LoggedInUserService loggedInUserService;
    private final FacebookLinkedAccountsService facebookLinkedAccountsService;

    private String facebookClientId;
    private String facebookClientSecret;

    private InstanceConfig instanceConfig;
    private SquadlistApi api;
    private String clientId;
    private String clientSecret;

    private final static Version FACEBOOK_API_VERSION = Version.LATEST;

    @Autowired
    public FacebookSigninController(UrlBuilder urlBuilder, FacebookOauthStateService facebookOauthStateService,
                                    LoggedInUserService loggedInUserService, FacebookLinkedAccountsService facebookLinkedAccountsService,
                                    @Value("${facebook.clientId}") String facebookClientId,
                                    @Value("${facebook.clientSecret}") String facebookClientSecret,
                                    InstanceConfig instanceConfig,
                                    SquadlistApiFactory squadlistApiFactory,
                                    @Value("${client.id}") String clientId,
                                    @Value("${client.secret}") String clientSecret) throws IOException {
        this.urlBuilder = urlBuilder;
        this.facebookOauthStateService = facebookOauthStateService;
        this.loggedInUserService = loggedInUserService;
        this.facebookLinkedAccountsService = facebookLinkedAccountsService;
        this.facebookClientId = facebookClientId;
        this.facebookClientSecret = facebookClientSecret;
        this.instanceConfig = instanceConfig;
        this.api = squadlistApiFactory.createClient();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @RequestMapping(value = "/social/facebook/link", method = RequestMethod.GET)
    public ModelAndView link() {
        String facebookLoginDialogUrl = buildFacebookLoginRedirectUrl(urlBuilder.getLinkFacebookCallbackUrl());
        log.info("Redirecting user to facebook auth url: " + facebookLoginDialogUrl);
        return new ModelAndView(new RedirectView(facebookLoginDialogUrl));
    }

    @RequestMapping(value = "/social/facebook/link/callback", method = RequestMethod.GET)
    public ModelAndView facebookLinkCallback(@RequestParam(required = false) String code, @RequestParam(required = false) String state) throws IOException, UnknownMemberException, SignedInMemberRequiredException, InvalidMemberException {
        if (code == null || state == null) {
            log.warn("Not a complete Facebook callback; redirecting back to social settings");
            return redirectToSocialSettings();
        }

        log.info("Got facebook auth callback code and state; exchanging for facebook token: " + code + ", " + state);
        facebookOauthStateService.clearState(state);

        com.restfb.FacebookClient.AccessToken facebookUserAccessToken = getFacebookUserToken(code, urlBuilder.getLinkFacebookCallbackUrl());
        log.info("Got access token: " + facebookUserAccessToken);

        final User facebookUser = getFacebookUserById(facebookUserAccessToken);
        facebookLinkedAccountsService.linkAccount(loggedInUserService.getLoggedInMember().getId(), facebookUser.getId());        // TODO Persist the access token as well to permit us to tell the user which Facebook account they are linked to

        return redirectToSocialSettings();
    }

    @RequestMapping(value = "/social/facebook/signin", method = RequestMethod.GET)
    public ModelAndView signin() {
        String facebookLoginDialogUrl = buildFacebookLoginRedirectUrl(urlBuilder.facebookSigninCallbackUrl());
        log.info("Redirecting user to facebook auth url: " + facebookLoginDialogUrl);
        return new ModelAndView(new RedirectView(facebookLoginDialogUrl));
    }

    @RequestMapping(value = "/social/facebook/signin/callback", method = RequestMethod.GET)
    public ModelAndView facebookSigninCallback(@RequestParam(required = true) String code, @RequestParam(required = true) String state) throws IOException {
        if (Strings.isNullOrEmpty(code) || Strings.isNullOrEmpty(state)) {
            return redirectToSignin();
        }

        log.info("Got facebook auth callback code and state; exchanging for facebook token: " + code + ", " + state);
        facebookOauthStateService.clearState(state);

        FacebookClient.AccessToken facebookUserAccessToken = getFacebookUserToken(code, urlBuilder.facebookSigninCallbackUrl());
        log.info("Got Facebook access token: " + facebookUserAccessToken);

        final String authenticatedUsersAccessToken = authWithFacebook(facebookUserAccessToken.getAccessToken());
        if (authenticatedUsersAccessToken == null) {
            log.warn("No linked Facebook account");
            return new ModelAndView(new RedirectView(urlBuilder.loginUrl() + "?errors=true"));    // TODO specific error
        }

        Member authenticatedMember = api.verify(authenticatedUsersAccessToken);
        if (authenticatedMember != null) {
            log.info("Auth successful for user: " + authenticatedMember.getId());
            loggedInUserService.setSignedIn(authenticatedUsersAccessToken);
            return new ModelAndView(new RedirectView(urlBuilder.getBaseUrl()));  // TODO can get normal redirect to wanted page?

        } else {
            return new ModelAndView(new RedirectView(urlBuilder.loginUrl() + "?errors=true"));  // TODO specific error
        }
    }

    @RequestMapping(value = "/social/facebook/remove", method = RequestMethod.GET)
    public ModelAndView remove() throws UnknownMemberException, SignedInMemberRequiredException, InvalidMemberException {
        facebookLinkedAccountsService.removeLinkage(loggedInUserService.getLoggedInMember().getId());    // TODO Should tell Facebook to invalidate the token as well
        return redirectToSocialSettings();
    }

    private String buildFacebookLoginRedirectUrl(String redirectUrl) {
        ScopeBuilder scopeBuilder = new ScopeBuilder();
        FacebookClient client = new DefaultFacebookClient(FACEBOOK_API_VERSION);
        return client.getLoginDialogUrl(facebookClientId, redirectUrl, scopeBuilder) + "&state=" + facebookOauthStateService.registerState();
    }

    private FacebookClient.AccessToken getFacebookUserToken(String code, String redirectUrl) throws IOException {
        FacebookClient.AccessToken accessToken = new DefaultFacebookClient(FACEBOOK_API_VERSION).obtainUserAccessToken(facebookClientId, facebookClientSecret, redirectUrl, code);
        return accessToken;
    }

    private com.restfb.types.User getFacebookUserById(com.restfb.FacebookClient.AccessToken accessToken) {
        final FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken(), FACEBOOK_API_VERSION);
        return facebookClient.fetchObject("me", com.restfb.types.User.class);
    }

    private ModelAndView redirectToSignin() {
        return new ModelAndView(new RedirectView(urlBuilder.loginUrl()));
    }

    private ModelAndView redirectToSocialSettings() {
        return new ModelAndView(new RedirectView(urlBuilder.socialMediaAccounts()));
    }

    private String authWithFacebook(String facebookAccessToken) {
        try {
            return api.requestAccessTokenWithFacebook(instanceConfig.getInstance(), facebookAccessToken, clientId, clientSecret);

        } catch (Exception e) {
            log.error("Uncaught error", e);    // TODO
            return null;
        }
    }
}