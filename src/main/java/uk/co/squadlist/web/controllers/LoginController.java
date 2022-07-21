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
import org.springframework.web.servlet.view.RedirectView;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.urls.UrlBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@Controller
public class LoginController {

    private final static Logger log = LogManager.getLogger(LoginController.class);

    private final InstanceConfig instanceConfig;
    private final SquadlistApi api;
    private final String clientId;
    private final String clientSecret;
    private final LoggedInUserService loggedInUserService;
    private final UrlBuilder urlBuilder;

    @Autowired
    public LoginController(LoggedInUserService loggedInUserService, UrlBuilder urlBuilder,
                           InstanceConfig instanceConfig,
                           SquadlistApiFactory squadlistApiFactory,
                           @Value("${client.id}") String clientId,
                           @Value("${client.secret}") String clientSecret
    ) throws IOException {
        this.loggedInUserService = loggedInUserService;
        this.urlBuilder = urlBuilder;
        this.instanceConfig = instanceConfig;
        this.api = squadlistApiFactory.createClient();

        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.HEAD})
    // TODO SEO this onto the root url
    public ModelAndView login(HttpServletRequest request) throws Exception {
        boolean error = false;
        String username = null;
        Map inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            Boolean flashedError = (Boolean) inputFlashMap.get("error");
            error = flashedError != null ? flashedError : false;
            username = (String) inputFlashMap.get("username");
        }
        return renderLoginScreen(error, username);
    }

    @RequestMapping(value = "/login", method = {RequestMethod.POST})
    public ModelAndView loginSubmit(
            @RequestParam(value = "username", required = true) String username,
            @RequestParam(value = "password", required = true) String password,
            RedirectAttributes redirectAttributes) {

        log.info("Attempting to auth user: " + username);
        final String authenticatedUsersAccessToken = auth(username, password);
        log.info("Auth got access token: " + authenticatedUsersAccessToken);
        if (authenticatedUsersAccessToken != null) {
            Member authenticatedMember = api.verify(authenticatedUsersAccessToken);
            if (authenticatedMember != null) {
                log.info("Auth successful for user: " + username);
                loggedInUserService.setSignedIn(authenticatedUsersAccessToken);
                return redirectionTo(urlBuilder.getBaseUrl());
            }
        }
        redirectAttributes.addFlashAttribute("error", true);
        redirectAttributes.addFlashAttribute("username", username);
        return redirectionTo(urlBuilder.loginUrl());
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET})
    public ModelAndView logout() {
        loggedInUserService.cleanSignedIn();
        return redirectionTo(urlBuilder.loginUrl());
    }

    private ModelAndView renderLoginScreen(boolean errors, String username) throws UnknownInstanceException {
        final Instance instance = api.getInstance(instanceConfig.getInstance());
        return new ModelAndView("login").
                addObject("title", instance.getName()).
                addObject("username", username).
                addObject("errors", errors);
    }

    private String auth(String username, String password) {
        try {
            return api.requestAccessToken(instanceConfig.getInstance(), username, password, clientId, clientSecret);

        } catch (Exception e) {
            log.error("Uncaught error", e);    // TODO
            return null;
        }
    }

    private ModelAndView redirectionTo(String url) {
        RedirectView redirectView = new RedirectView(url);
        redirectView.setExposeModelAttributes(false);
        return new ModelAndView(redirectView);
    }

}
