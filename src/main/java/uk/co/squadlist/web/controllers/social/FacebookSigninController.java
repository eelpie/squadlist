package uk.co.squadlist.web.controllers.social;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.urls.UrlBuilder;

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
			LoggedInUserService loggedInUserService, FacebookLinkedAccountsService facebookLinkedAccountsService) {
		this.urlBuilder = urlBuilder;
		this.facebookOauthStateService = facebookOauthStateService;
		this.loggedInUserService = loggedInUserService;
		this.facebookLinkedAccountsService = facebookLinkedAccountsService;
	}
		
	@RequestMapping(value="/social/facebook/link", method=RequestMethod.GET)
	public ModelAndView link() {
		final String facebookAuthUrl = "https://www.facebook.com/dialog/oauth?client_id=" + facebookClientId + 
			"&redirect_uri=" + urlBuilder.getFacebookCallbackUrl() + "&state=" + facebookOauthStateService.registerState();
		
		log.info("Redirecting user to facebook auth url: " + facebookAuthUrl);
		return new ModelAndView(new RedirectView(facebookAuthUrl));	
	}
	
	@RequestMapping(value="/social/facebook/remove", method=RequestMethod.GET)
	public ModelAndView remove() {
		facebookLinkedAccountsService.removeLinkage(loggedInUserService.getLoggedInUser());
		return new ModelAndView(new RedirectView(urlBuilder.socialMediaAccounts()));	
	}
	
	@RequestMapping(value="/social/facebook/callback", method=RequestMethod.GET)
	public ModelAndView facebookCallback(@RequestParam(required=true) String code, @RequestParam(required=true) String state) throws IOException {				
		log.info("Got facebook auth callback code and state; exchanging for facebook token: " + code + ", " + state);
		com.restfb.FacebookClient.AccessToken facebookUserAccessToken = getFacebookUserToken(code,  urlBuilder.getFacebookCallbackUrl());
		facebookOauthStateService.clearState(state);
		log.info("Got access token: " + facebookUserAccessToken);
		
		final User facebookUser = getFacebookUserById(facebookUserAccessToken);
		facebookLinkedAccountsService.linkAccount(loggedInUserService.getLoggedInUser(), facebookUser.getId());
		
		return new ModelAndView(new RedirectView(urlBuilder.socialMediaAccounts()));		
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
