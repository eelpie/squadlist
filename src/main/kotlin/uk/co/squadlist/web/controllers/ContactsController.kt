package uk.co.squadlist.web.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.model.swagger.Member
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.services.PreferredSquadService
import uk.co.squadlist.web.views.NavItemsBuilder
import uk.co.squadlist.web.views.ViewFactory

@Controller
class ContactsController @Autowired constructor(private val preferredSquadService: PreferredSquadService,
                                                private val viewFactory: ViewFactory,
                                                private val contactsModelPopulator: ContactsModelPopulator,
                                                private val loggedInUserService: LoggedInUserService,
                                                private val navItemsBuilder: NavItemsBuilder,
                                                private val instanceConfig: InstanceConfig) {
    @RequestMapping("/contacts")
    fun contacts(): ModelAndView {
        val renderContactsPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
            val navItems = navItemsBuilder.navItemsFor(loggedInMember, "contacts", swaggerApiClientForLoggedInUser, instance, squads)
            viewFactory.getViewFor("contacts", instance).addObject("title", "Contacts").addObject("mavItems", navItems).addObject("squads", squads) // TODO leaves squad null on view
        }
        return withSignedInMember(renderContactsPage);
    }

    @RequestMapping("/contacts/{squadId}")
    fun squadContacts(@PathVariable squadId: String?): ModelAndView {
        val renderSquadContactsPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squadToShow = preferredSquadService.resolveSquad(squadId, swaggerApiClientForLoggedInUser, instance)
            val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
            val navItems = navItemsBuilder.navItemsFor(loggedInMember, "contacts", swaggerApiClientForLoggedInUser, instance, squads)
            val mv = viewFactory.getViewFor("contacts", instance).addObject("title", "Contacts").addObject("navItems", navItems).addObject("squads", squads)
            if (!squads.isEmpty()) {
                contactsModelPopulator.populateModel(squadToShow, mv, swaggerApiClientForLoggedInUser, loggedInMember)
            }
            mv
        }
        return withSignedInMember(renderSquadContactsPage);
    }

    // Given a func to render a page, resolve the logged in member and instance then execute that function
    private fun withSignedInMember(page: (Instance, Member, DefaultApi) -> ModelAndView): ModelAndView {
        val swaggerApiClientForLoggedInUser = loggedInUserService.swaggerApiClientForLoggedInUser
        val instance = swaggerApiClientForLoggedInUser.getInstance(instanceConfig.instance)
        val loggedInMember = loggedInUserService.loggedInMember
        return page(instance, loggedInMember, swaggerApiClientForLoggedInUser)
    }

}