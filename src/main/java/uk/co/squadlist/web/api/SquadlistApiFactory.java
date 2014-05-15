package uk.co.squadlist.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SquadlistApiFactory {

	private final String apiUrl;
	
	@Autowired
	public SquadlistApiFactory(	@Value("#{squadlist['apiUrl']}") String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	public SquadlistApi create() {
		return new SquadlistApi(apiUrl);
	}
	
}
