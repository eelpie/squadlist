package uk.co.squadlist.web.controllers.social;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class FacebookOauthStateService {
	
	private final Cache<String, String> states;

	public FacebookOauthStateService() {		
		this.states = CacheBuilder.newBuilder().maximumSize(10000).expireAfterAccess(1, TimeUnit.HOURS).build();
	}
	
	public String registerState() {
		final String state = UUID.randomUUID().toString().replaceAll("-", "");
		states.put(state, state);
		return state;
	}
	
	public boolean isValid(String state) {
		return states.getIfPresent(state) != null;
	}
	
	public void clearState(String state) {
		states.invalidate(state);
	}
	
}
