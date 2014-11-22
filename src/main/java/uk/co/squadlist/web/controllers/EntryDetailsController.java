package uk.co.squadlist.web.controllers;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.views.CSVLinePrinter;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

@Controller
public class EntryDetailsController {
		
	private final static Logger log = Logger.getLogger(EntryDetailsController.class);
		
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

		List<String> rowingPoints = Lists.newArrayList();
		List<String> scullingPoints = Lists.newArrayList();
		for (Member member: selectedMembers) {			
			rowingPoints.add(member.getRowingPoints());
			scullingPoints.add(member.getScullingPoints());			
		}
		
		final ModelAndView mv = viewFactory.getView("entryDetailsAjax");
		if (!selectedMembers.isEmpty()) {
			mv.addObject("members", selectedMembers);
			
			int crewSize = selectedMembers.size();
			final boolean isFullBoat = governingBody.getBoatSizes().contains(crewSize);
			mv.addObject("ok", isFullBoat);
			if (isFullBoat) {
				mv.addObject("rowingPoints", governingBody.getTotalPoints(rowingPoints));
				mv.addObject("rowingStatus", governingBody.getRowingStatus(rowingPoints));
				
				mv.addObject("scullingPoints", governingBody.getTotalPoints(scullingPoints));
				mv.addObject("scullingStatus", governingBody.getScullingStatus(scullingPoints));
				
				List<Date> datesOfBirth = Lists.newArrayList();
				for (Member member: selectedMembers) {
					datesOfBirth.add(member.getDateOfBirth());
				}
				
				Integer effectiveAge = governingBody.getEffectiveAge(datesOfBirth);
				if (effectiveAge != null) {
					mv.addObject("effectiveAge", effectiveAge);
					mv.addObject("ageGrade", governingBody.getAgeGrade(effectiveAge));
				}
			}
		}
		return mv;
	}
	
	@RequestMapping(value="/entrydetails/{squadId}.csv", method=RequestMethod.GET)
    public void entrydetailsCSV(@PathVariable String squadId, HttpServletResponse response) throws Exception {
    	final Squad squadToShow = preferedSquadService.resolveSquad(squadId);
    	final List<Member> squadMembers = api.getSquadMembers(squadToShow.getId());
    	
		final String output = csvLinePrinter.printAsCSVLine(entryDetailsModelPopulator.getEntryDetailsRows(squadMembers));
		
    	response.setContentType("text/csv");
    	PrintWriter writer = response.getWriter();
		writer.print(output);
		writer.flush();
		return;
	}
	
	@RequestMapping(value="/entrydetails/selected.csv", method=RequestMethod.GET)
    public void entrydetailsSelectedCSV(@RequestParam String members, HttpServletResponse response) throws Exception {
		log.info("Selected members: " + members);
    	List<Member> selectedMembers = Lists.newArrayList();    	
		final Iterator<String> iterator = Splitter.on(",").split(members).iterator();
    	while(iterator.hasNext()) {
    		final String selectedMemberId = iterator.next();
    		log.info("Selected member id: " + selectedMemberId);
			selectedMembers.add(api.getMemberDetails(selectedMemberId));
    	}
    	
		final String output = csvLinePrinter.printAsCSVLine(entryDetailsModelPopulator.getEntryDetailsRows(selectedMembers));
		
    	response.setContentType("text/csv");
    	PrintWriter writer = response.getWriter();
		writer.print(output);
		writer.flush();
		return;
	}
	
}
