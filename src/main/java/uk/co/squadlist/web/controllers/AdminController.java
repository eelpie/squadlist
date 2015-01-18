package uk.co.squadlist.web.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.CsvOutputRenderer;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Controller
public class AdminController {

	private final static Logger log = Logger.getLogger(AdminController.class);

	private final static Splitter COMMA_SPLITTER = Splitter.on(",");

	private InstanceSpecificApiClient api;
	private ViewFactory viewFactory;
	private GoverningBody governingBody;
	private ActiveMemberFilter activeMemberFilter;
	private CsvOutputRenderer csvOutputRenderer;
	private UrlBuilder urlBuilder;

	public AdminController() {
	}

	@Autowired
	public AdminController(InstanceSpecificApiClient api, ViewFactory viewFactory, GoverningBody governingBody,
			ActiveMemberFilter activeMemberFilter, CsvOutputRenderer csvOutputRenderer,
			UrlBuilder urlBuilder) {
		this.api = api;
		this.viewFactory = viewFactory;
		this.governingBody = governingBody;
		this.activeMemberFilter = activeMemberFilter;
		this.csvOutputRenderer = csvOutputRenderer;
		this.urlBuilder = urlBuilder;
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
		mv.addObject("activeMembers", activeMemberFilter.extractActive(members));
		mv.addObject("inactiveMembers", activeMemberFilter.extractInactive(members));
    	mv.addObject("admins", extractAdminUsersFrom(members));
    	mv.addObject("instance", api.getInstance());
    	mv.addObject("governingBody", governingBody);
    	mv.addObject("statistics", api.statistics());
    	return mv;
    }

	@RequiresPermission(permission=Permission.VIEW_ADMIN_SCREEN)
	@RequestMapping(value="/admin/admins", method=RequestMethod.GET)
	public ModelAndView setAdminsPrompt() throws Exception {
		List<Member> adminMembers = Lists.newArrayList();
		List<Member> availableMembers = Lists.newArrayList();
		for (Member member : api.getMembers()) {
			if (member.getAdmin()) {
				adminMembers.add(member);
			} else {
				availableMembers.add(member);
			}
		}
		return viewFactory.getView("editAdmins").addObject("admins", adminMembers).addObject("availableMembers", availableMembers);
	}

	@RequiresPermission(permission=Permission.VIEW_ADMIN_SCREEN)
	@RequestMapping(value="/admin/admins", method=RequestMethod.POST)
	public ModelAndView setAdmins(@RequestParam String admins) throws Exception {
	    log.info("Setting admins request: " + admins);
		final Set<String> updatedAdmins = Sets.newHashSet(COMMA_SPLITTER.split(admins).iterator());

		log.info("Setting admins to: " + updatedAdmins);
		api.setAdmins(updatedAdmins);

		return new ModelAndView(new RedirectView(urlBuilder.adminUrl()));
	}

	@RequiresPermission(permission=Permission.VIEW_ADMIN_SCREEN)
	@RequestMapping(value="/admin/export/members.csv", method=RequestMethod.GET)
    public void membersCSV(HttpServletResponse response) throws Exception {
		final List<List<String>> rows = Lists.newArrayList();
    	for (Member member : api.getMembers()) {
    		rows.add(Arrays.asList(new String[] {member.getFirstName(), member.getLastName(), member.getEmailAddress()}));
		}
    	csvOutputRenderer.renderCsvResponse(response, rows);
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