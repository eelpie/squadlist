package uk.co.squadlist.web.views;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.localisation.text.PropertiesFileParser;

@Component
public class TextHelper {
	
	private Map<String, String> text;

	@Autowired
	public TextHelper(PropertiesFileParser propertiesFileParser) {
		this.text = propertiesFileParser.readTextPropertiesFromFile("EN.properties");
	}
	
	public String text(String key) {
		if (text.containsKey(key)) {
			return text.get(key);
		}		
		return key;
	}

}
