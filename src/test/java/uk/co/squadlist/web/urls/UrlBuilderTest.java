package uk.co.squadlist.web.urls;

import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;
import org.junit.Before;
import org.junit.Test;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.Squad;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.controllers.DateRange;
import uk.co.squadlist.web.localisation.BritishRowing;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UrlBuilderTest {

    private InstanceConfig instanceConfig = mock(InstanceConfig.class);

    private SeoLinkBuilder seoLinkBuilder = new SeoLinkBuilder();

    private Instance instance = new Instance().id("aninstance").name("An instance").governingBody("british-rowing");

    private final UrlBuilder urlBuilder = new UrlBuilder("https://twrc.squadlist.app", instanceConfig, seoLinkBuilder, null, null);


    @Before
    public void setup() {
        when(instanceConfig.getVhost()).thenReturn("twrc");
    }

    @Test
    public void canInsertInstanceIntoBaseUrl() {
        assertEquals("https://twrc.squadlist.app/meh", urlBuilder.applicationUrl("/meh"));
    }

    @Test
    public void canComposeWebcalFeedUrlFromBaseUrl() throws Exception {
        assertEquals("webcal://twrc.squadlist.app/ical?user=a-user&key=9329bb2beff6556bf2698163732bb9a2", urlBuilder.outingsIcal("a-user", instance));
    }

    @Test
    public void webcalFeedsAreHttpEvenIfTheMainSiteIsHttps() throws Exception {
        assertEquals("webcal://twrc.squadlist.app/ical?user=a-user&key=9329bb2beff6556bf2698163732bb9a2", urlBuilder.outingsIcal("a-user", instance));
    }

    @Test
    public void canComposeRssFeedUrlFromBaseUrl() throws Exception {
        assertEquals("https://twrc.squadlist.app/rss?user=a-user&key=9329bb2beff6556bf2698163732bb9a2", urlBuilder.outingsRss("a-user", instance));
    }

    @Test
    public void canLinkToGoverningBodyPage() {
        assertEquals("https://twrc.squadlist.app/governing-body/british-rowing", urlBuilder.governingBody(new BritishRowing()));
    }

    @Test
    public void canComposeMailtoListUrl() {
        final List<String> emails = Lists.newArrayList("auser@localhost", "anotheruser@localhost");
        assertEquals("mailto:auser@localhost,anotheruser@localhost", urlBuilder.mailto(emails));
    }

    @Test
    public void canComposeExportAvailabilityMonthUrl() {
        Squad squad = new Squad();
        squad.setId("a-squad");
        DateRange august2022 = new DateRange(null, null, new YearMonth(2022, 8), false);

        String url = urlBuilder.availabilityCsv(squad, august2022);

        assertEquals("https://twrc.squadlist.app/availability/a-squad.csv?month=2022-08", url);
    }

    @Test
    public void canComposeExportAvailabilitySpecificDateRangeUrl() {
        Squad squad = new Squad();
        squad.setId("a-squad");
        DateRange august2022 = new DateRange(
                new LocalDate(2022, 8, 1),
                new LocalDate(2022, 8, 8),
                null, false);

        String url = urlBuilder.availabilityCsv(squad, august2022);

        assertEquals("https://twrc.squadlist.app/availability/a-squad.csv?startDate=2022-08-01&endDate=2022-08-08", url);
    }

}