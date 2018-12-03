package uk.co.squadlist.web.controllers

import org.apache.log4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.urls.UrlBuilder

@Controller
class LoginController(val api: InstanceSpecificApiClient, val loggedInUserService: LoggedInUserService, val urlBuilder: UrlBuilder) {

    private val log = Logger.getLogger(LoginController::class.java)

    @RequestMapping(value = "/login", method = arrayOf(RequestMethod.GET, RequestMethod.HEAD))     // TODO SEO this onto the root url
    fun login() = renderLoginScreen()

    @PostMapping("/login")
    fun loginSubmit(
            @RequestParam(value = "username", required = true) username: String,
            @RequestParam(value = "password", required = true) password: String): ModelAndView {

        log.info("Attempting to auth user: $username")
        api.auth(username, password)?.let { token ->
            api.verify(token)?.let { user ->
                log.info("Auth successful for user: " + user.username)
                loggedInUserService.setSignedIn(token)
                return ModelAndView(RedirectView(urlBuilder.getBaseUrl()))
            }
        }

        return renderLoginScreen(true, username)
    }

    @GetMapping("/logout")
    fun logout(): ModelAndView {
        loggedInUserService.cleanSignedIn()
        return ModelAndView(RedirectView(urlBuilder.loginUrl()))
    }

    private fun renderLoginScreen(errors: Boolean = false, username: String? = null): ModelAndView {
        val instance = api.instance
        return ModelAndView("login").addAllObjects(mapOf(
                "title" to instance.name,
                "username" to username,
                "errors" to errors,
                "urlBuilder" to urlBuilder
        ))
    }

}
