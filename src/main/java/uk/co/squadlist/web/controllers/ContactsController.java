package uk.co.squadlist.web.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;

import com.google.common.base.Strings;

@Controller
public class ContactsController {
		
	private SquadlistApi api;
	private LoggedInUserService loggedInUserService;
	private InstanceConfig instanceConfig;
	
	@Autowired
	public ContactsController(SquadlistApi api, LoggedInUserService loggedInUserService, InstanceConfig instanceConfig) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.instanceConfig = instanceConfig;
	}
	
	@RequestMapping("/contacts")
    public ModelAndView contacts(@RequestParam(required=false, value="squad") String squadId) throws Exception {
		
    	final ModelAndView mv = new ModelAndView("squadContacts");
    	mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	final List<Squad> allSquads = api.getSquads(instanceConfig.getInstance());
		mv.addObject("squads", allSquads);
    	if (!allSquads.isEmpty()) {
    		final Squad squadToShow = resolveSquad(squadId, allSquads);
    		mv.addObject("squad", squadToShow);
    		mv.addObject("members", api.getSquadMembers(instanceConfig.getInstance(), squadToShow.getId()));    		
    	}
    	return mv;
    }

	private Squad resolveSquad(String squadId, List<Squad> allSquads) {
    	if(!Strings.isNullOrEmpty(squadId)) {
    		return api.getSquad(instanceConfig.getInstance(), squadId);
    	}
    	
    	final Member loggedInMember = api.getMemberDetails(instanceConfig.getInstance(), loggedInUserService.getLoggedInUser());
    	if (!loggedInMember.getSquads().isEmpty()) {
    		return loggedInMember.getSquads().iterator().next();
    	}
    	
    	if (!allSquads.isEmpty()) {
    		return allSquads.iterator().next();
    	}
    	return null;
	}
	
}
