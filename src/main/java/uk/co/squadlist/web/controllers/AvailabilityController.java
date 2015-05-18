package uk.co.squadlist.web.controllers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.AvailabilityOption;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferedSquadService;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.collect.Maps;

@Controller
public class AvailabilityController {

	private InstanceSpecificApiClient api;
	private PreferedSquadService preferedSquadService;
	private ViewFactory viewFactory;
	private ActiveMemberFilter activeMemberFilter;

	@Autowired
	public AvailabilityController(InstanceSpecificApiClient api, PreferedSquadService preferedSquadService, ViewFactory viewFactory, ActiveMemberFilter activeMemberFilter) {
		this.api = api;
		this.preferedSquadService = preferedSquadService;
		this.viewFactory = viewFactory;
		this.activeMemberFilter = activeMemberFilter;
	}

	@RequestMapping("/availability")
    public ModelAndView availability() throws Exception {
    	ModelAndView mv = viewFactory.getView("availability");
    	mv.addObject("squads", api.getSquads());
		return mv;
    }

	@RequestMapping("/availability/{squadId}")
    public ModelAndView squadAvailability(@PathVariable String squadId, @RequestParam(value = "month", required = false) String month) throws Exception {
    	ModelAndView mv = viewFactory.getView("availability");
    	mv.addObject("squads", api.getSquads());

    	final Squad squad = preferedSquadService.resolveSquad(squadId);

    	if (squad != null) {
			mv.addObject("squad", squad);
			mv.addObject("title", squad.getName() + " availability");
	    	mv.addObject("members", activeMemberFilter.extractActive(api.getSquadMembers(squad.getId())));

			if (api.getSquadMembers(squad.getId()).isEmpty()) {
				return mv;
			}

	    	Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
	    	Date endDate = DateHelper.endOfCurrentOutingPeriod().toDate();
			if (month != null) {
	    		final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);	// TODO Can be moved to spring?
	    		startDate = monthDateTime.toDate();
	    		endDate = monthDateTime.plusMonths(1).toDate();
	    	} else {
	    		mv.addObject("current", true);
	    	}

	    	final List<OutingWithSquadAvailability> squadAvailability = api.getSquadAvailability(squad.getId(), startDate, endDate);
	    	final List<Outing> outings = api.getSquadOutings(squad.getId(), startDate, endDate);

	    	mv.addObject("squadAvailability", decorateOutingsWithMembersAvailability(squadAvailability, outings));
	    	mv.addObject("outings", outings);
			mv.addObject("outingMonths", api.getSquadOutingMonths(squad.getId()));
			mv.addObject("month", month);
    	}
		return mv;
    }

	private Map<String, AvailabilityOption> decorateOutingsWithMembersAvailability(final List<OutingWithSquadAvailability> squadAvailability, final List<Outing> outings) {
		final Map<String, AvailabilityOption> allAvailability = Maps.newHashMap();
    	for (OutingWithSquadAvailability outingWithSquadAvailability : squadAvailability) {
			final Map<String, AvailabilityOption> outingAvailability = outingWithSquadAvailability.getAvailability();
			for (String member : outingAvailability.keySet()) {
				allAvailability.put(outingWithSquadAvailability.getOuting().getId() + "-" + member, outingAvailability.get(member));
			}
		}
		return allAvailability;
	}

}
