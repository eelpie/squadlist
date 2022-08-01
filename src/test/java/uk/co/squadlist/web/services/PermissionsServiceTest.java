package uk.co.squadlist.web.services;

import org.junit.Test;
import uk.co.squadlist.model.swagger.Member;

import static org.junit.Assert.assertTrue;

public class PermissionsServiceTest {

	private static final String ADMIN_ID = "admin-id";
	private static final String COACH_ID = "coach-id";
	private static final String ROWER_ID = "auser";

	private final Member admin = new Member().id(ADMIN_ID).admin(true);
	private final Member coach = new Member().id(COACH_ID).admin(false).role("Coach");
	private final Member rower = new Member().id(ROWER_ID).admin(false).role("Rower");

	private final PermissionsService permissionsService = new PermissionsService();

	@Test
	public void adminsCanEditMemberDetails() {
		assertTrue(permissionsService.hasMemberPermission(admin, Permission.EDIT_MEMBER_DETAILS, rower));
	}
	
	@Test
	public void coachesCanEditMemberDetails() {
		assertTrue(permissionsService.hasMemberPermission(coach, Permission.EDIT_MEMBER_DETAILS, rower));
	}
	
	@Test
	public void roweerCanEditThereOwnMemberDetails() {
		assertTrue(permissionsService.hasMemberPermission(rower, Permission.EDIT_MEMBER_DETAILS, rower));
	}
	
}