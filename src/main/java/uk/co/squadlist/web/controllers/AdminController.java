package uk.co.squadlist.web.controllers;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.runtime.directive.Foreach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.views.CSVLinePrinter;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.collect.Lists;

@Controller
public class AdminController {
		
	private InstanceSpecificApiClient api;
	private ViewFactory viewFactory;
	private GoverningBody governingBody;
	private CSVLinePrinter csvLinePrinter;
	
	public AdminController() {
	}
	
	@Autowired
	public AdminController(InstanceSpecificApiClient api, ViewFactory viewFactory, GoverningBody governingBody, CSVLinePrinter csvLinePrinter) {
		this.api = api;
		this.viewFactory = viewFactory;
		this.governingBody = governingBody;
		this.csvLinePrinter = csvLinePrinter;
	}
	
	@RequiresPermission(permission=Permission.VIEW_ADMIN_SCREEN)
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
    	mv.addObject("governingBody", governingBody);
    	return mv;
    }
	
	@RequiresPermission(permission=Permission.VIEW_ADMIN_SCREEN)
	@RequestMapping(value="/admin/export/members.csv", method=RequestMethod.GET)
    public void membersCSV(HttpServletResponse response) throws Exception {
		final List<List<String>> rows = Lists.newArrayList();
    	for (Member member : api.getMembers()) {
    		rows.add(Arrays.asList(new String[] {member.getFirstName(), member.getLastName(), member.getEmailAddress()}));
		}
    	
    	final String output = csvLinePrinter.printAsCSVLine(rows);
		
    	response.setContentType("text/csv");
    	PrintWriter writer = response.getWriter();
		writer.print(output);
		writer.flush();
		return;
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
