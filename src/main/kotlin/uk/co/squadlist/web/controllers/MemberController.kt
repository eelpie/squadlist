package uk.co.squadlist.web.controllers

import com.google.common.base.Strings
import com.google.common.collect.Lists
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.IllegalFieldValueException
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.squadlist.web.annotations.RequiresMemberPermission
import uk.co.squadlist.web.annotations.RequiresPermission
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.api.SquadlistApiFactory
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.GoverningBodyFactory
import uk.co.squadlist.web.exceptions.InvalidImageException
import uk.co.squadlist.web.exceptions.InvalidMemberException
import uk.co.squadlist.web.exceptions.UnknownSquadException
import uk.co.squadlist.web.model.Member
import uk.co.squadlist.web.model.Squad
import uk.co.squadlist.web.model.forms.MemberDetails
import uk.co.squadlist.web.model.forms.MemberSquad
import uk.co.squadlist.web.services.PasswordGenerator
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.ViewFactory
import java.util.*
import javax.validation.Valid

@Controller
public class MembersController(val instanceSpecificApiClient: InstanceSpecificApiClient, val loggedInUserService: LoggedInUserService, val urlBuilder: UrlBuilder,
                               val viewFactory: ViewFactory,
                               val passwordGenerator: PasswordGenerator,
                               val permissionsService: PermissionsService,
                               val governingBodyFactory: GoverningBodyFactory,
                               val squadlistApiFactory: SquadlistApiFactory) {

    private val log = Logger.getLogger(MembersController::class.java)

    private val GENDER_OPTIONS = listOf("female", "male")
    private val ROLES_OPTIONS = listOf("Rower", "Rep", "Coach", "Cox", "Non rowing")
    private val SWEEP_OAR_SIDE_OPTIONS = listOf("Bow", "Stroke", "Bow/Stroke", "Stroke/Bow")
    private val YES_NO_OPTIONS = listOf("Y", "N")

    private val squadlistApi = squadlistApiFactory.createClient()

    @RequiresMemberPermission(permission = Permission.VIEW_MEMBER_DETAILS)
    @GetMapping("/member/{id}")
    fun member(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val members = loggedInUserApi.getMember(id)

        val mv = viewFactory.getViewForLoggedInUser("memberDetails")
        mv.addObject("member", members)
        mv.addObject("title", members.firstName + " " + members.lastName)
        mv.addObject("governingBody", governingBodyFactory.governingBody)
        return mv
    }

    @RequiresPermission(permission = Permission.ADD_MEMBER)
    @GetMapping("/member/new")
    fun newMember(@ModelAttribute("memberDetails") memberDetails: MemberDetails): ModelAndView {
        return renderNewMemberForm()
    }

    @RequiresPermission(permission = Permission.ADD_MEMBER)
    @PostMapping("/member/new")
    fun newMemberSubmit(@Valid @ModelAttribute("memberDetails") memberDetails: MemberDetails, result: BindingResult): ModelAndView {
        val requestedSquads = extractAndValidateRequestedSquads(memberDetails, result)

        if (result.hasErrors()) {
            log.info("New member submission has errors: " + result.allErrors)
            return renderNewMemberForm()
        }

        val initialPassword = passwordGenerator.generateRandomPassword(10)

        try {
            val newMember = instanceSpecificApiClient.createMember(memberDetails.firstName,
                    memberDetails.lastName,
                    requestedSquads,
                    memberDetails.emailAddress,
                    initialPassword,
                    null,
                    memberDetails.role
            )

            return viewFactory.getViewForLoggedInUser("memberAdded").addObject("member", newMember).addObject("initialPassword", initialPassword)

        } catch (e: InvalidMemberException) {
            log.warn("Invalid member exception: " + e.message)
            result.addError(ObjectError("memberDetails", e.message))
            return renderNewMemberForm()
        }

    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @GetMapping("/member/{id}/edit")
    fun updateMember(@PathVariable id: String, @RequestParam(required = false) invalidImage: Boolean?): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val member = loggedInUserApi.getMember(id)

        val memberDetails = MemberDetails()
        memberDetails.firstName = member.firstName
        memberDetails.lastName = member.lastName
        memberDetails.knownAs = member.knownAs
        memberDetails.gender = member.gender

        memberDetails.dateOfBirthDay = if (member.dateOfBirth != null) DateTime(member.dateOfBirth).dayOfMonth else null
        memberDetails.dateOfBirthMonth = if (member.dateOfBirth != null) DateTime(member.dateOfBirth).monthOfYear else null
        memberDetails.dateOfBirthYear = if (member.dateOfBirth != null) DateTime(member.dateOfBirth).year else null

        memberDetails.weight = if (member.weight != null) Integer.toString(member.weight!!) else null
        memberDetails.emailAddress = member.emailAddress
        memberDetails.contactNumber = member.contactNumber
        memberDetails.registrationNumber = member.registrationNumber
        memberDetails.rowingPoints = member.rowingPoints
        memberDetails.sculling = member.sculling
        memberDetails.scullingPoints = member.scullingPoints
        memberDetails.sweepOarSide = member.sweepOarSide

        memberDetails.postcode = if (member.address != null) member.address["postcode"] else null

        val memberSquads = Lists.newArrayList<MemberSquad>()
        for (squad in member.squads) {
            memberSquads.add(MemberSquad(squad.id))
        }

        memberDetails.squads = memberSquads
        memberDetails.emergencyContactName = member.emergencyContactName
        memberDetails.emergencyContactNumber = member.emergencyContactNumber
        memberDetails.role = member.role
        memberDetails.profileImage = member.profileImage

        val mv = renderEditMemberDetailsForm(memberDetails, member.id, member.firstName + " " + member.lastName, member)
        mv.addObject("invalidImage", invalidImage)
        return mv
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @PostMapping("/member/{id}/edit")
    fun updateMemberSubmit(@PathVariable id: String, @Valid @ModelAttribute("member") memberDetails: MemberDetails, result: BindingResult): ModelAndView {
        log.info("Received edit member request: $memberDetails")
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val member = loggedInUserApi.getMember(id)

        val squads = extractAndValidateRequestedSquads(memberDetails, result)
        if (!Strings.isNullOrEmpty(memberDetails.scullingPoints)) {
            if (!governingBodyFactory.governingBody.pointsOptions.contains(memberDetails.scullingPoints)) {
                result.addError(ObjectError("member.scullingPoints", "Invalid points option"))
            }
        }
        if (!Strings.isNullOrEmpty(memberDetails.rowingPoints)) {
            if (!governingBodyFactory.governingBody.pointsOptions.contains(memberDetails.rowingPoints)) {
                result.addError(ObjectError("member.rowingPoints", "Invalid points option"))
            }
        }

        var updatedDateOfBirth: Date? = null
        if (memberDetails.dateOfBirthDay != null &&
                memberDetails.dateOfBirthMonth != null &&
                memberDetails.dateOfBirthYear != null) {
            try {
                updatedDateOfBirth = DateTime(memberDetails.dateOfBirthYear!!,
                        memberDetails.dateOfBirthMonth!!, memberDetails.dateOfBirthDay!!, 0, 0, 0,
                        DateTimeZone.UTC).toDate()
                member.dateOfBirth = updatedDateOfBirth
            } catch (e: IllegalFieldValueException) {
                result.addError(ObjectError("member.dateOfBirthYear", "Invalid date"))
            }

        }

        if (result.hasErrors()) {
            return renderEditMemberDetailsForm(memberDetails, member.id, member.firstName + " " + member.lastName, member)
        }

        log.info("Updating member details: " + member.id)
        member.firstName = memberDetails.firstName
        member.lastName = memberDetails.lastName
        member.knownAs = memberDetails.knownAs
        member.gender = memberDetails.gender

        member.dateOfBirth = updatedDateOfBirth

        try {
            member.weight = if (!Strings.isNullOrEmpty(memberDetails.weight)) Integer.parseInt(memberDetails.weight) else null  // TODO validate
        } catch (iae: IllegalArgumentException) {
            log.warn(iae)
        }

        member.emailAddress = memberDetails.emailAddress
        member.contactNumber = memberDetails.contactNumber
        member.rowingPoints = if (!Strings.isNullOrEmpty(memberDetails.rowingPoints)) memberDetails.rowingPoints else null
        member.sculling = memberDetails.sculling
        member.scullingPoints = if (!Strings.isNullOrEmpty(memberDetails.scullingPoints)) memberDetails.scullingPoints else null
        member.registrationNumber = memberDetails.registrationNumber
        member.emergencyContactName = memberDetails.emergencyContactName
        member.emergencyContactNumber = memberDetails.emergencyContactNumber
        member.sweepOarSide = memberDetails.sweepOarSide

        val canChangeRole = permissionsService.canChangeRoleFor(loggedInUserService.loggedInMember, member)
        if (canChangeRole) {
            member.role = memberDetails.role
        }

        if (canChangeRole) {
            member.squads = squads
        }

        member.address["postcode"] = memberDetails.postcode

        log.info("Submitting updated member: $member")
        loggedInUserApi.updateMemberDetails(member)
        return ModelAndView(RedirectView(urlBuilder.memberUrl(member)))
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @GetMapping("/member/{id}/make-inactive")
    fun makeInactivePrompt(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val member = loggedInUserApi.getMember(id)
        return viewFactory.getViewForLoggedInUser("makeMemberInactivePrompt").addObject("member", member).addObject("title", "Make member inactive - " + member.displayName)
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @PostMapping("/member/{id}/make-inactive")
    fun makeInactive(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        log.info("Making member inactive: $id")
        val member = loggedInUserApi.getMember(id)
        member.inactive = true
        loggedInUserApi.updateMemberDetails(member)

        return redirectToAdminScreen()
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @GetMapping("/member/{id}/delete")
    fun deletePrompt(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val member = loggedInUserApi.getMember(id)
        return viewFactory.getViewForLoggedInUser("deleteMemberPrompt").addObject("member", member).addObject("title", "Delete member - " + member.displayName)
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @PostMapping("/member/{id}/delete")
    fun delete(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val member = loggedInUserApi.getMember(id)

        loggedInUserApi.deleteMember(member)
        return redirectToAdminScreen()
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @GetMapping("/member/{id}/make-active")
    fun makeActivePrompt(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val member = loggedInUserApi.getMember(id)

        return viewFactory.getViewForLoggedInUser("makeMemberActivePrompt").addObject("member", member).addObject("title", "Make member active - " + member.displayName)
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @PostMapping("/member/{id}/make-active")
    fun makeActive(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        log.info("Making member active: $id")
        val member = loggedInUserApi.getMember(id)
        member.inactive = false
        loggedInUserApi.updateMemberDetails(member)
        return redirectToAdminScreen()
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @PostMapping("/member/{id}/edit/profileimage")
    fun updateMemberProfileImageSubmit(@PathVariable id: String, request: MultipartHttpServletRequest): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        log.info("Received update member profile image request: $id")
        val member = loggedInUserApi.getMember(id)

        val file = request.getFile("image")

        log.info("Submitting updated member: $member")
        try {
            loggedInUserApi.updateMemberProfileImage(member, file!!.bytes)
        } catch (e: InvalidImageException) {
            log.warn("Invalid image file submitted")
            return ModelAndView(RedirectView(urlBuilder.editMemberUrl(member) + "?invalidImage=true"))
        }

        return ModelAndView(RedirectView(urlBuilder.memberUrl(member)))
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @GetMapping("/member/{id}/reset")
    fun resetMemberPasswordPrompt(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)
        val member = loggedInUserApi.getMember(id)
        return viewFactory.getViewForLoggedInUser("memberPasswordResetPrompt").addObject("member", member)
    }

    @RequiresMemberPermission(permission = Permission.EDIT_MEMBER_DETAILS)
    @PostMapping("/member/{id}/reset")
    fun resetMemberPassword(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val member = loggedInUserApi.getMember(id)

        val newPassword = instanceSpecificApiClient.resetMemberPassword(member)

        return viewFactory.getViewForLoggedInUser("memberPasswordReset").addObject("member", member).addObject("password", newPassword)
    }

    private fun redirectToAdminScreen(): ModelAndView {
        return ModelAndView(RedirectView(urlBuilder.adminUrl()))
    }

    private fun renderNewMemberForm(): ModelAndView {
        val mv = viewFactory.getViewForLoggedInUser("newMember")
        mv.addObject("squads", instanceSpecificApiClient.squads)
        mv.addObject("title", "Adding a new member")
        mv.addObject("rolesOptions", ROLES_OPTIONS)
        return mv
    }

    private fun renderEditMemberDetailsForm(memberDetails: MemberDetails, memberId: String, title: String, member: Member): ModelAndView {
        val mv = viewFactory.getViewForLoggedInUser("editMemberDetails")
        mv.addObject("member", memberDetails)
        mv.addObject("memberId", memberId)
        mv.addObject("title", title)
        mv.addObject("squads", instanceSpecificApiClient.squads)
        mv.addObject("governingBody", governingBodyFactory.governingBody)

        mv.addObject("genderOptions", GENDER_OPTIONS)
        mv.addObject("pointsOptions", governingBodyFactory.governingBody.pointsOptions)
        mv.addObject("rolesOptions", ROLES_OPTIONS)
        mv.addObject("sweepOarSideOptions", SWEEP_OAR_SIDE_OPTIONS)
        mv.addObject("yesNoOptions", YES_NO_OPTIONS)

        val canChangeRole = permissionsService.canChangeRoleFor(loggedInUserService.loggedInMember, member)
        mv.addObject("canChangeRole", canChangeRole)
        mv.addObject("canChangeSquads", canChangeRole)

        mv.addObject("memberSquads", member.squads)  // TODO would not be needed id member.squads would form bind
        return mv
    }

    private fun extractAndValidateRequestedSquads(memberDetails: MemberDetails, result: BindingResult): List<Squad> {
        val squads = Lists.newArrayList<Squad>()
        if (memberDetails.squads == null) {
            return squads
        }

        for (requestedSquad in memberDetails.squads) {
            log.info("Requested squad: $requestedSquad")
            try {
                squads.add(squadlistApi.getSquad(requestedSquad.id))  // TODO Validate instance
            } catch (e: UnknownSquadException) {
                log.warn("Rejecting unknown squad: $requestedSquad")
                result.addError(ObjectError("memberDetails.squad", "Unknown squad"))
            }

        }
        log.info("Assigned squads: $squads")
        return squads
    }

}
