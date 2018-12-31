package uk.co.squadlist.web.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.web.api.ApiUrlBuilder
import uk.co.squadlist.web.context.GoverningBodyFactory
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.PermissionsHelper
import uk.co.squadlist.web.views.ViewFactory

@Controller
public class GoverningBodyController(val governingBodyFactory: GoverningBodyFactory, val viewFactory: ViewFactory,
                                     val urlBuilder: UrlBuilder, val permissionsHelper: PermissionsHelper) {

    @GetMapping("/governing-body/british-rowing")
    fun governingBody(): ModelAndView {
        val governingBody = governingBodyFactory.governingBody

        return viewFactory.getViewForLoggedInUser("governingBody").addAllObjects(
                mapOf("governingBody" to governingBody,
                        "title" to governingBody.name,
                        "ageGrades" to governingBody.ageGrades,
                        "statuses" to governingBody.statusPoints,
                        "boatSizes" to governingBody.boatSizes,
                        "urlBuilder" to urlBuilder,
                        "permissionsHelper" to permissionsHelper)
        )
    }

}

