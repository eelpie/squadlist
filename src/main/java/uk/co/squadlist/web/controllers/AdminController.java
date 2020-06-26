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
import org.springframework.web.servlet.view.RedirectView;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.Context;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.forms.InstanceDetails;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.CsvOutputRenderer;
import uk.co.squadlist.web.views.DateFormatter;
import uk.co.squadlist.web.views.ViewFactory;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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

    @Autowired
    public AdminController(ViewFactory viewFactory,
                           ActiveMemberFilter activeMemberFilter, CsvOutputRenderer csvOutputRenderer,
                           UrlBuilder urlBuilder,
                           Context context, DateFormatter dateFormatter, GoverningBodyFactory governingBodyFactory,
                           LoggedInUserService loggedInUserService) {
        this.viewFactory = viewFactory;
        this.activeMemberFilter = activeMemberFilter;
        this.csvOutputRenderer = csvOutputRenderer;
        this.urlBuilder = urlBuilder;
        this.context = context;
        this.dateFormatter = dateFormatter;
        this.governingBodyFactory = governingBodyFactory;
        this.loggedInUserService = loggedInUserService;
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public ModelAndView member() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final List<Member> members = loggedInUserApi.getMembers();

        return viewFactory.getViewForLoggedInUser("admin").
                addObject("squads", loggedInUserApi.getSquads()).
                addObject("availabilityOptions", loggedInUserApi.getAvailabilityOptions()).
                addObject("title", "Admin").
                addObject("members", members).
                addObject("activeMembers", activeMemberFilter.extractActive(members)).
                addObject("inactiveMembers", activeMemberFilter.extractInactive(members)).
                addObject("admins", extractAdminUsersFrom(members)).
                addObject("governingBody", governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance())).
                addObject("statistics", loggedInUserApi.statistics()).
                addObject("boats", loggedInUserApi.getBoats()).
                addObject("language", context.getLanguage());
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
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        List<Member> adminMembers = Lists.newArrayList();
        List<Member> availableMembers = Lists.newArrayList();
        for (Member member : loggedInUserApi.getMembers()) {
            if (member.getAdmin()) {
                adminMembers.add(member);
            } else {
                availableMembers.add(member);
            }
        }
        return viewFactory.getViewForLoggedInUser("editAdmins").
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
        return new ModelAndView(new RedirectView(urlBuilder.adminUrl()));
    }

    private ModelAndView renderEditInstanceDetailsForm(final InstanceDetails instanceDetails) throws SignedInMemberRequiredException, UnknownInstanceException {
        return viewFactory.getViewForLoggedInUser("editInstance").
                addObject("instanceDetails", instanceDetails).
                addObject("memberOrderings", MEMBER_ORDERINGS).
                addObject("governingBodies", GOVERNING_BODIES);
    }

}