package uk.co.squadlist.web.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.annotations.RequiresOutingPermission;
import uk.co.squadlist.web.annotations.RequiresPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.*;
import uk.co.squadlist.web.model.AvailabilityOption;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.forms.OutingDetails;
import uk.co.squadlist.web.services.OutingAvailabilityCountsService;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.CsvOutputRenderer;
import uk.co.squadlist.web.views.DateFormatter;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Controller
public class OutingsController {

  private final static Logger log = Logger.getLogger(OutingsController.class);

  private final LoggedInUserService loggedInUserService;
  private final InstanceSpecificApiClient instanceSpecificApiClient;
  private final UrlBuilder urlBuilder;
  private final DateFormatter dateFormatter;
  private final PreferedSquadService preferedSquadService;
  private final ViewFactory viewFactory;
  private final OutingAvailabilityCountsService outingAvailabilityCountsService;
  private final ActiveMemberFilter activeMemberFilter;
  private final CsvOutputRenderer csvOutputRenderer;
  private final SquadlistApiFactory squadlistApiFactory;

  @Autowired
  public OutingsController(LoggedInUserService loggedInUserService, InstanceSpecificApiClient instanceSpecificApiClient, UrlBuilder urlBuilder,
                           DateFormatter dateFormatter, PreferedSquadService preferedSquadService, ViewFactory viewFactory,
                           OutingAvailabilityCountsService outingAvailabilityCountsService, ActiveMemberFilter activeMemberFilter,
                           CsvOutputRenderer csvOutputRenderer, SquadlistApiFactory squadlistApiFactory) throws IOException {
    this.loggedInUserService = loggedInUserService;
    this.instanceSpecificApiClient = instanceSpecificApiClient;
    this.urlBuilder = urlBuilder;
    this.dateFormatter = dateFormatter;
    this.preferedSquadService = preferedSquadService;
    this.viewFactory = viewFactory;
    this.outingAvailabilityCountsService = outingAvailabilityCountsService;
    this.activeMemberFilter = activeMemberFilter;
    this.csvOutputRenderer = csvOutputRenderer;
    this.squadlistApiFactory = squadlistApiFactory;
  }

  @RequestMapping("/outings")
  public ModelAndView outings(@RequestParam(required = false, value = "squad") String squadId,
                              @RequestParam(value = "month", required = false) String month) throws Exception {
    SquadlistApi loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

    final Squad squadToShow = preferedSquadService.resolveSquad(squadId);
    final ModelAndView mv = viewFactory.getViewForLoggedInUser("outings");
    if (squadToShow == null) {
      mv.addObject("title", "Outings");
      return mv;
    }

    Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
    Date endDate = DateHelper.endOfCurrentOutingPeriod().toDate();

    String title = squadToShow.getName() + " outings";
    if (month != null) {
      final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);  // TODO Can be moved to spring?
      startDate = monthDateTime.toDate();
      endDate = monthDateTime.plusMonths(1).toDate();
      title = squadToShow.getName() + " outings - " + dateFormatter.fullMonthYear(startDate);
    } else {
      mv.addObject("current", true);
    }

    mv.addObject("title", title);
    mv.addObject("squad", squadToShow);
    mv.addObject("startDate", startDate);
    mv.addObject("endDate", endDate);
    mv.addObject("month", month);
    mv.addObject("outingMonths", getOutingMonthsFor(squadToShow));

    final List<OutingWithSquadAvailability> squadOutings = loggedInUserApi.getSquadAvailability(squadToShow.getId(), startDate, endDate);
    mv.addObject("outings", squadOutings);
    mv.addObject("outingAvailabilityCounts", outingAvailabilityCountsService.buildOutingAvailabilityCounts(squadOutings));

    mv.addObject("squads", instanceSpecificApiClient.getSquads());
    return mv;
  }

  @RequestMapping("/outings/{id}")
  public ModelAndView outing(@PathVariable String id) throws Exception {
    SquadlistApi loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

    final Outing outing = loggedInUserApi.getOuting(id);

    final ModelAndView mv = viewFactory.getViewForLoggedInUser("outing");
    mv.addObject("title", outing.getSquad().getName() + " - " + dateFormatter.dayMonthYearTime(outing.getDate()));
    mv.addObject("outing", outing);
    mv.addObject("outingMonths", getOutingMonthsFor(outing.getSquad()));
    mv.addObject("squad", outing.getSquad());
    mv.addObject("squadAvailability", loggedInUserApi.getOutingAvailability(outing.getId()));
    mv.addObject("squads", instanceSpecificApiClient.getSquads());
    mv.addObject("members", activeMemberFilter.extractActive(loggedInUserApi.getSquadMembers(outing.getSquad().getId())));
    mv.addObject("month", ISODateTimeFormat.yearMonth().print(outing.getDate().getTime()));  // TODO push to date parser - local time
    return mv;
  }

  @RequestMapping("/outings/{id}.csv")
  public void outingCsv(@PathVariable String id, HttpServletResponse response) throws Exception {
    SquadlistApi loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

    final Outing outing = loggedInUserApi.getOuting(id);
    final List<Member> squadMembers = loggedInUserApi.getSquadMembers(outing.getSquad().getId());
    final Map<String, AvailabilityOption> outingAvailability = loggedInUserApi.getOutingAvailability(outing.getId());

    final List<List<String>> rows = Lists.newArrayList();
    for (Member member : squadMembers) {
      rows.add(Arrays.asList(
              dateFormatter.dayMonthYearTime(outing.getDate()),
              outing.getSquad().getName(),
              member.getDisplayName(),
              member.getRole(),
              outingAvailability.get(member.getId()) != null ? outingAvailability.get(member.getId()).getLabel() : null
      ));
    }
    csvOutputRenderer.renderCsvResponse(response, Lists.newArrayList("Date", "Squad", "Member", "Role", "Availability"), rows);
  }

  @RequiresPermission(permission = Permission.ADD_OUTING)
  @RequestMapping(value = "/outings/new", method = RequestMethod.GET)  // TODO fails hard if no squads are available
  public ModelAndView newOuting() throws Exception {
    final LocalDateTime defaultOutingDateTime = DateHelper.defaultOutingStartDateTime();
    final OutingDetails outingDefaults = new OutingDetails(defaultOutingDateTime);
    outingDefaults.setSquad(preferedSquadService.resolveSquad(null).getId());
    return renderNewOutingForm(outingDefaults);
  }

  @RequiresPermission(permission = Permission.ADD_OUTING)
  @RequestMapping(value = "/outings/new", method = RequestMethod.POST)
  public ModelAndView newOutingSubmit(@Valid @ModelAttribute("outing") OutingDetails outingDetails, BindingResult result) throws Exception {
    SquadlistApi loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

    if (result.hasErrors()) {
      return renderNewOutingForm(outingDetails);
    }

    try {
      final Outing newOuting = buildOutingFromOutingDetails(outingDetails, instanceSpecificApiClient.getInstance(), loggedInUserApi);
      if (outingDetails.getRepeats() != null && outingDetails.getRepeats() && outingDetails.getRepeatsCount() != null) {
        loggedInUserApi.createOuting(newOuting, outingDetails.getRepeatsCount());
      } else {
        loggedInUserApi.createOuting(newOuting, null);
      }

      final String outingsViewForNewOutingsSquadAndMonth = urlBuilder.outings(newOuting.getSquad(), new DateTime(newOuting.getDate()).toString("yyyy-MM"));
      return new ModelAndView(new RedirectView(outingsViewForNewOutingsSquadAndMonth));

    } catch (InvalidOutingException e) {
      result.addError(new ObjectError("outing", e.getMessage()));
      return renderNewOutingForm(outingDetails);

    } catch (Exception e) {
      result.addError(new ObjectError("outing", "Unknown error"));
      return renderNewOutingForm(outingDetails);
    }
  }

  @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
  @RequestMapping(value = "/outings/{id}/edit", method = RequestMethod.GET)
  public ModelAndView outingEdit(@PathVariable String id) throws Exception {
    SquadlistApi loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

    final Outing outing = loggedInUserApi.getOuting(id);

    final OutingDetails outingDetails = new OutingDetails(new LocalDateTime(outing.getDate()));
    outingDetails.setSquad(outing.getSquad().getId());
    outingDetails.setNotes(outing.getNotes());

    return renderEditOutingForm(outingDetails, outing);
  }

  @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
  @RequestMapping(value = "/outings/{id}/delete", method = RequestMethod.GET)
  public ModelAndView deleteOutingPrompt(@PathVariable String id) throws Exception {
    SquadlistApi loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
    final Outing outing = loggedInUserApi.getOuting(id);
    return viewFactory.getViewForLoggedInUser("deleteOuting").addObject("outing", outing);
  }

  @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
  @RequestMapping(value = "/outings/{id}/delete", method = RequestMethod.POST)
  public ModelAndView deleteOuting(@PathVariable String id) throws Exception {
    SquadlistApi loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
    final Outing outing = loggedInUserApi.getOuting(id);

    loggedInUserApi.deleteOuting(outing.getId());

    final String exitUrl = outing.getSquad() == null ? urlBuilder.outings(outing.getSquad()) : urlBuilder.outingsUrl();
    return new ModelAndView(new RedirectView(exitUrl));
  }

  @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
  @RequestMapping(value = "/outings/{id}/close", method = RequestMethod.GET)
  public ModelAndView closeOuting(@PathVariable String id) throws Exception {
    SquadlistApi loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

    final Outing outing = loggedInUserApi.getOuting(id);

    log.info("Closing outing: " + outing);
    outing.setClosed(true);
    loggedInUserApi.updateOuting(outing);

    return redirectToOuting(outing);
  }

  @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
  @RequestMapping(value = "/outings/{id}/reopen", method = RequestMethod.GET)
  public ModelAndView reopenOuting(@PathVariable String id) throws Exception {
    SquadlistApi loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

    final Outing outing = loggedInUserApi.getOuting(id);

    log.info("Reopening outing: " + outing);
    outing.setClosed(false);
    loggedInUserApi.updateOuting(outing);

    return redirectToOuting(outing);
  }

  @RequiresOutingPermission(permission = Permission.EDIT_OUTING)
  @RequestMapping(value = "/outings/{id}/edit", method = RequestMethod.POST)
  public ModelAndView editOutingSubmit(@PathVariable String id,
                                       @Valid @ModelAttribute("outing") OutingDetails outingDetails, BindingResult result) throws Exception {
    SquadlistApi loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

    final Outing outing = loggedInUserApi.getOuting(id);
    if (result.hasErrors()) {
      return renderEditOutingForm(outingDetails, outing);
    }
    try {
      final Outing updatedOuting = buildOutingFromOutingDetails(outingDetails, instanceSpecificApiClient.getInstance(), loggedInUserApi);
      updatedOuting.setId(id);

      loggedInUserApi.updateOuting(updatedOuting);
      return redirectToOuting(updatedOuting);

    } catch (InvalidOutingException e) {
      result.addError(new ObjectError("outing", e.getMessage()));
      return renderEditOutingForm(outingDetails, outing);

    } catch (Exception e) {
      log.error(e);
      result.addError(new ObjectError("outing", "Unknown exception"));
      return renderEditOutingForm(outingDetails, outing);
    }
  }

  @RequestMapping(value = "/availability/ajax", method = RequestMethod.POST)
  public ModelAndView updateAvailability(
          @RequestParam(value = "outing", required = true) String outingId,
          @RequestParam(value = "availability", required = true) String availability) throws Exception {
    SquadlistApi loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

    final Outing outing = loggedInUserApi.getOuting(outingId);

    if (!outing.isClosed()) {
      final OutingAvailability result = loggedInUserApi.setOutingAvailability(loggedInUserService.getLoggedInMember(), outing, getAvailabilityOptionById(availability));
      return viewFactory.getViewForLoggedInUser("includes/availability").addObject("availability", result.getAvailabilityOption());
    }

    throw new OutingClosedException();
  }

  private ModelAndView redirectToOuting(final Outing updatedOuting) {
    return new ModelAndView(new RedirectView(urlBuilder.outingUrl(updatedOuting)));
  }

  private ModelAndView renderNewOutingForm(OutingDetails outingDetails) throws UnknownMemberException, UnknownSquadException, UnknownInstanceException, SignedInMemberRequiredException {
    final Squad squad = preferedSquadService.resolveSquad(null);
    return viewFactory.getViewForLoggedInUser("newOuting").
            addObject("squads", instanceSpecificApiClient.getSquads()).
            addObject("squad", squad).
            addObject("outingMonths", getOutingMonthsFor(squad)).
            addObject("outing", outingDetails);
  }

  private ModelAndView renderEditOutingForm(OutingDetails outingDetails, Outing outing) throws UnknownMemberException, UnknownSquadException, UnknownInstanceException, SignedInMemberRequiredException {
    return viewFactory.getViewForLoggedInUser("editOuting").
            addObject("squads", instanceSpecificApiClient.getSquads()).
            addObject("squad", outing.getSquad()).
            addObject("outing", outingDetails).
            addObject("outingObject", outing).
            addObject("outingMonths", getOutingMonthsFor(outing.getSquad())).
            addObject("month", ISODateTimeFormat.yearMonth().print(outing.getDate().getTime()));
  }

  private AvailabilityOption getAvailabilityOptionById(String availabilityId) throws JsonParseException, JsonMappingException, HttpFetchException, IOException, UnknownAvailabilityOptionException {
    if (Strings.isNullOrEmpty(availabilityId)) {
      return null;
    }
    return instanceSpecificApiClient.getAvailabilityOption(availabilityId);
  }

  private Map<String, Integer> getOutingMonthsFor(final Squad squad) {
    return instanceSpecificApiClient.getOutingMonths(squad);
  }

  private Outing buildOutingFromOutingDetails(OutingDetails outingDetails, Instance instance, SquadlistApi loggedInUserApi) throws UnknownSquadException {
    Date date = outingDetails.toLocalTime().toDateTime(DateTimeZone.forID(instance.getTimeZone())).toDate();
    Squad squad = outingDetails.getSquad() != null ? loggedInUserApi.getSquad(outingDetails.getSquad()) : null;  // TODO validation
    String notes = outingDetails.getNotes();
    return new Outing(squad, date, notes);
  }

}
