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
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.PreferredSquadService;
import uk.co.squadlist.web.views.ViewFactory;

import java.util.List;

@Controller
public class ContactsController {

    private static Logger log = LogManager.getLogger(ContactsController.class);

    private PreferredSquadService preferredSquadService;
    private ViewFactory viewFactory;
    private ContactsModelPopulator contactsModelPopulator;
    private LoggedInUserService loggedInUserService;

    @Autowired
    public ContactsController(PreferredSquadService preferredSquadService, ViewFactory viewFactory, ContactsModelPopulator contactsModelPopulator,
                              LoggedInUserService loggedInUserService) {
        this.preferredSquadService = preferredSquadService;
        this.viewFactory = viewFactory;
        this.contactsModelPopulator = contactsModelPopulator;
        this.loggedInUserService = loggedInUserService;
    }

    @RequestMapping("/contacts")
    public ModelAndView contacts() throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final List<Squad> allSquads = loggedInUserApi.getSquads();

        return viewFactory.getViewForLoggedInUser("contacts").
                addObject("squads", allSquads);    // TODO leaves squad null on view
    }

    @RequestMapping("/contacts/{squadId}")
    public ModelAndView squadContacts(@PathVariable String squadId) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();

        final Squad squadToShow = preferredSquadService.resolveSquad(squadId, loggedInUserApi);
        final List<Squad> allSquads = loggedInUserApi.getSquads();

        final ModelAndView mv = viewFactory.getViewForLoggedInUser("contacts").
                addObject("squads", allSquads);
        if (!allSquads.isEmpty()) {
            log.info("Squad to show: " + squadToShow);
            contactsModelPopulator.populateModel(squadToShow, mv, loggedInUserApi.getInstance(), loggedInUserApi);
        }
        return mv;
    }

}
