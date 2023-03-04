package uk.co.squadlist.web.controllers

import com.fasterxml.jackson.databind.JsonNode
import com.google.common.collect.Lists
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.model.swagger.Member
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.GoverningBodyFactory
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.exceptions.PermissionDeniedException
import uk.co.squadlist.web.localisation.GoverningBody
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.services.PreferredSquadService
import uk.co.squadlist.web.services.filters.ActiveMemberFilter
import uk.co.squadlist.web.views.CsvOutputRenderer
import uk.co.squadlist.web.views.DateFormatter
import uk.co.squadlist.web.views.NavItemsBuilder
import uk.co.squadlist.web.views.ViewFactory
import java.util.*
import javax.servlet.http.HttpServletResponse

@Controller
class EntryDetailsController @Autowired constructor(private val preferredSquadService: PreferredSquadService,
                                                    private val viewFactory: ViewFactory,
                                                    private val csvOutputRenderer: CsvOutputRenderer,
                                                    private val governingBodyFactory: GoverningBodyFactory,
                                                    private val navItemsBuilder: NavItemsBuilder,
                                                    private val permissionsService: PermissionsService,
                                                    private val displayMemberFactory: DisplayMemberFactory,
                                                    private val activeMemberFilter: ActiveMemberFilter,
                                                    loggedInUserService: LoggedInUserService,
                                                    instanceConfig: InstanceConfig) : WithSignedInUser(instanceConfig, loggedInUserService, permissionsService) {

    private val cvsHeaders = Lists.newArrayList(
        "First name", "Last name", "Date of birth", "Effective age", "Age grade",
        "Weight", "Rowing points", "Rowing status",
        "Sculling points", "Sculling status", "Registration number"
    )

    @GetMapping("/entrydetails/{squadId}")
    fun entrydetails(@PathVariable squadId: String?): ModelAndView {
        val renderSquadEntryDetailsPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)

            val squadsUserCanSeeEntryDetailsFor = squads.filter { squad ->
                permissionsService.hasSquadPermission(loggedInMember, Permission.VIEW_SQUAD_ENTRY_DETAILS, squad)
            }

            val squadToShow = preferredSquadService.resolveSquad(squadId, swaggerApiClientForLoggedInUser, instance)

            if (permissionsService.hasSquadPermission(loggedInMember, Permission.VIEW_SQUAD_ENTRY_DETAILS, squadToShow)) {
                val navItems = navItemsBuilder.navItemsFor(loggedInMember, "entry.details", swaggerApiClientForLoggedInUser, instance, squads)

                val squadMembers = swaggerApiClientForLoggedInUser.getSquadMembers(squadToShow.id)
                val activeMembers = activeMemberFilter.extractActive(squadMembers)
                val displayMembers = displayMemberFactory.toDisplayMembers(activeMembers, loggedInMember)

                viewFactory.getViewFor("entryDetails", instance)
                    .addObject("title", "Entry details")
                    .addObject("navItems", navItems)
                    .addObject("squads", squadsUserCanSeeEntryDetailsFor)
                    .addObject("governingBody", governingBodyFactory.getGoverningBody(instance))
                    .addObject("squad", squadToShow)
                    .addObject("title", squadToShow.name + " entry details")
                    .addObject("members", displayMembers)

            } else {
                throw PermissionDeniedException()
            }
        }

        return withSignedInMember(renderSquadEntryDetailsPage)
    }

    @PostMapping("/entrydetails/ajax")
    fun ajax(@RequestBody json: JsonNode): ModelAndView {
        val renderSelectedMembersAjax = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val selectedMembers: MutableList<Member> = Lists.newArrayList()
            for (jsonNode in json) {
                selectedMembers.add(swaggerApiClientForLoggedInUser.getMember(jsonNode.asText()))
            }
            val rowingPoints: MutableList<String?> = Lists.newArrayList()
            val scullingPoints: MutableList<String?> = Lists.newArrayList()
            for (member in selectedMembers) {
                rowingPoints.add(member.rowingPoints)
                scullingPoints.add(member.scullingPoints)
            }
            val mv = viewFactory.getViewFor("entryDetailsAjax", instance)
            if (!selectedMembers.isEmpty()) {
                mv.addObject("members", displayMemberFactory.toDisplayMembers(selectedMembers, loggedInMember))
                val governingBody = governingBodyFactory.getGoverningBody(instance)
                val crewSize = selectedMembers.size
                val isFullBoat = governingBody.boatSizes.contains(crewSize)
                mv.addObject("ok", isFullBoat)
                if (isFullBoat) {
                    mv.addObject("rowingPoints", governingBody.getTotalPoints(rowingPoints))
                    mv.addObject("rowingStatus", governingBody.getRowingStatus(rowingPoints))
                    mv.addObject("scullingPoints", governingBody.getTotalPoints(scullingPoints))
                    mv.addObject("scullingStatus", governingBody.getScullingStatus(scullingPoints))
                    val datesOfBirth: MutableList<DateTime?> = Lists.newArrayList()
                    for (member in selectedMembers) {
                        datesOfBirth.add(member.dateOfBirth)
                    }
                    val effectiveAge = governingBody.getEffectiveAge(datesOfBirth)
                    if (effectiveAge != null) {
                        mv.addObject("effectiveAge", effectiveAge)
                        mv.addObject("ageGrade", governingBody.getAgeGrade(effectiveAge))
                    }
                }
            }
            mv
        }
        return withSignedInMember(renderSelectedMembersAjax)
    }

    @GetMapping("/entrydetails/{squadId}.csv")
    fun entrydetailsCSV(@PathVariable squadId: String, response: HttpServletResponse?) {
        val renderEntryDetailsCsv = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squadToShow = preferredSquadService.resolveSquad(squadId, swaggerApiClientForLoggedInUser, instance)
            if (permissionsService.hasSquadPermission(loggedInMember, Permission.VIEW_SQUAD_ENTRY_DETAILS, squadToShow)) {
                val squadMembers = swaggerApiClientForLoggedInUser.getSquadMembers(squadToShow.id)
                val activeMembers = activeMemberFilter.extractActive(squadMembers)
                val governingBody = governingBodyFactory.getGoverningBody(instance)
                val entryDetailsRows = makeEntryDetailsRowsFor(activeMembers, governingBody, instance)
                csvOutputRenderer.renderCsvResponse(response, cvsHeaders, entryDetailsRows)
                ModelAndView()  // TODO questionable
            } else {
                throw PermissionDeniedException()
            }
        }
        withSignedInMember(renderEntryDetailsCsv)
    }

    private fun makeEntryDetailsRowsFor(members: List<Member>, governingBody: GoverningBody, instance: Instance): List<List<String>>? {
        val dateFormatter = DateFormatter(DateTimeZone.forID(instance.timeZone))
        return members.map { member ->
            val effectiveAge = if (member.dateOfBirth != null) governingBody.getEffectiveAge(member.dateOfBirth) else null
            val ageGrade = if (effectiveAge != null) governingBody.getAgeGrade(effectiveAge) else null
            val formattedDob = if (member.dateOfBirth != null) dateFormatter.dayMonthYear(member.dateOfBirth.toDate()) else ""
            Arrays.asList(
                member.firstName, member.lastName,
                formattedDob,
                effectiveAge?.toString() ?: "",
                ageGrade ?: "",
                if (member.weight != null) member.weight.toString() else "",
                member.rowingPoints,
                governingBody.getRowingStatus(member.rowingPoints),
                member.scullingPoints,
                governingBody.getScullingStatus(member.scullingPoints),
                member.registrationNumber
            )
        }
    }

}