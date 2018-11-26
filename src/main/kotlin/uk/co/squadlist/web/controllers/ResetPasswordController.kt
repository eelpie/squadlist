package uk.co.squadlist.web.controllers

import com.google.common.base.Strings
import org.apache.log4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.exceptions.UnknownMemberException
import uk.co.squadlist.web.urls.UrlBuilder

@Controller
class ResetPasswordController(val api: InstanceSpecificApiClient, val urlBuilder: UrlBuilder) {

    private val log = Logger.getLogger(ResetPasswordController::class.java)

    @GetMapping(value = "/reset-password")
    fun resetPasswordPrompt(): ModelAndView {
        return resetPasswordScreen().addObject("errors", false)
    }

    @PostMapping(value = "/reset-password")
    fun resetPassword(@RequestParam(value = "username", required = false) username: String): ModelAndView {
        if (Strings.isNullOrEmpty(username)) {
            return resetPasswordScreen().addObject("errors", true)
        }

        log.info("Resetting password for: $username")
        try {
            api.resetPassword(username)    // TODO errors
            log.info("Reset password call successful for: $username")
            val mv = ModelAndView("resetPasswordSent")
            mv.addObject("title", "Reset password")
            mv.addObject("urlBuilder", urlBuilder)
            return mv

        } catch (e: UnknownMemberException) {
            return resetPasswordScreen().addObject("errors", true)
        }
    }

    @GetMapping(value = "/reset-password/confirm")
    fun confirmPasswordReset(@RequestParam token: String): ModelAndView {
        try {
            val newPassword = api.confirmResetPassword(token)

            val mv = ModelAndView("resetPasswordConfirm")
            mv.addObject("newPassword", newPassword)
            mv.addObject("urlBuilder", urlBuilder)
            return mv

        } catch (e: Exception) {
            return ModelAndView("resetPasswordInvalidToken").
                    addObject("urlBuilder", urlBuilder)
        }

    }

    private fun resetPasswordScreen(): ModelAndView {
        val mv = ModelAndView("resetPassword")
        mv.addObject("title", "Reset password")
        mv.addObject("urlBuilder", urlBuilder)
        return mv
    }

}