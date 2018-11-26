package uk.co.squadlist.web.controllers

import com.google.common.base.Strings
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.web.annotations.RequiresSignedInMember
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.api.SquadlistApiFactory
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.exceptions.UnknownMemberException
import uk.co.squadlist.web.services.OutingAvailabilityCountsService
import uk.co.squadlist.web.services.ical.OutingCalendarService
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.DateHelper
import uk.co.squadlist.web.views.PermissionsHelper
import uk.co.squadlist.web.views.ViewFactory
import javax.servlet.http.HttpServletResponse

@Controller
class MyOutingsController(val loggedInUserService: LoggedInUserService, val instanceSpecificApiClient: InstanceSpecificApiClient, val viewFactory: ViewFactory,
                          val outingAvailabilityCountsService: OutingAvailabilityCountsService, private val urlBuilder: UrlBuilder,
                          val outingCalendarService: OutingCalendarService, val squadlistApiFactory: SquadlistApiFactory, val permissionsHelper: PermissionsHelper) {

    private val squadlistApi = squadlistApiFactory.createClient()

    @RequiresSignedInMember
    @RequestMapping("/")
    fun outings(): ModelAndView {
        val loggedInUser = loggedInUserService.loggedInMember.id
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val mv = viewFactory.getViewForLoggedInUser("myOutings")
        mv.addObject("member", loggedInUserApi.getMember(loggedInUser))

        val startDate = DateHelper.startOfCurrentOutingPeriod().toDate()
        val endDate = DateHelper.oneYearFromNow().toDate()

        mv.addObject("outings", loggedInUserApi.getAvailabilityFor(loggedInUser, startDate, endDate))

        mv.addObject("title", "My outings")
        mv.addObject("availabilityOptions", instanceSpecificApiClient.availabilityOptions)
        mv.addObject("rssUrl", urlBuilder.outingsRss(loggedInUser, instanceSpecificApiClient.instance))
        mv.addObject("icalUrl", urlBuilder.outingsIcal(loggedInUser, instanceSpecificApiClient.instance))
        mv.addObject("permissionsHelper", permissionsHelper)
        mv.addObject("urlBuilder", urlBuilder)
        return mv
    }

    @RequestMapping("/ical")
    fun outingsIcal(@RequestParam(value = "user", required = false) user: String, response: HttpServletResponse) {
        if (Strings.isNullOrEmpty(user)) {
            throw UnknownMemberException()
        }

        val member = squadlistApi.getMember(user)

        val calendar = outingCalendarService.buildCalendarFor(squadlistApi.getAvailabilityFor(member.id,
                DateHelper.startOfCurrentOutingPeriod().toDate(),
                DateHelper.oneYearFromNow().toDate()),
                instanceSpecificApiClient.instance)

        response.status = HttpServletResponse.SC_OK
        response.contentType = "text/calendar"

        val writer = response.writer
        writer.println(calendar.toString())
        writer.flush()
    }

    @RequiresSignedInMember
    @RequestMapping("/myoutings/ajax")
    fun ajax(): ModelAndView {
        val mv = viewFactory.getViewForLoggedInUser("myOutingsAjax")
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)
        val pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUserService.loggedInMember.id, loggedInUserApi)
        if (pendingOutingsCountFor > 0) {
            mv.addObject("pendingOutingsCount", pendingOutingsCountFor)
        }
        return mv
    }

}
