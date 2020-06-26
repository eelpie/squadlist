package uk.co.squadlist.web.annotations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.PermissionDeniedException;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;

@Component
@Aspect
public class RequiresPermissionAspect {
	
	private static Logger log = LogManager.getLogger(RequiresPermissionAspect.class);
	
	private LoggedInUserService loggedInUserService;
	private PermissionsService permissionsService;

	@Autowired
	public RequiresPermissionAspect(LoggedInUserService loggedInUserService, PermissionsService permissionsService) {		
		this.loggedInUserService = loggedInUserService;
		this.permissionsService = permissionsService;
	}


	@Before("@annotation( requiresPermissionAnnotation ) ")
	public void processSystemRequest(final JoinPoint jp, RequiresPermission requiresPermissionAnnotation) throws Throwable {		
		try {						
			MethodSignature methodSignature = (MethodSignature) jp.getSignature();	
			Permission permission = requiresPermissionAnnotation.permission();
	
			final boolean hasPermission = permissionsService.hasPermission(loggedInUserService.getLoggedInMember(), permission);
			log.debug(methodSignature.getName() + " requires permission: "  + permission + "; logged in user is: " + loggedInUserService.getLoggedInMember().getUsername() + ": " + hasPermission);
			
			if (!hasPermission) {
				throw new PermissionDeniedException();
			}
			
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}
