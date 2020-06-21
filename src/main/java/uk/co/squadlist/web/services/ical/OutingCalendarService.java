package uk.co.squadlist.web.services.ical;

import com.google.common.base.Strings;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;

import java.time.Duration;
import java.util.List;

@Component
public class OutingCalendarService {

	private static final TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();

	public Calendar buildCalendarFor(List<OutingAvailability> availability, Instance instance) {
        TimeZone timeZone = registry.getTimeZone(instance.getTimeZone());

		final Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//Squadlist//iCal4j 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		calendar.getComponents().add(timeZone.getVTimeZone());

		final String name = instance.getName() + " outings";
		calendar.getProperties().add(new Name(name));
		calendar.getProperties().add(new XProperty("X-WR-CALNAME", name));	// TODO check source code for enum
		calendar.getProperties().add(new XProperty("X-PUBLISHED-TTL", "PT1H"));
		calendar.getProperties().add(new XProperty("REFRESH-INTERVAL;VALUE=DURATION", "P1H"));

        for (OutingAvailability outingAvailability : availability) {
            final Outing outing = outingAvailability.getOuting();
            calendar.getComponents().add(buildEventFor(outing, timeZone));
		}
		return calendar;
	}

	private VEvent buildEventFor(final Outing outing, TimeZone timeZone) {
		DateTime eventStartDate = new DateTime(outing.getDate());
		eventStartDate.setTimeZone(timeZone);

		Duration eventDuration = Duration.ofMinutes(60);
		String eventSummary = outing.getSquad().getName();

		final VEvent outingEvent = new VEvent(eventStartDate, eventDuration, eventSummary);


		if (!Strings.isNullOrEmpty(outing.getNotes())) {
			outingEvent.getProperties().add(new Description(outing.getNotes()));		
		}

		Uid uid = new Uid(outing.getId());
		outingEvent.getProperties().add(uid);
		
		return outingEvent;
	}
	
}