package uk.co.squadlist.web.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.co.squadlist.web.exceptions.HttpFetchException;
import uk.co.squadlist.web.model.Availability;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.Squad;

@Service("squadlistApi")
public class SquadlistApi {

	private static Logger log = Logger.getLogger(SquadlistApi.class);
	
	private static final String API_URL = "http://localhost:8080/squadlist-api-0.0.1-SNAPSHOT";
	
	private HttpFetcher httpFetcher;
	private JsonDeserializer jsonDeserializer;
	
	@Autowired
	public SquadlistApi(HttpFetcher httpFetcher, JsonDeserializer jsonDeserializer) {
		this.httpFetcher = httpFetcher;
		this.jsonDeserializer = jsonDeserializer;
	}
	
	public boolean auth(String username, String password) throws ClientProtocolException, IOException, AuthenticationException {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(API_URL + "/auth?username=" + username + "&password=" + password); 	// TODO should be a post
		HttpResponse execute = client.execute(get);				
		final int statusCode = execute.getStatusLine().getStatusCode();
		log.info("Auth attempt status code was: " + statusCode);
		return statusCode == HttpStatus.SC_OK;
	}
	
	public List<OutingAvailability> getAvailabilityFor(String memberId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getMembersAvailabilityUrl(memberId), "UTF-8");
		return jsonDeserializer.deserializeListOfOutingAvailability(json);	
	}
	
	public List<Squad> getSquads() throws JsonParseException, JsonMappingException, IOException, HttpFetchException {
		final String json = httpFetcher.fetchContent(getSquadsUrl(), "UTF-8");
		return jsonDeserializer.deserializeListOfSquads(json);
	}
	
	public Map<Integer, Squad> getSquadsMap() throws JsonParseException, JsonMappingException, IOException, HttpFetchException {
		Map<Integer, Squad> map = new HashMap<Integer, Squad>();
		for(Squad squad : getSquads()) {
			map.put(squad.getId(), squad);
		}
		return map;
	}
	
	public List<Outing> getSquadOutings(int squadId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getSquadOutingsUrl(squadId), "UTF-8");
		return jsonDeserializer.deserializeListOfOutings(json);	
	}
	
	public Map<String, String> getOutingAvailability(int outingId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getOutingAvailabilityUrl(outingId), "UTF-8");
		return jsonDeserializer.deserializeListOfOutingAvailabilityMap(json);	
	}
	
	public Member getMemberDetails(String memberId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getMemberDetailsUrl(memberId), "UTF-8");
		return jsonDeserializer.deserializeMemberDetails(json);	
	}
	
	public Squad getSquad(int squadId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getSquadUrl(squadId), "UTF-8");
		return jsonDeserializer.deserializeSquad(json);	
	}
		
	public Outing getOuting(String outingId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getOutingUrl(outingId), "UTF-8");
		return jsonDeserializer.deserializeOuting(json);	
	}
	
	public List<Member> getSquadMembers(int squadId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getSquadMembersUrl(squadId), "UTF-8");
		return jsonDeserializer.deserializeListOfMembers(json);	
	}
	
	private String getSquadsUrl() {
		return API_URL + "/squads";
	}
	
	private String getSquadUrl(int squadId) {
		return getSquadsUrl() + "/" + squadId;
	}
	
	private String getSquadMembersUrl(int squadId) {
		return getSquadUrl(squadId) + "/members";
	}
	
	private String getSquadOutingsUrl(int squadId) {
		return getSquadUrl(squadId) + "/outings";
	}

	private String getMemberDetailsUrl(String memberId) {
		return API_URL + "/members/" + memberId;
	}
	
	private String getMembersAvailabilityUrl(String memberId) {
		return getMemberDetailsUrl(memberId) + "/availability";
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
