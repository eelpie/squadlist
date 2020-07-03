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
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;

@Component
@Aspect
public class RequiresMemberPermissionAspect {

	private final static Logger log = LogManager.getLogger(RequiresMemberPermissionAspect.class);

	private LoggedInUserService loggedInUserService;
	private PermissionsService permissionsService;

	@Autowired
	public RequiresMemberPermissionAspect(LoggedInUserService loggedInUserService, PermissionsService permissionsService) {
		this.loggedInUserService = loggedInUserService;
		this.permissionsService = permissionsService;
	}

	@Before("@annotation( requiresMemberPermissionAnnotation ) ")
	public void processSystemRequest(final JoinPoint jp, RequiresMemberPermission requiresMemberPermissionAnnotation) throws Throwable {
		try {
			MethodSignature methodSignature = (MethodSignature) jp.getSignature();
			Permission permission = requiresMemberPermissionAnnotation.permission();
			String memberId = (String) jp.getArgs()[0];

			final Member loggedInMember = loggedInUserService.getLoggedInMember();
			final Member member = loggedInUserService.getApiClientForLoggedInUser().getMember(memberId);

			final boolean hasPermission = permissionsService.hasMemberPermission(loggedInMember, permission, member);
			log.debug(methodSignature.getName() + " requires permission: "  + permission + " for member " + memberId + "; logged in user is: " + loggedInMember.getUsername() + ": " + hasPermission);

			if (!hasPermission) {
				throw new PermissionDeniedException();
			}

		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}
