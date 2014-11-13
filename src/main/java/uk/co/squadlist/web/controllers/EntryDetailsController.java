package uk.co.squadlist.web.controllers;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.views.CSVLinePrinter;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Controller
public class EntryDetailsController {
		
	private static final List<Integer> BOAT_SIZES = Lists.newArrayList(1, 2, 4, 8);
	
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
	
	@RequestMapping("/entrydetails/ajax")
	public ModelAndView ajax(@RequestBody String json) throws Exception {
		List<Member> selectedMembers = Lists.newArrayList();
		
		JsonNode readTree = new ObjectMapper().readTree(json);
		Iterator<JsonNode> iterator = readTree.iterator();
		while (iterator.hasNext()) {
			selectedMembers.add(api.getMemberDetails(iterator.next().asText()));
		}

		int rowingPoints = 0;
		int scullingPoints = 0;
		for (Member member: selectedMembers) {
			if (!Strings.isNullOrEmpty(member.getRowingPoints())) {
				rowingPoints = rowingPoints + Integer.parseInt(member.getRowingPoints());
			}
			if (!Strings.isNullOrEmpty(member.getScullingPoints())) {
				scullingPoints = scullingPoints + Integer.parseInt(member.getScullingPoints());
			}
		}
		
		final ModelAndView mv = viewFactory.getView("entryDetailsAjax");
		if (!selectedMembers.isEmpty()) {
			mv.addObject("members", selectedMembers);
			
			int crewSize = selectedMembers.size();
			final boolean isFullBoat = BOAT_SIZES.contains(crewSize);
			mv.addObject("ok", isFullBoat);
			if (isFullBoat) {
				mv.addObject("rowingPoints", rowingPoints);				
				mv.addObject("rowingStatus", governingBody.getRowingStatus(Integer.toString(rowingPoints), crewSize));
				
				mv.addObject("scullingPoints", scullingPoints);
				mv.addObject("scullingStatus", governingBody.getScullingStatus(Integer.toString(scullingPoints), crewSize));
			}
		}
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
