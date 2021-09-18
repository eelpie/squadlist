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
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.UnknownMemberException;

import java.io.IOException;

@Controller
public class ResetPasswordController {

    private final static Logger log = LogManager.getLogger(ResetPasswordController.class);

    private final InstanceConfig instanceConfig;
    private final SquadlistApi squadlistApi;

    @Autowired
    public ResetPasswordController(InstanceConfig instanceConfig, SquadlistApiFactory squadlistApiFactory) throws IOException {
        this.instanceConfig = instanceConfig;
        this.squadlistApi = squadlistApiFactory.createClient();
    }

    @RequestMapping(value = "/reset-password", method = RequestMethod.GET)
    public ModelAndView resetPasswordPrompt() throws Exception {
        return new ModelAndView("resetPassword").addObject("title", "Reset password");
    }

    @RequestMapping(value = "/reset-password", method = RequestMethod.POST)
    public ModelAndView resetPassword(@RequestParam(value = "username", required = false) String username) throws Exception {
        if (Strings.isNullOrEmpty(username)) {
            return new ModelAndView("resetPassword").
                    addObject("errors", true).
                    addObject("title", "Reset password");
        }

        String instance = instanceConfig.getInstance();
        log.info("Resetting password for: " + instance + " / " + username);
        try {
            squadlistApi.resetPassword(instance, username.trim());    // TODO errors
            log.info("Reset password call successful for: " + username);
            return new ModelAndView("resetPasswordSent").addObject("title", "Reset password");

        } catch (UnknownMemberException e) {
            return new ModelAndView("resetPassword").
                    addObject("title", "Reset password").
                    addObject("errors", true);
        }
    }

    @RequestMapping(value = "/reset-password/confirm", method = RequestMethod.GET)
    public ModelAndView confirmPasswordReset(@RequestParam String token) throws Exception {
        try {
            final String newPassword = squadlistApi.confirmResetPassword(instanceConfig.getInstance(), token);
            return new ModelAndView("resetPasswordConfirm").addObject("newPassword", newPassword);

        } catch (Exception e) {
            log.warn("Reset password failed", e);
            return new ModelAndView("resetPasswordInvalidToken");
        }
    }

}
