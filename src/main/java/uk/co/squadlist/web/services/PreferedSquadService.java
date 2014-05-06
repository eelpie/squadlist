package uk.co.squadlist.web.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.controllers.InstanceConfig;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;

@Component
public class PreferedSquadService {

	private final SquadlistApi api;
	private final InstanceConfig instanceConfig;
	
	@Autowired
	public PreferedSquadService(SquadlistApi api, InstanceConfig instanceConfig) {
		this.api = api;
		this.instanceConfig = instanceConfig;
	}
	
	public Squad resolvedPreferedSquad(String loggedInUser) {
		final Member loggedInMember = api.getMemberDetails(instanceConfig.getInstance(), loggedInUser);
    	if (!loggedInMember.getSquads().isEmpty()) {
    		return loggedInMember.getSquads().iterator().next();
    	}
    	
    	final List<Squad> allSquads = api.getSquads(instanceConfig.getInstance());
    	if (!allSquads.isEmpty()) {
    		return allSquads.iterator().next();
    	}
    	return null;
	}
	
}
