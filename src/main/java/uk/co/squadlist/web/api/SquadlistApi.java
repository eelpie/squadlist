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
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.exceptions.InvalidInstanceException;
import uk.co.squadlist.web.exceptions.InvalidSquadException;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.model.AvailabilityOption;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;

import com.google.common.collect.Lists;

@Service("squadlistApi")
public class SquadlistApi {

	@Value("#{squadlist['instance']}")
	public static String INSTANCE = "demoinstance";
	
	private static Logger log = Logger.getLogger(SquadlistApi.class);
		
	private RequestBuilder requestBuilder;
	private ApiUrlBuilder urlBuilder;
	private HttpFetcher httpFetcher;
	private JsonDeserializer jsonDeserializer;
	
	@Autowired
	public SquadlistApi(RequestBuilder requestBuilder, ApiUrlBuilder urlBuilder, HttpFetcher httpFetcher, JsonDeserializer jsonDeserializer) {
		this.requestBuilder = requestBuilder;
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
			throw new RuntimeException(e);
		}
	}
	
	public Instance createInstance(String id, String name) throws InvalidInstanceException {
		try {
			final HttpPost post = requestBuilder.buildCreateInstanceRequest(id, name);		
			return jsonDeserializer.deserializeInstanceDetails(httpFetcher.post(post));
			
		} catch (HttpBadRequestException e) {
			throw new InvalidInstanceException();
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
	
	public List<OutingAvailability> getAvailabilityFor(String instance, String memberId, Date fromDate, Date toDate) {
		try {
			log.info("getAvailabilityFor: " + memberId + ", " + fromDate);
			final String json = httpFetcher.get(urlBuilder.getMembersAvailabilityUrl(instance, memberId, fromDate, toDate));
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
	
	public List<Outing> getSquadOutings(String instance, String squadId, Date fromDate, Date toDate) {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadOutingsUrl(instance, squadId, fromDate, toDate));
			return jsonDeserializer.deserializeListOfOutings(json);
		
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}			
	}
	
	public Map<String, Integer> getSquadOutingMonths(String instance, String squadId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadOutingsMonthsUrl(instance, squadId));
			return jsonDeserializer.deserializeOutingsMonthsMap(json);
		
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}	
	}
	
	public Map<String, Integer> getMemberOutingMonths(String instance, String memberId) {
		try {
			final String json = httpFetcher.get(urlBuilder.getMemberDetailsUrl(instance, memberId) + "/outings/months");
			return jsonDeserializer.deserializeOutingsMonthsMap(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}	
	}
		
	public Map<String, String> getOutingAvailability(String instance, String outingId) {
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
	
	public Outing getOuting(String instance, String outingId) {
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
	
	public List<OutingWithSquadAvailability> getSquadAvailability(String instance, String squadId, Date fromDate, Date toDate) {
		try {
			final String json = httpFetcher.get(urlBuilder.getSquadAvailabilityUrl(instance, squadId, fromDate, toDate));
			return jsonDeserializer.deserializeSquadAvailability(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public List<AvailabilityOption> getAvailabilityOptions(String instance) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		try {
			final String json = httpFetcher.get(urlBuilder.getAvailabilityOptionsUrl(instance));
			return jsonDeserializer.deserializeAvailabilityOptions(json);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public OutingAvailability setOutingAvailability(String instance, String memberId, String outingId, String availability) {
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
			nameValuePairs.add(new BasicNameValuePair("squad", squad != null ? squad.getId() : null));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));		
			return jsonDeserializer.deserializeMemberDetails(httpFetcher.post(post));
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public Squad createSquad(String instance, String name) throws InvalidSquadException {
		try {
			final HttpPost post = new HttpPost(urlBuilder.getSquadsUrl(instance));
		
			final List<NameValuePair> nameValuePairs = Lists.newArrayList();
			nameValuePairs.add(new BasicNameValuePair("name", name));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));	
			return jsonDeserializer.deserializeSquadDetails(httpFetcher.post(post));
			
		} catch (HttpBadRequestException e) {
			log.info("Bad request response to new squad request: " + e.getResponseBody());
			throw new InvalidSquadException();
			
		} catch (HttpFetchException e) {
			log.error(e);
			throw new RuntimeException(e);
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}		
		
	}
	
	public Member updateMemberDetails(String instance, Member member) {
		try {
			final HttpPost post = requestBuilder.buildUpdateMemberRequest(instance, member);
			return jsonDeserializer.deserializeMemberDetails(httpFetcher.post(post));
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public Outing createOuting(String instance, String squad, LocalDateTime outingDate) {
		try {
			final HttpPost post = new HttpPost(urlBuilder.getOutingsUrl(instance));
		
			final List<NameValuePair> nameValuePairs = Lists.newArrayList();
			nameValuePairs.add(new BasicNameValuePair("squad", squad));
			nameValuePairs.add(new BasicNameValuePair("date", ISODateTimeFormat.dateTimeNoMillis().print(outingDate)));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));			
			return jsonDeserializer.deserializeOutingDetails(httpFetcher.post(post));
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}		
	}

	public AvailabilityOption createAvailabilityOption(String instance, String label) {
		try {
			final HttpPost post = new HttpPost(urlBuilder.getAvailabilityOptionsUrl(instance));
		
			final List<NameValuePair> nameValuePairs = Lists.newArrayList();
			nameValuePairs.add(new BasicNameValuePair("label", label));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));			
			return jsonDeserializer.deserializeAvailabilityOption(httpFetcher.post(post));
			
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}		
	}
	
}
