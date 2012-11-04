package uk.co.squadlist.web.api;

import java.util.List;

import org.junit.Test;

import uk.co.squadlist.web.model.Instance;

public class SquadlistApiIT {

	@Test
	public void canCreateInstances() throws Exception {
		final SquadlistApi api = new SquadlistApi("http://localhost:9090");
		
		final List<Instance> instances = api.getInstances();
		System.out.println(instances);
		
		final Instance instance = api.createInstance("test", "Test instance");
		System.out.println(instance);		
	}
	
}
