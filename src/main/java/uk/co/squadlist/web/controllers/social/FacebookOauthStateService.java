package uk.co.squadlist.web.controllers.social;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Component
public class FacebookOauthStateService {
	
	private final Set<String> states;	// TODO memory overflow vector

	public FacebookOauthStateService() {
		this.states = Sets.newConcurrentHashSet();
	}
	
	public String registerState() {
		final String state = UUID.randomUUID().toString().replaceAll("-", "");
		states.add(state);
		return state;
	}
	
	public boolean isValid(String state) {
		return states.contains(state);
	}
	
	public void clearState(String state) {
		states.remove(state);
	}
	
}
