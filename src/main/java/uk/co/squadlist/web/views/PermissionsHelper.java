package uk.co.squadlist.web.views;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;

@Deprecated // TODO makes API calls from the view; migrate to controller logic
@Component
public class PermissionsHelper {

	private final static Logger log = LogManager.getLogger(PermissionsHelper.class);
	
	private final LoggedInUserService loggedInUserService;
	private final PermissionsService permissionsService;
	
	@Autowired
	public PermissionsHelper(LoggedInUserService loggedInUserService, PermissionsService permissionsService) {
		this.loggedInUserService = loggedInUserService;
		this.permissionsService = permissionsService;
	}
	
	public boolean hasPermission(String permissionName) throws SignedInMemberRequiredException {
		final Permission permission = Permission.valueOf(permissionName);
		final Member loggedInMember = loggedInUserService.getLoggedInMember();
		log.debug("Checking view permission " + permission +  " for " + loggedInMember.getUsername());
		return permissionsService.hasPermission(loggedInMember, permission);
	}
}
