package uk.co.squadlist.web.controllers;

import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.model.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.EtagGenerator;
import uk.co.squadlist.web.annotations.RequiresSignedInMember;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.ical.OutingCalendarService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.RssOuting;
import uk.co.squadlist.web.views.SquadNamesHelper;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Controller
public class MyOutingsController {
	
	private final LoggedInUserService loggedInUserService;
	private final InstanceSpecificApiClient instanceSpecificApiClient;
	private final ViewFactory viewFactory;
	private final OutingAvailabilityCountsService outingAvailabilityCountsService;
	private final UrlBuilder urlBuilder;
	private final SquadNamesHelper squadNamesHelper;
	private final OutingCalendarService outingCalendarService;
	private final SquadlistApiFactory squadlistApiFactory;
	private final SquadlistApi squadlistApi;

	@Autowired
	public MyOutingsController(LoggedInUserService loggedInUserService, InstanceSpecificApiClient instanceSpecificApiClient, ViewFactory viewFactory,
														 OutingAvailabilityCountsService outingAvailabilityCountsService, UrlBuilder urlBuilder,
														 SquadNamesHelper squadNamesHelper,
														 OutingCalendarService outingCalendarService, SquadlistApiFactory squadlistApiFactory) {
		this.loggedInUserService = loggedInUserService;
		this.instanceSpecificApiClient = instanceSpecificApiClient;
		this.viewFactory = viewFactory;
		this.outingAvailabilityCountsService = outingAvailabilityCountsService;
		this.urlBuilder = urlBuilder;
		this.squadNamesHelper = squadNamesHelper;
		this.outingCalendarService = outingCalendarService;
		this.squadlistApiFactory = squadlistApiFactory;
		this.squadlistApi = squadlistApiFactory.createClient();
	}

	@RequiresSignedInMember
	@RequestMapping("/")
	public ModelAndView outings() throws Exception {
		final String loggedInUser = loggedInUserService.getLoggedInMember().getId();
    SquadlistApi loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.getLoggedInMembersToken());

    final ModelAndView mv = viewFactory.getViewForLoggedInUser("myOutings");
    mv.addObject("member", loggedInUserApi.getMember(loggedInUser));

		final Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
		final Date endDate = DateHelper.oneYearFromNow().toDate();

		mv.addObject("outings", loggedInUserApi.getAvailabilityFor(loggedInUser, startDate, endDate));

		mv.addObject("title", "My outings");
		mv.addObject("availabilityOptions", instanceSpecificApiClient.getAvailabilityOptions());
		mv.addObject("rssUrl", urlBuilder.outingsRss(loggedInUser, instanceSpecificApiClient.getInstance()));
		mv.addObject("icalUrl", urlBuilder.outingsIcal(loggedInUser, instanceSpecificApiClient.getInstance()));
		return mv;
	}

	@RequestMapping("/ical")
    public void outingsIcal(@RequestParam(value="user", required=false) String user, HttpServletResponse response) throws Exception {
		if (Strings.isNullOrEmpty(user)) {
			throw new UnknownMemberException();
		}

		final Member member = squadlistApi.getMember(user);

		final Calendar calendar = outingCalendarService.buildCalendarFor(squadlistApi.getAvailabilityFor(member.getId(),
				DateHelper.startOfCurrentOutingPeriod().toDate(),
				DateHelper.oneYearFromNow().toDate()),
				instanceSpecificApiClient.getInstance());

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/calendar");

		final PrintWriter writer = response.getWriter();
		writer.println(calendar.toString());
		writer.flush();
    }

	@RequestMapping("/rss")
    public ModelAndView outingsRss(@RequestParam(value="user", required=false) String user) throws Exception {
		if (Strings.isNullOrEmpty(user)) {
			throw new UnknownMemberException();
		}
		
		final Member member = squadlistApi.getMember(user);
				
		final List<OutingAvailability> availabilityFor = squadlistApi.getAvailabilityFor(member.getId(),
				DateHelper.startOfCurrentOutingPeriod().toDate(),
				DateHelper.oneYearFromNow().toDate());
				
		final String title = instanceSpecificApiClient.getInstance().getName() + " outings";
		final ModelAndView mv = new ModelAndView(new uk.co.eelpieconsulting.common.views.ViewFactory(new EtagGenerator()).getRssView(title, urlBuilder.getBaseUrl(), 
				squadNamesHelper.list(member.getSquads()) + " outings"));
		mv.addObject("data", buildRssItemsFor(availabilityFor));
		return mv;
    }

	@RequiresSignedInMember
	@RequestMapping("/myoutings/ajax")
    public ModelAndView ajax() throws Exception {
    	final ModelAndView mv = viewFactory.getViewForLoggedInUser("myOutingsAjax");
			SquadlistApi loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.getLoggedInMembersToken());
			int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUserService.getLoggedInMember().getId(), loggedInUserApi);
    	if (pendingOutingsCountFor > 0) {
    		mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
    	}
    	return mv;
    }

	private List<RssOuting> buildRssItemsFor(final List<OutingAvailability> availabilityFor) {
		List<RssOuting> outings = Lists.newArrayList();
		for (OutingAvailability outingAvailability : availabilityFor) {
			outings.add(new RssOuting(outingAvailability.getOuting(), urlBuilder.outingUrl(outingAvailability.getOuting())));					
		}
		return outings;
	}
	
}
