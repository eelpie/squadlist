package uk.co.squadlist.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.model.NavItem;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
public class NavItemsBuilder {

    private final OutingAvailabilityCountsService outingAvailabilityCountsService;
    private final GoverningBodyFactory governingBodyFactory;
    private final UrlBuilder urlBuilder;
    private final PermissionsService permissionsService;
    private final PreferredSquadService preferredSquadService;

    @Autowired
    public NavItemsBuilder(OutingAvailabilityCountsService outingAvailabilityCountsService,
                           GoverningBodyFactory governingBodyFactory,
                           UrlBuilder urlBuilder,
                           PermissionsService permissionsService,
                           PreferredSquadService preferredSquadService) {
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.governingBodyFactory = governingBodyFactory;
        this.urlBuilder = urlBuilder;
        this.permissionsService = permissionsService;
        this.preferredSquadService = preferredSquadService;
    }

    public List<NavItem> navItemsFor(Member loggedInUser, InstanceSpecificApiClient loggedInUserApi, String selected) throws URISyntaxException, UnknownInstanceException {
        final int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUser.getId(), loggedInUserApi);
        final int memberDetailsProblems = governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance()).checkRegistrationNumber(loggedInUser.getRegistrationNumber()) != null ? 1 : 0;
        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());

        List<NavItem> navItems = new ArrayList<>();
        navItems.add(makeNavItemFor("my.outings", urlBuilder.applicationUrl("/"), pendingOutingsCountFor, "pendingOutings", selected));
        navItems.add(makeNavItemFor("my.details", urlBuilder.applicationUrl("/member/" + loggedInUser.getId() + "/edit"), memberDetailsProblems, "memberDetailsProblems", selected));
        navItems.add(makeNavItemFor("outings", urlBuilder.outingsUrl(preferredSquad), null, null, selected));
        navItems.add(makeNavItemFor("availability", urlBuilder.availabilityUrl(preferredSquad), null, null, selected));
        navItems.add(makeNavItemFor("contacts", urlBuilder.contactsUrl(preferredSquad), null, null, selected));

        if (permissionsService.hasPermission(loggedInUser, Permission.VIEW_ENTRY_DETAILS)) {
            navItems.add(makeNavItemFor("entry.details", urlBuilder.entryDetailsUrl(preferredSquad), null, null, selected));
        }
        if (permissionsService.hasPermission(loggedInUser, Permission.VIEW_ADMIN_SCREEN)) {
            navItems.add(makeNavItemFor("admin", urlBuilder.adminUrl(), null, null, selected));
        }
        return navItems;
    }

    private NavItem makeNavItemFor(String label, String url, Integer count, String countId, String selected) {
        return new NavItem(label, url, count, countId, label.equals(selected));
    }

}
