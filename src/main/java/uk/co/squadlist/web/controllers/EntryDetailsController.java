package uk.co.squadlist.web.controllers;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.views.CsvOutputRenderer;
import uk.co.squadlist.web.views.ViewFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Controller
public class EntryDetailsController {

  private final static Logger log = Logger.getLogger(EntryDetailsController.class);

  private final InstanceSpecificApiClient instanceSpecificApiClient;
  private final PreferedSquadService preferedSquadService;
  private final ViewFactory viewFactory;
  private final EntryDetailsModelPopulator entryDetailsModelPopulator;
  private final CsvOutputRenderer csvOutputRenderer;
  private final GoverningBodyFactory governingBodyFactory;
  private final SquadlistApi squadlistApi;
  private final LoggedInUserService loggedInUserService;
  private final SquadlistApiFactory squadlistApiFactory;

  @Autowired
  public EntryDetailsController(InstanceSpecificApiClient instanceSpecificApiClient, PreferedSquadService preferedSquadService, ViewFactory viewFactory,
                                EntryDetailsModelPopulator entryDetailsModelPopulator,
                                CsvOutputRenderer csvOutputRenderer, GoverningBodyFactory governingBodyFactory,
                                SquadlistApiFactory squadlistApiFactory, LoggedInUserService loggedInUserService) throws IOException {
    this.instanceSpecificApiClient = instanceSpecificApiClient;
    this.preferedSquadService = preferedSquadService;
    this.viewFactory = viewFactory;
    this.entryDetailsModelPopulator = entryDetailsModelPopulator;
    this.csvOutputRenderer = csvOutputRenderer;
    this.governingBodyFactory = governingBodyFactory;
    this.loggedInUserService = loggedInUserService;
    this.squadlistApiFactory = squadlistApiFactory;
    this.squadlistApi = squadlistApiFactory.createClient();
  }

  @RequestMapping("/entrydetails/{squadId}")
  public ModelAndView entrydetails(@PathVariable String squadId) throws Exception {
    final ModelAndView mv = viewFactory.getViewForLoggedInUser("entryDetails");
    mv.addObject("squads", instanceSpecificApiClient.getSquads());
    mv.addObject("governingBody", governingBodyFactory.getGoverningBody());

    final Squad squadToShow = preferedSquadService.resolveSquad(squadId);
    entryDetailsModelPopulator.populateModel(squadToShow, mv);
    return mv;
  }

  @RequestMapping("/entrydetails/ajax")
  public ModelAndView ajax(@RequestBody String json) throws Exception {
    SquadlistApi loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.getLoggedInMembersToken());

    List<Member> selectedMembers = Lists.newArrayList();

    JsonNode readTree = new ObjectMapper().readTree(json);
    Iterator<JsonNode> iterator = readTree.iterator();
    while (iterator.hasNext()) {
      selectedMembers.add(loggedInUserApi.getMember(iterator.next().asText()));
    }

    List<String> rowingPoints = Lists.newArrayList();
    List<String> scullingPoints = Lists.newArrayList();
    for (Member member : selectedMembers) {
      rowingPoints.add(member.getRowingPoints());
      scullingPoints.add(member.getScullingPoints());
    }

    final ModelAndView mv = viewFactory.getViewForLoggedInUser("entryDetailsAjax");
    if (!selectedMembers.isEmpty()) {
      mv.addObject("members", selectedMembers);

      final GoverningBody governingBody = governingBodyFactory.getGoverningBody();

      int crewSize = selectedMembers.size();
      final boolean isFullBoat = governingBody.getBoatSizes().contains(crewSize);
      mv.addObject("ok", isFullBoat);
      if (isFullBoat) {
        mv.addObject("rowingPoints", governingBody.getTotalPoints(rowingPoints));
        mv.addObject("rowingStatus", governingBody.getRowingStatus(rowingPoints));

        mv.addObject("scullingPoints", governingBody.getTotalPoints(scullingPoints));
        mv.addObject("scullingStatus", governingBody.getScullingStatus(scullingPoints));

        List<Date> datesOfBirth = Lists.newArrayList();
        for (Member member : selectedMembers) {
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

  @RequestMapping(value = "/entrydetails/{squadId}.csv", method = RequestMethod.GET)
  public void entrydetailsCSV(@PathVariable String squadId, HttpServletResponse response) throws Exception {
    viewFactory.getViewForLoggedInUser("entryDetails");  // TODO

    final Squad squadToShow = preferedSquadService.resolveSquad(squadId);
    final List<Member> squadMembers = squadlistApi.getSquadMembers(squadToShow.getId());

    List<List<String>> entryDetailsRows = entryDetailsModelPopulator.getEntryDetailsRows(squadMembers);
    csvOutputRenderer.renderCsvResponse(response, entryDetailsModelPopulator.getEntryDetailsHeaders(), entryDetailsRows);
  }

  @RequestMapping(value = "/entrydetails/selected.csv", method = RequestMethod.GET) // TODO Unused
  public void entrydetailsSelectedCSV(@RequestParam String members, HttpServletResponse response) throws Exception {
    log.info("Selected members: " + members);
    List<Member> selectedMembers = Lists.newArrayList();
    final Iterator<String> iterator = Splitter.on(",").split(members).iterator();
    while (iterator.hasNext()) {
      final String selectedMemberId = iterator.next();
      log.info("Selected member id: " + selectedMemberId);
      selectedMembers.add(squadlistApi.getMember(selectedMemberId));
    }

    csvOutputRenderer.renderCsvResponse(response, entryDetailsModelPopulator.getEntryDetailsHeaders(), entryDetailsModelPopulator.getEntryDetailsRows(selectedMembers));
  }

}