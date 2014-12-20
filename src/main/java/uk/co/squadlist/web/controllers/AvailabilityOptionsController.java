package uk.co.squadlist.web.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.InvalidAvailabilityOptionException;
import uk.co.squadlist.web.model.forms.AvailabilityOptionDetails;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class AvailabilityOptionsController {

	private InstanceSpecificApiClient api;
	private ViewFactory viewFactory;
	private UrlBuilder urlBuilder;

	public AvailabilityOptionsController() {
	}
	
	@Autowired
	public AvailabilityOptionsController(InstanceSpecificApiClient api, ViewFactory viewFactory, UrlBuilder urlBuilder) {
		this.api = api;
		this.viewFactory = viewFactory;
		this.urlBuilder = urlBuilder;
	}

	@RequiresPermission(permission=Permission.VIEW_ADMIN_SCREEN)
	@RequestMapping(value="/availability-option/new", method=RequestMethod.GET)
    public ModelAndView availability() throws Exception {
    	AvailabilityOptionDetails availabilityOption = new AvailabilityOptionDetails();
    	availabilityOption.setColour("green");
		return renderNewAvailabilityOptionForm(availabilityOption);
    }
	
	@RequiresPermission(permission=Permission.VIEW_ADMIN_SCREEN)
	@RequestMapping(value="/availability-option/new", method=RequestMethod.POST)
    public ModelAndView newSquadSubmit(@Valid @ModelAttribute("availabilityOptionDetails") AvailabilityOptionDetails availabilityOptionDetails, BindingResult result) {
		if (result.hasErrors()) {
			return renderNewAvailabilityOptionForm(availabilityOptionDetails);
		}
		
		try {
			api.createAvailabilityOption(availabilityOptionDetails.getName(),
					availabilityOptionDetails.getColour());			
			return redirectToAdmin();
			
		} catch (InvalidAvailabilityOptionException e) {
			result.rejectValue("name", null, e.getMessage());	         
			return renderNewAvailabilityOptionForm(availabilityOptionDetails);
		}
    }

	private ModelAndView renderNewAvailabilityOptionForm(AvailabilityOptionDetails availabilityOptionDetails) {
		return viewFactory.getView("newAvailabilityOption").addObject("availabilityOptionDetails", availabilityOptionDetails);
	}
	
	private ModelAndView redirectToAdmin() {
		return new ModelAndView(new RedirectView(urlBuilder.adminUrl()));
	}
	
}