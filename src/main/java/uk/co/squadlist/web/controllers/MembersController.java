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
import org.springframework.web.servlet.view.RedirectView;
import uk.co.squadlist.web.annotations.RequiresMemberPermission;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.exceptions.*;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.ChangePassword;
import uk.co.squadlist.web.model.forms.MemberDetails;
import uk.co.squadlist.web.model.forms.MemberSquad;
import uk.co.squadlist.web.services.PasswordGenerator;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
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

    public MembersController() {
    }

    @Autowired
    public MembersController(LoggedInUserService loggedInUserService, UrlBuilder urlBuilder,
                             ViewFactory viewFactory,
                             PasswordGenerator passwordGenerator,
                             PermissionsService permissionsService,
                             GoverningBodyFactory governingBodyFactory) {
        this.loggedInUserService = loggedInUserService;
        this.urlBuilder = urlBuilder;
        this.viewFactory = viewFactory;
        this.passwordGenerator = passwordGenerator;
        this.permissionsService = permissionsService;
        this.governingBodyFactory = governingBodyFactory;
    }

    @RequiresMemberPermission(permission = Permission.VIEW_MEMBER_DETAILS)
    @RequestMapping("/member/{id}")
    public ModelAndView member(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Member members = loggedInUserApi.getMember(id);

        final ModelAndView mv = viewFactory.getViewForLoggedInUser("memberDetails");
        mv.addObject("member", members);
        mv.addObject("title", members.getFirstName() + " " + members.getLastName());
        mv.addObject("governingBody", governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance()));
        return mv;
    }

    @RequiresPermission(permission = Permission.ADD_MEMBER)
    @RequestMapping(value = "/member/new", method = RequestMethod.GET)
    public ModelAndView newMember(@ModelAttribute("memberDetails") MemberDetails memberDetails) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        return renderNewMemberForm(loggedInUserApi);
    }

    @RequiresPermission(permission = Permission.ADD_MEMBER)
    @RequestMapping(value = "/member/new", method = RequestMethod.POST)
    public ModelAndView newMemberSubmit(@Valid @ModelAttribute("memberDetails") MemberDetails memberDetails, BindingResult result) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final List<Squad> requestedSquads = extractAndValidateRequestedSquads(memberDetails, result, loggedInUserApi);
        if (result.hasErrors()) {
            log.info("New member submission has errors: " + result.getAllErrors());
            return renderNewMemberForm(loggedInUserApi);
        }

        final String initialPassword = passwordGenerator.generateRandomPassword();

        try {
            final Member newMember = loggedInUserApi.createMember(memberDetails.getFirstName(), // TODO should be a user API client
                    memberDetails.getLastName(),
                    requestedSquads,
                    memberDetails.getEmailAddress(),
                    initialPassword,
                    null,
                    memberDetails.getRole()
            );

            return viewFactory.getViewForLoggedInUser("memberAdded").
                    addObject("member", newMember).
                    addObject("initialPassword", initialPassword);

        } catch (InvalidMemberException e) {
            log.warn("Invalid member exception: " + e.getMessage());
            result.addError(new ObjectError("memberDetails", e.getMessage()));
            return renderNewMemberForm(loggedInUserApi);
        }
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.GET)
    public ModelAndView changePassword() throws Exception {
        return renderChangePasswordForm(new ChangePassword());
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
    public ModelAndView editMember(@Valid @ModelAttribute("changePassword") ChangePassword changePassword, BindingResult result) throws Exception {
        if (result.hasErrors()) {
            return renderChangePasswordForm(changePassword);
        }

        final Member member = loggedInUserService.getLoggedInMember();
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        log.info("Requesting change password for member: " + member.getId());
        if (loggedInUserApi.changePassword(member.getId(), changePassword.getCurrentPassword(), changePassword.getNewPassword())) {
            return new ModelAndView(new RedirectView(urlBuilder.memberUrl(member)));
        } else {
            result.addError(new ObjectError("changePassword", "Change password failed"));
            return renderChangePasswordForm(changePassword);
        }
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @RequestMapping(value = "/member/{id}/edit", method = RequestMethod.GET)
    public ModelAndView updateMember(@PathVariable String id, @RequestParam(required = false) Boolean invalidImage) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Member member = loggedInUserApi.getMember(id);

        final MemberDetails memberDetails = new MemberDetails();
        memberDetails.setFirstName(member.getFirstName());
        memberDetails.setLastName(member.getLastName());
        memberDetails.setKnownAs(member.getKnownAs());
        memberDetails.setGender(member.getGender());

        memberDetails.setDateOfBirthDay(member.getDateOfBirth() != null ? new DateTime(member.getDateOfBirth()).getDayOfMonth() : null);
        memberDetails.setDateOfBirthMonth(member.getDateOfBirth() != null ? new DateTime(member.getDateOfBirth()).getMonthOfYear() : null);
        memberDetails.setDateOfBirthYear(member.getDateOfBirth() != null ? new DateTime(member.getDateOfBirth()).getYear() : null);

        memberDetails.setWeight(member.getWeight() != null ? Integer.toString(member.getWeight()) : null);
        memberDetails.setEmailAddress(member.getEmailAddress());
        memberDetails.setContactNumber(member.getContactNumber());
        memberDetails.setRegistrationNumber(member.getRegistrationNumber());
        memberDetails.setRowingPoints(member.getRowingPoints());
        memberDetails.setSculling(member.getSculling());
        memberDetails.setScullingPoints(member.getScullingPoints());
        memberDetails.setSweepOarSide(member.getSweepOarSide());

        memberDetails.setPostcode(member.getAddress() != null ? member.getAddress().get("postcode") : null);

        List<MemberSquad> memberSquads = Lists.newArrayList();
        for (Squad squad : member.getSquads()) {
            memberSquads.add(new MemberSquad(squad.getId()));
        }

        memberDetails.setSquads(memberSquads);
        memberDetails.setEmergencyContactName(member.getEmergencyContactName());
        memberDetails.setEmergencyContactNumber(member.getEmergencyContactNumber());
        memberDetails.setRole(member.getRole());
        memberDetails.setProfileImage(member.getProfileImage());

        GoverningBody governingBody = governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance());

        return renderEditMemberDetailsForm(memberDetails, member.getId(), member.getFirstName() + " " + member.getLastName(), member, loggedInUserApi, governingBody).
                addObject("invalidImage", invalidImage);
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @RequestMapping(value = "/member/{id}/edit", method = RequestMethod.POST)
    public ModelAndView updateMemberSubmit(@PathVariable String id, @Valid @ModelAttribute("member") MemberDetails memberDetails, BindingResult result) throws Exception {
        log.info("Received edit member request: " + memberDetails);
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Member member = loggedInUserApi.getMember(id);
        final List<Squad> squads = extractAndValidateRequestedSquads(memberDetails, result, loggedInUserApi);
        final GoverningBody governingBody = governingBodyFactory.getGoverningBody(loggedInUserApi.getInstance());

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

        Date updatedDateOfBirth = null;
        if (memberDetails.getDateOfBirthDay() != null &&
                memberDetails.getDateOfBirthMonth() != null &&
                memberDetails.getDateOfBirthYear() != null) {
            try {
                updatedDateOfBirth = new DateTime(memberDetails.getDateOfBirthYear(),
                        memberDetails.getDateOfBirthMonth(), memberDetails.getDateOfBirthDay(), 0, 0, 0,
                        DateTimeZone.UTC).toDate();
                member.setDateOfBirth(updatedDateOfBirth);
            } catch (IllegalFieldValueException e) {
                result.addError(new ObjectError("member.dateOfBirthYear", "Invalid date"));
            }
        }

        if (result.hasErrors()) {
            return renderEditMemberDetailsForm(memberDetails, member.getId(), member.getFirstName() + " " + member.getLastName(), member, loggedInUserApi, governingBody);
        }

        log.info("Updating member details: " + member.getId());
        member.setFirstName(memberDetails.getFirstName());
        member.setLastName(memberDetails.getLastName());
        member.setKnownAs(memberDetails.getKnownAs());
        member.setGender(memberDetails.getGender());

        member.setDateOfBirth(updatedDateOfBirth);

        try {
            member.setWeight(!Strings.isNullOrEmpty(memberDetails.getWeight()) ? Integer.parseInt(memberDetails.getWeight()) : null);  // TODO validate
        } catch (IllegalArgumentException iae) {
            log.warn(iae);
        }

        member.setEmailAddress(memberDetails.getEmailAddress());
        member.setContactNumber(memberDetails.getContactNumber());
        member.setRowingPoints(!Strings.isNullOrEmpty(memberDetails.getRowingPoints()) ? memberDetails.getRowingPoints() : null);
        member.setSculling(memberDetails.getSculling());
        member.setScullingPoints(!Strings.isNullOrEmpty(memberDetails.getScullingPoints()) ? memberDetails.getScullingPoints() : null);
        member.setRegistrationNumber(memberDetails.getRegistrationNumber());
        member.setEmergencyContactName(memberDetails.getEmergencyContactName());
        member.setEmergencyContactNumber(memberDetails.getEmergencyContactNumber());
        member.setSweepOarSide(memberDetails.getSweepOarSide());

        final boolean canChangeRole = permissionsService.canChangeRoleFor(loggedInUserService.getLoggedInMember(), member);
        if (canChangeRole) {
            member.setRole(memberDetails.getRole());
        }

        final boolean canChangeSquads = canChangeRole;  // TODO really?
        if (canChangeSquads) {
            member.setSquads(squads);
        }

        member.getAddress().put("postcode", memberDetails.getPostcode());

        log.info("Submitting updated member: " + member);
        loggedInUserApi.updateMemberDetails(member);
        return new ModelAndView(new RedirectView(urlBuilder.memberUrl(member)));
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @RequestMapping(value = "/member/{id}/make-inactive", method = RequestMethod.GET)
    public ModelAndView makeInactivePrompt(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Member member = loggedInUserApi.getMember(id);
        return viewFactory.getViewForLoggedInUser("makeMemberInactivePrompt").
                addObject("member", member).
                addObject("title", "Make member inactive - " + member.getDisplayName());
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @RequestMapping(value = "/member/{id}/make-inactive", method = RequestMethod.POST)
    public ModelAndView makeInactive(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        log.info("Making member inactive: " + id);
        final Member member = loggedInUserApi.getMember(id);
        member.setInactive(true);
        loggedInUserApi.updateMemberDetails(member);

        return redirectToAdminScreen();
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @RequestMapping(value = "/member/{id}/delete", method = RequestMethod.GET)
    public ModelAndView deletePrompt(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Member member = loggedInUserApi.getMember(id);
        return viewFactory.getViewForLoggedInUser("deleteMemberPrompt").
                addObject("member", member).
                addObject("title", "Delete member - " + member.getDisplayName());
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @RequestMapping(value = "/member/{id}/delete", method = RequestMethod.POST)
    public ModelAndView delete(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Member member = loggedInUserApi.getMember(id);

        loggedInUserApi.deleteMember(member);
        return redirectToAdminScreen();
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @RequestMapping(value = "/member/{id}/make-active", method = RequestMethod.GET)
    public ModelAndView makeActivePrompt(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Member member = loggedInUserApi.getMember(id);

        return viewFactory.getViewForLoggedInUser("makeMemberActivePrompt").
                addObject("member", member).
                addObject("title", "Make member active - " + member.getDisplayName());
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @RequestMapping(value = "/member/{id}/make-active", method = RequestMethod.POST)
    public ModelAndView makeActive(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        log.info("Making member active: " + id);
        final Member member = loggedInUserApi.getMember(id);
        member.setInactive(false);
        loggedInUserApi.updateMemberDetails(member);
        return redirectToAdminScreen();
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @RequestMapping(value = "/member/{id}/edit/profileimage", method = RequestMethod.POST)
    public ModelAndView updateMemberProfileImageSubmit(@PathVariable String id, MultipartHttpServletRequest request) throws UnknownMemberException, IOException, SignedInMemberRequiredException {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        log.info("Received update member profile image request: " + id);
        final Member member = loggedInUserApi.getMember(id);

        final MultipartFile file = request.getFile("image");

        log.info("Submitting updated member: " + member);
        try {
            loggedInUserApi.updateMemberProfileImage(member, file.getBytes());
        } catch (InvalidImageException e) {
            log.warn("Invalid image file submitted");
            return new ModelAndView(new RedirectView(urlBuilder.editMemberUrl(member) + "?invalidImage=true"));
        }
        return new ModelAndView(new RedirectView(urlBuilder.memberUrl(member)));
    }

    private ModelAndView renderNewMemberForm(InstanceSpecificApiClient api) throws SignedInMemberRequiredException, UnknownInstanceException {
        final ModelAndView mv = viewFactory.getViewForLoggedInUser("newMember");
        mv.addObject("squads", api.getSquads());
        mv.addObject("title", "Adding a new member");
        mv.addObject("rolesOptions", ROLES_OPTIONS);
        return mv;
    }

    private ModelAndView renderEditMemberDetailsForm(MemberDetails memberDetails, String memberId, String title, Member member, InstanceSpecificApiClient api, GoverningBody governingBody) throws SignedInMemberRequiredException, UnknownInstanceException {
        final ModelAndView mv = viewFactory.getViewForLoggedInUser("editMemberDetails");
        mv.addObject("member", memberDetails);
        mv.addObject("memberId", memberId);
        mv.addObject("title", title);
        mv.addObject("squads", api.getSquads());
        mv.addObject("governingBody", governingBody);

        mv.addObject("genderOptions", GENDER_OPTIONS);
        mv.addObject("pointsOptions", governingBody.getPointsOptions());
        mv.addObject("rolesOptions", ROLES_OPTIONS);
        mv.addObject("sweepOarSideOptions", SWEEP_OAR_SIDE_OPTIONS);
        mv.addObject("yesNoOptions", YES_NO_OPTIONS);

        final boolean canChangeRole = permissionsService.canChangeRoleFor(loggedInUserService.getLoggedInMember(), member);
        mv.addObject("canChangeRole", canChangeRole);
        mv.addObject("canChangeSquads", canChangeRole);

        mv.addObject("memberSquads", member.getSquads());  // TODO would not be needed id member.squads would form bind
        return mv;
    }

    private ModelAndView renderChangePasswordForm(ChangePassword changePassword) throws SignedInMemberRequiredException, UnknownInstanceException {
        final ModelAndView mv = viewFactory.getViewForLoggedInUser("changePassword");
        mv.addObject("member", loggedInUserService.getLoggedInMember());
        mv.addObject("changePassword", changePassword);
        mv.addObject("title", "Change password");
        return mv;
    }

    private List<Squad> extractAndValidateRequestedSquads(MemberDetails memberDetails, BindingResult result, InstanceSpecificApiClient squadlistApi) {
        final List<Squad> squads = Lists.newArrayList();
        if (memberDetails.getSquads() == null) {
            return squads;
        }

        for (MemberSquad requestedSquad : memberDetails.getSquads()) {
            log.info("Requested squad: " + requestedSquad);
            try {
                squads.add(squadlistApi.getSquad(requestedSquad.getId()));  // TODO Validate instance
            } catch (UnknownSquadException e) {
                log.warn("Rejecting unknown squad: " + requestedSquad);
                result.addError(new ObjectError("memberDetails.squad", "Unknown squad"));
            }
        }
        log.info("Assigned squads: " + squads);
        return squads;
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @RequestMapping(value = "/member/{id}/reset", method = RequestMethod.GET)
    public ModelAndView resetMemberPasswordPrompt(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final Member member = loggedInUserApi.getMember(id);
        return viewFactory.getViewForLoggedInUser("memberPasswordResetPrompt").addObject("member", member);
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @RequestMapping(value = "/member/{id}/reset", method = RequestMethod.POST)
    public ModelAndView resetMemberPassword(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Member member = loggedInUserApi.getMember(id);

        final String newPassword = loggedInUserApi.resetMemberPassword(member);

        return viewFactory.getViewForLoggedInUser("memberPasswordReset").
                addObject("member", member).
                addObject("password", newPassword);
    }

    private ModelAndView redirectToAdminScreen() {
        return new ModelAndView(new RedirectView(urlBuilder.adminUrl()));
    }

}
