package uk.co.squadlist.web.api;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.Test;

public class ApiUrlBuilderTest {

	private static final String MEMBER = "SOMEMEMBER";

	@Test
	public void canAppendFromDateToMemberAvailabilityUrl() throws Exception {		
		ApiUrlBuilder urlBuilder = new ApiUrlBuilder();
		urlBuilder.setApiUrl("http://api.local");
		
		final String url = urlBuilder.getMembersAvailabilityUrl(MEMBER, new DateTime(2012, 7, 13, 12, 10).toDate());
		
		assertEquals("http://api.local/members/SOMEMEMBER/availability?fromDate=2012-07-13T12:10", url);
	}
	
}
