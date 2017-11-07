package uk.co.squadlist.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.urls.UrlBuilder;

@Controller
public class LoginController {

    private final static Logger log = Logger.getLogger(LoginController.class);

    private final InstanceSpecificApiClient api;
    private final LoggedInUserService loggedInUserService;
    private final UrlBuilder urlBuilder;

    @Autowired
    public LoginController(InstanceSpecificApiClient api, LoggedInUserService loggedInUserService, UrlBuilder urlBuilder) {
        this.api = api;
        this.loggedInUserService = loggedInUserService;
        this.urlBuilder = urlBuilder;
    }

    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.HEAD})     // TODO SEO this onto the root url
    public ModelAndView login(@RequestParam(value = "errors", required = false) Boolean errors) throws Exception {
        final ModelAndView mv = new ModelAndView("login");
        final Instance instance = api.getInstance();
        mv.addObject("title", instance.getName());
        mv.addObject("instance", instance);
        if (errors != null && errors) {
            mv.addObject("errors", true);
        }
        return mv;
    }

    @RequestMapping(value = "/login", method = {RequestMethod.POST})
    public ModelAndView loginSubmit(
            @RequestParam(value = "username", required = true) String username,
            @RequestParam(value = "password", required = true) String password) throws Exception {

        log.info("Attempting to auth user: " + username);
        final String authenticatedUsersAccessToken = api.auth(username, password);
        if (authenticatedUsersAccessToken != null) {
            Member authenticatedMember = api.verify(authenticatedUsersAccessToken);
            if (authenticatedMember != null) {
                log.info("Auth successful for user: " + username);
                loggedInUserService.setSignedIn(authenticatedUsersAccessToken);
                return new ModelAndView(new RedirectView(urlBuilder.getBaseUrl()));
            }
        }

        return new ModelAndView(new RedirectView(urlBuilder.loginUrl()));    // TODO errors
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET})
    public ModelAndView logout() throws Exception {
        loggedInUserService.cleanSignedIn();
        return new ModelAndView(new RedirectView(urlBuilder.loginUrl()));
    }

}
