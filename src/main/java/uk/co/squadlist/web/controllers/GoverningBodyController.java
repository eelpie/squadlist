package uk.co.squadlist.web.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.localisation.BritishRowing;
import uk.co.squadlist.web.views.ViewFactory;

import com.google.common.collect.Maps;

@Controller
public class GoverningBodyController {
		
	private ViewFactory viewFactory;
	
	public GoverningBodyController() {
	}
	
	@Autowired
	public GoverningBodyController(ViewFactory viewFactory) {
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping(value="/governing-body/british-rowing", method=RequestMethod.GET)
    public ModelAndView member() throws Exception {
    	final ModelAndView mv = viewFactory.getView("governingBody");
    	
    	BritishRowing governingBody = new BritishRowing();	// TODO make general
    	
    	mv.addObject("title", governingBody.getName());

		mv.addObject("ageGrades", toStringMap(governingBody.getAgeGrades()));
    	mv.addObject("statuses", toStringMap(governingBody.getStatusPoints()));
    	return mv;
    }
	
	private Map<String, String> toStringMap(Map<String, Integer> map) {
		final Map<String, String> stringMap = Maps.newHashMap();		
		for (String key : map.keySet()) {
			stringMap.put(key, map.get(key).toString());
		}		
		return stringMap;
	}
	
}
