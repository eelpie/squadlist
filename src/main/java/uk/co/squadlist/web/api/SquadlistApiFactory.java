package uk.co.squadlist.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SquadlistApiFactory {

	private final String apiUrl;
	private final String apiAccessToken;
	
	@Autowired
	public SquadlistApiFactory(@Value("#{squadlist['apiUrl']}") String apiUrl,
			@Value("#{squadlist['apiAccessToken']}") String apiAccessToken) {
		this.apiUrl = apiUrl;
		this.apiAccessToken = apiAccessToken;		
	}
	
	public SquadlistApi create() {
		return new SquadlistApi(apiUrl, apiAccessToken);
	}
	
}
