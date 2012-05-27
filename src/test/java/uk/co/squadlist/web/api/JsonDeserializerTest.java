package uk.co.squadlist.web.api;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import uk.co.squadlist.web.model.Availability;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;

public class JsonDeserializerTest {

	@Test
	public void canDeserializeListOfOutings() throws Exception {
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResource("outings.json"));
		
		JsonDeserializer deserializer = new JsonDeserializer();
		List<Outing> outings = deserializer.deserializeListOfOutings(json);
		
		assertEquals(51, outings.size());
	}
	
	@Test
	public void canDeserializeListOfAvailability() throws Exception {
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResource("availability.json"));
		
		JsonDeserializer deserializer = new JsonDeserializer();
		List<Availability> availability = deserializer.deserializeListOfAvailability(json);
		
		assertEquals(21, availability.size());
	}
	
	@Test
	public void canDeserializeMember() throws Exception {
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResource("member.json"));
		JsonDeserializer deserializer = new JsonDeserializer();
		
		final Member member = deserializer.deserializeMemberDetails(json);
		
		assertEquals("ISADOREA", member.getId());
	}

}
