package uk.co.squadlist.web.views;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

public class DateHelperTest {

	@Test
	public void canOutputListOfMonthNames() throws Exception {
		final Map<Integer, String> months = DateHelper.getMonths();

		assertEquals(12, months.size());
		assertEquals("Jan", months.get(1));
		assertEquals("Nov", months.get(11));
	}

	@Test
	public void canOutputListOfUpcomingYears() throws Exception {
		final List<String> years = DateHelper.getYears();

		assertEquals(3, years.size());
		assertEquals(DateTime.now().toString("YYYY"), years.get(0));
	}

}
