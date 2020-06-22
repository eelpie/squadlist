package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Change;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.views.ViewFactory;

import java.util.List;

@Controller
public class ChangesController {

    private final ViewFactory viewFactory;
    private final String apiUrl;

    @Autowired
    public ChangesController(ViewFactory viewFactory, @Value("${apiUrl}") String apiUrl) {
        this.viewFactory = viewFactory;
        this.apiUrl = apiUrl;
    }

    @RequestMapping("/changes")
    public ModelAndView changes() throws Exception {
        DefaultApi clientApi = new DefaultApi();
        clientApi.getApiClient().setBasePath(apiUrl);

        List<Change> changes = clientApi.changeLogGet();

        return viewFactory.getViewForLoggedInUser("changes").addObject("changes", changes);
    }

}
