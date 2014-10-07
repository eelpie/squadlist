package uk.co.squadlist.web.controllers;

import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Name;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.util.UidGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.EtagGenerator;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.RssOuting;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Controller
public class MyOutingsController {
		
	private static final Dur ONE_HOUR = new Dur(0, 1, 0, 0);
	private final LoggedInUserService loggedInUserService;
	private final InstanceSpecificApiClient api;
	private final ViewFactory viewFactory;
	private final OutingAvailabilityCountsService outingAvailabilityCountsService;
	private final UrlBuilder urlBuilder;
	
	@Autowired
	public MyOutingsController(LoggedInUserService loggedInUserService, InstanceSpecificApiClient api, ViewFactory viewFactory,
			OutingAvailabilityCountsService outingAvailabilityCountsService, UrlBuilder urlBuilder) {
		this.loggedInUserService = loggedInUserService;
		this.api = api;
		this.viewFactory = viewFactory;
		this.outingAvailabilityCountsService = outingAvailabilityCountsService;
		this.urlBuilder = urlBuilder;
	}
	
	@RequestMapping("/")
    public ModelAndView outings() throws Exception {
    	final ModelAndView mv = viewFactory.getView("myOutings");
    	final String loggedInUser = loggedInUserService.getLoggedInUser();
		mv.addObject("member", api.getMemberDetails(loggedInUser));
		
		final Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
		final Date endDate = DateHelper.oneYearFromNow().toDate();
		
		mv.addObject("outings", api.getAvailabilityFor(loggedInUser, startDate, endDate));
		
		mv.addObject("title", "My outings");		
    	mv.addObject("availabilityOptions", api.getAvailabilityOptions());
    	return mv;
    }
	
	
	@RequestMapping("/ical")
    public void outingsIcal(@RequestParam(value="user", required=false) String user, HttpServletResponse response) throws Exception {
		if (Strings.isNullOrEmpty(user)) {
			return;	// TODO 404
		}
		
		api.getMemberDetails(user);	// TODO could be removed if getavailability for 404ed nicely
		
    	final String loggedInUser = user;	// TODO access control
		
		final Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
		final Date endDate = DateHelper.oneYearFromNow().toDate();		
		List<OutingAvailability> availabilityFor = api.getAvailabilityFor(loggedInUser, startDate, endDate);
		
		final Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//Squadlist//iCal4j 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);

		final String name = api.getInstance().getName() + " outings";
		calendar.getProperties().add(new Name(name));
		calendar.getProperties().add(new XProperty("X-WR-CALNAME", name));	// TODO check source code for enum
		calendar.getProperties().add(new XProperty("X-PUBLISHED-TTL", "PT1H"));
		calendar.getProperties().add(new XProperty("REFRESH-INTERVAL;VALUE=DURATION", "P1H"));
		
		for (OutingAvailability outingAvailability : availabilityFor) {
			final Outing outing = outingAvailability.getOuting();
			calendar.getComponents().add(buildEventFor(outing));
		}
				
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/calendar");
		
		final PrintWriter writer = response.getWriter();
		writer.println(calendar.toString());
		writer.flush();
    }

	@RequestMapping("/rss")
    public ModelAndView outingsRss(@RequestParam(value="user", required=false) String user) throws Exception {
		if (Strings.isNullOrEmpty(user)) {
			return null;	// TODO 404
		}
		
		api.getMemberDetails(user);	// TODO could be removed if getavailability for 404ed nicely
		
    	final String loggedInUser = user;	// TODO access control
		
		final Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
		final Date endDate = DateHelper.oneYearFromNow().toDate();		
		List<OutingAvailability> availabilityFor = api.getAvailabilityFor(loggedInUser, startDate, endDate);
				
		List<RssOuting> outings = Lists.newArrayList();
		for (OutingAvailability outingAvailability : availabilityFor) {
			outings.add(new RssOuting(outingAvailability.getOuting(), urlBuilder.outingUrl(outingAvailability.getOuting())));					
		}

		final String title = api.getInstance().getName() + " outings";
		final ModelAndView mv = new ModelAndView(new uk.co.eelpieconsulting.common.views.ViewFactory(new EtagGenerator()).getRssView(title, urlBuilder.getBaseUrl(), title));
		mv.addObject("data", outings);
		return mv;
    }
	
	@RequestMapping("/myoutings/ajax")
    public ModelAndView ajax() throws Exception {
    	final ModelAndView mv = viewFactory.getView("myOutingsAjax");
    	int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUserService.getLoggedInUser());
    	if (pendingOutingsCountFor > 0) {
    		mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
    	}
    	return mv;
    }

	private VEvent buildEventFor(final Outing outing) throws SocketException {
		final VEvent outingEvent = new VEvent(new net.fortuna.ical4j.model.DateTime(outing.getDate()), ONE_HOUR, outing.getSquad().getName());
		
		final TzId tzParam = new TzId(api.getInstance().getTimeZone());
		outingEvent.getProperties().getProperty(Property.DTSTART).getParameters().add(tzParam);
		
		if (!Strings.isNullOrEmpty(outing.getNotes())) {
			outingEvent.getProperties().add(new Description(outing.getNotes()));		
		}
		
		final UidGenerator ug = new UidGenerator(outing.getId());	// TODO how does this work - can we use the outing id?
		outingEvent.getProperties().add(ug.generateUid());
		return outingEvent;
	}
	
}
