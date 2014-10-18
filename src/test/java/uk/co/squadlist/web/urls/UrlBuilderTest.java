package uk.co.squadlist.web.urls;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.co.squadlist.web.api.InstanceConfig;
import uk.co.squadlist.web.localisation.BritishRowing;

public class UrlBuilderTest {

	@Mock
	private InstanceConfig instanceConfig;
	private SeoLinkBuilder seoLinkBuilder;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(instanceConfig.getInstance()).thenReturn("twrc");
		seoLinkBuilder = new SeoLinkBuilder();
	}
	
	@Test
	public void canInsertInstanceIntoBaseUrl() throws Exception {
		final UrlBuilder urlBuilder = new UrlBuilder("https://INSTANCEbeta.squadlist.co.uk", instanceConfig, seoLinkBuilder);	
		assertEquals("https://twrcbeta.squadlist.co.uk/meh", urlBuilder.applicationUrl("/meh"));
	}
	
	@Test
	public void canComposeWebcalFeedUrlFromBaseUrl() throws Exception {		
		final UrlBuilder urlBuilder = new UrlBuilder("http://localhost", instanceConfig, seoLinkBuilder);
		assertEquals("webcal://localhost/ical?user=a-user", urlBuilder.outingsIcal("a-user"));
	}
	
	@Test
	public void webcalFeedsAreHttpEvenIfTheMainSiteIsHttps() throws Exception {
		final UrlBuilder urlBuilder = new UrlBuilder("https://INSTANCEbeta.squadlist.co.uk", instanceConfig, seoLinkBuilder);
		assertEquals("webcal://twrcbeta.squadlist.co.uk/ical?user=a-user", urlBuilder.outingsIcal("a-user"));
	}
	
	@Test
	public void canComposeRssFeedUrlFromBaseUrl() throws Exception {		
		final UrlBuilder urlBuilder = new UrlBuilder("https://INSTANCEbeta.squadlist.co.uk", instanceConfig, seoLinkBuilder);
		assertEquals("https://twrcbeta.squadlist.co.uk/rss?user=a-user", urlBuilder.outingsRss("a-user"));
	}
	
	@Test
	public void canLinkToGoverningBodyPage() throws Exception {
		final UrlBuilder urlBuilder = new UrlBuilder("https://localhost", instanceConfig, seoLinkBuilder);
		assertEquals("https://localhost/governing-body/british-rowing", urlBuilder.governingBody(new BritishRowing()));
	}
	
}