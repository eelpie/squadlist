package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.model.swagger.Squad;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.views.NavItemsBuilder;
import uk.co.squadlist.web.views.ViewFactory;
import uk.co.squadlist.web.views.model.NavItem;

import java.util.List;

@Controller
public class GoverningBodyController {

    private final GoverningBodyFactory governingBodyFactory;
    private final ViewFactory viewFactory;
    private final LoggedInUserService loggedInUserService;
    private final NavItemsBuilder navItemsBuilder;
    private final InstanceConfig instanceConfig;

    @Autowired
    public GoverningBodyController(GoverningBodyFactory governingBodyFactory,
                                   ViewFactory viewFactory,
                                   LoggedInUserService loggedInUserService,
                                   NavItemsBuilder navItemsBuilder,
                                   InstanceConfig instanceConfig) {
        this.governingBodyFactory = governingBodyFactory;
        this.viewFactory = viewFactory;
        this.loggedInUserService = loggedInUserService;
        this.navItemsBuilder = navItemsBuilder;
        this.instanceConfig = instanceConfig;
    }

    @RequestMapping(value = "/governing-body/british-rowing", method = RequestMethod.GET)
    public ModelAndView member() throws Exception {
        Member loggedInUser = loggedInUserService.getLoggedInMember();
        DefaultApi swaggerApiClientForLoggedInUser = loggedInUserService.getSwaggerApiClientForLoggedInUser();
        Instance instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.getInstance());
        List<Squad> squads = swaggerApiClientForLoggedInUser.getSquads(instance.getId());

        final GoverningBody governingBody = governingBodyFactory.governingBodyFor("british-rowing");  // TODO take from path

        List<NavItem> navItems = navItemsBuilder.navItemsFor(loggedInUser, null, swaggerApiClientForLoggedInUser, instance, squads);

        return viewFactory.getViewFor("governingBody", instance).
                addObject("governingBody", governingBody).
                addObject("title", governingBody.getName()).
                addObject("navItems",navItems).
                addObject("ageGrades", governingBody.getAgeGrades()).
                addObject("statuses", governingBody.getStatusPoints()).
                addObject("boatSizes", governingBody.getBoatSizes());
    }

}