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
import uk.co.squadlist.model.swagger.Outing;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.PermissionDeniedException;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;

@Component
@Aspect
public class RequiresOutingPermissionAspect {

    private static Logger log = LogManager.getLogger(RequiresOutingPermissionAspect.class);

    private LoggedInUserService loggedInUserService;
    private PermissionsService permissionsService;

    @Autowired
    public RequiresOutingPermissionAspect(LoggedInUserService loggedInUserService, PermissionsService permissionsService) {
        this.loggedInUserService = loggedInUserService;
        this.permissionsService = permissionsService;
    }

    @Before("@annotation( requiresOutingPermissionAnnotation ) ")
    public void processSystemRequest(final JoinPoint jp, RequiresOutingPermission requiresOutingPermissionAnnotation) throws Throwable {
        try {
            MethodSignature methodSignature = (MethodSignature) jp.getSignature();
            Permission permission = requiresOutingPermissionAnnotation.permission();
            String outingId = (String) jp.getArgs()[0];

            Member loggedInMember = loggedInUserService.getLoggedInMember();
            Outing outing = loggedInUserService.getSwaggerApiClientForLoggedInUser().outingsIdGet(outingId);

            final boolean hasPermission = permissionsService.hasOutingPermission(loggedInMember, permission, outing);
            log.debug(methodSignature.getName() + " requires permission: " + permission + " for outing " + outingId + "; logged in user is: " + loggedInMember.getUsername() + ": " + hasPermission);

            if (!hasPermission) {
                throw new PermissionDeniedException();
            }

        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

}
