package uk.co.squadlist.web.localisation;

import static org.junit.Assert.*;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;

public class BritishRowingTest {

	private final BritishRowing britishRowing = new BritishRowing();

	@Test
	public void canRecogniseValidBRRegistrationNumbers() throws Exception {
		assertNull(britishRowing.checkRegistrationNumber("202605G1020791"));
	}

	@Test
	public void shouldReturnValidationMessageForInvalidLookingRegistrationNumber() throws Exception {
		assertEquals("Not in the expected British Rowing format", britishRowing.checkRegistrationNumber("2013343434"));
	}

	@Test
	public void shouldDetectValidButExpiredRegistrationNumbers() throws Exception {
		assertEquals("Expired registration", britishRowing.checkRegistrationNumber("201405G1020791"));
	}

	@Test
	public void shouldReturnNullProblemsForValidRegistrationNumbers() throws Exception {
		assertNull(britishRowing.checkRegistrationNumber("201805G1020791"));
	}

	@Test
	public void shouldReturnNullProblemsForNonSetRegistrationNumber() throws Exception {
		assertNull(britishRowing.checkRegistrationNumber(null));
	}

	@Test
	public void effectiveAgeForMastersIsTheAgeTheyWouldReachBy31DecOfTheCurrentYear() throws Exception {
		assertEquals(40, britishRowing.getEffectiveAge(new DateTime(1975, 12, 13, 0, 0, 0, 0).toDate()));
		assertEquals(40, britishRowing.getEffectiveAge(new DateTime(1975, 1, 1, 0, 0, 0, 0).toDate()));
		assertEquals(40, britishRowing.getEffectiveAge(new DateTime(1975, 12, 31, 0, 0, 0, 0).toDate()));
	}

	@Test
	public void canDetermineAgeGradeFromEffectiveAge() throws Exception {
		assertEquals("Masters B", britishRowing.getAgeGrade(40));
	}

	@Test
	public void canDetermineAgeGradeFromDateOfBirth() throws Exception {
		assertEquals("Masters B", britishRowing.getAgeGrade(new DateTime(1975, 2, 1, 0, 0, 0, 0).toDate()));
	}

	@Test
	public void pointsGradesAreBasedOnMaximumPointsAllowed() throws Exception {
		assertEquals("Novice", britishRowing.getRowingStatus(Lists.newArrayList("0")));
		assertEquals("Intermediate 3", britishRowing.getRowingStatus(Lists.newArrayList("1")));
		assertEquals("Intermediate 3", britishRowing.getRowingStatus(Lists.newArrayList("2")));
		assertEquals("Intermediate 2", britishRowing.getRowingStatus(Lists.newArrayList("3")));
		assertEquals("Intermediate 1", britishRowing.getRowingStatus(Lists.newArrayList("5")));
		assertEquals("Senior", britishRowing.getRowingStatus(Lists.newArrayList("9")));
		assertEquals("Elite", britishRowing.getRowingStatus(Lists.newArrayList("10")));
	}

	@Test
	public void nullPointsImpliesUnknownStatus() throws Exception {
		assertEquals(null, britishRowing.getRowingStatus(""));
	}

	@Test
	public void crewPointsCannotBeInferredIfSomeCrewMembersHaveNotProvidedPointsInformation() throws Exception {
		List<String> rowingPoints = Lists.newArrayList("0", "1", null, "1");

		assertNull(britishRowing.getRowingStatus(rowingPoints));
	}

	@Test
	public void canCalculateStatusForDifferentCrewSizes() throws Exception {
		assertEquals("Novice", britishRowing.getRowingStatus(Lists.newArrayList("0")));
		assertEquals("Intermediate 3", britishRowing.getRowingStatus(Lists.newArrayList("1", "0","0", "0")));
		assertEquals("Intermediate 2", britishRowing.getRowingStatus(Lists.newArrayList("16", "0","0", "0")));
		assertEquals("Intermediate 1", britishRowing.getRowingStatus(Lists.newArrayList("24", "0","0", "0")));
		assertEquals("Senior", britishRowing.getRowingStatus(Lists.newArrayList("25", "0","0", "0")));
		assertEquals("Elite", britishRowing.getRowingStatus(Lists.newArrayList("37", "0","0", "0")));
		assertEquals("Senior", britishRowing.getRowingStatus(Lists.newArrayList("72", "0", "0", "0", "0", "0", "0", "0")));
		assertEquals("Elite", britishRowing.getRowingStatus(Lists.newArrayList("73", "0", "0", "0", "0", "0", "0", "0")));
	}

}