package uk.co.squadlist.web.context;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Component
public class RequestHostService {

	private final static Logger log = LogManager.getLogger(RequestHostService.class);

	private static final String X_FORWARDED_HOST = "x-forwarded-host";

	private final HttpServletRequest request;

	@Autowired
	public RequestHostService(HttpServletRequest request) {
		this.request = request;
	}

	public String getRequestHost() {
		Enumeration<String> headerNames = request.getHeaderNames();
		Joiner on = Joiner.on(", ");
		log.info("All request headers: " + on.join(headerNames.asIterator()));

		final String xForwardedHost = request.getHeader(X_FORWARDED_HOST);
		if (Strings.isNullOrEmpty(xForwardedHost)) {
			log.debug("Request x-forwarded-host is: " + xForwardedHost);
			return xForwardedHost;
		}

		final String host = request.getHeader("host");
		if (!Strings.isNullOrEmpty(host)) {
			log.debug("Request host is: " + host);
			return host;
		}
		return null;
	}

}
