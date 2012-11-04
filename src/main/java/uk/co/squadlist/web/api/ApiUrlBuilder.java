package uk.co.squadlist.web.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiUrlBuilder {
	
	private static final String UTF_8 = "UTF-8";
	
	private static DateTimeFormatter dateHourMinute = ISODateTimeFormat.dateHourMinute();
	
	@Value("#{squadlist['apiUrl']}")
	private String apiUrl;
	
	public ApiUrlBuilder(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	public String getMembersAvailabilityUrl(String memberId, Date fromDate) {
		final StringBuilder url = new StringBuilder(getMemberDetailsUrl(memberId) + "/availability");
		appendFromDate(fromDate, url);
		return url.toString();		
	}

	public String getInstancesUrl() {
		return apiUrl + "/instances";

	}
	
	public String getSquadsUrl() {
		return apiUrl + "/squads";
	}
	
	public String getSquadUrl(int squadId) {
		return getSquadsUrl() + "/" + squadId;
	}
	
	public String getSquadAvailabilityUrl(int squadId, Date fromDate) {
		final StringBuilder url = new StringBuilder(getSquadUrl(squadId) + "/availability");
		appendFromDate(fromDate, url);
		return url.toString();
	}
	
	public String getSquadMembersUrl(int squadId) {
		return getSquadUrl(squadId) + "/members";
	}
	
	public String getSquadOutingsUrl(int squadId) {
		return getSquadUrl(squadId) + "/outings";
	}
	
	public String getMembersUrl() {
		return apiUrl + "/members";
	}
	
	public String getMemberDetailsUrl(String memberId) {
		return apiUrl + "/members/" + memberId;
	}
		
	public String getOutingUrl(int outingId) {
		return apiUrl + "/outings/" + outingId;
	}
	
	public String getOutingAvailabilityUrl(int outingId) {
		return getOutingUrl(outingId) + "/availability";
	}
	
	public String getAvailabilityOptionsUrl() {
		return apiUrl + "/availability/options";
	}
	
	public String getAuthUrlFor(String username,String password){
		return apiUrl + "/auth?username=" + urlEncode(username) + "&password=" + urlEncode(password);
	}

	private void appendFromDate(Date fromDate, final StringBuilder url) {
		if (fromDate != null) {
			url.append("?fromDate=" + dateHourMinute.print(new DateTime(fromDate)));
		}
	}

	private String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
