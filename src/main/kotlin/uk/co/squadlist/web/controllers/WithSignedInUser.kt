package uk.co.squadlist.web.controllers

import org.apache.logging.log4j.LogManager
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.model.swagger.Member
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.exceptions.PermissionDeniedException
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.services.PermissionsService

abstract class WithSignedInUser(private val instanceConfig: InstanceConfig, private val loggedInUserService: LoggedInUserService,
                                private val permissionsService: PermissionsService) {

    private val log = LogManager.getLogger(WithSignedInUser::class.java)

    // Given a func to render a page, resolve the logged in member and instance then execute that function
    protected fun withSignedInMember(page: (Instance, Member, DefaultApi) -> ModelAndView): ModelAndView {
        val swaggerApiClientForLoggedInUser = loggedInUserService.swaggerApiClientForLoggedInUser
        val instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.instance)
        val loggedInMember = loggedInUserService.loggedInMember

        log.info("Rendering page for " + instance.id + " / " + loggedInMember.username)
        return page(instance, loggedInMember, swaggerApiClientForLoggedInUser)
    }

    protected fun withSignedInMemberWhoCanViewAdminScreen(page: (Instance, Member, DefaultApi) -> ModelAndView): ModelAndView {
        val swaggerApiClientForLoggedInUser = loggedInUserService.swaggerApiClientForLoggedInUser
        val instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.instance)
        val loggedInMember = loggedInUserService.loggedInMember
        if (!permissionsService.hasPermission(loggedInMember, Permission.VIEW_ADMIN_SCREEN)) {
            throw PermissionDeniedException()
        }

        log.info("Rendering page for admin " + instance.id + " / " + loggedInMember.username)
        return page(instance, loggedInMember, swaggerApiClientForLoggedInUser)
    }


}