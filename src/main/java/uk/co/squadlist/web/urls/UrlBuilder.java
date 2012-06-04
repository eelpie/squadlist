package uk.co.squadlist.web.urls;

import org.springframework.stereotype.Component;

@Component("urlBuilder")
public class UrlBuilder {
	
	public String applicationUrl(String uri) {
		return getBaseUrl() + uri;
	}

	private String getBaseUrl() {
		return "http://localhost:8080/squadlist-0.0.1-SNAPSHOT";	// TODO auto discover
	}

}
