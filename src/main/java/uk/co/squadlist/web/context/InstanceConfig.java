package uk.co.squadlist.web.context;

import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.localisation.BritishRowing;
import uk.co.squadlist.web.localisation.GoverningBody;

@Component
public class InstanceConfig {

	private final static Logger log = Logger.getLogger(InstanceConfig.class);

	private final RequestHostService requestHostService;
	private CustomInstanceUrls customInstanceUrls;

	private final String manuallyConfiguredInstanceToUseForAllRequests;

	@Autowired
	public InstanceConfig(RequestHostService requestHostService, @Value("#{squadlist['instance']}") String manuallyConfiguredInstanceToUseForAllRequests,
			CustomInstanceUrls customInstanceUrls) {
		this.requestHostService = requestHostService;
		this.manuallyConfiguredInstanceToUseForAllRequests = manuallyConfiguredInstanceToUseForAllRequests;
		this.customInstanceUrls = customInstanceUrls;
	}

	public String getInstance() {
		final String vhostName = getVhost();

		final String nonPrefixed = vhostName.replaceAll(".*-(.*)$", "$1");
		log.debug("Non prefixed vhost is: " + nonPrefixed);
		return nonPrefixed;
	}

	public String getVhost() {
		if (!Strings.isNullOrEmpty(manuallyConfiguredInstanceToUseForAllRequests)) {
			log.debug("Using manually configured instance: " + manuallyConfiguredInstanceToUseForAllRequests);
			return manuallyConfiguredInstanceToUseForAllRequests;
		}

		final String requestHost = requestHostService.getRequestHost();
		log.debug("Request host is: " + requestHost);

		final String customUrl = customInstanceUrls.customUrl(requestHost);
		if (customUrl != null) {
			return customUrl;
		}

		final String vhostName = requestHost.split("\\.")[0];
		log.debug("Request vhost is: " + vhostName);
		return vhostName;
	}

}
