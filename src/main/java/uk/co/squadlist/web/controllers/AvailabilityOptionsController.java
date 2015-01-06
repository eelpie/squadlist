package uk.co.squadlist.web.controllers;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.InvalidAvailabilityOptionException;
import uk.co.squadlist.web.model.AvailabilityOption;
import uk.co.squadlist.web.model.forms.AvailabilityOptionDetails;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class AvailabilityOptionsController {

	private final static Logger log = Logger.getLogger(AvailabilityOptionsController.class);

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
	@RequestMapping(value="/availability-option/{id}/edit", method=RequestMethod.GET)
    public ModelAndView editPrompt(@PathVariable String id) throws Exception {
		final AvailabilityOption a = api.getAvailabilityOption(id);

		final AvailabilityOptionDetails availabilityOption = new AvailabilityOptionDetails();
    	availabilityOption.setName(a.getLabel());
    	availabilityOption.setColour(a.getColour());

		return renderEditAvailabilityOptionForm(availabilityOption, a);
    }

	@RequiresPermission(permission=Permission.VIEW_ADMIN_SCREEN)
	@RequestMapping(value="/availability-option/{id}/delete", method=RequestMethod.GET)
	public ModelAndView deletePrompt(@PathVariable String id) throws Exception {
		final AvailabilityOption a = api.getAvailabilityOption(id);
		return new ModelAndView("deleteAvailabilityOption").addObject("availabilityOption", a);
	}

	@RequiresPermission(permission=Permission.VIEW_ADMIN_SCREEN)
	@RequestMapping(value="/availability-option/{id}/delete", method=RequestMethod.POST)
	public ModelAndView delete(@PathVariable String id) throws Exception {
		final AvailabilityOption a = api.getAvailabilityOption(id);

		log.info("Deleting availability option: " + a);
		api.deleteAvailabilityOption(a);

		return redirectToAdmin();
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
			api.createAvailabilityOption(availabilityOptionDetails.getName(), availabilityOptionDetails.getColour());
			return redirectToAdmin();

		} catch (InvalidAvailabilityOptionException e) {
			result.rejectValue("name", null, e.getMessage());
			return renderNewAvailabilityOptionForm(availabilityOptionDetails);
		}
    }

	@RequiresPermission(permission=Permission.VIEW_ADMIN_SCREEN)
	@RequestMapping(value="/availability-option/{id}/edit", method=RequestMethod.POST)
    public ModelAndView editPost(@PathVariable String id, @Valid @ModelAttribute("availabilityOptionDetails") AvailabilityOptionDetails availabilityOptionDetails, BindingResult result) throws Exception {
		final AvailabilityOption a = api.getAvailabilityOption(id);
		if (result.hasErrors()) {
			return renderEditAvailabilityOptionForm(availabilityOptionDetails, a);
		}

		a.setLabel(availabilityOptionDetails.getName());
		a.setColour(availabilityOptionDetails.getColour());

		try {
			api.updateAvailabilityOption(a);

		} catch (InvalidAvailabilityOptionException e) {
			result.rejectValue("name", null, e.getMessage());
			return renderEditAvailabilityOptionForm(availabilityOptionDetails, a);
		}

		return redirectToAdmin();
    }

	private ModelAndView renderNewAvailabilityOptionForm(AvailabilityOptionDetails availabilityOptionDetails) {
		return viewFactory.getView("newAvailabilityOption").addObject("availabilityOptionDetails", availabilityOptionDetails);
	}

	private ModelAndView renderEditAvailabilityOptionForm(AvailabilityOptionDetails availabilityOptionDetails, AvailabilityOption a) {
		return viewFactory.getView("editAvailabilityOption").
				addObject("availabilityOptionDetails", availabilityOptionDetails).
				addObject("availabilityOption", a);
	}

	private ModelAndView redirectToAdmin() {
		return new ModelAndView(new RedirectView(urlBuilder.adminUrl()));
	}

}