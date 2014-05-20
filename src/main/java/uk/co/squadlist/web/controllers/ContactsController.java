package uk.co.squadlist.web.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

@Controller
public class ContactsController {
	
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
	
	private final InstanceSpecificApiClient api;
	private final PreferedSquadService preferedSquadService;
	private final ViewFactory viewFactory;
		
	@Autowired
	public ContactsController(InstanceSpecificApiClient api, PreferedSquadService preferedSquadService, ViewFactory viewFactory) {
		this.api = api;
		this.preferedSquadService = preferedSquadService;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping("/contacts")
    public ModelAndView contacts(@RequestParam(required=false, value="squad") String squadId) throws Exception {
    	final ModelAndView mv =  viewFactory.getView("contacts");
    	final List<Squad> allSquads = api.getSquads();
		mv.addObject("squads", allSquads);
    	if (!allSquads.isEmpty()) {
    		final Squad squadToShow = preferedSquadService.resolveSquad(squadId);
    		mv.addObject("title", squadToShow.getName() + " contacts");
    		mv.addObject("squad", squadToShow);    		
    		mv.addObject("members", byRoleThenLastName.sortedCopy(api.getSquadMembers(squadToShow.getId())));    		
    	}
    	return mv;
    }
	
}
