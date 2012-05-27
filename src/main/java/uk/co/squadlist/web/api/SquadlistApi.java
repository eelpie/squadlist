package uk.co.squadlist.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.exceptions.HttpFetchException;

@Component
public class SquadlistApi {
	
	private static final String API_URL = "http://localhost:8080/squadlist-api-0.0.1-SNAPSHOT";
	private HttpFetcher httpFetcher;
	
	@Autowired
	public SquadlistApi(HttpFetcher httpFetcher) {
		this.httpFetcher = httpFetcher;
	}

	public String getOutingsFor(String loggedInUser) throws HttpFetchException {
		return httpFetcher.fetchContent(getMembersOutingsUrl(loggedInUser), "UTF-8");
	}

	private String getMembersOutingsUrl(String loggedInUser) {
		return API_URL + "/members/" + loggedInUser + "/outings";
	}
	
}
