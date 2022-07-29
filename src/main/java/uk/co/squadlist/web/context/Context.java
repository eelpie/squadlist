package uk.co.squadlist.web.context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.Instance;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;

@Component
public class Context {

	private static final String INSTANCE = "instance";

	private final static Logger log = LogManager.getLogger(Context.class);

	private final HttpServletRequest request;
	private final SquadlistApi squadlistApi;
	private final InstanceConfig instanceConfig;
	private final LocaleResolver localeResolver;

	@Autowired
	public Context(SquadlistApiFactory squadlistApiFactory,
				   InstanceConfig instanceConfig,
				   LocaleResolver localeResolver,
				   HttpServletRequest request) throws IOException {
		this.instanceConfig = instanceConfig;
		this.squadlistApi = squadlistApiFactory.createClient();
		this.localeResolver = localeResolver;
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

	private Instance getInstance() throws UnknownInstanceException {	// TODO this is questionable
		Instance instance = (Instance) request.getAttribute(INSTANCE);
		if (instance != null) {
			return instance;
		}
		instance = squadlistApi.getInstance(instanceConfig.getInstance());
		request.setAttribute(INSTANCE, instance);
		return instance;
	}

	public Locale getLocale() {
		return localeResolver.resolveLocale(request);
	}

}