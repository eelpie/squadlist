package uk.co.squadlist.web.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;

import com.google.common.base.Strings;

@Component
public class PreferredSquadService {

    private final static Logger log = LogManager.getLogger(PreferredSquadService.class);

    private static final String SELECTED_SQUAD = "selectedSquad";

    private final SquadlistApi squadlistApi;
    private final HttpServletRequest request;
    private final LoggedInUserService loggedInUserService;

    @Autowired
    public PreferredSquadService(HttpServletRequest request, LoggedInUserService loggedInUserService, SquadlistApiFactory squadlistApiFactory) throws IOException {
        this.request = request;
        this.loggedInUserService = loggedInUserService;
        this.squadlistApi = squadlistApiFactory.createClient();
    }

    public Squad resolveSquad(String squadId, InstanceSpecificApiClient instanceSpecificApiClient) throws UnknownSquadException, SignedInMemberRequiredException {
        if (!Strings.isNullOrEmpty(squadId)) {
            final Squad selectedSquad = squadlistApi.getSquad(squadId);
            setPreferredSquad(selectedSquad);
            return selectedSquad;
        }
        return resolvedPreferredSquad(loggedInUserService.getLoggedInMember(), instanceSpecificApiClient.getSquads());
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
