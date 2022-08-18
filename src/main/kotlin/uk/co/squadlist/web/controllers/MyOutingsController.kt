package uk.co.squadlist.web.controllers

import com.google.common.base.Strings
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.Availability
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.model.swagger.Member
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.exceptions.OutingClosedException
import uk.co.squadlist.web.services.OutingAvailabilityCountsService
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.DateHelper
import uk.co.squadlist.web.views.NavItemsBuilder
import uk.co.squadlist.web.views.TextHelper
import uk.co.squadlist.web.views.ViewFactory

@Controller
class MyOutingsController @Autowired constructor(
    private val viewFactory: ViewFactory,
    private val outingAvailabilityCountsService: OutingAvailabilityCountsService,
    private val urlBuilder: UrlBuilder,
    private val navItemsBuilder: NavItemsBuilder,
    private val textHelper: TextHelper,
    loggedInUserService: LoggedInUserService,
    permissionsService: PermissionsService,
    instanceConfig: InstanceConfig): WithSignedInUser(instanceConfig, loggedInUserService, permissionsService) {

    private val log = LogManager.getLogger(MyOutingsController::class.java)

    @GetMapping("/")
    fun myOutings(): ModelAndView {
        val renderMyOutingsPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
            val startDate = DateHelper.startOfCurrentOutingPeriod()
            val endDate = DateHelper.oneYearFromNow()
            val availabilityFor = swaggerApiClientForLoggedInUser.getMemberAvailability(loggedInMember.id, startDate, endDate)
            val navItems = navItemsBuilder.navItemsFor(loggedInMember,"my.outings", swaggerApiClientForLoggedInUser, instance, squads)

            viewFactory.getViewFor("myOutings", instance)
                .addObject("member", swaggerApiClientForLoggedInUser.getMember(loggedInMember.id))
                .addObject("outings", availabilityFor).addObject("title", textHelper.text("my.outings"))
                .addObject("navItems", navItems).addObject("availabilityOptions", swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsGet(instance.id))
                .addObject("rssUrl", urlBuilder.outingsRss(loggedInMember.id, instance))
                .addObject("icalUrl", urlBuilder.outingsIcal(loggedInMember.id, instance))
        }

        return withSignedInMember(renderMyOutingsPage)
    }

    @PostMapping("/availability/ajax")
    fun updateAvailability(@RequestParam(value = "outing", required = true) outingId: String?, @RequestParam(value = "availability", required = true) availability: String?): ModelAndView {
        val handleUpdateAvailability = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val outing = swaggerApiClientForLoggedInUser.getOuting(outingId)
            if (!outing.isClosed) {
                val availabilityOption =
                    if (!Strings.isNullOrEmpty(availability)) swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdGet(
                        instance.id,
                        availability
                    ) else null
                val body =
                    Availability().availabilityOption(availabilityOption).member(loggedInMember).outing(outing)
                log.info("Setting availability for " + loggedInMember.username + " / " + outing.id + " to " + availabilityOption)
                val result = swaggerApiClientForLoggedInUser.setOutingAvailability(body, outing.id)
                log.info("Set availability result: $result")
                viewFactory.getViewFor("includes/availability", instance).addObject("availability", result.availabilityOption)
            } else {
                throw OutingClosedException()
            }
        }
        return withSignedInMember(handleUpdateAvailability)
    }

    @PostMapping("/myoutings/ajax")
    fun ajax(): ModelAndView {
        val handleMyOutingsAjax = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val mv = viewFactory.getViewFor("myOutingsAjax", instance)
            val pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(
                loggedInMember.id,
                swaggerApiClientForLoggedInUser
            )
            if (pendingOutingsCountFor > 0) {
                mv.addObject("pendingOutingsCount", pendingOutingsCountFor)
            }
            mv
        }

        return withSignedInMember(handleMyOutingsAjax)
    }

}