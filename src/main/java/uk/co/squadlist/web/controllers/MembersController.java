package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;

@Controller
public class MembersController {
	
	private SquadlistApi api;
	
	@Autowired
	public MembersController(SquadlistApi api) {
		this.api = api;
	}
	
	@RequestMapping("/member/{id}")
    public ModelAndView members(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("memberDetails");
    	mv.addObject("member", api.getMemberDetails(id));
    	return mv;
    }
	
}
