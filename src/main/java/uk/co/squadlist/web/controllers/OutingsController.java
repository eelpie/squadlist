package uk.co.squadlist.web.controllers;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.eelpieconsulting.common.dates.DateFormatter;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.OutingDetails;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.JsonSerializer;
import uk.co.squadlist.web.views.JsonView;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

@Controller
public class OutingsController {
	
	private final static Logger log = Logger.getLogger(OutingsController.class);
	
	private final LoggedInUserService loggedInUserService;
	private final SquadlistApi api;
	private final UrlBuilder urlBuilder;
	private final InstanceConfig instanceConfig;
	private final DateFormatter dateFormatter;
	private final PreferedSquadService preferedSquadService;
	
	@Autowired
	public OutingsController(LoggedInUserService loggedInUserService, SquadlistApi api, UrlBuilder urlBuilder, 
			InstanceConfig instanceConfig, DateFormatter dateFormatter, PreferedSquadService preferedSquadService) {
		this.loggedInUserService = loggedInUserService;
		this.api = api;
		this.urlBuilder = urlBuilder;
		this.instanceConfig = instanceConfig;
		this.dateFormatter = dateFormatter;
		this.preferedSquadService = preferedSquadService;
	}
	
	@RequestMapping("/outings")
    public ModelAndView outings(@RequestParam(required=false, value="squad") String squadId,
    		@RequestParam(value = "month", required = false) String month) throws Exception {
    	final ModelAndView mv = new ModelAndView("outings");
    	mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());	// TODO shouldn't need todo this explictly on each controller - move to velocity context
    	
    	final Squad squadToShow = resolveSquad(squadId);	// TODO null safe	

    	Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
		Date endDate = DateHelper.endOfCurrentOutingPeriod().toDate();
		
		String title = squadToShow.getName() + " outings";
		if (month != null) {
    		final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);	// TODO Can be moved to spring?
    		startDate = monthDateTime.toDate();
    		endDate = monthDateTime.plusMonths(1).toDate();
    		title =  squadToShow.getName() + " outings - " + dateFormatter.fullMonthYear(startDate);
    	}

		mv.addObject("title", title);
		mv.addObject("squad", squadToShow);
		mv.addObject("startDate", startDate);
		mv.addObject("endDate", endDate);
    	    	
    	mv.addObject("outings", api.getSquadOutings(instanceConfig.getInstance(), squadToShow.getId(), startDate, endDate));
    	mv.addObject("outingMonths", getOutingMonthsFor(squadToShow));
		mv.addObject("squads", api.getSquads(instanceConfig.getInstance()));
    	return mv;
    }
	
	@RequestMapping("/outings/{id}")
    public ModelAndView outing(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("outing");
    	mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());	// TODO shouldn't need todo this explictly on each controller - move to velocity context
    	
    	final Outing outing = api.getOuting(instanceConfig.getInstance(), id);
    	
    	mv.addObject("title", outing.getSquad().getName() + " - " + dateFormatter.dayMonthYearTime(outing.getDate()));
		mv.addObject("outing", outing);
		mv.addObject("outingMonths", getOutingMonthsFor(outing.getSquad()));
		mv.addObject("squad", outing.getSquad());
    	mv.addObject("availability", api.getOutingAvailability(instanceConfig.getInstance(), outing.getId()));
		mv.addObject("squads", api.getSquads(instanceConfig.getInstance()));
		mv.addObject("members", api.getSquadMembers(instanceConfig.getInstance(), outing.getSquad().getId()));
    	return mv;
    }
	
	@RequestMapping(value="/outings/new", method=RequestMethod.GET)
    public ModelAndView newOuting() throws Exception {
		final LocalDateTime defaultOutingDateTime = DateHelper.defaultOutingStartDateTime();
		final OutingDetails defaultOutingDetails = new OutingDetails(defaultOutingDateTime);  
    	return renderNewOutingForm(defaultOutingDetails);
	}
	
	@RequestMapping(value="/outings/new", method=RequestMethod.POST)
    public ModelAndView newOutingSubmit(@Valid @ModelAttribute("outing") OutingDetails outingDetails, BindingResult result) throws Exception {
		if (result.hasErrors()) {
			return renderNewOutingForm(outingDetails);
		}		
		
		try {
			final Outing newOuting = buildOutingFromOutingDetails(outingDetails, api.getInstance(instanceConfig.getInstance()));			
			final Outing outing = api.createOuting(instanceConfig.getInstance(), newOuting);
			return new ModelAndView(new RedirectView(urlBuilder.outingUrl(outing)));
			
		} catch (Exception e) {
			result.addError(new ObjectError("outing", "Invalid outing"));
			return renderNewOutingForm(outingDetails);
		}
	}
	
	@RequestMapping(value="/outings/{id}/edit", method=RequestMethod.GET)
    public ModelAndView outingEdit(@PathVariable String id) throws Exception {    	
    	final Outing outing = api.getOuting(instanceConfig.getInstance(), id);

    	final OutingDetails outingDetails = new OutingDetails(new LocalDateTime(outing.getDate()));
    	outingDetails.setSquad(outing.getSquad().getId());
    	outingDetails.setNotes(outing.getNotes());
    	
    	return renderEditOutingForm(outingDetails, outing);
    }
	
	@RequestMapping(value="/outings/{id}/edit", method=RequestMethod.POST)
    public ModelAndView editOutingSubmit(@PathVariable String id,
    		@Valid @ModelAttribute("outing") OutingDetails outingDetails, BindingResult result) throws Exception {
		final Outing outing = api.getOuting(instanceConfig.getInstance(), id);
		if (result.hasErrors()) {
			return renderEditOutingForm(outingDetails, outing);
		}
		try {
			final Outing updatedOuting = buildOutingFromOutingDetails(outingDetails, api.getInstance(instanceConfig.getInstance()));
			updatedOuting.setId(id);
			
			api.updateOuting(instanceConfig.getInstance(), updatedOuting);
			return new ModelAndView(new RedirectView(urlBuilder.outingUrl(updatedOuting)));
			
		} catch (Exception e) {
			log.error(e);
			result.addError(new ObjectError("outing", "Unknown exception"));
			return renderEditOutingForm(outingDetails, outing);
		}
	}
	
	private ModelAndView renderNewOutingForm(OutingDetails outingDetails) throws UnknownMemberException {
		ModelAndView mv = new ModelAndView("newOuting");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		mv.addObject("squads", api.getSquads(instanceConfig.getInstance()));
		mv.addObject("squad", preferedSquadService.resolvedPreferedSquad(loggedInUserService.getLoggedInUser()));
		mv.addObject("outing", outingDetails);
		return mv;
	}
	
	private ModelAndView renderEditOutingForm(OutingDetails outingDetails, Outing outing) throws UnknownMemberException {
		final ModelAndView mv = new ModelAndView("editOuting");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		mv.addObject("squads", api.getSquads(instanceConfig.getInstance()));
		mv.addObject("squad", preferedSquadService.resolvedPreferedSquad(loggedInUserService.getLoggedInUser()));
		mv.addObject("outing", outingDetails);
    	mv.addObject("outingObject", outing);
		return mv;
	}
	
	@RequestMapping(value="/availability/ajax", method=RequestMethod.POST)
    public ModelAndView updateAvailability(
    		@RequestParam(value="outing", required=true) String outingId,
    		@RequestParam(value="availability", required=true) String availability) throws Exception {
    	final Outing outing = api.getOuting(instanceConfig.getInstance(), outingId);
    	
    	OutingAvailability result = api.setOutingAvailability(instanceConfig.getInstance(), loggedInUserService.getLoggedInUser(), outing.getId(), availability);
    	
    	final ModelAndView mv = new ModelAndView(new JsonView(new JsonSerializer()));
		mv.addObject("data", result);
    	return mv;
    }
	
	private Squad resolveSquad(String squadId) throws UnknownSquadException, UnknownMemberException {
    	if(!Strings.isNullOrEmpty(squadId)) {
    		final Squad selectedSquad = api.getSquad(instanceConfig.getInstance(), squadId);
    		preferedSquadService.setPreferedSquad(selectedSquad);
			return selectedSquad;
    	}    	
    	return preferedSquadService.resolvedPreferedSquad(loggedInUserService.getLoggedInUser());
	}
	
	private Map<String, Integer> getOutingMonthsFor(final Squad squad) {
		final Map<String, Integer> squadOutingMonths = api.getSquadOutingMonths(instanceConfig.getInstance(), squad.getId());
		
		final Map<String, Integer> currentAndFutureOutingMonths = Maps.newTreeMap();		
		final Iterator<String> iterator = squadOutingMonths.keySet().iterator();
		final DateTime firstMonthToShow = DateHelper.startOfCurrentOutingPeriod().minusMonths(1);
		while(iterator.hasNext()) {
			final String month = iterator.next();
			if (ISODateTimeFormat.yearMonth().parseDateTime(month).isAfter(firstMonthToShow)) {
				currentAndFutureOutingMonths.put(month, squadOutingMonths.get(month));
			}
		}
		
		return currentAndFutureOutingMonths;
	}
	
    @ExceptionHandler(UnknownOutingException.class)
    @ResponseStatus(value = org.springframework.http.HttpStatus.NOT_FOUND, reason = "No outing was found with the requested id")
    public void unknownUser(UnknownOutingException e) {
    }
    
	private Outing buildOutingFromOutingDetails(OutingDetails outingDetails, Instance instance) throws UnknownSquadException {
		final Outing newOuting = new Outing();
		newOuting.setDate(outingDetails.toLocalTime().toDateTime(DateTimeZone.forID(instance.getTimeZone())).toDate());
		newOuting.setSquad(outingDetails.getSquad() != null ? api.getSquad(instanceConfig.getInstance(), outingDetails.getSquad()) : null);	// TODO validation step
		newOuting.setNotes(outingDetails.getNotes());	// TODO flatten these lines into a constructor
		return newOuting;
	}
    
}
