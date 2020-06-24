package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.context.GoverningBodyFactory;
import uk.co.squadlist.web.localisation.BritishRowing;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class GoverningBodyController {

    private final GoverningBodyFactory governingBodyFactory;
    private final ViewFactory viewFactory;

    @Autowired
    public GoverningBodyController(GoverningBodyFactory governingBodyFactory, ViewFactory viewFactory) {
        this.governingBodyFactory = governingBodyFactory;
        this.viewFactory = viewFactory;
    }

    @RequestMapping(value = "/governing-body/british-rowing", method = RequestMethod.GET)
    public ModelAndView member() throws Exception {
        final GoverningBody governingBody = governingBodyFactory.governingBodyFor("british-rowing");  // TODO take from path

        return viewFactory.getViewForLoggedInUser("governingBody").
                addObject("governingBody", governingBody).
                addObject("title", governingBody.getName()).
                addObject("ageGrades", governingBody.getAgeGrades()).
                addObject("statuses", governingBody.getStatusPoints()).
                addObject("boatSizes", governingBody.getBoatSizes());
    }

}