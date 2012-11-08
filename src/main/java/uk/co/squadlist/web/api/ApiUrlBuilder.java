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
	
	public String getMembersAvailabilityUrl(String instance, String memberId, Date fromDate) {
		final StringBuilder url = new StringBuilder(getMemberDetailsUrl(instance, memberId) + "/availability");
		appendFromDate(fromDate, url);
		return url.toString();		
	}

	public String getInstancesUrl() {
		return apiUrl + "/instances";

	}
	
	public String getSquadsUrl(String instance) {
		return apiUrl + "/" + instance + "/squads";
	}
	
	public String getSquadUrl(String instance, String squadId) {
		return getSquadsUrl(instance) + "/" + squadId;
	}
	
	public String getSquadAvailabilityUrl(String instance, String squadId, Date fromDate) {
		final StringBuilder url = new StringBuilder(getSquadUrl(instance, squadId) + "/availability");
		appendFromDate(fromDate, url);
		return url.toString();
	}
	
	public String getSquadMembersUrl(String instance, String squadId) {
		return getSquadUrl(instance, squadId) + "/members";
	}
	
	public String getSquadOutingsUrl(String instance, String squadId) {
		return getSquadUrl(instance, squadId) + "/outings";
	}
	
	public String getMembersUrl(String instance) {
		return apiUrl + "/" +  urlEncode(instance) + "/members";
	}
	
	public String getMemberDetailsUrl(String instance, String memberId) {
		return apiUrl + "/" +  urlEncode(instance) + "/members/" + memberId;
	}
	
	public String getOutingsUrl(String instance) {
		return apiUrl + "/" + urlEncode(instance) + "/outings";
	}
	
	public String getOutingUrl(String instance, String outingId) {
		return getOutingsUrl(instance) + "/" + urlEncode(outingId);
	}
	
	public String getOutingAvailabilityUrl(String instance, String outingId) {
		return getOutingUrl(instance, outingId) + "/availability";
	}
	
	public String getAvailabilityOptionsUrl(String instance) {
		return apiUrl + "/" +  urlEncode(instance) + "/availability/options";
	}
	
	public String getAuthUrlFor(String instance, String username,String password){
		return apiUrl + "/" + urlEncode(instance) + "/auth?username=" + urlEncode(username) + "&password=" + urlEncode(password);
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
