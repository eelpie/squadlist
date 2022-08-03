package uk.co.squadlist.web.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.web.api.SquadlistApiFactory;

import java.util.List;

@Component
public class PasswordGenerator {

    private final SquadlistApiFactory squadlistApiFactory;

    @Autowired
    public PasswordGenerator(SquadlistApiFactory squadlistApiFactory) {
        this.squadlistApiFactory = squadlistApiFactory;
    }

    public String generateRandomPassword() {
        try {
            List<String> suggestions = squadlistApiFactory.createUnauthenticatedSwaggerClient().passwordSuggestionsGet();
            return suggestions.get(0);  // This is ok because the list is randomised.
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

}
