package uk.co.squadlist.web.urls;

import org.springframework.stereotype.Component;

@Component
public class SeoLinkBuilder {

	public String makeSeoLinkFor(String name) {
		String result = name.toLowerCase().trim().replaceAll("\\s", "-");
		result = result.replaceAll("[^\\-a-z0-9_]", "");
		result = result.replaceAll("--+", "-");
		return result;
	}
	
}
