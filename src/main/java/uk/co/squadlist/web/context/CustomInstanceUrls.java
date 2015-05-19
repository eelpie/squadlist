package uk.co.squadlist.web.context;

import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

@Component
public class CustomInstanceUrls {

	private final Map<String, String> customInstanceUrls;

	@Autowired
	public CustomInstanceUrls( @Value("#{squadlist['customUrls']}") String customUrls) {
		customInstanceUrls = parseConfig(customUrls);
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

	private Map<String, String> parseConfig(final String customUrls) {
		Map<String, String> customInstanceUrls = Maps.newHashMap();
		Iterator<String> i = Splitter.on(",").split(customUrls).iterator();
		while (i.hasNext()) {
			String hostInstance = i.next();
			Iterator<String> hi = Splitter.on("|").split(hostInstance).iterator();
			if (hi.hasNext()) {
				String host = hi.next();
				if (hi.hasNext()) {
					String instance = hi.next();
					customInstanceUrls.put(host, instance);
				}
			}
		}
		return customInstanceUrls;
	}

}