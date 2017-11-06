package uk.co.squadlist.web.annotations;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.PermissionDeniedException;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;

@Component
@Aspect
public class RequiresSignedInMemberAspect {

    private static Logger log = Logger.getLogger(RequiresSignedInMemberAspect.class);

    private LoggedInUserService loggedInUserService;

    @Autowired
    public RequiresSignedInMemberAspect(LoggedInUserService loggedInUserService) {
        this.loggedInUserService = loggedInUserService;
    }

    @Before("@annotation( requiresSignedInMemberAnnotation ) ")
    public void processSystemRequest(final JoinPoint jp, RequiresSignedInMember requiresSignedInMemberAnnotation) throws Throwable {
        try {
            final boolean isMemberSignedIn = loggedInUserService.getLoggedInMember() != null;
            if (!isMemberSignedIn) {
                throw new SignedInMemberRequiredException();
            }
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

}
