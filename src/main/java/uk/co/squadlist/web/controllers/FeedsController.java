package uk.co.squadlist.web.controllers;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.eelpieconsulting.common.views.EtagGenerator;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.OutingAvailability;
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
    private final SquadlistApi squadlistApi;
    private final SquadNamesHelper squadNamesHelper;
    private final DefaultApi squadlistSwaggerApi;

    @Autowired
    public FeedsController(InstanceConfig instanceConfig, UrlBuilder urlBuilder,
                           SquadlistApiFactory squadlistApiFactory, SquadNamesHelper squadNamesHelper) throws IOException {
        this.instanceConfig = instanceConfig;
        this.urlBuilder = urlBuilder;
        this.squadNamesHelper = squadNamesHelper;
        this.squadlistApi = squadlistApiFactory.createClient();
        this.squadlistSwaggerApi = squadlistApiFactory.createSwaggerClient();
    }

    @RequestMapping("/ical")
    public void outingsIcal(@RequestParam(value = "user", required = false) String user, HttpServletResponse response) throws Exception {
        if (Strings.isNullOrEmpty(user)) {
            throw new UnknownMemberException();
        }

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
    public ModelAndView outingsRss(@RequestParam(value = "user", required = false) String user) throws Exception {
        if (Strings.isNullOrEmpty(user)) {
            throw new UnknownMemberException();
        }

        final Member member = squadlistApi.getMember(user);
        final Instance instance = squadlistApi.getInstance(instanceConfig.getInstance());

        final List<OutingAvailability> availabilityFor = squadlistApi.getAvailabilityFor(member.getId(),
                DateHelper.startOfCurrentOutingPeriod().toDate(),
                DateHelper.oneYearFromNow().toDate());

        final String title = instance.getName() + " outings";

        return new ModelAndView(new uk.co.eelpieconsulting.common.views.ViewFactory(new EtagGenerator()).getRssView(title, urlBuilder.getBaseUrl(),
                squadNamesHelper.list(member.getSquads()) + " outings")).
                addObject("data", buildRssItemsFor(availabilityFor));
    }

    private List<RssOuting> buildRssItemsFor(final List<OutingAvailability> availabilityFor) {
        List<RssOuting> outings = Lists.newArrayList();
        for (OutingAvailability outingAvailability : availabilityFor) {
            outings.add(new RssOuting(outingAvailability.getOuting(), urlBuilder.outingUrl(outingAvailability.getOuting())));
        }
        return outings;
    }

}
