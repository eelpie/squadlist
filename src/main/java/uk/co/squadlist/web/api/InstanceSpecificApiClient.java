package uk.co.squadlist.web.api;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.*;
import uk.co.squadlist.web.model.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class InstanceSpecificApiClient {

	private final static Logger log = Logger.getLogger(InstanceSpecificApiClient.class);

	private final InstanceConfig instanceConfig;
	private final SquadlistApi api;
	private final String clientId;
	private final String clientSecret;

	@Autowired
	public InstanceSpecificApiClient(InstanceConfig instanceConfig,
									 SquadlistApiFactory squadlistApiFactory, @Value("${client.id}") String clientId,
									 @Value("${client.secret}") String clientSecret) {
		this.instanceConfig = instanceConfig;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.api = squadlistApiFactory.createClient();
	}

	public List<Boat> getBoats() {
		return api.getBoats(instanceConfig.getInstance());
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

	public List<AvailabilityOption> getAvailabilityOptions() throws HttpFetchException, IOException {
		return api.getAvailabilityOptions(instanceConfig.getInstance());
	}

	public AvailabilityOption getAvailabilityOption(String id) throws HttpFetchException, IOException, UnknownAvailabilityOptionException {
		List<AvailabilityOption> availabilityOptions = getAvailabilityOptions();
		for (AvailabilityOption availabilityOption : availabilityOptions) {	// TODO API end point
			if (availabilityOption.getId().equals(id)) {
				return availabilityOption;
			}
		}
		throw new UnknownAvailabilityOptionException();
	}

	public String auth(String username, String password) {
		try {
			return api.requestAccessToken(instanceConfig.getInstance(), username, password, clientId, clientSecret);

		} catch (Exception e) {
			log.error("Uncaught error", e);	// TODO
			return null;
		}
	}

	public String authWithFacebook(String facebookAccessToken) {
		try {
			return api.requestAccessTokenWithFacebook(instanceConfig.getInstance(), facebookAccessToken, clientId, clientSecret);

		} catch (Exception e) {
			log.error("Uncaught error", e);	// TODO
			return null;
		}
	}

	public Member verify(String token) {
		return api.verify(token);
	}

	public Map<String, Integer> getOutingMonths(Squad squad) {
		return api.getOutingMonths(instanceConfig.getInstance(), Lists.newArrayList(squad), DateTime.now().toDateMidnight().minusDays(1).toDate(), DateTime.now().plusYears(20).toDate());
	}

	public List<OutingWithSquadAvailability> getSquadAvailability(String squadId, Date startDate, Date endDate) {
		return api.getSquadAvailability(instanceConfig.getInstance(), squadId, startDate, endDate);
	}

	public List<Outing> getSquadOutings(Squad squad, Date startDate, Date endDate) {
		return api.getOutings(instanceConfig.getInstance(), Lists.newArrayList(squad), startDate, endDate);
	}

	public Squad getSquad(String squadId) throws UnknownSquadException {
		return api.getSquad(instanceConfig.getInstance(), squadId);
	}

	public Instance getInstance() throws UnknownInstanceException {
		return api.getInstance(instanceConfig.getInstance());
	}

	public Member createMember(String firstName, String lastName, List<Squad> squads,
			String emailAddress, String initialPassword, Date dateOfBirth, String role) throws InvalidMemberException {
		return api.createMember(instanceConfig.getInstance(), firstName, lastName, squads, emailAddress, initialPassword, dateOfBirth, role);
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

	public Squad setSquadMembers(Squad squad, Set<String> updatedSquadMembers) throws IOException, HttpFetchException {
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

	public void deleteSquad(Squad squad) {
		api.deleteSquad(instanceConfig.getInstance(), squad);
	}

	public void setAdmins(Set<String> admins) throws IOException, HttpFetchException {
		api.setAdmins(instanceConfig.getInstance(), admins);
	}

	public void updateInstance(Instance instance) {
		api.updateInstance(instance);
	}

	public Boat getBoat(String id) throws UnknownSquadException, UnknownBoatException {
		return api.getBoat(instanceConfig.getInstance(), id);
	}

}