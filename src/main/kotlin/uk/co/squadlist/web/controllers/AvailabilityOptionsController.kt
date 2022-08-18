package uk.co.squadlist.web.controllers

import com.google.common.base.Strings
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.ApiException
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.AvailabilityOption
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.model.swagger.Member
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.model.forms.AvailabilityOptionDetails
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.NavItemsBuilder
import uk.co.squadlist.web.views.ViewFactory
import java.net.URISyntaxException
import javax.validation.Valid

@Controller
class AvailabilityOptionsController @Autowired constructor(
    private val viewFactory: ViewFactory,
    private val urlBuilder: UrlBuilder,
    private val navItemsBuilder: NavItemsBuilder,
    loggedInUserService: LoggedInUserService,
    permissionsService: PermissionsService,
    instanceConfig: InstanceConfig): WithSignedInUser(instanceConfig, loggedInUserService, permissionsService){

    private val log = LogManager.getLogger(AvailabilityOptionsController::class.java)
    @GetMapping("/availability-option/{id}/edit")
    @Throws(URISyntaxException::class, ApiException::class)
    fun editPrompt(@PathVariable id: String): ModelAndView {
        val renderEditAvailabilityOptionPage = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val availabilityOption =
                swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdGet(instance.id, id)
            val availabilityOptionDetails = AvailabilityOptionDetails()
            availabilityOptionDetails.name = availabilityOption.label
            availabilityOptionDetails.colour = availabilityOption.colour
            renderEditAvailabilityOptionForm(instance, availabilityOptionDetails, availabilityOption, swaggerApiClientForLoggedInUser, loggedInMember)
        }
        return withSignedInMemberWhoCanViewAdminScreen(renderEditAvailabilityOptionPage)
    }

    @GetMapping("/availability-option/{id}/delete")
    @Throws(URISyntaxException::class, ApiException::class)
    fun deletePrompt(@PathVariable id: String): ModelAndView {
        val renderDeleteAvailabilityOptionPrompt = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val availabilityOption = swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdGet(instance.id, id)
            renderDeleteForm(instance, loggedInMember, availabilityOption, swaggerApiClientForLoggedInUser)
        }
        return withSignedInMemberWhoCanViewAdminScreen(renderDeleteAvailabilityOptionPrompt)
    }

    @PostMapping("/availability-option/{id}/delete")
    @Throws(ApiException::class)
    fun delete(@PathVariable id: String, @RequestParam(required = false) alternative: String?): ModelAndView {
        val handleDeleteAvailabilityOption = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val availabilityOption = swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdGet(instance.id, id)
            if (!Strings.isNullOrEmpty(alternative)) {
                val alternativeOption =
                    swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdGet(
                        instance.id,
                        alternative
                    )
                log.info("Deleting availability option: $availabilityOption replacing with: $alternativeOption")
                swaggerApiClientForLoggedInUser.deleteAvailabilityOption(
                    instance.id,
                    availabilityOption.id,
                    alternativeOption.id
                )
            } else {
                log.info("Deleting availability option: $availabilityOption")
                swaggerApiClientForLoggedInUser.deleteAvailabilityOption(instance.id, availabilityOption.id, null)
            }
            redirectToAdmin()
        }
        return withSignedInMemberWhoCanViewAdminScreen(handleDeleteAvailabilityOption)
    }

    @GetMapping("/availability-option/new")
    @Throws(URISyntaxException::class, ApiException::class)
    fun availability(): ModelAndView {
        val renderNewAvailabilityOptionPrompt = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
             val availabilityOptionDetails = AvailabilityOptionDetails()
            availabilityOptionDetails.colour = "green"
            renderNewAvailabilityOptionForm(instance, loggedInMember, availabilityOptionDetails, swaggerApiClientForLoggedInUser)
        }
        return withSignedInMemberWhoCanViewAdminScreen(renderNewAvailabilityOptionPrompt)
    }

    @PostMapping("/availability-option/new")
    @Throws(URISyntaxException::class, ApiException::class)
    fun submitNewAvailabilityOption(@Valid @ModelAttribute("availabilityOptionDetails") availabilityOptionDetails: AvailabilityOptionDetails?, result: BindingResult): ModelAndView {
        val handleNewAvailability = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            if (result.hasErrors()) {
                renderNewAvailabilityOptionForm(instance, loggedInMember, availabilityOptionDetails, swaggerApiClientForLoggedInUser)
            } else try {
                val newAvailabilityOption = AvailabilityOption().label(availabilityOptionDetails!!.name).colour(
                    availabilityOptionDetails.colour
                )
                swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsPost(
                    newAvailabilityOption,
                    instance.id
                )
                redirectToAdmin()
            } catch (e: ApiException) {
                result.rejectValue("name", null, e.responseBody)
                renderNewAvailabilityOptionForm(instance, loggedInMember, availabilityOptionDetails, swaggerApiClientForLoggedInUser)
            }
        }
        return withSignedInMemberWhoCanViewAdminScreen(handleNewAvailability)
    }

    @PostMapping("/availability-option/{id}/edit")
    @Throws(URISyntaxException::class, ApiException::class)
    fun editPost(@PathVariable id: String,  @Valid @ModelAttribute("availabilityOptionDetails") availabilityOptionDetails: AvailabilityOptionDetails?, result: BindingResult): ModelAndView {
        val handleEditAvailabilityOption = { instance: Instance, loggedInMember: Member, swaggerApiClientForLoggedInUser: DefaultApi ->
            val availabilityOption = swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdGet(instance.id, id)
            if (result.hasErrors()) {
                renderEditAvailabilityOptionForm(instance, availabilityOptionDetails, availabilityOption, swaggerApiClientForLoggedInUser, loggedInMember)
            }
            availabilityOption.label = availabilityOptionDetails!!.name
            availabilityOption.colour = availabilityOptionDetails.colour
            try {
                swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsIdPost(
                    availabilityOption,
                    instance.id,
                    availabilityOption.id
                )
            } catch (e: ApiException) {
                result.rejectValue("name", null, e.responseBody)
                renderEditAvailabilityOptionForm(instance, availabilityOptionDetails, availabilityOption, swaggerApiClientForLoggedInUser, loggedInMember)
            }
            redirectToAdmin()
        }
        return withSignedInMemberWhoCanViewAdminScreen(handleEditAvailabilityOption)
    }

    private fun renderNewAvailabilityOptionForm(instance: Instance, loggedInMember: Member,
                                                availabilityOptionDetails: AvailabilityOptionDetails?,
                                                swaggerApiClientForLoggedInUser: DefaultApi): ModelAndView {
        val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
        val navItems = navItemsBuilder.navItemsFor(loggedInMember, "admin", swaggerApiClientForLoggedInUser, instance, squads)
        return viewFactory.getViewFor("newAvailabilityOption", instance)
            .addObject("title", "Add new availability option").addObject("navItems", navItems)
            .addObject("availabilityOptionDetails", availabilityOptionDetails)
    }

    private fun renderEditAvailabilityOptionForm(instance: Instance, availabilityOptionDetails: AvailabilityOptionDetails?,
                                                 availabilityOption: AvailabilityOption,
                                                 swaggerApiClientForLoggedInUser: DefaultApi, loggedInMember: Member): ModelAndView {
        val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
        val navItems = navItemsBuilder.navItemsFor(loggedInMember, "admin", swaggerApiClientForLoggedInUser, instance, squads)
        return viewFactory.getViewFor("editAvailabilityOption", instance)
            .addObject("title", "Edit availability options").addObject("navItems", navItems)
            .addObject("availabilityOptionDetails", availabilityOptionDetails)
            .addObject("availabilityOption", availabilityOption)
    }

    private fun redirectToAdmin(): ModelAndView {
        return viewFactory.redirectionTo(urlBuilder.adminUrl())
    }

    private fun renderDeleteForm(instance: Instance, loggedInMember: Member, selected: AvailabilityOption, swaggerApiClientForLoggedInUser: DefaultApi
    ): ModelAndView {
        val squads = swaggerApiClientForLoggedInUser.getSquads(instance.id)
        val alternatives = swaggerApiClientForLoggedInUser.instancesInstanceAvailabilityOptionsGet(instance.id)
        alternatives.remove(selected)
        val navItems =
            navItemsBuilder.navItemsFor(loggedInMember, "admin", swaggerApiClientForLoggedInUser, instance, squads)
        return viewFactory.getViewFor("deleteAvailabilityOption", instance)
            .addObject("title", "Delete availability option").addObject("navItems", navItems)
            .addObject("availabilityOption", selected).addObject("alternatives", alternatives)
    }

}