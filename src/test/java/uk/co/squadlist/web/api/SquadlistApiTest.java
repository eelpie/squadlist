package uk.co.squadlist.web.api;

import static org.mockito.Mockito.when;

import org.apache.http.client.methods.HttpPost;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.squadlist.web.exceptions.InvalidInstanceException;

public class SquadlistApiTest {

	@Mock
	private RequestBuilder requestBuilder;
	@Mock
	private ApiUrlBuilder urlBuilder;
	@Mock
	private HttpFetcher httpFetcher;
	@Mock
	private JsonDeserializer jsonDeserializer;
	
	@Mock
	private HttpPost request;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test(expected=InvalidInstanceException.class)
	public void shouldReturnInvalidInstanceExceptionInResponseToNewInstance400Response() throws Exception {
		SquadlistApi api = new SquadlistApi(requestBuilder, urlBuilder, httpFetcher, jsonDeserializer);
		
		when(requestBuilder.buildCreateInstanceRequest("invalid", "Invalid")).thenReturn(request);
		when(httpFetcher.post(request)).thenThrow(new HttpBadRequestException("Invalid instance resposne body"));
		
		api.createInstance("invalid", "Invalid");		
	}
	
}
