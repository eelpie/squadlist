package uk.co.squadlist.web.views;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.context.Context;

import java.util.Locale;

@Component
public class TextHelper {    // TODO why does this exist; feelds like something which Spring can probably do itself?

    private static final String DUTCH = "nl";
    private static final String ENGLISH = "en";
    private static final String FRENCH = "fr";

    private final Context context;
    private final MessageSource messageSource;

    private final static Logger log = LogManager.getLogger(TextHelper.class);

    @Autowired
    public TextHelper(Context context,
                      MessageSource messageSource) {
        this.context = context;
        this.messageSource = messageSource;
    }

    public String text(String key) {
        log.info("text requested: " + key);
        Locale locale = context.getLocale();
        String message = messageSource.getMessage(key, null, key, locale);
        log.info("Key " + key + " for locale " + locale + " resolved by + " + messageSource.getClass().getCanonicalName() + " to: " + message);
        return message;
    }

    public String text(String key, String... values) {
        log.info("text requested: " + key);
        Locale locale = context.getLocale();
        String message = messageSource.getMessage(key, values, key, locale);
        log.info("Key " + key + " for locale " + locale + " with values + " + values + " resolved by + " + messageSource.getClass().getCanonicalName() + " to: " + message);
        return message;
    }

}
