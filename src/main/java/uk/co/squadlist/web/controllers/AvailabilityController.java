package uk.co.squadlist.web.controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.*;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.views.*;
import uk.co.squadlist.web.views.model.DisplayMember;
import uk.co.squadlist.web.views.model.NavItem;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

@Controller
public class AvailabilityController {

    private final PreferredSquadService preferredSquadService;
    private final ViewFactory viewFactory;
    private final ActiveMemberFilter activeMemberFilter;
    private final LoggedInUserService loggedInUserService;
    private final NavItemsBuilder navItemsBuilder;
    private final InstanceConfig instanceConfig;
    private final DisplayMemberFactory displayMemberFactory;
    private final CsvOutputRenderer csvOutputRenderer;
    private final PermissionsService permissionsService;

    @Autowired
    public AvailabilityController(PreferredSquadService preferredSquadService, ViewFactory viewFactory,
                                  ActiveMemberFilter activeMemberFilter,
                                  LoggedInUserService loggedInUserService,
                                  NavItemsBuilder navItemsBuilder,
                                  InstanceConfig instanceConfig,
                                  DisplayMemberFactory displayMemberFactory,
                                  CsvOutputRenderer csvOutputRenderer,
                                  PermissionsService permissionsService) {
        this.preferredSquadService = preferredSquadService;
        this.viewFactory = viewFactory;
        this.activeMemberFilter = activeMemberFilter;
        this.loggedInUserService = loggedInUserService;
        this.navItemsBuilder = navItemsBuilder;
        this.instanceConfig = instanceConfig;
        this.displayMemberFactory = displayMemberFactory;
        this.csvOutputRenderer = csvOutputRenderer;
        this.permissionsService = permissionsService;
    }

    @RequestMapping("/availability")
    public ModelAndView availability() throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
        List<Squad> squads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());
        Member loggedInMember = loggedInUserService.getLoggedInMember();

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, "availability", swaggerApiClientForLoggedInUser, instance, squads);

        return viewFactory.getViewFor("availability", instance).
                addObject("title", "Availability").
                addObject("navItems", navItems).
                addObject("squads", swaggerApiClientForLoggedInUser.getSquads(instance.getId()));
    }

    @RequestMapping("/availability/{squadId}")
    public ModelAndView squadAvailability(@PathVariable String squadId,
                                          @RequestParam(value = "month", required = false) String month,
                                          @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                          @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
        List<Squad> squads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());
        Member loggedInMember = loggedInUserService.getLoggedInMember();

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, "availability", swaggerApiClientForLoggedInUser, instance, squads);

        final Squad squad = preferredSquadService.resolveSquad(squadId, swaggerApiClientForLoggedInUser, instance);

        if (squad != null) {
            List<Member> squadMembers = swaggerApiClientForLoggedInUser.getSquadMembers(squad.getId());
            List<Member> activeSquadMembers = activeMemberFilter.extractActive(squadMembers);

            YearMonth yearMonth = month != null ? YearMonth.parse(month): null; // TODO push to Spring parameter?
            DateRange dateRange = DateRange.from(yearMonth, startDate, endDate);

            DateTimeZone dateTimeZone = DateTimeZone.forID(instance.getTimeZone());
            DateTime startDateTime = dateRange.getStart().toDateTimeAtStartOfDay(dateTimeZone);
            DateTime endDateTime = dateRange.getEnd().toDateTimeAtStartOfDay(dateTimeZone).plusDays(1);

            final List<Outing> outings = swaggerApiClientForLoggedInUser.outingsGet(instance.getId(), squad.getId(), startDateTime, endDateTime);
            Map<String, AvailabilityOption> memberOutingAvailabilityMap = decorateOutingsWithMembersAvailability(squad, startDateTime, endDateTime);

            boolean showExport = permissionsService.hasSquadPermission(loggedInMember, Permission.VIEW_SQUAD_ENTRY_DETAILS, squad);

            List<String> outingMonths = getOutingMonthsFor(instance, squad, swaggerApiClientForLoggedInUser);
            outingMonths.sort(Comparator.naturalOrder());

            String title = squad.getName() + " availability";
            if (dateRange.getMonth() != null) {
                title = squad.getName() + " availability - " + dateRange.getMonth().toString("MMMMM yyyy");
            }

            return viewFactory.getViewFor("availability", instance).
                    addObject("squads", squads).
                    addObject("title", title).
                    addObject("navItems", navItems).addObject("squad", squad).
                    addObject("members", displayMemberFactory.toDisplayMembers(activeSquadMembers, loggedInMember)).
                    addObject("outings", outings).
                    addObject("squadAvailability", memberOutingAvailabilityMap).
                    addObject("outingMonths", outingMonths).
                    addObject("current", dateRange.isCurrent()).
                    addObject("squad", squad).
                    addObject("dateRange", dateRange).
                    addObject("showExport", showExport);

        } else {
            return null;    // TODO this should 404
        }
    }

    @RequestMapping("/availability/{squadId}.csv")
    public void squadAvailabilityCsv(@PathVariable String squadId,
                                     @RequestParam(value = "month", required = false) String month,
                                     @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                     @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                     HttpServletResponse response) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        final Squad squad = preferredSquadService.resolveSquad(squadId, swaggerApiClientForLoggedInUser, instance);

        if (squad != null) {
            List<Member> squadMembers = swaggerApiClientForLoggedInUser.getSquadMembers(squad.getId());
            List<Member> activeSquadMembers = activeMemberFilter.extractActive(squadMembers);

            YearMonth yearMonth = month != null ? YearMonth.parse(month): null; // TODO push to Spring parameter?
            DateRange dateRange = DateRange.from(yearMonth, startDate, endDate);

            DateTimeZone dateTimeZone = DateTimeZone.forID(instance.getTimeZone());
            DateTime startDateTime = dateRange.getStart().toDateTimeAtStartOfDay(dateTimeZone);
            DateTime endDateTime = dateRange.getEnd().toDateTimeAtStartOfDay(dateTimeZone).plusDays(1);

            final List<Outing> outings = swaggerApiClientForLoggedInUser.outingsGet(instance.getId(), squad.getId(), startDateTime, endDateTime);
            Map<String, AvailabilityOption> memberOutingAvailabilityMap = decorateOutingsWithMembersAvailability(squad, startDateTime, endDateTime);

            DateFormatter dateFormatter = new DateFormatter(DateTimeZone.forID(instance.getTimeZone()));

            // Headings are Name the the outing dates
            // Second row is the outing notes
            ArrayList<String> headings = Lists.newArrayList("Name");
            List<String> notes = Lists.newArrayList();
            notes.add(null);

            for (Outing outing : outings) {
                headings.add(dateFormatter.dayMonthYearTime(outing.getDate()));
                notes.add(outing.getNotes());
            }

            // Iterate through the rows of squad members, outputting rows of outing availability
            final List<List<String>> rows = Lists.newArrayList();
            rows.add(notes);

            for (Member member : activeSquadMembers) {
                DisplayMember displayMember = new DisplayMember(member, false);
                List<String> cells = Lists.newArrayList();
                cells.add(displayMember.getDisplayName());

                for (Outing outing : outings) {
                    AvailabilityOption availabilityOption = memberOutingAvailabilityMap.get(outing.getId() + "-" + member.getId());
                    cells.add(availabilityOption != null ? availabilityOption.getLabel() : "");
                }
                rows.add(cells);
            }

            csvOutputRenderer.renderCsvResponse(response, headings, rows);
        }
    }

    private Map<String, AvailabilityOption> decorateOutingsWithMembersAvailability(Squad squad, DateTime startDate, DateTime endDate) throws SignedInMemberRequiredException, ApiException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        final List<OutingWithSquadAvailability> squadAvailability = swaggerApiClientForLoggedInUser.getSquadAvailability(squad.getId(), new DateTime(startDate), new DateTime(endDate));

        final Map<String, AvailabilityOption> allAvailability = Maps.newHashMap();

        for (OutingWithSquadAvailability outingWithSquadAvailability : squadAvailability) {
            final Map<String, AvailabilityOption> outingAvailability = outingWithSquadAvailability.getAvailability();
            for (String memberId : outingAvailability.keySet()) {
                String key = outingWithSquadAvailability.getOuting().getId() + "-" + memberId;    // TODO this magic format is very questionable
                allAvailability.put(key, outingAvailability.get(memberId));
            }
        }
        return allAvailability;
    }

    // TODO duplication with outings controller
    private List<String> getOutingMonthsFor(Instance instance, Squad squad, DefaultApi swaggerApiClientForLoggedInUser) throws ApiException {
        Map<String, BigDecimal> stringBigDecimalMap = swaggerApiClientForLoggedInUser.outingsMonthsGet(instance.getId(), squad.getId(), DateTime.now().toDateMidnight().minusDays(1).toLocalDate(), DateTime.now().plusYears(20).toLocalDate());// TODO timezone
        List<String> outingMonths = Lists.newArrayList(stringBigDecimalMap.keySet());
        outingMonths.sort(Comparator.naturalOrder());
        return outingMonths;
    }

}
