package uk.co.squadlist.web.auth;

import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("loggedInUserService")
public class LoggedInUserService {

    private static Logger log = Logger.getLogger(LoggedInUserService.class);
    
	public String getLoggedInUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final String username = userDetails.getUsername();
        log.info("Logged in user is: " + username);
		return username;
	}
	
}
