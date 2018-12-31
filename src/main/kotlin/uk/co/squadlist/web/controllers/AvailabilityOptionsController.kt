package uk.co.squadlist.web.controllers

import com.google.common.base.Strings
import org.apache.log4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.squadlist.web.annotations.RequiresPermission
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.exceptions.InvalidAvailabilityOptionException
import uk.co.squadlist.web.model.AvailabilityOption
import uk.co.squadlist.web.model.forms.AvailabilityOptionDetails
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.ViewFactory
import javax.validation.Valid

@Controller
class AvailabilityOptionsController(val api: InstanceSpecificApiClient, val viewFactory: ViewFactory, val urlBuilder: UrlBuilder) {

    private val log = Logger.getLogger(AvailabilityOptionsController::class.java)

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @GetMapping("/availability-option/{id}/edit")
    fun editPrompt(@PathVariable id: String): ModelAndView {
        val a = api.getAvailabilityOption(id)

        val availabilityOption = AvailabilityOptionDetails()
        availabilityOption.name = a.label
        availabilityOption.colour = a.colour

        return renderEditAvailabilityOptionForm(availabilityOption, a)
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @GetMapping("/availability-option/{id}/delete")
    fun deletePrompt(@PathVariable id: String): ModelAndView {
        val a = api.getAvailabilityOption(id)
        return renderDeleteForm(a)
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @PostMapping("/availability-option/{id}/delete")
    fun delete(@PathVariable id: String, @RequestParam(required = false) alternative: String): ModelAndView {
        val a = api.getAvailabilityOption(id)

        if (!Strings.isNullOrEmpty(alternative)) {
            val alternativeOption = api.getAvailabilityOption(alternative)
            log.info("Deleting availability option: $a replacing with: $alternativeOption")
            api.deleteAvailabilityOption(a, alternativeOption)

        } else {
            log.info("Deleting availability option: $a")
            api.deleteAvailabilityOption(a)
        }

        return redirectToAdmin()
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @GetMapping("/availability-option/new")
    fun availability(): ModelAndView {
        val availabilityOption = AvailabilityOptionDetails()
        availabilityOption.colour = "green"
        return renderNewAvailabilityOptionForm(availabilityOption)
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @PostMapping("/availability-option/new")
    fun newSquadSubmit(@Valid @ModelAttribute("availabilityOptionDetails") availabilityOptionDetails: AvailabilityOptionDetails, result: BindingResult): ModelAndView {
        if (result.hasErrors()) {
            return renderNewAvailabilityOptionForm(availabilityOptionDetails)
        }

        try {
            api.createAvailabilityOption(availabilityOptionDetails.name, availabilityOptionDetails.colour)
            return redirectToAdmin()

        } catch (e: InvalidAvailabilityOptionException) {
            result.rejectValue("name", null!!, e.message)
            return renderNewAvailabilityOptionForm(availabilityOptionDetails)
        }

    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @PostMapping("/availability-option/{id}/edit")
    fun editPost(@PathVariable id: String, @Valid @ModelAttribute("availabilityOptionDetails") availabilityOptionDetails: AvailabilityOptionDetails, result: BindingResult): ModelAndView {
        val a = api.getAvailabilityOption(id)
        if (result.hasErrors()) {
            return renderEditAvailabilityOptionForm(availabilityOptionDetails, a)
        }

        a.label = availabilityOptionDetails.name
        a.colour = availabilityOptionDetails.colour

        try {
            api.updateAvailabilityOption(a)

        } catch (e: InvalidAvailabilityOptionException) {
            result.rejectValue("name", null!!, e.message)
            return renderEditAvailabilityOptionForm(availabilityOptionDetails, a)
        }

        return redirectToAdmin()
    }

    private fun renderNewAvailabilityOptionForm(availabilityOptionDetails: AvailabilityOptionDetails): ModelAndView {
        return viewFactory.getViewForLoggedInUser("newAvailabilityOption").addObject("availabilityOptionDetails", availabilityOptionDetails)
    }

    private fun renderEditAvailabilityOptionForm(availabilityOptionDetails: AvailabilityOptionDetails, a: AvailabilityOption): ModelAndView {
        return viewFactory.getViewForLoggedInUser("editAvailabilityOption").addObject("availabilityOptionDetails", availabilityOptionDetails).addObject("availabilityOption", a)
    }

    private fun redirectToAdmin(): ModelAndView {
        return ModelAndView(RedirectView(urlBuilder.adminUrl()))
    }

    private fun renderDeleteForm(a: AvailabilityOption): ModelAndView {
        val alternatives = api.availabilityOptions
        alternatives.remove(a)

        return viewFactory.getViewForLoggedInUser("deleteAvailabilityOption").addObject("availabilityOption", a).addObject("alternatives", alternatives)
    }

}