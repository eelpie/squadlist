package uk.co.squadlist.web.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.Member;

public class PermissionsServiceTest {

	private static final String ADMIN_ID = "admin-id";
	private static final String COACH_ID = "coach-id";
	
	@Mock
	private InstanceSpecificApiClient api;

	private Member admin;
	private Member coach;

	private PermissionsService permissionsService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		this.admin = new Member();
		admin.setId(ADMIN_ID);
		admin.setAdmin(true);
		
		this.coach = new Member();
		coach.setId(COACH_ID);
		coach.setAdmin(false);
		coach.setRole("Coach");
		
		this.permissionsService = new PermissionsService(api);
	}

	@Test
	public void adminsCanEditMemberDetails() throws Exception {
		when(api.getMemberDetails(ADMIN_ID)).thenReturn(admin);
		
		assertTrue(permissionsService.hasMemberPermission(ADMIN_ID, Permission.EDIT_MEMBER_DETAILS, "auser"));
	}
	@Test
	public void coachesCanEditMemberDetails() throws Exception {
		when(api.getMemberDetails(COACH_ID)).thenReturn(coach);
		
		assertTrue(permissionsService.hasMemberPermission(COACH_ID, Permission.EDIT_MEMBER_DETAILS, "auser"));
	}
	
}
