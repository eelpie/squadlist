package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Change;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.model.swagger.Squad;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.views.NavItemsBuilder;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import java.util.List;

@Controller
public class ChangesController {

    private final ViewFactory viewFactory;
    private final SquadlistApiFactory squadlistApiFactory;
    private final LoggedInUserService loggedInUserService;
    private final NavItemsBuilder navItemsBuilder;
    private final InstanceConfig instanceConfig;

    @Autowired
    public ChangesController(ViewFactory viewFactory,
                             SquadlistApiFactory squadlistApiFactory,
                             LoggedInUserService loggedInUserService,
                             NavItemsBuilder navItemsBuilder,
                             InstanceConfig instanceConfig) {
        this.viewFactory = viewFactory;
        this.squadlistApiFactory = squadlistApiFactory;
        this.loggedInUserService = loggedInUserService;
        this.navItemsBuilder = navItemsBuilder;
        this.instanceConfig = instanceConfig;
    }

    @RequestMapping("/changes")
    public ModelAndView changes() throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        final Member loggedInUser = loggedInUserService.getLoggedInMember();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
        List<Squad> squads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());

        List<NavItem> navItems =  navItemsBuilder.navItemsFor(loggedInUser, null, swaggerApiClientForLoggedInUser, instance, squads);

        List<Change> changes = squadlistApiFactory.createUnauthenticatedSwaggerClient().changeLogGet();

        return viewFactory.getViewFor("changes", instance).
                addObject("title", "What's changed").
                addObject("navItems", navItems).
                addObject("changes", changes);
    }

}
