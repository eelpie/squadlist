package uk.co.squadlist.web.controllers;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.*;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.DisplayMember;
import uk.co.squadlist.web.views.model.NavItem;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class AvailabilityController {

    private final PreferredSquadService preferredSquadService;
    private final ViewFactory viewFactory;
    private final ActiveMemberFilter activeMemberFilter;
    private final LoggedInUserService loggedInUserService;
    private final PermissionsService permissionsService;
    private final UrlBuilder urlBuilder;
    private final GoverningBodyFactory governingBodyFactory;
    private final OutingAvailabilityCountsService outingAvailabilityCountsService;

    @Autowired
    public AvailabilityController(PreferredSquadService preferredSquadService, ViewFactory viewFactory,
                                  ActiveMemberFilter activeMemberFilter,
                                  LoggedInUserService loggedInUserService,
                                  PermissionsService permissionsService,
                                  UrlBuilder urlBuilder,
                                  GoverningBodyFactory governingBodyFactory,
                                  OutingAvailabilityCountsService outingAvailabilityCountsService) {
        this.preferredSquadService = preferredSquadService;
        this.viewFactory = viewFactory;
        this.activeMemberFilter = activeMemberFilter;
        this.loggedInUserService = loggedInUserService;
        this.permissionsService = permissionsService;
        this.urlBuilder = urlBuilder;
        this.governingBodyFactory = governingBodyFactory;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
    }

    @RequestMapping("/availability")
    public ModelAndView availability() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInMember, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInMember, loggedInUserApi, preferredSquad);

        return viewFactory.getViewForLoggedInUser("availability").
                addObject("title", "Availability").
                addObject("navItems", navItems).
                addObject("squads", loggedInUserApi.getSquads());
    }

    @RequestMapping("/availability/{squadId}")
    public ModelAndView squadAvailability(@PathVariable String squadId, @RequestParam(value = "month", required = false) String month) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        ModelAndView mv = viewFactory.getViewForLoggedInUser("availability");
        mv.addObject("squads", loggedInUserApi.getSquads());

        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInMember, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInMember, loggedInUserApi, preferredSquad);
        mv.addObject("navItems", navItems);

        final Squad squad = preferredSquadService.resolveSquad(squadId, loggedInUserApi);

        if (squad != null) {
            List<Member> squadMembers = loggedInUserApi.getSquadMembers(squad.getId());
            List<Member> activeSquadMembers = activeMemberFilter.extractActive(squadMembers);

            mv.addObject("squad", squad).
                    addObject("title", squad.getName() + " availability").
                    addObject("members", toDisplayMembers(activeSquadMembers, loggedInUserService.getLoggedInMember()));

            if (activeSquadMembers.isEmpty()) {
                return mv;
            }

            Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
            Date endDate = DateHelper.endOfCurrentOutingPeriod().toDate();
            if (month != null) {
                final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);    // TODO Can be moved to spring?
                startDate = monthDateTime.toDate();
                endDate = monthDateTime.plusMonths(1).toDate();
            } else {
                mv.addObject("current", true);
            }

            final List<OutingWithSquadAvailability> squadAvailability = loggedInUserApi.getSquadAvailability(squad.getId(), startDate, endDate);
            final List<Outing> outings = loggedInUserApi.getSquadOutings(squad, startDate, endDate);

            mv.addObject("squadAvailability", decorateOutingsWithMembersAvailability(squadAvailability, outings));
            mv.addObject("outings", outings);
            mv.addObject("outingMonths", loggedInUserApi.getOutingMonths(squad));
            mv.addObject("month", month);
        }
        return mv;
    }

    private Map<String, AvailabilityOption> decorateOutingsWithMembersAvailability(final List<OutingWithSquadAvailability> squadAvailability, final List<Outing> outings) {
        final Map<String, AvailabilityOption> allAvailability = Maps.newHashMap();
        for (OutingWithSquadAvailability outingWithSquadAvailability : squadAvailability) {
            final Map<String, AvailabilityOption> outingAvailability = outingWithSquadAvailability.getAvailability();
            for (String member : outingAvailability.keySet()) {
                allAvailability.put(outingWithSquadAvailability.getOuting().getId() + "-" + member, outingAvailability.get(member));
            }
        }
        return allAvailability;
    }

    private List<DisplayMember> toDisplayMembers(List<Member> members, Member loggedInUser) {
        List<DisplayMember> displayMembers = new ArrayList<>();
        for (Member member : members) {
            boolean isEditable = permissionsService.hasMemberPermission(loggedInUser, Permission.EDIT_MEMBER_DETAILS, member);
            displayMembers.add(new DisplayMember(member, isEditable));
        }
        return displayMembers;
    }

    private List<NavItem> navItemsFor(Member loggedInUser, InstanceSpecificApiClient loggedInUserApi, Squad preferredSquad) throws URISyntaxException, UnknownInstanceException {
        final int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUser.getId(), loggedInUserApi);
        final int memberDetailsProblems = governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance()).checkRegistrationNumber(loggedInUser.getRegistrationNumber()) != null ? 1 : 0;

        List<NavItem> navItems = new ArrayList<>();
        navItems.add(new NavItem("my.outings", urlBuilder.applicationUrl("/"), pendingOutingsCountFor, "pendingOutings", false));
        navItems.add(new NavItem("my.details", urlBuilder.applicationUrl("/member/" + loggedInUser.getId() + "/edit"), memberDetailsProblems, "memberDetailsProblems", false));
        navItems.add(new NavItem("outings", urlBuilder.outingsUrl(preferredSquad), null, null, false));
        navItems.add(new NavItem("availability", urlBuilder.availabilityUrl(preferredSquad), null, null, true));
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
