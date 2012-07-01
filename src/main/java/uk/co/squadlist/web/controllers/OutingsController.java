package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;

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
    public ModelAndView outings(@PathVariable int id) throws Exception {
    	ModelAndView mv = new ModelAndView("outing");
    	final Outing outing = api.getOuting(id);
    	    	
		mv.addObject("outing", outing);
		mv.addObject("squad", api.getSquad(outing.getSquad()));
    	mv.addObject("members", api.getSquadMembers(outing.getSquad()));
    	mv.addObject("availability", api.getOutingAvailability(outing.getId()));
    	return mv;
    }
	
	@RequestMapping(value="/availability/ajax", method=RequestMethod.POST)
    public ModelAndView updateAvailability(
    		@RequestParam(value="outing", required=true) int outingId,
    		@RequestParam(value="availability", required=true) String availability) throws Exception {
    	final Outing outing = api.getOuting(outingId);
    	
    	OutingAvailability result = api.setOutingAvailability(loggedInUserService.getLoggedInUser(), outing.getId(), availability);    	
    	ModelAndView mv = new ModelAndView("outing");
		mv.addObject("data", result);
    	return mv;
    }
	
    @ExceptionHandler(UnknownOutingException.class)
    @ResponseStatus(value = org.springframework.http.HttpStatus.NOT_FOUND, reason = "No outing was found with the requested id")
    public void unknownUser(UnknownOutingException e) {
    }
    
}
