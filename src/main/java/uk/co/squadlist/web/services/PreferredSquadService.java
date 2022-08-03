package uk.co.squadlist.web.services;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.model.swagger.Squad;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Component
public class PreferredSquadService {

    private final static Logger log = LogManager.getLogger(PreferredSquadService.class);

    private static final String SELECTED_SQUAD = "selectedSquad";

    private final HttpServletRequest request;

    @Autowired
    public PreferredSquadService(HttpServletRequest request) {
        this.request = request;
    }

    public Squad resolveSquad(String squadId, DefaultApi swaggerApiClientForLoggedInUser, Instance instance) throws ApiException {
        if (!Strings.isNullOrEmpty(squadId)) {
            final Squad selectedSquad = swaggerApiClientForLoggedInUser.getSquad(squadId);    // TODO can probably just make one list squads call
            setPreferredSquad(selectedSquad);
            return selectedSquad;
        }
        return resolvedPreferredSquad(swaggerApiClientForLoggedInUser.getSquads(instance.getId()));
    }

    public Squad resolvedPreferredSquad(List<Squad> squads) {
        final String selectedSquadId = (String) request.getSession().getAttribute(SELECTED_SQUAD);
        if (selectedSquadId != null) {
            Optional<Squad> selectedSquad = squads.stream().filter(s -> s.getId().equals(selectedSquadId)).findFirst();
            if (selectedSquad.isPresent()) {
                return selectedSquad.get();
            }
            clearPreferredSquad();
        }

        Optional<Squad> firstInstanceSquad = squads.stream().findFirst();
        return firstInstanceSquad.orElse(null);
    }

    public void setPreferredSquad(Squad selectedSquad) {
        log.debug("Setting selected squad to: " + selectedSquad.getId());
        request.getSession().setAttribute(SELECTED_SQUAD, selectedSquad.getId());

    }

    private void clearPreferredSquad() {
        request.getSession().removeAttribute(SELECTED_SQUAD);
    }

}
