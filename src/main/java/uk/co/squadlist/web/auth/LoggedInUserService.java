package uk.co.squadlist.web.auth;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.Member;

import javax.servlet.http.HttpServletRequest;

@Component
public class LoggedInUserService {

	private final static Logger log = Logger.getLogger(LoggedInUserService.class);

	private static final String SIGNED_IN_USER_ACCESS_TOKEN = "signedInAccessToken";

    private final InstanceSpecificApiClient api;
	private final HttpServletRequest request;

    @Autowired
	public LoggedInUserService(InstanceSpecificApiClient api, HttpServletRequest request) {
		this.api = api;
		this.request = request;
	}

	public Member getLoggedInMember() {
		String token = (String) request.getSession().getAttribute(SIGNED_IN_USER_ACCESS_TOKEN);
		if (token != null) {
			log.info("Found signed in user token; need to verify: " + token);
			Member verifiedMember = api.verify(token);
			log.info("Verified member: "+ verifiedMember);
			return verifiedMember;
		}

		log.info("No signed in user token found; returning null");
		return null;
	}

	public void setSignedIn(String token) {
		request.getSession().setAttribute(SIGNED_IN_USER_ACCESS_TOKEN, token);
	}

}
