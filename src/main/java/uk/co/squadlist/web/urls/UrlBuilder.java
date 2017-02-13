package uk.co.squadlist.web.urls;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.*;

import java.net.URISyntaxException;
import java.util.List;

@Component("urlBuilder")
public class UrlBuilder {

	private final String baseUrl;
	private final InstanceConfig instanceConfig;
	private final SeoLinkBuilder seoLinkBuilder;
	private String apiUrl;

	@Autowired
	public UrlBuilder(@Value("${baseUrl}") String baseUrl, InstanceConfig instanceConfig, SeoLinkBuilder seoLinkBuilder, @Value("${apiUrl}") String apiUrl) {
		this.baseUrl = baseUrl;
		this.instanceConfig = instanceConfig;
		this.seoLinkBuilder = seoLinkBuilder;
		this.apiUrl = apiUrl;
	}

	public String applicationUrl(String uri) {
		return getBaseUrl() + uri;
	}

	public String loginUrl() {
		return applicationUrl("/login");
	}

	public String staticUrl(String uri) {
		return applicationUrl("/static/") + uri;
	}

	public String boatUrl(Boat boat) {
		return applicationUrl("/boats/" + boat.getId());
	}
	public String memberUrl(Member member) {
		return memberUrl(member.getId());
	}

	public String memberResetPasswordUrl(Member member) {
		return memberUrl(member.getId()) + "/reset";
	}

	public String memberUrl(String memberId) {
		return applicationUrl("/member/" + memberId);
	}

	public String makeActive(Member member) {
		return memberUrl(member.getId()) + "/make-active";
	}

	public String makeInactive(Member member) {
		return memberUrl(member.getId()) + "/make-inactive";
	}

	public String delete(Member member) {
		return memberUrl(member.getId()) + "/delete";
	}

	public String delete(Squad squad) {
		return applicationUrl("/squad/" + squad.getId() + "/delete");
	}

	public String newMemberUrl() {
		return applicationUrl("/member/new");
	}

	public String newSquadUrl() {
		return applicationUrl("/squad/new");
	}

	public String editSquadUrl(Squad squad) {
		return applicationUrl("/squad/" + squad.getId() + "/edit");
	}

	public String adminUrl() {
		return applicationUrl("/admin");
	}

	public String outingsUrl() {
		return applicationUrl("/outings");
	}

	public String outingUrl(Outing outing) {
		return outingsUrl() + "/" + outing.getId();
	}

	public String outingAvailabilityCsv(Outing outing) {
		return outingUrl(outing) + ".csv";
	}

	public String outingCloseUrl(Outing outing) {
		return outingUrl(outing) + "/close";
	}

	public String deleteOuting(Outing outing) {
		return outingUrl(outing) + "/delete";
	}

	public String outingReopenUrl(Outing outing) {
		return outingUrl(outing) + "/reopen";
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

	public String availability(Squad squad) {
		return applicationUrl("/availability/" + squad.getId());
	}

	public String availability(Squad squad, String month) {
		return availability(squad) + "?month=" + month;
	}

	public String editInstanceSettings() {
		return adminUrl() + "/instance";
	}

	public String editMemberUrl(Member member) {
		return memberUrl(member) + "/edit";
	}

	public String editMemberUrl(String memberId) {
		return memberUrl(memberId) + "/edit";
	}

	public String entryDetailsCsv(Squad squad) {
		return applicationUrl("/entrydetails/" + squad.getId() + ".csv");
	}

	public String entryDetailsCsv(List<Member> members) {
		try {
			final URIBuilder url = new URIBuilder(applicationUrl("/entrydetails/selected.csv"));

			final List<String> memberIds = Lists.newArrayList();
			for (Member member : members) {
				memberIds.add(member.getId());
			}
			url.addParameter("members", Joiner.on(",").join(memberIds));

			return url.toString();

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public String changePassword() {
		return applicationUrl("/change-password");
	}

	public String socialMediaAccounts() {
		return applicationUrl("/social");
	}

	public String linkFacebookUrl() {
		return applicationUrl("/social/facebook/link");
	}

	public String removeFacebookUrl() {
		return applicationUrl("/social/facebook/remove");
	}

	public String getLinkFacebookCallbackUrl() {
		return applicationUrl("/social/facebook/link/callback");
	}

	public String facebookSigninCallbackUrl() {
		return applicationUrl("/social/facebook/signin/callback");
	}

	public String facebookSignin() {
		return applicationUrl("/social/facebook/signin");
	}

	public String getBaseUrl() {
		return baseUrl.replace("INSTANCE", instanceConfig.getVhost());
	}

	public String contactsUrl() {
		return applicationUrl("/contacts");
	}
	public String contactsUrl(Squad prefferredSquad) {
		return appendSquad(prefferredSquad, contactsUrl());
	}

	public String outingsUrl(Squad prefferredSquad) throws URISyntaxException {
		final URIBuilder url = new URIBuilder(outingsUrl());
		if (prefferredSquad != null) {
			url.addParameter("squad", prefferredSquad.getId());
		}
		return url.build().toString();
	}

	public String outingsRss(String userid, Instance instance) throws URISyntaxException {
		final URIBuilder url = new URIBuilder(applicationUrl("/rss"));
		url.addParameter("user", userid);
		url.addParameter("key", generateFeedKeyFor(userid, instance));
		return url.build().toString();
	}

	public String outingsIcal(String userid, Instance instance) throws URISyntaxException {
		String webcalBaseUrl = getBaseUrl().replaceAll("^https?://", "webcal://");
		final URIBuilder url = new URIBuilder(webcalBaseUrl  + "/ical");
		url.addParameter("user", userid);
		url.addParameter("key", generateFeedKeyFor(userid, instance));
		return url.build().toString();
	}

	public String availabilityUrl(Squad prefferredSquad) {
		return appendSquad(prefferredSquad, availabilityUrl());
	}

	public String governingBody(GoverningBody governingBody) {
		return applicationUrl("/governing-body/" + seoLinkBuilder.makeSeoLinkFor(governingBody.getName()));
	}

	public String staticImage(String filename) {
		return apiUrl + "/static/" + filename + ".jpg";
	}

	public String newAvailabilityOptionUrl() {
		return applicationUrl("/availability-option/new");
	}

	public String deleteAvailabilityOptionUrl(AvailabilityOption availabilityOption) {
		return applicationUrl("/availability-option/" + availabilityOption.getId() + "/delete");
	}

	public String editAvailabilityOptionUrl(AvailabilityOption availabilityOption) {
		return applicationUrl("/availability-option/" + availabilityOption.getId() + "/edit");
	}

	public String editAdmins() {
		return adminUrl() + "/admins";
	}

	public String adminExportMembersAsCSV() {
		return adminUrl() + "/export/members.csv";
	}

	private String availabilityUrl() {
		return applicationUrl("/availability");
	}

	private String appendSquad(Squad prefferredSquad, final String baseUrl) {
		return prefferredSquad != null ? baseUrl + "/" + prefferredSquad.getId() : baseUrl;
	}

	private String generateFeedKeyFor(String userid, Instance instance) {
		return DigestUtils.md5Hex(instance.getId() + userid);
	}

	public String mailto(List<String> emails) {
		if (emails.isEmpty()) {
			return null;
		}
		return "mailto:" + Joiner.on(",").join(emails);
	}

}