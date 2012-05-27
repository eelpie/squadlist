package uk.co.squadlist.web.api;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.model.Outing;

@Component
public class JsonDeserializer {

	private ObjectMapper mapper;
	
	public JsonDeserializer() {
		mapper = new ObjectMapper();
	}
	
	public List<Outing> deserializeListOfOutings(String json) throws JsonParseException, JsonMappingException, IOException {
		@SuppressWarnings("unchecked")
		List<Outing> outings = (List<Outing>) mapper.readValue(json, Collection.class);
		return outings;
	}

}
