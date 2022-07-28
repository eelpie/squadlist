package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Boat;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import java.util.ArrayList;

@Controller
public class BoatsController {

    private ViewFactory viewFactory;
    private LoggedInUserService loggedInUserService;

    @Autowired
    public BoatsController(ViewFactory viewFactory, LoggedInUserService loggedInUserService) {
        this.viewFactory = viewFactory;
        this.loggedInUserService = loggedInUserService;
    }

    @RequestMapping("/boats/{id}")
    public ModelAndView outing(@PathVariable String id) throws Exception {
        InstanceSpecificApiClient loggedInUserApi = loggedInUserService.getApiClientForLoggedInUser();
        Instance instance = loggedInUserApi.getInstance();
        Boat boat = loggedInUserApi.getBoat(id);

        return viewFactory.getViewFor("boat", instance).
                addObject("title", "View boat").
                addObject("navItems", new ArrayList<NavItem>()).
                addObject("boat", boat);
    }

}
