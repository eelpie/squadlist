package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import uk.co.squadlist.web.annotations.RequiresSquadPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.Permission;

@Component
public class ContactsModelPopulator {

	private static Function<Member, String> roleName = new Function<Member, String>() {
		@Override
		public String apply(Member obj) {
			return obj.getRole();
		}
	};
	
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
	
	public ContactsModelPopulator() {
	}
	
	@Autowired
	public ContactsModelPopulator(InstanceSpecificApiClient api) {
		this.api = api;
	}
	
	@RequiresSquadPermission(permission=Permission.VIEW_SQUAD_CONTACT_DETAILS)
	public void populateModel(final Squad squad, final ModelAndView mv) {
		mv.addObject("title", squad.getName() + " contacts");
		mv.addObject("squad", squad);    		
		mv.addObject("members", byRoleThenLastName.sortedCopy(api.getSquadMembers(squad.getId())));
	}
	
}
