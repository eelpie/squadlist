package uk.co.squadlist.web.controllers

import com.google.common.base.Strings
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.client.swagger.ApiException
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.web.api.SquadlistApiFactory
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.views.ViewFactory

@Controller
class ResetPasswordController @Autowired constructor(private val instanceConfig: InstanceConfig,
                                                     private val viewFactory: ViewFactory,
                                                     squadlistApiFactory: SquadlistApiFactory) {

    private val log = LogManager.getLogger(ResetPasswordController::class.java)

    private val squadlistApi: DefaultApi = squadlistApiFactory.createSwaggerClient()

    @GetMapping("/reset-password")
    fun resetPasswordPrompt(): ModelAndView {
        val instance = squadlistApi.getInstance(instanceConfig.instance)
        return viewFactory.getViewFor("resetPassword", instance).addObject("title", "Reset password")
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestParam(value = "username", required = false) username: String): ModelAndView {
        val instance = squadlistApi.getInstance(instanceConfig.instance)
        if (Strings.isNullOrEmpty(username)) {
            return viewFactory.redirectionTo("/reset-password")
        }
        log.info("Resetting password for: " + instance.id + " / " + username)
        return try {
            squadlistApi.instancesInstanceResetPasswordPost(instance.id, username.trim { it <= ' ' }) // TODO errors
            log.info("Reset password call successful for: $username")
            viewFactory.getViewFor("resetPasswordSent", instance).addObject("title", "Reset password")
        } catch (e: ApiException) {  // TODO more precise; use redirect pattern
            log.warn(e.code.toString() + " / " + e.responseBody)
            viewFactory.getViewFor("resetPassword", instance).addObject("title", "Reset password").addObject("errors", true)
        }
    }

    @GetMapping("/reset-password/confirm")
    fun confirmPasswordReset(@RequestParam token: String?): ModelAndView {
        val instance = squadlistApi.getInstance(instanceConfig.instance)
        return try {
            val newPassword = squadlistApi.instancesInstanceResetPasswordConfirmPost(instance.id, token)
            viewFactory.getViewFor("resetPasswordConfirm", instance).addObject("newPassword", newPassword)
        } catch (e: Exception) {
            log.warn("Reset password failed", e)
            ModelAndView("resetPasswordInvalidToken")
        }
    }

}