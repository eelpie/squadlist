package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class EntryDetailsController {
		
	private InstanceSpecificApiClient api;
	private PreferedSquadService preferedSquadService;
	private ViewFactory viewFactory;
	private EntryDetailsModelPopulator entryDetailsModelPopulator;
	
	public EntryDetailsController() {
	}
	
	@Autowired
	public EntryDetailsController(InstanceSpecificApiClient api, PreferedSquadService preferedSquadService, ViewFactory viewFactory, EntryDetailsModelPopulator entryDetailsModelPopulator) {
		this.api = api;
		this.preferedSquadService = preferedSquadService;
		this.viewFactory = viewFactory;
		this.entryDetailsModelPopulator = entryDetailsModelPopulator;
	}
	
	@RequestMapping("/entrydetails")
    public ModelAndView entrydetails(@RequestParam(required=false, value="squad") String squadId) throws Exception {
    	final ModelAndView mv = viewFactory.getView("entryDetails");
    	mv.addObject("squads", api.getSquads());
    	
    	final Squad squadToShow = preferedSquadService.resolveSquad(squadId);
		entryDetailsModelPopulator.populateModel(squadToShow, mv);
    	return mv;
    }

}
