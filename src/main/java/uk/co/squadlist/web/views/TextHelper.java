package uk.co.squadlist.web.views;

import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.context.Context;
import uk.co.squadlist.web.localisation.text.PropertiesFileParser;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class TextHelper {

	private static final String DUTCH = "nl";
	private static final String ENGLISH = "en";
	private static final String FRENCH = "fr";

	private final Context context;

	private Map<String, Map<String, String>> text;

	@Autowired
	public TextHelper(PropertiesFileParser propertiesFileParser, Context context) {
		this.context = context;

		this.text = Maps.newHashMap();
		for (String availableLanguage : Lists.newArrayList(DUTCH, ENGLISH, FRENCH)) {
			text.put(availableLanguage, propertiesFileParser.readTextPropertiesFromFile(availableLanguage + ".properties"));
		}
	}

	public String text(String key) {
		if (text.get(context.getLanguage()).containsKey(key)) {
			return text.get(context.getLanguage()).get(key);
		}
		return key;
	}

	public String text(String key, String... values) {
		if (text.get(context.getLanguage()).containsKey(key)) {
			String value = text.get(context.getLanguage()).get(key);
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
