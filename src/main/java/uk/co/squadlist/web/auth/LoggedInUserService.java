package uk.co.squadlist.web.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;

import javax.servlet.http.HttpServletRequest;

@Component
public class LoggedInUserService {

    private final static Logger log = LogManager.getLogger(LoggedInUserService.class);

    private static final String SIGNED_IN_USER_ACCESS_TOKEN = "signedInAccessToken";

    private final SquadlistApiFactory squadlistApiFactory;
    private final HttpServletRequest request;

    @Autowired
    public LoggedInUserService(SquadlistApiFactory squadlistApiFactory, HttpServletRequest request) {
        this.squadlistApiFactory = squadlistApiFactory;
        this.request = request;
    }

    public Member getLoggedInMember() throws SignedInMemberRequiredException, ApiException {
        String token = getLoggedInMembersToken();
        if (token != null) {
            log.debug("Found signed in user token; need to verify: " + token);
            Member verifiedMember = squadlistApiFactory.createSwaggerApiClientForToken(token).verifyPost();
            log.debug("Verified member: " + verifiedMember);
            return verifiedMember;
        }

        log.debug("No signed in user token found");
        throw new SignedInMemberRequiredException();
    }

    public DefaultApi getSwaggerApiClientForLoggedInUser() throws SignedInMemberRequiredException {
        final String token = getLoggedInMembersToken();
        if (token == null) {
            log.debug("No signed in user token found");
            throw new SignedInMemberRequiredException();
        }

        return squadlistApiFactory.createSwaggerApiClientForToken(token);
    }

    private String getLoggedInMembersToken() {
        return (String) request.getSession().getAttribute(SIGNED_IN_USER_ACCESS_TOKEN);
    }

    public void setSignedIn(String token) {
        request.getSession().setAttribute(SIGNED_IN_USER_ACCESS_TOKEN, token);
    }

    public void cleanSignedIn() {
        request.getSession().removeAttribute(SIGNED_IN_USER_ACCESS_TOKEN);
    }

}
