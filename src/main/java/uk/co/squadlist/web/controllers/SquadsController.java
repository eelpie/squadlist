package uk.co.squadlist.web.controllers;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.exceptions.InvalidSquadException;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.SquadDetails;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class SquadsController {

    private final static Logger log = LogManager.getLogger(SquadsController.class);

    private final static Splitter COMMA_SPLITTER = Splitter.on(",");

    private final UrlBuilder urlBuilder;
    private final ViewFactory viewFactory;
    private final LoggedInUserService loggedInUserService;
    private final PermissionsService permissionsService;
    private final OutingAvailabilityCountsService outingAvailabilityCountsService;
    private final PreferredSquadService preferredSquadService;
    private final GoverningBodyFactory governingBodyFactory;

    @Autowired
    public SquadsController(UrlBuilder urlBuilder,
                            ViewFactory viewFactory,
                            LoggedInUserService loggedInUserService,
                            PermissionsService permissionsService,
                            OutingAvailabilityCountsService outingAvailabilityCountsService,
                            PreferredSquadService preferredSquadService,
                            GoverningBodyFactory governingBodyFactory
                            ) {
        this.urlBuilder = urlBuilder;
        this.viewFactory = viewFactory;
        this.loggedInUserService = loggedInUserService;
        this.permissionsService = permissionsService;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.preferredSquadService = preferredSquadService;
        this.governingBodyFactory = governingBodyFactory;
    }

    @RequestMapping(value = "/squad/new", method = RequestMethod.GET)
    public ModelAndView newSquad(@ModelAttribute("squadDetails") SquadDetails squadDetails) throws Exception {
        return renderNewSquadForm(new SquadDetails());
    }

    @RequestMapping(value = "/squad/new", method = RequestMethod.POST)
    public ModelAndView newSquadSubmit(@Valid @ModelAttribute("squadDetails") SquadDetails squadDetails, BindingResult result) throws UnknownInstanceException, SignedInMemberRequiredException, URISyntaxException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        if (result.hasErrors()) {
            return renderNewSquadForm(squadDetails);
        }

        try {
            loggedInUserApi.createSquad(squadDetails.getName());
            return redirectionTo(urlBuilder.adminUrl());

        } catch (InvalidSquadException e) {
            log.info("Invalid squad");
            result.rejectValue("name", null, "squad name is already in use");
            return renderNewSquadForm(squadDetails);
        }
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/squad/{id}/delete", method = RequestMethod.GET)
    public ModelAndView deletePrompt(@PathVariable String id) throws UnknownSquadException, SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final Member loggedInUser = loggedInUserService.getLoggedInMember();

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInUser, loggedInUserApi, preferredSquad);

        final Squad squad = loggedInUserApi.getSquad(id);
        return viewFactory.getViewForLoggedInUser("deleteSquadPrompt").
                addObject("title", "Delete squad").
                addObject("navItems", navItems).
                addObject("squad", squad);
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/squad/{id}/delete", method = RequestMethod.POST)
    public ModelAndView delete(@PathVariable String id) throws UnknownSquadException, SignedInMemberRequiredException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Squad squad = loggedInUserApi.getSquad(id);
        loggedInUserApi.deleteSquad(squad);
        return redirectionTo(urlBuilder.adminUrl());
    }

    @RequestMapping(value = "/squad/{id}/edit", method = RequestMethod.GET)
    public ModelAndView editSquad(@PathVariable String id) throws UnknownSquadException, SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Squad squad = loggedInUserApi.getSquad(id);

        final SquadDetails squadDetails = new SquadDetails();
        squadDetails.setName(squad.getName());

        return renderEditSquadForm(squad, squadDetails, loggedInUserApi);
    }

    @RequestMapping(value = "/squad/{id}/edit", method = RequestMethod.POST)
    public ModelAndView editSquadSubmit(@PathVariable String id, @Valid @ModelAttribute("squadDetails") SquadDetails squadDetails, BindingResult result) throws UnknownSquadException, IOException, HttpFetchException, SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Squad squad = loggedInUserApi.getSquad(id);
        if (result.hasErrors()) {
            return renderEditSquadForm(squad, squadDetails, loggedInUserApi);
        }

        squad.setName(squadDetails.getName());
        log.info("Updating squad: " + squad);

        try {
            loggedInUserApi.updateSquad(squad);
        } catch (InvalidSquadException e) {
            log.warn("Invalid squad");
            return renderEditSquadForm(squad, squadDetails, loggedInUserApi);
        }

        final Set<String> updatedSquadMembers = Sets.newHashSet(COMMA_SPLITTER.split(squadDetails.getMembers()).iterator());
        log.info("Setting squad members to " + updatedSquadMembers.size() + " members: " + updatedSquadMembers);
        loggedInUserApi.setSquadMembers(squad.getId(), updatedSquadMembers);

        return redirectionTo(urlBuilder.adminUrl());
    }

    private ModelAndView renderNewSquadForm(SquadDetails squadDetails) throws SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final Member loggedInUser = loggedInUserService.getLoggedInMember();

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInUser, loggedInUserApi, preferredSquad);

        return viewFactory.getViewForLoggedInUser("newSquad").
                addObject("title", "Add new squad").
                addObject("navItems", navItems).
                addObject("squadDetails", squadDetails);
    }

    private ModelAndView renderEditSquadForm(final Squad squad, final SquadDetails squadDetails, InstanceSpecificApiClient squadlistApi) throws SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        final List<Member> squadMembers = squadlistApi.getSquadMembers(squad.getId());
        final List<Member> availableMembers = squadlistApi.getMembers();
        availableMembers.removeAll(squadMembers);

        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final Member loggedInUser = loggedInUserService.getLoggedInMember();

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInUser, loggedInUserApi, preferredSquad);

        return viewFactory.getViewForLoggedInUser("editSquad").
                addObject("title", "Editing a squad").
                addObject("navItems", navItems).
                addObject("squad", squad).
                addObject("squadDetails", squadDetails).
                addObject("squadMembers", squadMembers).
                addObject("availableMembers", availableMembers);
    }

    private ModelAndView redirectionTo(String url) {
        RedirectView redirectView = new RedirectView(url);
        redirectView.setExposeModelAttributes(false);
        return new ModelAndView(redirectView);
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
            navItems.add(new NavItem("admin", urlBuilder.adminUrl(), null, null, true));
        }
        return navItems;
    }

}
