package uk.co.squadlist.web.controllers;

import java.util.List;

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

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.ChangePassword;
import uk.co.squadlist.web.model.forms.MemberDetails;
import uk.co.squadlist.web.urls.UrlBuilder;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Controller
public class MembersController {
	
	private final static Logger log = Logger.getLogger(MembersController.class);
	
	private final InstanceSpecificApiClient api;
	private final LoggedInUserService loggedInUserService;
	private final UrlBuilder urlBuilder;
	
	@Autowired
	public MembersController(InstanceSpecificApiClient api, LoggedInUserService loggedInUserService, UrlBuilder urlBuilder) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.urlBuilder = urlBuilder;
	}

	@RequestMapping("/member/{id}")
    public ModelAndView member(@PathVariable String id) throws Exception {
		final Member members = api.getMemberDetails(id);

		final ModelAndView mv = new ModelAndView("memberDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		mv.addObject("member", members);
    	mv.addObject("title", members.getFirstName() + " " + members.getLastName());
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
		
		final Squad squad = memberDetails.getSquad() != null && !memberDetails.getSquad().isEmpty() ? api.getSquad(memberDetails.getSquad()) : null;	// TODO push to spring
		final Member newMember = api.createMember(memberDetails.getFirstName(), 
				memberDetails.getLastName(),
				squad,
				memberDetails.getEmailAddress(),
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
		
		final Member member = api.getMemberDetails(loggedInUserService.getLoggedInUser());
    	
		log.info("Requesting change password for member: " + member.getId());
    	if (api.changePassword(member.getId(), changePassword.getCurrentPassword(), changePassword.getNewPassword())) {	
    		return new ModelAndView(new RedirectView(urlBuilder.memberUrl(member)));
    	} else {
    		result.addError(new ObjectError("changePassword", "Change password failed"));
    		return renderChangePasswordForm(changePassword);
    	}    	
    }
	
	@RequestMapping(value="/member/{id}/edit", method=RequestMethod.GET)
    public ModelAndView updateMember(@PathVariable String id) throws Exception {	
		final Member member = api.getMemberDetails(id);
		
		final MemberDetails memberDetails = new MemberDetails();
		memberDetails.setFirstName(member.getFirstName());
		memberDetails.setLastName(member.getLastName());
		memberDetails.setEmailAddress(member.getEmailAddress());
		memberDetails.setContactNumber(member.getContactNumber());
		memberDetails.setRegistrationNumber(member.getRegistrationNumber());
		memberDetails.setRowingPoints(member.getRowingPoints());
		memberDetails.setScullingPoints(member.getScullingPoints());
		memberDetails.setSquad(!member.getSquads().isEmpty() ? member.getSquads().get(0).getId() : null);
		return renderEditMemberDetailsForm(memberDetails, member.getId());
    }
	
	@RequestMapping(value="/member/{id}/edit", method=RequestMethod.POST)
    public ModelAndView updateMemberSubmit(@PathVariable String id, @Valid @ModelAttribute("member") MemberDetails memberDetails, BindingResult result) throws Exception {
		final Member member = api.getMemberDetails(id);
		
		final List<Squad> squads = Lists.newArrayList();
		String requestedSquadId = memberDetails.getSquad();
		log.info("Requested squad: " + requestedSquadId);
		if (!Strings.isNullOrEmpty(requestedSquadId)) {
			try {
				squads.add(api.getSquad(requestedSquadId));
			} catch (UnknownSquadException e) {
				log.warn("Rejecting unknown squad: " + requestedSquadId);
				result.addError(new ObjectError("member.squad", "Unknown squad"));
			}
		}		
		log.info("Assigned squads: " + squads);
		
		if (result.hasErrors()) {
			return renderEditMemberDetailsForm(memberDetails, member.getId());
		}
		
		log.info("Updating member details: " + member.getId());		
		member.setFirstName(memberDetails.getFirstName());
		member.setLastName(memberDetails.getLastName());
		member.setEmailAddress(memberDetails.getEmailAddress());
		member.setContactNumber(memberDetails.getContactNumber());
		member.setRowingPoints(memberDetails.getRowingPoints());
		member.setScullingPoints(memberDetails.getScullingPoints());
		member.setRegistrationNumber(memberDetails.getRegistrationNumber());
		member.setSquads(squads);
		
		
		
		api.updateMemberDetails(member);		
		return new ModelAndView(new RedirectView(urlBuilder.memberUrl(member)));	
    }

	private ModelAndView renderNewMemberForm() {
		final ModelAndView mv = new ModelAndView("newMember");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		mv.addObject("squads", api.getSquads());
		return mv;
	}
	
	private ModelAndView renderEditMemberDetailsForm(MemberDetails memberDetails, String memberId) {
		ModelAndView mv = new ModelAndView("editMemberDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("member", memberDetails);
    	mv.addObject("memberId", memberId);
    	return mv;
	}
	
	private ModelAndView renderChangePasswordForm(ChangePassword changePassword) throws UnknownMemberException {
		final ModelAndView mv = new ModelAndView("changePassword");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		mv.addObject("member", api.getMemberDetails(loggedInUserService.getLoggedInUser()));
    	mv.addObject("changePassword", changePassword);
    	mv.addObject("title", "Change password");
    	return mv;
	}
	
}
