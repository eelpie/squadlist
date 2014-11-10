package uk.co.squadlist.web.auth;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.model.Member;

import com.google.common.collect.Lists;

@Component("userDetailsService")
public class ApiBackedUserDetailsService implements UserDetailsService {

	private static final Logger log = Logger.getLogger(ApiBackedUserDetailsService.class);

	private final InstanceSpecificApiClient api;

	@Autowired
	public ApiBackedUserDetailsService(InstanceSpecificApiClient api) {
		this.api = api;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			final Member preAuthenticatedMember = api.getMemberDetails(username);
			log.info("Pre authed user: " + preAuthenticatedMember);

			Collection<SimpleGrantedAuthority> authorities = Lists.newArrayList();
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
			return new org.springframework.security.core.userdetails.User(preAuthenticatedMember.getId(), "password", authorities);
			
		} catch (UnknownMemberException e) {
			throw new RuntimeException(e);
		}
	}

}
