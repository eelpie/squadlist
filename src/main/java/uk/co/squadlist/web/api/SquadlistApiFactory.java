package uk.co.squadlist.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SquadlistApiFactory {

	private final String apiUrl;
	private final String clientAccessToken;
	
	@Autowired
	public SquadlistApiFactory(@Value("${apiUrl}") String apiUrl,
			@Value("${apiAccessToken}") String clientAccessToken) {
		this.apiUrl = apiUrl;
		this.clientAccessToken = clientAccessToken;
	}
	
	public SquadlistApi createClient() {
		return new SquadlistApi(apiUrl, clientAccessToken);
	}

}
