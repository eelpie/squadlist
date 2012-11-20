package uk.co.squadlist.web.controllers;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.forms.OutingDetails;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.JsonSerializer;
import uk.co.squadlist.web.views.JsonView;

@Controller
public class OutingsController {
	
	private LoggedInUserService loggedInUserService;
	private SquadlistApi api;
	private UrlBuilder urlBuilder;
	
	@Autowired
	public OutingsController(LoggedInUserService loggedInUserService, SquadlistApi api, UrlBuilder urlBuilder) {
		this.loggedInUserService = loggedInUserService;
		this.api = api;
		this.urlBuilder = urlBuilder;
	}
	
	@RequestMapping("/outings/{id}")
    public ModelAndView outings(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("outing");
    	final Outing outing = api.getOuting(SquadlistApi.INSTANCE, id);
    	    	
		mv.addObject("outing", outing);
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());	// TODO shouldn't need todo this explictly on each controller - move to velocity context
		mv.addObject("squad", outing.getSquad());
    	mv.addObject("members", api.getSquadMembers(SquadlistApi.INSTANCE, outing.getSquad().getId()));
    	mv.addObject("availability", api.getOutingAvailability(SquadlistApi.INSTANCE, outing.getId()));
    	return mv;
    }
	
	@RequestMapping(value="/outings/new", method=RequestMethod.GET)
    public ModelAndView newOuting() throws Exception {
    	ModelAndView mv = new ModelAndView("newOuting");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(SquadlistApi.INSTANCE));
    	
    	final LocalDateTime defaultOutingDateTime = new DateTime(DateTime.now().toDateMidnight()).plusDays(1).plusHours(8).toLocalDateTime();
    	final OutingDetails defaultOutingDetails = new OutingDetails(defaultOutingDateTime);    	
		mv.addObject("outing", defaultOutingDetails);
    	return mv;
	}
	
	@RequestMapping(value="/outings/new", method=RequestMethod.POST)
    public ModelAndView newOutingSubmit(@ModelAttribute("outing") OutingDetails outingDetails) throws Exception {		
    	final Outing outing = api.createOuting(SquadlistApi.INSTANCE, outingDetails.getSquad(), outingDetails.toLocalTime());
		ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.outingUrl(outing)));
    	return mv;
	}
	
	@RequestMapping(value="/availability/ajax", method=RequestMethod.POST)
    public ModelAndView updateAvailability(
    		@RequestParam(value="outing", required=true) String outingId,
    		@RequestParam(value="availability", required=true) String availability) throws Exception {
    	final Outing outing = api.getOuting(SquadlistApi.INSTANCE, outingId);
    	
    	OutingAvailability result = api.setOutingAvailability(SquadlistApi.INSTANCE, loggedInUserService.getLoggedInUser(), outing.getId(), availability);    	
    	ModelAndView mv = new ModelAndView(new JsonView(new JsonSerializer()));
		mv.addObject("data", result);
    	return mv;
    }
	
    @ExceptionHandler(UnknownOutingException.class)
    @ResponseStatus(value = org.springframework.http.HttpStatus.NOT_FOUND, reason = "No outing was found with the requested id")
    public void unknownUser(UnknownOutingException e) {
    }
    
}
