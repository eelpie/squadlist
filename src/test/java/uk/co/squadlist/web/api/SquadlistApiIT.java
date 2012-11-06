package uk.co.squadlist.web.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
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
		
		List<Member> members = api.getMembers(instanceName);
		System.out.println(members);
		assertTrue(members.isEmpty());
		
		api.createMember(instanceName, "John", "Smith");
		members = api.getMembers(instanceName);
		System.out.println(members);
		assertEquals(1, members.size());
		
		List<Squad> squads = api.getSquads(instanceName);
		System.out.println(squads);
		assertTrue(squads.isEmpty());
		
		api.createSquad(instanceName, "Novice men");
		squads = api.getSquads(instanceName);
		System.out.println(squads);
		assertEquals(1, squads.size());
	}
	
}
