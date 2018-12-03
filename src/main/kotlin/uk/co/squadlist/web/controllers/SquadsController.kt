package uk.co.squadlist.web.controllers

import com.google.common.base.Splitter
import com.google.common.collect.Sets
import org.apache.log4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.squadlist.web.annotations.RequiresPermission
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.api.SquadlistApi
import uk.co.squadlist.web.api.SquadlistApiFactory
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.exceptions.InvalidSquadException
import uk.co.squadlist.web.model.Squad
import uk.co.squadlist.web.model.forms.SquadDetails
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.ViewFactory
import javax.validation.Valid

@Controller
class SquadsController(
        val instanceSpecificApiClient: InstanceSpecificApiClient,
        val urlBuilder: UrlBuilder,
        val viewFactory: ViewFactory,
        val instanceConfig: InstanceConfig,
        val squadlistApiFactory: SquadlistApiFactory) {

    private val log = Logger.getLogger(SquadsController::class.java)

    private val COMMA_SPLITTER = Splitter.on(",")

    private val squadlistApi: SquadlistApi = squadlistApiFactory.createClient()

    @GetMapping("/squad/new")
    fun newSquad(@ModelAttribute("squadDetails") squadDetails: SquadDetails): ModelAndView {
        return renderNewSquadForm(SquadDetails())
    }

    @PostMapping("/squad/new")
    fun newSquadSubmit(@Valid @ModelAttribute("squadDetails") squadDetails: SquadDetails, result: BindingResult): ModelAndView {
        if (result.hasErrors()) {
            return renderNewSquadForm(squadDetails)
        }

        try {
            val instance = squadlistApi.getInstance(instanceConfig.instance)
            squadlistApi.createSquad(instance, squadDetails.name)

            return ModelAndView(RedirectView(urlBuilder.adminUrl()))

        } catch (e: InvalidSquadException) {
            log.info("Invalid squad")
            result.rejectValue("name", null, "squad name is already in use")
            return renderNewSquadForm(squadDetails)
        }

    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @GetMapping("/squad/{id}/delete")
    fun deletePrompt(@PathVariable id: String): ModelAndView {
        val squad = squadlistApi.getSquad(id)
        return viewFactory.getViewForLoggedInUser("deleteSquadPrompt").addObject("squad", squad).addObject("title", "Delete squad")
    }

    @RequiresPermission(permission = Permission.VIEW_ADMIN_SCREEN)
    @PostMapping("/squad/{id}/delete")
    fun delete(@PathVariable id: String): ModelAndView {
        val squad = squadlistApi.getSquad(id)
        squadlistApi.deleteSquad(squad)
        return ModelAndView(RedirectView(urlBuilder.adminUrl()))
    }

    @GetMapping("/squad/{id}/edit")
    fun editSquad(@PathVariable id: String): ModelAndView {
        val squad = squadlistApi.getSquad(id)

        val squadDetails = SquadDetails()
        squadDetails.name = squad.name

        return renderEditSquadForm(squad, squadDetails)
    }

    @PostMapping("/squad/{id}/edit")
    fun editSquadSubmit(@PathVariable id: String, @Valid @ModelAttribute("squadDetails") squadDetails: SquadDetails, result: BindingResult): ModelAndView {
        val squad = squadlistApi.getSquad(id)
        if (result.hasErrors()) {
            return renderEditSquadForm(squad, squadDetails)
        }

        squad.name = squadDetails.name
        log.info("Updating squad: $squad")
        squadlistApi.updateSquad(squad)

        val updatedSquadMembers = Sets.newHashSet(COMMA_SPLITTER.split(squadDetails.members).iterator())
        log.info("Setting squad members to " + updatedSquadMembers.size + " members: " + updatedSquadMembers)
        squadlistApi.setSquadMembers(squad.id, updatedSquadMembers)

        return ModelAndView(RedirectView(urlBuilder.adminUrl()))
    }

    private fun renderNewSquadForm(squadDetails: SquadDetails): ModelAndView {
        return viewFactory.getViewForLoggedInUser("newSquad").addObject("squadDetails", squadDetails)
    }

    private fun renderEditSquadForm(squad: Squad, squadDetails: SquadDetails): ModelAndView {
        val mv = viewFactory.getViewForLoggedInUser("editSquad")
        mv.addObject("squad", squad)
        mv.addObject("squadDetails", squadDetails)

        val squadMembers = squadlistApi.getSquadMembers(squad.id)
        mv.addObject("squadMembers", squadMembers)

        val availableMembers = instanceSpecificApiClient.members
        availableMembers.removeAll(squadMembers)
        mv.addObject("availableMembers", availableMembers)
        return mv
    }

}
