package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;

@Controller
public class MembersController {
	
	private SquadlistApi api;
	private final LoggedInUserService loggedInUserService;
	
	@Autowired
	public MembersController(SquadlistApi api, LoggedInUserService loggedInUserService) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
	}
	
	@RequestMapping("/member/{id}")
    public ModelAndView members(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("memberDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("member", api.getMemberDetails(id));
    	return mv;
    }
	
}
