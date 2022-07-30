package uk.co.squadlist.web.controllers;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.web.annotations.RequiresOutingPermission;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.*;
import uk.co.squadlist.web.model.*;
import uk.co.squadlist.web.model.forms.OutingDetails;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.*;
import uk.co.squadlist.web.views.model.DisplayMember;
import uk.co.squadlist.web.views.model.NavItem;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@Controller
public class OutingsController {

    private final static Logger log = LogManager.getLogger(OutingsController.class);

    private final LoggedInUserService loggedInUserService;
    private final UrlBuilder urlBuilder;
    private final DateFormatter dateFormatter;
    private final PreferredSquadService preferredSquadService;
    private final ViewFactory viewFactory;
    private final OutingAvailabilityCountsService outingAvailabilityCountsService;
    private final ActiveMemberFilter activeMemberFilter;
    private final CsvOutputRenderer csvOutputRenderer;
    private final PermissionsService permissionsService;
    private final NavItemsBuilder navItemsBuilder;

    @Autowired
    public OutingsController(LoggedInUserService loggedInUserService, UrlBuilder urlBuilder,
                             DateFormatter dateFormatter, PreferredSquadService preferredSquadService, ViewFactory viewFactory,
                             OutingAvailabilityCountsService outingAvailabilityCountsService, ActiveMemberFilter activeMemberFilter,
                             CsvOutputRenderer csvOutputRenderer, PermissionsService permissionsService,
                             NavItemsBuilder navItemsBuilder) {
        this.loggedInUserService = loggedInUserService;
        this.urlBuilder = urlBuilder;
        this.dateFormatter = dateFormatter;
        this.preferredSquadService = preferredSquadService;
        this.viewFactory = viewFactory;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.activeMemberFilter = activeMemberFilter;
        this.csvOutputRenderer = csvOutputRenderer;
        this.permissionsService = permissionsService;
        this.navItemsBuilder = navItemsBuilder;
    }

    @RequestMapping("/outings")
    public ModelAndView outings(@RequestParam(required = false, value = "squad") String squadId,
                                @RequestParam(value = "month", required = false) String month) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();

        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        Instance instance = loggedInUserApi.getInstance();

        final Squad squadToShow = preferredSquadService.resolveSquad(squadId, loggedInUserApi, loggedInUser);
        final ModelAndView mv = viewFactory.getViewFor("outings", instance);
        if (squadToShow == null) {
            mv.addObject("title", "Outings");
            return mv;
        }

        Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
        Date endDate = DateHelper.endOfCurrentOutingPeriod().toDate();

        String title = squadToShow.getName() + " outings";
        if (month != null) {
            final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);  // TODO Can be moved to spring?
            startDate = monthDateTime.toDate();
            endDate = monthDateTime.plusMonths(1).toDate();
            title = squadToShow.getName() + " outings - " + dateFormatter.fullMonthYear(startDate);
        } else {
            mv.addObject("current", true);
        }

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, loggedInUserApi, preferredSquad, "outings");

        mv.addObject("title", title).
                addObject("navItems", navItems).
                addObject("squad", squadToShow).
                addObject("startDate", startDate).
                addObject("endDate", endDate).
                addObject("month", month).
                addObject("outingMonths", getOutingMonthsFor(squadToShow, loggedInUserApi));

        List<uk.co.squadlist.model.swagger.OutingWithSquadAvailability> squadOutings = swaggerApiClientForLoggedInUser.getSquadAvailability(squadToShow.getId(), new DateTime(startDate), new DateTime(endDate));

        mv.addObject("outings", squadOutings);
        mv.addObject("outingAvailabilityCounts", outingAvailabilityCountsService.buildOutingAvailabilityCounts(squadOutings));
        mv.addObject("squads", loggedInUserApi.getSquads());

        mv.addObject("canAddOuting", permissionsService.hasPermission(loggedInUser, Permission.ADD_OUTING));
        return mv;
    }

    @RequestMapping("/outings/{id}")
    public ModelAndView outing(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();

        Instance instance = loggedInUserApi.getInstance();

        Outing oldStyleOuting = loggedInUserApi.getOuting(id);
        uk.co.squadlist.model.swagger.Outing outing = swaggerApiClientForLoggedInUser.outingsIdGet(id);

        final Map<String, uk.co.squadlist.model.swagger.AvailabilityOption> outingAvailability = swaggerApiClientForLoggedInUser.getOutingAvailability(outing.getId());
        final List<Squad> squads = loggedInUserApi.getSquads();

        final List<Member> squadMembers = loggedInUserApi.getSquadMembers(outing.getSquad().getId());
        final List<Member> activeMembers = activeMemberFilter.extractActive(squadMembers);

        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        List<DisplayMember> displayMembers = toDisplayMembers(activeMembers, loggedInUser);

        final boolean canEditOuting = permissionsService.hasOutingPermission(loggedInUser, Permission.EDIT_OUTING, oldStyleOuting);

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, loggedInUserApi, preferredSquad, "outings");

        return viewFactory.getViewFor("outing", instance).
                addObject("title", outing.getSquad().getName() + " - " + dateFormatter.dayMonthYearTime(outing.getDate())).
                addObject("navItems", navItems).
                addObject("outing", outing).
                addObject("canEditOuting", canEditOuting).
                addObject("outingMonths", getOutingMonthsFor(oldStyleOuting.getSquad(), loggedInUserApi)).
                addObject("squad", outing.getSquad()).
                addObject("squadAvailability", outingAvailability).
                addObject("squads", squads).
                addObject("members", displayMembers).
                addObject("month", ISODateTimeFormat.yearMonth().print(outing.getDate().toLocalDateTime())).    // TODO push to date parser
                addObject("canAddOuting", permissionsService.hasPermission(loggedInUser, Permission.ADD_OUTING));
    }

    @RequestMapping("/outings/{id}.csv")
    public void outingCsv(@PathVariable String id, HttpServletResponse response) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Outing outing = loggedInUserApi.getOuting(id);
        final List<Member> squadMembers = loggedInUserApi.getSquadMembers(outing.getSquad().getId());
        final Map<String, AvailabilityOption> outingAvailability = loggedInUserApi.getOutingAvailability(outing.getId());

        final List<List<String>> rows = Lists.newArrayList();
        for (Member member : squadMembers) {
            rows.add(Arrays.asList(
                    dateFormatter.dayMonthYearTime(outing.getDate()),
                    outing.getSquad().getName(),
                    member.getDisplayName(),
                    member.getRole(),
                    outingAvailability.get(member.getId()) != null ? outingAvailability.get(member.getId()).getLabel() : null
            ));
        }
        csvOutputRenderer.renderCsvResponse(response, Lists.newArrayList("Date", "Squad", "Member", "Role", "Availability"), rows);
    }

    @RequiresPermission(permission = Permission.ADD_OUTING)
    @RequestMapping(value = "/outings/new", method = RequestMethod.GET)  // TODO fails hard if no squads are available
    public ModelAndView newOuting() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        Member loggedInUser = loggedInUserService.getLoggedInMember();

        Instance instance = loggedInUserApi.getInstance();
        String timeZone = instance.getTimeZone();

        final LocalDateTime defaultOutingDateTime = DateHelper.defaultOutingStartDateTime(timeZone);
        final OutingDetails outingDefaults = new OutingDetails(defaultOutingDateTime);
        outingDefaults.setSquad(preferredSquadService.resolveSquad(null ,loggedInUserApi, loggedInUser).getId());
        return renderNewOutingForm(outingDefaults, loggedInUserApi);
    }

    @RequiresPermission(permission = Permission.ADD_OUTING)
    @RequestMapping(value = "/outings/new", method = RequestMethod.POST)
    public ModelAndView newOutingSubmit(@Valid @ModelAttribute("outing") OutingDetails outingDetails, BindingResult result) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        if (result.hasErrors()) {
            return renderNewOutingForm(outingDetails, loggedInUserApi);
        }

        try {
            final Outing newOuting = buildOutingFromOutingDetails(outingDetails, loggedInUserApi.getInstance(), loggedInUserApi);
            if (outingDetails.getRepeats() != null && outingDetails.getRepeats() && outingDetails.getRepeatsCount() != null) {
                loggedInUserApi.createOuting(newOuting, outingDetails.getRepeatsCount());
            } else {
                loggedInUserApi.createOuting(newOuting, null);
            }

            final String outingsViewForNewOutingsSquadAndMonth = urlBuilder.outings(newOuting.getSquad(), new DateTime(newOuting.getDate()).toString("yyyy-MM"));
            return viewFactory.redirectionTo(outingsViewForNewOutingsSquadAndMonth);

        } catch (InvalidOutingException e) {
            result.addError(new ObjectError("outing", e.getMessage()));
            return renderNewOutingForm(outingDetails, loggedInUserApi);

        } catch (Exception e) {
            result.addError(new ObjectError("outing", "Unknown error"));
            return renderNewOutingForm(outingDetails, loggedInUserApi);
        }
    }

    @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @RequestMapping(value = "/outings/{id}/edit", method = RequestMethod.GET)
    public ModelAndView outingEdit(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Outing outing = loggedInUserApi.getOuting(id);

        Instance instance = loggedInUserApi.getInstance();
        String timeZone = instance.getTimeZone();

        LocalDateTime outingLocalDateTime = new LocalDateTime(outing.getDate(),  DateTimeZone.forID(timeZone));
        log.info("Outing date " + outing.getDate() + " cast to localdatetime " + outingLocalDateTime + " using timezone " + timeZone);

        final OutingDetails outingDetails = new OutingDetails(outingLocalDateTime);
        outingDetails.setSquad(outing.getSquad().getId());
        outingDetails.setNotes(outing.getNotes());

        return renderEditOutingForm(outingDetails, outing, loggedInUserApi);
    }

    @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @RequestMapping(value = "/outings/{id}/delete", method = RequestMethod.GET)
    public ModelAndView deleteOutingPrompt(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        Instance instance = loggedInUserApi.getInstance();
        final Outing outing = loggedInUserApi.getOuting(id);

        final Member loggedInUser = loggedInUserService.getLoggedInMember();

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, loggedInUserApi, preferredSquad, "outings");

        return viewFactory.getViewFor("deleteOuting", instance).
                addObject("title", "Deleting an outing").
                addObject("navItems", navItems).
                addObject("outing", outing).
                addObject("canAddOuting", permissionsService.hasPermission(loggedInUser, Permission.ADD_OUTING));
    }

    @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @RequestMapping(value = "/outings/{id}/delete", method = RequestMethod.POST)
    public ModelAndView deleteOuting(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final Outing outing = loggedInUserApi.getOuting(id);

        loggedInUserApi.deleteOuting(outing.getId());

        final String exitUrl = outing.getSquad() == null ? urlBuilder.outings(outing.getSquad()) : urlBuilder.outingsUrl();
        return viewFactory.redirectionTo(exitUrl);
    }

    @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @RequestMapping(value = "/outings/{id}/close", method = RequestMethod.GET)
    public ModelAndView closeOuting(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Outing outing = loggedInUserApi.getOuting(id);

        log.info("Closing outing: " + outing);
        outing.setClosed(true);
        loggedInUserApi.updateOuting(outing);

        return redirectToOuting(outing);
    }

    @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @RequestMapping(value = "/outings/{id}/reopen", method = RequestMethod.GET)
    public ModelAndView reopenOuting(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Outing outing = loggedInUserApi.getOuting(id);

        log.info("Reopening outing: " + outing);
        outing.setClosed(false);
        loggedInUserApi.updateOuting(outing);

        return redirectToOuting(outing);
    }

    @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @RequestMapping(value = "/outings/{id}/edit", method = RequestMethod.POST)
    public ModelAndView editOutingSubmit(@PathVariable String id,
                                         @Valid @ModelAttribute("outing") OutingDetails outingDetails, BindingResult result) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Outing outing = loggedInUserApi.getOuting(id);
        if (result.hasErrors()) {
            return renderEditOutingForm(outingDetails, outing, loggedInUserApi);
        }
        try {
            final Outing updatedOuting = buildOutingFromOutingDetails(outingDetails, loggedInUserApi.getInstance(), loggedInUserApi);
            updatedOuting.setId(id);

            loggedInUserApi.updateOuting(updatedOuting);
            return redirectToOuting(updatedOuting);

        } catch (InvalidOutingException e) {
            result.addError(new ObjectError("outing", e.getMessage()));
            return renderEditOutingForm(outingDetails, outing, loggedInUserApi);

        } catch (Exception e) {
            log.error(e);
            result.addError(new ObjectError("outing", "Unknown exception"));
            return renderEditOutingForm(outingDetails, outing, loggedInUserApi);
        }
    }

    @RequestMapping(value = "/availability/ajax", method = RequestMethod.POST)
    public ModelAndView updateAvailability(
            @RequestParam(value = "outing", required = true) String outingId,
            @RequestParam(value = "availability", required = true) String availability) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        Instance instance = loggedInUserApi.getInstance();
        Member loggedInMember = loggedInUserService.getLoggedInMember();

        final Outing outing = loggedInUserApi.getOuting(outingId);

        if (!outing.isClosed()) {
            AvailabilityOption availabilityOption = getAvailabilityOptionById(availability, loggedInUserApi);
            final OutingAvailability result = loggedInUserApi.setOutingAvailability(loggedInMember, outing, availabilityOption);
            log.info("Set availability for " + loggedInMember.getUsername() + " / " + outing.getId() + ": " + availabilityOption);
            return viewFactory.getViewFor("includes/availability", instance).
                    addObject("availability", result.getAvailabilityOption());
        }

        throw new OutingClosedException();
    }

    private ModelAndView redirectToOuting(final Outing updatedOuting) {
        return viewFactory.redirectionTo(urlBuilder.outingUrl(updatedOuting));
    }

    private ModelAndView renderNewOutingForm(OutingDetails outingDetails, InstanceSpecificApiClient api) throws UnknownSquadException, SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        Instance instance = loggedInUserApi.getInstance();

        final Squad squad = preferredSquadService.resolveSquad(null, api, loggedInUser);
        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, api.getSquads());
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, api, preferredSquad, "outings");

        return viewFactory.getViewFor("newOuting", instance).
                addObject("title", "Add a new outing").
                addObject("navItems", navItems).
                addObject("squads", api.getSquads()).
                addObject("squad", squad).
                addObject("outingMonths", getOutingMonthsFor(squad, api)).
                addObject("outing", outingDetails);
    }

    private ModelAndView renderEditOutingForm(OutingDetails outingDetails, Outing outing, InstanceSpecificApiClient api) throws SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        Instance instance = loggedInUserApi.getInstance();

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, api.getSquads());
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, api, preferredSquad, "outings");

        return viewFactory.getViewFor("editOuting", instance).
                addObject("title", "Editing an outing").
                addObject("navItems", navItems).
                addObject("squads", api.getSquads()).
                addObject("squad", outing.getSquad()).
                addObject("outing", outingDetails).
                addObject("outingObject", outing).
                addObject("outingMonths", getOutingMonthsFor(outing.getSquad(), api)).
                addObject("month", ISODateTimeFormat.yearMonth().print(outing.getDate().getTime())).
                addObject("canAddOuting", permissionsService.hasPermission(loggedInUser, Permission.ADD_OUTING));
    }

    private AvailabilityOption getAvailabilityOptionById(String availabilityId, InstanceSpecificApiClient api) throws HttpFetchException, IOException, UnknownAvailabilityOptionException {
        if (Strings.isNullOrEmpty(availabilityId)) {
            return null;
        }
        return api.getAvailabilityOption(availabilityId);
    }

    private Map<String, Integer> getOutingMonthsFor(final Squad squad, InstanceSpecificApiClient api) {
        return api.getOutingMonths(squad);
    }

    private Outing buildOutingFromOutingDetails(OutingDetails outingDetails, Instance instance, InstanceSpecificApiClient loggedInUserApi) throws UnknownSquadException {
        Date date = outingDetails.toLocalTime().toDateTime(DateTimeZone.forID(instance.getTimeZone())).toDate();
        Squad squad = outingDetails.getSquad() != null ? loggedInUserApi.getSquad(outingDetails.getSquad()) : null;  // TODO validation
        String notes = outingDetails.getNotes();
        return new Outing(squad, date, notes);
    }

    private List<DisplayMember> toDisplayMembers(List<Member> members, Member loggedInUser) {
        List<DisplayMember> displayMembers = new ArrayList<>();
        for (Member member : members) {
            boolean isEditable = permissionsService.hasMemberPermission(loggedInUser, Permission.EDIT_MEMBER_DETAILS, member);
            displayMembers.add(new DisplayMember(member, isEditable));
        }
        return displayMembers;
    }

}
