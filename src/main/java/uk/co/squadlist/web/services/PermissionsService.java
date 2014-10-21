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
	
	public boolean hasPermission(String loggedInMemberId, Permission permission) throws UnknownMemberException {
		final Member loggedInMember = api.getMemberDetails(loggedInMemberId);	// TODO once per request?
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

	public boolean hasMemberPermission(String loggedInMemberId, Permission permission, String memberId) throws UnknownMemberException {
		final Member loggedInMember = api.getMemberDetails(loggedInMemberId);	// TODO once per request?
		if (loggedInMember.getAdmin() != null && loggedInMember.getAdmin()) {
			return true;
		}
		
		if (permission == Permission.VIEW_MEMBER_DETAILS || permission == Permission.EDIT_MEMBER_DETAILS) {
			return canEditMembersDetails(loggedInMember, api.getMemberDetails(memberId));
		}
		
		return false;
	}
	
	public boolean hasSquadPermission(String loggedInMemberId, Permission permission, Squad squad) throws UnknownMemberException, UnknownSquadException {
		final Member loggedInMember = api.getMemberDetails(loggedInMemberId);	// TODO once per request?
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
	
	public boolean hasOutingPermission(String loggedInMemberId, Permission permission, String outingId) throws UnknownMemberException, UnknownOutingException {
		final Member loggedInMember = api.getMemberDetails(loggedInMemberId);	// TODO once per request?
		if (loggedInMember.getAdmin() != null && loggedInMember.getAdmin()) {
			return true;
		}

		final Outing outing = api.getOuting(outingId);
		if (permission == Permission.EDIT_OUTING) {
			return canEditOuting(loggedInMember, outing);
		}
		
		return false;
	}
	
	private boolean canSeeAdminScreen(Member member) {
		return userIsCoachOrSquadRep(member);
	}
	
	public boolean canSetAdminUsers(Member member) {
		return userIsCoach(member);
	}
	
	public boolean canEditConfig(Member member) {
		return false;
	}
	
	public boolean canAlterSquadMembers(Member member, Squad squad) {
		return userIsCoach(member) || isSquadRepForThisSquad(member, squad);
	}
		
	public boolean canViewOutingsForSquad(Member loggedInRower, Squad squad) {		
  		final boolean userIsAMemberOfThisSquad = isMemberOfSquad(loggedInRower, squad);
   		final boolean memberIsCoach = userIsCoach(loggedInRower);
  		return memberIsCoach || userIsAMemberOfThisSquad;	
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
	
	public boolean canSeePhoneNumberForRower(Member loggedInMember, Member member) {
		if (loggedInMember.equals(member)) {
			return true;
		}
		final boolean targetIsCoachOrSquadRep = userIsCoach(member) || userIsSquadRep(member);
		final boolean isCoach = userIsCoach(loggedInMember);
		final boolean areInSameSquad = areInSameSquads(loggedInMember, member);		
		return targetIsCoachOrSquadRep || isCoach || areInSameSquad;		
	}
	
	public boolean canAddNewOuting(Member member) {
		return userIsCoachOrSquadRep(member);
	}
	
	public boolean canResetRowersPassword(Member member) {
		return userIsCoachOrSquadRep(member);
	}

	public boolean canSeeAllSquadsAvailability(Member loggedInRower) {
		return userIsCoach(loggedInRower);
	}
	
	public boolean canSeeAllSquadsEntryDetails(Member loggedInRower) {
		return canSeeAllSquadsAvailability(loggedInRower);
	}
		
	public boolean canAddNewSquad(Member member) {
		return userIsCoach(member);
	}
	
	public boolean canDeleteSquad(Member loggedInRower) {
		return userIsCoach(loggedInRower);
	}

	// TODO squad reps should only be able to delete users from their own squads.
	public boolean canDeleteRower(Member member) {
		return userIsCoachOrSquadRep(member);
	}
	
	public boolean canEditMembersDetails(Member loggedInRower, Member member) {
		final boolean isSameRower = loggedInRower.equals(member);
		final boolean isSquadRepForRower = isSquadRepForRower(loggedInRower, member);
		final boolean isCoach =  userIsCoach(loggedInRower);
		return isSameRower || isCoach || isSquadRepForRower;
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
	
	public boolean canSeeEntryFormDetailsFor(Member member, Squad squad) {		
		if (userIsCoach(member)) {
			return true;
		}
		if (isSquadRepForSquad(member, squad)) {
			return true;
		}
		return false;
	}
	
	public boolean canChangeRoleFor(Member loggedInRower, Member member) {		
		if (userIsCoach(member)) {
			return true;
		}		
		return false;
	}
	
	public boolean canExportRowerData(Member member) {
		return userIsCoach(member);
	}
	
	public boolean canCancelOuting(Member member, Outing outing) {
      	return userIsCoach(member) || isSquadRepForOuting(member, outing);
   	}
	
	public boolean canCloseOuting(Member member, Outing outing) {
		return userIsCoach(member) || isSquadRepForOuting(member, outing);
	}
	
	public boolean canReopenOuting(Member member, Outing outing) {
		return userIsCoach(member) || isSquadRepForOuting(member, outing);		
	}
		
	public boolean canEditOuting(Member member, Outing outing) {
		return userIsCoach(member) || isSquadRepForOuting(member, outing);
	}
		
	public boolean canSendUsersPassword(Member member) {
		return userIsCoachOrSquadRep(member);
	}

	public boolean canMakeRowerActive(Member loggedInRower) {
		return userIsCoach(loggedInRower) || userIsSquadRep(loggedInRower);
	}
	
	public boolean canMakeRowerInactive(Member loggedInRower) {
		return userIsCoach(loggedInRower) || userIsSquadRep(loggedInRower);
	}
	
	public boolean canMakeSquadInactive(Member loggedInRower) {
		return userIsCoach(loggedInRower);
	}
	
	public boolean canSeeEntryFormDetailsForSquad(Member member, Squad squad) {
		return userIsCoach(member) || isSquadRepForThisSquad(member, squad);
	}
	
	public boolean canSeeContactDetailsForSquad(Member member, Squad squad) {
		return true;
	}

	private boolean isMemberOfSquad(Member member, Squad squad) {
		return member.getSquads().contains(squad);
	}
		
	public boolean canSeeOtherSquadMembersAvailability(Member loggedInRower, Squad squad, Instance instance) {
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
	
}