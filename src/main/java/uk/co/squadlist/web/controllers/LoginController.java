package uk.co.squadlist.web.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@Controller
public class LoginController {

    private final static Logger log = LogManager.getLogger(LoginController.class);

    private final InstanceConfig instanceConfig;
    private final String clientId;
    private final String clientSecret;
    private final String apiUrl;
    private final LoggedInUserService loggedInUserService;
    private final UrlBuilder urlBuilder;
    private final SquadlistApiFactory squadlistApiFactory;
    private final ViewFactory viewFactory;
    private final DefaultApi swaggerApi;

    @Autowired
    public LoginController(LoggedInUserService loggedInUserService, UrlBuilder urlBuilder,
                           InstanceConfig instanceConfig,
                           SquadlistApiFactory squadlistApiFactory,
                           ViewFactory viewFactory,
                           @Value("${client.id}") String clientId,
                           @Value("${client.secret}") String clientSecret,
                           @Value("${apiUrl}") String apiUrl
    ) throws IOException {
        this.loggedInUserService = loggedInUserService;
        this.urlBuilder = urlBuilder;
        this.instanceConfig = instanceConfig;
        this.swaggerApi = squadlistApiFactory.createSwaggerClient();
        this.squadlistApiFactory = squadlistApiFactory;
        this.viewFactory = viewFactory;

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.apiUrl = apiUrl;
    }

    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ModelAndView login(HttpServletRequest request) throws Exception {
        boolean error = false;
        String username = null;
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            Boolean flashedError = (Boolean) inputFlashMap.get("error");
            error = flashedError != null ? flashedError : false;
            username = (String) inputFlashMap.get("username");
        }
        return renderLoginScreen(error, username);
    }

    @RequestMapping(value = "/login", method = {RequestMethod.POST})
    public ModelAndView loginSubmit(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password,
            RedirectAttributes redirectAttributes) {

        // Use the OAuth password flow to swap our user's username and password for an access token
        log.info("Attempting to auth user: " + username);
        final String authenticatedUsersAccessToken = auth(username, password);
        log.info("Auth got access token for: " + username);

        if (authenticatedUsersAccessToken != null) {
            try {
                // Call the API verify end point with the new access token to obtain the signed in user
                log.info("Verifying access token to find signed in user");
                Member authenticatedMember = squadlistApiFactory.createSwaggerApiClientForToken(authenticatedUsersAccessToken).verifyPost();
                if (authenticatedMember != null) {
                    log.info("Auth successful for user: " + username);
                    loggedInUserService.setSignedIn(authenticatedUsersAccessToken);
                    return viewFactory.redirectionTo(urlBuilder.getBaseUrl());

                } else {
                    log.warn("Verified user was null; this should not happen for valid access tokens");
                }

            } catch (ApiException e) {
                log.warn("ApiException during verify: " + e.getCode() + " / " + e.getResponseBody());
            }
        }

        redirectAttributes.addFlashAttribute("error", true);
        redirectAttributes.addFlashAttribute("username", username);
        return viewFactory.redirectionTo(urlBuilder.loginUrl());
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET})
    public ModelAndView logout() {
        loggedInUserService.cleanSignedIn();
        return viewFactory.redirectionTo(urlBuilder.loginUrl());
    }

    private ModelAndView renderLoginScreen(boolean errors, String username) throws ApiException {
        final Instance instance = swaggerApi.getInstance(instanceConfig.getInstance());
        return viewFactory.getViewFor("login", instance).
                addObject("title", instance.getName()).
                addObject("username", username).
                addObject("errors", errors);
    }

    private String auth(String username, String password) {
        // swagger-codegen does not appear to provide implementations of the auth flows so we need to handcraft these calls
        try {
            String instance = instanceConfig.getInstance();

            RequestBody formBody = new FormEncodingBuilder()
                    .add("grant_type", "password")
                    .add("username", instance + "/" + username)
                    .add("password", password).build();

            Request request = new Request.Builder().
                    url(apiUrl + "/oauth/token").
                    addHeader("Authorization", Credentials.basic(clientId, clientSecret)).
                    post(formBody).
                    build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();

            if (response.code() == 200) {
                String responseBody = response.body().string();
                log.info("Successful auth response");
                JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
                String accessToken = jsonNode.get("access_token").asText();
                log.debug("Parsed access token: " + accessToken);
                return accessToken;

            } else {
                String responseBody = response.body().string();
                log.warn("Response from auth call: " + response.code() + " / " + responseBody);
                throw new RuntimeException("Invalid auth");
            }

        } catch (Exception e) {
            log.error("Uncaught error", e);
            return null;
        }
    }

}
