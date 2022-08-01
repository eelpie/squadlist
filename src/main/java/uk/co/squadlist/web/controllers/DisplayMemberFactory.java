package uk.co.squadlist.web.controllers;

import org.springframework.stereotype.Component;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.views.model.DisplayMember;

import java.util.ArrayList;
import java.util.List;

@Component
public class DisplayMemberFactory {

    private final PermissionsService permissionsService;

    public DisplayMemberFactory(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    public List<DisplayMember> toDisplayMembers(List<uk.co.squadlist.model.swagger.Member> members, uk.co.squadlist.model.swagger.Member loggedInUser) {
        List<DisplayMember> displayMembers = new ArrayList<>();
        for (uk.co.squadlist.model.swagger.Member member : members) {
            boolean isEditable = permissionsService.hasMemberPermission(loggedInUser, Permission.EDIT_MEMBER_DETAILS, member);
            displayMembers.add(new DisplayMember(member, isEditable));
        }
        return displayMembers;
    }

}
