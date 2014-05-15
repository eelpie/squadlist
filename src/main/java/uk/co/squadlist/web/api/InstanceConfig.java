package uk.co.squadlist.web.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class InstanceConfig {
	
	@Value("#{squadlist['instance']}")
	private String instance;
	
	public String getInstance() {
		return instance;
	}
	

}
