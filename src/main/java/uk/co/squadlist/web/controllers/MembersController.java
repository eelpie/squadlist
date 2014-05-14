package uk.co.squadlist.web.controllers;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.ChangePassword;
import uk.co.squadlist.web.model.forms.MemberDetails;
import uk.co.squadlist.web.urls.UrlBuilder;

@Controller
public class MembersController {
	
	private final static Logger log = Logger.getLogger(MembersController.class);
	
	private final SquadlistApi api;
	private final LoggedInUserService loggedInUserService;
	private final UrlBuilder urlBuilder;
	private final InstanceConfig instanceConfig;
	
	@Autowired
	public MembersController(SquadlistApi api, LoggedInUserService loggedInUserService, UrlBuilder urlBuilder, InstanceConfig instanceConfig) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.urlBuilder = urlBuilder;
		this.instanceConfig = instanceConfig;
	}

	@RequestMapping("/member/{id}")
    public ModelAndView member(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("memberDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("member", api.getMemberDetails(instanceConfig.getInstance(), id));
    	return mv;
    }
	
	@RequestMapping(value="/member/new", method=RequestMethod.GET)
    public ModelAndView newMember(@ModelAttribute("member") MemberDetails memberDetails) throws Exception {    	
		return renderNewMemberForm();
    }

	@RequestMapping(value="/member/new", method=RequestMethod.POST)
    public ModelAndView newMemberSubmit(@Valid @ModelAttribute("member") MemberDetails memberDetails, BindingResult result) throws Exception {		
		if (result.hasErrors()) {
			return renderNewMemberForm();
		}
		
		final String initialPassword = "password";	// TODO add to form
		
		final Squad squad = memberDetails.getSquad() != null && !memberDetails.getSquad().isEmpty() ? api.getSquad(instanceConfig.getInstance(), memberDetails.getSquad()) : null;	// TODO push to spring
		final Member newMember = api.createMember(instanceConfig.getInstance(),
				memberDetails.getFirstName(), 
				memberDetails.getLastName(),
				squad,
				null,
				initialPassword,
				null);
		
		return new ModelAndView(new RedirectView(urlBuilder.memberUrl(newMember)));
	}
	
	@RequestMapping(value="/change-password", method=RequestMethod.GET)
    public ModelAndView changePassword() throws Exception {
		return renderChangePasswordForm(new ChangePassword());
    }
	
	@RequestMapping(value="/change-password", method=RequestMethod.POST)
    public ModelAndView editMember(@Valid @ModelAttribute("changePassword") ChangePassword changePassword, BindingResult result) throws Exception {		
		if (result.hasErrors()) {
			return renderChangePasswordForm(changePassword);
		}
		
		final Member member = api.getMemberDetails(instanceConfig.getInstance(), loggedInUserService.getLoggedInUser());
    	
		log.info("Requesting change password for member: " + member.getId());
    	if (api.changePassword(instanceConfig.getInstance(), member.getId(), changePassword.getCurrentPassword(), changePassword.getNewPassword())) {	
    		return new ModelAndView(new RedirectView(urlBuilder.memberUrl(member)));
    	} else {
    		result.addError(new ObjectError("changePassword", "Change password failed"));
    		return renderChangePasswordForm(changePassword);
    	}
    	
    }
	
	@RequestMapping(value="/member/{id}/edit", method=RequestMethod.POST)
    public ModelAndView updateMember(@PathVariable String id, @Valid @ModelAttribute("member") MemberDetails memberDetails, BindingResult result) throws Exception {
		if (result.hasErrors()) {
			return renderEditMemberDetailsForm(memberDetails, id);
		}
		
		final Member member = api.getMemberDetails(instanceConfig.getInstance(), id);

		log.info("Updating member details: " + member.getId());		
		member.setFirstName(memberDetails.getFirstName());
		member.setLastName(memberDetails.getLastName());
		member.setEmailAddress(memberDetails.getEmailAddress());
		member.setContactNumber(memberDetails.getContactNumber());
		member.setRowingPoints(memberDetails.getRowingPoints());
		member.setScullingPoints(memberDetails.getScullingPoints());
		member.setRegistrationNumber(memberDetails.getRegistrationNumber());
		
		api.updateMemberDetails(instanceConfig.getInstance(), member);		
		return new ModelAndView(new RedirectView(urlBuilder.memberUrl(member)));	
    }

	private ModelAndView renderNewMemberForm() {
		final ModelAndView mv = new ModelAndView("newMember");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		mv.addObject("squads", api.getSquads(instanceConfig.getInstance()));
		return mv;
	}
	
	private ModelAndView renderEditMemberDetailsForm(MemberDetails memberDetails, String id) {
		ModelAndView mv = new ModelAndView("editMemberDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("member", memberDetails);
    	mv.addObject("memberId", id);
    	return mv;
	}
	
	private ModelAndView renderChangePasswordForm(ChangePassword changePassword) throws UnknownMemberException {
		final ModelAndView mv = new ModelAndView("changePassword");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		mv.addObject("member", api.getMemberDetails(instanceConfig.getInstance(), loggedInUserService.getLoggedInUser()));
    	mv.addObject("changePassword", changePassword);
    	return mv;
	}
	
}
