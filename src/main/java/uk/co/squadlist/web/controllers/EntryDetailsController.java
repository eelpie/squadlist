package uk.co.squadlist.web.controllers;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.views.CSVLinePrinter;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class EntryDetailsController {
		
	private InstanceSpecificApiClient api;
	private PreferedSquadService preferedSquadService;
	private ViewFactory viewFactory;
	private EntryDetailsModelPopulator entryDetailsModelPopulator;
	private GoverningBody governingBody;
	private CSVLinePrinter csvLinePrinter;
	
	public EntryDetailsController() {
	}
	
	@Autowired
	public EntryDetailsController(InstanceSpecificApiClient api, PreferedSquadService preferedSquadService, ViewFactory viewFactory, 
			EntryDetailsModelPopulator entryDetailsModelPopulator, GoverningBody governingBody, CSVLinePrinter csvLinePrinter) {
		this.api = api;
		this.preferedSquadService = preferedSquadService;
		this.viewFactory = viewFactory;
		this.entryDetailsModelPopulator = entryDetailsModelPopulator;
		this.governingBody = governingBody;
		this.csvLinePrinter = csvLinePrinter;
	}
	
	@RequestMapping("/entrydetails/{squadId}")
    public ModelAndView entrydetails(@PathVariable String squadId) throws Exception {
    	final ModelAndView mv = viewFactory.getView("entryDetails");
    	mv.addObject("squads", api.getSquads());
    	mv.addObject("governingBody", governingBody);
    	
    	final Squad squadToShow = preferedSquadService.resolveSquad(squadId);
		entryDetailsModelPopulator.populateModel(squadToShow, mv);
    	return mv;
    }
	
	@RequestMapping(value="/entrydetails/{squadId}.csv", method=RequestMethod.GET)
    public void entrydetailsCSV(@PathVariable String squadId, HttpServletResponse response) throws Exception {
    	final Squad squadToShow = preferedSquadService.resolveSquad(squadId);
    	
		final String output = csvLinePrinter.printAsCSVLine(entryDetailsModelPopulator.getEntryDetailsRows(squadToShow));
		
    	response.setContentType("text/csv");
    	PrintWriter writer = response.getWriter();
		writer.print(output);
		writer.flush();
		return;
	}
	
}
