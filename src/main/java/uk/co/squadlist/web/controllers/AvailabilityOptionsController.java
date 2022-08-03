package uk.co.squadlist.web.controllers;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.AvailabilityOption;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownAvailabilityOptionException;
import uk.co.squadlist.web.model.forms.AvailabilityOptionDetails;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.NavItemsBuilder;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Controller
public class AvailabilityOptionsController {

    private final static Logger log = LogManager.getLogger(AvailabilityOptionsController.class);

    private final ViewFactory viewFactory;
    private final UrlBuilder urlBuilder;
    private final LoggedInUserService loggedInUserService;
    private final NavItemsBuilder navItemsBuilder;
    private final InstanceConfig instanceConfig;

    @Autowired
    public AvailabilityOptionsController(ViewFactory viewFactory,
                                         UrlBuilder urlBuilder,
                                         LoggedInUserService loggedInUserService,
                                         NavItemsBuilder navItemsBuilder,
                                         InstanceConfig instanceConfig) {
        this.viewFactory = viewFactory;
        this.urlBuilder = urlBuilder;
        this.loggedInUserService = loggedInUserService;
        this.navItemsBuilder = navItemsBuilder;
        this.instanceConfig = instanceConfig;
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/availability-option/{id}/edit", method = RequestMethod.GET)
    public ModelAndView editPrompt(@PathVariable String id) throws IOException, SignedInMemberRequiredException, URISyntaxException, ApiException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        final AvailabilityOption availabilityOption = swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdGet(instance.getId(), id);

        final AvailabilityOptionDetails availabilityOptionDetails = new AvailabilityOptionDetails();
        availabilityOptionDetails.setName(availabilityOption.getLabel());
        availabilityOptionDetails.setColour(availabilityOption.getColour());

        return renderEditAvailabilityOptionForm(instance, availabilityOptionDetails, availabilityOption);
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/availability-option/{id}/delete", method = RequestMethod.GET)
    public ModelAndView deletePrompt(@PathVariable String id) throws IOException, SignedInMemberRequiredException, URISyntaxException, ApiException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        final AvailabilityOption availabilityOption = swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdGet(instance.getId(), id);

        return renderDeleteForm(instance, loggedInUserService.getLoggedInMember(), availabilityOption);
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/availability-option/{id}/delete", method = RequestMethod.POST)
    public ModelAndView delete(@PathVariable String id, @RequestParam(required = false) String alternative) throws HttpFetchException, IOException, UnknownAvailabilityOptionException, SignedInMemberRequiredException, ApiException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        final AvailabilityOption availabilityOption = swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdGet(instance.getId(), id);

        if (!Strings.isNullOrEmpty(alternative)) {
            final AvailabilityOption alternativeOption = swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdGet(instance.getId(), alternative);
            log.info("Deleting availability option: " + availabilityOption + " replacing with: " + alternativeOption);
            swaggerApiClientForLoggedInUser.deleteAvailabilityOption(instance.getId(), availabilityOption.getId(), alternativeOption.getId());

        } else {
            log.info("Deleting availability option: " + availabilityOption);
            swaggerApiClientForLoggedInUser.deleteAvailabilityOption(instance.getId(), availabilityOption.getId(), null);
        }

        return redirectToAdmin();
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/availability-option/new", method = RequestMethod.GET)
    public ModelAndView availability() throws SignedInMemberRequiredException, URISyntaxException, ApiException, IOException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
        Member loggedInMember = loggedInUserService.getLoggedInMember();

        AvailabilityOptionDetails availabilityOptionDetails = new AvailabilityOptionDetails();
        availabilityOptionDetails.setColour("green");
        return renderNewAvailabilityOptionForm(instance, loggedInMember, availabilityOptionDetails);
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/availability-option/new", method = RequestMethod.POST)
    public ModelAndView newSquadSubmit(@Valid @ModelAttribute("availabilityOptionDetails") AvailabilityOptionDetails availabilityOptionDetails, BindingResult result) throws SignedInMemberRequiredException, URISyntaxException, ApiException, IOException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
        Member loggedInMember = loggedInUserService.getLoggedInMember();

        if (result.hasErrors()) {
            return renderNewAvailabilityOptionForm(instance, loggedInMember, availabilityOptionDetails);
        }

        try {
            AvailabilityOption newAvailabilityOption = new AvailabilityOption().label(availabilityOptionDetails.getName()).colour(availabilityOptionDetails.getColour());
            swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsPost(newAvailabilityOption, instance.getId());
            return redirectToAdmin();

        } catch (ApiException e) {
            result.rejectValue("name", null, e.getResponseBody());
            return renderNewAvailabilityOptionForm(instance, loggedInMember, availabilityOptionDetails);
        }
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/availability-option/{id}/edit", method = RequestMethod.POST)
    public ModelAndView editPost(@PathVariable String id, @Valid @ModelAttribute("availabilityOptionDetails") AvailabilityOptionDetails availabilityOptionDetails, BindingResult result) throws IOException, SignedInMemberRequiredException, URISyntaxException, ApiException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        final AvailabilityOption availabilityOption = swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdGet(instance.getId(), id);
        if (result.hasErrors()) {
            return renderEditAvailabilityOptionForm(instance, availabilityOptionDetails, availabilityOption);
        }

        availabilityOption.setLabel(availabilityOptionDetails.getName());
        availabilityOption.setColour(availabilityOptionDetails.getColour());

        try {
            swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdPost(availabilityOption, instance.getId(), availabilityOption.getId());

        } catch (ApiException e) {
            result.rejectValue("name", null, e.getResponseBody());
            return renderEditAvailabilityOptionForm(instance, availabilityOptionDetails, availabilityOption);
        }

        return redirectToAdmin();
    }

    private ModelAndView renderNewAvailabilityOptionForm(Instance instance, Member loggedInMember, AvailabilityOptionDetails availabilityOptionDetails) throws SignedInMemberRequiredException, URISyntaxException, ApiException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, "admin", swaggerApiClientForLoggedInUser, instance);

        return viewFactory.getViewFor("newAvailabilityOption", instance).
                addObject("title", "Add new availability option").
                addObject("navItems", navItems).
                addObject("availabilityOptionDetails", availabilityOptionDetails);
    }

    private ModelAndView renderEditAvailabilityOptionForm(Instance instance, AvailabilityOptionDetails availabilityOptionDetails, AvailabilityOption availabilityOption) throws SignedInMemberRequiredException, URISyntaxException, ApiException, IOException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Member loggedInMember = loggedInUserService.getLoggedInMember();

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, "admin", swaggerApiClientForLoggedInUser, instance);

        return viewFactory.getViewFor("editAvailabilityOption", instance).
                addObject("title", "Edit availability options").
                addObject("navItems", navItems).
                addObject("availabilityOptionDetails", availabilityOptionDetails).
                addObject("availabilityOption", availabilityOption);
    }

    private ModelAndView redirectToAdmin() {
        return viewFactory.redirectionTo(urlBuilder.adminUrl());
    }

    private ModelAndView renderDeleteForm(Instance instance, Member loggedInMember, AvailabilityOption selected) throws SignedInMemberRequiredException, URISyntaxException, ApiException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();

        final List<AvailabilityOption> alternatives = swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsGet(instance.getId());
        alternatives.remove(selected);

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, "admin", swaggerApiClientForLoggedInUser, instance);

        return viewFactory.getViewFor("deleteAvailabilityOption", instance).
                addObject("title", "Delete availability option").
                addObject("navItems", navItems).
                addObject("availabilityOption", selected).
                addObject("alternatives", alternatives);
    }

}