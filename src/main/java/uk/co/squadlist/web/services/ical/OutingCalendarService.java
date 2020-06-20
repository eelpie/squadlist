package uk.co.squadlist.web.services.ical;

import com.google.common.base.Strings;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.property.*;
import org.springframework.stereotype.Component;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;

import java.net.SocketException;
import java.util.List;

@Component
public class OutingCalendarService {

	private static final Dur ONE_HOUR = new Dur(0, 1, 0, 0);

	public Calendar buildCalendarFor(List<OutingAvailability> availability, Instance instance) throws SocketException {
		final Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//Squadlist//iCal4j 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		
		final String name = instance.getName() + " outings";
		calendar.getProperties().add(new Name(name));
		calendar.getProperties().add(new XProperty("X-WR-CALNAME", name));	// TODO check source code for enum
		calendar.getProperties().add(new XProperty("X-PUBLISHED-TTL", "PT1H"));
		calendar.getProperties().add(new XProperty("REFRESH-INTERVAL;VALUE=DURATION", "P1H"));
		
		for (OutingAvailability outingAvailability : availability) {
			final Outing outing = outingAvailability.getOuting();
			calendar.getComponents().add(buildEventFor(outing, instance.getTimeZone()));
		}
		return calendar;
	}

	private VEvent buildEventFor(final Outing outing, String timeZone) {
		final VEvent outingEvent = new VEvent(new net.fortuna.ical4j.model.DateTime(outing.getDate()), ONE_HOUR, outing.getSquad().getName());

		final TzId tzParam = new TzId(timeZone);
		outingEvent.getProperties().getProperty(Property.DTSTART).getParameters().add(tzParam);

		if (!Strings.isNullOrEmpty(outing.getNotes())) {
			outingEvent.getProperties().add(new Description(outing.getNotes()));		
		}

		Uid uid = new Uid(outing.getId());
		outingEvent.getProperties().add(uid);
		
		return outingEvent;
	}
	
}