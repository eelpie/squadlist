package uk.co.squadlist.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.email.EmailService;
import uk.co.squadlist.web.exceptions.PermissionDeniedException;
import uk.co.squadlist.web.exceptions.UnknownAvailabilityOptionException;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;

import com.google.common.base.Throwables;

@Component
public class ExceptionHandler implements HandlerExceptionResolver, Ordered {

	private final static Logger log = Logger.getLogger(ExceptionHandler.class);

	private EmailService emailService;

	@Autowired
	public ExceptionHandler(EmailService emailService) {
		this.emailService = emailService;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response,
			Object handler,
			Exception e) {

		log.info("Handling exception of type: " + e.getClass());

		if (e instanceof UnknownAvailabilityOptionException) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return new ModelAndView("404");
		}
		if (e instanceof UnknownInstanceException) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return new ModelAndView("404");
		}
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

		if (e instanceof PermissionDeniedException) {
			response.setStatus(HttpStatus.FORBIDDEN.value());
			return new ModelAndView("403");
		}

		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		log.error("Returning 500 error", e);

		try {
			StringBuilder body = new StringBuilder(request.getServerName() + " " + request.getRequestURI() + "\n");
			body.append(Throwables.getStackTraceAsString(e));		
			emailService.sendPlaintextEmail("Squadlist website 500 error", "www@hampton.eelpieconsulting.co.uk", "tony@eelpieconsulting.co.uk", body.toString());
			
		} catch (Exception me) {
			log.error("Exception while trying to mail exception report", me);
		}

		return new ModelAndView("500");
	}

	@Override
	public int getOrder() {
		return Integer.MIN_VALUE;
	}

}
