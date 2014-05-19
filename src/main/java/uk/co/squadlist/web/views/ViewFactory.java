package uk.co.squadlist.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.auth.LoggedInUserService;

@Component
public class ViewFactory {
	
	@Autowired
	private LoggedInUserService loggedInUserService;
	
	public ModelAndView getView(String templateName) {
		ModelAndView mv = new ModelAndView(templateName);
    	mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		return mv;
	}

}
