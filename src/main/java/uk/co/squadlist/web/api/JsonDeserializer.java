package uk.co.squadlist.web.api;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.model.Availability;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.Squad;

@Component
public class JsonDeserializer {

	private ObjectMapper mapper;
	
	public JsonDeserializer() {
		mapper = new ObjectMapper();
	}
	
	@SuppressWarnings("unchecked")
	public List<Outing> deserializeListOfOutings(String json) throws JsonParseException, JsonMappingException, IOException {
		return (List<Outing>) mapper.readValue(json, Collection.class);
	}
	
	@SuppressWarnings("unchecked")
	public List<Member> deserializeListOfMembers(String json) throws JsonParseException, JsonMappingException, IOException {
		return (List<Member>) mapper.readValue(json, Collection.class);
	}
	
	@SuppressWarnings("unchecked")
	public List<Availability> deserializeListOfAvailability(String json) throws JsonParseException, JsonMappingException, IOException {
		return (List<Availability>) mapper.readValue(json, Collection.class);
	}
	
	public Member deserializeMemberDetails(String json) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, Member.class);
	}

	public Outing deserializeOuting(String json) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, Outing.class);
	}

	public Squad deserializeSquad(String json) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, Squad.class);
	}
	
}
