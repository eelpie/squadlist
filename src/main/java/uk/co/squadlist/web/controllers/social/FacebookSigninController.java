package uk.co.squadlist.web.controllers.social;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.scope.ScopeBuilder;
import com.restfb.scope.UserDataPermissions;
import com.restfb.types.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.urls.UrlBuilder;

import java.io.IOException;
import java.util.Collection;

@Controller
public class FacebookSigninController {

	private final static Logger log = Logger.getLogger(FacebookSigninController.class);

	private final UrlBuilder urlBuilder;
	private final FacebookOauthStateService facebookOauthStateService;
	private final LoggedInUserService loggedInUserService;
	private final FacebookLinkedAccountsService facebookLinkedAccountsService;
	private final InstanceSpecificApiClient api;

	private String facebookClientId;
	private String facebookClientSecret;

	private final static Version FACEBOOK_API_VERSION = Version.LATEST;

	@Autowired
	public FacebookSigninController(UrlBuilder urlBuilder, FacebookOauthStateService facebookOauthStateService,
			LoggedInUserService loggedInUserService, FacebookLinkedAccountsService facebookLinkedAccountsService,
			InstanceSpecificApiClient api,
			 @Value("${facebook.clientId}") String facebookClientId,
			 @Value("${facebook.clientSecret}") String facebookClientSecret) {
		this.urlBuilder = urlBuilder;
		this.facebookOauthStateService = facebookOauthStateService;
		this.loggedInUserService = loggedInUserService;
		this.facebookLinkedAccountsService = facebookLinkedAccountsService;
		this.api = api;
		this.facebookClientId = facebookClientId;
		this.facebookClientSecret = facebookClientSecret;
	}

	@RequestMapping(value="/social/facebook/link", method=RequestMethod.GET)
	public ModelAndView link() {
		String facebookLoginDialogUrl = buildFacebookLoginRedirectUrl(urlBuilder.getLinkFacebookCallbackUrl());
		log.info("Redirecting user to facebook auth url: " + facebookLoginDialogUrl);
		return new ModelAndView(new RedirectView(facebookLoginDialogUrl));
	}

	@RequestMapping(value="/social/facebook/link/callback", method=RequestMethod.GET)
	public ModelAndView facebookLinkCallback(@RequestParam(required=false) String code, @RequestParam(required=false) String state) throws IOException, UnknownMemberException {
		if (code == null || state == null) {
			log.warn("Not a complete Facebook callback; redirecting back to social settings");
			return redirectToSocialSettings();
		}

		log.info("Got facebook auth callback code and state; exchanging for facebook token: " + code + ", " + state);
		facebookOauthStateService.clearState(state);

		com.restfb.FacebookClient.AccessToken facebookUserAccessToken = getFacebookUserToken(code,  urlBuilder.getLinkFacebookCallbackUrl());
		log.info("Got access token: " + facebookUserAccessToken);

		final User facebookUser = getFacebookUserById(facebookUserAccessToken);
		facebookLinkedAccountsService.linkAccount(loggedInUserService.getLoggedInMember().getId(), facebookUser.getId());

		return redirectToSocialSettings();
	}

	@RequestMapping(value="/social/facebook/signin", method=RequestMethod.GET)
	public ModelAndView signin() {
		String facebookLoginDialogUrl = buildFacebookLoginRedirectUrl(urlBuilder.facebookSigninCallbackUrl());
		log.info("Redirecting user to facebook auth url: " + facebookLoginDialogUrl);
		return new ModelAndView(new RedirectView(facebookLoginDialogUrl));
	}

	@RequestMapping(value="/social/facebook/signin/callback", method=RequestMethod.GET)
	public ModelAndView facebookSigninCallback(@RequestParam(required=true) String code, @RequestParam(required=true) String state) throws IOException {
		if (Strings.isNullOrEmpty(code) || Strings.isNullOrEmpty(state)) {
			return redirectToSignin();
		}
		
		log.info("Got facebook auth callback code and state; exchanging for facebook token: " + code + ", " + state);
		facebookOauthStateService.clearState(state);

		FacebookClient.AccessToken facebookUserAccessToken = getFacebookUserToken(code,  urlBuilder.facebookSigninCallbackUrl());
		log.info("Got access token: " + facebookUserAccessToken);

		final Member linkedMember = api.authFacebook(facebookUserAccessToken.getAccessToken());
		if (linkedMember == null) {
			log.warn("No linked Facebook account");
			return new ModelAndView(new RedirectView(urlBuilder.loginUrl() + "?errors=true"));	// TODO specific error
		}

		setLoggedInSpringUser(linkedMember);

		return new ModelAndView(new RedirectView(urlBuilder.applicationUrl("/")));	// TODO can get normal redirect to wanted page?
	}

	@RequestMapping(value="/social/facebook/remove", method=RequestMethod.GET)
	public ModelAndView remove() throws UnknownMemberException {
		facebookLinkedAccountsService.removeLinkage(loggedInUserService.getLoggedInMember().getId());	// TODO Should tell Facebook to invalidate the token as well
		return redirectToSocialSettings();
	}

	private String buildFacebookLoginRedirectUrl(String redirectUrl) {
		ScopeBuilder scopeBuilder = new ScopeBuilder();
		scopeBuilder.addPermission(UserDataPermissions.USER_ABOUT_ME);

		FacebookClient client = new DefaultFacebookClient(Version.VERSION_2_6);
		return client.getLoginDialogUrl(facebookClientId, redirectUrl, scopeBuilder) + "&state=" + facebookOauthStateService.registerState();
	}

	private FacebookClient.AccessToken getFacebookUserToken(String code, String redirectUrl) throws IOException {
		FacebookClient.AccessToken accessToken = new DefaultFacebookClient(Version.VERSION_2_6).obtainUserAccessToken(facebookClientId, facebookClientSecret, redirectUrl, code);
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

	private void setLoggedInSpringUser(final Member linkedMember) {	// TODO this is abit of a grey area; why are we missing with native Spring?
		log.info("Authenticating user: " + linkedMember);
		Collection<SimpleGrantedAuthority> authorities = Lists.newArrayList();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		UserDetails userDetail = new org.springframework.security.core.userdetails.User(linkedMember.getId(), "unknown", authorities);
		Authentication authentication = new PreAuthenticatedAuthenticationToken(userDetail, null, authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

}