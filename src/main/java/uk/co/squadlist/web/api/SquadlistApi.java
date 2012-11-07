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
import uk.co.squadlist.web.model.Instance;
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
	
	public SquadlistApi(String apiUrl) {
		this.urlBuilder  = new ApiUrlBuilder(apiUrl);
		this.httpFetcher = new HttpFetcher();
		this.jsonDeserializer = new JsonDeserializer();
	}

	public List<Instance> getInstances() {
		try {
			final String json = httpFetcher.get(urlBuilder.getInstancesUrl());
			return jsonDeserializer.deserializeListOfInstances(json);
		
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public Instance createInstance(String id, String name) {
		try {
			final HttpPost post = new HttpPost(urlBuilder.getInstancesUrl());
		
			final List<NameValuePair> nameValuePairs = Lists.newArrayList();
			nameValuePairs.add(new BasicNameValuePair("id", id));
			nameValuePairs.add(new BasicNameValuePair("name", name));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));		
			return jsonDeserializer.deserializeInstanceDetails(httpFetcher.post(post));
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}		
	}
	
	public boolean auth(String instance, String username, String password) {
		try {
			final HttpClient client = new DefaultHttpClient();
			final HttpGet get = new HttpGet(urlBuilder.getAuthUrlFor(instance, username, password)); 	// TODO should be a post
			final HttpResponse execute = client.execute(get);
			
			final int statusCode = execute.getStatusLine().getStatusCode();
			log.info("Auth attempt status code was: " + statusCode);
			return statusCode == HttpStatus.SC_OK;
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public List<OutingAvailability> getAvailabilityFor(String instance, String memberId, Date date) {
		try {
			log.info("getAvailabilityFor: " + memberId + ", " + date);
			final String json = httpFetcher.get(urlBuilder.getMembersAvailabilityUrl(instance, memberId, date));
			return jsonDeserializer.deserializeListOfOutingAvailability(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public List<Squad> getSquads(String instance) {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadsUrl(instance));		
			return jsonDeserializer.deserializeListOfSquads(json);
		
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public Map<String, Squad> getSquadsMap(String instance) {
		try {
			final Map<String, Squad> map = new HashMap<String, Squad>();
			for(Squad squad : getSquads(instance)) {
				map.put(squad.getId(), squad);
			}
			return map;
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public List<Outing> getSquadOutings(String instance, String squadId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadOutingsUrl(instance, squadId));
			return jsonDeserializer.deserializeListOfOutings(json);
		
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}			
	}
	
	public Map<String, String> getOutingAvailability(String instance, int outingId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getOutingAvailabilityUrl(instance, outingId));
			return jsonDeserializer.deserializeListOfOutingAvailabilityMap(json);

		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public Member getMemberDetails(String instance, String memberId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getMemberDetailsUrl(instance, memberId));
			return jsonDeserializer.deserializeMemberDetails(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public Squad getSquad(String instance, String squadId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadUrl(instance, squadId));
			return jsonDeserializer.deserializeSquad(json);

		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public List<Outing> getOutings(String instance) {
		try {
			final String json = httpFetcher.get(urlBuilder.getOutingsUrl(instance));
			return jsonDeserializer.deserializeListOfOutings(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public Outing getOuting(String instance, int outingId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getOutingUrl(instance, outingId));
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
	
	public List<Member> getMembers(String instance) {
		try {
			final String json = httpFetcher.get(urlBuilder.getMembersUrl(instance));
			return jsonDeserializer.deserializeListOfMembers(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}		
	}
	
	public List<Member> getSquadMembers(String instance, String squadId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadMembersUrl(instance, squadId));
			return jsonDeserializer.deserializeListOfMembers(json);

		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public List<OutingWithSquadAvailability> getSquadAvailability(String instance, String squadId, Date fromDate) {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadAvailabilityUrl(instance, squadId, fromDate));
			return jsonDeserializer.deserializeSquadAvailability(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public List<String> getAvailabilityOptions(String instance) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		try {
			final String json = httpFetcher.get(urlBuilder.getAvailabilityOptionsUrl(instance));
			return jsonDeserializer.deserializeListOfStrings(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public OutingAvailability setOutingAvailability(String instance, String memberId, int outingId, String availability) {
		try {
			final HttpPost post = new HttpPost(urlBuilder.getOutingAvailabilityUrl(instance, outingId));
		
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
	
	public Member createMember(String instance, String firstName, String lastName, Squad squad) {
		try {
			final HttpPost post = new HttpPost(urlBuilder.getMembersUrl(instance));
		
			final List<NameValuePair> nameValuePairs = Lists.newArrayList();
			nameValuePairs.add(new BasicNameValuePair("firstName", firstName));
			nameValuePairs.add(new BasicNameValuePair("lastName", lastName));
			nameValuePairs.add(new BasicNameValuePair("squad", squad.getId()));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));		
			return jsonDeserializer.deserializeMemberDetails(httpFetcher.post(post));
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}		
	}
	
	public Squad createSquad(String instance, String name) {
		try {
			final HttpPost post = new HttpPost(urlBuilder.getSquadsUrl(instance));
		
			final List<NameValuePair> nameValuePairs = Lists.newArrayList();
			nameValuePairs.add(new BasicNameValuePair("name", name));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));		
			return jsonDeserializer.deserializeSquadDetails(httpFetcher.post(post));
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}		
		
	}
	
	public Member updateMemberDetails(String instance, Member member) {
		try {
			final HttpPost post = new HttpPost(urlBuilder.getMemberDetailsUrl(instance, member.getId()));
		
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

	public Outing createOuting(String instance, String squad) {
		try {
			final HttpPost post = new HttpPost(urlBuilder.getOutingsUrl(instance));
		
			final List<NameValuePair> nameValuePairs = Lists.newArrayList();
			nameValuePairs.add(new BasicNameValuePair("squad", squad));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));		
			return jsonDeserializer.deserializeOutingDetails(httpFetcher.post(post));
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}		
	}
	
}
