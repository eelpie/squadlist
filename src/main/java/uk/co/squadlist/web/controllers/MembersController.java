package uk.co.squadlist.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.forms.MemberDetails;
import uk.co.squadlist.web.urls.UrlBuilder;

@Controller
public class MembersController {
	
	private static Logger log = Logger.getLogger(MembersController.class);
	
	private final SquadlistApi api;
	private final LoggedInUserService loggedInUserService;
	private final UrlBuilder urlBuilder;
	
	@Autowired
	public MembersController(SquadlistApi api, LoggedInUserService loggedInUserService, UrlBuilder urlBuilder) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.urlBuilder = urlBuilder;
	}

	@RequestMapping("/member/{id}")
    public ModelAndView member(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("memberDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("member", api.getMemberDetails(id));
    	return mv;
    }
	
	@RequestMapping("/member/{id}/edit")
    public ModelAndView editMember(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("editMemberDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("member", api.getMemberDetails(id));
    	return mv;
    }
	
	@RequestMapping(value="/member/{id}/edit", method=RequestMethod.POST)
    public ModelAndView updateMember(@PathVariable String id, @ModelAttribute("member") MemberDetails memberDetails) throws Exception {		
		final Member member = api.getMemberDetails(id);
		log.info("Updating member details: " + member.getId());
		api.updateMemberDetails(member);		
		return new ModelAndView(new RedirectView(urlBuilder.memberUrl(member)));	
    }
	
}
