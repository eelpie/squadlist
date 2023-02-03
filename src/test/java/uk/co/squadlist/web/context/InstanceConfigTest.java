package uk.co.squadlist.web.context;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstanceConfigTest {

	private final RequestHostService requestHostService = mock(RequestHostService.class);

	@Test
	public void liveMultiTenantedInstanceShouldBeInferredFromTheRequestHostname() {
		InstanceConfig instanceConfig =  new InstanceConfig(requestHostService, null);

		when(requestHostService.getRequestHost()).thenReturn("aninstance.squadlist.co.uk");

		assertEquals("aninstance", instanceConfig.getInstance());
	}

	@Test
	public void shouldHandleHyphensInIds() {
		InstanceConfig instanceConfig =  new InstanceConfig(requestHostService, null);

		when(requestHostService.getRequestHost()).thenReturn("an-instance.squadlist.co.uk");

		assertEquals("an-instance", instanceConfig.getInstance());
	}

	@Test
	public void inDevelopmentEnvironmentsWeCanHardCodeTheInstanceNameSoThatWeCanDeployOnNonLiveUrls() {
		InstanceConfig instanceConfigWithManualInstance = new InstanceConfig(requestHostService, "manuallyconfiguredinstance");

		when(requestHostService.getRequestHost()).thenReturn("aninstance.squadlist.co.uk");

		assertEquals("manuallyconfiguredinstance", instanceConfigWithManualInstance.getInstance());
	}

}