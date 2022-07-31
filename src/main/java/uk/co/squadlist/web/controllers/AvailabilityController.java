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
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.NavItemsBuilder;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.DisplayMember;
import uk.co.squadlist.web.views.model.NavItem;

import java.math.BigDecimal;
import java.util.*;

@Controller
public class AvailabilityController {

    private final PreferredSquadService preferredSquadService;
    private final ViewFactory viewFactory;
    private final ActiveMemberFilter activeMemberFilter;
    private final LoggedInUserService loggedInUserService;
    private final PermissionsService permissionsService;
    private final NavItemsBuilder navItemsBuilder;
    private final InstanceConfig instanceConfig;

    @Autowired
    public AvailabilityController(PreferredSquadService preferredSquadService, ViewFactory viewFactory,
                                  ActiveMemberFilter activeMemberFilter,
                                  LoggedInUserService loggedInUserService,
                                  PermissionsService permissionsService,
                                  NavItemsBuilder navItemsBuilder,
                                  InstanceConfig instanceConfig) {
        this.preferredSquadService = preferredSquadService;
        this.viewFactory = viewFactory;
        this.activeMemberFilter = activeMemberFilter;
        this.loggedInUserService = loggedInUserService;
        this.permissionsService = permissionsService;
        this.navItemsBuilder = navItemsBuilder;
        this.instanceConfig = instanceConfig;
    }

    @RequestMapping("/availability")
    public ModelAndView availability() throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        uk.co.squadlist.model.swagger.Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        Member loggedInMember = loggedInUserService.getLoggedInMember();
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, "availability", swaggerApiClientForLoggedInUser, instance);

        return viewFactory.getViewFor("availability", instance).
                addObject("title", "Availability").
                addObject("navItems", navItems).
                addObject("squads", swaggerApiClientForLoggedInUser.squadsGet(instance.getId()));
    }

    @RequestMapping("/availability/{squadId}")
    public ModelAndView squadAvailability(@PathVariable String squadId, @RequestParam(value = "month", required = false) String month) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        uk.co.squadlist.model.swagger.Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        Member loggedInMember = loggedInUserService.getLoggedInMember();

        ModelAndView mv = viewFactory.getViewFor("availability", instance).
                addObject("squads", swaggerApiClientForLoggedInUser.squadsGet(instance.getId()));

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, "availability", swaggerApiClientForLoggedInUser, instance);
        mv.addObject("navItems", navItems);

        final uk.co.squadlist.model.swagger.Squad squad = preferredSquadService.resolveSquad(squadId, swaggerApiClientForLoggedInUser, instance);

        if (squad != null) {
            List<Member> squadMembers = loggedInUserApi.getSquadMembers(squad.getId());
            List<Member> activeSquadMembers = activeMemberFilter.extractActive(squadMembers);

            mv.addObject("squad", squad).
                    addObject("title", squad.getName() + " availability").
                    addObject("members", toDisplayMembers(activeSquadMembers, loggedInMember));

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

            final List<uk.co.squadlist.model.swagger.OutingWithSquadAvailability> squadAvailability = swaggerApiClientForLoggedInUser.getSquadAvailability(squad.getId(), new DateTime(startDate), new DateTime(endDate));
            final List<uk.co.squadlist.model.swagger.Outing> outings = swaggerApiClientForLoggedInUser.outingsGet(instance.getId(), squad.getId(), new DateTime(startDate), new DateTime(endDate));

            mv.addObject("squadAvailability", decorateOutingsWithMembersAvailability(squadAvailability));
            mv.addObject("outings", outings);
            mv.addObject("outingMonths", getOutingMonthsFor(instance, squad, swaggerApiClientForLoggedInUser));
            mv.addObject("month", month);
        }
        return mv;
    }

    private Map<String, uk.co.squadlist.model.swagger.AvailabilityOption> decorateOutingsWithMembersAvailability(final List<uk.co.squadlist.model.swagger.OutingWithSquadAvailability> squadAvailability) {
        final Map<String, uk.co.squadlist.model.swagger.AvailabilityOption> allAvailability = Maps.newHashMap();

        for (uk.co.squadlist.model.swagger.OutingWithSquadAvailability outingWithSquadAvailability : squadAvailability) {
            final Map<String, uk.co.squadlist.model.swagger.AvailabilityOption> outingAvailability = outingWithSquadAvailability.getAvailability();
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

    // TODO duplication with outings controller
    private Map<String, Integer> getOutingMonthsFor(uk.co.squadlist.model.swagger.Instance instance, uk.co.squadlist.model.swagger.Squad squad, DefaultApi swaggerApiClientForLoggedInUser) throws ApiException {
        Map<String, BigDecimal> stringBigDecimalMap = swaggerApiClientForLoggedInUser.outingsMonthsGet(instance.getId(), squad.getId(), DateTime.now().toDateMidnight().minusDays(1).toLocalDate(), DateTime.now().plusYears(20).toLocalDate());// TODO timezone
        Map<String, Integer> result = new HashMap<>();
        for (String key: stringBigDecimalMap.keySet()) {
            result.put(key, stringBigDecimalMap.get(key).intValue());   // TODO can this int format be set in the swagger API defination?
        }
        return result;
    }

}
