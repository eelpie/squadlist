package uk.co.squadlist.web.controllers;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;

import java.io.IOException;

@Controller
public class ResetPasswordController {

    private final static Logger log = LogManager.getLogger(ResetPasswordController.class);

    private final InstanceConfig instanceConfig;
    private final DefaultApi squadlistApi;
    private final ViewFactory viewFactory;
    private final UrlBuilder urlBuilder;

    @Autowired
    public ResetPasswordController(InstanceConfig instanceConfig,
                                   SquadlistApiFactory squadlistApiFactory,
                                   ViewFactory viewFactory,
                                   UrlBuilder urlBuilder) throws IOException {
        this.instanceConfig = instanceConfig;
        this.squadlistApi = squadlistApiFactory.createSwaggerClient();
        this.viewFactory = viewFactory;
        this.urlBuilder = urlBuilder;
    }

    @RequestMapping(value = "/reset-password", method = RequestMethod.GET)
    public ModelAndView resetPasswordPrompt() throws Exception {
        Instance instance = squadlistApi.getInstance(instanceConfig.getInstance());
        return viewFactory.getViewFor("resetPassword", instance).
                addObject("title", "Reset password");
    }

    @RequestMapping(value = "/reset-password", method = RequestMethod.POST)
    public ModelAndView resetPassword(@RequestParam(value = "username", required = false) String username) throws Exception {
        Instance instance = squadlistApi.getInstance(instanceConfig.getInstance());

        if (Strings.isNullOrEmpty(username)) {
            return viewFactory.redirectionTo("/reset-password");
        }

        log.info("Resetting password for: " + instance.getId() + " / " + username);
        try {
            squadlistApi.instancesInstanceResetPasswordPost(instance.getId(), username.trim());    // TODO errors
            log.info("Reset password call successful for: " + username);
            return viewFactory.getViewFor("resetPasswordSent", instance).
                    addObject("title", "Reset password");

        } catch (ApiException e) {  // TODO more precise; use redirect pattern
            log.warn(e.getCode() + " / " + e.getResponseBody());
            return viewFactory.getViewFor("resetPassword", instance).
                    addObject("title", "Reset password").
                    addObject("errors", true);
        }
    }

    @RequestMapping(value = "/reset-password/confirm", method = RequestMethod.GET)
    public ModelAndView confirmPasswordReset(@RequestParam String token) throws Exception {
        final Instance instance = squadlistApi.getInstance(instanceConfig.getInstance());

        try {
            final String newPassword = squadlistApi.instancesInstanceResetPasswordConfirmPost(instance.getId(), token);
            return viewFactory.getViewFor("resetPasswordConfirm", instance).
                    addObject("newPassword", newPassword);

        } catch (Exception e) {
            log.warn("Reset password failed", e);
            return new ModelAndView("resetPasswordInvalidToken");
        }
    }

}
