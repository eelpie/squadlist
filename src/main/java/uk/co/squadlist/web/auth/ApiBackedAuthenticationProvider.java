package uk.co.squadlist.web.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.SquadlistApi;

@Component("authenticationProvider")
public class ApiBackedAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
	
	private static Logger log = Logger.getLogger(ApiBackedAuthenticationProvider.class);
	    
    private static final String INVALID_USERNAME_OR_PASSWORD = "Invalid username or password";
    
    private SquadlistApi api;    
    
    @Autowired
    public ApiBackedAuthenticationProvider(SquadlistApi api) {
    	super();
		this.api = api;
	}

	@Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authToken) throws AuthenticationException {
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authToken) throws AuthenticationException {    	
    	final String password = authToken.getCredentials().toString();
    	try {
    		log.info("Attempting to auth user: " + username);
			if (api.auth(username, password)) {
				log.info("Auth successful for user: " + username);
				Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
				authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
				return new org.springframework.security.core.userdetails.User(username, password, authorities);    		
			}
			log.info("Auth attempt unsuccessful for user: " + username);
			
		} catch (org.apache.http.auth.AuthenticationException e) {
			log.error(e);
		} catch (ClientProtocolException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
    	
    	throw new BadCredentialsException(INVALID_USERNAME_OR_PASSWORD);	 
    }

}
