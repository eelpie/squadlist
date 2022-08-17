package uk.co.squadlist.web.controllers

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.YearMonth
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.ApiException
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.*
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.services.PreferredSquadService
import uk.co.squadlist.web.services.filters.ActiveMemberFilter
import uk.co.squadlist.web.views.CsvOutputRenderer
import uk.co.squadlist.web.views.DateFormatter
import uk.co.squadlist.web.views.NavItemsBuilder
import uk.co.squadlist.web.views.ViewFactory
import uk.co.squadlist.web.views.model.DisplayMember
import javax.servlet.http.HttpServletResponse

@Controller
class AvailabilityController @Autowired constructor(
    private val preferredSquadService: PreferredSquadService,
    private val viewFactory: ViewFactory,
    private val activeMemberFilter: ActiveMemberFilter,
    private val displayMemberFactory: DisplayMemberFactory,
    private val navItemsBuilder: NavItemsBuilder,
    private val csvOutputRenderer: CsvOutputRenderer,
    private val permissionsService: PermissionsService,
    instanceConfig: InstanceConfig,
    loggedInUserService: LoggedInUserService
) : WithSignedInUser(instanceConfig, loggedInUserService) {

    @RequestMapping("/availability")
    fun availability(): ModelAndView {
        val renderAvailabilityPage =
            { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
                val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
                val navItems = navItemsBuilder.navItemsFor(
                    loggedInMember,
                    "availability",
                    swaggerApiClientForLoggedInUser,
                    instance,
                    squads
                )
                viewFactory.getViewFor("availability", instance).addObject("title", "Availability")
                    .addObject("navItems", navItems)
                    .addObject("squads", swaggerApiClientForLoggedInUser.getSquads(instance.id))
            }
        return withSignedInMember(renderAvailabilityPage)
    }

    @RequestMapping("/availability/{squadId}")
    fun squadAvailability(
        @PathVariable squadId: String?,
        @RequestParam(value = "month", required = false) month: String?,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ModelAndView? {
        val renderAvailabilitySquadPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
            val navItems = navItemsBuilder.navItemsFor(
                loggedInMember,
                "availability",
                swaggerApiClientForLoggedInUser,
                instance,
                squads
            )
            val squad = preferredSquadService.resolveSquad(squadId, swaggerApiClientForLoggedInUser, instance)
            if (squad != null) {
                val squadMembers = swaggerApiClientForLoggedInUser.getSquadMembers(squad.id)
                val activeSquadMembers = activeMemberFilter.extractActive(squadMembers)
                val yearMonth =
                    if (month != null) YearMonth.parse(month) else null // TODO push to Spring parameter?
                val dateRange = DateRange.from(yearMonth, startDate, endDate)
                val dateTimeZone = DateTimeZone.forID(instance.timeZone)
                val startDateTime = dateRange.start!!.toDateTimeAtStartOfDay(dateTimeZone)
                val endDateTime = dateRange.end!!.toDateTimeAtStartOfDay(dateTimeZone).plusDays(1)
                val outings =
                    swaggerApiClientForLoggedInUser.outingsGet(instance.id, squad.id, startDateTime, endDateTime)
                val memberOutingAvailabilityMap = decorateOutingsWithMembersAvailability(
                    squad,
                    startDateTime,
                    endDateTime,
                    swaggerApiClientForLoggedInUser
                )
                val showExport =
                    permissionsService.hasSquadPermission(
                        loggedInMember,
                        Permission.VIEW_SQUAD_ENTRY_DETAILS,
                        squad
                    )
                val outingMonths = getOutingMonthsFor(instance, squad, swaggerApiClientForLoggedInUser).sorted()

                var title = squad.name + " availability"
                if (dateRange.month != null) {
                    title = squad.name + " availability - " + dateRange.month.toString("MMMMM yyyy")
                }
                viewFactory.getViewFor("availability", instance).addObject("squads", squads)
                    .addObject("title", title)
                    .addObject("navItems", navItems).addObject("squad", squad)
                    .addObject("members", displayMemberFactory.toDisplayMembers(activeSquadMembers, loggedInMember))
                    .addObject("outings", outings).addObject("squadAvailability", memberOutingAvailabilityMap)
                    .addObject("outingMonths", outingMonths).addObject("current", dateRange.isCurrent())
                    .addObject("squad", squad).addObject("dateRange", dateRange).addObject("showExport", showExport)

            } else {
                throw java.lang.RuntimeException("Squad no found")  // TODO more graceful exit
            }
        }

        return withSignedInMember(renderAvailabilitySquadPage)
    }

    @RequestMapping("/availability/{squadId}.csv")
    fun squadAvailabilityCsv(
        @PathVariable squadId: String?,
        @RequestParam(value = "month", required = false) month: String?,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        response: HttpServletResponse?) {
        val renderAvailabilitySquadCsv = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squad = preferredSquadService.resolveSquad(squadId, swaggerApiClientForLoggedInUser, instance)
            if (squad != null) {
                val squadMembers = swaggerApiClientForLoggedInUser.getSquadMembers(squad.id)
                val activeSquadMembers = activeMemberFilter.extractActive(squadMembers)
                val yearMonth = if (month != null) YearMonth.parse(month) else null // TODO push to Spring parameter?
                val (start, end) = DateRange.from(yearMonth, startDate, endDate)
                val dateTimeZone = DateTimeZone.forID(instance.timeZone)

                val startDateTime = start!!.toDateTimeAtStartOfDay(dateTimeZone)
                val endDateTime = end!!.toDateTimeAtStartOfDay(dateTimeZone).plusDays(1)
                val outings =
                    swaggerApiClientForLoggedInUser.outingsGet(instance.id, squad.id, startDateTime, endDateTime)
                val memberOutingAvailabilityMap =
                    decorateOutingsWithMembersAvailability(
                        squad,
                        startDateTime,
                        endDateTime,
                        swaggerApiClientForLoggedInUser
                    )
                val dateFormatter = DateFormatter(DateTimeZone.forID(instance.timeZone))

                // Headings are Name and the outing dates
                // Second row is the outing notes
                val headings = Lists.newArrayList("Name")
                val notes: MutableList<String?> = Lists.newArrayList()
                notes.add(null)
                for (outing in outings) {
                    headings.add(dateFormatter.dayMonthYearTime(outing.date))
                    notes.add(outing.notes)
                }

                // Iterate through the rows of squad members, outputting rows of outing availability
                val rows: MutableList<List<String?>> = Lists.newArrayList()
                rows.add(notes)
                for (member in activeSquadMembers) {
                    val displayMember = DisplayMember(member, false)
                    val cells: MutableList<String?> = Lists.newArrayList()
                    cells.add(displayMember.displayName)
                    for (outing in outings) {
                        val availabilityOption = memberOutingAvailabilityMap[outing.id + "-" + member.id]
                        cells.add(if (availabilityOption != null) availabilityOption.label else "")
                    }
                    rows.add(cells)
                }
                csvOutputRenderer.renderCsvResponse(response, headings, rows)
            }

            ModelAndView()// TODO throw away return
        }

        withSignedInMember(renderAvailabilitySquadCsv)
    }

    @Throws(SignedInMemberRequiredException::class, ApiException::class)
    private fun decorateOutingsWithMembersAvailability(
        squad: Squad,
        startDate: DateTime,
        endDate: DateTime,
        swaggerApiClientForLoggedInUser: DefaultApi
    ): Map<String, AvailabilityOption?> {
        val squadAvailability = swaggerApiClientForLoggedInUser.getSquadAvailability(squad.id, DateTime(startDate), DateTime(endDate))
        val allAvailability: MutableMap<String, AvailabilityOption?> = Maps.newHashMap()
        for (outingWithSquadAvailability in squadAvailability) {
            val outingAvailability = outingWithSquadAvailability.availability
            for (memberId in outingAvailability.keys) {
                val key =
                    outingWithSquadAvailability.outing.id + "-" + memberId // TODO this magic format is very questionable
                allAvailability[key] = outingAvailability[memberId]
            }
        }
        return allAvailability
    }

    // TODO duplication with outings controller
    @Throws(ApiException::class)
    private fun getOutingMonthsFor(instance: Instance, squad: Squad, swaggerApiClientForLoggedInUser: DefaultApi): List<String> {
        val stringBigDecimalMap = swaggerApiClientForLoggedInUser.outingsMonthsGet(
            instance.id,
            squad.id,
            DateTime.now().toDateMidnight().minusDays(1).toLocalDate(),
            DateTime.now().plusYears(20).toLocalDate()
        ) // TODO timezone
        return Lists.newArrayList(stringBigDecimalMap.keys).sorted()
    }

}