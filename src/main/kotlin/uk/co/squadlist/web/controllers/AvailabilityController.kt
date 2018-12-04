package uk.co.squadlist.web.controllers

import com.google.common.collect.Maps
import org.joda.time.format.ISODateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.api.SquadlistApi
import uk.co.squadlist.web.api.SquadlistApiFactory
import uk.co.squadlist.web.model.AvailabilityOption
import uk.co.squadlist.web.model.Outing
import uk.co.squadlist.web.model.OutingWithSquadAvailability
import uk.co.squadlist.web.services.PreferedSquadService
import uk.co.squadlist.web.services.filters.ActiveMemberFilter
import uk.co.squadlist.web.views.DateHelper
import uk.co.squadlist.web.views.ViewFactory

@Controller
class AvailabilityController(private val instanceSpecificApiClient: InstanceSpecificApiClient, private val preferedSquadService: PreferedSquadService,
                             private val viewFactory: ViewFactory,
                             private val activeMemberFilter: ActiveMemberFilter, squadlistApiFactory: SquadlistApiFactory) {

    private val squadlistApi: SquadlistApi = squadlistApiFactory.createClient()

    @GetMapping("/availability")
    fun availability(): ModelAndView {
        return viewFactory.getViewForLoggedInUser("availability").
                addObject("squads", instanceSpecificApiClient.squads)
    }

    @GetMapping("/availability/{squadId}")
    fun squadAvailability(@PathVariable squadId: String, @RequestParam(value = "month", required = false) month: String?): ModelAndView {
        val mv = viewFactory.getViewForLoggedInUser("availability")
        mv.addObject("squads", instanceSpecificApiClient.squads)

        val squad = preferedSquadService.resolveSquad(squadId)

        if (squad != null) {
            mv.addObject("squad", squad)
            mv.addObject("title", squad.name + " availability")
            mv.addObject("members", activeMemberFilter.extractActive(squadlistApi.getSquadMembers(squad.id)))

            if (squadlistApi.getSquadMembers(squad.id).isEmpty()) {
                return mv
            }

            var startDate = DateHelper.startOfCurrentOutingPeriod().toDate()
            var endDate = DateHelper.endOfCurrentOutingPeriod().toDate()
            if (month != null) {
                val monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month)    // TODO Can be moved to spring?
                startDate = monthDateTime.toDate()
                endDate = monthDateTime.plusMonths(1).toDate()
            } else {
                mv.addObject("current", true)
            }

            val squadAvailability = squadlistApi.getSquadAvailability(squad.id, startDate, endDate)
            val outings = instanceSpecificApiClient.getSquadOutings(squad, startDate, endDate)

            mv.addObject("squadAvailability", decorateOutingsWithMembersAvailability(squadAvailability, outings))
            mv.addObject("outings", outings)
            mv.addObject("outingMonths", instanceSpecificApiClient.getOutingMonths(squad))
            mv.addObject("month", month)
        }
        return mv
    }

    private fun decorateOutingsWithMembersAvailability(squadAvailability: List<OutingWithSquadAvailability>, outings: List<Outing>): Map<String, AvailabilityOption> {
        val allAvailability = Maps.newHashMap<String, AvailabilityOption>()
        for (outingWithSquadAvailability in squadAvailability) {
            val outingAvailability = outingWithSquadAvailability.availability
            for (member in outingAvailability.keys) {
                allAvailability[outingWithSquadAvailability.outing.id + "-" + member] = outingAvailability[member]
            }
        }
        return allAvailability
    }

}
