package uk.co.squadlist.web.api;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.exceptions.InvalidMemberException;
import uk.co.squadlist.web.exceptions.InvalidOutingException;
import uk.co.squadlist.web.exceptions.InvalidSquadException;
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

	private final InstanceConfig instanceConfig;
	private final SquadlistApi api;

	@Autowired
	public InstanceSpecificApiClient(InstanceConfig instanceConfig, SquadlistApi api) {
		this.instanceConfig = instanceConfig;
		this.api = api;
	}
	
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

	public Member auth(String username, String password) {
		return api.auth(instanceConfig.getInstance(), username, password);
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

	public Instance getInstance() {
		return api.getInstance(instanceConfig.getInstance());
	}

	public Member createMember(String firstName, String lastName, List<Squad> squads, 
			String emailAddress, String initialPassword, Date dateOfBirth) throws InvalidMemberException {
		return api.createMember(instanceConfig.getInstance(), firstName, lastName, squads, emailAddress, initialPassword, dateOfBirth);
	}

	public Member updateMemberDetails(Member member) {
		return api.updateMemberDetails(instanceConfig.getInstance(), member);		
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

	public OutingAvailability setOutingAvailability(String loggedInUser, String id, String availability) {
		return api.setOutingAvailability(instanceConfig.getInstance(), loggedInUser, id, availability);
	}

	public Squad updateSquad(Squad squad) {
		return api.updateSquad(instanceConfig.getInstance(), squad);
	}
	
}
