package uk.co.squadlist.web.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Component
public class Context {

	private final HttpServletRequest request;
	private final LocaleResolver localeResolver;

	@Autowired
	public Context(LocaleResolver localeResolver,
				   HttpServletRequest request) {
		this.localeResolver = localeResolver;
		this.request = request;
	}

	public Locale getLocale() {
		return localeResolver.resolveLocale(request);
	}

}