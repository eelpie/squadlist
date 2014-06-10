package uk.co.squadlist.web.localisation;

import org.springframework.stereotype.Component;

@Component
public class BritishRowing implements GoverningBody {

	@Override
	public String getName() {
		return "British Rowing";
	}

}
