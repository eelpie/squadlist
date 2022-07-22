package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.annotations.RequiresSignedInMember;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class MyOutingsController {

    private final LoggedInUserService loggedInUserService;
    private final ViewFactory viewFactory;
    private final OutingAvailabilityCountsService outingAvailabilityCountsService;
    private final UrlBuilder urlBuilder;
    private final GoverningBodyFactory governingBodyFactory;
    private final PreferredSquadService preferredSquadService;
    private final PermissionsService permissionsService;

    @Autowired
    public MyOutingsController(LoggedInUserService loggedInUserService,
                               ViewFactory viewFactory,
                               OutingAvailabilityCountsService outingAvailabilityCountsService,
                               UrlBuilder urlBuilder,
                               GoverningBodyFactory governingBodyFactory,
                               PreferredSquadService preferredSquadService,
                               PermissionsService permissionsService) {
        this.loggedInUserService = loggedInUserService;
        this.viewFactory = viewFactory;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.urlBuilder = urlBuilder;
        this.governingBodyFactory = governingBodyFactory;
        this.preferredSquadService = preferredSquadService;
        this.permissionsService = permissionsService;
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
        List<NavItem> navItems = navItemsFor(loggedInUser, loggedInUserApi, preferredSquad);

        if (permissionsService.hasPermission(loggedInUser, Permission.VIEW_ENTRY_DETAILS)) {
            navItems.add(new NavItem("entry.details", urlBuilder.entryDetailsUrl(preferredSquad), null, null, false));
        }
        if (permissionsService.hasPermission(loggedInUser, Permission.VIEW_ADMIN_SCREEN)) {
            navItems.add(new NavItem("admin", urlBuilder.adminUrl(), null, null, false));
        }

        return viewFactory.getViewForLoggedInUser("myOutings").
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
        final ModelAndView mv = viewFactory.getViewForLoggedInUser("myOutingsAjax");
        int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUserService.getLoggedInMember().getId(), loggedInUserApi);
        if (pendingOutingsCountFor > 0) {
            mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
        }
        return mv;
    }

    private List<NavItem> navItemsFor(Member loggedInUser, InstanceSpecificApiClient loggedInUserApi, Squad preferredSquad) throws URISyntaxException, UnknownInstanceException {
        final int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUser.getId(), loggedInUserApi);
        final int memberDetailsProblems = governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance()).checkRegistrationNumber(loggedInUser.getRegistrationNumber()) != null ? 1 : 0;

        List<NavItem> navItems = new ArrayList<>();
        navItems.add(new NavItem("my.outings", urlBuilder.applicationUrl("/"), pendingOutingsCountFor, "pendingOutings", true));
        navItems.add(new NavItem("my.details", urlBuilder.applicationUrl("/member/" + loggedInUser.getId() + "/edit"), memberDetailsProblems, "memberDetailsProblems", false));
        navItems.add(new NavItem("outings", urlBuilder.outingsUrl(preferredSquad), null, null, false));
        navItems.add(new NavItem("availability", urlBuilder.availabilityUrl(preferredSquad), null, null, false));
        navItems.add(new NavItem("contacts", urlBuilder.contactsUrl(preferredSquad), null, null, false));
        return navItems;
    }

}
