package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.Instance;

@Controller
public class LoginController {
		
	private final InstanceSpecificApiClient api;
	
	@Autowired
	public LoginController(InstanceSpecificApiClient api) {
		this.api = api;
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)	// TODO SEO this onto the root url
	public ModelAndView member(@RequestParam(value = "errors", required = false) Boolean errors) throws Exception {
		final ModelAndView mv = new ModelAndView("login");
		final Instance instance = getInstance();
		mv.addObject("title", instance.getName());
		mv.addObject("instance", instance);
		if (errors != null && errors) {
			mv.addObject("errors", true);
		}
		return mv;
	}
	
	private Instance getInstance() {
		return api.getInstance();
	}
	
}
