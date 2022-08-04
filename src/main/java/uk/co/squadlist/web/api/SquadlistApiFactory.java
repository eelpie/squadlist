package uk.co.squadlist.web.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.squadlist.client.swagger.ApiClient;
import uk.co.squadlist.client.swagger.api.DefaultApi;

import java.io.IOException;

@Component
public class SquadlistApiFactory {

    private final static Logger log = LogManager.getLogger(SquadlistApiFactory.class);

    private final String apiUrl;
    private final String clientId;
    private final String clientSecret;

    @Autowired
    public SquadlistApiFactory(@Value("${apiUrl}") String apiUrl,
                               @Value("${client.id}") String clientId,
                               @Value("${client.secret}") String clientSecret) {
        this.apiUrl = apiUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public DefaultApi createSwaggerClient() throws IOException {
        String clientAccessToken = requestClientAccessToken(clientId, clientSecret);
        return createSwaggerApiClientForToken(clientAccessToken);
    }

    public DefaultApi createSwaggerApiClientForToken(String accessToken) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(apiUrl);
        apiClient.setAccessToken(accessToken);
        return new DefaultApi(apiClient);
    }

    public DefaultApi createUnauthenticatedSwaggerClient() {
        DefaultApi clientApi = new DefaultApi();
        clientApi.getApiClient().setBasePath(apiUrl);
        return clientApi;
    }

    private String requestClientAccessToken(String clientId, String clientSecret) throws IOException {
        RequestBody formBody = new FormEncodingBuilder().
                add("grant_type", "client_credentials").
                build();

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
    }

}
