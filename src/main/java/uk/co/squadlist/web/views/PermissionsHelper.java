package uk.co.squadlist.web.views;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;

@Component
public class PermissionsHelper {

	private final static Logger log = Logger.getLogger(PermissionsHelper.class);
	
	private final LoggedInUserService loggedInUserService;
	private final PermissionsService permissionsService;
	
	@Autowired
	public PermissionsHelper(LoggedInUserService loggedInUserService, PermissionsService permissionsService) {
		this.loggedInUserService = loggedInUserService;
		this.permissionsService = permissionsService;
	}
	
	public boolean hasPermission(String permissionName) throws UnknownMemberException {
		Permission.valueOf(permissionName);
		final String loggedInUser = loggedInUserService.getLoggedInUser();
		final Permission permission = Permission.valueOf(permissionName);
		log.info("Checking view permission " + permission +  " for " + loggedInUserService);
		return permissionsService.hasPermission(loggedInUser, permission);
	}
	
}
