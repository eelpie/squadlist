package uk.co.squadlist.web.views;

import org.springframework.stereotype.Component;

@Component
public class TextHelper {
	
	public String text(String key) {
		if ("username".equals(key)) {
			return "Username";
		}
		return key;
	}

}
