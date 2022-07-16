package uk.co.squadlist.web.controllers.social;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.exceptions.InvalidMemberException;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.model.Member;

import java.io.IOException;

;

@Component
public class FacebookLinkedAccountsService {
	
	private final static Logger log = LogManager.getLogger(FacebookLinkedAccountsService.class);

	private final SquadlistApi api;
	
	@Autowired
	public FacebookLinkedAccountsService(SquadlistApiFactory squadlistApiFactory) throws IOException {
		this.api = squadlistApiFactory.createClient();
	}

	public boolean isLinked(Member member) {
		return member.getFacebookId() != null;
	}
	
	public void removeLinkage(String memberId) throws UnknownMemberException, InvalidMemberException {
		log.info("Removing linked Facebook account for logged in user " + memberId);		
		Member member = api.getMember(memberId);
		member.setFacebookId(null);
		api.updateMemberDetails(member);
	}
	
}
