package uk.co.squadlist.web.views;

import static org.junit.Assert.*;

import org.junit.Test;

public class CssHelperTest {

	@Test
	public void canFlattedAvailablityLabelIntoCssClassname() throws Exception {
		final String availabilityClass = new CssHelper().classFor("Available - 1st session only");
		assertEquals("available1stsessiononly", availabilityClass);
	}
	
}
