package uk.co.squadlist.web.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import de.rrze.jpwgen.options.PwGeneratorOptionBuilder;
import de.rrze.jpwgen.utils.PwHelper;

@Component
public class PasswordGenerator {		// TODO push to the API and call as a service

	private final static List <String> BLACK_LIST = Lists.newArrayList("password", "badpassword");
	
	public String generateRandomPassword(int length) {		
		final PwGeneratorOptionBuilder options = new PwGeneratorOptionBuilder()
			.setNumberOfPasswords(10).setMaxAttempts(100)
			.setOnly1Digit()
			.setPasswordLength(length)
			.setDoNotEndWithSmallLetter().setIncludeAmbiguous(false)
			.setIncludeSymbols(false).setUseRandom()
			.setDoNotStartWithDigit();
		
		final List <String> passwords = PwHelper.process(options.build(), BLACK_LIST);
		return passwords.get(0);
	}
	
}
