package uk.co.squadlist.web.auth;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.stereotype.Component;
import org.apache.log4j.Logger;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


@Component("mockAuthenticationProvider")
public class MockAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
	
    private static Logger log = Logger.getLogger(MockAuthenticationProvider.class);
    
    private static final String INVALID_USERNAME_OR_PASSWORD = "Invalid username or password";
    
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authToken) throws AuthenticationException {
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authToken) throws AuthenticationException {
    	if (username.equals("TEMPLEB")) {
    		final String password = authToken.getCredentials().toString();
    		final boolean isCorrectPassword = password.equals("password");
    		if (isCorrectPassword) {
    			Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
    			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    			return new org.springframework.security.core.userdetails.User(username, password, authorities);
    		}
    		log.debug("Incorrect password for username: " + username);
    		throw new BadCredentialsException(INVALID_USERNAME_OR_PASSWORD);    		
    	}
    	
    	throw new UsernameNotFoundException(INVALID_USERNAME_OR_PASSWORD);          
    }

}
