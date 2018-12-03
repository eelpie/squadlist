package uk.co.squadlist.web.controllers

import com.google.common.base.Strings
import com.google.common.collect.Lists
import org.apache.log4j.Logger
import org.codehaus.jackson.JsonParseException
import org.codehaus.jackson.map.JsonMappingException
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.common.http.HttpFetchException
import uk.co.squadlist.web.annotations.RequiresOutingPermission
import uk.co.squadlist.web.annotations.RequiresPermission
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.api.SquadlistApi
import uk.co.squadlist.web.api.SquadlistApiFactory
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.exceptions.InvalidOutingException
import uk.co.squadlist.web.exceptions.OutingClosedException
import uk.co.squadlist.web.exceptions.UnknownAvailabilityOptionException
import uk.co.squadlist.web.model.AvailabilityOption
import uk.co.squadlist.web.model.Instance
import uk.co.squadlist.web.model.Outing
import uk.co.squadlist.web.model.Squad
import uk.co.squadlist.web.model.forms.OutingDetails
import uk.co.squadlist.web.services.OutingAvailabilityCountsService
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.services.PreferedSquadService
import uk.co.squadlist.web.services.filters.ActiveMemberFilter
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.CsvOutputRenderer
import uk.co.squadlist.web.views.DateFormatter
import uk.co.squadlist.web.views.DateHelper
import uk.co.squadlist.web.views.ViewFactory
import java.io.IOException
import java.util.*
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@Controller
class OutingsController(val loggedInUserService: LoggedInUserService, val instanceSpecificApiClient: InstanceSpecificApiClient, val urlBuilder: UrlBuilder,
                        val dateFormatter: DateFormatter, val preferedSquadService: PreferedSquadService, val viewFactory: ViewFactory,
                        val outingAvailabilityCountsService: OutingAvailabilityCountsService, val activeMemberFilter: ActiveMemberFilter,
                        val csvOutputRenderer: CsvOutputRenderer, val squadlistApiFactory: SquadlistApiFactory) {

    private val log = Logger.getLogger(OutingsController::class.java)

    private val squadlistApi: SquadlistApi = squadlistApiFactory.createClient()

    @GetMapping("/outings")
    fun outings(@RequestParam(required = false, value = "squad") squadId: String,
                @RequestParam(value = "month", required = false) month: String?): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)  // TODO why is this unused!

        val squadToShow = preferedSquadService.resolveSquad(squadId)
        val mv = viewFactory.getViewForLoggedInUser("outings")
        if (squadToShow == null) {
            mv.addObject("title", "Outings")
            return mv
        }

        var startDate = DateHelper.startOfCurrentOutingPeriod().toDate()
        var endDate = DateHelper.endOfCurrentOutingPeriod().toDate()

        var title = squadToShow.name + " outings"
        if (month != null) {
            val monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month)  // TODO Can be moved to spring?
            startDate = monthDateTime.toDate()
            endDate = monthDateTime.plusMonths(1).toDate()
            title = squadToShow.name + " outings - " + dateFormatter.fullMonthYear(startDate)
        } else {
            mv.addObject("current", true)
        }

        mv.addObject("title", title)
        mv.addObject("squad", squadToShow)
        mv.addObject("startDate", startDate)
        mv.addObject("endDate", endDate)
        mv.addObject("month", month)
        mv.addObject("outingMonths", getOutingMonthsFor(squadToShow))

        val squadOutings = squadlistApi.getSquadAvailability(squadToShow.id, startDate, endDate)
        mv.addObject("outings", squadOutings)
        mv.addObject("outingAvailabilityCounts", outingAvailabilityCountsService.buildOutingAvailabilityCounts(squadOutings))

        mv.addObject("squads", instanceSpecificApiClient.squads)
        return mv
    }

    @GetMapping("/outings/{id}")
    fun outing(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val outing = loggedInUserApi.getOuting(id)

        val mv = viewFactory.getViewForLoggedInUser("outing")
        mv.addObject("title", outing.squad.name + " - " + dateFormatter.dayMonthYearTime(outing.date))
        mv.addObject("outing", outing)
        mv.addObject("outingMonths", getOutingMonthsFor(outing.squad))
        mv.addObject("squad", outing.squad)
        mv.addObject("squadAvailability", loggedInUserApi.getOutingAvailability(outing.id))
        mv.addObject("squads", instanceSpecificApiClient.squads)
        mv.addObject("members", activeMemberFilter.extractActive(squadlistApi.getSquadMembers(outing.squad.id)))
        mv.addObject("month", ISODateTimeFormat.yearMonth().print(outing.date.time))  // TODO push to date parser - local time
        return mv
    }

    @GetMapping("/outings/{id}.csv")
    fun outingCsv(@PathVariable id: String, response: HttpServletResponse) {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val outing = loggedInUserApi.getOuting(id)
        val squadMembers = squadlistApi.getSquadMembers(outing.squad.id)
        val outingAvailability = loggedInUserApi.getOutingAvailability(outing.id)

        val rows = Lists.newArrayList<List<String>>()
        for (member in squadMembers) {
            rows.add(Arrays.asList<String>(
                    dateFormatter.dayMonthYearTime(outing.date),
                    outing.squad.name,
                    member.displayName,
                    member.role,
                    if (outingAvailability[member.id] != null) outingAvailability[member.id]!!.getLabel() else null
            ))
        }
        csvOutputRenderer.renderCsvResponse(response, Lists.newArrayList("Date", "Squad", "Member", "Role", "Availability"), rows)
    }

    @RequiresPermission(permission = Permission.ADD_OUTING)
    @GetMapping("/outings/new")  // TODO fails hard if no squads are available
    fun newOuting(): ModelAndView {
        val defaultOutingDateTime = DateHelper.defaultOutingStartDateTime()
        val outingDefaults = OutingDetails(defaultOutingDateTime)
        outingDefaults.squad = preferedSquadService.resolveSquad(null).id
        return renderNewOutingForm(outingDefaults)
    }

    @RequiresPermission(permission = Permission.ADD_OUTING)
    @PostMapping("/outings/new")
    fun newOutingSubmit(@Valid @ModelAttribute("outing") outingDetails: OutingDetails, result: BindingResult): ModelAndView {
        if (result.hasErrors()) {
            return renderNewOutingForm(outingDetails)
        }

        try {
            val newOuting = buildOutingFromOutingDetails(outingDetails, instanceSpecificApiClient.instance)
            if (outingDetails.repeats != null && outingDetails.repeats!! && outingDetails.repeatsCount != null) {
                squadlistApi.createOuting(newOuting, outingDetails.repeatsCount)
            } else {
                squadlistApi.createOuting(newOuting, null)
            }

            val outingsViewForNewOutingsSquadAndMonth = urlBuilder.outings(newOuting.squad, DateTime(newOuting.date).toString("yyyy-MM"))
            return ModelAndView(RedirectView(outingsViewForNewOutingsSquadAndMonth))

        } catch (e: InvalidOutingException) {
            result.addError(ObjectError("outing", e.message))
            return renderNewOutingForm(outingDetails)

        } catch (e: Exception) {
            result.addError(ObjectError("outing", "Unknown error"))
            return renderNewOutingForm(outingDetails)
        }

    }

    @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @GetMapping("/outings/{id}/edit")
    fun outingEdit(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val outing = loggedInUserApi.getOuting(id)

        val outingDetails = OutingDetails(LocalDateTime(outing.date))
        outingDetails.squad = outing.squad.id
        outingDetails.notes = outing.notes

        return renderEditOutingForm(outingDetails, outing)
    }

    @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @RequestMapping("/outings/{id}/delete", method = arrayOf(RequestMethod.GET))
    fun deleteOutingPrompt(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)
        val outing = loggedInUserApi.getOuting(id)
        return viewFactory.getViewForLoggedInUser("deleteOuting").addObject("outing", outing)
    }

    @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @PostMapping("/outings/{id}/delete")
    fun deleteOuting(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)
        val outing = loggedInUserApi.getOuting(id)

        squadlistApi.deleteOuting(outing.id)

        val exitUrl = if (outing.squad == null) urlBuilder.outings(outing.squad) else urlBuilder.outingsUrl()
        return ModelAndView(RedirectView(exitUrl))
    }

    @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @GetMapping("/outings/{id}/close")
    fun closeOuting(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val outing = loggedInUserApi.getOuting(id)

        log.info("Closing outing: $outing")
        outing.isClosed = true
        loggedInUserApi.updateOuting(outing)

        return redirectToOuting(outing)
    }

    @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @GetMapping("/outings/{id}/reopen")
    fun reopenOuting(@PathVariable id: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val outing = loggedInUserApi.getOuting(id)

        log.info("Reopening outing: $outing")
        outing.isClosed = false
        loggedInUserApi.updateOuting(outing)

        return redirectToOuting(outing)
    }

    @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @PostMapping("/outings/{id}/edit")
    fun editOutingSubmit(@PathVariable id: String,
                         @Valid @ModelAttribute("outing") outingDetails: OutingDetails, result: BindingResult): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val outing = loggedInUserApi.getOuting(id)
        if (result.hasErrors()) {
            return renderEditOutingForm(outingDetails, outing)
        }
        try {
            val updatedOuting = buildOutingFromOutingDetails(outingDetails, instanceSpecificApiClient.instance)
            updatedOuting.id = id

            loggedInUserApi.updateOuting(updatedOuting)
            return redirectToOuting(updatedOuting)

        } catch (e: InvalidOutingException) {
            result.addError(ObjectError("outing", e.message))
            return renderEditOutingForm(outingDetails, outing)

        } catch (e: Exception) {
            log.error(e)
            result.addError(ObjectError("outing", "Unknown exception"))
            return renderEditOutingForm(outingDetails, outing)
        }

    }

    @PostMapping("/availability/ajax")
    fun updateAvailability(
            @RequestParam(value = "outing", required = true) outingId: String,
            @RequestParam(value = "availability", required = true) availability: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val outing = loggedInUserApi.getOuting(outingId)

        if (!outing.isClosed) {
            val result = loggedInUserApi.setOutingAvailability(loggedInUserService.loggedInMember, outing, getAvailabilityOptionById(availability))
            return viewFactory.getViewForLoggedInUser("includes/availability").addObject("availability", result.availabilityOption)
        }

        throw OutingClosedException()
    }

    private fun redirectToOuting(updatedOuting: Outing): ModelAndView {
        return ModelAndView(RedirectView(urlBuilder.outingUrl(updatedOuting)))
    }

    private fun renderNewOutingForm(outingDetails: OutingDetails): ModelAndView {
        val mv = viewFactory.getViewForLoggedInUser("newOuting")
        mv.addObject("squads", instanceSpecificApiClient.squads)
        val squad = preferedSquadService.resolveSquad(null)
        mv.addObject("squad", squad)
        mv.addObject("outingMonths", getOutingMonthsFor(squad))
        mv.addObject("outing", outingDetails)
        return mv
    }

    private fun renderEditOutingForm(outingDetails: OutingDetails, outing: Outing): ModelAndView {
        val mv = viewFactory.getViewForLoggedInUser("editOuting")
        mv.addObject("squads", instanceSpecificApiClient.squads)
        mv.addObject("squad", outing.squad)
        mv.addObject("outing", outingDetails)
        mv.addObject("outingObject", outing)
        mv.addObject("outingMonths", getOutingMonthsFor(outing.squad))
        mv.addObject("month", ISODateTimeFormat.yearMonth().print(outing.date.time))  // TODO push to date parser - local time
        return mv
    }

    @Throws(JsonParseException::class, JsonMappingException::class, HttpFetchException::class, IOException::class, UnknownAvailabilityOptionException::class)
    private fun getAvailabilityOptionById(availabilityId: String): AvailabilityOption? {
        return if (Strings.isNullOrEmpty(availabilityId)) {
            null
        } else instanceSpecificApiClient.getAvailabilityOption(availabilityId)
    }

    private fun getOutingMonthsFor(squad: Squad?): Map<String, Int> {
        return instanceSpecificApiClient.getOutingMonths(squad)
    }

    private fun buildOutingFromOutingDetails(outingDetails: OutingDetails, instance: Instance): Outing {
        val date = outingDetails.toLocalTime().toDateTime(DateTimeZone.forID(instance.timeZone)).toDate()
        val squad = if (outingDetails.squad != null) squadlistApi.getSquad(outingDetails.squad) else null  // TODO validation
        val notes = outingDetails.notes
        return Outing(squad, date, notes)
    }

}
