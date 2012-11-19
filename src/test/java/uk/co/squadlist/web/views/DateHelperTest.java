package uk.co.squadlist.web.views;

import static org.junit.Assert.*;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;


public class DateHelperTest {

	@Test
	public void canOutputListOfMonthNames() throws Exception {
		final List<String> months = DateHelper.getMonths();
		
		assertEquals(12, months.size());
		assertEquals("Jan", months.get(0));
		assertEquals("Dec", months.get(11));
	}
	
	@Test
	public void canOutputListOfUpcomingYears() throws Exception {
		final List<String> years = DateHelper.getYears();

		assertEquals(3, years.size());
		assertEquals(DateTime.now().toString("YYYY"), years.get(0));
	}
	
}
