package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.views.NavItemsBuilder;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import java.util.List;

@Controller
public class GoverningBodyController {

    private final GoverningBodyFactory governingBodyFactory;
    private final ViewFactory viewFactory;
    private final LoggedInUserService loggedInUserService;
    private final PreferredSquadService preferredSquadService;
    private final NavItemsBuilder navItemsBuilder;

    @Autowired
    public GoverningBodyController(GoverningBodyFactory governingBodyFactory,
                                   ViewFactory viewFactory,
                                   LoggedInUserService loggedInUserService,
                                   PreferredSquadService preferredSquadService,
                                   NavItemsBuilder navItemsBuilder) {
        this.governingBodyFactory = governingBodyFactory;
        this.viewFactory = viewFactory;
        this.loggedInUserService = loggedInUserService;
        this.preferredSquadService = preferredSquadService;
        this.navItemsBuilder = navItemsBuilder;
    }

    @RequestMapping(value = "/governing-body/british-rowing", method = RequestMethod.GET)
    public ModelAndView member() throws Exception {
        Member loggedInUser = loggedInUserService.getLoggedInMember();
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final GoverningBody governingBody = governingBodyFactory.governingBodyFor("british-rowing");  // TODO take from path

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, loggedInUserApi, preferredSquad, null);

        return viewFactory.getViewForLoggedInUser("governingBody").
                addObject("governingBody", governingBody).
                addObject("title", governingBody.getName()).
                addObject("navItems",navItems).
                addObject("ageGrades", governingBody.getAgeGrades()).
                addObject("statuses", governingBody.getStatusPoints()).
                addObject("boatSizes", governingBody.getBoatSizes());
    }

}