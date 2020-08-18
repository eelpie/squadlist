package uk.co.squadlist.web.api;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.squadlist.web.exceptions.*;
import uk.co.squadlist.web.model.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InstanceSpecificApiClient {

    private final SquadlistApi api;
    private final String instanceId;

    public InstanceSpecificApiClient(SquadlistApi squadlistApi, String instanceId) {
        this.api = squadlistApi;
        this.instanceId = instanceId;
    }

    public List<Boat> getBoats() {
        return api.getBoats(instanceId);
    }

    public List<Squad> getSquads() {
        return api.getSquads(instanceId);
    }

    public List<Member> getMembers() {
        return api.getMembers(instanceId);
    }

    public List<AvailabilityOption> getAvailabilityOptions() throws HttpFetchException, IOException {
        return api.getAvailabilityOptions(instanceId);
    }

    public AvailabilityOption getAvailabilityOption(String id) throws HttpFetchException, IOException, UnknownAvailabilityOptionException {
        List<AvailabilityOption> availabilityOptions = getAvailabilityOptions();
        for (AvailabilityOption availabilityOption : availabilityOptions) {    // TODO API end point
            if (availabilityOption.getId().equals(id)) {
                return availabilityOption;
            }
        }
        throw new UnknownAvailabilityOptionException();
    }

    public Map<String, Integer> getOutingMonths(Squad squad) {
        return api.getOutingMonths(instanceId, Lists.newArrayList(squad), DateTime.now().toDateMidnight().minusDays(1).toDate(), DateTime.now().plusYears(20).toDate());
    }

    public List<Outing> getSquadOutings(Squad squad, Date startDate, Date endDate) {
        return api.getOutings(instanceId, Lists.newArrayList(squad), startDate, endDate);
    }

    public Instance getInstance() throws UnknownInstanceException {
        return api.getInstance(instanceId);
    }

    public Member createMember(String firstName, String lastName, List<Squad> squads,
                               String emailAddress, String initialPassword, Date dateOfBirth, String role) throws InvalidMemberException {
        return api.createMember(instanceId, firstName, lastName, squads, emailAddress, initialPassword, dateOfBirth, role);
    }

    public void resetPassword(String username) throws UnknownMemberException {
        api.resetPassword(instanceId, username);
    }

    public String confirmResetPassword(String token) {
        return api.confirmResetPassword(instanceId, token);
    }

    public Map<String, Object> statistics() throws UnknownInstanceException {
        return api.getInstanceStatistics(instanceId);
    }

    public String resetMemberPassword(Member member) throws UnknownMemberException {
        return api.resetMemberPassword(instanceId, member.getId());
    }

    public void createAvailabilityOption(String name, String colour) throws InvalidAvailabilityOptionException {
        api.createAvailabilityOption(instanceId, new AvailabilityOption(name, colour));
    }

    public void updateAvailabilityOption(AvailabilityOption availabilityOption) throws InvalidAvailabilityOptionException {
        api.updateAvailabilityOption(instanceId, availabilityOption);
    }

    public void deleteAvailabilityOption(AvailabilityOption availabilityOption) {
        api.deleteAvailabilityOption(instanceId, availabilityOption);
    }

    public void deleteAvailabilityOption(AvailabilityOption availabilityOption, AvailabilityOption alternativeOption) {
        api.deleteAvailabilityOption(instanceId, availabilityOption, alternativeOption);
    }

    public void setAdmins(Set<String> admins) throws IOException, HttpFetchException {
        api.setAdmins(instanceId, admins);
    }

    public void updateInstance(Instance instance) {
        api.updateInstance(instance);
    }

    public Boat getBoat(String id) throws UnknownSquadException, UnknownBoatException {
        return api.getBoat(instanceId, id);
    }

    public Outing getOuting(String id) throws UnknownOutingException {
        return api.getOuting(id);
    }

    public Squad getSquad(String id) throws UnknownSquadException {
        return api.getSquad(id);
    }

    public List<Member> getSquadMembers(String id) {
        return api.getSquadMembers(id);
    }

    public List<OutingWithSquadAvailability> getSquadAvailability(String id, Date startDate, Date endDate) {
        return api.getSquadAvailability(id, startDate, endDate);
    }

    public Map<String, AvailabilityOption> getOutingAvailability(String id) throws UnknownOutingException {
        return api.getOutingAvailability(id);
    }

    public Squad createSquad(String name) throws UnknownInstanceException, InvalidSquadException {
        Instance instance = getInstance();
        return api.createSquad(instance, name);
    }

    public List<OutingAvailability> getAvailabilityFor(String memberId, Date toDate, Date toDate1) {
        return api.getAvailabilityFor(memberId, toDate, toDate1);
    }

    public Member getMember(String memberId) throws UnknownMemberException {
        return api.getMember(memberId);
    }

    public Member updateMemberDetails(Member member) throws InvalidMemberException {
        return api.updateMemberDetails(member);
    }

    public Member updateMemberProfileImage(Member member, byte[] bytes) throws InvalidImageException {
        return api.updateMemberProfileImage(member, bytes);
    }

    public Squad updateSquad(Squad squad) throws InvalidSquadException {
        return api.updateSquad(squad);
    }

    public Outing updateOuting(Outing outing) throws InvalidOutingException {
        return api.updateOuting(outing);
    }

    public Outing createOuting(Outing newOuting, Integer repeatsCount) throws InvalidOutingException {
        return api.createOuting(newOuting, repeatsCount);
    }

    public void deleteMember(Member member) {
        api.deleteMember(member);
    }

    public boolean changePassword(String id, String currentPassword, String newPassword) {
        return api.changePassword(id, currentPassword, newPassword);
    }

    public void deleteSquad(Squad squad) {
        api.deleteSquad(squad);
    }

    public Squad setSquadMembers(String id, Set<String> updatedSquadMembers) throws IOException, HttpFetchException {
        return api.setSquadMembers(id, updatedSquadMembers);
    }

    public void deleteOuting(String id) throws InvalidInstanceException {
        api.deleteOuting(id);
    }

    public OutingAvailability setOutingAvailability(Member loggedInMember, Outing outing, AvailabilityOption availabilityOptionById) {
        return api.setOutingAvailability(loggedInMember, outing, availabilityOptionById);
    }
}