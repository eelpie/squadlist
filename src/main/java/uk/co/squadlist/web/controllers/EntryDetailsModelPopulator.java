package uk.co.squadlist.web.controllers;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.annotations.RequiresSquadPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.views.DateFormatter;

import com.google.common.collect.Lists;

@Component
public class EntryDetailsModelPopulator {

	private InstanceSpecificApiClient api;
	private DateFormatter dateFormatter;
	private ActiveMemberFilter activeMemberFilter;
	private GoverningBodyFactory governingBodyFactory;

	public EntryDetailsModelPopulator() {
	}

	@Autowired
	public EntryDetailsModelPopulator(InstanceSpecificApiClient api, DateFormatter dateFormatter, ActiveMemberFilter activeMemberFilter, GoverningBodyFactory governingBodyFactory) {
		this.api = api;
		this.dateFormatter = dateFormatter;
		this.activeMemberFilter = activeMemberFilter;
		this.governingBodyFactory = governingBodyFactory;
	}

	@RequiresSquadPermission(permission=Permission.VIEW_SQUAD_ENTRY_DETAILS)
	public void populateModel(final Squad squadToShow, final ModelAndView mv) {
		mv.addObject("squad", squadToShow);
		mv.addObject("title", squadToShow.getName() + " entry details");
    	mv.addObject("members", activeMemberFilter.extractActive(api.getSquadMembers(squadToShow.getId())));
	}

	@RequiresSquadPermission(permission=Permission.VIEW_SQUAD_ENTRY_DETAILS)
	public List<List<String>> getEntryDetailsRows(Squad squadToShow) {
		return getEntryDetailsRows(activeMemberFilter.extractActive(api.getSquadMembers(squadToShow.getId())));
	}

	public List<List<String>> getEntryDetailsRows(List<Member> members) {	// TOOD permissions
		final GoverningBody governingBody = governingBodyFactory.getGoverningBody();

		final List<List<String>> rows = Lists.newArrayList();
		for (Member member : members) {
    		final Integer effectiveAge = member.getDateOfBirth() != null ? governingBody.getEffectiveAge(member.getDateOfBirth()) : null;
    		final String ageGrade = effectiveAge != null ? governingBody.getAgeGrade(effectiveAge) : null;

			rows.add(Arrays.asList(new String[] {member.getFirstName(), member.getLastName(),
    				member.getDateOfBirth() != null ? dateFormatter.dayMonthYear(member.getDateOfBirth()) : "",
    				effectiveAge != null ? effectiveAge.toString() : "",
    				ageGrade != null ? ageGrade : "",
    				member.getWeight() != null ? member.getWeight().toString() : "",
    				member.getRowingPoints(),
    				governingBody.getRowingStatus(member.getRowingPoints()),
    				member.getScullingPoints(),
    				governingBody.getScullingStatus(member.getScullingPoints()),
    				member.getRegistrationNumber()}));
		}
		return rows;
	}

	public List<String> getEntryDetailsHeaders() {
		return Lists.newArrayList("First name", "Last name", "Date of birth", "Effective age", "Age grade", 
				"Weight", "Rowing points", "Rowing status",
				"Sculling points", "Sculling status", "Registration number");
	}

}
