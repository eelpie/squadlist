package uk.co.squadlist.web.api;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class InstanceConfig {
	
	private final static Logger log = Logger.getLogger(InstanceConfig.class);
	
	private final String instance;

	private final RequestHostService requestHostService;
	
	@Autowired
	public InstanceConfig(@Value("#{squadlist['instance']}") String instance, RequestHostService requestHostService) {
		this.instance = instance;
		this.requestHostService = requestHostService;
	}
	
	public String getInstance() {
		log.info("Request host is: " + requestHostService.getRequestHost());
		return instance;
	}

}
