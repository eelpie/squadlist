package uk.co.squadlist.web.urls;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.Squad;

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
	
	public String squadUrl(Squad squad) {
		return applicationUrl("/squad/" + squad.getId());
	}
	
	public String outingUrl(Outing outing) {
		return applicationUrl("/outings/" + outing.getId());
	}
	
	public String editMemberUrl(Member member) {
		return memberUrl(member) + "/edit";
	}

	private String getBaseUrl() {
		return baseUrl;
	}

}
