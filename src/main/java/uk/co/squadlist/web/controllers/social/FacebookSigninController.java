package uk.co.squadlist.web.controllers.social;

import java.io.IOException;
import java.util.Collection;

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

import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.urls.UrlBuilder;

import com.google.common.collect.Lists;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultWebRequestor;
import com.restfb.FacebookClient;
import com.restfb.WebRequestor;
import com.restfb.types.User;

@Controller
public class FacebookSigninController {

	private final static Logger log = Logger.getLogger(FacebookSigninController.class);
	
	private final UrlBuilder urlBuilder;
	private final FacebookOauthStateService facebookOauthStateService;
	private final LoggedInUserService loggedInUserService;
	private final FacebookLinkedAccountsService facebookLinkedAccountsService;
	
	private String facebookClientId = "1499025690313695";
	private String facebookClientSecret = "ef47cf4e85efba72255a2b8be663dd78";
	
	@Autowired
	public FacebookSigninController(UrlBuilder urlBuilder, FacebookOauthStateService facebookOauthStateService,
			LoggedInUserService loggedInUserService, FacebookLinkedAccountsService facebookLinkedAccountsService,
			 @Value("#{squadlist['facebook.clientId']}") String facebookClientId,
			 @Value("#{squadlist['facebook.clientSecret']}") String facebookClientSecret) {
		this.urlBuilder = urlBuilder;
		this.facebookOauthStateService = facebookOauthStateService;
		this.loggedInUserService = loggedInUserService;
		this.facebookLinkedAccountsService = facebookLinkedAccountsService;
	}
		
	@RequestMapping(value="/social/facebook/link", method=RequestMethod.GET)
	public ModelAndView link() {
		final String facebookAuthUrl = "https://www.facebook.com/dialog/oauth?client_id=" + facebookClientId + 
			"&redirect_uri=" + urlBuilder.getLinkFacebookCallbackUrl() + "&state=" + facebookOauthStateService.registerState();
		
		log.info("Redirecting user to facebook auth url: " + facebookAuthUrl);
		return new ModelAndView(new RedirectView(facebookAuthUrl));	
	}
	
	@RequestMapping(value="/social/facebook/signin", method=RequestMethod.GET)
	public ModelAndView signin() {
		final String facebookAuthUrl = "https://www.facebook.com/dialog/oauth?client_id=" + facebookClientId + 
			"&redirect_uri=" + urlBuilder.facebookSigninCallbackUrl() + "&state=" + facebookOauthStateService.registerState();
		
		log.info("Redirecting user to facebook auth url: " + facebookAuthUrl);
		return new ModelAndView(new RedirectView(facebookAuthUrl));	
	}
	
	
	@RequestMapping(value="/social/facebook/remove", method=RequestMethod.GET)
	public ModelAndView remove() {
		facebookLinkedAccountsService.removeLinkage(loggedInUserService.getLoggedInUser());
		return new ModelAndView(new RedirectView(urlBuilder.socialMediaAccounts()));	
	}
	
	@RequestMapping(value="/social/facebook/link/callback", method=RequestMethod.GET)
	public ModelAndView facebookLinkCallback(@RequestParam(required=true) String code, @RequestParam(required=true) String state) throws IOException {				
		log.info("Got facebook auth callback code and state; exchanging for facebook token: " + code + ", " + state);
		com.restfb.FacebookClient.AccessToken facebookUserAccessToken = getFacebookUserToken(code,  urlBuilder.getLinkFacebookCallbackUrl());
		facebookOauthStateService.clearState(state);
		log.info("Got access token: " + facebookUserAccessToken);
		
		final User facebookUser = getFacebookUserById(facebookUserAccessToken);
		facebookLinkedAccountsService.linkAccount(loggedInUserService.getLoggedInUser(), facebookUser.getId());
		
		return new ModelAndView(new RedirectView(urlBuilder.socialMediaAccounts()));		
	}
	
	@RequestMapping(value="/social/facebook/signin/callback", method=RequestMethod.GET)
	public ModelAndView facebookSigninCallback(@RequestParam(required=true) String code, @RequestParam(required=true) String state) throws IOException {				
		log.info("Got facebook auth callback code and state; exchanging for facebook token: " + code + ", " + state);
		com.restfb.FacebookClient.AccessToken facebookUserAccessToken = getFacebookUserToken(code,  urlBuilder.facebookSigninCallbackUrl());
		facebookOauthStateService.clearState(state);
		log.info("Got access token: " + facebookUserAccessToken);
		
		final User facebookUser = getFacebookUserById(facebookUserAccessToken);
		final String linkedMember = facebookLinkedAccountsService.getLinkedUserFor(facebookUser.getId());
		if (linkedMember == null) {
			throw new RuntimeException("No linked account");	// TODO
		}
		
		log.info("Authenticating user: " + linkedMember);
		Collection<SimpleGrantedAuthority> authorities = Lists.newArrayList();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		
		UserDetails userDetail = new org.springframework.security.core.userdetails.User(linkedMember, "unknown", authorities);
		
		Authentication authentication = new PreAuthenticatedAuthenticationToken(userDetail, null, authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	  	    		
		return new ModelAndView(new RedirectView(urlBuilder.applicationUrl("/")));	// TODO can get normal redirect to wanted page?
	}

	// http://stackoverflow.com/questions/13671694/restfb-using-a-facebook-app-to-get-the-users-access-token
	private com.restfb.FacebookClient.AccessToken getFacebookUserToken(String code, String redirectUrl) throws IOException {
		final String tokenRequestUrl = "https://graph.facebook.com/oauth/access_token?client_id=" + facebookClientId + "&redirect_uri=" + redirectUrl
		+ "&client_secret=" + facebookClientSecret + "&code=" + code;
		
		log.info("Requesting facebook access token from: " + tokenRequestUrl);
		
	    WebRequestor wr = new DefaultWebRequestor();	    
		WebRequestor.Response accessTokenResponse = wr.executeGet(tokenRequestUrl);
	    return DefaultFacebookClient.AccessToken.fromQueryString(accessTokenResponse.getBody());
	}
	
	private com.restfb.types.User getFacebookUserById(com.restfb.FacebookClient.AccessToken facebookUserAccessToken) {
         final FacebookClient facebookClient = new DefaultFacebookClient(facebookUserAccessToken.getAccessToken());
         final com.restfb.types.User facebookUser = facebookClient.fetchObject("me", com.restfb.types.User.class);               
         return facebookUser;
	 }
	
}
