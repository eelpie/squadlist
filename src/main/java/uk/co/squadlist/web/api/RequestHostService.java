package uk.co.squadlist.web.api;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RequestHostService {
	
	private final static Logger log = Logger.getLogger(RequestHostService.class);
	
	public String getRequestHost() {
		final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();

		final String serverName = request.getServerName();
		log.info("Request host is: " + serverName);
		return serverName;
	}

}
