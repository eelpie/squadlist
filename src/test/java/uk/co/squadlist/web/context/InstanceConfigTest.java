package uk.co.squadlist.web.context;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.context.RequestHostService;

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
	public void shouldHandleHyphensInIds() throws Exception {
		when(requestHostService.getRequestHost()).thenReturn("an-instance.squadlist.co.uk");

		assertEquals("an-instance", instanceConfig.getInstance());
	}

	@Test
	public void inDevelopmentEnvironmentsWeCanHardCodeTheInstanceNameSoThatWeCanDeployOnNonLiveUrls() throws Exception {
		instanceConfig = new InstanceConfig(requestHostService, "manuallyconfiguredinstance");

		when(requestHostService.getRequestHost()).thenReturn("aninstance.squadlist.co.uk");

		assertEquals("manuallyconfiguredinstance", instanceConfig.getInstance());
	}

}