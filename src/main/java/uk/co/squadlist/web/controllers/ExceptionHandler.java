package uk.co.squadlist.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;

@Component
public class ExceptionHandler implements HandlerExceptionResolver, Ordered {

	private final static Logger log = Logger.getLogger(ExceptionHandler.class);
	
	@Override
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response,
			Object handler, 
			Exception e) {
		
		log.info("Handling exception of type: " + e.getClass());		
		if (e instanceof UnknownOutingException) {			
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return new ModelAndView("404");
		}		
		if (e instanceof UnknownSquadException) {			
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return new ModelAndView("404");
		}		
		if (e instanceof UnknownMemberException) {			
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return new ModelAndView("404");
		}
		
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		log.error("Returning 500 error", e);
		return new ModelAndView("500");
	}
	
	@Override
	public int getOrder() {
		return Integer.MIN_VALUE;
	}
	
}
