package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.model.swagger.Change;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.views.ViewFactory;

import java.util.List;

@Controller
public class ChangesController {

    private final ViewFactory viewFactory;
    private final SquadlistApiFactory squadlistApiFactory;

    @Autowired
    public ChangesController(ViewFactory viewFactory, SquadlistApiFactory squadlistApiFactory) {
        this.viewFactory = viewFactory;
        this.squadlistApiFactory = squadlistApiFactory;
    }

    @RequestMapping("/changes")
    public ModelAndView changes() throws Exception {
        List<Change> changes = squadlistApiFactory.createUnauthenticatedSwaggerClient().changeLogGet();

        return viewFactory.getViewForLoggedInUser("changes").addObject("changes", changes);
    }

}
