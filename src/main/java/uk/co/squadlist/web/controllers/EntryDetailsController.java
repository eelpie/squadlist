package uk.co.squadlist.web.controllers;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.CsvOutputRenderer;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Controller
public class EntryDetailsController {

    private final static Logger log = LogManager.getLogger(EntryDetailsController.class);

    private final PreferredSquadService preferredSquadService;
    private final ViewFactory viewFactory;
    private final EntryDetailsModelPopulator entryDetailsModelPopulator;
    private final CsvOutputRenderer csvOutputRenderer;
    private final GoverningBodyFactory governingBodyFactory;
    private final LoggedInUserService loggedInUserService;
    private final OutingAvailabilityCountsService outingAvailabilityCountsService;
    private final UrlBuilder urlBuilder;
    private final PermissionsService permissionsService;

    @Autowired
    public EntryDetailsController(PreferredSquadService preferredSquadService,
                                  ViewFactory viewFactory,
                                  EntryDetailsModelPopulator entryDetailsModelPopulator,
                                  CsvOutputRenderer csvOutputRenderer,
                                  GoverningBodyFactory governingBodyFactory,
                                  LoggedInUserService loggedInUserService,
                                  OutingAvailabilityCountsService outingAvailabilityCountsService,
                                  UrlBuilder urlBuilder,
                                  PermissionsService permissionsService
                                  ) {
        this.preferredSquadService = preferredSquadService;
        this.viewFactory = viewFactory;
        this.entryDetailsModelPopulator = entryDetailsModelPopulator;
        this.csvOutputRenderer = csvOutputRenderer;
        this.governingBodyFactory = governingBodyFactory;
        this.loggedInUserService = loggedInUserService;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.urlBuilder = urlBuilder;
        this.permissionsService = permissionsService;
    }

    @RequestMapping("/entrydetails/{squadId}")
    public ModelAndView entrydetails(@PathVariable String squadId) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final Squad squadToShow = preferredSquadService.resolveSquad(squadId, loggedInUserApi);

        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInMember, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInMember, loggedInUserApi, preferredSquad);

        final ModelAndView mv = viewFactory.getViewForLoggedInUser("entryDetails").
                addObject("title", "Entry details").
                addObject("navItems", navItems).
                addObject("squads", loggedInUserApi.getSquads()).
                addObject("governingBody", governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance()));
        entryDetailsModelPopulator.populateModel(squadToShow, loggedInUserApi, mv, loggedInUserService.getLoggedInMember());
        return mv;
    }

    @RequestMapping("/entrydetails/ajax")
    public ModelAndView ajax(@RequestBody String json) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        List<Member> selectedMembers = Lists.newArrayList();

        JsonNode readTree = new ObjectMapper().readTree(json);
        Iterator<JsonNode> iterator = readTree.iterator();
        while (iterator.hasNext()) {
            selectedMembers.add(loggedInUserApi.getMember(iterator.next().asText()));
        }

        List<String> rowingPoints = Lists.newArrayList();
        List<String> scullingPoints = Lists.newArrayList();
        for (Member member : selectedMembers) {
            rowingPoints.add(member.getRowingPoints());
            scullingPoints.add(member.getScullingPoints());
        }

        final ModelAndView mv = viewFactory.getViewForLoggedInUser("entryDetailsAjax");
        if (!selectedMembers.isEmpty()) {
            mv.addObject("members", selectedMembers);

            final GoverningBody governingBody = governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance());

            int crewSize = selectedMembers.size();
            final boolean isFullBoat = governingBody.getBoatSizes().contains(crewSize);
            mv.addObject("ok", isFullBoat);
            if (isFullBoat) {
                mv.addObject("rowingPoints", governingBody.getTotalPoints(rowingPoints));
                mv.addObject("rowingStatus", governingBody.getRowingStatus(rowingPoints));

                mv.addObject("scullingPoints", governingBody.getTotalPoints(scullingPoints));
                mv.addObject("scullingStatus", governingBody.getScullingStatus(scullingPoints));

                List<Date> datesOfBirth = Lists.newArrayList();
                for (Member member : selectedMembers) {
                    datesOfBirth.add(member.getDateOfBirth());
                }

                Integer effectiveAge = governingBody.getEffectiveAge(datesOfBirth);
                if (effectiveAge != null) {
                    mv.addObject("effectiveAge", effectiveAge);
                    mv.addObject("ageGrade", governingBody.getAgeGrade(effectiveAge));
                }
            }
        }
        return mv;
    }

    @RequestMapping(value = "/entrydetails/{squadId}.csv", method = RequestMethod.GET)
    public void entrydetailsCSV(@PathVariable String squadId, HttpServletResponse response) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        viewFactory.getViewForLoggedInUser("entryDetails");  // TODO This call is probably only been used for access control

        final Squad squadToShow = preferredSquadService.resolveSquad(squadId, loggedInUserApi);
        final List<Member> squadMembers = loggedInUserApi.getSquadMembers(squadToShow.getId());

        GoverningBody governingBody = governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance());
        List<List<String>> entryDetailsRows = entryDetailsModelPopulator.getEntryDetailsRows(squadMembers, governingBody);

        csvOutputRenderer.renderCsvResponse(response, entryDetailsModelPopulator.getEntryDetailsHeaders(), entryDetailsRows);
    }

    @RequestMapping(value = "/entrydetails/selected.csv", method = RequestMethod.GET) // TODO Unused
    public void entrydetailsSelectedCSV(@RequestParam String members, HttpServletResponse response) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        List<Member> selectedMembers = Lists.newArrayList();
        final Iterator<String> iterator = Splitter.on(",").split(members).iterator();
        while (iterator.hasNext()) {
            final String selectedMemberId = iterator.next();
            log.info("Selected member id: " + selectedMemberId);
            selectedMembers.add(loggedInUserApi.getMember(selectedMemberId));
        }

        GoverningBody governingBody = governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance());
        csvOutputRenderer.renderCsvResponse(response,
                entryDetailsModelPopulator.getEntryDetailsHeaders(),
                entryDetailsModelPopulator.getEntryDetailsRows(selectedMembers, governingBody)
        );
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
            navItems.add(new NavItem("entry.details", urlBuilder.entryDetailsUrl(preferredSquad), null, null, true));
        }
        if (permissionsService.hasPermission(loggedInUser, Permission.VIEW_ADMIN_SCREEN)) {
            navItems.add(new NavItem("admin", urlBuilder.adminUrl(), null, null, false));
        }
        return navItems;
    }

}
