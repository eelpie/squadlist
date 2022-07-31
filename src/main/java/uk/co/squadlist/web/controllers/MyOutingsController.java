package uk.co.squadlist.web.controllers;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.OutingWithAvailability;
import uk.co.squadlist.web.annotations.RequiresSignedInMember;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.NavItemsBuilder;
import uk.co.squadlist.web.views.TextHelper;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import java.util.List;

@Controller
public class MyOutingsController {

    private final LoggedInUserService loggedInUserService;
    private final ViewFactory viewFactory;
    private final OutingAvailabilityCountsService outingAvailabilityCountsService;
    private final UrlBuilder urlBuilder;
    private final NavItemsBuilder navItemsBuilder;
    private final TextHelper textHelper;
    private final InstanceConfig instanceConfig;

    @Autowired
    public MyOutingsController(LoggedInUserService loggedInUserService,
                               ViewFactory viewFactory,
                               OutingAvailabilityCountsService outingAvailabilityCountsService,
                               UrlBuilder urlBuilder,
                               NavItemsBuilder navItemsBuilder,
                               TextHelper textHelper,
                               InstanceConfig instanceConfig) {
        this.loggedInUserService = loggedInUserService;
        this.viewFactory = viewFactory;
        this.outingAvailabilityCountsService = outingAvailabilityCountsService;
        this.urlBuilder = urlBuilder;
        this.navItemsBuilder = navItemsBuilder;
        this.textHelper = textHelper;
        this.instanceConfig = instanceConfig;
    }

    @RequiresSignedInMember
    @RequestMapping("/")
    public ModelAndView outings() throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        DateTime startDate = DateHelper.startOfCurrentOutingPeriod();
        DateTime endDate = DateHelper.oneYearFromNow();
        List<OutingWithAvailability> availabilityFor = swaggerApiClientForLoggedInUser.getMemberAvailability(loggedInUser.getId(), startDate, endDate);
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, "my.outings", swaggerApiClientForLoggedInUser, instance);

        return viewFactory.getViewFor("myOutings", instance).
                addObject("member", swaggerApiClientForLoggedInUser.membersIdGet(loggedInUser.getId())).
                addObject("outings", availabilityFor).
                addObject("title", textHelper.text("my.outings")).
                addObject("navItems", navItems).
                addObject("availabilityOptions", swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsGet(instance.getId())).
                addObject("rssUrl", urlBuilder.outingsRss(loggedInUser.getId(), instance)).
                addObject("icalUrl", urlBuilder.outingsIcal(loggedInUser.getId(), instance));
    }

    @RequiresSignedInMember
    @RequestMapping("/myoutings/ajax")
    public ModelAndView ajax() throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final ModelAndView mv = viewFactory.getViewFor("myOutingsAjax", instance);
        int pendingOutingsCountFor = outingAvailabilityCountsService.getPendingOutingsCountFor(loggedInMember.getId(), swaggerApiClientForLoggedInUser);
        if (pendingOutingsCountFor > 0) {
            mv.addObject("pendingOutingsCount", pendingOutingsCountFor);
        }
        return mv;
    }

}
