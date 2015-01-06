package uk.co.squadlist.web.api;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.squadlist.web.annotations.Timed;
import uk.co.squadlist.web.exceptions.InvalidAvailabilityOptionException;
import uk.co.squadlist.web.exceptions.InvalidInstanceException;
import uk.co.squadlist.web.exceptions.InvalidMemberException;
import uk.co.squadlist.web.exceptions.InvalidOutingException;
import uk.co.squadlist.web.exceptions.InvalidSquadException;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.AvailabilityOption;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;

@Component
public class InstanceSpecificApiClient {

	private InstanceConfig instanceConfig;
	private SquadlistApi api;

	public InstanceSpecificApiClient() {
	}

	@Autowired
	public InstanceSpecificApiClient(InstanceConfig instanceConfig, SquadlistApi api) {
		this.instanceConfig = instanceConfig;
		this.api = api;
	}

	@Timed
	public List<Squad> getSquads() {
		return api.getSquads(instanceConfig.getInstance());
	}

	public List<Member> getSquadMembers(String squadId) {
		return api.getSquadMembers(instanceConfig.getInstance(), squadId);
	}

	public List<Member> getMembers() {
		return api.getMembers(instanceConfig.getInstance());
	}

	public List<AvailabilityOption> getAvailabilityOptions() throws JsonParseException, JsonMappingException, HttpFetchException, IOException {
		return api.getAvailabilityOptions(instanceConfig.getInstance());
	}

	public AvailabilityOption getAvailabilityOption(String id) throws JsonParseException, JsonMappingException, HttpFetchException, IOException {
		List<AvailabilityOption> availabilityOptions = getAvailabilityOptions();
		for (AvailabilityOption availabilityOption : availabilityOptions) {	// TODO API end point
			if (availabilityOption.getId().equals(id)) {
				return availabilityOption;
			}
		}
		return null;
	}

	public Member auth(String username, String password) {
		return api.auth(instanceConfig.getInstance(), username, password);
	}

	public Member authFacebook(String token) {
		return api.authFacebook(instanceConfig.getInstance(), token);
	}

	public Map<String, Integer> getSquadOutingMonths(String squadId) {
		return api.getSquadOutingMonths(instanceConfig.getInstance(), squadId);
	}

	public List<OutingWithSquadAvailability> getSquadAvailability(String squadId, Date startDate, Date endDate) {
		return api.getSquadAvailability(instanceConfig.getInstance(), squadId, startDate, endDate);
	}

	public List<Outing> getSquadOutings(String squadId, Date startDate, Date endDate) {
		return api.getSquadOutings(instanceConfig.getInstance(), squadId, startDate, endDate);
	}

	public Squad getSquad(String squadId) throws UnknownSquadException {
		return api.getSquad(instanceConfig.getInstance(), squadId);
	}

	public Member getMemberDetails(String id) throws UnknownMemberException {
		return api.getMemberDetails(instanceConfig.getInstance(), id);
	}

	public Instance getInstance() throws UnknownInstanceException {
		return api.getInstance(instanceConfig.getInstance());
	}

	public Member createMember(String firstName, String lastName, List<Squad> squads,
			String emailAddress, String initialPassword, Date dateOfBirth, String role) throws InvalidMemberException {
		return api.createMember(instanceConfig.getInstance(), firstName, lastName, squads, emailAddress, initialPassword, dateOfBirth, role);
	}

	public Member updateMemberDetails(Member member) {
		return api.updateMemberDetails(instanceConfig.getInstance(), member);
	}

	public Member updateMemberProfileImage(Member member, byte[] image) {
		return api.updateMemberProfileImage(instanceConfig.getInstance(), member, image);
	}

	public boolean changePassword(String id, String currentPassword, String newPassword) {
		return api.changePassword(instanceConfig.getInstance(), id, currentPassword, newPassword);
	}

	public List<OutingAvailability> getAvailabilityFor(String loggedInUser, Date startDate, Date endDate) {
		return api.getAvailabilityFor(instanceConfig.getInstance(), loggedInUser, startDate, endDate);
	}

	public Squad createSquad(String name) throws InvalidSquadException {
		return api.createSquad(instanceConfig.getInstance(), name);
	}

	public void resetPassword(String username) throws UnknownMemberException {
		api.resetPassword(instanceConfig.getInstance(), username);
	}

	public String confirmResetPassword(String token) {
		return api.confirmResetPassword(instanceConfig.getInstance(), token);
	}

	public Outing getOuting(String id) throws UnknownOutingException {
		return api.getOuting(instanceConfig.getInstance(), id);
	}

	public Map<String, AvailabilityOption> getOutingAvailability(String id) throws UnknownOutingException {
		return api.getOutingAvailability(instanceConfig.getInstance(), id);
	}

	public Outing createOuting(Outing newOuting) throws InvalidOutingException {
		return api.createOuting(instanceConfig.getInstance(), newOuting);
	}

	public Outing createOuting(Outing newOuting, int repeats) throws InvalidOutingException {
		return api.createOuting(instanceConfig.getInstance(), newOuting, repeats);
	}

	public Outing updateOuting(Outing updatedOuting) throws InvalidOutingException {
		return api.updateOuting(instanceConfig.getInstance(), updatedOuting);
	}

	public OutingAvailability setOutingAvailability(Member member, Outing outing, AvailabilityOption availabilityOption) {
		return api.setOutingAvailability(instanceConfig.getInstance(), member, outing, availabilityOption);
	}

	public Squad updateSquad(Squad squad) {
		return api.updateSquad(instanceConfig.getInstance(), squad);
	}

	public Squad setSquadMembers(Squad squad, Set<String> updatedSquadMembers) throws JsonGenerationException, JsonMappingException, HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, IOException, HttpFetchException {
		return api.setSquadMembers(instanceConfig.getInstance(), squad.getId(), updatedSquadMembers);
	}

	public Map<String, Object> statistics() throws UnknownInstanceException {
		return api.getInstanceStatistics(instanceConfig.getInstance());
	}

	public String resetMemberPassword(Member member) throws UnknownMemberException {
		return api.resetMemberPassword(instanceConfig.getInstance(), member.getId());
	}

	public void deleteOuting(Outing outing) throws InvalidInstanceException {
		api.deleteOuting(instanceConfig.getInstance(), outing.getId());	// TODO 404?
	}

	public void createAvailabilityOption(String name, String colour) throws InvalidAvailabilityOptionException {
		api.createAvailabilityOption(instanceConfig.getInstance(), new AvailabilityOption(name, colour));
	}

	public void updateAvailabilityOption(AvailabilityOption availabilityOption) throws InvalidAvailabilityOptionException {
		api.updateAvailabilityOption(instanceConfig.getInstance(), availabilityOption);
	}

	public void deleteAvailabilityOption(AvailabilityOption availabilityOption) {
		api.deleteAvailabilityOption(instanceConfig.getInstance(), availabilityOption);
	}

	public void deleteAvailabilityOption(AvailabilityOption availabilityOption, AvailabilityOption alternativeOption) {
		api.deleteAvailabilityOption(instanceConfig.getInstance(), availabilityOption, alternativeOption);
	}
	
	public void deleteMember(Member member) {
		api.deleteMember(instanceConfig.getInstance(), member);
	}

	public void setAdmins(Set<String> admins) throws JsonGenerationException, JsonMappingException, HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, IOException, HttpFetchException {
		api.setAdmins(instanceConfig.getInstance(), admins);
	}

}