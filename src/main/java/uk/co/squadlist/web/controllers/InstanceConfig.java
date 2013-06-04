package uk.co.squadlist.web.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InstanceConfig {
	
	@Value("#{squadlist['instance']}")
	private String instance;
	
	public String getInstance() {
		return instance;
	}
	

}
