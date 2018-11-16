package uk.co.squadlist.web.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.views.ViewFactory

@Controller
class BoatsController(val api: InstanceSpecificApiClient, val viewFactory: ViewFactory){

    @GetMapping("/boats/{id}")
    fun outing(@PathVariable id: String): ModelAndView {
        return viewFactory.getViewForLoggedInUser("boat").addObject("boat", api.getBoat(id))
    }

}
