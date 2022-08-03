package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Boat;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import java.util.ArrayList;

@Controller
public class BoatsController {

    private final ViewFactory viewFactory;
    private final LoggedInUserService loggedInUserService;
    private final InstanceConfig instanceConfig;

    @Autowired
    public BoatsController(ViewFactory viewFactory,
                           LoggedInUserService loggedInUserService,
                           InstanceConfig instanceConfig) {
        this.viewFactory = viewFactory;
        this.loggedInUserService = loggedInUserService;
        this.instanceConfig = instanceConfig;
    }

    @RequestMapping("/boats/{id}")
    public ModelAndView outing(@PathVariable String id) throws Exception {
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());

        Boat boat = swaggerApiClientForLoggedInUser.instancesInstanceBoatsIdGet(instance.getId(), id);

        return viewFactory.getViewFor("boat", instance).
                addObject("title", "View boat").
                addObject("navItems", new ArrayList<NavItem>()).
                addObject("boat", boat);
    }

}
