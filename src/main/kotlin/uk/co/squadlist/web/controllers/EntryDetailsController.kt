package uk.co.squadlist.web.controllers

import com.google.common.base.Splitter
import com.google.common.collect.Lists
import org.codehaus.jackson.map.ObjectMapper
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.api.SquadlistApiFactory
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.GoverningBodyFactory
import uk.co.squadlist.web.services.PreferedSquadService
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.CsvOutputRenderer
import uk.co.squadlist.web.views.DateFormatter
import uk.co.squadlist.web.views.PermissionsHelper
import uk.co.squadlist.web.views.ViewFactory
import java.util.*
import javax.servlet.http.HttpServletResponse

@Controller
public class EntryDetailsController(val instanceSpecificApiClient: InstanceSpecificApiClient,
                                    val preferedSquadService: PreferedSquadService,
                                    val viewFactory: ViewFactory,
                                    val entryDetailsModelPopulator: EntryDetailsModelPopulator,
                                    val csvOutputRenderer: CsvOutputRenderer, val governingBodyFactory: GoverningBodyFactory,
                                    val squadlistApiFactory: SquadlistApiFactory, val loggedInUserService: LoggedInUserService,
                                    val urlBuilder: UrlBuilder, val permissionsHelper: PermissionsHelper, val dateFormatter: DateFormatter) {

    private val entryDetailsHeaders = Lists.newArrayList("First name", "Last name", "Date of birth", "Effective age", "Age grade",
            "Weight", "Rowing points", "Rowing status",
            "Sculling points", "Sculling status", "Registration number")

    @GetMapping("/entry-details/{squadId}")
    fun entrydetails(@PathVariable squadId: String): ModelAndView {
        val mv = viewFactory.getViewForLoggedInUser("entryDetails")
        mv.addObject("squads", instanceSpecificApiClient.squads)
        mv.addObject("governingBody", governingBodyFactory.governingBody)
        mv.addObject("urlBuilder", urlBuilder)
        mv.addObject("permissionsHelper", permissionsHelper)

        val squadToShow = preferedSquadService.resolveSquad(squadId)
        entryDetailsModelPopulator.populateModel(squadToShow, mv)
        return mv.addObject("dateFormatter", dateFormatter)
    }

    @GetMapping("/entry-details/ajax")
    fun ajax(@RequestBody json: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val memberIds = ObjectMapper().readTree(json).map { it.asText() }
        val selectedMembers= memberIds.map { loggedInUserApi.getMember(it)}

        val rowingPoints = selectedMembers.map { it.rowingPoints }
        val scullingPoints = selectedMembers.map { it.scullingPoints }

        val mv = viewFactory.getViewForLoggedInUser("entryDetailsAjax")
        if (!selectedMembers.isEmpty()) {
            mv.addObject("members", selectedMembers)

            val governingBody = governingBodyFactory.governingBody

            val crewSize = selectedMembers.size
            val isFullBoat = governingBody.boatSizes.contains(crewSize)
            mv.addObject("ok", isFullBoat)
            if (isFullBoat) {
                mv.addObject("rowingPoints", governingBody.getTotalPoints(rowingPoints))
                mv.addObject("rowingStatus", governingBody.getRowingStatus(rowingPoints))

                mv.addObject("scullingPoints", governingBody.getTotalPoints(scullingPoints))
                mv.addObject("scullingStatus", governingBody.getScullingStatus(scullingPoints))

                val datesOfBirth = Lists.newArrayList<Date>()
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
        return mv
    }

    @GetMapping("/entry-details/{squadId}.csv")
    fun entrydetailsCSV(@PathVariable squadId: String, response: HttpServletResponse) {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val squadToShow = preferedSquadService.resolveSquad(squadId)
        val squadMembers = loggedInUserApi.getSquadMembers(squadToShow.id)

        val entryDetailsRows = entryDetailsModelPopulator.getEntryDetailsRows(squadMembers)
        csvOutputRenderer.renderCsvResponse(response, entryDetailsHeaders, entryDetailsRows)
    }

    @GetMapping("/entry-details/selected.csv") // TODO Unused
    fun entrydetailsSelectedCSV(@RequestParam members: String, response: HttpServletResponse) {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val selectedMembers = Splitter.on(",").split(members).map { loggedInUserApi.getMember(it) }

        csvOutputRenderer.renderCsvResponse(response, entryDetailsHeaders, entryDetailsModelPopulator.getEntryDetailsRows(selectedMembers))
    }

}