package uk.co.squadlist.web.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.localisation.BritishRowing;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.localisation.RowingIreland;

@Component
public class GoverningBodyFactory {

    private final InstanceSpecificApiClient api;

    @Autowired
    public GoverningBodyFactory(InstanceSpecificApiClient api) {
        this.api = api;
    }

    public GoverningBody getGoverningBody()  {
        try {
            return governingBodyFor(api.getInstance().getGoverningBody());
        } catch (UnknownInstanceException e) {
            return new BritishRowing();
        }
    }

    private GoverningBody governingBodyFor(String id) {
        if ("rowing-ireland".equals(id)) {
            return new RowingIreland();
        }
        return new BritishRowing();
    }

}
