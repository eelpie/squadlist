package uk.co.squadlist.web.controllers;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.dates.DateFormatter;
import uk.co.squadlist.web.annotations.RequiresSquadPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.Permission;

import com.google.common.collect.Lists;

@Component
public class EntryDetailsModelPopulator {
	
	private InstanceSpecificApiClient api;
	private DateFormatter dateFormatter;
	
	public EntryDetailsModelPopulator() {
	}
	
	@Autowired
	public EntryDetailsModelPopulator(InstanceSpecificApiClient api, DateFormatter dateFormatter) {		
		this.api = api;
		this.dateFormatter = dateFormatter;
	}
	
	@RequiresSquadPermission(permission=Permission.VIEW_SQUAD_ENTRY_DETAILS)
	public void populateModel(final Squad squadToShow, final ModelAndView mv) {
		mv.addObject("squad", squadToShow);
		mv.addObject("title", squadToShow.getName() + " entry details");
    	mv.addObject("members", api.getSquadMembers(squadToShow.getId()));
	}
	
	@RequiresSquadPermission(permission=Permission.VIEW_SQUAD_ENTRY_DETAILS)
	public List<List<String>> getEntryDetailsRows(Squad squadToShow) {
		final List<List<String>> rows = Lists.newArrayList();
    	for (Member member :api.getSquadMembers(squadToShow.getId())) {			
    		rows.add(Arrays.asList(new String[] {member.getFirstName(), member.getLastName(), 
    				member.getDateOfBirth() != null ? dateFormatter.dayMonthYear(member.getDateOfBirth()) : "", 
    				member.getWeight() != null ? member.getWeight().toString() : "",
    				member.getRowingPoints(),
    				member.getScullingPoints(), 
    				member.getRegistrationNumber()}));
		}
		return rows;
	}
	
}
