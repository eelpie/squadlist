package uk.co.squadlist.web.context;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component
public class CustomInstanceUrls {

	private final  Map<String, String> customInstanceUrls;

	public CustomInstanceUrls() {
		customInstanceUrls = Maps.newHashMap();
		customInstanceUrls.put("avail.twickenhamrc.net", "twickenham");
	}

	public String customUrl(final String requestHost) {
		if (customInstanceUrls.containsKey(requestHost)) {
			return customInstanceUrls.get(requestHost);
		}
		return null;
	}

	public boolean hasCustomUrl(String instance) {
		return customInstanceUrls.containsValue(instance);
	}

	public String customUrlForInstance(String instance) {
		return customInstanceUrls.get(instance);
	}

}
