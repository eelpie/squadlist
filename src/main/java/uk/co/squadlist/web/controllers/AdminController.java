package uk.co.squadlist.web.controllers;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.Context;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.InstanceDetails;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
    private final Context context;
    private final DateFormatter dateFormatter;
    private final GoverningBodyFactory governingBodyFactory;
    private final LoggedInUserService loggedInUserService;
    private final InstanceConfig instanceConfig;
    private final PermissionsService permissionsService;
    private final PreferredSquadService preferredSquadService;
    private final NavItemsBuilder navItemsBuilder;
    private final TextHelper textHelper;

    @Autowired
    public AdminController(ViewFactory viewFactory,
                           ActiveMemberFilter activeMemberFilter, CsvOutputRenderer csvOutputRenderer,
                           UrlBuilder urlBuilder,
                           Context context, DateFormatter dateFormatter, GoverningBodyFactory governingBodyFactory,
                           LoggedInUserService loggedInUserService,
                           InstanceConfig instanceConfig,
                           PermissionsService permissionsService,
                           PreferredSquadService preferredSquadService,
                           NavItemsBuilder navItemsBuilder,
                           TextHelper textHelper) {
        this.viewFactory = viewFactory;
        this.activeMemberFilter = activeMemberFilter;
        this.csvOutputRenderer = csvOutputRenderer;
        this.urlBuilder = urlBuilder;
        this.context = context;
        this.dateFormatter = dateFormatter;
        this.governingBodyFactory = governingBodyFactory;
        this.loggedInUserService = loggedInUserService;
        this.instanceConfig = instanceConfig;
        this.permissionsService = permissionsService;
        this.preferredSquadService = preferredSquadService;
        this.navItemsBuilder = navItemsBuilder;
        this.textHelper = textHelper;
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public ModelAndView member() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = loggedInUserApi.getInstance();

        final List<Member> members = loggedInUserApi.getMembers();

        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        List<DisplayMember> activeDisplayMembers = toDisplayMembers(activeMemberFilter.extractActive(members), loggedInUser);
        List<DisplayMember> inactiveDisplayMembers = toDisplayMembers(activeMemberFilter.extractInactive(members), loggedInUser);
        List<DisplayMember> adminUsers = toDisplayMembers(extractAdminUsersFrom(members), loggedInUser);

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, loggedInUserApi, preferredSquad, "admin");

        return viewFactory.getViewFor("admin", instance).
                addObject("squads", loggedInUserApi.getSquads()).
                addObject("availabilityOptions", swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsGet(instance.getId())).
                addObject("title", textHelper.text("admin")).
                addObject("navItems", navItems).
                addObject("members", members).
                addObject("activeMembers", activeDisplayMembers).
                addObject("inactiveMembers", inactiveDisplayMembers).
                addObject("admins", adminUsers).
                addObject("governingBody", governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance())).
                addObject("boats", loggedInUserApi.getBoats()).
                addObject("statistics", swaggerApiClientForLoggedInUser.instancesInstanceStatisticsGet(instanceConfig.getInstance()));
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/instance", method = RequestMethod.GET)
    public ModelAndView instance() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final InstanceDetails instanceDetails = new InstanceDetails();
        instanceDetails.setMemberOrdering(loggedInUserApi.getInstance().getMemberOrdering());
        instanceDetails.setGoverningBody(loggedInUserApi.getInstance().getGoverningBody());
        return renderEditInstanceDetailsForm(instanceDetails);
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/instance", method = RequestMethod.POST)
    public ModelAndView instanceSubmit(@Valid @ModelAttribute("instanceDetails") InstanceDetails instanceDetails, BindingResult result) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        if (result.hasErrors()) {
            return renderEditInstanceDetailsForm(instanceDetails);
        }

        Instance instance = loggedInUserApi.getInstance();
        instance.setMemberOrdering(instanceDetails.getMemberOrdering());  // TODO validate
        instance.setGoverningBody(instanceDetails.getGoverningBody());  // TODO validate

        loggedInUserApi.updateInstance(instance);

        return redirectToAdminScreen();
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/admins", method = RequestMethod.GET)
    public ModelAndView setAdminsPrompt() throws Exception {
        Member loggedInUser = loggedInUserService.getLoggedInMember();
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        Instance instance = loggedInUserApi.getInstance();

        List<Member> adminMembers = Lists.newArrayList();
        List<Member> availableMembers = Lists.newArrayList();
        for (Member member : loggedInUserApi.getMembers()) {
            if (member.getAdmin()) {
                adminMembers.add(member);
            } else {
                availableMembers.add(member);
            }
        }

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, loggedInUserApi, preferredSquad, "admin");

        return viewFactory.getViewFor("editAdmins", instance).
                addObject("title", textHelper.text("edit.admins")).
                addObject("navItems", navItems).
                addObject("admins", adminMembers).
                addObject("availableMembers", availableMembers);
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/admins", method = RequestMethod.POST)
    public ModelAndView setAdmins(@RequestParam String admins) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        log.info("Setting admins request: " + admins);
        final Set<String> updatedAdmins = Sets.newHashSet(COMMA_SPLITTER.split(admins).iterator());

        log.info("Setting admins to: " + updatedAdmins);
        loggedInUserApi.setAdmins(updatedAdmins);

        return redirectToAdminScreen();
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/export/members.csv", method = RequestMethod.GET)
    public void membersCSV(HttpServletResponse response) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final List<List<String>> rows = Lists.newArrayList();
        for (Member member : loggedInUserApi.getMembers()) {
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
            if (member.getAdmin() != null && member.getAdmin()) {  // TODO should be boolean is the API knows that it is always present.
                admins.add(member);
            }
        }
        return admins;
    }

    private ModelAndView redirectToAdminScreen() {
        return viewFactory.redirectionTo(urlBuilder.adminUrl());
    }

    private ModelAndView renderEditInstanceDetailsForm(final InstanceDetails instanceDetails) throws SignedInMemberRequiredException, UnknownInstanceException, URISyntaxException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        Instance instance = loggedInUserApi.getInstance();

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, loggedInUserApi, preferredSquad, "admin");

        return viewFactory.getViewFor("editInstance", instance).
                addObject("title", textHelper.text("edit.instance.settings")).
                addObject("navItems", navItems).
                addObject("instanceDetails", instanceDetails).
                addObject("memberOrderings", MEMBER_ORDERINGS).
                addObject("governingBodies", GOVERNING_BODIES);
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