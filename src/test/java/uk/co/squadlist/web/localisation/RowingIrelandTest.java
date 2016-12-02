package uk.co.squadlist.web.localisation;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RowingIrelandTest {

	private final RowingIreland rowingIreland = new RowingIreland();

	@Test
	public void rowingIrelandRegisterationNumbersumbersAreNumbersWithUptoFiveDigits() throws Exception {
		assertNull(rowingIreland.checkRegistrationNumber("99999"));
		assertNull(rowingIreland.checkRegistrationNumber("12345"));
		assertNull(rowingIreland.checkRegistrationNumber("12"));
	}

	@Test
	public void shouldReturnValidationMessageForInvalidLookingRegistrationNumber() throws Exception {
		assertEquals("Not in the expected Rowing Ireland format", rowingIreland.checkRegistrationNumber("abc123"));
		assertEquals("Not in the expected Rowing Ireland format", rowingIreland.checkRegistrationNumber("1234567"));
	}

	@Test
	public void shouldReturnNullProblemsForEmptyRegistrationNumber() throws Exception {
		assertNull(rowingIreland.checkRegistrationNumber(null));
	}

	@Test
	public void pointsGradesAreBasedOnMaximumPointsAllowed() throws Exception {
		assertEquals("Novice", rowingIreland.getRowingStatus(Lists.newArrayList("0")));
		assertEquals("Club 2", rowingIreland.getRowingStatus(Lists.newArrayList("200")));
		assertEquals("Club 2", rowingIreland.getRowingStatus(Lists.newArrayList("250")));
		assertEquals("Club 1", rowingIreland.getRowingStatus(Lists.newArrayList("251")));
		assertEquals("Club 1", rowingIreland.getRowingStatus(Lists.newArrayList("450")));
		assertEquals("Intermediate", rowingIreland.getRowingStatus(Lists.newArrayList("550")));
		assertEquals("Senior", rowingIreland.getRowingStatus(Lists.newArrayList("800")));	// TODO check edging - is 800 Senior or not
		assertEquals("Senior", rowingIreland.getRowingStatus(Lists.newArrayList("1010")));
	}

	@Test
	public void canCalculateStatusForDifferentCrewSizes() throws Exception {
		assertEquals("Club 2", rowingIreland.getRowingStatus(Lists.newArrayList("50", "400", "50", "50")));
		assertEquals("Club 1", rowingIreland.getRowingStatus(Lists.newArrayList("50", "400", "50", "550")));
	}

	@Test
	public void nullPointsImpliesUnknownStatus() throws Exception {
		assertEquals(null, rowingIreland.getRowingStatus(""));
	}

	@Test
	public void crewPointsCannotBeInferredIfSomeCrewMembersHaveNotProvidedPointsInformation() throws Exception {
		List<String> rowingPoints = Lists.newArrayList("0", "1", null, "1");
		assertNull(rowingIreland.getRowingStatus(rowingPoints));
	}

	@Test
	public void effectiveAgeForMastersIsTheAgeTheyWouldReachBy31DecOfTheCurrentYear() throws Exception {
		assertEquals(40, rowingIreland.getEffectiveAge(new DateTime(1975, 12, 13, 0, 0, 0, 0).toDate()));
		assertEquals(40, rowingIreland.getEffectiveAge(new DateTime(1975, 1, 1, 0, 0, 0, 0).toDate()));
		assertEquals(40, rowingIreland.getEffectiveAge(new DateTime(1975, 12, 31, 0, 0, 0, 0).toDate()));
	}

	@Test
	public void canDetermineAgeGradeFromEffectiveAge() throws Exception {
		assertEquals("Masters B", rowingIreland.getAgeGrade(40));
	}

	@Test
	public void canDetermineAgeGradeFromDateOfBirth() throws Exception {
		assertEquals("Masters B", rowingIreland.getAgeGrade(new DateTime(1975, 2, 1, 0, 0, 0, 0).toDate()));
	}

}