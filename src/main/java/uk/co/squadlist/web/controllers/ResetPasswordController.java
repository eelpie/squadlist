package uk.co.squadlist.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.UnknownMemberException;

import com.google.common.base.Strings;

@Controller
public class ResetPasswordController {
	
	private final static Logger log = Logger.getLogger(ResetPasswordController.class);
	
	private final InstanceSpecificApiClient api;
	
	@Autowired
	public ResetPasswordController(InstanceSpecificApiClient api) {
		this.api = api;
	}
	
	@RequestMapping(value = "/reset-password", method = RequestMethod.GET)
	public ModelAndView resetPasswordPrompt() throws Exception {
		final ModelAndView mv = new ModelAndView("resetPassword");
		mv.addObject("instance", api.getInstance());
		return mv;
	}
	
	@RequestMapping(value = "/reset-password", method = RequestMethod.POST)
	public ModelAndView resetPassword(@RequestParam(value = "username", required = false) String username) throws Exception {
		if (Strings.isNullOrEmpty(username)) {
			final ModelAndView mv = new ModelAndView("resetPassword");
			mv.addObject("instance", api.getInstance());
			mv.addObject("errors", true);
			return mv;
		}
		
		log.info("Reseting password for: " + username);
		try {
			api.resetPassword(username);	// TODO errors	
			log.info("Reset password call successful for: " + username);
			final ModelAndView mv = new ModelAndView("resetPasswordSent");
			mv.addObject("instance", api.getInstance());		
			return mv;		
			
		} catch (UnknownMemberException e) {
			final ModelAndView mv = new ModelAndView("resetPassword");
			mv.addObject("instance", api.getInstance());
			mv.addObject("errors", true);
			return mv;
		}
	}
	
	@RequestMapping(value = "/reset-password/confirm", method = RequestMethod.GET)
	public ModelAndView confirmPasswordReset(@RequestParam String token) throws Exception {
		final String newPassword = api.confirmResetPassword(token);
		
		final ModelAndView mv = new ModelAndView("resetPasswordConfirm");
		mv.addObject("instance", api.getInstance());
		mv.addObject("newPassword", newPassword);
		return mv;
	}
	
}
