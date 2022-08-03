package uk.co.squadlist.web.annotations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.model.swagger.Squad;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.PermissionDeniedException;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;

@Component
@Aspect
public class RequiresSquadPermissionAspect {
	
	private static Logger log = LogManager.getLogger(RequiresSquadPermissionAspect.class);
	
	private LoggedInUserService loggedInUserService;
	private PermissionsService permissionsService;

	@Autowired
	public RequiresSquadPermissionAspect(LoggedInUserService loggedInUserService, PermissionsService permissionsService) {		
		this.loggedInUserService = loggedInUserService;
		this.permissionsService = permissionsService;
	}
	
	@Before("@annotation( requiresSquadPermissionAnnotation ) ")
	public void processSystemRequest(final JoinPoint jp, RequiresSquadPermission requiresSquadPermissionAnnotation) throws Throwable {		
		try {						
			MethodSignature methodSignature = (MethodSignature) jp.getSignature();	
			Permission permission = requiresSquadPermissionAnnotation.permission();
			Squad squad = (Squad) jp.getArgs()[0];

			Member loggedInMember = loggedInUserService.getLoggedInMember();
			final boolean hasPermission = permissionsService.hasSquadPermission(loggedInMember, permission, squad);
			log.debug(methodSignature.getName() + " requires permission: "  + permission + " for squad " + squad + "; logged in user is: " + loggedInMember.getUsername() + ": " + hasPermission);
			
			if (!hasPermission) {
				throw new PermissionDeniedException();
			}
			
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}
