package uk.co.squadlist.web.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InstanceConfigTest {

	@Mock
	private RequestHostService requestHostService;
	
	private InstanceConfig instanceConfig;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		instanceConfig = new InstanceConfig(requestHostService, null);
	}
	
	@Test
	public void liveMultiTenantedInstanceShouldBeInferredFromTheRequestHostname() throws Exception {		
		when(requestHostService.getRequestHost()).thenReturn("aninstance.squadlist.co.uk");
		
		assertEquals("aninstance", instanceConfig.getInstance());		
	}
	
	@Test
	public void shouldAlsoRecogniseBetaInstances() throws Exception {		
		when(requestHostService.getRequestHost()).thenReturn("shinynewbeta.squadlist.co.uk");
		
		assertEquals("shinynew", instanceConfig.getInstance());		
	}
	
	@Test
	public void needToBeCarefulAboutLeadingBetas() throws Exception {		
		when(requestHostService.getRequestHost()).thenReturn("reallycalledbetasomething.squadlist.co.uk");
		
		assertEquals("reallycalledbetasomething", instanceConfig.getInstance());		
	}
		
}
