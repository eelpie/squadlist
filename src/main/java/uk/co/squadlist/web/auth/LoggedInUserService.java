package uk.co.squadlist.web.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.model.Member;

@Component
public class LoggedInUserService {

	private final static Logger log = Logger.getLogger(LoggedInUserService.class);

	private static final String LOGGED_IN_MEMBER = "loggedInMember";

    private final InstanceSpecificApiClient api;
	private final HttpServletRequest request;

    @Autowired
	public LoggedInUserService(InstanceSpecificApiClient api, HttpServletRequest request) {
		this.api = api;
		this.request = request;
	}

	public Member getLoggedInMember() {
		Member loggedInMember = (Member) request.getAttribute(LOGGED_IN_MEMBER);
		if (loggedInMember != null) {
			log.debug("Returning cached logged in member");
			return loggedInMember;
		}

		log.debug("Fetching logged in member");
		try {
			loggedInMember = api.getMemberDetails(getLoggedInUsername());
			request.setAttribute(LOGGED_IN_MEMBER, loggedInMember);
			return loggedInMember;

		} catch (UnknownMemberException e) {
			throw new RuntimeException(e);
		}
	}

	private String getLoggedInUsername() {
		final UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		final String username = userDetails.getUsername();
		log.debug("Logged in user is: " + username);
		return username;
	}

}
