package uk.co.squadlist.web.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.collect.Lists;

@Controller
public class AdminController {
		
	private InstanceSpecificApiClient api;
	private ViewFactory viewFactory;
	
	public AdminController() {
	}
	
	@Autowired
	public AdminController(InstanceSpecificApiClient api, ViewFactory viewFactory) {
		this.api = api;
		this.viewFactory = viewFactory;
	}
	
	@RequiresPermission(permission=Permission.SEE_ADMIN_SCREEN)
	@RequestMapping(value="/admin", method=RequestMethod.GET)
    public ModelAndView member() throws Exception {
    	final ModelAndView mv = viewFactory.getView("admin");
    	mv.addObject("squads", api.getSquads());
    	mv.addObject("availabilityOptions", api.getAvailabilityOptions());
    	mv.addObject("title", "Admin");

    	final List<Member> members = api.getMembers();
		mv.addObject("members", members);
    	mv.addObject("admins", extractAdminUsersFrom(members));
    	mv.addObject("instance", api.getInstance());
    	return mv;
    }

	private List<Member> extractAdminUsersFrom(List<Member> members) {
		List<Member> admins = Lists.newArrayList();
		for (Member member : members) {
			if (member.getAdmin() != null && member.getAdmin()) {	// TODO should be boolean is the API knows that it is always present.
				admins.add(member);
			}
		}
		return admins;
	}
	
}
