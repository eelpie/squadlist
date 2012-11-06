package uk.co.squadlist.web.api;

import java.util.List;

import org.junit.Test;

import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;

public class SquadlistApiIT {

	private static final String TEST_INSTANCE = "testit";

	@Test
	public void canCreateInstances() throws Exception {		
		final SquadlistApi api = new SquadlistApi("http://localhost:9090");
		
		final List<Instance> instances = api.getInstances();
		System.out.println(instances);
		
		final Instance instance = api.createInstance(TEST_INSTANCE, "Test instance");
		System.out.println(instance);	
		
		List<Member> members = api.getMembers(TEST_INSTANCE);
		System.out.println(members);
		
		api.createMember(TEST_INSTANCE, "John", "Smith");
		members = api.getMembers(TEST_INSTANCE);
		System.out.println(members);
		
		List<Squad> squads = api.getSquads(TEST_INSTANCE);
		System.out.println(squads);
	}
	
}
