package uk.co.squadlist.web.views;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.context.Context;
import uk.co.squadlist.web.localisation.text.PropertiesFileParser;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class TextHelper {	// TODO why does this exist; feelds like something which Spring can probably do itself?

	private static final String DUTCH = "nl";
	private static final String ENGLISH = "en";
	private static final String FRENCH = "fr";

	private final Context context;
	private final MessageSource messageSource;

	private Map<String, Map<String, String>> text;

	private final static Logger log = LogManager.getLogger(TextHelper.class);

	@Autowired
	public TextHelper(PropertiesFileParser propertiesFileParser,
					  Context context,
					  MessageSource messageSource) {
		this.context = context;
		this.messageSource = messageSource;

		this.text = Maps.newHashMap();
		for (String availableLanguage : Lists.newArrayList(DUTCH, ENGLISH, FRENCH)) {
			text.put(availableLanguage, propertiesFileParser.readTextPropertiesFromFile(availableLanguage + ".properties"));
		}
	}

	public String text(String key) {
		log.info("text requested: " + key);
		Locale locale = context.getLocale();
		String message = messageSource.getMessage(key, null, key, locale);
		log.info("Key " + key + " for locale " + locale + " resolved by + " + messageSource.getClass().getCanonicalName() + " to: " + message);
		return message;
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
