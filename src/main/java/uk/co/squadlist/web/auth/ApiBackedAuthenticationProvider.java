package uk.co.squadlist.web.auth;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.model.Member;

import com.google.common.collect.Lists;

@Component("authenticationProvider")
public class ApiBackedAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
	
	private static final Logger log = Logger.getLogger(ApiBackedAuthenticationProvider.class);
	
    private static final String INVALID_USERNAME_OR_PASSWORD = "Invalid username or password";
    
    private final InstanceSpecificApiClient api;

	private final HttpServletRequest request;
    
    @Autowired
    public ApiBackedAuthenticationProvider(InstanceSpecificApiClient api, HttpServletRequest request) {
		this.api = api;
		this.request = request;
	}

	@Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authToken) throws AuthenticationException {
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authToken) throws AuthenticationException { 
    	final String password = authToken.getCredentials().toString();
    	log.info("Attempting to auth user: " + username);
    	
    	String preauthedUser = request.getHeader("squadlist-user");
		if (preauthedUser != null) {
			log.info("Pre authing user: " + preauthedUser);
			try {
				final Member preAuthenticatedMember = api.getMemberDetails(preauthedUser);
				
				Collection<SimpleGrantedAuthority> authorities = Lists.newArrayList();
				authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
				return new org.springframework.security.core.userdetails.User(preAuthenticatedMember.getId(), null, authorities);
				
			} catch (UnknownMemberException e) {
				log.error(e);
				throw new RuntimeException(e);
			}			
    	}   	
    	
		final Member authenticatedMember = api.auth(username, password);
		if (authenticatedMember != null) {
			log.info("Auth successful for user: " + username);
					
			Collection<SimpleGrantedAuthority> authorities = Lists.newArrayList();
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
			return new org.springframework.security.core.userdetails.User(authenticatedMember.getId(), password, authorities);		
		}
		
		log.info("Auth attempt unsuccessful for user: " + username);		
    	throw new BadCredentialsException(INVALID_USERNAME_OR_PASSWORD);	 
    }

}
