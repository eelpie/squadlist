package uk.co.squadlist.web.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.controllers.InstanceConfig;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;

@Component
public class PreferedSquadService {

	private final static Logger log = Logger.getLogger(PreferedSquadService.class);
	
	private static final String SELECTED_SQUAD = "selectedSquad";
	
	private final SquadlistApi api;
	private final InstanceConfig instanceConfig;
	private final HttpServletRequest request;
	
	@Autowired
	public PreferedSquadService(SquadlistApi api, InstanceConfig instanceConfig, HttpServletRequest request) {
		this.api = api;
		this.instanceConfig = instanceConfig;
		this.request = request;
	}
	
	public Squad resolveSquad(String squadId, String loggedInUser) throws UnknownSquadException, UnknownMemberException {	// TODO inject logged in user
    	if(!Strings.isNullOrEmpty(squadId)) {
    		final Squad selectedSquad = api.getSquad(instanceConfig.getInstance(), squadId);
    		setPreferedSquad(selectedSquad);
			return selectedSquad;
    	}
    	return resolvedPreferedSquad(loggedInUser);
	}
	
	private Squad resolvedPreferedSquad(String loggedInUser) throws UnknownMemberException {		
    	final String selectedSquad = (String) request.getSession().getAttribute(SELECTED_SQUAD);
		if (selectedSquad != null) {
    		try {
				return api.getSquad(instanceConfig.getInstance(), selectedSquad);
			} catch (UnknownSquadException e) {
				request.getSession().removeAttribute(SELECTED_SQUAD);
			}
    	}
    	
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

	private void setPreferedSquad(Squad selectedSquad) {
		log.info("Setting selected squad to: " + selectedSquad.getId());
		request.getSession().setAttribute(SELECTED_SQUAD, selectedSquad.getId());
		
	}
	
}
