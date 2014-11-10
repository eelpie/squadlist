package uk.co.squadlist.web.localisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;

public class BritishRowingTest {
	
	private final BritishRowing britishRowing = new BritishRowing();
	
	@Test
	public void canRecogniseValidBRRegistrationNumbers() throws Exception {		
		assertNull(britishRowing.checkRegistrationNumber("201505G1020791"));	
	}
	
	@Test
	public void shouldReturnValidationMessageForInvalidLookingRegistrationNumber() throws Exception {
		assertEquals("Not in the expected British Rowing format", britishRowing.checkRegistrationNumber("2013343434"));
	}
	
	@Test
	public void shouldDetectValidButExpiredRegistrationNumbers() throws Exception {
		assertEquals("Expired registration", britishRowing.checkRegistrationNumber("201405G1020791"));
	}
	
	@Test
	public void effectiveAgeForMastersIsTheAgeTheyWouldReachBy31DecOfTheCurrentYear() throws Exception {
		assertEquals(39, britishRowing.getEffectiveAge(new DateTime(1975, 12, 13, 0, 0, 0, 0).toDate()));		
		assertEquals(39, britishRowing.getEffectiveAge(new DateTime(1975, 1, 1, 0, 0, 0, 0).toDate()));		
		assertEquals(39, britishRowing.getEffectiveAge(new DateTime(1975, 12, 31, 0, 0, 0, 0).toDate()));		
	}
	
}