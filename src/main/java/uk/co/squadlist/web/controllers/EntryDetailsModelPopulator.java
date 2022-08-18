package uk.co.squadlist.web.controllers;

import com.google.common.collect.Lists;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.model.swagger.Squad;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.views.DateFormatter;
import uk.co.squadlist.web.views.model.DisplayMember;

import java.util.Arrays;
import java.util.List;

@Component
public class EntryDetailsModelPopulator {

	private final ActiveMemberFilter activeMemberFilter;
	private final DisplayMemberFactory displayMemberFactory;

	@Autowired
	public EntryDetailsModelPopulator(ActiveMemberFilter activeMemberFilter,
									  DisplayMemberFactory displayMemberFactory) {
		this.activeMemberFilter = activeMemberFilter;
		this.displayMemberFactory = displayMemberFactory;
	}

	public void populateModel(final Squad squadToShow, DefaultApi api, final ModelAndView mv, Member loggedInMember) throws ApiException {
		List<Member> activeMembers = activeMemberFilter.extractActive(api.getSquadMembers(squadToShow.getId()));
		List<DisplayMember> displayMembers = displayMemberFactory.toDisplayMembers(activeMembers, loggedInMember);
		mv.addObject("squad", squadToShow);
		mv.addObject("title", squadToShow.getName() + " entry details");
		mv.addObject("members", displayMembers);
	}

	public List<List<String>> getEntryDetailsRows(Squad squadToShow, DefaultApi api, GoverningBody governingBody, Instance instance) throws ApiException {
		List<Member> squadMembers = api.getSquadMembers(squadToShow.getId());
		return getEntryDetailsRows(activeMemberFilter.extractActive(squadMembers), governingBody, instance);
	}

	public List<List<String>> getEntryDetailsRows(List<Member> members, GoverningBody governingBody, Instance instance) {
		DateFormatter dateFormatter = new DateFormatter(DateTimeZone.forID(instance.getTimeZone()));

		final List<List<String>> rows = Lists.newArrayList();
		for (Member member : members) {

			final Integer effectiveAge = member.getDateOfBirth() != null ? governingBody.getEffectiveAge(member.getDateOfBirth()) : null;
    		final String ageGrade = effectiveAge != null ? governingBody.getAgeGrade(effectiveAge) : null;

			String formattedDob = member.getDateOfBirth() != null ? dateFormatter.dayMonthYear(member.getDateOfBirth().toDate()) : "";
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

}
