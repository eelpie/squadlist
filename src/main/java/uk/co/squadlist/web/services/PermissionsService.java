package uk.co.squadlist.web.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.UnknownMemberException;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.exceptions.UnknownSquadException;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.Squad;

@Component
public class PermissionsService {

	private static final String REP = "Rep";
	private static final String COACH = "Coach";	// TODO push to an enum

	private InstanceSpecificApiClient api;

	@Autowired
	public PermissionsService(InstanceSpecificApiClient api) {
		this.api = api;
	}

	public boolean hasPermission(Member loggedInMember, Permission permission) throws UnknownMemberException {
		if (loggedInMember.getAdmin() != null && loggedInMember.getAdmin()) {
			return true;
		}

		if (permission == Permission.ADD_MEMBER) {
			return canAddNewMember(loggedInMember);
		}
		if (permission == Permission.VIEW_ADMIN_SCREEN) {
			return canSeeAdminScreen(loggedInMember);
		}
		if (permission == Permission.ADD_OUTING) {
			return canAddNewOuting(loggedInMember);
		}
		if (permission == Permission.VIEW_ENTRY_DETAILS) {
			return canSeeEntryFormDetails(loggedInMember);
		}

		return false;
	}

	public boolean hasMemberPermission(Member loggedInMember, Permission permission, String memberId) throws UnknownMemberException {
		if (loggedInMember.getAdmin() != null && loggedInMember.getAdmin()) {
			return true;
		}

		if (permission == Permission.VIEW_MEMBER_DETAILS || permission == Permission.EDIT_MEMBER_DETAILS) {
			return canEditMembersDetails(loggedInMember, memberId);
		}

		return false;
	}

	public boolean hasSquadPermission(Member loggedInMember, Permission permission, Squad squad) throws UnknownMemberException, UnknownSquadException {
		if (loggedInMember.getAdmin() != null && loggedInMember.getAdmin()) {
			return true;
		}

		if (permission == Permission.VIEW_SQUAD_ENTRY_DETAILS) {
			return canSeeEntryFormDetailsForSquad(loggedInMember, squad);
		}
		if (permission == Permission.VIEW_SQUAD_CONTACT_DETAILS) {
			return canSeeContactDetailsForSquad(loggedInMember, squad);
		}

		return false;
	}

	public boolean hasOutingPermission(Member loggedInMember, Permission permission, String outingId) throws UnknownMemberException, UnknownOutingException {
		if (loggedInMember.getAdmin() != null && loggedInMember.getAdmin()) {
			return true;
		}

		final Outing outing = api.getOuting(outingId);
		if (permission == Permission.EDIT_OUTING) {
			return canEditOuting(loggedInMember, outing);
		}

		return false;
	}

	public boolean canSeePhoneNumberForRower(Member loggedInMember, Member member) {
		if (loggedInMember.getAdmin() != null && loggedInMember.getAdmin()) {
			return true;
		}
		if (loggedInMember.equals(member)) {
			return true;
		}
		final boolean targetIsCoachOrSquadRep = userIsCoach(member) || userIsSquadRep(member);
		final boolean isCoach = userIsCoach(loggedInMember);
		final boolean areInSameSquad = areInSameSquads(loggedInMember, member);
		return targetIsCoachOrSquadRep || isCoach || areInSameSquad;
	}

	public boolean canAddNewOuting(Member member) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
		return userIsCoachOrSquadRep(member);
	}

	public boolean canSeeAllSquadsAvailability(Member loggedInRower) {
		if (loggedInRower.getAdmin() != null && loggedInRower.getAdmin()) {
			return true;
		}
		return userIsCoach(loggedInRower);
	}

	public boolean canChangeRoleFor(Member loggedInRower, Member member) {
		if (loggedInRower.getAdmin() != null && loggedInRower.getAdmin()) {
			return true;
		}
		if (userIsCoach(loggedInRower)) {
			return true;
		}
		return false;
	}

	public boolean canEditOuting(Member member, Outing outing) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
		return userIsCoach(member) || isSquadRepForOuting(member, outing);
	}

	public boolean canSeeEntryFormDetailsForSquad(Member member, Squad squad) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
		return userIsCoach(member) || isSquadRepForThisSquad(member, squad);
	}

	public boolean canSeeContactDetailsForSquad(Member member, Squad squad) {
		return true;	// TODO
	}

	private boolean canSeeAllSquadsEntryDetails(Member loggedInRower) {
		if (loggedInRower.getAdmin() != null && loggedInRower.getAdmin()) {
			return true;
		}
		return canSeeAllSquadsAvailability(loggedInRower);
	}

	private boolean canAddNewSquad(Member member) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
		return userIsCoach(member);
	}

	private boolean canDeleteSquad(Member loggedInRower) {
		if (loggedInRower.getAdmin() != null && loggedInRower.getAdmin()) {
			return true;
		}
		return userIsCoach(loggedInRower);
	}

	// TODO squad reps should only be able to delete users from their own squads.
	private boolean canDeleteRower(Member member) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
		return userIsCoachOrSquadRep(member);
	}

	private boolean canEditMembersDetails(Member loggedInRower, String memberId) throws UnknownMemberException {
		final boolean isAdmin = loggedInRower.getAdmin() != null && loggedInRower.getAdmin();
		if (isAdmin) {
			return true;
		}
		
		final boolean isCoach =  userIsCoach(loggedInRower);
		if (isCoach) {
			return true;
		}
		
		final boolean isSameRower = loggedInRower.getId().equals(memberId);
		if (isSameRower) {
			return true;
		}
		
		final Member member = api.getMemberDetails(memberId);
		final boolean isSquadRepForRower = isSquadRepForRower(loggedInRower, member);
		return isSquadRepForRower;
	}

	private boolean canExportRowerData(Member member) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
		return userIsCoach(member);
	}

	private boolean canCancelOuting(Member member, Outing outing) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
      	return userIsCoach(member) || isSquadRepForOuting(member, outing);
   	}

	private boolean canCloseOuting(Member member, Outing outing) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
		return userIsCoach(member) || isSquadRepForOuting(member, outing);
	}

	private boolean canReopenOuting(Member member, Outing outing) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
		return userIsCoach(member) || isSquadRepForOuting(member, outing);
	}

	private boolean canSendUsersPassword(Member member) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
		return userIsCoachOrSquadRep(member);
	}

	private boolean canMakeRowerActive(Member loggedInRower) {
		if (loggedInRower.getAdmin() != null && loggedInRower.getAdmin()) {
			return true;
		}
		return userIsCoach(loggedInRower) || userIsSquadRep(loggedInRower);
	}

	private boolean canMakeRowerInactive(Member loggedInRower) {
		if (loggedInRower.getAdmin() != null && loggedInRower.getAdmin()) {
			return true;
		}
		return userIsCoach(loggedInRower) || userIsSquadRep(loggedInRower);
	}

	private boolean canMakeSquadInactive(Member loggedInRower) {
		if (loggedInRower.getAdmin() != null && loggedInRower.getAdmin()) {
			return true;
		}
		return userIsCoach(loggedInRower);
	}

	private boolean canResetRowersPassword(Member member) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
		return userIsCoachOrSquadRep(member);
	}

	private boolean canSetAdminUsers(Member member) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
		return userIsCoach(member);
	}

	private boolean canEditConfig(Member member) {
		return false;
	}

	private boolean canAlterSquadMembers(Member member, Squad squad) {
		if (member.getAdmin() != null && member.getAdmin()) {
			return true;
		}
		return userIsCoach(member) || isSquadRepForThisSquad(member, squad);
	}

	private boolean canViewOutingsForSquad(Member loggedInRower, Squad squad) {
		if (loggedInRower.getAdmin() != null && loggedInRower.getAdmin()) {
			return true;
		}
		final boolean userIsAMemberOfThisSquad = isMemberOfSquad(loggedInRower, squad);
		final boolean memberIsCoach = userIsCoach(loggedInRower);
		return memberIsCoach || userIsAMemberOfThisSquad;
	}

	private boolean isMemberOfSquad(Member member, Squad squad) {
		return member.getSquads().contains(squad);
	}

	private boolean canSeeOtherSquadMembersAvailability(Member loggedInRower, Squad squad, Instance instance) {
		if (loggedInRower.getAdmin() != null && loggedInRower.getAdmin()) {
			return true;
		}
  		 return userIsCoach(loggedInRower) || isSquadRepForThisSquad(loggedInRower, squad) || true;	// TODO make configuable and migrate
  	}

	private boolean canAddNewMember(Member member) {
		return userIsCoachOrSquadRep(member);
	}

	private boolean isSquadRepForThisSquad(Member member, Squad squad) {
		if (userIsSquadRep(member)) {
			return isMemberOfSquad(member, squad);
		}
		return false;
	}

	private boolean isSquadRepForRower(Member loggedInRower, Member member) {
		if (userIsSquadRep(loggedInRower)) {
			return areInSameSquads(loggedInRower, member);
		}
		return false;
	}

	private boolean isSquadRepForOuting(Member member, Outing outing) {
		return isSquadRepForSquad(member, outing.getSquad());
	}

	private boolean isSquadRepForSquad(Member member, Squad squad) {
		return userIsSquadRep(member) && isMemberOfSquad(member, squad);
	}

	private boolean userIsCoach(Member member) {
		return COACH.equals(member.getRole());
	}

	private boolean userIsSquadRep(Member member) {
		return REP.equals(member.getRole());
	}

	private boolean userIsCoachOrSquadRep(Member member) {
		return userIsCoach(member) || userIsSquadRep(member);
	}

	private boolean canSeeAdminScreen(Member member) {
		return userIsCoachOrSquadRep(member);
	}

	private boolean areInSameSquads(Member a, Member b) {
		List<Squad> squads = a.getSquads();
		for (Squad squad : squads) {
			if (isMemberOfSquad(b, squad)) {
				return true;
			}
		}
		return false;
	}

	private boolean canSeeEntryFormDetails(Member loggedInMember) {
		List<Squad> squads = api.getSquads();
		for (Squad squad : squads) {
			if (canSeeEntryFormDetailsForSquad(loggedInMember, squad)) {
				return true;
			}
		}
		return false;
	}

}