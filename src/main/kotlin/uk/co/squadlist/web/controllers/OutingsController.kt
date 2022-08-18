package uk.co.squadlist.web.controllers

import com.google.common.collect.Lists
import org.apache.logging.log4j.LogManager
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.joda.time.YearMonth
import org.joda.time.format.ISODateTimeFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.ApiException
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.*
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.controllers.DateRange.Companion.from
import uk.co.squadlist.web.exceptions.PermissionDeniedException
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException
import uk.co.squadlist.web.model.forms.OutingDetails
import uk.co.squadlist.web.services.OutingAvailabilityCountsService
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.services.PreferredSquadService
import uk.co.squadlist.web.services.filters.ActiveMemberFilter
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.*
import uk.co.squadlist.web.views.model.DisplayMember
import java.net.URISyntaxException
import java.util.*
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@Controller
class OutingsController @Autowired constructor(    // TODO remove open when annotions are gone
    private val urlBuilder: UrlBuilder,
    private val preferredSquadService: PreferredSquadService,
    private val viewFactory: ViewFactory,
    private val outingAvailabilityCountsService: OutingAvailabilityCountsService,
    private val activeMemberFilter: ActiveMemberFilter,
    private val csvOutputRenderer: CsvOutputRenderer,
    private val permissionsService: PermissionsService,
    private val navItemsBuilder: NavItemsBuilder,
    private val displayMemberFactory: DisplayMemberFactory,
    loggedInUserService: LoggedInUserService,
    instanceConfig: InstanceConfig): WithSignedInUser(instanceConfig, loggedInUserService, permissionsService) {

    private val log = LogManager.getLogger(OutingsController::class.java)

    @GetMapping("/outings")
    fun outings(
        @RequestParam(required = false, value = "squad") squadId: String?,
        @RequestParam(value = "month", required = false) month: String?): ModelAndView {
        val renderOutingsPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
                val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
                val squadToShow = preferredSquadService.resolveSquad(squadId, swaggerApiClientForLoggedInUser, instance)
                val mv = viewFactory.getViewFor("outings", instance)
                if (squadToShow == null) {
                    mv.addObject("title", "Outings")
                }
                val yearMonth = if (month != null) YearMonth.parse(month) else null // TODO push to Spring parameter?
                val dateRange = from(yearMonth, null, null)
                var title = squadToShow.name + " outings"
                if (dateRange.month != null) {
                    title = squadToShow.name + " outings - " + dateRange.month.toString("MMMMM yyyy")
                }
                val navItems =
                    navItemsBuilder.navItemsFor(
                        loggedInMember,
                        "outings",
                        swaggerApiClientForLoggedInUser,
                        instance,
                        squads
                    )
                val outingMonths = getOutingMonthsFor(instance, squadToShow, swaggerApiClientForLoggedInUser)
                val dateTimeZone = DateTimeZone.forID(instance.timeZone)
                val startDateTime = dateRange.start!!.toDateTimeAtStartOfDay(dateTimeZone)
                val endDateTime = dateRange.end!!.toDateTimeAtStartOfDay(dateTimeZone).plusDays(1)
                val squadOutings = swaggerApiClientForLoggedInUser.getSquadAvailability(
                    squadToShow.id,
                    DateTime(startDateTime),
                    DateTime(endDateTime)
                )
                mv.addObject("title", title).addObject("navItems", navItems)
                    .addObject("squad", squadToShow)
                    .addObject("dateRange", dateRange)
                    .addObject("outingMonths", outingMonths)
                    .addObject("outings", squadOutings)
                    .addObject("outingAvailabilityCounts", outingAvailabilityCountsService.buildOutingAvailabilityCounts(squadOutings))
                    .addObject("squads", squads)
                    .addObject("canAddOuting", permissionsService.hasPermission(loggedInMember, Permission.ADD_OUTING))
                mv
            }

        return withSignedInMember(renderOutingsPage)
    }

    @GetMapping("/outings/{id}")
    fun outing(@PathVariable id: String?): ModelAndView {
        val renderOutingPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
            val outing = swaggerApiClientForLoggedInUser.getOuting(id)
            val outingAvailability = swaggerApiClientForLoggedInUser.getOutingAvailability(outing.id)
            val squadMembers = swaggerApiClientForLoggedInUser.getSquadMembers(outing.squad.id)
            val activeMembers = activeMemberFilter.extractActive(squadMembers)
            val displayMembers = displayMemberFactory.toDisplayMembers(activeMembers, loggedInMember)
            val canEditOuting = permissionsService.hasOutingPermission(loggedInMember, Permission.EDIT_OUTING, outing)
            val navItems = navItemsBuilder.navItemsFor(loggedInMember, "outings", swaggerApiClientForLoggedInUser, instance, squads)
            val dateFormatter = DateFormatter(DateTimeZone.forID(instance.timeZone))

            val canAddOuting = permissionsService.hasPermission(loggedInMember, Permission.ADD_OUTING)

            viewFactory.getViewFor("outing", instance)
                .addObject("title", outing.squad.name + " - " + dateFormatter.dayMonthYearTime(outing.date))
                .addObject("navItems", navItems).addObject("outing", outing)
                .addObject("canEditOuting", canEditOuting)
                .addObject("outingMonths", getOutingMonthsFor(instance, outing.squad, swaggerApiClientForLoggedInUser))
                .addObject("squad", outing.squad)
                .addObject("squadAvailability", outingAvailability)
                .addObject("squads", squads).addObject("members", displayMembers)
                .addObject("month", ISODateTimeFormat.yearMonth().print(outing.date.toLocalDateTime())) // TODO push to date parser
                .addObject("canAddOuting", canAddOuting)
        }

        return withSignedInMember(renderOutingPage)
    }

    @GetMapping("/outings/{id}.csv")
    fun outingCsv(@PathVariable id: String?, response: HttpServletResponse?) {
        val renderOutingCsvPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val outing = swaggerApiClientForLoggedInUser.getOuting(id)
            val squadMembers = swaggerApiClientForLoggedInUser.getSquadMembers(outing.squad.id)
            val outingAvailability = swaggerApiClientForLoggedInUser.getOutingAvailability(outing.id)
            val dateFormatter = DateFormatter(DateTimeZone.forID(instance.timeZone))
            val rows: MutableList<List<String?>> = Lists.newArrayList()
            for (member in squadMembers) {
                val displayMember = DisplayMember(member, false)
                rows.add(
                    Arrays.asList(
                        dateFormatter.dayMonthYearTime(outing.date),
                        outing.squad.name,
                        displayMember.displayName,
                        member.role,
                        if (outingAvailability[member.id] != null) outingAvailability[member.id]!!.label else null
                    )
                )
            }

            csvOutputRenderer.renderCsvResponse(
                response,
                Lists.newArrayList("Date", "Squad", "Member", "Role", "Availability"),
                rows
            )
            ModelAndView()  // TODO questionable
        }

        withSignedInMember(renderOutingCsvPage)
    }

    @GetMapping("/outings/new") // TODO fails hard if no squads are available
    fun newOuting(): ModelAndView {
        val renderNewOutingPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            if (permissionsService.canAddNewOuting(loggedInMember)) {
                val timeZone = instance.timeZone
                val defaultOutingDateTime = DateHelper.defaultOutingStartDateTime(timeZone)
                val outingDefaults = OutingDetails(defaultOutingDateTime)
                outingDefaults.squad =
                    preferredSquadService.resolveSquad(null, swaggerApiClientForLoggedInUser, instance).id
                renderNewOutingForm(outingDefaults, loggedInMember, instance, swaggerApiClientForLoggedInUser)
            } else {
                throw PermissionDeniedException()
            }
        }
        return withSignedInMember(renderNewOutingPage)
    }

    @PostMapping("/outings/new")
    fun newOutingSubmit(
        @ModelAttribute("outing") outingDetails: @Valid OutingDetails?,
        result: BindingResult
    ): ModelAndView {
        val handleNewOutingPost = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            if (permissionsService.canAddNewOuting(loggedInMember)) {

                if (result.hasErrors()) {
                    renderNewOutingForm(outingDetails, loggedInMember, instance, swaggerApiClientForLoggedInUser)
                } else try {
                    val newOuting =
                        buildOutingFromOutingDetails(outingDetails, instance, swaggerApiClientForLoggedInUser)
                    if (outingDetails!!.repeats != null && outingDetails.repeats && outingDetails.repeatsCount != null) {
                        swaggerApiClientForLoggedInUser.createOuting(newOuting, outingDetails.repeatsCount)
                    } else {
                        swaggerApiClientForLoggedInUser.createOuting(newOuting, null)
                    }
                    val outingsViewForNewOutingsSquadAndMonth = urlBuilder.outings(
                        swaggerApiClientForLoggedInUser.getSquad(newOuting.squad.id),
                        DateTime(newOuting.date).toString("yyyy-MM")
                    )
                    viewFactory.redirectionTo(outingsViewForNewOutingsSquadAndMonth)
                } catch (e: ApiException) {
                    log.warn(e.code.toString() + ": " + e.responseBody)
                    result.addError(ObjectError("outing", e.responseBody))
                    renderNewOutingForm(outingDetails, loggedInMember, instance, swaggerApiClientForLoggedInUser)
                } catch (e: Exception) {
                    result.addError(ObjectError("outing", "Unknown error"))
                    renderNewOutingForm(outingDetails, loggedInMember, instance, swaggerApiClientForLoggedInUser)
                }

            } else {
                throw PermissionDeniedException()
            }
        }

        return withSignedInMember(handleNewOutingPost)
    }

    // TODO  @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @GetMapping("/outings/{id}/edit")
    fun outingEdit(@PathVariable id: String?): ModelAndView {
        val renderEditOutingPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val outing = swaggerApiClientForLoggedInUser.getOuting(id)
            val timeZone = instance.timeZone
            val outingLocalDateTime = LocalDateTime(outing.date, DateTimeZone.forID(timeZone))
            log.info("Outing date " + outing.date + " cast to localdatetime " + outingLocalDateTime + " using timezone " + timeZone)
            val outingDetails = OutingDetails(outingLocalDateTime)
            outingDetails.squad = outing.squad.id
            outingDetails.notes = outing.notes
            renderEditOutingForm(outingDetails, loggedInMember, outing, instance, swaggerApiClientForLoggedInUser)
        }

        return withSignedInMember(renderEditOutingPage)
    }

    // TODO @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
    @GetMapping("/outings/{id}/delete")
    fun deleteOutingPrompt(@PathVariable id: String?): ModelAndView {
        val renderDeleteOutingPromptPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
            val outing = swaggerApiClientForLoggedInUser.getOuting(id)
            val navItems = navItemsBuilder.navItemsFor(
                    loggedInMember,
                    "outings",
                    swaggerApiClientForLoggedInUser,
                    instance,
                    squads
                )
            viewFactory.getViewFor("deleteOuting", instance).addObject("title", "Deleting an outing")
                .addObject("navItems", navItems).addObject("outing", outing)
                .addObject("canAddOuting", permissionsService.hasPermission(loggedInMember, Permission.ADD_OUTING))
            }

        return withSignedInMember(renderDeleteOutingPromptPage)
    }

    @PostMapping("/outings/{id}/delete")
    fun deleteOuting(@PathVariable id: String): ModelAndView {
        val handleDeleteOuting = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val outing = swaggerApiClientForLoggedInUser.getOuting(id)
            if (permissionsService.hasOutingPermission(loggedInMember, Permission.EDIT_OUTING, outing)) {
                swaggerApiClientForLoggedInUser.deleteOuting(outing.id)
                val exitUrl = if (outing.squad == null) urlBuilder.outings(outing.squad) else urlBuilder.outingsUrl()
                viewFactory.redirectionTo(exitUrl)
            } else {
                throw PermissionDeniedException()
            }
        }
        return withSignedInMember(handleDeleteOuting)
    }

    @GetMapping("/outings/{id}/close")
    fun closeOuting(@PathVariable id: String): ModelAndView {
        val handleCloseOuting = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val outing = swaggerApiClientForLoggedInUser.getOuting(id)
            if (permissionsService.hasOutingPermission(loggedInMember, Permission.EDIT_OUTING, outing)) {
                outing.isClosed = true
                swaggerApiClientForLoggedInUser.updateOuting(outing, id)
                redirectToOuting(outing)
            } else {
                throw PermissionDeniedException()
            }
        }
        return withSignedInMember(handleCloseOuting)
    }

    @GetMapping("/outings/{id}/reopen")
    fun reopenOuting(@PathVariable id: String): ModelAndView {
        val handleReopeningOuting = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val outing = swaggerApiClientForLoggedInUser.getOuting(id)
            if (permissionsService.hasOutingPermission(loggedInMember, Permission.EDIT_OUTING, outing)) {
                log.info("Reopening outing: $outing")
                outing.isClosed = false
                swaggerApiClientForLoggedInUser.updateOuting(outing, id)
                redirectToOuting(outing)

            } else {
                throw PermissionDeniedException()
            }
        }
        return withSignedInMember(handleReopeningOuting)
    }

    @PostMapping("/outings/{id}/edit")
    fun editOutingSubmit(
        @PathVariable id: String,
        @ModelAttribute("outing") outingDetails: @Valid OutingDetails?, result: BindingResult
    ): ModelAndView {
        val handleEditOuting = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val outing = swaggerApiClientForLoggedInUser.getOuting(id)
            if (permissionsService.hasOutingPermission(loggedInMember, Permission.EDIT_OUTING, outing)) {
                if (result.hasErrors()) {
                    renderEditOutingForm(outingDetails, loggedInMember, outing, instance, swaggerApiClientForLoggedInUser)
                } else try {
                    val updatedOuting = buildOutingFromOutingDetails(outingDetails, instance, swaggerApiClientForLoggedInUser)
                    updatedOuting.id = id
                    swaggerApiClientForLoggedInUser.updateOuting(updatedOuting, id)
                    redirectToOuting(updatedOuting)
                } catch (e: ApiException) {
                    result.addError(ObjectError("outing", e.responseBody))
                    renderEditOutingForm(outingDetails, loggedInMember, outing, instance, swaggerApiClientForLoggedInUser)
                } catch (e: Exception) {
                    log.error(e)
                    result.addError(ObjectError("outing", "Unknown exception"))
                    renderEditOutingForm(outingDetails, loggedInMember, outing, instance, swaggerApiClientForLoggedInUser)
                }

            } else {
                throw PermissionDeniedException()
            }
        }

        return withSignedInMember(handleEditOuting)
    }

    private fun redirectToOuting(updatedOuting: Outing): ModelAndView {
        return viewFactory.redirectionTo(urlBuilder.outingUrl(updatedOuting))
    }

    @Throws(SignedInMemberRequiredException::class, URISyntaxException::class, ApiException::class)
    private fun renderNewOutingForm(
        outingDetails: OutingDetails?,
        loggedInMember: Member,
        instance: Instance,
        swaggerApiClientForLoggedInUser: DefaultApi
    ): ModelAndView {
        val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
        val squad = preferredSquadService.resolveSquad(null, swaggerApiClientForLoggedInUser, instance)
        val navItems =
            navItemsBuilder.navItemsFor(loggedInMember, "outings", swaggerApiClientForLoggedInUser, instance, squads)
        return viewFactory.getViewFor("newOuting", instance).addObject("title", "Add a new outing")
            .addObject("navItems", navItems).addObject("squads", swaggerApiClientForLoggedInUser.getSquads(instance.id))
            .addObject("squad", squad)
            .addObject("outingMonths", getOutingMonthsFor(instance, squad, swaggerApiClientForLoggedInUser))
            .addObject("outing", outingDetails)
    }

    @Throws(SignedInMemberRequiredException::class, URISyntaxException::class, ApiException::class)
    private fun renderEditOutingForm(
        outingDetails: OutingDetails?,
        loggedInMember: Member,
        outing: Outing,
        instance: Instance,
        swaggerApiClientForLoggedInUser: DefaultApi
    ): ModelAndView {
        val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
        val navItems =
            navItemsBuilder.navItemsFor(loggedInMember, "outings", swaggerApiClientForLoggedInUser, instance, squads)
        return viewFactory.getViewFor("editOuting", instance).addObject("title", "Editing an outing")
            .addObject("navItems", navItems).addObject("squads", swaggerApiClientForLoggedInUser.getSquads(instance.id))
            .addObject("squad", outing.squad).addObject("outing", outingDetails).addObject("outingObject", outing)
            .addObject(
                "outingMonths",
                getOutingMonthsFor(
                    instance,
                    swaggerApiClientForLoggedInUser.getSquad(outing.squad.id),
                    swaggerApiClientForLoggedInUser
                )
            ).addObject("month", ISODateTimeFormat.yearMonth().print(outing.date))
            .addObject("canAddOuting", permissionsService.hasPermission(loggedInMember, Permission.ADD_OUTING))
    }

    @Throws(ApiException::class)
    private fun getOutingMonthsFor(
        instance: Instance,
        squad: Squad,
        swaggerApiClientForLoggedInUser: DefaultApi
    ): List<String> {
        val stringBigDecimalMap = swaggerApiClientForLoggedInUser.outingsMonthsGet(
            instance.id,
            squad.id,
            DateTime.now().toDateMidnight().minusDays(1).toLocalDate(),
            DateTime.now().plusYears(20).toLocalDate()
        ) // TODO timezone
        return Lists.newArrayList(stringBigDecimalMap.keys).sorted()
    }

    @Throws(ApiException::class)
    private fun buildOutingFromOutingDetails(
        outingDetails: OutingDetails?,
        instance: Instance,
        api: DefaultApi
    ): Outing {
        val date = outingDetails!!.toLocalTime().toDateTime(DateTimeZone.forID(instance.timeZone))
        val squad = if (outingDetails.squad != null) api.getSquad(outingDetails.squad) else null // TODO validation
        val notes = outingDetails.notes
        return Outing().squad(squad).date(date).notes(notes)
    }

}