package uk.co.squadlist.web.api;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class InstanceConfig {
	
	private final static Logger log = Logger.getLogger(InstanceConfig.class);
	
	private final String manuallyConfiguredInstanceToUseForAllRequests;

	private final RequestHostService requestHostService;
	
	@Autowired
	public InstanceConfig(RequestHostService requestHostService, @Value("#{squadlist['instance']}") String manuallyConfiguredInstanceToUseForAllRequests) {
		this.requestHostService = requestHostService;
		this.manuallyConfiguredInstanceToUseForAllRequests = manuallyConfiguredInstanceToUseForAllRequests;
	}
	
	public String getInstance() {
		if (!Strings.isNullOrEmpty(manuallyConfiguredInstanceToUseForAllRequests)) {
			log.info("Using manually configured instance: " + manuallyConfiguredInstanceToUseForAllRequests);
			return manuallyConfiguredInstanceToUseForAllRequests;			
		}
		
		final String requestHost = requestHostService.getRequestHost();
		
		log.debug("Request host is: " + requestHost);
		final String vhostName = requestHost.split("\\.")[0];
		log.debug("Request vhost is: " + vhostName);		
		
		final String nonBetaVhostName = vhostName.replaceAll("(.*)beta$", "$1");
		log.debug("Non beta vhost is: " + nonBetaVhostName);
		return nonBetaVhostName;
	}

}
