package uk.co.squadlist.web.controllers;

import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.model.Instance;

@Controller
public class LoginController {
	
	private SquadlistApi api;
	
	@Autowired
	public LoginController(SquadlistApi api) {
		this.api = api;
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
		final ModelAndView mv = new ModelAndView("resetPassword");
		mv.addObject("instance", getInstance());
		if (Strings.isNullOrEmpty(username)) {
			mv.addObject("errors", true);
		
		} else {
			api.resetPassword(InstanceConfig.INSTANCE, username);
		}
		
		
		return mv;
	}
	
	private Instance getInstance() {
		return api.getInstance(InstanceConfig.INSTANCE);
	}
	
}
