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
	public void officialNameIsRowingSpaceIreland() {
		assertEquals("Rowing Ireland", rowingIreland.getName());
	}

	@Test
	public void rowingIrelandRegisterationNumbersumbersAreNumbersWithUptoFiveDigits() {
		assertNull(rowingIreland.checkRegistrationNumber("99999"));
		assertNull(rowingIreland.checkRegistrationNumber("12345"));
		assertNull(rowingIreland.checkRegistrationNumber("12"));
	}

	@Test
	public void shouldReturnValidationMessageForInvalidLookingRegistrationNumber() {
		assertEquals("Not in the expected Rowing Ireland format", rowingIreland.checkRegistrationNumber("abc123"));
		assertEquals("Not in the expected Rowing Ireland format", rowingIreland.checkRegistrationNumber("1234567"));
	}

	@Test
	public void shouldReturnNullProblemsForEmptyRegistrationNumber() {
		assertNull(rowingIreland.checkRegistrationNumber(null));
	}

	@Test
	public void pointsGradesAreBasedOnMaximumPointsAllowed() {
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
	public void canCalculateStatusForDifferentCrewSizes() {
		assertEquals("Club 2", rowingIreland.getRowingStatus(Lists.newArrayList("50", "400", "50", "50")));
		assertEquals("Club 1", rowingIreland.getRowingStatus(Lists.newArrayList("50", "400", "50", "550")));
	}

	@Test
	public void nullPointsImpliesUnknownStatus() {
		assertEquals(null, rowingIreland.getRowingStatus(""));
	}

	@Test
	public void crewPointsCannotBeInferredIfSomeCrewMembersHaveNotProvidedPointsInformation() {
		List<String> rowingPoints = Lists.newArrayList("0", "1", null, "1");
		assertNull(rowingIreland.getRowingStatus(rowingPoints));
	}

	@Test
	public void effectiveAgeForMastersIsTheAgeTheyWouldReachBy31DecOfTheCurrentYear() {
		assertEquals(48, rowingIreland.getEffectiveAge(new DateTime(1975, 12, 13, 0, 0, 0, 0)));
		assertEquals(48, rowingIreland.getEffectiveAge(new DateTime(1975, 1, 1, 0, 0, 0, 0)));
		assertEquals(48, rowingIreland.getEffectiveAge(new DateTime(1975, 12, 31, 0, 0, 0, 0)));
	}

	@Test
	public void canDetermineAgeGradeFromEffectiveAge() {
		assertEquals("Masters B", rowingIreland.getAgeGrade(40));
	}

	@Test
	public void canDetermineAgeGradeFromDateOfBirth() {
		assertEquals("Masters C", rowingIreland.getAgeGrade(new DateTime(1975, 2, 1, 0, 0, 0, 0)));
	}

}