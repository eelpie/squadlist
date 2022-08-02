package uk.co.squadlist.web.controllers;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.forms.InstanceDetails;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.*;
import uk.co.squadlist.web.views.model.DisplayMember;
import uk.co.squadlist.web.views.model.NavItem;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@Controller
public class AdminController {

    private final static Logger log = LogManager.getLogger(AdminController.class);

    private static final List<String> MEMBER_ORDERINGS = Lists.newArrayList("firstName", "lastName");
    private static final List<String> GOVERNING_BODIES = Lists.newArrayList("british-rowing", "rowing-ireland");

    private final static Splitter COMMA_SPLITTER = Splitter.on(",");

    private final ViewFactory viewFactory;
    private final ActiveMemberFilter activeMemberFilter;
    private final CsvOutputRenderer csvOutputRenderer;
    private final UrlBuilder urlBuilder;
    private final GoverningBodyFactory governingBodyFactory;
    private final LoggedInUserService loggedInUserService;
    private final InstanceConfig instanceConfig;
    private final NavItemsBuilder navItemsBuilder;
    private final TextHelper textHelper;
    private final DisplayMemberFactory displayMemberFactory;

    @Autowired
    public AdminController(ViewFactory viewFactory,
                           ActiveMemberFilter activeMemberFilter, CsvOutputRenderer csvOutputRenderer,
                           UrlBuilder urlBuilder,
                           GoverningBodyFactory governingBodyFactory,
                           LoggedInUserService loggedInUserService,
                           InstanceConfig instanceConfig,
                           NavItemsBuilder navItemsBuilder,
                           TextHelper textHelper,
                           DisplayMemberFactory displayMemberFactory) {
        this.viewFactory = viewFactory;
        this.activeMemberFilter = activeMemberFilter;
        this.csvOutputRenderer = csvOutputRenderer;
        this.urlBuilder = urlBuilder;
        this.governingBodyFactory = governingBodyFactory;
        this.loggedInUserService = loggedInUserService;
        this.instanceConfig = instanceConfig;
        this.navItemsBuilder = navItemsBuilder;
        this.textHelper = textHelper;
        this.displayMemberFactory = displayMemberFactory;
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public ModelAndView member() throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        final List<Member> members = swaggerApiClientForLoggedInUser.instancesInstanceMembersGet(instance.getId());

        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        List<DisplayMember> activeDisplayMembers = displayMemberFactory.toDisplayMembers(activeMemberFilter.extractActive(members), loggedInUser);
        List<DisplayMember> inactiveDisplayMembers = displayMemberFactory.toDisplayMembers(activeMemberFilter.extractInactive(members), loggedInUser);
        List<DisplayMember> adminUsers = displayMemberFactory.toDisplayMembers(extractAdminUsersFrom(members), loggedInUser);

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, "admin", swaggerApiClientForLoggedInUser, instance);

        return viewFactory.getViewFor("admin", instance).
                addObject("squads", swaggerApiClientForLoggedInUser.squadsGet(instance.getId())).
                addObject("availabilityOptions", swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsGet(instance.getId())).
                addObject("title", textHelper.text("admin")).
                addObject("navItems", navItems).
                addObject("members", members).
                addObject("activeMembers", activeDisplayMembers).
                addObject("inactiveMembers", inactiveDisplayMembers).
                addObject("admins", adminUsers).
                addObject("governingBody", governingBodyFactory.getGoverningBody(instance)).
                addObject("boats", Lists.newArrayList()).
                addObject("statistics", swaggerApiClientForLoggedInUser.instancesInstanceStatisticsGet(instanceConfig.getInstance()));
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/instance", method = RequestMethod.GET)
    public ModelAndView instance() throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        final InstanceDetails instanceDetails = new InstanceDetails();
        instanceDetails.setMemberOrdering(instance.getMemberOrdering());
        instanceDetails.setGoverningBody(instance.getGoverningBody());
        return renderEditInstanceDetailsForm(instanceDetails, instance);
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/instance", method = RequestMethod.POST)
    public ModelAndView instanceSubmit(@Valid @ModelAttribute("instanceDetails") InstanceDetails instanceDetails, BindingResult result) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        if (result.hasErrors()) {
            return renderEditInstanceDetailsForm(instanceDetails, instance);
        }

        instance.setMemberOrdering(instanceDetails.getMemberOrdering());  // TODO validate
        instance.setGoverningBody(instanceDetails.getGoverningBody());  // TODO validate

        swaggerApiClientForLoggedInUser.updateInstance(instance, instance.getId());

        return redirectToAdminScreen();
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/admins", method = RequestMethod.GET)
    public ModelAndView setAdminsPrompt() throws Exception {
        Member loggedInUser = loggedInUserService.getLoggedInMember();
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        List<Member> adminMembers = Lists.newArrayList();
        List<Member> availableMembers = Lists.newArrayList();
        for (Member member : swaggerApiClientForLoggedInUser.instancesInstanceMembersGet(instance.getId())) {
            if (member.isAdmin()) {
                adminMembers.add(member);
            } else {
                availableMembers.add(member);
            }
        }

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, "admin", swaggerApiClientForLoggedInUser, instance);

        return viewFactory.getViewFor("editAdmins", instance).
                addObject("title", textHelper.text("edit.admins")).
                addObject("navItems", navItems).
                addObject("admins", adminMembers).
                addObject("availableMembers", availableMembers);
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/admins", method = RequestMethod.POST)
    public ModelAndView setAdmins(@RequestParam String admins) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        log.info("Setting admins request: " + admins);
        final List<String> updatedAdmins = Lists.newArrayList(COMMA_SPLITTER.split(admins).iterator());

        log.info("Setting admins to: " + updatedAdmins);
        swaggerApiClientForLoggedInUser.setInstanceAdmins(updatedAdmins, instance.getId());

        return redirectToAdminScreen();
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/export/members.csv", method = RequestMethod.GET)
    public void membersCSV(HttpServletResponse response) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        final List<List<String>> rows = Lists.newArrayList();
        for (Member member : swaggerApiClientForLoggedInUser.instancesInstanceMembersGet(instance.getId())) {
            DateFormatter dateFormatter = new DateFormatter(DateTimeZone.forID(instance.getTimeZone()));
            rows.add(Arrays.asList(member.getFirstName(),
                    member.getLastName(),
                    member.getKnownAs(),
                    member.getEmailAddress(),
                    member.getGender(),
                    member.getDateOfBirth() != null ? dateFormatter.dayMonthYear(member.getDateOfBirth()) : "",
                    member.getEmergencyContactName(),
                    member.getEmergencyContactNumber(),
                    member.getWeight() != null ? member.getWeight().toString() : "",
                    member.getSweepOarSide(),
                    member.getSculling(),
                    member.getRegistrationNumber(),
                    member.getRowingPoints(),
                    member.getScullingPoints(),
                    member.getRole()
            ));
        }

        csvOutputRenderer.renderCsvResponse(response, Lists.newArrayList("First name", "Last name", "Known as", "Email",
                "Gender", "Date of birth", "Emergency contact name", "Emergency contact number",
                "Weight", "Sweep oar side", "Sculling", "Registration number", "Rowing points", "Sculling points", "Role"), rows);
    }

    private List<Member> extractAdminUsersFrom(List<Member> members) {
        List<Member> admins = Lists.newArrayList();
        for (Member member : members) {
            if (member.isAdmin()) {
                admins.add(member);
            }
        }
        return admins;
    }

    private ModelAndView redirectToAdminScreen() {
        return viewFactory.redirectionTo(urlBuilder.adminUrl());
    }

    private ModelAndView renderEditInstanceDetailsForm(final InstanceDetails instanceDetails, Instance instance) throws SignedInMemberRequiredException, URISyntaxException, ApiException, IOException, UnknownInstanceException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();

        final Member loggedInUser = loggedInUserService.getLoggedInMember();

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, "admin", swaggerApiClientForLoggedInUser, instance);

        return viewFactory.getViewFor("editInstance", instance).
                addObject("title", textHelper.text("edit.instance.settings")).
                addObject("navItems", navItems).
                addObject("instanceDetails", instanceDetails).
                addObject("memberOrderings", MEMBER_ORDERINGS).
                addObject("governingBodies", GOVERNING_BODIES);
    }

}