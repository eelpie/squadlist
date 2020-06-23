package uk.co.squadlist.web.views;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.squadlist.web.context.Context;

import java.time.OffsetDateTime;

import static org.junit.Assert.assertEquals;

public class DateFormatterTest {

    @Test
    public void canRenderJavaTimes() {
        Context context = Mockito.mock(Context.class);
        DateFormatter dateFormatter = new DateFormatter(context);

        DateTime dateTime = new DateTime(2020, 6, 20, 8, 00, 0, DateTimeZone.forID("Europe/London"));
        assertEquals("20 Jun 2020", dateFormatter.dayMonthYear(dateTime.toDate()));

        java.time.Instant instant = java.time.Instant.ofEpochMilli(dateTime.getMillis());
        java.time.ZoneId zoneId = java.time.ZoneId.of(dateTime.getZone().getID());
        OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, zoneId);

        assertEquals("20 Jun 2020", dateFormatter.dayMonthYear(offsetDateTime));
    }

}
