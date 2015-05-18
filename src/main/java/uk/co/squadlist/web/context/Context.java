package uk.co.squadlist.web.context;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.Instance;

@Component
public class Context {

	private static final String INSTANCE = "instance";
	private static final String DUTCH = "nl";
	private static final String ENGLISH = "en";
	private static final String FRENCH = "fr";

	private final static Logger log = Logger.getLogger(Context.class);

	private InstanceSpecificApiClient api;

	private final HttpServletRequest request;

	@Autowired
	public Context(InstanceSpecificApiClient api, HttpServletRequest request) {
		this.api = api;
		this.request = request;
	}

	public String getTimeZone() {
		try {
			Instance instance = getInstance();
			return instance.getTimeZone();
		} catch (UnknownInstanceException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	private Instance getInstance() throws UnknownInstanceException {
		Instance instance = (Instance) request.getAttribute(INSTANCE);
		if (instance != null) {
			return instance;
		}
		instance = api.getInstance();
		request.setAttribute(INSTANCE, instance);
		return instance;
	}

	public String getLanguage() {
		if ("Europe/Amsterdam".equals(getTimeZone())) {
			return DUTCH;
		}
		if ("Europe/Paris".equals(getTimeZone())) {
			return FRENCH;
		}
		return ENGLISH;
	}

}