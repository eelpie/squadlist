package uk.co.squadlist.web.api;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import uk.co.squadlist.web.model.Outing;

public class JsonDeserializerTest {

	@Test
	public void test() throws Exception {
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResource("outings.json"));
		
		JsonDeserializer deserializer = new JsonDeserializer();
		List<Outing> outings = deserializer.deserializeListOfOutings(json);
		
		assertEquals(51, outings.size());
	}

}
