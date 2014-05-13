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
	
	public String staticUrl(String uri) {
		return applicationUrl("/static/") + uri;
	}
	
	public String memberUrl(Member member) {
		return memberUrl(member.getId());
	}
	
	public String memberUrl(String memberId) {
		return applicationUrl("/member/" + memberId);
	}
	
	public String newMemberUrl() {
		return applicationUrl("/member/new");
	}
	
	public String squadUrl(Squad squad) {
		return applicationUrl("/squad/" + squad.getId());
	}
	
	public String newSquadUrl() {
		return applicationUrl("/squad/new");
	}
	
	public String adminUrl() {
		return applicationUrl("/admin");
	}
	
	public String outingUrl(Outing outing) {
		return applicationUrl("/outings/" + outing.getId());
	}
	
	public String outingEditUrl(Outing outing) {
		return outingUrl(outing) + "/edit";
	}
	
	public String outings(Squad squad) {
		return applicationUrl("/outings?squad=" + squad.getId());
	}
	
	public String outings(Squad squad, String month) {
		return outings(squad) + "&month=" + month;
	}
	
	public String editMemberUrl(Member member) {
		return memberUrl(member) + "/edit";
	}
	
	public String editMemberUrl(String memberId) {
		return memberUrl(memberId) + "/edit";
	}
	
	private String getBaseUrl() {
		return baseUrl;
	}

}
