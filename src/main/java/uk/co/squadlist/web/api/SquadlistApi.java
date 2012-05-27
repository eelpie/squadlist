package uk.co.squadlist.web.api;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.exceptions.HttpFetchException;
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
	
	public List<Outing> getOutingsFor(String loggedInUser) throws HttpFetchException, JsonParseException, JsonMappingException, IOException {
		final String json = httpFetcher.fetchContent(getMembersOutingsUrl(loggedInUser), "UTF-8");
		return jsonDeserializer.deserializeListOfOutings(json);	
	}
	
	private String getMembersOutingsUrl(String loggedInUser) {
		return API_URL + "/members/" + loggedInUser + "/outings";
	}
	
}
