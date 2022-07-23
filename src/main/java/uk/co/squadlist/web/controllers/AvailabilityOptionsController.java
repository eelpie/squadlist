package uk.co.squadlist.web.controllers;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.exceptions.InvalidAvailabilityOptionException;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownAvailabilityOptionException;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.AvailabilityOption;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.AvailabilityOptionDetails;
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

@Controller
public class AvailabilityOptionsController {

    private final static Logger log = LogManager.getLogger(AvailabilityOptionsController.class);

    private final ViewFactory viewFactory;
    private final UrlBuilder urlBuilder;
    private final LoggedInUserService loggedInUserService;
    private final PreferredSquadService preferredSquadService;
    private final OutingAvailabilityCountsService outingAvailabilityCountsService;
    private final PermissionsService permissionsService;
    private final GoverningBodyFactory governingBodyFactory;

    @Autowired
    public AvailabilityOptionsController(ViewFactory viewFactory,
                                         UrlBuilder urlBuilder,
                                         LoggedInUserService loggedInUserService,
                                         PreferredSquadService preferredSquadService,
                                         OutingAvailabilityCountsService outingAvailabilityCountsService,
                                         PermissionsService permissionsService,
                                         GoverningBodyFactory governingBodyFactory) {
        this.viewFactory = viewFactory;
        this.urlBuilder = urlBuilder;
        this.loggedInUserService = loggedInUserService;
        this.preferredSquadService = preferredSquadService;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.permissionsService = permissionsService;
        this.governingBodyFactory = governingBodyFactory;
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/availability-option/{id}/edit", method = RequestMethod.GET)
    public ModelAndView editPrompt(@PathVariable String id) throws HttpFetchException, IOException, UnknownAvailabilityOptionException, SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final AvailabilityOption a = loggedInUserApi.getAvailabilityOption(id);

        final AvailabilityOptionDetails availabilityOption = new AvailabilityOptionDetails();
        availabilityOption.setName(a.getLabel());
        availabilityOption.setColour(a.getColour());

        return renderEditAvailabilityOptionForm(availabilityOption, a);
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/availability-option/{id}/delete", method = RequestMethod.GET)
    public ModelAndView deletePrompt(@PathVariable String id) throws HttpFetchException, IOException, UnknownAvailabilityOptionException, SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final AvailabilityOption a = loggedInUserApi.getAvailabilityOption(id);
        return renderDeleteForm(a, loggedInUserApi);
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/availability-option/{id}/delete", method = RequestMethod.POST)
    public ModelAndView delete(@PathVariable String id, @RequestParam(required = false) String alternative) throws HttpFetchException, IOException, UnknownAvailabilityOptionException, SignedInMemberRequiredException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final AvailabilityOption a = loggedInUserApi.getAvailabilityOption(id);

        if (!Strings.isNullOrEmpty(alternative)) {
            final AvailabilityOption alternativeOption = loggedInUserApi.getAvailabilityOption(alternative);
            log.info("Deleting availability option: " + a + " replacing with: " + alternativeOption);
            loggedInUserApi.deleteAvailabilityOption(a, alternativeOption);

        } else {
            log.info("Deleting availability option: " + a);
            loggedInUserApi.deleteAvailabilityOption(a);
        }

        return redirectToAdmin();
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/availability-option/new", method = RequestMethod.GET)
    public ModelAndView availability() throws SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        AvailabilityOptionDetails availabilityOption = new AvailabilityOptionDetails();
        availabilityOption.setColour("green");
        return renderNewAvailabilityOptionForm(availabilityOption);
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/availability-option/new", method = RequestMethod.POST)
    public ModelAndView newSquadSubmit(@Valid @ModelAttribute("availabilityOptionDetails") AvailabilityOptionDetails availabilityOptionDetails, BindingResult result) throws SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        if (result.hasErrors()) {
            return renderNewAvailabilityOptionForm(availabilityOptionDetails);
        }

        try {
            loggedInUserApi.createAvailabilityOption(availabilityOptionDetails.getName(), availabilityOptionDetails.getColour());
            return redirectToAdmin();

        } catch (InvalidAvailabilityOptionException e) {
            result.rejectValue("name", null, e.getMessage());
            return renderNewAvailabilityOptionForm(availabilityOptionDetails);
        }
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/availability-option/{id}/edit", method = RequestMethod.POST)
    public ModelAndView editPost(@PathVariable String id, @Valid @ModelAttribute("availabilityOptionDetails") AvailabilityOptionDetails availabilityOptionDetails, BindingResult result) throws HttpFetchException, IOException, UnknownAvailabilityOptionException, SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final AvailabilityOption a = loggedInUserApi.getAvailabilityOption(id);
        if (result.hasErrors()) {
            return renderEditAvailabilityOptionForm(availabilityOptionDetails, a);
        }

        a.setLabel(availabilityOptionDetails.getName());
        a.setColour(availabilityOptionDetails.getColour());

        try {
            loggedInUserApi.updateAvailabilityOption(a);

        } catch (InvalidAvailabilityOptionException e) {
            result.rejectValue("name", null, e.getMessage());
            return renderEditAvailabilityOptionForm(availabilityOptionDetails, a);
        }

        return redirectToAdmin();
    }

    private ModelAndView renderNewAvailabilityOptionForm(AvailabilityOptionDetails availabilityOptionDetails) throws SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInMember, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInMember, loggedInUserApi, preferredSquad);

        return viewFactory.getViewForLoggedInUser("newAvailabilityOption").
                addObject("title", "Add new availability option").
                addObject("navItems", navItems).
                addObject("availabilityOptionDetails", availabilityOptionDetails);
    }

    private ModelAndView renderEditAvailabilityOptionForm(AvailabilityOptionDetails availabilityOptionDetails, AvailabilityOption a) throws SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInMember, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInMember, loggedInUserApi, preferredSquad);

        return viewFactory.getViewForLoggedInUser("editAvailabilityOption").
                addObject("title", "Edit availability options").
                addObject("navItems", navItems).
                addObject("availabilityOptionDetails", availabilityOptionDetails).
                addObject("availabilityOption", a);
    }

    private ModelAndView redirectToAdmin() {
        return redirectionTo(urlBuilder.adminUrl());
    }

    private ModelAndView renderDeleteForm(final AvailabilityOption a, InstanceSpecificApiClient api) throws HttpFetchException, IOException, SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        Member loggedInMember = loggedInUserService.getLoggedInMember();

        final List<AvailabilityOption> alternatives = api.getAvailabilityOptions();
        alternatives.remove(a);

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInMember, api.getSquads());
        List<NavItem> navItems = navItemsFor(loggedInMember, api, preferredSquad);

        return viewFactory.getViewForLoggedInUser("deleteAvailabilityOption").
                addObject("title", "Delete availability option").
                addObject("navItems", navItems).
                addObject("availabilityOption", a).
                addObject("alternatives", alternatives);
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