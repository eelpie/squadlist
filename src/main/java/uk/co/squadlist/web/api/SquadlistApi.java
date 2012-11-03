package uk.co.squadlist.web.api;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
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

import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;

import com.google.common.collect.Lists;

@Service("squadlistApi")
public class SquadlistApi {

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
	
	public boolean auth(String username, String password) {
		try {
			final HttpClient client = new DefaultHttpClient();
			final HttpGet get = new HttpGet(urlBuilder.getAuthUrlFor(username, password)); 	// TODO should be a post
			final HttpResponse execute = client.execute(get);
			
			final int statusCode = execute.getStatusLine().getStatusCode();
			log.info("Auth attempt status code was: " + statusCode);
			return statusCode == HttpStatus.SC_OK;
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public List<OutingAvailability> getAvailabilityFor(String memberId, Date date) {
		try {
			log.info("getAvailabilityFor: " + memberId + ", " + date);
			final String json = httpFetcher.get(urlBuilder.getMembersAvailabilityUrl(memberId, date));
			return jsonDeserializer.deserializeListOfOutingAvailability(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public List<Squad> getSquads() {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadsUrl());		
			return jsonDeserializer.deserializeListOfSquads(json);
		
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public Map<Integer, Squad> getSquadsMap() {
		try {
			final Map<Integer, Squad> map = new HashMap<Integer, Squad>();
			for(Squad squad : getSquads()) {
				map.put(squad.getId(), squad);
			}
			return map;
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public List<Outing> getSquadOutings(int squadId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadOutingsUrl(squadId));
			return jsonDeserializer.deserializeListOfOutings(json);
		
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}			
	}
	
	public Map<String, String> getOutingAvailability(int outingId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getOutingAvailabilityUrl(outingId));
			return jsonDeserializer.deserializeListOfOutingAvailabilityMap(json);

		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public Member getMemberDetails(String memberId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getMemberDetailsUrl(memberId));
			return jsonDeserializer.deserializeMemberDetails(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public Squad getSquad(int squadId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadUrl(squadId));
			return jsonDeserializer.deserializeSquad(json);

		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
		
	public Outing getOuting(int outingId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getOutingUrl(outingId));
			final Outing outing = jsonDeserializer.deserializeOuting(json);
			if (outing == null) {
				throw new UnknownOutingException();
			}
			return outing;	
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public List<Member> getSquadMembers(int squadId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadMembersUrl(squadId));
			return jsonDeserializer.deserializeListOfMembers(json);

		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public List<OutingWithSquadAvailability> getSquadAvailability(int squadId, Date fromDate) {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadAvailabilityUrl(squadId, fromDate));
			return jsonDeserializer.deserializeSquadAvailability(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public List<String> getAvailabilityOptions() throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		try {
			final String json = httpFetcher.get(urlBuilder.getAvailabilityOptionsUrl());
			return jsonDeserializer.deserializeListOfStrings(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public OutingAvailability setOutingAvailability(String memberId, int outingId, String availability) {
		try {
			final HttpPost post = new HttpPost(urlBuilder.getOutingAvailabilityUrl(outingId));
		
			final List<NameValuePair> nameValuePairs = Lists.newArrayList();
			nameValuePairs.add(new BasicNameValuePair("member", memberId));
			nameValuePairs.add(new BasicNameValuePair("availability", availability));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			return jsonDeserializer.deserializeOutingAvailability(httpFetcher.post(post));
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}		
	}
	
	public Member updateMemberDetails(Member member) {
		try {
			final HttpPost post = new HttpPost(urlBuilder.getMemberDetailsUrl(member.getId()));
		
			final List<NameValuePair> nameValuePairs = Lists.newArrayList();
			nameValuePairs.add(new BasicNameValuePair("firstName", member.getFirstName()));
			nameValuePairs.add(new BasicNameValuePair("lastName", member.getLastName()));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));		
			return jsonDeserializer.deserializeMemberDetails(httpFetcher.post(post));
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
}
