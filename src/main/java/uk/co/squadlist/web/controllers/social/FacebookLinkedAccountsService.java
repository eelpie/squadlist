package uk.co.squadlist.web.controllers.social;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.model.Member;

import java.io.IOException;

@Component
public class FacebookLinkedAccountsService {
	
	private final static Logger log = Logger.getLogger(FacebookLinkedAccountsService.class);

	private final SquadlistApi api;
	
	@Autowired
	public FacebookLinkedAccountsService(SquadlistApiFactory squadlistApiFactory) throws IOException {
		this.api = squadlistApiFactory.createClient();
	}
	
	public void linkAccount(String memberId, String facebookId) throws UnknownMemberException {
		log.info("Linking logged in user " + memberId + " to Facebook user: " + facebookId);
		
		final Member updateMember = api.getMember(memberId);
		updateMember.setFacebookId(facebookId);
		api.updateMemberDetails(updateMember);
	}
	
	public boolean isLinked(Member member) throws UnknownMemberException {
		return member.getFacebookId() != null;
	}
	
	public void removeLinkage(String memberId) throws UnknownMemberException {
		log.info("Removing linked Facebook account for logged in user " + memberId);		
		Member member = api.getMember(memberId);
		member.setFacebookId(null);
		api.updateMemberDetails(member);
	}
	
}
