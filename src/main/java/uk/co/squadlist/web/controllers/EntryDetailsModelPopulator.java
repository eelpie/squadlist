package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.annotations.RequiresSquadPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.Permission;

@Component
public class EntryDetailsModelPopulator {
	
	private InstanceSpecificApiClient api;
	
	public EntryDetailsModelPopulator() {
	}
	
	@Autowired
	public EntryDetailsModelPopulator(InstanceSpecificApiClient api) {		
		this.api = api;
	}
	
	@RequiresSquadPermission(permission=Permission.VIEW_SQUAD_ENTRY_DETAILS)
	public void populateModel(final Squad squadToShow, final ModelAndView mv) {
		mv.addObject("squad", squadToShow);
		mv.addObject("title", squadToShow.getName() + " entry details");
    	mv.addObject("members", api.getSquadMembers(squadToShow.getId()));
	}
	
}
