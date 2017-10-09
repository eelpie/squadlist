package uk.co.squadlist.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.auth.LoggedInUserService;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {

	private final static Logger log = Logger.getLogger(ErrorController.class);

	private final HttpServletRequest request;

	@Autowired
	public ErrorController(HttpServletRequest request) {
		this.request = request;
	}

	@RequestMapping("/error")
	public ModelAndView error() throws Exception {
		log.warn("Error page shown");	// TODO how to get the original request before spring rewrites it to /error
		return new ModelAndView("404");
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}

}
