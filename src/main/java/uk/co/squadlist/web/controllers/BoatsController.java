package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class BoatsController {

	private InstanceSpecificApiClient api;
	private ViewFactory viewFactory;

	public BoatsController() {
	}

	@Autowired
	public BoatsController(InstanceSpecificApiClient api, ViewFactory viewFactory) {
		this.api = api;
		this.viewFactory = viewFactory;
	}

	@RequestMapping("/boats/{id}")
    public ModelAndView outing(@PathVariable String id) throws Exception {
    	return viewFactory.getView("boat").addObject("boat", api.getBoat(id));
    }

}
