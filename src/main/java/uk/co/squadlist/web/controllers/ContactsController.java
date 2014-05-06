package uk.co.squadlist.web.controllers;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.urls.UrlBuilder;

@Controller
public class ContactsController {
	
	private static Logger log = Logger.getLogger(ContactsController.class);
	
	private SquadlistApi api;
	private LoggedInUserService loggedInUserService;
	private UrlBuilder urlBuilder;
	private InstanceConfig instanceConfig;
	
	@Autowired
	public ContactsController(SquadlistApi api, LoggedInUserService loggedInUserService, UrlBuilder urlBuilder, InstanceConfig instanceConfig) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.urlBuilder = urlBuilder;
		this.instanceConfig = instanceConfig;
	}
	
	@RequestMapping("/contacts")
    public ModelAndView contacts(@RequestParam(required=false, value="squad") String squadId) throws Exception {
		
    	final ModelAndView mv = new ModelAndView("squadContacts");
    	mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	List<Squad> squads = api.getSquads(instanceConfig.getInstance());
		mv.addObject("squads", squads);
    	if (!squads.isEmpty()) {
    		final Squad squad = resolveSquad(squadId);
    		mv.addObject("squad", squad);
    		mv.addObject("members", api.getSquadMembers(instanceConfig.getInstance(), squad.getId()));    		
    	}
    	return mv;
    }

	private Squad resolveSquad(String squadId) {
    	if(!Strings.isNullOrEmpty(squadId)) {
    		return api.getSquad(instanceConfig.getInstance(), squadId);
    	}
    	
    	final Member loggedInMember = api.getMemberDetails(instanceConfig.getInstance(), loggedInUserService.getLoggedInUser());
    	if (!loggedInMember.getSquads().isEmpty()) {
    		return loggedInMember.getSquads().iterator().next();
    	}
    	
    	final List<Squad> squads = api.getSquads(instanceConfig.getInstance());
    	if (!squads.isEmpty()) {
    		return squads.iterator().next();
    	}
    	
    	return null;
	}
	
}
