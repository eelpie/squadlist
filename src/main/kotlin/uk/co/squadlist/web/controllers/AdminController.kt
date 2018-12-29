package uk.co.squadlist.web.controllers

import com.google.common.base.Splitter
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import org.apache.log4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.squadlist.web.annotations.RequiresPermission
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.context.Context
import uk.co.squadlist.web.context.GoverningBodyFactory
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException
import uk.co.squadlist.web.model.Member
import uk.co.squadlist.web.model.forms.InstanceDetails
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.services.filters.ActiveMemberFilter
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.CsvOutputRenderer
import uk.co.squadlist.web.views.DateFormatter
import uk.co.squadlist.web.views.ViewFactory
import java.util.*
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@Controller
class AdminController(val api: InstanceSpecificApiClient,   // TODO move to user specific api
                      val viewFactory: ViewFactory,
                      val activeMemberFilter: ActiveMemberFilter, val csvOutputRenderer: CsvOutputRenderer,
                      val urlBuilder: UrlBuilder,
                      val context: Context, val dateFormatter: DateFormatter, val governingBodyFactory: GoverningBodyFactory) {

    private val log = Logger.getLogger(AdminController::class.java)

    private val MEMBER_ORDERINGS = Lists.newArrayList("firstName", "lastName")
    private val GOVERNING_BODIES = Lists.newArrayList("british-rowing", "rowing-ireland")

    private val COMMA_SPLITTER = Splitter.on(",")

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin", method = arrayOf(RequestMethod.GET))
    @Throws(Exception::class)
    fun member(): ModelAndView {
        val mv = viewFactory.getViewForLoggedInUser("admin")
        mv.addObject("squads", api.squads)
        mv.addObject("availabilityOptions", api.availabilityOptions)
        mv.addObject("title", "Admin")

        val members = api.members
        mv.addObject("members", members)
        mv.addObject("activeMembers", activeMemberFilter.extractActive(members))
        mv.addObject("inactiveMembers", activeMemberFilter.extractInactive(members))
        mv.addObject("admins", extractAdminUsersFrom(members))
        mv.addObject("governingBody", governingBodyFactory.governingBody)
        mv.addObject("statistics", api.statistics())
        mv.addObject("boats", api.boats)
        mv.addObject("language", context.language)
        return mv
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/instance", method = arrayOf(RequestMethod.GET))
    @Throws(Exception::class)
    fun instance(): ModelAndView {
        val instanceDetails = InstanceDetails()
        instanceDetails.memberOrdering = api.instance.memberOrdering
        instanceDetails.governingBody = api.instance.governingBody
        return renderEditInstanceDetailsForm(instanceDetails)
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/instance", method = arrayOf(RequestMethod.POST))
    @Throws(Exception::class)
    fun instanceSubmit(@Valid @ModelAttribute("instanceDetails") instanceDetails: InstanceDetails, result: BindingResult): ModelAndView {
        if (result.hasErrors()) {
            return renderEditInstanceDetailsForm(instanceDetails)
        }

        val instance = api.instance
        instance.memberOrdering = instanceDetails.memberOrdering  // TODO validate
        instance.governingBody = instanceDetails.governingBody  // TODO validate

        api.updateInstance(instance)

        return redirectToAdminScreen()
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/admins", method = arrayOf(RequestMethod.GET))
    @Throws(Exception::class)
    fun setAdminsPrompt(): ModelAndView {
        val adminMembers = Lists.newArrayList<Member>()
        val availableMembers = Lists.newArrayList<Member>()
        for (member in api.members) {
            if (member.admin!!) {
                adminMembers.add(member)
            } else {
                availableMembers.add(member)
            }
        }
        return viewFactory.getViewForLoggedInUser("editAdmins").addObject("admins", adminMembers).addObject("availableMembers", availableMembers)
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/admins", method = arrayOf(RequestMethod.POST))
    @Throws(Exception::class)
    fun setAdmins(@RequestParam admins: String): ModelAndView {
        log.info("Setting admins request: $admins")
        val updatedAdmins = Sets.newHashSet(COMMA_SPLITTER.split(admins).iterator())

        log.info("Setting admins to: $updatedAdmins")
        api.setAdmins(updatedAdmins)

        return redirectToAdminScreen()
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @RequestMapping(value = "/admin/export/members.csv", method = arrayOf(RequestMethod.GET))
    @Throws(Exception::class)
    fun membersCSV(response: HttpServletResponse) {
        val rows = Lists.newArrayList<List<String>>()
        for (member in api.members) {
            rows.add(Arrays.asList(member.firstName,
                    member.lastName,
                    member.knownAs,
                    member.emailAddress,
                    member.gender,
                    if (member.dateOfBirth != null) dateFormatter.dayMonthYear(member.dateOfBirth) else "",
                    member.emergencyContactName,
                    member.emergencyContactNumber,
                    if (member.weight != null) member.weight!!.toString() else "",
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
    }

    private fun extractAdminUsersFrom(members: List<Member>): List<Member> {
        val admins = Lists.newArrayList<Member>()
        for (member in members) {
            if (member.admin != null && member.admin!!) {  // TODO should be boolean is the API knows that it is always present.
                admins.add(member)
            }
        }
        return admins
    }

    private fun redirectToAdminScreen(): ModelAndView {
        return ModelAndView(RedirectView(urlBuilder.adminUrl()))
    }

    @Throws(SignedInMemberRequiredException::class)
    private fun renderEditInstanceDetailsForm(instanceDetails: InstanceDetails): ModelAndView {
        return viewFactory.getViewForLoggedInUser("editInstance").addObject("instanceDetails", instanceDetails).addObject("memberOrderings", MEMBER_ORDERINGS).addObject("governingBodies", GOVERNING_BODIES)
    }


}