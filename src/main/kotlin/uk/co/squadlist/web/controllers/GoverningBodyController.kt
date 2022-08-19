package uk.co.squadlist.web.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.model.swagger.Member
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.GoverningBodyFactory
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.views.NavItemsBuilder
import uk.co.squadlist.web.views.ViewFactory
import javax.websocket.server.PathParam

@Controller
class GoverningBodyController @Autowired constructor(
    private val governingBodyFactory: GoverningBodyFactory,
    private val viewFactory: ViewFactory,
    private val navItemsBuilder: NavItemsBuilder,
    loggedInUserService: LoggedInUserService,
    instanceConfig: InstanceConfig,
    permissionsService: PermissionsService
) : WithSignedInUser(instanceConfig, loggedInUserService, permissionsService) {

    @GetMapping("/governing-body/{id}")
    fun member(@PathVariable id: String): ModelAndView {
        val renderGoverningBodyPage =
            { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
                val governingBody = governingBodyFactory.governingBodyFor(id)

                val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
                val navItems =
                    navItemsBuilder.navItemsFor(loggedInMember, null, swaggerApiClientForLoggedInUser, instance, squads)
                viewFactory.getViewFor("governingBody", instance).addObject("governingBody", governingBody)
                    .addObject("title", governingBody.name).addObject("navItems", navItems)
                    .addObject("ageGrades", governingBody.ageGrades).addObject("statuses", governingBody.statusPoints)
                    .addObject("boatSizes", governingBody.boatSizes)

            }

        return withSignedInMember(renderGoverningBodyPage)
    }

}