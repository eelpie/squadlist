package uk.co.squadlist.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.squadlist.client.swagger.ApiClient;
import uk.co.squadlist.client.swagger.api.DefaultApi;

import java.io.IOException;

@Component
public class SquadlistApiFactory {

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

    public SquadlistApi createClient() throws IOException {
        String clientAccessToken = new SquadlistApi(apiUrl).requestClientAccessToken(clientId, clientSecret);
        return createForToken(clientAccessToken);
    }

    public DefaultApi createSwaggerClient() throws IOException {
        String clientAccessToken = new SquadlistApi(apiUrl).requestClientAccessToken(clientId, clientSecret);
        return createSwaggerApiClientForToken(clientAccessToken);
    }

    public DefaultApi createSwaggerApiClientForToken(String clientAccessToken) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(apiUrl);
        apiClient.setAccessToken(clientAccessToken);
        return new DefaultApi(apiClient);
    }

    public SquadlistApi createForToken(String token) {
        return new SquadlistApi(apiUrl, token);
    }

    public DefaultApi createUnauthenticatedSwaggerClient() {
        DefaultApi clientApi = new DefaultApi();
        clientApi.getApiClient().setBasePath(apiUrl);
        return clientApi;
    }

}
