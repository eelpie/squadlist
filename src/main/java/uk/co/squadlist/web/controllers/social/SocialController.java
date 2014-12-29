package uk.co.squadlist.web.controllers.social;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.views.ViewFactory;

@Controller
public class SocialController {
		
	private final FacebookLinkedAccountsService facebookLinkedAccountsService;
	private final LoggedInUserService loggedInUserService;
	private final ViewFactory viewFactory;
	
	@Autowired
	public SocialController(LoggedInUserService loggedInUserService, FacebookLinkedAccountsService facebookLinkedAccountsService, 
			ViewFactory viewFactory) {
		this.viewFactory = viewFactory;
		this.loggedInUserService = loggedInUserService;
		this.facebookLinkedAccountsService = facebookLinkedAccountsService;
	}
	
	@RequestMapping(value="/social", method=RequestMethod.GET)
    public ModelAndView social() throws Exception {
    	final ModelAndView mv = viewFactory.getView("social"); 
    	mv.addObject("hasLinkedFacebook", facebookLinkedAccountsService.isLinked(loggedInUserService.getLoggedInMember()));
    	return mv;
    }
	
}
