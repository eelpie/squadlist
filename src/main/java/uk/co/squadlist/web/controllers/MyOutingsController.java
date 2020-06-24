package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.annotations.RequiresSignedInMember;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.ViewFactory;

import java.util.Date;
import java.util.List;

@Controller
public class MyOutingsController {

    private final LoggedInUserService loggedInUserService;
    private final ViewFactory viewFactory;
    private final OutingAvailabilityCountsService outingAvailabilityCountsService;
    private final UrlBuilder urlBuilder;

    @Autowired
    public MyOutingsController(LoggedInUserService loggedInUserService, ViewFactory viewFactory,
                               OutingAvailabilityCountsService outingAvailabilityCountsService, UrlBuilder urlBuilder) {
        this.loggedInUserService = loggedInUserService;
        this.viewFactory = viewFactory;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.urlBuilder = urlBuilder;
    }

    @RequiresSignedInMember
    @RequestMapping("/")
    public ModelAndView outings() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final String loggedInUser = loggedInUserService.getLoggedInMember().getId();
        Instance instance = loggedInUserApi.getInstance();

        final Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
        final Date endDate = DateHelper.oneYearFromNow().toDate();
        List<OutingAvailability> availabilityFor = loggedInUserApi.getAvailabilityFor(loggedInUser, startDate, endDate);

        return viewFactory.getViewForLoggedInUser("myOutings").
                addObject("member", loggedInUserApi.getMember(loggedInUser)).
                addObject("outings", availabilityFor).
                addObject("title", "My outings").
                addObject("availabilityOptions", loggedInUserApi.getAvailabilityOptions()).
                addObject("rssUrl", urlBuilder.outingsRss(loggedInUser, instance)).
                addObject("icalUrl", urlBuilder.outingsIcal(loggedInUser, instance));
    }

    @RequiresSignedInMember
    @RequestMapping("/myoutings/ajax")
    public ModelAndView ajax() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final ModelAndView mv = viewFactory.getViewForLoggedInUser("myOutingsAjax");
        int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInUserService.getLoggedInMember().getId(), loggedInUserApi);
        if (pendingOutingsCountFor > 0) {
            mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
        }
        return mv;
    }

}
