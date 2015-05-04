package uk.co.squadlist.web.context;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;

@Component
public class Context {

	private final static Logger log = Logger.getLogger(Context.class);
	
	private final InstanceSpecificApiClient api;
	
	@Autowired
	public Context(InstanceSpecificApiClient api) {
		this.api = api;
	}

	public String getTimeZone() {
		try {
			return api.getInstance().getTimeZone();
		} catch (UnknownInstanceException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
}
