package uk.co.squadlist.web.controllers

import com.fasterxml.jackson.databind.JsonNode
import com.google.common.collect.Lists
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.model.swagger.Member
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.GoverningBodyFactory
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.exceptions.PermissionDeniedException
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.services.PreferredSquadService
import uk.co.squadlist.web.services.filters.ActiveMemberFilter
import uk.co.squadlist.web.views.CsvOutputRenderer
import uk.co.squadlist.web.views.NavItemsBuilder
import uk.co.squadlist.web.views.ViewFactory
import javax.servlet.http.HttpServletResponse

@Controller
class EntryDetailsController @Autowired constructor(private val preferredSquadService: PreferredSquadService,
                                                    private val viewFactory: ViewFactory,
                                                    private val entryDetailsModelPopulator: EntryDetailsModelPopulator,
                                                    private val csvOutputRenderer: CsvOutputRenderer,
                                                    private val governingBodyFactory: GoverningBodyFactory,
                                                    private val navItemsBuilder: NavItemsBuilder,
                                                    private val permissionsService: PermissionsService,
                                                    private val displayMemberFactory: DisplayMemberFactory,
                                                    private val activeMemberFilter: ActiveMemberFilter,
                                                    loggedInUserService: LoggedInUserService,
                                                    instanceConfig: InstanceConfig) : WithSignedInUser(instanceConfig, loggedInUserService, permissionsService) {
    @GetMapping("/entrydetails/{squadId}")
    fun entrydetails(@PathVariable squadId: String?): ModelAndView {
        val renderSquadEntryDetailsPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
            val squadToShow = preferredSquadService.resolveSquad(squadId, swaggerApiClientForLoggedInUser, instance)

            if (permissionsService.hasSquadPermission(loggedInMember, Permission.VIEW_SQUAD_ENTRY_DETAILS, squadToShow)) {
                val navItems = navItemsBuilder.navItemsFor(loggedInMember, "entry.details", swaggerApiClientForLoggedInUser, instance, squads)

                val squadMembers = swaggerApiClientForLoggedInUser.getSquadMembers(squadToShow.id)
                val activeMembers = activeMemberFilter.extractActive(squadMembers)
                val displayMembers = displayMemberFactory.toDisplayMembers(activeMembers, loggedInMember)

                viewFactory.getViewFor("entryDetails", instance)
                    .addObject("title", "Entry details")
                    .addObject("navItems", navItems)
                    .addObject("squads", squads)
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
                val entryDetailsRows = entryDetailsModelPopulator.getEntryDetailsRows(activeMembers, governingBody, instance)
                csvOutputRenderer.renderCsvResponse(response, entryDetailsModelPopulator.entryDetailsHeaders, entryDetailsRows)
                ModelAndView()  // TODO questionable
            } else {
                throw PermissionDeniedException()
            }
        }
        withSignedInMember(renderEntryDetailsCsv)
    }

}