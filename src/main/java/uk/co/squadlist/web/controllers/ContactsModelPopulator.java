package uk.co.squadlist.web.controllers;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.annotations.RequiresSquadPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

@Component
public class ContactsModelPopulator {

	private static Function<Member, String> roleName = new Function<Member, String>() {
		@Override
		public String apply(Member obj) {
			return obj.getRole();
		}
	};

	public ContactsModelPopulator() {
	}

	private static Function<Member, String> lastName = new Function<Member, String>() {
		@Override
		public String apply(Member obj) {
			return obj.getLastName();
		}
	};

	private final static Ordering<Member> byLastName = Ordering.natural().nullsLast().onResultOf(lastName);
	private final static Ordering<Member> byRole = Ordering.natural().nullsLast().onResultOf(roleName);
	private final static Ordering<Member> byRoleThenLastName =byRole.compound(byLastName);

	private InstanceSpecificApiClient api;
	private LoggedInUserService loggedInUserService;
	private PermissionsService permissionsService;
	private ActiveMemberFilter activeMemberFilter;

	@Autowired
	public ContactsModelPopulator(InstanceSpecificApiClient api, LoggedInUserService loggedInUserService, PermissionsService permissionsService, ActiveMemberFilter activeMemberFilter) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.permissionsService = permissionsService;
		this.activeMemberFilter = activeMemberFilter;
	}

	@RequiresSquadPermission(permission=Permission.VIEW_SQUAD_CONTACT_DETAILS)
	public void populateModel(final Squad squad, final ModelAndView mv) throws UnknownMemberException {
		mv.addObject("title", squad.getName() + " contacts");
		mv.addObject("squad", squad);

		final List<Member> activeMembers = byRoleThenLastName.sortedCopy(activeMemberFilter.extractActive(api.getSquadMembers(squad.getId())));
		final List<Member> redactedMembers = redactContentDetailsForMembers(loggedInUserService.getLoggedInMember(), activeMembers);
		mv.addObject("members", redactedMembers);
		
		final Set<String> emails = Sets.newHashSet();
		for (Member member : redactedMembers) {
			if (!Strings.isNullOrEmpty(member.getEmailAddress())) {
				emails.add(member.getEmailAddress());
			}
		}
		if (!emails.isEmpty()) {
			mv.addObject("emails", Lists.newArrayList(emails));
		}
	}
	
	private List<Member> redactContentDetailsForMembers(Member loggedInMember, List<Member> members) {
		List<Member> redactedMembers = Lists.newArrayList();
		for (Member member : members) {
			if (!permissionsService.canSeePhoneNumberForRower(loggedInMember, member)) {
				member.setContactNumber(null);
			}
			redactedMembers.add(member);
		}
		return redactedMembers;
	}

}