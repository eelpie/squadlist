package uk.co.squadlist.web.services;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PasswordGeneratorTest {

	@Test
	public void canGenerateRandomPasswordOfRequiredLength() throws Exception {
		final String password = new PasswordGenerator().generateRandomPassword(10);
		
		assertEquals(10, password.length());
	}
	
}
