package uk.co.squadlist.web.controllers;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.eelpieconsulting.common.views.EtagGenerator;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.model.swagger.OutingWithAvailability;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.RssOuting;
import uk.co.squadlist.web.views.SquadNamesHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
public class FeedsController {

    private final InstanceConfig instanceConfig;
    private final UrlBuilder urlBuilder;
    private final SquadNamesHelper squadNamesHelper;
    private final DefaultApi squadlistSwaggerApi;

    @Autowired
    public FeedsController(InstanceConfig instanceConfig, UrlBuilder urlBuilder,
                           SquadlistApiFactory squadlistApiFactory, SquadNamesHelper squadNamesHelper) throws IOException {
        this.instanceConfig = instanceConfig;
        this.urlBuilder = urlBuilder;
        this.squadNamesHelper = squadNamesHelper;
        this.squadlistSwaggerApi = squadlistApiFactory.createSwaggerClient();
    }

    @RequestMapping("/ical")
    public void outingsIcal(@RequestParam String user, HttpServletResponse response) throws Exception {
        final String calendar = squadlistSwaggerApi.getMemberOutingsCalendar(user,
                DateHelper.startOfCurrentOutingPeriod(),
                DateHelper.oneYearFromNow());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/calendar");

        final PrintWriter writer = response.getWriter();
        writer.println(calendar);
        writer.flush();
    }

    @RequestMapping("/rss")
    public ModelAndView outingsRss(@RequestParam(value = "user", required = true) String user) throws Exception {
        final Member member = squadlistSwaggerApi.getMember(user);
        final Instance instance = squadlistSwaggerApi.getInstance(instanceConfig.getInstance());

        final List<OutingWithAvailability> availabilityFor = squadlistSwaggerApi.getMemberAvailability(member.getId(),
                DateHelper.startOfCurrentOutingPeriod(),
                DateHelper.oneYearFromNow());

        final String title = instance.getName() + " outings";

        return new ModelAndView(new uk.co.eelpieconsulting.common.views.ViewFactory(new EtagGenerator()).getRssView(title, urlBuilder.getBaseUrl(),
                squadNamesHelper.list(member.getSquads()) + " outings")).
                addObject("data", buildRssItemsFor(availabilityFor));
    }

    private List<RssOuting> buildRssItemsFor(final List<OutingWithAvailability> availabilityFor) {
        List<RssOuting> outings = Lists.newArrayList();
        for (OutingWithAvailability outingAvailability : availabilityFor) {
            outings.add(new RssOuting(outingAvailability.getOuting(), urlBuilder.outingUrl(outingAvailability.getOuting())));
        }
        return outings;
    }

}
