package uk.co.squadlist.web.api;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.exceptions.HttpFetchException;
import uk.co.squadlist.web.model.Availability;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.Squad;

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
	
	public List<Outing> getSquadOutings(String squadId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getSquadOutingsUrl(squadId), "UTF-8");
		return jsonDeserializer.deserializeListOfOutings(json);	
	}
	
	public List<Availability> getOutingAvailability(int outingId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getOutingAvailabilityUrl(outingId), "UTF-8");
		return jsonDeserializer.deserializeListOfAvailability(json);	
	}
	
	public Member getMemberDetails(String memberId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getMemberDetailsUrl(memberId), "UTF-8");
		return jsonDeserializer.deserializeMemberDetails(json);	
	}
	
	public Squad getSquad(String squadId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getSquadUrl(squadId), "UTF-8");
		return jsonDeserializer.deserializeSquad(json);	
	}
		
	public Outing getOuting(String outingId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getOutingUrl(outingId), "UTF-8");
		return jsonDeserializer.deserializeOuting(json);	
	}
	
	public List<Member> getSquadMembers(String squadId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getSquadMembersUrl(squadId), "UTF-8");
		return jsonDeserializer.deserializeListOfMembers(json);	
	}
	
	private String getSquadUrl(String squadId) {
		return API_URL + "/squads/" + squadId;
	}
	
	private String getSquadMembersUrl(String squadId) {
		return getSquadUrl(squadId) + "/members";
	}
	
	private String getSquadOutingsUrl(String squadId) {
		return getSquadUrl(squadId) + "/outings";
	}

	private String getMemberDetailsUrl(String memberId) {
		return API_URL + "/members/" + memberId;
	}
	
	private String getOutingUrl(String outingId) {
		return API_URL + "/outings/" + outingId;
	}
	
	private String getOutingAvailabilityUrl(int outingId) {
		return getOutingUrl(Integer.toString(outingId)) + "/availability";
	}
	
	private String getMembersOutingsUrl(String memberId) {
		return getMemberDetailsUrl(memberId) + "/outings";
	}
	
}
