package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.services.LoggedInUserService;

@Controller
public class OutingsController {
	
	private LoggedInUserService loggedInUserService;
	private SquadlistApi api;
	
	@Autowired
	public OutingsController(LoggedInUserService loggedInUserService, SquadlistApi api) {
		this.loggedInUserService = loggedInUserService;
		this.api = api;
	}
	
	@RequestMapping("/outings/{id}")
    public ModelAndView outings(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("outing");
    	mv.addObject("outing", api.getOuting(id));
    	return mv;
    }
	
}
