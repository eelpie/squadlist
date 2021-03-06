package uk.co.squadlist.web.context;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Component
public class InstanceConfig {

	private final static Logger log = LogManager.getLogger(InstanceConfig.class);

	private final RequestHostService requestHostService;

	private final String manuallyConfiguredInstanceToUseForAllRequests;

	@Autowired
	public InstanceConfig(RequestHostService requestHostService, @Value("${instance}") String manuallyConfiguredInstanceToUseForAllRequests) {
		this.requestHostService = requestHostService;
		this.manuallyConfiguredInstanceToUseForAllRequests = manuallyConfiguredInstanceToUseForAllRequests;
	}

	public String getInstance() {
		return getVhost();
	}

	public String getVhost() {
		if (!Strings.isNullOrEmpty(manuallyConfiguredInstanceToUseForAllRequests)) {
			log.debug("Using manually configured instance: " + manuallyConfiguredInstanceToUseForAllRequests);
			return manuallyConfiguredInstanceToUseForAllRequests;
		}

		final String requestHost = requestHostService.getRequestHost();
		log.debug("Request host is: " + requestHost);
		if (requestHost == null) {
			throw new RuntimeException("No vhost header seen and no manual host configured");
		}

		final String vhostName = requestHost.split("\\.")[0];
		log.debug("Request vhost is: " + vhostName);
		return vhostName;
	}

}
