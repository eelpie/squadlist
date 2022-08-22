package uk.co.squadlist.web.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.model.swagger.Member
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.views.ViewFactory
import uk.co.squadlist.web.views.model.NavItem

@Controller
class BoatsController @Autowired constructor(
    private val viewFactory: ViewFactory,
    loggedInUserService: LoggedInUserService,
    permissionsService: PermissionsService,
    instanceConfig: InstanceConfig) : WithSignedInUser(instanceConfig, loggedInUserService, permissionsService) {

    @GetMapping("/boats/{id}")
    fun outing(@PathVariable id: String?): ModelAndView {
        val renderBoatPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
                val boat = swaggerApiClientForLoggedInUser.instancesInstanceBoatsIdGet(instance.id, id)
                viewFactory.getViewFor("boat", instance)
                    .addObject("title", "View boat")
                    .addObject("navItems", ArrayList<NavItem>())
                    .addObject("boat", boat)
            }

        return withSignedInMember(renderBoatPage)
    }

}