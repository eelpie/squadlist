package uk.co.squadlist.web.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.OutingAvailability;

public class MyOutingsControllerTest {

	private static final String MEMBER = "AMEMBER";
	
	@Mock private InstanceSpecificApiClient api;
	@Mock private LoggedInUserService loggedInUserService;
	
	@Mock private List<OutingAvailability> membersAvailability;

	private MyOutingsController controller;


	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		controller = new MyOutingsController(loggedInUserService, api);
	}
	
	@Test
	public void myOutingsShouldShowCurrentOutingsForTodayAndTheNextTwoWeeks() throws Exception {
		when(loggedInUserService.getLoggedInUser()).thenReturn(MEMBER);
		when(api.getAvailabilityFor(MEMBER, DateTime
						.now().minusDays(1).toDateMidnight().toDate(), DateTime
						.now().minusDays(1).toDateMidnight().plusWeeks(2).toDate()))
				.thenReturn(membersAvailability);
						
		final ModelAndView mv = controller.outings();
		
		assertEquals(membersAvailability, mv.getModel().get("outings"));		
	}
	
}
