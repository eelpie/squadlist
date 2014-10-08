package uk.co.squadlist.web.urls;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.co.squadlist.web.api.InstanceConfig;

public class UrlBuilderTest {

	@Mock
	private InstanceConfig instanceConfig;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(instanceConfig.getInstance()).thenReturn("twrc");				
	}
	
	@Test
	public void canInsertInstanceIntoBaseUrl() throws Exception {
		final UrlBuilder urlBuilder = new UrlBuilder("https://INSTANCEbeta.squadlist.co.uk", instanceConfig);	
		assertEquals("https://twrcbeta.squadlist.co.uk/meh", urlBuilder.applicationUrl("/meh"));
	}
	
	@Test
	public void canComposeWebcalFeedUrlFromBaseUrl() throws Exception {		
		final UrlBuilder urlBuilder = new UrlBuilder("http://localhost", instanceConfig);
		assertEquals("webcal://localhost/ical?user=a-user", urlBuilder.outingsIcal("a-user"));
	}
	
	@Test
	public void webcalFeedsAreHttpEvenIfTheMainSiteIsHttps() throws Exception {
		final UrlBuilder urlBuilder = new UrlBuilder("https://INSTANCEbeta.squadlist.co.uk", instanceConfig);
		assertEquals("webcal://twrcbeta.squadlist.co.uk/ical?user=a-user", urlBuilder.outingsIcal("a-user"));
	}
	
	@Test
	public void canComposeRssFeedUrlFromBaseUrl() throws Exception {		
		final UrlBuilder urlBuilder = new UrlBuilder("https://INSTANCEbeta.squadlist.co.uk", instanceConfig);
		assertEquals("https://twrcbeta.squadlist.co.uk/rss?user=a-user", urlBuilder.outingsRss("a-user"));
	}
	
}