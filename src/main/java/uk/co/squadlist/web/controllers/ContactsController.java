package uk.co.squadlist.web.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
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
public class ContactsController {

    private static final Logger log = LogManager.getLogger(ContactsController.class);

    private final PreferredSquadService preferredSquadService;
    private final ViewFactory viewFactory;
    private final ContactsModelPopulator contactsModelPopulator;
    private final LoggedInUserService loggedInUserService;
    private PermissionsService permissionsService;
    private UrlBuilder urlBuilder;
    private OutingAvailabilityCountsService outingAvailabilityCountsService;
    private GoverningBodyFactory governingBodyFactory;

    @Autowired
    public ContactsController(PreferredSquadService preferredSquadService,
                              ViewFactory viewFactory,
                              ContactsModelPopulator contactsModelPopulator,
                              LoggedInUserService loggedInUserService,
                              PermissionsService permissionsService,
                              UrlBuilder urlBuilder,
                              OutingAvailabilityCountsService outingAvailabilityCountsService,
                              GoverningBodyFactory governingBodyFactory) {
        this.preferredSquadService = preferredSquadService;
        this.viewFactory = viewFactory;
        this.contactsModelPopulator = contactsModelPopulator;
        this.loggedInUserService = loggedInUserService;
        this.permissionsService = permissionsService;
        this.urlBuilder = urlBuilder;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.governingBodyFactory = governingBodyFactory;
    }

    @RequestMapping("/contacts")
    public ModelAndView contacts() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final List<Squad> allSquads = loggedInUserApi.getSquads();

        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInMember, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInMember, loggedInUserApi, preferredSquad);

        return viewFactory.getViewForLoggedInUser("contacts").
                addObject("title", "Contacts").
                addObject("squads", allSquads);    // TODO leaves squad null on view
    }

    @RequestMapping("/contacts/{squadId}")
    public ModelAndView squadContacts(@PathVariable String squadId) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Squad squadToShow = preferredSquadService.resolveSquad(squadId, loggedInUserApi);
        final List<Squad> allSquads = loggedInUserApi.getSquads();

        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInMember, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInMember, loggedInUserApi, preferredSquad);

        final ModelAndView mv = viewFactory.getViewForLoggedInUser("contacts").
                addObject("title", "Contacts").
                addObject("navItems", navItems).
                addObject("squads", allSquads);

        if (!allSquads.isEmpty()) {
            log.info("Squad to show: " + squadToShow);
            contactsModelPopulator.populateModel(squadToShow, mv, loggedInUserApi.getInstance(), loggedInUserApi, loggedInUserService.getLoggedInMember());
        }
        return mv;
    }

    private List<NavItem> navItemsFor(Member loggedInUser, InstanceSpecificApiClient loggedInUserApi, Squad preferredSquad) throws URISyntaxException, UnknownInstanceException {
        final int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUser.getId(), loggedInUserApi);
        final int memberDetailsProblems = governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance()).checkRegistrationNumber(loggedInUser.getRegistrationNumber()) != null ? 1 : 0;

        List<NavItem> navItems = new ArrayList<>();
        navItems.add(new NavItem("my.outings", urlBuilder.applicationUrl("/"), pendingOutingsCountFor, "pendingOutings", false));
        navItems.add(new NavItem("my.details", urlBuilder.applicationUrl("/member/" + loggedInUser.getId() + "/edit"), memberDetailsProblems, "memberDetailsProblems", false));
        navItems.add(new NavItem("outings", urlBuilder.outingsUrl(preferredSquad), null, null, false));
        navItems.add(new NavItem("availability", urlBuilder.availabilityUrl(preferredSquad), null, null, false));
        navItems.add(new NavItem("contacts", urlBuilder.contactsUrl(preferredSquad), null, null, true));

        if (permissionsService.hasPermission(loggedInUser, Permission.VIEW_ENTRY_DETAILS)) {
            navItems.add(new NavItem("entry.details", urlBuilder.entryDetailsUrl(preferredSquad), null, null, false));
        }
        if (permissionsService.hasPermission(loggedInUser, Permission.VIEW_ADMIN_SCREEN)) {
            navItems.add(new NavItem("admin", urlBuilder.adminUrl(), null, null, false));
        }
        return navItems;
    }

}
