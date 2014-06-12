package uk.co.squadlist.web.controllers;

import java.util.List;

import javax.validation.Valid;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
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

import uk.co.eelpieconsulting.common.email.EmailService;
import uk.co.squadlist.web.annotations.RequiresMemberPermission;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.InvalidMemberException;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.exceptions.propertyeditors.SquadPropertyEditor;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.ChangePassword;
import uk.co.squadlist.web.model.forms.MemberDetails;
import uk.co.squadlist.web.services.PasswordGenerator;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.email.EmailMessageComposer;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Controller
public class MembersController {
	
	private final static Logger log = Logger.getLogger(MembersController.class);

	private final static DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.basicDate();
	private static final String NOREPLY_SQUADLIST_CO_UK = "noreply@squadlist.co.uk";
	private static final List<String> SWEEP_OAR_SIDE_OPTIONS = Lists.newArrayList("Bow", "Stroke", "Bow/Stroke", "Stroke/Bow");
	
	private InstanceSpecificApiClient api;
	private LoggedInUserService loggedInUserService;
	private UrlBuilder urlBuilder;
	private ViewFactory viewFactory;
	private SquadPropertyEditor squadPropertyEditor;
	private EmailMessageComposer emailMessageComposer;
	private EmailService emailService;
	private PasswordGenerator passwordGenerator;
	private GoverningBody governingBody;
	
	public MembersController() {
	}
	
	@Autowired
	public MembersController(InstanceSpecificApiClient api, LoggedInUserService loggedInUserService, UrlBuilder urlBuilder,
			ViewFactory viewFactory, SquadPropertyEditor squadPropertyEditor,
			EmailMessageComposer emailMessageComposer, EmailService emailService,
			PasswordGenerator passwordGenerator, GoverningBody governingBody) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.urlBuilder = urlBuilder;
		this.viewFactory = viewFactory;
		this.squadPropertyEditor = squadPropertyEditor;
		this.emailMessageComposer = emailMessageComposer;
		this.emailService = emailService;
		this.passwordGenerator = passwordGenerator;
		this.governingBody = governingBody;
	}

	@RequiresMemberPermission(permission=Permission.VIEW_MEMBER_DETAILS)
	@RequestMapping("/member/{id}")
    public ModelAndView member(@PathVariable String id) throws Exception {
		final Member members = api.getMemberDetails(id);

		final ModelAndView mv = viewFactory.getView("memberDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		mv.addObject("member", members);
    	mv.addObject("title", members.getFirstName() + " " + members.getLastName());
    	return mv;
    }
	
	@RequiresPermission(permission=Permission.ADD_MEMBER)
	@RequestMapping(value="/member/new", method=RequestMethod.GET)
    public ModelAndView newMember(@ModelAttribute("memberDetails") MemberDetails memberDetails) throws Exception {    	
		return renderNewMemberForm();
    }

	@RequiresPermission(permission=Permission.ADD_MEMBER)
	@RequestMapping(value="/member/new", method=RequestMethod.POST)
    public ModelAndView newMemberSubmit(@Valid @ModelAttribute("memberDetails") MemberDetails memberDetails, BindingResult result) throws Exception {
		final List<Squad> requestedSquads = extractAndValidateRequestedSquads(memberDetails, result);
		
		if (result.hasErrors()) {
			return renderNewMemberForm();
		}
		
		final String initialPassword = passwordGenerator.generateRandomPassword(10);
		
		try {
			final Member newMember = api.createMember(memberDetails.getFirstName(), 
				memberDetails.getLastName(),
				requestedSquads,
				memberDetails.getEmailAddress(),
				initialPassword,
				null);
			
			sendNewMemberInvite(api.getInstance(), newMember, initialPassword);
			
			return new ModelAndView("memberAdded").
				addObject("member", newMember).
				addObject("initialPassword", initialPassword).
				addObject("inviteMessage", emailMessageComposer.composeNewMemberInviteMessage(api.getInstance(), newMember, initialPassword));
		
		} catch (InvalidMemberException e) {
			result.addError(new ObjectError("memberDetails", e.getMessage()));
			return renderNewMemberForm();
		}
	}
	
	private void sendNewMemberInvite(final Instance instance, final Member member, String initialPassword) throws EmailException {
		final String body = emailMessageComposer.composeNewMemberInviteMessage(instance, member, initialPassword);
		emailService.sendPlaintextEmail(instance.getName() + " availability invite", NOREPLY_SQUADLIST_CO_UK, member.getEmailAddress(), body);
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
	
	@RequiresMemberPermission(permission=Permission.EDIT_MEMBER_DETAILS)
	@RequestMapping(value="/member/{id}/edit", method=RequestMethod.GET)
    public ModelAndView updateMember(@PathVariable String id) throws Exception {	
		final Member member = api.getMemberDetails(id);
		
		final MemberDetails memberDetails = new MemberDetails();
		memberDetails.setFirstName(member.getFirstName());
		memberDetails.setLastName(member.getLastName());
		memberDetails.setKnownAs(member.getKnownAs());
		memberDetails.setGender(member.getGender());
		memberDetails.setDateOfBirth(DATE_FORMAT.print(member.getDateOfBirth().getTime()));
		memberDetails.setWeight(Integer.toString(member.getWeight()));
		memberDetails.setEmailAddress(member.getEmailAddress());
		memberDetails.setContactNumber(member.getContactNumber());
		memberDetails.setRegistrationNumber(member.getRegistrationNumber());
		memberDetails.setRowingPoints(member.getRowingPoints());
		memberDetails.setSculling(member.getSculling());
		memberDetails.setScullingPoints(member.getScullingPoints());
		memberDetails.setSweepOarSide(member.getSweepOarSide());
		memberDetails.setSquads(member.getSquads());
		memberDetails.setEmergencyContactName(member.getEmergencyContactName());
		memberDetails.setEmergencyContactNumber(member.getEmergencyContactNumber());
		return renderEditMemberDetailsForm(memberDetails, member.getId(), member.getFirstName() + " " + member.getLastName());
    }
	
	@RequiresMemberPermission(permission=Permission.EDIT_MEMBER_DETAILS)
	@RequestMapping(value="/member/{id}/edit", method=RequestMethod.POST)
    public ModelAndView updateMemberSubmit(@PathVariable String id, @Valid @ModelAttribute("member") MemberDetails memberDetails, BindingResult result) throws Exception {
		final Member member = api.getMemberDetails(id);
		
		final List<Squad> squads = extractAndValidateRequestedSquads(memberDetails, result);
		if (!Strings.isNullOrEmpty(memberDetails.getScullingPoints())) {
			if (!governingBody.getPointsOptions().contains(memberDetails.getScullingPoints())) {
				result.addError(new ObjectError("member.scullingPoints", "Invalid points option"));	
			}
		}
		if (!Strings.isNullOrEmpty(memberDetails.getRowingPoints())) {
			if (!governingBody.getPointsOptions().contains(memberDetails.getRowingPoints())) {
				result.addError(new ObjectError("member.rowingPoints", "Invalid points option"));	
			}
		}
		
 		if (result.hasErrors()) {
			return renderEditMemberDetailsForm(memberDetails, member.getId(), member.getFirstName() + " " + member.getLastName());
		}
		
		log.info("Updating member details: " + member.getId());		
		member.setFirstName(memberDetails.getFirstName());
		member.setLastName(memberDetails.getLastName());
		member.setKnownAs(memberDetails.getKnownAs());
		member.setGender(memberDetails.getGender());
		member.setDateOfBirth(!Strings.isNullOrEmpty(memberDetails.getDateOfBirth()) ? DATE_FORMAT.parseDateTime(memberDetails.getDateOfBirth()).toDate() : null);
		member.setWeight(Integer.parseInt(memberDetails.getWeight()));
		member.setEmailAddress(memberDetails.getEmailAddress());
		member.setContactNumber(memberDetails.getContactNumber());
		member.setRowingPoints(memberDetails.getRowingPoints());
		member.setSculling(memberDetails.getSculling());
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
		final ModelAndView mv = viewFactory.getView("newMember");
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
    	mv.addObject("pointsOptions", governingBody.getPointsOptions());
    	mv.addObject("sweepOarSideOptions", SWEEP_OAR_SIDE_OPTIONS);
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
