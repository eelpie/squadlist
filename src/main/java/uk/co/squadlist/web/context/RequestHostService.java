package uk.co.squadlist.web.context;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class RequestHostService {

	private final static Logger log = LogManager.getLogger(RequestHostService.class);

	private final HttpServletRequest request;

	@Autowired
	public RequestHostService(HttpServletRequest request) {
		this.request = request;
	}

	public String getRequestHost() {
		// The requested host name is likely to be on the host of forwarded host header
		Stream<String> vhostHeaders = Stream.of("x-forwarded-host", "host");

		Optional<String> firstHost = vhostHeaders.map(h -> request.getHeader(h)).filter(v -> !Strings.isNullOrEmpty(v)).findFirst();
		if (firstHost.isPresent()) {
			log.debug("Request host is: " + firstHost.get());
			return firstHost.get();
		}

		return null;
	}

}
