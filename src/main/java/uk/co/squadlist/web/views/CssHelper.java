package uk.co.squadlist.web.views;

import org.springframework.stereotype.Component;
import uk.co.squadlist.model.swagger.AvailabilityOption;

@Component
public class CssHelper {

	public String classFor(AvailabilityOption availability) {
		if (availability != null) {
			return availability.getColour();
		}
		return null;
	}

}
