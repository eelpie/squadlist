package uk.co.squadlist.web.controllers

import org.apache.log4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.api.SquadlistApiFactory
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.model.forms.ChangePassword
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.PermissionsHelper
import uk.co.squadlist.web.views.ViewFactory
import javax.validation.Valid

@Controller
class ChangePasswordController(val api: InstanceSpecificApiClient, val urlBuilder: UrlBuilder, val viewFactory: ViewFactory,
                               val loggedInUserService: LoggedInUserService, val squadlistApiFactory: SquadlistApiFactory, val permissionsHelper: PermissionsHelper) {

    private val log = Logger.getLogger(ChangePasswordController::class.java)

    @GetMapping("/change-password")
    fun changePassword(): ModelAndView {
        return renderChangePasswordForm(ChangePassword())
    }

    @PostMapping("/change-password")
    fun changePasswordSubmit(@Valid @ModelAttribute("changePassword") changePassword: ChangePassword, result: BindingResult): ModelAndView {
        if (result.hasErrors()) {
            return renderChangePasswordForm(changePassword)
        }

        val member = loggedInUserService.getLoggedInMember()
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.getLoggedInMembersToken())

        log.info("Requesting change password for member: " + member.getId())
        if (loggedInUserApi.changePassword(member.getId(), changePassword.currentPassword, changePassword.newPassword)) {
            return ModelAndView(RedirectView(urlBuilder.memberUrl(member)))
        } else {
            result.addError(ObjectError("changePassword", "Change password failed"))
            return renderChangePasswordForm(changePassword)
        }
    }


    private fun renderChangePasswordForm(changePassword: ChangePassword): ModelAndView {
        val mv = viewFactory.getViewForLoggedInUser("changePassword")
        mv.addObject("member", loggedInUserService.getLoggedInMember())
        mv.addObject("changePassword", changePassword)
        mv.addObject("title", "Change password")
        mv.addObject("urlBuilder", urlBuilder)
        mv.addObject("permissionsHelper", permissionsHelper)
        return mv
    }

}