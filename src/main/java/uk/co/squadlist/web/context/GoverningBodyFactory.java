package uk.co.squadlist.web.context;

import org.springframework.stereotype.Component;
import uk.co.squadlist.model.swagger.Instance;
import uk.co.squadlist.web.localisation.BritishRowing;
import uk.co.squadlist.web.localisation.GoverningBody;
import uk.co.squadlist.web.localisation.RowingIreland;

@Component
public class GoverningBodyFactory {

    public GoverningBody getGoverningBody(Instance instance)  {
        return governingBodyFor(instance.getGoverningBody());
    }

    public GoverningBody governingBodyFor(String id) {
        if ("rowing-ireland".equals(id)) {
            return new RowingIreland();
        }
        return new BritishRowing();
    }

}
