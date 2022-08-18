package uk.co.squadlist.web.controllers

import org.springframework.stereotype.Component
import uk.co.squadlist.model.swagger.Member
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.views.model.DisplayMember

@Component
class DisplayMemberFactory(private val permissionsService: PermissionsService) {
    fun toDisplayMembers(members: List<Member?>, loggedInUser: Member?): List<DisplayMember> {
        return members.map { member ->
            val isEditable = permissionsService.hasMemberPermission(loggedInUser, Permission.EDIT_MEMBER_DETAILS, member)
            DisplayMember(member, isEditable)
        }
    }
}
