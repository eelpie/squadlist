package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.localisation.BritishRowing;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class GoverningBodyController {

    private final GoverningBodyFactory governingBodyFactory;
    private final ViewFactory viewFactory;
    private final LoggedInUserService loggedInUserService;
    private final OutingAvailabilityCountsService outingAvailabilityCountsService;
    private final PreferredSquadService preferredSquadService;
    private final UrlBuilder urlBuilder;
    private final PermissionsService permissionsService;

    @Autowired
    public GoverningBodyController(GoverningBodyFactory governingBodyFactory,
                                   ViewFactory viewFactory,
                                   LoggedInUserService loggedInUserService,
                                   OutingAvailabilityCountsService outingAvailabilityCountsService,
                                   PreferredSquadService preferredSquadService,
                                   UrlBuilder urlBuilder,
                                   PermissionsService permissionsService) {
        this.governingBodyFactory = governingBodyFactory;
        this.viewFactory = viewFactory;
        this.loggedInUserService = loggedInUserService;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.preferredSquadService = preferredSquadService;
        this.urlBuilder = urlBuilder;
        this.permissionsService = permissionsService;
    }

    @RequestMapping(value = "/governing-body/british-rowing", method = RequestMethod.GET)
    public ModelAndView member() throws Exception {
        Member loggedInUser = loggedInUserService.getLoggedInMember();
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final GoverningBody governingBody = governingBodyFactory.governingBodyFor("british-rowing");  // TODO take from path

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInUser, loggedInUserApi, preferredSquad);

        return viewFactory.getViewForLoggedInUser("governingBody").
                addObject("governingBody", governingBody).
                addObject("title", governingBody.getName()).
                addObject("navItems",navItems).
                addObject("ageGrades", governingBody.getAgeGrades()).
                addObject("statuses", governingBody.getStatusPoints()).
                addObject("boatSizes", governingBody.getBoatSizes());
    }

    private List<NavItem> navItemsFor(Member loggedInUser, InstanceSpecificApiClient loggedInUserApi, Squad preferredSquad) throws URISyntaxException, UnknownInstanceException {
        final int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUser.getId(), loggedInUserApi);
        final int memberDetailsProblems = governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance()).checkRegistrationNumber(loggedInUser.getRegistrationNumber()) != null ? 1 : 0;

        List<NavItem> navItems = new ArrayList<>();
        navItems.add(new NavItem("my.outings", urlBuilder.applicationUrl("/"), pendingOutingsCountFor, "pendingOutings", false));
        navItems.add(new NavItem("my.details", urlBuilder.applicationUrl("/member/" + loggedInUser.getId() + "/edit"), memberDetailsProblems, "memberDetailsProblems", false));
        navItems.add(new NavItem("outings", urlBuilder.outingsUrl(preferredSquad), null, null, false));
        navItems.add(new NavItem("availability", urlBuilder.availabilityUrl(preferredSquad), null, null, false));
        navItems.add(new NavItem("contacts", urlBuilder.contactsUrl(preferredSquad), null, null, false));

        if (permissionsService.hasPermission(loggedInUser, Permission.VIEW_ENTRY_DETAILS)) {
            navItems.add(new NavItem("entry.details", urlBuilder.entryDetailsUrl(preferredSquad), null, null, false));
        }
        if (permissionsService.hasPermission(loggedInUser, Permission.VIEW_ADMIN_SCREEN)) {
            navItems.add(new NavItem("admin", urlBuilder.adminUrl(), null, null, false));
        }
        return navItems;
    }

}