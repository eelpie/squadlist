package uk.co.squadlist.web.controllers

import com.google.common.base.Splitter
import com.google.common.collect.Lists
import org.apache.logging.log4j.LogManager
import org.joda.time.DateTimeZone
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.ApiException
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.model.swagger.Member
import uk.co.squadlist.model.swagger.Squad
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.GoverningBodyFactory
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.exceptions.PermissionDeniedException
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException
import uk.co.squadlist.web.model.forms.InstanceDetails
import uk.co.squadlist.web.model.forms.MemberDetails
import uk.co.squadlist.web.services.PasswordGenerator
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.services.filters.ActiveMemberFilter
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.*
import java.net.URISyntaxException
import java.util.*
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@Controller
class AdminController @Autowired constructor(private val viewFactory: ViewFactory,
                                             private val activeMemberFilter: ActiveMemberFilter, private val csvOutputRenderer: CsvOutputRenderer,
                                             private val urlBuilder: UrlBuilder,
                                             private val governingBodyFactory: GoverningBodyFactory,
                                             private val permissionsService: PermissionsService,
                                             private val navItemsBuilder: NavItemsBuilder,
                                             private val textHelper: TextHelper,
                                             private val displayMemberFactory: DisplayMemberFactory,
                                             private val passwordGenerator: PasswordGenerator,
                                             loggedInUserService: LoggedInUserService,
                                             instanceConfig: InstanceConfig): WithSignedInUser(instanceConfig, loggedInUserService, permissionsService) {

    private val log = LogManager.getLogger(AdminController::class.java)

    private val MEMBER_ORDERINGS = Lists.newArrayList("firstName", "lastName")
    private val GOVERNING_BODIES = Lists.newArrayList("british-rowing", "rowing-ireland")
    private val COMMA_SPLITTER = Splitter.on(",")

    @GetMapping("/admin")
    fun member(): ModelAndView {
        val renderAdminPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
            val members = swaggerApiClientForLoggedInUser.instancesInstanceMembersGet(instance.id)
            val activeDisplayMembers = displayMemberFactory.toDisplayMembers(activeMemberFilter.extractActive(members), loggedInMember)
            val inactiveDisplayMembers = displayMemberFactory.toDisplayMembers(activeMemberFilter.extractInactive(members), loggedInMember)
            val adminUsers = displayMemberFactory.toDisplayMembers(extractAdminUsersFrom(members), loggedInMember)
            val navItems = navItemsBuilder.navItemsFor(loggedInMember, "admin", swaggerApiClientForLoggedInUser, instance, squads)

            viewFactory.getViewFor("admin", instance)
                    .addObject("squads", squads)
                    .addObject("availabilityOptions", swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsGet(instance.id))
                    .addObject("title", textHelper.text("admin"))
                    .addObject("navItems", navItems)
                    .addObject("activeMembers", activeDisplayMembers)
                    .addObject("inactiveMembers", inactiveDisplayMembers)
                    .addObject("admins", adminUsers)
                    .addObject("governingBody", governingBodyFactory.getGoverningBody(instance))
                    .addObject("boats", Lists.newArrayList<Any>())
                    .addObject("statistics", swaggerApiClientForLoggedInUser.instancesInstanceStatisticsGet(instance.id))
        }
        return withSignedInMemberWhoCanViewAdminScreen(renderAdminPage)
    }

    @RequestMapping(value = ["/admin/instance"], method = [RequestMethod.GET])
    fun instance(): ModelAndView {
        val renderEditInstancePage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val instanceDetails = InstanceDetails()
            instanceDetails.memberOrdering = instance.memberOrdering
            instanceDetails.governingBody = instance.governingBody
            renderEditInstanceDetailsForm(instanceDetails, instance, loggedInMember, swaggerApiClientForLoggedInUser)
        }
        return withSignedInMemberWhoCanViewAdminScreen(renderEditInstancePage)
    }

    @PostMapping("/admin/instance")
    fun instanceSubmit(@Valid @ModelAttribute("instanceDetails") instanceDetails: InstanceDetails?, result: BindingResult): ModelAndView {
        val handleEditInstancePage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            if (result.hasErrors()) {
                renderEditInstanceDetailsForm(instanceDetails, instance, loggedInMember, swaggerApiClientForLoggedInUser)
            }
            instance.memberOrdering = instanceDetails!!.memberOrdering // TODO validate
            instance.governingBody = instanceDetails.governingBody // TODO validate
            swaggerApiClientForLoggedInUser.updateInstance(instance, instance.id)
            redirectToAdminScreen()
        }

        return withSignedInMemberWhoCanViewAdminScreen(handleEditInstancePage)
    }

    @GetMapping("/admin/admins")
    fun editAdminsPrompt(): ModelAndView {
        val renderEditAdminsPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
            val adminMembers: MutableList<Member> = Lists.newArrayList()
            val availableMembers: MutableList<Member> = Lists.newArrayList()
            for (member in swaggerApiClientForLoggedInUser.instancesInstanceMembersGet(instance.id)) {
                if (member.isAdmin) {
                    adminMembers.add(member)    // TODO use a filter
                } else {
                    availableMembers.add(member)
                }
            }
            val navItems = navItemsBuilder.navItemsFor(loggedInMember, "admin", swaggerApiClientForLoggedInUser, instance, squads)
            viewFactory.getViewFor("editAdmins", instance)
                    .addObject("title", textHelper.text("edit.admins"))
                    .addObject("navItems", navItems)
                    .addObject("admins", displayMemberFactory.toDisplayMembers(adminMembers, loggedInMember))
                    .addObject("availableMembers", displayMemberFactory.toDisplayMembers(availableMembers, loggedInMember))

        }
        return withSignedInMemberWhoCanViewAdminScreen(renderEditAdminsPage)
    }

    @PostMapping("/admin/admins")
    fun editAdmins(@RequestParam admins: String): ModelAndView {
        val handleEditAdminsPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            log.info("Setting admins request: $admins")
            val updatedAdmins: List<String> = Lists.newArrayList(COMMA_SPLITTER.split(admins).iterator())
            log.info("Setting admins to: $updatedAdmins")
            swaggerApiClientForLoggedInUser.setInstanceAdmins(updatedAdmins, instance.id)
            redirectToAdminScreen()
        }
        return withSignedInMemberWhoCanViewAdminScreen(handleEditAdminsPage)
    }

    @GetMapping("/admin/export/members.csv")
    fun membersCSV(response: HttpServletResponse?) {
        val renderMembersCsv = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val rows: MutableList<List<String>> = Lists.newArrayList()
            for (member in swaggerApiClientForLoggedInUser.instancesInstanceMembersGet(instance.id)) {
                val dateFormatter = DateFormatter(DateTimeZone.forID(instance.timeZone))
                rows.add(Arrays.asList(member.firstName,
                        member.lastName,
                        member.knownAs,
                        member.emailAddress,
                        member.gender,
                        if (member.dateOfBirth != null) dateFormatter.dayMonthYear(member.dateOfBirth) else "",
                        member.emergencyContactName,
                        member.emergencyContactNumber,
                        if (member.weight != null) member.weight.toString() else "",
                        member.sweepOarSide,
                        member.sculling,
                        member.registrationNumber,
                        member.rowingPoints,
                        member.scullingPoints,
                        member.role
                ))
            }
            csvOutputRenderer.renderCsvResponse(response, Lists.newArrayList("First name", "Last name", "Known as", "Email",
                    "Gender", "Date of birth", "Emergency contact name", "Emergency contact number",
                    "Weight", "Sweep oar side", "Sculling", "Registration number", "Rowing points", "Sculling points", "Role"), rows)
            ModelAndView()  // TODO questionable
        }
        withSignedInMemberWhoCanViewAdminScreen(renderMembersCsv)
    }

    @GetMapping("/member/new")
    fun newMember(@ModelAttribute("memberDetails") memberDetails: MemberDetails): ModelAndView {
        val renderNewMemberPrompt = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            if (permissionsService.hasPermission(loggedInMember, Permission.ADD_MEMBER)) {
                renderNewMemberForm(loggedInMember, swaggerApiClientForLoggedInUser, instance)
            } else {
                throw PermissionDeniedException()
            }
        }
        return withSignedInMember(renderNewMemberPrompt)
    }

    @PostMapping("/member/new")
    fun newMemberSubmit(@Valid @ModelAttribute("memberDetails") memberDetails: MemberDetails?, result: BindingResult): ModelAndView {
        val handleNewMemberSubmit = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            if (permissionsService.hasPermission(loggedInMember, Permission.ADD_MEMBER)) {
                val requestedSquads = extractAndValidateRequestedSquads(memberDetails!!, result, swaggerApiClientForLoggedInUser)
                if (result.hasErrors()) {
                    log.info("New member submission has errors: " + result.allErrors)
                    renderNewMemberForm(loggedInMember, swaggerApiClientForLoggedInUser, instance)

                } else {
                    val initialPassword = passwordGenerator.generateRandomPassword()
                    try {
                        val newMember = Member().firstName(
                            memberDetails.firstName.trim { it <= ' ' })
                            .lastName(memberDetails.lastName.trim { it <= ' ' })
                            .squads(requestedSquads).emailAddress(
                                memberDetails.emailAddress.trim { it <= ' ' }).password(initialPassword)
                            .role(memberDetails.role)
                        val createdMember =
                            swaggerApiClientForLoggedInUser.instancesInstanceMembersPost(newMember, instance.id)
                        val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
                        val navItems =
                            navItemsBuilder.navItemsFor(
                                loggedInMember,
                                null,
                                swaggerApiClientForLoggedInUser,
                                instance,
                                squads
                            )
                        viewFactory.getViewFor("memberAdded", instance).addObject("title", "Member added")
                            .addObject("navItems", navItems).addObject("member", createdMember)
                            .addObject("initialPassword", initialPassword)
                    } catch (e: ApiException) {
                        log.warn("Invalid member exception: " + e.responseBody)
                        result.addError(ObjectError("memberDetails", e.message))
                        renderNewMemberForm(loggedInMember, swaggerApiClientForLoggedInUser, instance)
                    }
                }

            } else {
                throw PermissionDeniedException()
            }
        }

        return withSignedInMember(handleNewMemberSubmit)
    }

    private fun extractAndValidateRequestedSquads(memberDetails: MemberDetails, result: BindingResult, api: DefaultApi): List<Squad> { // TODO duplication
        val squads: MutableList<Squad> = Lists.newArrayList()
        if (memberDetails.squads == null) {
            return squads
        }
        for (requestedSquad in memberDetails.squads) {
            log.info("Requested squad: $requestedSquad")
            try {
                squads.add(api.getSquad(requestedSquad.id)) // TODO Validate instance
            } catch (e: ApiException) {
                log.warn("Rejecting unknown squad: $requestedSquad")
                result.addError(ObjectError("memberDetails.squad", "Unknown squad"))
            }
        }
        log.info("Assigned squads: $squads")
        return squads
    }

    private fun renderNewMemberForm(loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi, instance: Instance): ModelAndView {
        val ROLES_OPTIONS: List<String> = Lists.newArrayList("Rower", "Rep", "Coach", "Cox", "Non rowing")  // TODO duplication
        val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
        val navItems = navItemsBuilder.navItemsFor(loggedInMember, null, swaggerApiClientForLoggedInUser, instance, squads)
        return viewFactory.getViewFor("newMember", instance).addObject("squads", squads)
            .addObject("title", "Adding a new member").addObject("navItems", navItems)
            .addObject("rolesOptions", ROLES_OPTIONS)
    }

    private fun extractAdminUsersFrom(members: List<Member>): List<Member> {
        val admins: MutableList<Member> = Lists.newArrayList()
        for (member in members) {
            if (member.isAdmin) {
                admins.add(member)
            }
        }
        return admins
    }

    private fun redirectToAdminScreen(): ModelAndView {
        return viewFactory.redirectionTo(urlBuilder.adminUrl())
    }

    @Throws(SignedInMemberRequiredException::class, URISyntaxException::class, ApiException::class)
    private fun renderEditInstanceDetailsForm(instanceDetails: InstanceDetails?, instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi): ModelAndView {
        val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
        val navItems = navItemsBuilder.navItemsFor(loggedInMember, "admin", swaggerApiClientForLoggedInUser, instance, squads)
        return viewFactory.getViewFor("editInstance", instance).addObject("title", textHelper.text("edit.instance.settings")).addObject("navItems", navItems).addObject("instanceDetails", instanceDetails).addObject("memberOrderings", MEMBER_ORDERINGS).addObject("governingBodies", GOVERNING_BODIES)
    }

}