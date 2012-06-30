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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.co.squadlist.web.exceptions.HttpFetchException;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.model.Availability;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.Squad;

@Service("squadlistApi")
public class SquadlistApi {

	private static final String UTF_8 = "UTF-8";

	private static Logger log = Logger.getLogger(SquadlistApi.class);
		
	private HttpFetcher httpFetcher;
	private JsonDeserializer jsonDeserializer;
	
	@Value("#{squadlist['apiUrl']}")
	private String apiUrl;
	
	@Autowired
	public SquadlistApi(HttpFetcher httpFetcher, JsonDeserializer jsonDeserializer) {
		this.httpFetcher = httpFetcher;
		this.jsonDeserializer = jsonDeserializer;
	}
	
	public boolean auth(String username, String password) throws ClientProtocolException, IOException, AuthenticationException {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(apiUrl + "/auth?username=" + username + "&password=" + password); 	// TODO should be a post
		HttpResponse execute = client.execute(get);				
		final int statusCode = execute.getStatusLine().getStatusCode();
		log.info("Auth attempt status code was: " + statusCode);
		return statusCode == HttpStatus.SC_OK;
	}
	
	public List<OutingAvailability> getAvailabilityFor(String memberId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.get(getMembersAvailabilityUrl(memberId), UTF_8);
		return jsonDeserializer.deserializeListOfOutingAvailability(json);	
	}
	
	public List<Squad> getSquads() throws JsonParseException, JsonMappingException, IOException, HttpFetchException {
		final String json = httpFetcher.get(getSquadsUrl(), UTF_8);
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
		final String json = httpFetcher.get(getSquadOutingsUrl(squadId), UTF_8);
		return jsonDeserializer.deserializeListOfOutings(json);	
	}
	
	public Map<String, String> getOutingAvailability(int outingId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.get(getOutingAvailabilityUrl(outingId), UTF_8);
		return jsonDeserializer.deserializeListOfOutingAvailabilityMap(json);	
	}
	
	public Member getMemberDetails(String memberId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.get(getMemberDetailsUrl(memberId), UTF_8);
		return jsonDeserializer.deserializeMemberDetails(json);	
	}
	
	public Squad getSquad(int squadId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.get(getSquadUrl(squadId), UTF_8);
		return jsonDeserializer.deserializeSquad(json);	
	}
		
	public Outing getOuting(int outingId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException, UnknownOutingException {
		final String json = httpFetcher.get(getOutingUrl(outingId), UTF_8);
		final Outing outing = jsonDeserializer.deserializeOuting(json);
		if (outing == null) {
			throw new UnknownOutingException();
		}
		return outing;	
	}
	
	public List<Member> getSquadMembers(int squadId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.get(getSquadMembersUrl(squadId), UTF_8);
		return jsonDeserializer.deserializeListOfMembers(json);	
	}
	
	public List<String> getAvailabilityOptions() throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.get(getAvailabilityOptionsUrl(), UTF_8);
		return jsonDeserializer.deserializeListOfStrings(json);	
	}
	
	public List<Availability> setAvailability(String memberId, int outingId, String availability) throws JsonParseException, JsonMappingException, IOException, HttpFetchException {
		HttpPost post = new HttpPost(getOutingAvailabilityUrl(outingId));
		post.getParams().setParameter("member", memberId);
		post.getParams().setParameter("availability", availability);
		return jsonDeserializer.deserializeListOfAvailability(httpFetcher.post(post, UTF_8));		
	}
	
	private String getSquadsUrl() {
		return apiUrl + "/squads";
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
		return apiUrl + "/members/" + memberId;
	}
	
	private String getMembersAvailabilityUrl(String memberId) {
		return getMemberDetailsUrl(memberId) + "/availability";
	}
	
	private String getOutingUrl(int outingId) {
		return apiUrl + "/outings/" + outingId;
	}
	
	private String getOutingAvailabilityUrl(int outingId) {
		return getOutingUrl(outingId) + "/availability";
	}
	
	private String getAvailabilityOptionsUrl() {
		return apiUrl + "/availability/options";
	}
	
}
