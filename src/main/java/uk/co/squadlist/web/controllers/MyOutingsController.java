package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.annotations.RequiresSignedInMember;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.NavItemsBuilder;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import java.util.Date;
import java.util.List;

@Controller
public class MyOutingsController {

    private final LoggedInUserService loggedInUserService;
    private final ViewFactory viewFactory;
    private final OutingAvailabilityCountsService outingAvailabilityCountsService;
    private final UrlBuilder urlBuilder;
    private final PreferredSquadService preferredSquadService;
    private final NavItemsBuilder navItemsBuilder;

    @Autowired
    public MyOutingsController(LoggedInUserService loggedInUserService,
                               ViewFactory viewFactory,
                               OutingAvailabilityCountsService outingAvailabilityCountsService,
                               UrlBuilder urlBuilder,
                               PreferredSquadService preferredSquadService,
                               NavItemsBuilder navItemsBuilder) {
        this.loggedInUserService = loggedInUserService;
        this.viewFactory = viewFactory;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.urlBuilder = urlBuilder;
        this.preferredSquadService = preferredSquadService;
        this.navItemsBuilder = navItemsBuilder;
    }

    @RequiresSignedInMember
    @RequestMapping("/")
    public ModelAndView outings() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        Instance instance = loggedInUserApi.getInstance();

        final Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
        final Date endDate = DateHelper.oneYearFromNow().toDate();
        List<OutingAvailability> availabilityFor = loggedInUserApi.getAvailabilityFor(loggedInUser.getId(), startDate, endDate);

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, loggedInUserApi, preferredSquad, "my.outings");

        return viewFactory.getViewForLoggedInUser("myOutings", loggedInUser).
                addObject("member", loggedInUserApi.getMember(loggedInUser.getId())).
                addObject("outings", availabilityFor).
                addObject("title", "My outings").
                addObject("navItems", navItems).
                addObject("availabilityOptions", loggedInUserApi.getAvailabilityOptions()).
                addObject("rssUrl", urlBuilder.outingsRss(loggedInUser.getId(), instance)).
                addObject("icalUrl", urlBuilder.outingsIcal(loggedInUser.getId(), instance));
    }

    @RequiresSignedInMember
    @RequestMapping("/myoutings/ajax")
    public ModelAndView ajax() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final ModelAndView mv = viewFactory.getViewForLoggedInUser("myOutingsAjax", loggedInMember);
        int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInMember.getId(), loggedInUserApi);
        if (pendingOutingsCountFor > 0) {
            mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
        }
        return mv;
    }

}
