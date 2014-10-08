package uk.co.squadlist.web.urls;

import static org.junit.Assert.assertEquals;

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
	}

	@Test
	public void canComposeWebcalFeedUrlFromBaseUrl() throws Exception {		
		final UrlBuilder urlBuilder = new UrlBuilder("http://localhost", instanceConfig);
		assertEquals("webcal://localhost/ical?user=a-user", urlBuilder.outingsIcal("a-user"));
	}
	
	@Test
	public void webcalFeedsAreHttpEvenIfTheMainSiteIsHttps() throws Exception {
		final UrlBuilder urlBuilder = new UrlBuilder("https://localhost", instanceConfig);
		assertEquals("webcal://localhost/ical?user=a-user", urlBuilder.outingsIcal("a-user"));
	}
	
}
