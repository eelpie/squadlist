package uk.co.squadlist.web.controllers.social;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component
public class FacebookLinkedAccountsService {
	
	private final static Logger log = Logger.getLogger(FacebookLinkedAccountsService.class);

	private  final Map<String, String> linkedAccounts;

	public FacebookLinkedAccountsService() {
		linkedAccounts = Maps.newConcurrentMap();
	}
	
	public void linkAccount(String loggedInMember, String faceBookId) {
		log.info("Linking logged in user " + loggedInMember + " to Facebook user: " + faceBookId);
		linkedAccounts.put(faceBookId, loggedInMember);	
	}
	
	public boolean isLinked(String loggedInMember) {
		return linkedAccounts.containsValue(loggedInMember);
	}
	
	public void removeLinkage(String loggedInMember) {
		String facebookIdToRemove = null;
		for (String facebookId : linkedAccounts.keySet()) {
			if (linkedAccounts.get(facebookId).equals(loggedInMember)) {
				facebookIdToRemove = facebookId;
			}			
		}
		
		if (facebookIdToRemove != null) {
			log.info("Removed linkage from " + loggedInMember + " to Facebook account: " + facebookIdToRemove);
			linkedAccounts.remove(facebookIdToRemove);
		}
	}
	
}
