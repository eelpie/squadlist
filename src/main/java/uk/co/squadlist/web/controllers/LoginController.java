package uk.co.squadlist.web.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.model.Instance;

@Controller
public class LoginController {
	
	private SquadlistApi api;
	
	@Autowired
	public LoginController(SquadlistApi api) {
		this.api = api;
	}

	@RequestMapping(value="/login", method=RequestMethod.GET)
    public ModelAndView member() throws Exception {
    	final ModelAndView mv = new ModelAndView("login");
    	mv.addObject("instance", getInstance());
    	return mv;
    }

	private Instance getInstance() {
		List<Instance> instances = api.getInstances();
		for (Instance instance : instances) {
			if (instance.getId().equals(SquadlistApi.INSTANCE)) {
				return instance;
			}
		}
		throw new RuntimeException("Unknown instance");
	}
	
}
