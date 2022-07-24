package uk.co.squadlist.web.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.views.NavItemsBuilder;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import java.util.List;

@Controller
public class ContactsController {

    private static final Logger log = LogManager.getLogger(ContactsController.class);

    private final PreferredSquadService preferredSquadService;
    private final ViewFactory viewFactory;
    private final ContactsModelPopulator contactsModelPopulator;
    private final LoggedInUserService loggedInUserService;
    private final NavItemsBuilder navItemsBuilder;

    @Autowired
    public ContactsController(PreferredSquadService preferredSquadService,
                              ViewFactory viewFactory,
                              ContactsModelPopulator contactsModelPopulator,
                              LoggedInUserService loggedInUserService,
                              NavItemsBuilder navItemsBuilder) {
        this.preferredSquadService = preferredSquadService;
        this.viewFactory = viewFactory;
        this.contactsModelPopulator = contactsModelPopulator;
        this.loggedInUserService = loggedInUserService;
        this.navItemsBuilder = navItemsBuilder;
    }

    @RequestMapping("/contacts")
    public ModelAndView contacts() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final List<Squad> allSquads = loggedInUserApi.getSquads();

        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInMember, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, loggedInUserApi, preferredSquad, "contacts");

        return viewFactory.getViewForLoggedInUser("contacts").
                addObject("title", "Contacts").
                addObject("squads", allSquads);    // TODO leaves squad null on view
    }

    @RequestMapping("/contacts/{squadId}")
    public ModelAndView squadContacts(@PathVariable String squadId) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Squad squadToShow = preferredSquadService.resolveSquad(squadId, loggedInUserApi);
        final List<Squad> allSquads = loggedInUserApi.getSquads();

        Member loggedInMember = loggedInUserService.getLoggedInMember();
        final Squad preferredSquad = preferredSquadService.resolvedPreferredSquad(loggedInMember, loggedInUserApi.getSquads());
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, loggedInUserApi, preferredSquad, "contacts");

        final ModelAndView mv = viewFactory.getViewForLoggedInUser("contacts").
                addObject("title", "Contacts").
                addObject("navItems", navItems).
                addObject("squads", allSquads);

        if (!allSquads.isEmpty()) {
            log.info("Squad to show: " + squadToShow);
            contactsModelPopulator.populateModel(squadToShow, mv, loggedInUserApi.getInstance(), loggedInUserApi, loggedInUserService.getLoggedInMember());
        }
        return mv;
    }

}
