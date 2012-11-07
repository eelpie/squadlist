package uk.co.squadlist.web.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

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
		
		List<Squad> squads = api.getSquads(instanceName);
		System.out.println(squads);
		assertTrue(squads.isEmpty());
		
		Squad squad = api.createSquad(instanceName, "Novice men");
		System.out.println(squad);
		assertEquals("Novice men", squad.getName());
		squads = api.getSquads(instanceName);
		System.out.println(squads);
		
		assertEquals(1, squads.size());
		List<Member> members = api.getMembers(instanceName);
		System.out.println(members);
		assertTrue(members.isEmpty());
		
		api.createMember(instanceName, "John", "Smith", squad);
		members = api.getMembers(instanceName);
		System.out.println(members);
		assertEquals(1, members.size());
		assertEquals("John", members.get(0).getFirstName());
		assertEquals("Smith", members.get(0).getLastName());

		assertFalse(members.get(0).getSquads().isEmpty());
		assertEquals(members.get(0).getSquads().get(0).getName(), "Novice men");
		
		List<Outing> outings = api.getOutings(instanceName);
		System.out.println(outings);
		assertTrue(outings.isEmpty());
		
		final Outing newOuting = api.createOuting(instanceName, squad.getId());
		System.out.println(newOuting);
		assertEquals("Novice men", newOuting.getSquad().getName());
		
		outings = api.getOutings(instanceName);
		assertEquals(1, outings.size());
	}
	
}
