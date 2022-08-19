package uk.co.squadlist.web.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.squareup.okhttp.Credentials
import com.squareup.okhttp.FormEncodingBuilder
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.support.RequestContextUtils
import uk.co.squadlist.client.swagger.ApiException
import uk.co.squadlist.web.api.SquadlistApiFactory
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.ViewFactory
import javax.servlet.http.HttpServletRequest

@Controller
class LoginController @Autowired constructor(
    private val loggedInUserService: LoggedInUserService,
    private val urlBuilder: UrlBuilder,
    private val instanceConfig: InstanceConfig,
    private val squadlistApiFactory: SquadlistApiFactory,
    private val viewFactory: ViewFactory,
    @Value("\${client.id}") private val clientId: String,
    @Value("\${client.secret}") private val clientSecret: String,
    @Value("\${apiUrl}") private val apiUrl: String) {

    private val log = LogManager.getLogger(LoginController::class.java)

    private val swaggerApi = squadlistApiFactory.createSwaggerClient()

    @GetMapping("/login")
    fun login(request: HttpServletRequest?): ModelAndView {
        var error = false
        var username: String? = null
        val inputFlashMap = RequestContextUtils.getInputFlashMap(request)
        if (inputFlashMap != null) {
            val flashedError = inputFlashMap["error"] as Boolean?
            error = flashedError ?: false
            username = inputFlashMap["username"] as String?
        }
        return renderLoginScreen(error, username)
    }

    @PostMapping("/login")
    fun loginSubmit(@RequestParam(value = "username") username: String, @RequestParam(value = "password") password: String, redirectAttributes: RedirectAttributes): ModelAndView {
        // Use the OAuth password flow to swap our user's username and password for an access token
        log.info("Attempting to auth user: $username")
        val authenticatedUsersAccessToken = auth(username, password)
        log.info("Auth got access token for: $username")
        if (authenticatedUsersAccessToken != null) {
            try {
                // Call the API verify end point with the new access token to obtain the signed in user
                log.info("Verifying access token to find signed in user")
                val authenticatedMember =
                    squadlistApiFactory.createSwaggerApiClientForToken(authenticatedUsersAccessToken).verifyPost()
                if (authenticatedMember != null) {
                    log.info("Auth successful for user: $username")
                    loggedInUserService.setSignedIn(authenticatedUsersAccessToken)
                    return viewFactory.redirectionTo(urlBuilder.baseUrl)
                } else {
                    log.warn("Verified user was null; this should not happen for valid access tokens")
                }
            } catch (e: ApiException) {
                log.warn("ApiException during verify: " + e.code + " / " + e.responseBody)
            }
        }
        redirectAttributes.addFlashAttribute("error", true)
        redirectAttributes.addFlashAttribute("username", username)
        return viewFactory.redirectionTo(urlBuilder.loginUrl())
    }

    @GetMapping("/logout")
    fun logout(): ModelAndView {
        loggedInUserService.cleanSignedIn()
        return viewFactory.redirectionTo(urlBuilder.loginUrl())
    }

    private fun renderLoginScreen(errors: Boolean, username: String?): ModelAndView {
        val instance = swaggerApi.getInstance(instanceConfig.instance)
        return viewFactory.getViewFor("login", instance).addObject("title", instance.name)
            .addObject("username", username).addObject("errors", errors)
    }

    private fun auth(username: String, password: String): String? {
        // swagger-codegen does not appear to provide implementations of the auth flows so we need to handcraft these calls
        return try {
            val instance = instanceConfig.instance
            val formBody = FormEncodingBuilder()
                .add("grant_type", "password")
                .add("username", "$instance/$username")
                .add("password", password).build()
            val request = Request.Builder().url("$apiUrl/oauth/token")
                .addHeader("Authorization", Credentials.basic(clientId, clientSecret)).post(formBody).build()
            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            if (response.code() == 200) {
                val responseBody = response.body().string()
                log.info("Successful auth response")
                val jsonNode = ObjectMapper().readTree(responseBody)
                val accessToken = jsonNode["access_token"].asText()
                log.debug("Parsed access token: $accessToken")
                accessToken
            } else {
                val responseBody = response.body().string()
                log.warn("Response from auth call: " + response.code() + " / " + responseBody)
                throw RuntimeException("Invalid auth")
            }
        } catch (e: Exception) {
            log.error("Uncaught error", e)
            null
        }
    }

}