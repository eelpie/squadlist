package uk.co.squadlist.web.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.model.Member;

import java.io.IOException;

public class PermissionsServiceTest {

	private static final String ADMIN_ID = "admin-id";
	private static final String COACH_ID = "coach-id";
	private static final String ROWER_ID = "auser";
	
	@Mock
	private InstanceSpecificApiClient instanceSpecificApiClient;
	@Mock
	private SquadlistApiFactory squadlistApiFactory;
	@Mock
	private SquadlistApi squadlistApi;

	private Member admin;
	private Member coach;
	private Member rower;

	private PermissionsService permissionsService;
	
	@Before
	public void setup() throws UnknownMemberException, IOException {
		MockitoAnnotations.initMocks(this);
		
		this.admin = new Member();
		admin.setId(ADMIN_ID);
		admin.setAdmin(true);
		
		this.coach = new Member();
		coach.setId(COACH_ID);
		coach.setAdmin(false);
		coach.setRole("Coach");
		
		this.rower = new Member();
		rower.setId(ROWER_ID);
		rower.setAdmin(false);
		rower.setRole("Rower");

		when(squadlistApiFactory.createClient()).thenReturn(squadlistApi);
				
		this.permissionsService = new PermissionsService(instanceSpecificApiClient, squadlistApiFactory);
	}

	@Test
	public void adminsCanEditMemberDetails() throws Exception {
		when(squadlistApi.getMember(ADMIN_ID)).thenReturn(admin);
		
		assertTrue(permissionsService.hasMemberPermission(admin, Permission.EDIT_MEMBER_DETAILS, ROWER_ID));
	}
	
	@Test
	public void coachesCanEditMemberDetails() throws Exception {
		when(squadlistApi.getMember(COACH_ID)).thenReturn(coach);
		
		assertTrue(permissionsService.hasMemberPermission(coach, Permission.EDIT_MEMBER_DETAILS, ROWER_ID));
	}
	
	@Test
	public void roweerCanEditThereOwnMemberDetails() throws Exception {
		when(squadlistApi.getMember(ROWER_ID)).thenReturn(rower);
		
		assertTrue(permissionsService.hasMemberPermission(rower, Permission.EDIT_MEMBER_DETAILS, ROWER_ID));
	}
	
}