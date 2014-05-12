package uk.co.squadlist.web.views;

import org.springframework.stereotype.Component;

@Component
public class CssHelper {

	public String classFor(String availability) {		
		return availability.toLowerCase().replaceAll("\\s", "").replaceAll("-", "");
	}
	
}
