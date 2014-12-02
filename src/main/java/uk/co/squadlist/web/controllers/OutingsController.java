package uk.co.squadlist.web.controllers;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.eelpieconsulting.common.dates.DateFormatter;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.annotations.RequiresOutingPermission;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.InvalidOutingException;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.AvailabilityOption;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.OutingDetails;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.base.Strings;

@Controller
public class OutingsController {

	private final static Logger log = Logger.getLogger(OutingsController.class);

	private LoggedInUserService loggedInUserService;
	private InstanceSpecificApiClient api;
	private UrlBuilder urlBuilder;
	private DateFormatter dateFormatter;
	private PreferedSquadService preferedSquadService;
	private ViewFactory viewFactory;
	private OutingAvailabilityCountsService outingAvailabilityCountsService;
	private ActiveMemberFilter activeMemberFilter;

	public OutingsController() {
	}

	@Autowired
	public OutingsController(LoggedInUserService loggedInUserService, InstanceSpecificApiClient api, UrlBuilder urlBuilder,
			DateFormatter dateFormatter, PreferedSquadService preferedSquadService, ViewFactory viewFactory,
			OutingAvailabilityCountsService outingAvailabilityCountsService, ActiveMemberFilter activeMemberFilter) {
		this.loggedInUserService = loggedInUserService;
		this.api = api;
		this.urlBuilder = urlBuilder;
		this.dateFormatter = dateFormatter;
		this.preferedSquadService = preferedSquadService;
		this.viewFactory = viewFactory;
		this.outingAvailabilityCountsService = outingAvailabilityCountsService;
		this.activeMemberFilter = activeMemberFilter;
	}

	@RequestMapping("/outings")
    public ModelAndView outings(@RequestParam(required=false, value="squad") String squadId,
    		@RequestParam(value = "month", required = false) String month) throws Exception {
    	final Squad squadToShow = preferedSquadService.resolveSquad(squadId);
    	final ModelAndView mv = viewFactory.getView("outings");
    	if (squadToShow == null) {
    		mv.addObject("title", "Outings");
    		return mv;
    	}

    	Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
		Date endDate = DateHelper.endOfCurrentOutingPeriod().toDate();

		String title = squadToShow.getName() + " outings";
		if (month != null) {
    		final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);	// TODO Can be moved to spring?
    		startDate = monthDateTime.toDate();
    		endDate = monthDateTime.plusMonths(1).toDate();
    		title =  squadToShow.getName() + " outings - " + dateFormatter.fullMonthYear(startDate);
    	} else {
    		mv.addObject("current", true);
    	}

		mv.addObject("title", title);
		mv.addObject("squad", squadToShow);
		mv.addObject("startDate", startDate);
		mv.addObject("endDate", endDate);
		mv.addObject("month", month);
    	mv.addObject("outingMonths", getOutingMonthsFor(squadToShow));

		final List<OutingWithSquadAvailability> squadOutings = api.getSquadAvailability(squadToShow.getId(), startDate, endDate);
		mv.addObject("outings", squadOutings);
    	mv.addObject("outingAvailabilityCounts", outingAvailabilityCountsService.buildOutingAvailabilityCounts(squadOutings));

    	mv.addObject("squads", api.getSquads());
    	return mv;
    }

	@RequestMapping("/outings/{id}")
    public ModelAndView outing(@PathVariable String id) throws Exception {
    	final Outing outing = api.getOuting(id);

    	final ModelAndView mv = viewFactory.getView("outing");
    	mv.addObject("title", outing.getSquad().getName() + " - " + dateFormatter.dayMonthYearTime(outing.getDate()));
		mv.addObject("outing", outing);
		mv.addObject("outingMonths", getOutingMonthsFor(outing.getSquad()));
		mv.addObject("squad", outing.getSquad());
    	mv.addObject("squadAvailability", api.getOutingAvailability(outing.getId()));
		mv.addObject("squads", api.getSquads());
		mv.addObject("members", activeMemberFilter.extractActive(api.getSquadMembers(outing.getSquad().getId())));
		mv.addObject("month", ISODateTimeFormat.yearMonth().print(outing.getDate().getTime()));	// TODO push to date parser - local time
    	return mv;
    }

	@RequiresPermission(permission=Permission.ADD_OUTING)
	@RequestMapping(value="/outings/new", method=RequestMethod.GET)
    public ModelAndView newOuting() throws Exception {
		final LocalDateTime defaultOutingDateTime = DateHelper.defaultOutingStartDateTime();
		final OutingDetails outingDefaults = new OutingDetails(defaultOutingDateTime);
		outingDefaults.setSquad(preferedSquadService.resolveSquad(null).getId());
    	return renderNewOutingForm(outingDefaults);
	}

	@RequiresPermission(permission=Permission.ADD_OUTING)
	@RequestMapping(value="/outings/new", method=RequestMethod.POST)
    public ModelAndView newOutingSubmit(@Valid @ModelAttribute("outing") OutingDetails outingDetails, BindingResult result) throws Exception {
		if (result.hasErrors()) {
			return renderNewOutingForm(outingDetails);
		}

		try {
			final Outing newOuting = buildOutingFromOutingDetails(outingDetails, api.getInstance());
			if (outingDetails.getRepeats() != null && outingDetails.getRepeats() && outingDetails.getRepeatsCount() != null) {
				api.createOuting(newOuting, outingDetails.getRepeatsCount());
			} else {
				api.createOuting(newOuting);
			}

			final String outingsViewForNewOutingsSquadAndMonth = urlBuilder.outings(newOuting.getSquad(), new DateTime(newOuting.getDate()).toString("yyyy-MM"));
			return new ModelAndView(new RedirectView(outingsViewForNewOutingsSquadAndMonth));

		} catch (InvalidOutingException e) {
			result.addError(new ObjectError("outing", e.getMessage()));
			return renderNewOutingForm(outingDetails);

		} catch (Exception e) {
			result.addError(new ObjectError("outing", "Unknown error"));
			return renderNewOutingForm(outingDetails);
		}
	}

	@RequiresOutingPermission(permission=Permission.EDIT_OUTING)
	@RequestMapping(value="/outings/{id}/edit", method=RequestMethod.GET)
    public ModelAndView outingEdit(@PathVariable String id) throws Exception {
    	final Outing outing = api.getOuting(id);

    	final OutingDetails outingDetails = new OutingDetails(new LocalDateTime(outing.getDate()));
    	outingDetails.setSquad(outing.getSquad().getId());
    	outingDetails.setNotes(outing.getNotes());

    	return renderEditOutingForm(outingDetails, outing);
    }

	@RequiresOutingPermission(permission=Permission.EDIT_OUTING)
	@RequestMapping(value="/outings/{id}/close", method=RequestMethod.GET)
    public ModelAndView closeOuting(@PathVariable String id) throws Exception {
    	final Outing outing = api.getOuting(id);

    	log.info("Closing outing: " + outing);
    	outing.setClosed(true);
    	api.updateOuting(outing);

    	return redirectToOuting(outing);
    }

	@RequiresOutingPermission(permission=Permission.EDIT_OUTING)
	@RequestMapping(value="/outings/{id}/reopen", method=RequestMethod.GET)
    public ModelAndView reopenOuting(@PathVariable String id) throws Exception {
    	final Outing outing = api.getOuting(id);

    	log.info("Reopening outing: " + outing);
    	outing.setClosed(false);
    	api.updateOuting(outing);

    	return redirectToOuting(outing);
    }

	@RequiresOutingPermission(permission=Permission.EDIT_OUTING)
	@RequestMapping(value="/outings/{id}/edit", method=RequestMethod.POST)
    public ModelAndView editOutingSubmit(@PathVariable String id,
    		@Valid @ModelAttribute("outing") OutingDetails outingDetails, BindingResult result) throws Exception {
		final Outing outing = api.getOuting(id);
		if (result.hasErrors()) {
			return renderEditOutingForm(outingDetails, outing);
		}
		try {
			final Outing updatedOuting = buildOutingFromOutingDetails(outingDetails, api.getInstance());
			updatedOuting.setId(id);

			api.updateOuting(updatedOuting);
			return redirectToOuting(updatedOuting);

		} catch (InvalidOutingException e) {
			result.addError(new ObjectError("outing", e.getMessage()));
			return renderEditOutingForm(outingDetails, outing);

		} catch (Exception e) {
			log.error(e);
			result.addError(new ObjectError("outing", "Unknown exception"));
			return renderEditOutingForm(outingDetails, outing);
		}
	}

	private ModelAndView redirectToOuting(final Outing updatedOuting) {
		return new ModelAndView(new RedirectView(urlBuilder.outingUrl(updatedOuting)));
	}

	private ModelAndView renderNewOutingForm(OutingDetails outingDetails) throws UnknownMemberException, UnknownSquadException {
		final ModelAndView mv = viewFactory.getView("newOuting");
		mv.addObject("squads", api.getSquads());
		final Squad squad = preferedSquadService.resolveSquad(null);
		mv.addObject("squad", squad);
		mv.addObject("outingMonths", getOutingMonthsFor(squad));
		mv.addObject("outing", outingDetails);
		return mv;
	}

	private ModelAndView renderEditOutingForm(OutingDetails outingDetails, Outing outing) throws UnknownMemberException, UnknownSquadException {
    	final ModelAndView mv = viewFactory.getView("editOuting");
		mv.addObject("squads", api.getSquads());
		mv.addObject("squad", outing.getSquad());
		mv.addObject("outing", outingDetails);
    	mv.addObject("outingObject", outing);
    	mv.addObject("outingMonths", getOutingMonthsFor(outing.getSquad()));
		mv.addObject("month", ISODateTimeFormat.yearMonth().print(outing.getDate().getTime()));	// TODO push to date parser - local time
		return mv;
	}

	@RequestMapping(value="/availability/ajax", method=RequestMethod.POST)
    public ModelAndView updateAvailability(
    		@RequestParam(value="outing", required=true) String outingId,
    		@RequestParam(value="availability", required=true) String availability) throws Exception {
    	final Outing outing = api.getOuting(outingId);

    	if (!outing.isClosed()) {
    		final OutingAvailability result = api.setOutingAvailability(api.getMemberDetails(loggedInUserService.getLoggedInUser()),
    				outing, getAvailabilityOptionById(availability));
    		return viewFactory.getView("includes/availability").addObject("availability", result.getAvailabilityOption());
    	}

    	throw new RuntimeException("Outing is closed");	// TODO
	}

	private AvailabilityOption getAvailabilityOptionById(String availability) throws JsonParseException, JsonMappingException, HttpFetchException, IOException {
		if (Strings.isNullOrEmpty(availability)) {
			return null;
		}

		List<AvailabilityOption> availabilityOptions = api.getAvailabilityOptions();
		for (AvailabilityOption availabilityOption : availabilityOptions) {
			if (availabilityOption.getId().equals(availability)) {
				log.info(availability + ": " + availabilityOption);
				return availabilityOption;
			}
		}
		throw new RuntimeException("Unknown availability option: " + availability);	// TODO
	}

	private Map<String, Integer> getOutingMonthsFor(final Squad squad) {
		return api.getSquadOutingMonths(squad.getId());
	}

	private Outing buildOutingFromOutingDetails(OutingDetails outingDetails, Instance instance) throws UnknownSquadException {
		final Outing newOuting = new Outing();
		newOuting.setDate(outingDetails.toLocalTime().toDateTime(DateTimeZone.forID(instance.getTimeZone())).toDate());
		newOuting.setSquad(outingDetails.getSquad() != null ? api.getSquad(outingDetails.getSquad()) : null);	// TODO validation step
		newOuting.setNotes(outingDetails.getNotes());	// TODO flatten these lines into a constructor
		return newOuting;
	}

}
