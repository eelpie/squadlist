package uk.co.squadlist.web.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.InstanceConfig;
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
    private final InstanceConfig instanceConfig;

    @Autowired
    public ContactsController(PreferredSquadService preferredSquadService,
                              ViewFactory viewFactory,
                              ContactsModelPopulator contactsModelPopulator,
                              LoggedInUserService loggedInUserService,
                              NavItemsBuilder navItemsBuilder,
                              InstanceConfig instanceConfig) {
        this.preferredSquadService = preferredSquadService;
        this.viewFactory = viewFactory;
        this.contactsModelPopulator = contactsModelPopulator;
        this.loggedInUserService = loggedInUserService;
        this.navItemsBuilder = navItemsBuilder;
        this.instanceConfig = instanceConfig;
    }

    @RequestMapping("/contacts")
    public ModelAndView contacts() throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        final List<uk.co.squadlist.model.swagger.Squad> allSquads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());

        uk.co.squadlist.model.swagger.Member loggedInMember = loggedInUserService.getLoggedInMember();
        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, "contacts", swaggerApiClientForLoggedInUser, instance);

        return viewFactory.getViewFor("contacts", instance).
                addObject("title", "Contacts").
                addObject("mavItems", navItems).
                addObject("squads", allSquads);    // TODO leaves squad null on view
    }

    @RequestMapping("/contacts/{squadId}")
    public ModelAndView squadContacts(@PathVariable String squadId) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        uk.co.squadlist.model.swagger.Member loggedInMember = loggedInUserService.getLoggedInMember();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        final uk.co.squadlist.model.swagger.Squad squadToShow = preferredSquadService.resolveSquad(squadId, swaggerApiClientForLoggedInUser, instance);
        final List<uk.co.squadlist.model.swagger.Squad> allSquads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInMember, "contacts", swaggerApiClientForLoggedInUser, instance);

        final ModelAndView mv = viewFactory.getViewFor("contacts", instance).
                addObject("title", "Contacts").
                addObject("navItems", navItems).
                addObject("squads", allSquads);

        if (!allSquads.isEmpty()) {
            contactsModelPopulator.populateModel(squadToShow, mv, swaggerApiClientForLoggedInUser, loggedInMember);
        }
        return mv;
    }

}
