package uk.co.squadlist.web.views;

import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.localisation.text.PropertiesFileParser;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

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
	
	public String text(String key, String... values) {
		if (text.containsKey(key)) {
			String value = text.get(key);
			Iterator<String> split = Splitter.on("{}").split(value).iterator();			
			Iterator<String> valueIterator = Lists.newArrayList(values).iterator();		
			
			StringBuilder output = new StringBuilder();			
			while (valueIterator.hasNext() ) {
				output.append(split.next());
				output.append(valueIterator.next());				
			}
			output.append(split.next());
			return output.toString();
		}
		return key;
	}

}
