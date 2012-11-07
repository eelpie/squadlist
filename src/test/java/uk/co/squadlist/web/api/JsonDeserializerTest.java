package uk.co.squadlist.web.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;

public class JsonDeserializerTest {

	@Test
	public void canDeserializeListOfOutings() throws Exception {
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResource("outings.json"));
		
		JsonDeserializer deserializer = new JsonDeserializer();
		List<Outing> outings = deserializer.deserializeListOfOutings(json);
		
		assertEquals(106, outings.size());		
		for (Outing outing : outings) {
			assertTrue(outing instanceof Outing);
		}
		
		final Outing firstOuting = outings.get(0);
		assertEquals(241, firstOuting.getId());
		assertEquals("Fri Mar 16 08:00:00 GMT 2012", firstOuting.getDate().toString());
		assertEquals("Men's Senior Squad", firstOuting.getSquad().getName());
	}
	
	@Test
	public void canDeserializeOuting() throws Exception {
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResource("outing.json"));
		
		JsonDeserializer deserializer = new JsonDeserializer();
		final Outing outing = deserializer.deserializeOuting(json);
		
		assertEquals(0, outing.getId());
		assertEquals("Wed Nov 07 22:01:29 GMT 2012", outing.getDate().toString());
		assertEquals("Novice men", outing.getSquad().getName());
	}
	
	@Test
	public void canDeserializeListOfMembersAvailability() throws Exception {
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResource("availability.json"));
		
		JsonDeserializer deserializer = new JsonDeserializer();
		List<OutingAvailability> availability = deserializer.deserializeListOfOutingAvailability(json);
		
		assertEquals(106, availability.size());
	}
	
	@Test
	public void canDeserializeOutingAvailabilityMap() throws Exception {
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResource("outingAvailability.json"));
		
		JsonDeserializer deserializer = new JsonDeserializer();
		Map<String, String> availability = deserializer.deserializeListOfOutingAvailabilityMap(json);
		
		assertEquals("Available", availability.get("KELLEYJ"));
	}
	
	@Test
	public void canDeserializeSquadAvailability() throws Exception {
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResource("squadAvailability.json"));
		JsonDeserializer deserializer = new JsonDeserializer();
		
		final List<OutingWithSquadAvailability> availability = deserializer.deserializeSquadAvailability(json);
		
		final OutingWithSquadAvailability outingWithSquadAvailability = availability.get(0);
		assertEquals(241, outingWithSquadAvailability.getOuting().getId());
		assertEquals("Fri Mar 16 08:00:00 GMT 2012", outingWithSquadAvailability.getOuting().getDate().toString());		
		assertEquals("Injury", outingWithSquadAvailability.getAvailability().get("TEMPLEB"));
	}
	
	@Test
	public void canDeserializeMember() throws Exception {
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResource("member.json"));
		JsonDeserializer deserializer = new JsonDeserializer();
		
		final Member member = deserializer.deserializeMemberDetails(json);
		
		assertEquals("LILIANB", member.getId());
		assertEquals(1, member.getSquads().size());
		assertEquals("Men's Senior Squad", member.getSquads().get(0).getName());
	}

}
