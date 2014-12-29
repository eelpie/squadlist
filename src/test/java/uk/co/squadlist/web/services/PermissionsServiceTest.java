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
	
	@Mock
	private InstanceSpecificApiClient api;
	private Member admin;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.admin = new Member();
		admin.setId(ADMIN_ID);
		admin.setAdmin(true);
	}

	@Test
	public void adminsCanEditMemberDetails() throws Exception {
		when(api.getMemberDetails(ADMIN_ID)).thenReturn(admin);
		
		final PermissionsService permissionsService = new PermissionsService(api);
		
		assertTrue(permissionsService.hasMemberPermission(ADMIN_ID, Permission.EDIT_MEMBER_DETAILS, "auser"));
	}
	
}
