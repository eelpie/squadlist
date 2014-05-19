package uk.co.squadlist.web.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
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
import uk.co.squadlist.web.exceptions.propertyeditors.SquadPropertyEditor;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.ChangePassword;
import uk.co.squadlist.web.model.forms.MemberDetails;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.collect.Lists;

@Controller
public class MembersController {
	
	private final static Logger log = Logger.getLogger(MembersController.class);
	
	private final InstanceSpecificApiClient api;
	private final LoggedInUserService loggedInUserService;
	private final UrlBuilder urlBuilder;
	private final ViewFactory viewFactory;
	private final SquadPropertyEditor squadPropertyEditor;
	
	@Autowired
	public MembersController(InstanceSpecificApiClient api, LoggedInUserService loggedInUserService, UrlBuilder urlBuilder,
			ViewFactory viewFactory, SquadPropertyEditor squadPropertyEditor) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.urlBuilder = urlBuilder;
		this.viewFactory = viewFactory;
		this.squadPropertyEditor = squadPropertyEditor;
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
		final List<Squad> requestedSquads = extractAndValidateRequestedSquads(memberDetails, result);
		
		if (result.hasErrors()) {
			return renderNewMemberForm();
		}
		
		final String initialPassword = "password";	// TODO add to form		
		
		final Member newMember = api.createMember(memberDetails.getFirstName(), 
				memberDetails.getLastName(),
				requestedSquads,
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
		memberDetails.setSweepOarSide(member.getSweepOarSide());
		memberDetails.setSquads(member.getSquads());
		memberDetails.setEmergencyContactName(member.getEmergencyContactName());
		memberDetails.setEmergencyContactNumber(member.getEmergencyContactNumber());
		return renderEditMemberDetailsForm(memberDetails, member.getId(), member.getFirstName() + " " + member.getLastName());
    }
	
	@RequestMapping(value="/member/{id}/edit", method=RequestMethod.POST)
    public ModelAndView updateMemberSubmit(@PathVariable String id, @Valid @ModelAttribute("member") MemberDetails memberDetails, BindingResult result) throws Exception {
		final Member member = api.getMemberDetails(id);
		
		final List<Squad> squads = extractAndValidateRequestedSquads(memberDetails, result);
		
		if (result.hasErrors()) {
			return renderEditMemberDetailsForm(memberDetails, member.getId(), member.getFirstName() + " " + member.getLastName());
		}
		
		log.info("Updating member details: " + member.getId());		
		member.setFirstName(memberDetails.getFirstName());
		member.setLastName(memberDetails.getLastName());
		member.setEmailAddress(memberDetails.getEmailAddress());
		member.setContactNumber(memberDetails.getContactNumber());
		member.setRowingPoints(memberDetails.getRowingPoints());
		member.setScullingPoints(memberDetails.getScullingPoints());
		member.setRegistrationNumber(memberDetails.getRegistrationNumber());
		member.setEmergencyContactName(memberDetails.getEmergencyContactName());
		member.setEmergencyContactNumber(memberDetails.getEmergencyContactNumber());
		member.setSweepOarSide(memberDetails.getSweepOarSide());
		member.setSquads(squads);
		
		log.info("Submitting updated member: " + member);
		api.updateMemberDetails(member);
		return new ModelAndView(new RedirectView(urlBuilder.memberUrl(member)));	
    }
	
	private ModelAndView renderNewMemberForm() {
		final ModelAndView mv = new ModelAndView("newMember");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		mv.addObject("squads", api.getSquads());
		mv.addObject("title", "Adding a new member");
		return mv;
	}
	
	private ModelAndView renderEditMemberDetailsForm(MemberDetails memberDetails, String memberId, String title) {
		final ModelAndView mv = viewFactory.getView("editMemberDetails");
    	mv.addObject("member", memberDetails);
    	mv.addObject("memberId", memberId);
    	mv.addObject("title", title);
    	mv.addObject("squads", api.getSquads());    	
    	mv.addObject("pointsOptions", Lists.newArrayList("N", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"));
    	return mv;
	}
	
	private ModelAndView renderChangePasswordForm(ChangePassword changePassword) throws UnknownMemberException {
		final ModelAndView mv = viewFactory.getView("changePassword");
		mv.addObject("member", api.getMemberDetails(loggedInUserService.getLoggedInUser()));
    	mv.addObject("changePassword", changePassword);
    	mv.addObject("title", "Change password");
    	return mv;
	}
	
	private List<Squad> extractAndValidateRequestedSquads(MemberDetails memberDetails, BindingResult result) {
		final List<Squad> squads = Lists.newArrayList();
		if (memberDetails.getSquads() == null) {
			return squads;
		}
		
		for (Squad requestedSquadId : memberDetails.getSquads()) {					
			log.info("Requested squad: " + requestedSquadId);
			try {
				squads.add(api.getSquad(requestedSquadId.getId()));
			} catch (UnknownSquadException e) {
				log.warn("Rejecting unknown squad: " + requestedSquadId);
				result.addError(new ObjectError("member.squad", "Unknown squad"));			
			}
		}
		log.info("Assigned squads: " + squads);
		return squads;
	}
	
	@InitBinder
	public void binder(WebDataBinder binder) {
		log.debug("Registering property editor: " + squadPropertyEditor);
		binder.registerCustomEditor(Squad.class, squadPropertyEditor);
	}
	
}
