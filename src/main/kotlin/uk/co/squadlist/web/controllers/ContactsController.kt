package uk.co.squadlist.web.controllers

import org.apache.log4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.services.PreferedSquadService
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.PermissionsHelper
import uk.co.squadlist.web.views.ViewFactory

@Controller
class ContactsController(val api: InstanceSpecificApiClient, val preferedSquadService: PreferedSquadService,
                         val viewFactory: ViewFactory, val contactsModelPopulator: ContactsModelPopulator,
                         val urlBuilder: UrlBuilder, val permissionsHelper: PermissionsHelper) {

    private val log = Logger.getLogger(ContactsController::class.java)

    @GetMapping("/contacts")
    fun contacts(): ModelAndView {
        val mv = viewFactory.getViewForLoggedInUser("contacts")
        val allSquads = api.squads
        mv.addObject("squads", allSquads)    // TODO leaves squad null on view
        mv.addObject("urlBuilder", urlBuilder)
        mv.addObject("permissionsHelper", permissionsHelper)
        return mv
    }

    @GetMapping("/contacts/{squadId}")
    fun squadContacts(@PathVariable squadId: String): ModelAndView {
        val squadToShow = preferedSquadService.resolveSquad(squadId)

        val mv = viewFactory.getViewForLoggedInUser("contacts")
        val allSquads = api.squads
        mv.addObject("squads", allSquads)
        if (!allSquads.isEmpty()) {
            log.info("Squad to show: $squadToShow")
            contactsModelPopulator.populateModel(squadToShow, mv)
        }

        mv.addObject("urlBuilder", urlBuilder)
        mv.addObject("permissionsHelper", permissionsHelper)
        return mv
    }

}
