package uk.co.squadlist.web.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import uk.co.squadlist.web.model.AvailabilityOption;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.Squad;

public class SquadlistApiIT {

	private static final String TEST_INSTANCE_PREFIX = "testit";

	@Test
	public void canCreateAndPopulateNewInstances() throws Exception {		
		final SquadlistApi api = new SquadlistApi("http://localhost:9090");
		
		final String instanceName = TEST_INSTANCE_PREFIX + System.currentTimeMillis();
		
		final List<Instance> instances = api.getInstances();
		System.out.println(instances);
		
		final Instance instance = api.createInstance(instanceName, "Test instance");
		System.out.println(instance);	
		
		List<AvailabilityOption> availabilityOptions = api.getAvailabilityOptions(instanceName);
		System.out.println(availabilityOptions);
		assertTrue(availabilityOptions.isEmpty());
		
		api.createAvailabilityOption(instanceName, "Available");
		availabilityOptions = api.getAvailabilityOptions(instanceName);
		System.out.println(availabilityOptions);
		assertEquals(1, availabilityOptions.size());
		assertEquals("Available", availabilityOptions.get(0).getLabel());
		
		api.createAvailabilityOption(instanceName, "Not available");
		
		List<Squad> squads = api.getSquads(instanceName);
		System.out.println(squads);
		assertTrue(squads.isEmpty());
		
		Squad squad = api.createSquad(instanceName, "Novice men");
		System.out.println(squad);
		assertEquals("novice-men", squad.getId());
		assertEquals("Novice men", squad.getName());
		
		api.createSquad(instanceName, "Senior women");
		
		squads = api.getSquads(instanceName);
		assertEquals(2, squads.size());
		List<Member> members = api.getMembers(instanceName);
		System.out.println(members);
		assertTrue(members.isEmpty());
		
		squad = api.getSquad(instanceName, squad.getId());
		assertNotNull(squad);
		
		api.createMember(instanceName, "John", "Smith", squad);
		members = api.getMembers(instanceName);
		System.out.println(members);
		assertEquals(1, members.size());
		
		Member newMember = members.get(0);
		assertEquals("JOHNSMITH", newMember.getId());
		assertEquals("John", newMember.getFirstName());
		assertEquals("Smith", newMember.getLastName());
		
		newMember = api.getMemberDetails(instanceName, newMember.getId());
		assertNotNull(newMember);
		
		assertFalse(newMember.getSquads().isEmpty());
		assertEquals(newMember.getSquads().get(0).getName(), "Novice men");
		
		List<Outing> outings = api.getOutings(instanceName);
		System.out.println(outings);
		assertTrue(outings.isEmpty());
		
		final LocalDateTime outingDate = new LocalDateTime(2012, 11, 5, 8, 0);
		final Outing newOuting = api.createOuting(instanceName, squad.getId(), outingDate);	
		assertEquals("novice-men-2012-11-05-08:00", newOuting.getId());
		assertEquals("Novice men", newOuting.getSquad().getName());
		assertEquals(outingDate.toDateTime(DateTimeZone.forID("Europe/London")), new DateTime(newOuting.getDate()));
		
		final LocalDateTime outingDateDuringBST = new LocalDateTime(2012, 6, 5, 8, 0, 0);
		final Outing newOutingDuringBST = api.createOuting(instanceName, squad.getId(), outingDateDuringBST);
		assertEquals("novice-men-2012-06-05-08:00", newOutingDuringBST.getId());
		assertEquals(outingDateDuringBST.toDateTime(DateTimeZone.forID("Europe/London")), new DateTime(newOutingDuringBST.getDate()));
		assertEquals("2012-06-05T08:00:00.000+01:00", new DateTime(newOutingDuringBST.getDate()).toString());
		
		outings = api.getOutings(instanceName);
		assertEquals(2, outings.size());
	}
	
}
