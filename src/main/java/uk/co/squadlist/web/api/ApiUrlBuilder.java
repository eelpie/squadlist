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
	
	public ApiUrlBuilder() {
	}
	
	public ApiUrlBuilder(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	public String getMembersAvailabilityUrl(String instance, String memberId, Date fromDate, Date toDate) {
		final StringBuilder url = new StringBuilder(getMemberDetailsUrl(instance, memberId) + "/availability");
		appendDates(url, fromDate, toDate);
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
	
	public String getSquadAvailabilityUrl(String instance, String squadId, Date fromDate, Date toDate) {
		final StringBuilder url = new StringBuilder(getSquadUrl(instance, squadId) + "/availability");
		appendDates(url, fromDate, toDate);
		return url.toString();
	}
	
	public String getSquadMembersUrl(String instance, String squadId) {
		return getSquadUrl(instance, squadId) + "/members";
	}
	
	public String getSquadOutingsUrl(String instance, String squadId, Date fromDate, Date toDate) {
		final StringBuilder url = new StringBuilder(getSquadUrl(instance, squadId) + "/outings");
		appendDates(url, fromDate, toDate);
		return url.toString();
	}
	
	public String getSquadOutingsMonthsUrl(String instance, String squadId) {
		return getSquadOutingsUrl(instance, squadId, null, null) + "/months";
	}
	
	public String getMembersUrl(String instance) {
		return apiUrl + "/" +  urlEncode(instance) + "/members";
	}
	
	public String getMemberDetailsUrl(String instance, String memberId) {
		return apiUrl + "/" +  urlEncode(instance) + "/members/" + urlEncode(memberId);
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

	private void appendDates(final StringBuilder url, Date fromDate, Date toDate) {
		String joiner = "?";
		if (fromDate != null) {
			url.append(joiner + "fromDate=" + dateHourMinute.print(new DateTime(fromDate)));
			joiner = "&";
		}
		if (toDate != null) {
			url.append(joiner + "toDate=" + dateHourMinute.print(new DateTime(toDate)));
			joiner = "&";
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
