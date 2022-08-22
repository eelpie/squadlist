package uk.co.squadlist.web.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.model.swagger.Member
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.views.NavItemsBuilder
import uk.co.squadlist.web.views.ViewFactory

@Controller
class ChangesController @Autowired constructor(private val viewFactory: ViewFactory,
                                               private val navItemsBuilder: NavItemsBuilder,
                                               permissionsService: PermissionsService,
                                               loggedInUserService: LoggedInUserService,
                                               instanceConfig: InstanceConfig) : WithSignedInUser(instanceConfig, loggedInUserService, permissionsService) {
    @GetMapping("/changes")
    fun changes(): ModelAndView {
        val renderChangesPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
            val navItems = navItemsBuilder.navItemsFor(loggedInMember, null, swaggerApiClientForLoggedInUser, instance, squads)
            val changes = swaggerApiClientForLoggedInUser.changeLogGet()
            viewFactory.getViewFor("changes", instance).addObject("title", "What's changed").addObject("navItems", navItems).addObject("changes", changes)
        }
        return withSignedInMember(renderChangesPage)
    }

}