package uk.co.squadlist.web.controllers

import com.google.common.base.Splitter
import com.google.common.collect.Lists
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.ApiException
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.model.swagger.Member
import uk.co.squadlist.model.swagger.Squad
import uk.co.squadlist.model.swagger.SquadSubmission
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.model.forms.SquadDetails
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.NavItemsBuilder
import uk.co.squadlist.web.views.ViewFactory
import java.io.IOException
import java.net.URISyntaxException
import javax.validation.Valid

@Controller
class SquadsController @Autowired constructor(private val urlBuilder: UrlBuilder,
                                              private val viewFactory: ViewFactory,
                                              private val loggedInUserService: LoggedInUserService,
                                              private val navItemsBuilder: NavItemsBuilder,
                                              private val instanceConfig: InstanceConfig,
                                              private val displayMemberFactory: DisplayMemberFactory,
                                              permissionsService: PermissionsService,
                                              ): WithSignedInUser(instanceConfig, loggedInUserService, permissionsService) {

    private val log = LogManager.getLogger(SquadsController::class.java)

    private val COMMA_SPLITTER = Splitter.on(",")
    
    @GetMapping("/squad/new")
    fun newSquad(@ModelAttribute("squadDetails") squadDetails: SquadDetails?): ModelAndView {
        val renderAddSquadPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            renderNewSquadForm(SquadDetails(), loggedInMember, instance, swaggerApiClientForLoggedInUser)
        }
        return withSignedInMemberWhoCanViewAdminScreen(renderAddSquadPage)
    }

    @PostMapping("/squad/new")
    @Throws(URISyntaxException::class, ApiException::class, IOException::class)
    fun newSquadSubmit(@Valid @ModelAttribute("squadDetails") squadDetails: SquadDetails?, result: BindingResult): ModelAndView {
        val handleAddSquad = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            if (result.hasErrors()) {
                renderNewSquadForm(squadDetails, loggedInMember, instance, swaggerApiClientForLoggedInUser)
            } else try {
                val submission = SquadSubmission().instance(instance).name(squadDetails!!.name)
                swaggerApiClientForLoggedInUser.createSquad(submission)
                viewFactory.redirectionTo(urlBuilder.adminUrl())
            } catch (e: ApiException) {  // TODO more precise catch
                log.info("Invalid squad")
                result.rejectValue("name", null, "squad name is already in use")
                renderNewSquadForm(squadDetails, loggedInMember, instance, swaggerApiClientForLoggedInUser)
            }
        }
        return withSignedInMemberWhoCanViewAdminScreen(handleAddSquad)
    }

    @GetMapping("/squad/{id}/delete")
    @Throws(URISyntaxException::class, ApiException::class)
    fun deletePrompt(@PathVariable id: String): ModelAndView {
        val renderDeleteSquadPrompt = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
            val navItems = navItemsBuilder.navItemsFor(loggedInMember, "admin", swaggerApiClientForLoggedInUser, instance, squads)
            val squad = swaggerApiClientForLoggedInUser.getSquad(id)
            viewFactory.getViewFor("deleteSquadPrompt", instance)
                    .addObject("title", "Delete squad")
                    .addObject("navItems", navItems)
                    .addObject("squad", squad)
        }
        return withSignedInMemberWhoCanViewAdminScreen(renderDeleteSquadPrompt)
    }

    @PostMapping("/squad/{id}/delete")
    @Throws(ApiException::class)
    fun delete(@PathVariable id: String): ModelAndView {
        val handleDeleteSquad = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squad = swaggerApiClientForLoggedInUser.getSquad(id)
            swaggerApiClientForLoggedInUser.deleteSquad(squad.id)
            viewFactory.redirectionTo(urlBuilder.adminUrl())
        }
        return withSignedInMemberWhoCanViewAdminScreen(handleDeleteSquad)
    }

    @GetMapping("/squad/{id}/edit")
    @Throws(URISyntaxException::class, ApiException::class)
    fun editSquad(@PathVariable id: String): ModelAndView {
        val renderDeleteSquadPrompt = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val squad = swaggerApiClientForLoggedInUser.getSquad(id)
            val squadDetails = SquadDetails()
            squadDetails.name = squad.name
            renderEditSquadForm(loggedInMember, squad, squadDetails, instance, swaggerApiClientForLoggedInUser)
        }
        return withSignedInMemberWhoCanViewAdminScreen(renderDeleteSquadPrompt)
    }

    @PostMapping("/squad/{id}/edit")
    @Throws(URISyntaxException::class, ApiException::class)
    fun editSquadSubmit(@PathVariable id: String, @Valid @ModelAttribute("squadDetails") squadDetails: SquadDetails?, result: BindingResult): ModelAndView {
        val handleEditSquad = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->

            val squad = swaggerApiClientForLoggedInUser.getSquad(id)
            if (result.hasErrors()) {
                renderEditSquadForm(loggedInMember, squad, squadDetails, instance, swaggerApiClientForLoggedInUser)
            }
            squad.name = squadDetails!!.name
            log.info("Updating squad: $squad")
            try {
                swaggerApiClientForLoggedInUser.updateSquad(squad, squad.id)
            } catch (e: ApiException) {  // TODO more precise exception
                log.warn("Invalid squad")
                renderEditSquadForm(loggedInMember, squad, squadDetails, instance, swaggerApiClientForLoggedInUser)
            }
            val updatedSquadMembers: List<String> = Lists.newArrayList(COMMA_SPLITTER.split(squadDetails.members).iterator())
            log.info("Setting squad members to " + updatedSquadMembers.size + " members: " + updatedSquadMembers)
            swaggerApiClientForLoggedInUser.setSquadMembers(updatedSquadMembers, squad.id)
            viewFactory.redirectionTo(urlBuilder.adminUrl())
        }
        return withSignedInMemberWhoCanViewAdminScreen(handleEditSquad)
    }

    @Throws(URISyntaxException::class, ApiException::class)
    private fun renderNewSquadForm(squadDetails: SquadDetails?, loggedInUser: Member, instance: Instance, swaggerApiClientForLoggedInUser: DefaultApi): ModelAndView {
        val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
        val navItems = navItemsBuilder.navItemsFor(loggedInUser, "admin", swaggerApiClientForLoggedInUser, instance, squads)
        return viewFactory.getViewFor("newSquad", instance).addObject("title", "Add new squad").addObject("navItems", navItems).addObject("squadDetails", squadDetails)
    }

    @Throws(URISyntaxException::class, ApiException::class)
    private fun renderEditSquadForm(loggedInUser: Member, squad: Squad, squadDetails: SquadDetails?, instance: Instance, swaggerApiClientForLoggedInUser: DefaultApi): ModelAndView {
        val squadMembers = swaggerApiClientForLoggedInUser.getSquadMembers(squad.id)
        val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
        val availableMembers = swaggerApiClientForLoggedInUser.instancesInstanceMembersGet(instance.id)
        availableMembers.removeAll(squadMembers)
        val navItems = navItemsBuilder.navItemsFor(loggedInUser, "admin", swaggerApiClientForLoggedInUser, instance, squads)
        return viewFactory.getViewFor("editSquad", instance)
                .addObject("title", "Editing a squad")
                .addObject("navItems", navItems)
                .addObject("squad", squad)
                .addObject("squadDetails", squadDetails)
                .addObject("squadMembers", displayMemberFactory.toDisplayMembers(squadMembers, loggedInUser))
                .addObject("availableMembers", displayMemberFactory.toDisplayMembers(availableMembers, loggedInUser))
    }

}