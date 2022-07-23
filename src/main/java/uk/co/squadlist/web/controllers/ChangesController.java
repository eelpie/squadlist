package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.model.swagger.Change;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
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
public class ChangesController {

    private final ViewFactory viewFactory;
    private final SquadlistApiFactory squadlistApiFactory;
    private final LoggedInUserService loggedInUserService;
    private final PreferredSquadService preferredSquadService;
    private final OutingAvailabilityCountsService outingAvailabilityCountsService;
    private final GoverningBodyFactory governingBodyFactory;
    private final UrlBuilder urlBuilder;
    private final PermissionsService permissionsService;

    @Autowired
    public ChangesController(ViewFactory viewFactory,
                             SquadlistApiFactory squadlistApiFactory,
                             LoggedInUserService loggedInUserService,
                             PreferredSquadService preferredSquadService,
                             OutingAvailabilityCountsService outingAvailabilityCountsService,
                             GoverningBodyFactory governingBodyFactory,
                             UrlBuilder urlBuilder,
                             PermissionsService permissionsService) {
        this.viewFactory = viewFactory;
        this.squadlistApiFactory = squadlistApiFactory;
        this.loggedInUserService = loggedInUserService;
        this.preferredSquadService = preferredSquadService;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.governingBodyFactory = governingBodyFactory;
        this.urlBuilder = urlBuilder;
        this.permissionsService = permissionsService;
    }

    @RequestMapping("/changes")
    public ModelAndView changes() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final Member loggedInUser = loggedInUserService.getLoggedInMember();

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInUser, loggedInUserApi, preferredSquad);

        List<Change> changes = squadlistApiFactory.createUnauthenticatedSwaggerClient().changeLogGet();

        return viewFactory.getViewForLoggedInUser("changes").
                addObject("title", "What's changed").
                addObject("navItems", navItems).
                addObject("changes", changes);
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
