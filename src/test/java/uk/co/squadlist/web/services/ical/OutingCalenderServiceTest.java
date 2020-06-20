package uk.co.squadlist.web.services.ical;

import com.google.common.collect.Lists;
import net.fortuna.ical4j.model.Calendar;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.Squad;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class OutingCalenderServiceTest {

    private final OutingCalendarService outingCalendarService = new OutingCalendarService();

    @Test
    public void canBuildIcalCalenderForOuting() throws Exception {
        Squad squad = new Squad();
        squad.setName("A squad");

        Outing anOuting = new Outing();
        DateTime outingTime = new DateTime(2020, 6, 20, 8, 00, 0, DateTimeZone.forID("Europe/London"));
        anOuting.setDate(outingTime.toDate());
        anOuting.setSquad(squad);

        OutingAvailability anOutingAvailability = new OutingAvailability();
        anOutingAvailability.setOuting(anOuting);

        List<OutingAvailability> outingAvailabilities = Lists.newArrayList();
        outingAvailabilities.add(anOutingAvailability);

        Instance instance = new Instance();
        instance.setName("Some club");
        instance.setTimeZone("Europe/London");

        Calendar calendar = outingCalendarService.buildCalendarFor(outingAvailabilities, instance);

        final String calenderOutput = calendar.toString();
        assertTrue(calenderOutput.contains("NAME:Some club outings"));
        assertTrue(calenderOutput.contains("DTSTART;TZID=Europe/London:20200620T080000"));
    }

}
