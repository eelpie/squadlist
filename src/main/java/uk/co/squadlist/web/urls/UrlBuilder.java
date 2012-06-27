package uk.co.squadlist.web.urls;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("urlBuilder")
public class UrlBuilder {
	
	@Value("#{squadlist['baseUrl']}")
	private String baseUrl;
    
	public String applicationUrl(String uri) {
		return getBaseUrl() + uri;
	}

	private String getBaseUrl() {
		return baseUrl;
	}

}
