package uk.co.squadlist.web.urls;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.model.Member;

@Component("urlBuilder")
public class UrlBuilder {
	
	@Value("#{squadlist['baseUrl']}")
	private String baseUrl;
    
	public String applicationUrl(String uri) {
		return getBaseUrl() + uri;
	}
	
	public String memberUrl(Member member) {
		return applicationUrl("/member/" + member.getId());
	}
	
	public String editMemberUrl(Member member) {
		return memberUrl(member) + "/edit";
	}

	private String getBaseUrl() {
		return baseUrl;
	}

}
