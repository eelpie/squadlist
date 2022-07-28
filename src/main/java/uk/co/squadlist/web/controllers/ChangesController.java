package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.model.swagger.Change;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.views.NavItemsBuilder;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import java.util.List;

@Controller
public class ChangesController {

    private final ViewFactory viewFactory;
    private final SquadlistApiFactory squadlistApiFactory;
    private final LoggedInUserService loggedInUserService;
    private final PreferredSquadService preferredSquadService;
    private final NavItemsBuilder navItemsBuilder;

    @Autowired
    public ChangesController(ViewFactory viewFactory,
                             SquadlistApiFactory squadlistApiFactory,
                             LoggedInUserService loggedInUserService,
                             PreferredSquadService preferredSquadService,
                             NavItemsBuilder navItemsBuilder) {
        this.viewFactory = viewFactory;
        this.squadlistApiFactory = squadlistApiFactory;
        this.loggedInUserService = loggedInUserService;
        this.preferredSquadService = preferredSquadService;
        this.navItemsBuilder = navItemsBuilder;
    }

    @RequestMapping("/changes")
    public ModelAndView changes() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        Instance instance = loggedInUserApi.getInstance();

        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInUser, loggedInUserApi.getSquads());
        List<NavItem> navItems =  navItemsBuilder.navItemsFor(loggedInUser, loggedInUserApi, preferredSquad, null);

        List<Change> changes = squadlistApiFactory.createUnauthenticatedSwaggerClient().changeLogGet();

        return viewFactory.getViewFor("changes", instance).
                addObject("title", "What's changed").
                addObject("navItems", navItems).
                addObject("changes", changes);
    }

}
