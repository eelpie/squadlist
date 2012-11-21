package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.urls.UrlBuilder;

@Controller
public class LoginController {
	
	@RequestMapping(value="/login", method=RequestMethod.GET)
    public ModelAndView member() throws Exception {
    	final ModelAndView mv = new ModelAndView("login");		
    	return mv;
    }
	
}
