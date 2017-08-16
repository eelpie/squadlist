package uk.co.squadlist.web.urls;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.localisation.BritishRowing;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Subscription;
import uk.co.squadlist.web.model.Tariff;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class UrlBuilderTest {

	@Mock
	private InstanceConfig instanceConfig;
	private SeoLinkBuilder seoLinkBuilder;

	private Instance instance;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(instanceConfig.getVhost()).thenReturn("twrc");
		seoLinkBuilder = new SeoLinkBuilder();

		instance = new Instance("aninstance", "An instance", null, true, Tariff.PRE_JUNE_2015, Lists.<Subscription>newArrayList(), "british-rowing");
	}
	
	@Test
	public void canInsertInstanceIntoBaseUrl() throws Exception {
		final UrlBuilder urlBuilder = new UrlBuilder("https://INSTANCE.squadlist.co.uk", instanceConfig, seoLinkBuilder, null, null);
		assertEquals("https://twrc.squadlist.co.uk/meh", urlBuilder.applicationUrl("/meh"));
	}

	@Test
	public void canComposeWebcalFeedUrlFromBaseUrl() throws Exception {
		final UrlBuilder urlBuilder = new UrlBuilder("http://localhost", instanceConfig, seoLinkBuilder, null, null);
		assertEquals("webcal://localhost/ical?user=a-user&key=9329bb2beff6556bf2698163732bb9a2", urlBuilder.outingsIcal("a-user", instance));
	}

	@Test
	public void webcalFeedsAreHttpEvenIfTheMainSiteIsHttps() throws Exception {
		final UrlBuilder urlBuilder = new UrlBuilder("https://INSTANCE.squadlist.co.uk", instanceConfig, seoLinkBuilder, null, null);
		assertEquals("webcal://twrc.squadlist.co.uk/ical?user=a-user&key=9329bb2beff6556bf2698163732bb9a2", urlBuilder.outingsIcal("a-user", instance));
	}

	@Test
	public void canComposeRssFeedUrlFromBaseUrl() throws Exception {
		final UrlBuilder urlBuilder = new UrlBuilder("https://INSTANCE.squadlist.co.uk", instanceConfig, seoLinkBuilder, null, null);
		assertEquals("https://twrc.squadlist.co.uk/rss?user=a-user&key=9329bb2beff6556bf2698163732bb9a2", urlBuilder.outingsRss("a-user", instance));
	}

	@Test
	public void canLinkToGoverningBodyPage() throws Exception {
		final UrlBuilder urlBuilder = new UrlBuilder("https://localhost", instanceConfig, seoLinkBuilder, null, null);
		assertEquals("https://localhost/governing-body/british-rowing", urlBuilder.governingBody(new BritishRowing()));
	}

	@Test
	public void canComposeMailtoListUrl() throws Exception {
		final UrlBuilder urlBuilder = new UrlBuilder("https://localhost", instanceConfig, seoLinkBuilder, null, null);
		final List<String> emails = Lists.newArrayList("auser@localhost", "anotheruser@localhost");
		assertEquals("mailto:auser@localhost,anotheruser@localhost", urlBuilder.mailto(emails));
	}

}