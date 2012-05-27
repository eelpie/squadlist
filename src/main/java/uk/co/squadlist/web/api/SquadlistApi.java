package uk.co.squadlist.web.api;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.exceptions.HttpFetchException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;

@Component
public class SquadlistApi {
	
	private static final String API_URL = "http://localhost:8080/squadlist-api-0.0.1-SNAPSHOT";
	private HttpFetcher httpFetcher;
	private JsonDeserializer jsonDeserializer;
	
	@Autowired
	public SquadlistApi(HttpFetcher httpFetcher, JsonDeserializer jsonDeserializer) {
		this.httpFetcher = httpFetcher;
		this.jsonDeserializer = jsonDeserializer;
	}
	
	public List<Outing> getOutingsFor(String memberId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getMembersOutingsUrl(memberId), "UTF-8");
		return jsonDeserializer.deserializeListOfOutings(json);	
	}
	
	public Member getMemberDetails(String memberId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getMemberDetailsUrl(memberId), "UTF-8");
		return jsonDeserializer.deserializeMemberDetails(json);	
	}
	
	public List<Member> getSquadMembers(String squadId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getSquadMembersUrl(squadId), "UTF-8");
		return jsonDeserializer.deserializeListOfMembers(json);	
	}
	
	private String getSquadMembersUrl(String squadId) {
		return API_URL + "/squads/" + squadId + "/members";
	}

	private String getMemberDetailsUrl(String memberId) {
		return API_URL + "/members/" + memberId;
	}
	
	private String getMembersOutingsUrl(String memberId) {
		return getMemberDetailsUrl(memberId) + "/outings";
	}
	
}
