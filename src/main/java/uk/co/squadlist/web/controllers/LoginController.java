package uk.co.squadlist.web.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@Controller
public class LoginController {

    private final static Logger log = LogManager.getLogger(LoginController.class);

    private final InstanceConfig instanceConfig;
    private final String clientId;
    private final String clientSecret;
    private final LoggedInUserService loggedInUserService;
    private final UrlBuilder urlBuilder;
    private final SquadlistApiFactory squadlistApiFactory;
    private final ViewFactory viewFactory;
    private final DefaultApi swaggerApi;

    @Autowired
    public LoginController(LoggedInUserService loggedInUserService, UrlBuilder urlBuilder,
                           InstanceConfig instanceConfig,
                           SquadlistApiFactory squadlistApiFactory,
                           ViewFactory viewFactory,
                           @Value("${client.id}") String clientId,
                           @Value("${client.secret}") String clientSecret
    ) throws IOException {
        this.loggedInUserService = loggedInUserService;
        this.urlBuilder = urlBuilder;
        this.instanceConfig = instanceConfig;
        this.swaggerApi = squadlistApiFactory.createSwaggerClient();
        this.squadlistApiFactory = squadlistApiFactory;
        this.viewFactory = viewFactory;

        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ModelAndView login(HttpServletRequest request) throws Exception {
        boolean error = false;
        String username = null;
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            Boolean flashedError = (Boolean) inputFlashMap.get("error");
            error = flashedError != null ? flashedError : false;
            username = (String) inputFlashMap.get("username");
        }
        return renderLoginScreen(error, username);
    }

    @RequestMapping(value = "/login", method = {RequestMethod.POST})
    public ModelAndView loginSubmit(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password,
            RedirectAttributes redirectAttributes) {

        // Use the OAuth password flow to swap our user's username and password for an access token
        log.info("Attempting to auth user: " + username);
        final String authenticatedUsersAccessToken = auth(username, password);
        log.info("Auth got access token for: " + username);

        if (authenticatedUsersAccessToken != null) {
            try {
                // Call the API verify end point with the new access token to obtain the signed in user
                log.info("Verifying access token to find signed in user");
                Member authenticatedMember = squadlistApiFactory.createSwaggerApiClientForToken(authenticatedUsersAccessToken).verifyPost();
                if (authenticatedMember != null) {
                    log.info("Auth successful for user: " + username);
                    loggedInUserService.setSignedIn(authenticatedUsersAccessToken);
                    return viewFactory.redirectionTo(urlBuilder.getBaseUrl());

                } else {
                    log.warn("Verified user was null; this should not happen for valid access tokens");
                }

            } catch (ApiException e) {
                log.warn("ApiException during verify: " + e.getCode() + " / " + e.getResponseBody());
            }
        }

        redirectAttributes.addFlashAttribute("error", true);
        redirectAttributes.addFlashAttribute("username", username);
        return viewFactory.redirectionTo(urlBuilder.loginUrl());
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET})
    public ModelAndView logout() {
        loggedInUserService.cleanSignedIn();
        return viewFactory.redirectionTo(urlBuilder.loginUrl());
    }

    private ModelAndView renderLoginScreen(boolean errors, String username) throws ApiException {
        final Instance instance = swaggerApi.getInstance(instanceConfig.getInstance());
        return viewFactory.getViewFor("login", instance).
                addObject("title", instance.getName()).
                addObject("username", username).
                addObject("errors", errors);
    }

    private String auth(String username, String password) {
        try {
            final SquadlistApi api = squadlistApiFactory.createClient();
            return api.requestAccessToken(instanceConfig.getInstance(), username, password, clientId, clientSecret);

        } catch (Exception e) {
            log.error("Uncaught error", e);    // TODO
            return null;
        }
    }

}
