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

public class JsonDeserializerTest {

	@Test
	public void canDeserializeListOfOutings() throws Exception {
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResource("outings.json"));
		
		JsonDeserializer deserializer = new JsonDeserializer();
		List<Outing> outings = deserializer.deserializeListOfOutings(json);
		
		assertEquals(51, outings.size());		
		for (Outing outing : outings) {
			assertTrue(outing instanceof Outing);
		}
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
	public void canDeserializeMember() throws Exception {
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResource("member.json"));
		JsonDeserializer deserializer = new JsonDeserializer();
		
		final Member member = deserializer.deserializeMemberDetails(json);
		
		assertEquals("ISADOREA", member.getId());
	}

}
