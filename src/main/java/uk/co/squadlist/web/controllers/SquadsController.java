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
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.model.swagger.Squad;
import uk.co.squadlist.model.swagger.SquadSubmission;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.forms.SquadDetails;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.NavItemsBuilder;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.DisplayMember;
import uk.co.squadlist.web.views.model.NavItem;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

@Controller
public class SquadsController {

    private final static Logger log = LogManager.getLogger(SquadsController.class);

    private final static Splitter COMMA_SPLITTER = Splitter.on(",");

    private final UrlBuilder urlBuilder;
    private final ViewFactory viewFactory;
    private final LoggedInUserService loggedInUserService;
    private final NavItemsBuilder navItemsBuilder;
    private final InstanceConfig instanceConfig;
    private final DisplayMemberFactory displayMemberFactory;

    @Autowired
    public SquadsController(UrlBuilder urlBuilder,
                            ViewFactory viewFactory,
                            LoggedInUserService loggedInUserService,
                            NavItemsBuilder navItemsBuilder,
                            InstanceConfig instanceConfig,
                            DisplayMemberFactory displayMemberFactory
                            ) {
        this.urlBuilder = urlBuilder;
        this.viewFactory = viewFactory;
        this.loggedInUserService = loggedInUserService;
        this.navItemsBuilder = navItemsBuilder;
        this.instanceConfig = instanceConfig;
        this.displayMemberFactory = displayMemberFactory;
    }

    @RequestMapping(value = "/squad/new", method = RequestMethod.GET)
    public ModelAndView newSquad(@ModelAttribute("squadDetails") SquadDetails squadDetails) throws Exception {
        return renderNewSquadForm(new SquadDetails());
    }

    @RequestMapping(value = "/squad/new", method = RequestMethod.POST)
    public ModelAndView newSquadSubmit(@Valid @ModelAttribute("squadDetails") SquadDetails squadDetails, BindingResult result) throws UnknownInstanceException, SignedInMemberRequiredException, URISyntaxException, ApiException, IOException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        uk.co.squadlist.model.swagger.Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        if (result.hasErrors()) {
            return renderNewSquadForm(squadDetails);
        }

        try {
            SquadSubmission submission = new SquadSubmission().instance(instance).name(squadDetails.getName());
            swaggerApiClientForLoggedInUser.squadsPostWithHttpInfo(submission);
            return viewFactory.redirectionTo(urlBuilder.adminUrl());

        } catch (ApiException e) {  // TODO more precise catch
            log.info("Invalid squad");
            result.rejectValue("name", null, "squad name is already in use");
            return renderNewSquadForm(squadDetails);
        }
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/squad/{id}/delete", method = RequestMethod.GET)
    public ModelAndView deletePrompt(@PathVariable String id) throws SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException, ApiException, IOException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        uk.co.squadlist.model.swagger.Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, "admin", swaggerApiClientForLoggedInUser, instance);

        final uk.co.squadlist.model.swagger.Squad squad = swaggerApiClientForLoggedInUser.getSquad(id);
        return viewFactory.getViewFor("deleteSquadPrompt", instance).
                addObject("title", "Delete squad").
                addObject("navItems", navItems).
                addObject("squad", squad);
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/squad/{id}/delete", method = RequestMethod.POST)
    public ModelAndView delete(@PathVariable String id) throws UnknownSquadException, SignedInMemberRequiredException, ApiException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();

        final uk.co.squadlist.model.swagger.Squad squad = swaggerApiClientForLoggedInUser.getSquad(id);

        swaggerApiClientForLoggedInUser.deleteSquad(squad.getId());

        return viewFactory.redirectionTo(urlBuilder.adminUrl());
    }

    @RequestMapping(value = "/squad/{id}/edit", method = RequestMethod.GET)
    public ModelAndView editSquad(@PathVariable String id) throws SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException, ApiException, IOException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        uk.co.squadlist.model.swagger.Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        final uk.co.squadlist.model.swagger.Squad squad = swaggerApiClientForLoggedInUser.getSquad(id);

        final SquadDetails squadDetails = new SquadDetails();
        squadDetails.setName(squad.getName());

        return renderEditSquadForm(squad, squadDetails, swaggerApiClientForLoggedInUser, instance);
    }

    @RequestMapping(value = "/squad/{id}/edit", method = RequestMethod.POST)
    public ModelAndView editSquadSubmit(@PathVariable String id, @Valid @ModelAttribute("squadDetails") SquadDetails squadDetails, BindingResult result) throws UnknownSquadException, IOException, HttpFetchException, SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException, ApiException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        final Squad squad = swaggerApiClientForLoggedInUser.getSquad(id);
        if (result.hasErrors()) {
            return renderEditSquadForm(squad, squadDetails, swaggerApiClientForLoggedInUser, instance);
        }

        squad.setName(squadDetails.getName());
        log.info("Updating squad: " + squad);

        try {
            swaggerApiClientForLoggedInUser.updateSquad(squad, squad.getId());

        } catch (ApiException e) {  // TODO more precise exception
            log.warn("Invalid squad");
            return renderEditSquadForm(squad, squadDetails, swaggerApiClientForLoggedInUser, instance);
        }

        final Set<String> updatedSquadMembers = Sets.newHashSet(COMMA_SPLITTER.split(squadDetails.getMembers()).iterator());
        log.info("Setting squad members to " + updatedSquadMembers.size() + " members: " + updatedSquadMembers);
        loggedInUserApi.setSquadMembers(squad.getId(), updatedSquadMembers);

        return viewFactory.redirectionTo(urlBuilder.adminUrl());
    }

    private ModelAndView renderNewSquadForm(SquadDetails squadDetails) throws SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException, ApiException, IOException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        final Member loggedInUser = loggedInUserService.getLoggedInMember();

        uk.co.squadlist.model.swagger.Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, "admin", swaggerApiClientForLoggedInUser, instance);

        return viewFactory.getViewFor("newSquad", instance).
                addObject("title", "Add new squad").
                addObject("navItems", navItems).
                addObject("squadDetails", squadDetails);
    }

    private ModelAndView renderEditSquadForm(final uk.co.squadlist.model.swagger.Squad squad, final SquadDetails squadDetails, DefaultApi api, uk.co.squadlist.model.swagger.Instance instance) throws SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException, ApiException, IOException {
        final List<Member> squadMembers = api.squadsIdMembersGet(squad.getId());
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        final List<Member> availableMembers = api.instancesInstanceMembersGet(instance.getId());
        availableMembers.removeAll(squadMembers);

        final Member loggedInUser = loggedInUserService.getLoggedInMember();

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, "admin", swaggerApiClientForLoggedInUser, instance);

        return viewFactory.getViewFor("editSquad", instance).
                addObject("title", "Editing a squad").
                addObject("navItems", navItems).
                addObject("squad", squad).
                addObject("squadDetails", squadDetails).
                addObject("squadMembers", displayMemberFactory.toDisplayMembers(squadMembers, loggedInUser)).
                addObject("availableMembers", displayMemberFactory.toDisplayMembers(availableMembers, loggedInUser));
    }

}
