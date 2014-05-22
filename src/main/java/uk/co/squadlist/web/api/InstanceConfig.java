package uk.co.squadlist.web.api;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class InstanceConfig {
	
	private final static Logger log = Logger.getLogger(InstanceConfig.class);
	
	private final String instance;
	private final HttpServletRequest request;
	
	@Autowired
	public InstanceConfig(@Value("#{squadlist['instance']}") String instance, HttpServletRequest request) {
		this.instance = instance;
		this.request = request;
	}
	
	public String getInstance() {
		log.info("Request host is: " + request.getServerName());
		return instance;
	}

}
