package uk.co.squadlist.web.localisation.text;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

@Component
public class PropertiesFileParser {
	
	private final static Charset UTF8 = Charset.forName("UTF-8");
	private final static Logger log = LogManager.getLogger(PropertiesFileParser.class);

	public Map<String, String> readTextPropertiesFromFile(final String propertiesFilename) {
		try {
			final Map<String, String> textProperties = Maps.newHashMap();						
			Resource resource = new ClassPathResource(propertiesFilename);			
			if (resource != null) {				
				Properties properties = PropertiesLoaderUtils.loadProperties(new EncodedResource(resource, UTF8));				
				for (Object key : properties.keySet()) {
					textProperties.put((String) key, properties.getProperty((String) key));
				}			
				log.debug("Read properites from file " + propertiesFilename + ": " + textProperties);
			}
			return ImmutableMap.copyOf(textProperties);
			
		} catch (IOException e) {
			log.debug("Could not load properties file; returning empty: " + propertiesFilename);
		}

		return ImmutableMap.copyOf(Maps.<String, String>newHashMap());
	}
	
}
