package uk.co.squadlist.web.context;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class RequestHostService {

	private final static Logger log = Logger.getLogger(RequestHostService.class);

	private static final String X_FORWARDED_HOST = "x-forwarded-host";

	private final HttpServletRequest request;

	@Autowired
	public RequestHostService(HttpServletRequest request) {
		this.request = request;
	}

	public String getRequestHost() {
		final String serverName = request.getHeader(X_FORWARDED_HOST);
		log.debug("Request host is: " + serverName);
		return serverName;
	}

}
