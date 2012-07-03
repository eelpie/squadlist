package uk.co.squadlist.web.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.co.squadlist.web.exceptions.HttpFetchException;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;

@Service("squadlistApi")
public class SquadlistApi {

	private static final String UTF_8 = "UTF-8";

	private static Logger log = Logger.getLogger(SquadlistApi.class);
		
	private ApiUrlBuilder urlBuilder;
	private HttpFetcher httpFetcher;
	private JsonDeserializer jsonDeserializer;
	
	@Autowired
	public SquadlistApi(ApiUrlBuilder urlBuilder, HttpFetcher httpFetcher, JsonDeserializer jsonDeserializer) {
		this.urlBuilder = urlBuilder;
		this.httpFetcher = httpFetcher;
		this.jsonDeserializer = jsonDeserializer;
	}
	
	public boolean auth(String username, String password) throws ClientProtocolException, IOException, AuthenticationException {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(urlBuilder.getAuthUrlFor(username, password)); 	// TODO should be a post
		HttpResponse execute = client.execute(get);				
		final int statusCode = execute.getStatusLine().getStatusCode();
		log.info("Auth attempt status code was: " + statusCode);
		return statusCode == HttpStatus.SC_OK;
	}
	
	public List<OutingAvailability> getAvailabilityFor(String memberId, Date date) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		log.info("getAvailabilityFor: " + memberId + ", " + date);
		final String json = httpFetcher.get(urlBuilder.getMembersAvailabilityUrl(memberId, date), UTF_8);
		return jsonDeserializer.deserializeListOfOutingAvailability(json);
	}
	
	public List<Squad> getSquads() throws JsonParseException, JsonMappingException, IOException, HttpFetchException {
		final String json = httpFetcher.get(urlBuilder.getSquadsUrl(), UTF_8);
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
		final String json = httpFetcher.get(urlBuilder.getSquadOutingsUrl(squadId), UTF_8);
		return jsonDeserializer.deserializeListOfOutings(json);	
	}
	
	public Map<String, String> getOutingAvailability(int outingId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.get(urlBuilder.getOutingAvailabilityUrl(outingId), UTF_8);
		return jsonDeserializer.deserializeListOfOutingAvailabilityMap(json);	
	}
	
	public Member getMemberDetails(String memberId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.get(urlBuilder.getMemberDetailsUrl(memberId), UTF_8);
		return jsonDeserializer.deserializeMemberDetails(json);	
	}
	
	public Squad getSquad(int squadId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.get(urlBuilder.getSquadUrl(squadId), UTF_8);
		return jsonDeserializer.deserializeSquad(json);	
	}
		
	public Outing getOuting(int outingId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException, UnknownOutingException {
		final String json = httpFetcher.get(urlBuilder.getOutingUrl(outingId), UTF_8);
		final Outing outing = jsonDeserializer.deserializeOuting(json);
		if (outing == null) {
			throw new UnknownOutingException();
		}
		return outing;	
	}
	
	public List<Member> getSquadMembers(int squadId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.get(urlBuilder.getSquadMembersUrl(squadId), UTF_8);
		return jsonDeserializer.deserializeListOfMembers(json);	
	}
	
	public List<OutingWithSquadAvailability> getSquadAvailability(int squadId) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.get(urlBuilder.getSquadAvailabilityUrl(squadId), UTF_8);
		return jsonDeserializer.deserializeSquadAvailability(json);	
	}

	public List<String> getAvailabilityOptions() throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.get(urlBuilder.getAvailabilityOptionsUrl(), UTF_8);
		return jsonDeserializer.deserializeListOfStrings(json);	
	}
	
	public OutingAvailability setOutingAvailability(String memberId, int outingId, String availability) throws JsonParseException, JsonMappingException, IOException, HttpFetchException {
		HttpPost post = new HttpPost(urlBuilder.getOutingAvailabilityUrl(outingId));
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("member", memberId));
		nameValuePairs.add(new BasicNameValuePair("availability", availability));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		return jsonDeserializer.deserializeOutingAvailability(httpFetcher.post(post, UTF_8));		
	}
	
}
