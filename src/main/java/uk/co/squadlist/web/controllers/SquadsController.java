package uk.co.squadlist.web.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.exceptions.InvalidSquadException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.SquadDetails;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

@Controller
public class SquadsController {

	private final static Logger log = Logger.getLogger(SquadsController.class);

	private final static Splitter COMMA_SPLITTER = Splitter.on(",");

	private InstanceSpecificApiClient instanceSpecificApiClient;
	private UrlBuilder urlBuilder;
	private ViewFactory viewFactory;
	private SquadlistApi squadlistApi;

	public SquadsController() {
	}

	@Autowired
	public SquadsController(InstanceSpecificApiClient instanceSpecificApiClient, UrlBuilder urlBuilder, ViewFactory viewFactory, SquadlistApiFactory squadlistApiFactory) {
		this.instanceSpecificApiClient = instanceSpecificApiClient;
		this.urlBuilder = urlBuilder;
		this.viewFactory = viewFactory;
		this.squadlistApi = squadlistApiFactory.createClient();
	}

	@RequestMapping(value="/squad/new", method=RequestMethod.GET)
    public ModelAndView newSquad(@ModelAttribute("squadDetails") SquadDetails squadDetails) throws Exception {
		return renderNewSquadForm(new SquadDetails());
    }

	@RequestMapping(value="/squad/new", method=RequestMethod.POST)
    public ModelAndView newSquadSubmit(@Valid @ModelAttribute("squadDetails") SquadDetails squadDetails, BindingResult result) {
		if (result.hasErrors()) {
			return renderNewSquadForm(squadDetails);
		}

		try {
			instanceSpecificApiClient.createSquad(squadDetails.getName());

			return new ModelAndView(new RedirectView(urlBuilder.adminUrl()));

		} catch (InvalidSquadException e) {
			log.info("Invalid squad");
			result.rejectValue("name", null, "squad name is already in use");
			return renderNewSquadForm(squadDetails);
		}
    }

	@RequiresPermission(permission=Permission.VIEW_ADMIN_SCREEN)
	@RequestMapping(value="/squad/{id}/delete", method=RequestMethod.GET)
	public ModelAndView deletePrompt(@PathVariable String id) throws UnknownSquadException {
		final Squad squad = squadlistApi.getSquad(id);
		return viewFactory.getViewForLoggedInUser("deleteSquadPrompt").addObject("squad", squad).addObject("title", "Delete squad");
	}

	@RequiresPermission(permission=Permission.VIEW_ADMIN_SCREEN)
	@RequestMapping(value="/squad/{id}/delete", method=RequestMethod.POST)
	public ModelAndView delete(@PathVariable String id) throws UnknownSquadException {
		final Squad squad = squadlistApi.getSquad(id);
		squadlistApi.deleteSquad(squad);
		return new ModelAndView(new RedirectView(urlBuilder.adminUrl()));
	}

	@RequestMapping(value="/squad/{id}/edit", method=RequestMethod.GET)
	public ModelAndView editSquad(@PathVariable String id) throws UnknownSquadException {
		final Squad squad = squadlistApi.getSquad(id);

		final SquadDetails squadDetails = new SquadDetails();
		squadDetails.setName(squad.getName());

		return renderEditSquadForm(squad, squadDetails);
	}

	@RequestMapping(value="/squad/{id}/edit", method=RequestMethod.POST)
    public ModelAndView editSquadSubmit(@PathVariable String id, @Valid @ModelAttribute("squadDetails") SquadDetails squadDetails, BindingResult result) throws UnknownSquadException, JsonGenerationException, JsonMappingException, HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, IOException, HttpFetchException {
		final Squad squad = squadlistApi.getSquad(id);
		if (result.hasErrors()) {
			return renderEditSquadForm(squad, squadDetails);
		}

		squad.setName(squadDetails.getName());
		log.info("Updating squad: " + squad);
		squadlistApi.updateSquad(squad);

		final Set<String> updatedSquadMembers = Sets.newHashSet(COMMA_SPLITTER.split(squadDetails.getMembers()).iterator());
		log.info("Setting squad members to " + updatedSquadMembers.size() + " members: " + updatedSquadMembers);
		squadlistApi.setSquadMembers(squad.getId(), updatedSquadMembers);

		return new ModelAndView(new RedirectView(urlBuilder.adminUrl()));
    }

	private ModelAndView renderNewSquadForm(SquadDetails squadDetails) {
		return viewFactory.getViewForLoggedInUser("newSquad").addObject("squadDetails", squadDetails);
	}

	private ModelAndView renderEditSquadForm(final Squad squad, final SquadDetails squadDetails) {
		final ModelAndView mv = viewFactory.getViewForLoggedInUser("editSquad");
		mv.addObject("squad", squad);
		mv.addObject("squadDetails", squadDetails);

		final List<Member> squadMembers = squadlistApi.getSquadMembers(squad.getId());
		mv.addObject("squadMembers", squadMembers);

		final List<Member> availableMembers = instanceSpecificApiClient.getMembers();
		availableMembers.removeAll(squadMembers);
		mv.addObject("availableMembers", availableMembers);
		return mv;
	}

}
