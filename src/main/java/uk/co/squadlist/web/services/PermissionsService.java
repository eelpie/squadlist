package uk.co.squadlist.web.services;

import org.springframework.stereotype.Component;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.model.swagger.Outing;
import uk.co.squadlist.model.swagger.Squad;

import java.util.List;

@Component
public class PermissionsService {

	private static final String REP = "Rep";
	private static final String COACH = "Coach";	// TODO push to an enum

	public boolean hasPermission(Member loggedInMember, Permission permission) {
		if (isAdmin(loggedInMember)) {
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
			return canSeeEntryFormDetailsLink(loggedInMember);
		}

		return false;
	}

	public boolean hasMemberPermission(Member loggedInMember, Permission permission, Member member) {
		if (isAdmin(loggedInMember)) {
			return true;
		}

		if (permission == Permission.VIEW_MEMBER_DETAILS || permission == Permission.EDIT_MEMBER_DETAILS) {
			return canEditMembersDetails(loggedInMember, member);
		}

		return false;
	}

	public boolean hasSquadPermission(Member loggedInMember, Permission permission, Squad squad) {
		if (isAdmin(loggedInMember)) {
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

	public boolean hasOutingPermission(Member loggedInMember, Permission permission, Outing outing) {
		if (isAdmin(loggedInMember)) {
			return true;
		}

		if (permission == Permission.EDIT_OUTING) {
			return canEditOuting(loggedInMember, outing);
		}

		return false;
	}

	public boolean canAddNewOuting(Member member) {
		if (isAdmin(member)) {
			return true;
		}
		return userIsCoachOrSquadRep(member);
	}

	public boolean canChangeRoleFor(Member loggedInRower, Member member) {
		if (isAdmin(loggedInRower)) {
			return true;
		}
		if (loggedInRower.getId().equals(member.getId())) {
		    return false;
        }
        return userIsCoach(loggedInRower);
    }

	public boolean canEditOuting(Member member, Outing outing) {
		if (isAdmin(member)) {
			return true;
		}
		return userIsCoach(member) || isSquadRepForOuting(member, outing);
	}

	public boolean canSeeEntryFormDetailsForSquad(Member member, Squad squad) {
		return isAdmin(member) || userIsCoach(member) || isSquadRepForThisSquad(member, squad);
	}

	public boolean canSeeContactDetailsForSquad(Member member, Squad squad) {
		return true;	// TODO
	}
	
	private boolean canEditMembersDetails(Member loggedInRower, Member member) {
        if (isAdmin(loggedInRower) || (userIsCoach(loggedInRower)) || loggedInRower.getId().equals(member.getId())) {
			return true;
		}
        return isSquadRepForRower(loggedInRower, member);
	}

    private boolean isAdmin(Member loggedInRower) {
        return loggedInRower.isAdmin();
    }

    private boolean isMemberOfSquad(Member member, Squad squad) {
		return member.getSquads().contains(squad);
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

	private boolean canSeeEntryFormDetailsLink(Member loggedInMember) {
		return isAdmin(loggedInMember) || userIsCoach(loggedInMember) || userIsSquadRep(loggedInMember);
	}

}