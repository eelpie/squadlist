package uk.co.squadlist.web.services.ical;

import com.google.common.collect.Lists;
import net.fortuna.ical4j.model.Calendar;
import org.junit.Test;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.OutingAvailability;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class OutingCalenderServiceTest {

    private final OutingCalendarService outingCalendarService = new OutingCalendarService();

    @Test
    public void canBuildIcalCalenderForOuting() throws Exception {
        List<OutingAvailability> outings = Lists.newArrayList();
        Instance instance = new Instance();
        instance.setName("Some club");

        Calendar calendar = outingCalendarService.buildCalendarFor(outings, instance);

        final String calenderOutput = calendar.toString();
        assertTrue(calenderOutput.contains("NAME:Some club outings"));
    }

}
