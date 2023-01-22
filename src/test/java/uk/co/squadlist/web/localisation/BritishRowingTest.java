package uk.co.squadlist.web.localisation;

import static org.junit.Assert.*;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;

public class BritishRowingTest {

	private final BritishRowing britishRowing = new BritishRowing();

	@Test
	public void canRecogniseValidBRRegistrationNumbers() {
		assertNull(britishRowing.checkRegistrationNumber("202605G1020791"));
	}

	@Test
	public void shouldReturnValidationMessageForInvalidLookingRegistrationNumber() {
		assertEquals("Not in the expected British Rowing format", britishRowing.checkRegistrationNumber("2013343434"));
	}

	@Test
	public void shouldDetectValidButExpiredRegistrationNumbers() {
		assertEquals("Expired registration", britishRowing.checkRegistrationNumber("201405G1020791"));
	}

	@Test
	public void shouldReturnNullProblemsForValidRegistrationNumbers() {
		assertNull(britishRowing.checkRegistrationNumber("206505G1020791"));
	}

	@Test
	public void shouldRecogniseLifeMembershipsAsValidRegistrationNumbers() {
		assertNull(britishRowing.checkRegistrationNumber("206509P1012345"));
	}

	@Test
	public void shouldReturnNullProblemsForNonSetRegistrationNumber() {
		assertNull(britishRowing.checkRegistrationNumber(null));
	}

	@Test
	public void effectiveAgeForMastersIsTheAgeTheyWouldReachBy31DecOfTheCurrentYear() {
		assertEquals(48, britishRowing.getEffectiveAge(new DateTime(1975, 12, 13, 0, 0, 0, 0)));
		assertEquals(48, britishRowing.getEffectiveAge(new DateTime(1975, 1, 1, 0, 0, 0, 0)));
		assertEquals(48, britishRowing.getEffectiveAge(new DateTime(1975, 12, 31, 0, 0, 0, 0)));
	}

	@Test
	public void canDetermineAgeGradeFromEffectiveAge() {
		assertEquals("Masters B", britishRowing.getAgeGrade(40));
	}

	@Test
	public void canDetermineAgeGradeFromDateOfBirth() {
		assertEquals("Masters C", britishRowing.getAgeGrade(new DateTime(1975, 2, 1, 0, 0, 0, 0)));
	}

	@Test
	public void pointsGradesAreBasedOnMaximumPointsAllowed() {
		assertEquals("Novice", britishRowing.getRowingStatus(Lists.newArrayList("0")));
		assertEquals("Intermediate 3", britishRowing.getRowingStatus(Lists.newArrayList("1")));
		assertEquals("Intermediate 3", britishRowing.getRowingStatus(Lists.newArrayList("2")));
		assertEquals("Intermediate 2", britishRowing.getRowingStatus(Lists.newArrayList("3")));
		assertEquals("Intermediate 1", britishRowing.getRowingStatus(Lists.newArrayList("5")));
		assertEquals("Senior", britishRowing.getRowingStatus(Lists.newArrayList("9")));
		assertEquals("Elite", britishRowing.getRowingStatus(Lists.newArrayList("10")));
	}

	@Test
	public void nullPointsImpliesUnknownStatus() {
		assertEquals(null, britishRowing.getRowingStatus(""));
	}

	@Test
	public void crewPointsCannotBeInferredIfSomeCrewMembersHaveNotProvidedPointsInformation() {
		List<String> rowingPoints = Lists.newArrayList("0", "1", null, "1");

		assertNull(britishRowing.getRowingStatus(rowingPoints));
	}

	@Test
	public void canCalculateStatusForDifferentCrewSizes() {
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