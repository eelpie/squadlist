package uk.co.squadlist.web.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.display.DisplayOutingAvailability;
import uk.co.squadlist.web.views.DisplayObjectFactory;

public class MyOutingsControllerTest {

	private static final String MEMBER = "AMEMBER";
	
	@Mock private SquadlistApi api;
	@Mock private LoggedInUserService loggedInUserService;
	@Mock private DisplayObjectFactory displayObjectFactory;
	
	@Mock private List<OutingAvailability> membersAvailability;
	@Mock private List<DisplayOutingAvailability> membersAvailabilityDisplayObjects;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void myOutingsShouldShowOutings() throws Exception {
		when(loggedInUserService.getLoggedInUser()).thenReturn(MEMBER);
		when(api.getAvailabilityFor(MEMBER)).thenReturn(membersAvailability);
		when(displayObjectFactory.makeDisplayObjectsFor(membersAvailability)).thenReturn(membersAvailabilityDisplayObjects);

		MyOutingsController controller = new MyOutingsController(loggedInUserService, api, displayObjectFactory);
						
		ModelAndView mv = controller.outings();
		
		assertEquals(membersAvailabilityDisplayObjects, mv.getModel().get("outings"));		
	}
	
}
