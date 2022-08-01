package uk.co.squadlist.web.controllers;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Squad;
import uk.co.squadlist.web.annotations.RequiresSquadPermission;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.views.DateFormatter;
import uk.co.squadlist.web.views.model.DisplayMember;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class EntryDetailsModelPopulator {

	private final DateFormatter dateFormatter;
	private final ActiveMemberFilter activeMemberFilter;
	private final PermissionsService permissionsService;

	@Autowired
	public EntryDetailsModelPopulator(DateFormatter dateFormatter, ActiveMemberFilter activeMemberFilter, PermissionsService permissionsService) {
		this.dateFormatter = dateFormatter;
		this.activeMemberFilter = activeMemberFilter;
		this.permissionsService = permissionsService;
	}

	@RequiresSquadPermission(permission = Permission.VIEW_SQUAD_ENTRY_DETAILS)
	public void populateModel(final Squad squadToShow, DefaultApi api, final ModelAndView mv, uk.co.squadlist.model.swagger.Member loggedInMember) throws ApiException {
		List<uk.co.squadlist.model.swagger.Member> activeMembers = activeMemberFilter.extractActive(api.squadsIdMembersGet(squadToShow.getId()));
		List<DisplayMember> displayMembers = toDisplayMembers(activeMembers, loggedInMember);
		mv.addObject("squad", squadToShow);
		mv.addObject("title", squadToShow.getName() + " entry details");
		mv.addObject("members", displayMembers);
	}

	@RequiresSquadPermission(permission=Permission.VIEW_SQUAD_ENTRY_DETAILS)
	public List<List<String>> getEntryDetailsRows(Squad squadToShow, DefaultApi api, GoverningBody governingBody) throws ApiException {
		List<uk.co.squadlist.model.swagger.Member> squadMembers = api.squadsIdMembersGet(squadToShow.getId());
		return getEntryDetailsRows(activeMemberFilter.extractActive(squadMembers), governingBody);
	}

	public List<List<String>> getEntryDetailsRows(List<uk.co.squadlist.model.swagger.Member> members, GoverningBody governingBody) {	// TOOD permissions
		final List<List<String>> rows = Lists.newArrayList();
		for (uk.co.squadlist.model.swagger.Member member : members) {

			DateTime dobAsDate = member.getDateOfBirth() != null ? ISODateTimeFormat.dateTimeNoMillis().parseDateTime(member.getDateOfBirth()) : null;	// TODO push to API

    		final Integer effectiveAge = dobAsDate != null ? governingBody.getEffectiveAge(dobAsDate.toDate()) : null;
    		final String ageGrade = effectiveAge != null ? governingBody.getAgeGrade(effectiveAge) : null;

			String formattedDob = dobAsDate != null ? dateFormatter.dayMonthYear(dobAsDate.toDate()) : "";
			rows.add(Arrays.asList(member.getFirstName(), member.getLastName(),
					formattedDob,
    				effectiveAge != null ? effectiveAge.toString() : "",
    				ageGrade != null ? ageGrade : "",
    				member.getWeight() != null ? member.getWeight().toString() : "",
    				member.getRowingPoints(),
    				governingBody.getRowingStatus(member.getRowingPoints()),
    				member.getScullingPoints(),
    				governingBody.getScullingStatus(member.getScullingPoints()),
    				member.getRegistrationNumber()));
		}
		return rows;
	}

	public List<String> getEntryDetailsHeaders() {
		return Lists.newArrayList("First name", "Last name", "Date of birth", "Effective age", "Age grade", 
				"Weight", "Rowing points", "Rowing status",
				"Sculling points", "Sculling status", "Registration number");
	}

	// TODO duplication?
	private List<DisplayMember> toDisplayMembers(List<uk.co.squadlist.model.swagger.Member> members, uk.co.squadlist.model.swagger.Member loggedInUser) {
		List<DisplayMember> displayMembers = new ArrayList<>();
		for (uk.co.squadlist.model.swagger.Member member : members) {
			boolean isEditable = permissionsService.hasMemberPermission(loggedInUser, Permission.EDIT_MEMBER_DETAILS, member);
			displayMembers.add(new DisplayMember(member, isEditable));
		}
		return displayMembers;
	}

}
