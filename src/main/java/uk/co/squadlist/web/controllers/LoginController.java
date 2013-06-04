package uk.co.squadlist.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.model.Instance;

import com.google.common.base.Strings;

@Controller
public class LoginController {
	
	private static Logger log = Logger.getLogger(LoginController.class);
	
	private final SquadlistApi api;
	private final InstanceConfig instanceConfig;
	
	@Autowired
	public LoginController(SquadlistApi api, InstanceConfig instanceConfig) {
		this.api = api;
		this.instanceConfig = instanceConfig;
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView member(@RequestParam(value = "errors", required = false) Boolean errors) throws Exception {
		final ModelAndView mv = new ModelAndView("login");
		mv.addObject("instance", getInstance());
		if (errors != null && errors) {
			mv.addObject("errors", true);
		}
		return mv;
	}
	
	@RequestMapping(value = "/reset-password", method = RequestMethod.GET)
	public ModelAndView resetPasswordPrompt() throws Exception {
		final ModelAndView mv = new ModelAndView("resetPassword");
		mv.addObject("instance", getInstance());
		return mv;
	}
	
	@RequestMapping(value = "/reset-password", method = RequestMethod.POST)
	public ModelAndView resetPassword(@RequestParam(value = "username", required = false) String username) throws Exception {
		if (Strings.isNullOrEmpty(username)) {
			final ModelAndView mv = new ModelAndView("resetPassword");
			mv.addObject("instance", getInstance());
			mv.addObject("errors", true);
			return mv;
		}
		
		log.info("Reseting password for: " + username);
		api.resetPassword(instanceConfig.getInstance(), username);	// TODO errors	
		
		log.info("Reset password call successful for: " + username);
		final ModelAndView mv = new ModelAndView("resetPasswordSent");
		mv.addObject("instance", getInstance());		
		return mv;
	}
	
	private Instance getInstance() {
		return api.getInstance(instanceConfig.getInstance());
	}
	
}
