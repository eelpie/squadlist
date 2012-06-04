package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;

@Controller
public class SquadsController {
	
	private SquadlistApi api;
	
	@Autowired
	public SquadsController(SquadlistApi api) {
		this.api = api;
	}
	
	@RequestMapping("/squad/{id}/contacts")
    public ModelAndView contacts(@PathVariable int id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadContacts");
		mv.addObject("squad", api.getSquad(id));
    	mv.addObject("members", api.getSquadMembers(id));
    	return mv;
    }
	
	@RequestMapping("/squad/{id}/entrydetails")
    public ModelAndView entrydetails(@PathVariable int id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadEntryDetails");
		mv.addObject("squad", api.getSquad(id));
    	mv.addObject("members", api.getSquadMembers(id));
    	return mv;
    }
	
	@RequestMapping("/squad/{id}/outings")
    public ModelAndView outings(@PathVariable int id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadOutings");
		mv.addObject("squad", api.getSquad(id));
    	mv.addObject("outings", api.getSquadOutings(id));
    	return mv;
    }
	
}
