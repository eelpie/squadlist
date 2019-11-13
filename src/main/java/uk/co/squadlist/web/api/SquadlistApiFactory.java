package uk.co.squadlist.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SquadlistApiFactory {

	private final String apiUrl;
	private final String clientId;
	private final String clientSecret;

	@Autowired
	public SquadlistApiFactory(@Value("${apiUrl}") String apiUrl,
			@Value("${client.id}") String clientId,
			@Value("${client.secret}") String clientSecret) {
		this.apiUrl = apiUrl;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}
	
	public SquadlistApi createClient() throws IOException {
		String clientAccessToken = new SquadlistApi(apiUrl).requestClientAccessToken(clientId, clientSecret);
		return createForToken(clientAccessToken);
	}

	public SquadlistApi createForToken(String token) {
		return new SquadlistApi(apiUrl, token);
	}

}
