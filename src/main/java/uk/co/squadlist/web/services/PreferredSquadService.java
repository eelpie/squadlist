package uk.co.squadlist.web.services;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class PreferredSquadService {

    private final static Logger log = LogManager.getLogger(PreferredSquadService.class);

    private static final String SELECTED_SQUAD = "selectedSquad";

    private final HttpServletRequest request;

    @Autowired
    public PreferredSquadService(HttpServletRequest request) throws IOException {
        this.request = request;
    }

    public Squad resolveSquad(String squadId, InstanceSpecificApiClient instanceSpecificApiClient, Member loggedInMember) throws UnknownSquadException {
        if (!Strings.isNullOrEmpty(squadId)) {
            final Squad selectedSquad = instanceSpecificApiClient.getSquad(squadId);
            setPreferredSquad(selectedSquad);
            return selectedSquad;
        }
        return resolvedPreferredSquad(loggedInMember, instanceSpecificApiClient.getSquads());
    }

    public Squad resolvedPreferredSquad(Member loggedInMember, List<Squad> squads) {
        final String selectedSquadId = (String) request.getSession().getAttribute(SELECTED_SQUAD);
        if (selectedSquadId != null) {
            Optional<Squad> selectedSquad = squads.stream().filter(s -> s.getId().equals(selectedSquadId)).findFirst();
            if (selectedSquad.isPresent()) {
                return selectedSquad.get();
            }
            clearPreferredSquad();
        }

        if (!loggedInMember.getSquads().isEmpty()) {
            return loggedInMember.getSquads().iterator().next();
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
