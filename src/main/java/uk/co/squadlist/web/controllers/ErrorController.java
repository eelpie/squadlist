package uk.co.squadlist.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {

	@RequestMapping("/error")
	public ModelAndView error() throws Exception {
		return new ModelAndView("404");
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}

}
