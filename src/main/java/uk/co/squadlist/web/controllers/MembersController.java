package uk.co.squadlist.web.controllers;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalFieldValueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.model.swagger.Squad;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.PermissionDeniedException;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.forms.ChangePassword;
import uk.co.squadlist.web.model.forms.MemberDetails;
import uk.co.squadlist.web.model.forms.MemberSquad;
import uk.co.squadlist.web.services.PasswordGenerator;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.NavItemsBuilder;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

@Controller
public class MembersController {

    private final static Logger log = LogManager.getLogger(MembersController.class);

    private static final List<String> GENDER_OPTIONS = Lists.newArrayList("female", "male");
    private static final List<String> ROLES_OPTIONS = Lists.newArrayList("Rower", "Rep", "Coach", "Cox", "Non rowing");
    private static final List<String> SWEEP_OAR_SIDE_OPTIONS = Lists.newArrayList("Bow", "Stroke", "Bow/Stroke", "Stroke/Bow");
    private static final List<String> YES_NO_OPTIONS = Lists.newArrayList("Y", "N");

    private LoggedInUserService loggedInUserService;
    private UrlBuilder urlBuilder;
    private ViewFactory viewFactory;
    private PasswordGenerator passwordGenerator;
    private PermissionsService permissionsService;
    private GoverningBodyFactory governingBodyFactory;
    private NavItemsBuilder navItemsBuilder;
    private InstanceConfig instanceConfig;

    public MembersController() {
    }

    @Autowired
    public MembersController(LoggedInUserService loggedInUserService, UrlBuilder urlBuilder,
                             ViewFactory viewFactory,
                             PasswordGenerator passwordGenerator,
                             PermissionsService permissionsService,
                             GoverningBodyFactory governingBodyFactory,
                             NavItemsBuilder navItemsBuilder,
                             InstanceConfig instanceConfig) {
        this.loggedInUserService = loggedInUserService;
        this.urlBuilder = urlBuilder;
        this.viewFactory = viewFactory;
        this.passwordGenerator = passwordGenerator;
        this.permissionsService = permissionsService;
        this.governingBodyFactory = governingBodyFactory;
        this.navItemsBuilder = navItemsBuilder;
        this.instanceConfig = instanceConfig;
    }

    @RequestMapping("/member/{id}")
    public ModelAndView member(@PathVariable String id) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        final Member loggedInMember = loggedInUserService.getLoggedInMember();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
        final Member member = swaggerApiClientForLoggedInUser.getMember(id);

        if (permissionsService.hasMemberPermission(loggedInMember, Permission.EDIT_MEMBER_DETAILS, member)) {
            List<Squad> squads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());
            List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, null, swaggerApiClientForLoggedInUser, instance, squads);
            return viewFactory.getViewFor("memberDetails", instance).
                    addObject("member", member).
                    addObject("title", member.getFirstName() + " " + member.getLastName()).
                    addObject("navItems", navItems).
                    addObject("governingBody", governingBodyFactory.getGoverningBody(instance));
        } else {
            throw new PermissionDeniedException();
        }
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.GET)
    public ModelAndView changePassword() throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        return renderChangePasswordForm(instance, loggedInUser, new ChangePassword());
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
    public ModelAndView changePasswordSubmit(@Valid @ModelAttribute("changePassword") ChangePassword changePassword, BindingResult result) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        final Member loggedInMember = loggedInUserService.getLoggedInMember();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        if (result.hasErrors()) {
            return renderChangePasswordForm(instance, loggedInMember, changePassword);
        }

        log.info("Requesting change password for member: " + loggedInMember.getId());
        try {
            swaggerApiClientForLoggedInUser.changePassword(loggedInMember.getId(), changePassword.getCurrentPassword(), changePassword.getNewPassword());
            return viewFactory.redirectionTo(urlBuilder.memberUrl(loggedInMember));

        } catch (ApiException e) {
            result.addError(new ObjectError("changePassword", "Change password failed"));
            return renderChangePasswordForm(instance, loggedInMember, changePassword);
        }
    }

    @RequestMapping(value = "/member/{id}/edit", method = RequestMethod.GET)
    public ModelAndView updateMember(@PathVariable String id, @RequestParam(required = false) Boolean invalidImage) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
        final Member member = swaggerApiClientForLoggedInUser.getMember(id);
        final Member loggedInMember = loggedInUserService.getLoggedInMember();

        if (permissionsService.hasMemberPermission(loggedInMember, Permission.EDIT_MEMBER_DETAILS, member)) {
            final MemberDetails memberDetails = new MemberDetails();
            memberDetails.setFirstName(member.getFirstName());
            memberDetails.setLastName(member.getLastName());
            memberDetails.setKnownAs(member.getKnownAs());
            memberDetails.setGender(member.getGender());

            memberDetails.setDateOfBirthDay(member.getDateOfBirth() != null ? new DateTime(member.getDateOfBirth()).getDayOfMonth() : null);
            memberDetails.setDateOfBirthMonth(member.getDateOfBirth() != null ? new DateTime(member.getDateOfBirth()).getMonthOfYear() : null);
            memberDetails.setDateOfBirthYear(member.getDateOfBirth() != null ? new DateTime(member.getDateOfBirth()).getYear() : null);

            memberDetails.setWeight(member.getWeight() != null ? Integer.toString(member.getWeight().intValue()) : null);
            memberDetails.setEmailAddress(member.getEmailAddress());
            memberDetails.setContactNumber(member.getContactNumber());
            memberDetails.setRegistrationNumber(member.getRegistrationNumber());
            memberDetails.setRowingPoints(member.getRowingPoints());
            memberDetails.setSculling(member.getSculling());
            memberDetails.setScullingPoints(member.getScullingPoints());
            memberDetails.setSweepOarSide(member.getSweepOarSide());

            List<MemberSquad> memberSquads = Lists.newArrayList();
            for (Squad squad : member.getSquads()) {
                memberSquads.add(new MemberSquad(squad.getId()));
            }

            memberDetails.setSquads(memberSquads);
            memberDetails.setEmergencyContactName(member.getEmergencyContactName());
            memberDetails.setEmergencyContactNumber(member.getEmergencyContactNumber());
            memberDetails.setRole(member.getRole());
            memberDetails.setProfileImage(member.getProfileImage());

            GoverningBody governingBody = governingBodyFactory.getGoverningBody(instance);

            return renderEditMemberDetailsForm(instance,
                    memberDetails,
                    member,
                    governingBody, swaggerApiClientForLoggedInUser).
                    addObject("invalidImage", invalidImage);
        } else {
            throw new PermissionDeniedException();
        }
    }

    @RequestMapping(value = "/member/{id}/edit", method = RequestMethod.POST)
    public ModelAndView updateMemberSubmit(@PathVariable String id, @Valid @ModelAttribute("memberDetails") MemberDetails memberDetails, BindingResult result) throws Exception {
        log.info("Received edit member request: " + memberDetails);
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
        final Member member = swaggerApiClientForLoggedInUser.getMember(id);
        final Member loggedInMember = loggedInUserService.getLoggedInMember();

        if (permissionsService.hasMemberPermission(loggedInMember, Permission.EDIT_MEMBER_DETAILS, member)) {

            final List<Squad> squads = extractAndValidateRequestedSquads(memberDetails, result, swaggerApiClientForLoggedInUser);
            final GoverningBody governingBody = governingBodyFactory.getGoverningBody(instance);

            if (!Strings.isNullOrEmpty(memberDetails.getScullingPoints())) {
                if (!governingBody.getPointsOptions().contains(memberDetails.getScullingPoints())) {
                    result.addError(new ObjectError("member.scullingPoints", "Invalid points option"));
                }
            }
            if (!Strings.isNullOrEmpty(memberDetails.getRowingPoints())) {
                if (!governingBody.getPointsOptions().contains(memberDetails.getRowingPoints())) {
                    result.addError(new ObjectError("member.rowingPoints", "Invalid points option"));
                }
            }

            DateTime updatedDateOfBirth = null;
            if (memberDetails.getDateOfBirthDay() != null &&
                    memberDetails.getDateOfBirthMonth() != null &&
                    memberDetails.getDateOfBirthYear() != null) {
                try {
                    updatedDateOfBirth = new DateTime(memberDetails.getDateOfBirthYear(),
                            memberDetails.getDateOfBirthMonth(), memberDetails.getDateOfBirthDay(), 0, 0, 0,
                            DateTimeZone.UTC);
                    member.setDateOfBirth(updatedDateOfBirth);
                } catch (IllegalFieldValueException e) {
                    result.addError(new ObjectError("member.dateOfBirthYear", "Invalid date"));
                }
            }

            if (result.hasErrors()) {
                return renderEditMemberDetailsForm(instance, memberDetails,
                        swaggerApiClientForLoggedInUser.getMember(member.getId()), governingBody, swaggerApiClientForLoggedInUser);
            }

            log.info("Updating member details: " + member.getId());
            member.setFirstName(memberDetails.getFirstName().trim());
            member.setLastName(memberDetails.getLastName().trim());
            member.setKnownAs(memberDetails.getKnownAs().trim());
            member.setGender(memberDetails.getGender());

            member.setDateOfBirth(updatedDateOfBirth);

            try {
                Integer asInteger = !Strings.isNullOrEmpty(memberDetails.getWeight()) ? Integer.parseInt(memberDetails.getWeight()) : null;
                member.setWeight(asInteger != null ? new BigDecimal(asInteger) : null);  // TODO validate
            } catch (IllegalArgumentException iae) {
                log.warn(iae);
            }

            member.setEmailAddress(memberDetails.getEmailAddress().trim());
            member.setContactNumber(memberDetails.getContactNumber().trim());
            member.setRowingPoints(!Strings.isNullOrEmpty(memberDetails.getRowingPoints()) ? memberDetails.getRowingPoints() : null);
            member.setSculling(memberDetails.getSculling());
            member.setScullingPoints(!Strings.isNullOrEmpty(memberDetails.getScullingPoints()) ? memberDetails.getScullingPoints() : null);
            member.setRegistrationNumber(memberDetails.getRegistrationNumber().trim());
            member.setEmergencyContactName(memberDetails.getEmergencyContactName().trim());
            member.setEmergencyContactNumber(memberDetails.getEmergencyContactNumber().trim());
            member.setSweepOarSide(memberDetails.getSweepOarSide());

            final boolean canChangeRole = permissionsService.canChangeRoleFor(loggedInUserService.getLoggedInMember(), swaggerApiClientForLoggedInUser.getMember(member.getId()));
            if (canChangeRole) {
                member.setRole(memberDetails.getRole());
            }

            final boolean canChangeSquads = canChangeRole;  // TODO really?
            if (canChangeSquads) {
                member.setSquads(squads);
            }

            try {
                swaggerApiClientForLoggedInUser.updateMember(member, member.getId());
            } catch (ApiException e) {
                log.warn("Invalid member exception: " + e.getResponseBody());
                result.addError(new ObjectError("memberDetails", e.getMessage()));

                return renderEditMemberDetailsForm(instance, memberDetails,
                        swaggerApiClientForLoggedInUser.getMember(member.getId()),
                        governingBody, swaggerApiClientForLoggedInUser).
                        addObject("invalidImage", false);
            }

            return viewFactory.redirectionTo(urlBuilder.memberUrl(member));
        } else {
            throw new PermissionDeniedException();
        }
    }

    @RequestMapping(value = "/member/{id}/make-inactive", method = RequestMethod.GET)
    public ModelAndView makeInactivePrompt(@PathVariable String id) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        final Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Member member = swaggerApiClientForLoggedInUser.getMember(id);
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        if (permissionsService.hasMemberPermission(loggedInMember, Permission.EDIT_MEMBER_DETAILS, member)) {
            List<Squad> squads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());

            List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, null, swaggerApiClientForLoggedInUser, instance, squads);

            return viewFactory.getViewFor("makeMemberInactivePrompt", instance).
                    addObject("member", member).
                    addObject("title", "Make member inactive - " + member.getDisplayName()).
                    addObject("navItems", navItems);
        } else {
            throw new PermissionDeniedException();
        }
    }

    @RequestMapping(value = "/member/{id}/make-inactive", method = RequestMethod.POST)
    public ModelAndView makeInactive(@PathVariable String id) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        final Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Member member = swaggerApiClientForLoggedInUser.getMember(id);

        if (permissionsService.hasMemberPermission(loggedInMember, Permission.EDIT_MEMBER_DETAILS, member)) {
            log.info("Making member inactive: " + id);
            member.setInactive(true);
            swaggerApiClientForLoggedInUser.updateMember(member, member.getId());
            return redirectToAdminScreen();
        } else {
            throw new PermissionDeniedException();
        }
    }

    @RequestMapping(value = "/member/{id}/delete", method = RequestMethod.GET)
    public ModelAndView deletePrompt(@PathVariable String id) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Member member = swaggerApiClientForLoggedInUser.getMember(id);

        if (permissionsService.hasMemberPermission(loggedInMember, Permission.EDIT_MEMBER_DETAILS, member)) {
            List<Squad> squads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());
            List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, null, swaggerApiClientForLoggedInUser, instance, squads);

            return viewFactory.getViewFor("deleteMemberPrompt", instance).
                    addObject("title", "Delete member - " + member.getDisplayName()).
                    addObject("navItems", navItems).
                    addObject("member", member);
        } else {
            throw new PermissionDeniedException();
        }
    }

    @RequestMapping(value = "/member/{id}/delete", method = RequestMethod.POST)
    public ModelAndView delete(@PathVariable String id) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Member loggedInMember = loggedInUserService.getLoggedInMember();

        Member member = swaggerApiClientForLoggedInUser.getMember(id);
        if (permissionsService.hasMemberPermission(loggedInMember, Permission.EDIT_MEMBER_DETAILS, member)) {
            swaggerApiClientForLoggedInUser.deleteMember(member.getId());
            return redirectToAdminScreen();
        } else {
            throw new PermissionDeniedException();
        }
    }

    @RequestMapping(value = "/member/{id}/make-active", method = RequestMethod.GET)
    public ModelAndView makeActivePrompt(@PathVariable String id) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
        final Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Member member = swaggerApiClientForLoggedInUser.getMember(id);

        if (permissionsService.hasMemberPermission(loggedInMember, Permission.EDIT_MEMBER_DETAILS, member)) {
            List<Squad> squads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());
            List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, null, swaggerApiClientForLoggedInUser, instance, squads);
            return viewFactory.getViewFor("makeMemberActivePrompt", instance).
                    addObject("member", member).
                    addObject("title", "Make member active - " + member.getDisplayName()).
                    addObject("navItems", navItems);

        } else {
            throw new PermissionDeniedException();
        }
    }

    @RequestMapping(value = "/member/{id}/make-active", method = RequestMethod.POST)
    public ModelAndView makeActive(@PathVariable String id) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        final Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Member member = swaggerApiClientForLoggedInUser.getMember(id);
        if (permissionsService.hasMemberPermission(loggedInMember, Permission.EDIT_MEMBER_DETAILS, member)) {
            log.info("Making member active: " + id);
            member.setInactive(false);
            swaggerApiClientForLoggedInUser.updateMember(member, member.getId());
            return redirectToAdminScreen();
        } else {
            throw new PermissionDeniedException();
        }
    }

    @RequestMapping(value = "/member/{id}/edit/profileimage", method = RequestMethod.POST)
    public ModelAndView updateMemberProfileImageSubmit(@PathVariable String id, MultipartHttpServletRequest request) throws IOException, SignedInMemberRequiredException, ApiException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        log.info("Received update member profile image request: " + id);
        final Member member = swaggerApiClientForLoggedInUser.getMember(id);
        final Member loggedInMember = loggedInUserService.getLoggedInMember();

        if (permissionsService.hasMemberPermission(loggedInMember, Permission.EDIT_MEMBER_DETAILS, member)) {
            final MultipartFile file = request.getFile("image");
            log.info("Submitting updated member: " + member);
            File imageFile = Files.createTempFile("profileimage", ".tmp").toFile();
            file.transferTo(imageFile);
            try {
                swaggerApiClientForLoggedInUser.updateProfileImage(member.getId(), imageFile);
                imageFile.delete();

            } catch (ApiException e) {
                log.warn("Invalid image file submitted");
                imageFile.delete();
                return viewFactory.redirectionTo(urlBuilder.editMemberUrl(member) + "?invalidImage=true");
            }
            return viewFactory.redirectionTo(urlBuilder.memberUrl(member));

        } else {
            throw new PermissionDeniedException();
        }
    }

    private ModelAndView renderEditMemberDetailsForm(Instance instance, MemberDetails memberDetails, Member member,
                                                     GoverningBody governingBody, DefaultApi swaggerApiClientForLoggedInUser) throws SignedInMemberRequiredException, URISyntaxException, ApiException {
        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        final boolean canChangeRole = permissionsService.canChangeRoleFor(loggedInUser, member);
        List<Squad> squads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, null, swaggerApiClientForLoggedInUser, instance, squads);

        String title = member.getFirstName() + " " + member.getLastName();  // TODO push to member class
        return viewFactory.getViewFor("editMemberDetails", instance).
                addObject("member", member).
                addObject("memberDetails", memberDetails).
                addObject("title", title).
                addObject("navItems", navItems).
                addObject("squads", squads).
                addObject("governingBody", governingBody).
                addObject("genderOptions", GENDER_OPTIONS).
                addObject("pointsOptions", governingBody.getPointsOptions()).
                addObject("rolesOptions", ROLES_OPTIONS).
                addObject("sweepOarSideOptions", SWEEP_OAR_SIDE_OPTIONS).
                addObject("yesNoOptions", YES_NO_OPTIONS).
                addObject("canChangeRole", canChangeRole).
                addObject("canChangeSquads", canChangeRole).
                addObject("memberSquads", member.getSquads());          // TODO would not be needed id member.squads would form bind
    }

    private ModelAndView renderChangePasswordForm(Instance instance, Member loggedInUser, ChangePassword changePassword) throws SignedInMemberRequiredException, URISyntaxException, ApiException {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        List<Squad> squads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, null, swaggerApiClientForLoggedInUser, instance, squads);

        return viewFactory.getViewFor("changePassword", instance).
                addObject("title", "Change password").
                addObject("navItems", navItems).
                addObject("member", loggedInUser).
                addObject("changePassword", changePassword);
    }

    private List<Squad> extractAndValidateRequestedSquads(MemberDetails memberDetails, BindingResult result, DefaultApi api) {
        final List<Squad> squads = Lists.newArrayList();
        if (memberDetails.getSquads() == null) {
            return squads;
        }

        for (MemberSquad requestedSquad : memberDetails.getSquads()) {
            log.info("Requested squad: " + requestedSquad);
            try {
                squads.add(api.getSquad(requestedSquad.getId()));  // TODO Validate instance
            } catch (ApiException e) {
                log.warn("Rejecting unknown squad: " + requestedSquad);
                result.addError(new ObjectError("memberDetails.squad", "Unknown squad"));
            }
        }
        log.info("Assigned squads: " + squads);
        return squads;
    }

    @RequestMapping(value = "/member/{id}/reset", method = RequestMethod.GET)
    public ModelAndView resetMemberPasswordPrompt(@PathVariable String id) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Member loggedInMember = loggedInUserService.getLoggedInMember();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
        final Member member = swaggerApiClientForLoggedInUser.getMember(id);

        if (permissionsService.hasMemberPermission(loggedInMember, Permission.EDIT_MEMBER_DETAILS, member)) {
            List<Squad> squads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());
            List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, null, swaggerApiClientForLoggedInUser, instance, squads);
            return viewFactory.getViewFor("memberPasswordResetPrompt", instance).
                    addObject("title", "Reset a member's password").
                    addObject("navItems", navItems).
                    addObject("member", member);
        } else {
            throw new PermissionDeniedException();
        }
    }

    @RequestMapping(value = "/member/{id}/reset", method = RequestMethod.POST)
    public ModelAndView resetMemberPassword(@PathVariable String id) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Member member = swaggerApiClientForLoggedInUser.getMember(id);

        if (permissionsService.hasMemberPermission(loggedInMember, Permission.EDIT_MEMBER_DETAILS, member)) {
            Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
            List<Squad> squads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());
            List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, null, swaggerApiClientForLoggedInUser, instance, squads);

            final String newPassword = swaggerApiClientForLoggedInUser.resetMemberPassword(instance.getId(), member.getId());

            return viewFactory.getViewFor("memberPasswordReset", instance).
                    addObject("title", "Password reset details").
                    addObject("navItems", navItems).
                    addObject("member", member).
                    addObject("password", newPassword);
        } else {
            throw new PermissionDeniedException();
        }
    }

    private ModelAndView redirectToAdminScreen() {
        return viewFactory.redirectionTo(urlBuilder.adminUrl());
    }

}
