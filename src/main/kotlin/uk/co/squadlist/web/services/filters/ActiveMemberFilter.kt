package uk.co.squadlist.web.services.filters

import org.springframework.stereotype.Component
import uk.co.squadlist.model.swagger.Member

@Component
class ActiveMemberFilter {
    // TODO suggests missing API functionality
    fun extractActive(members: List<Member>): List<Member> {
        return members.filter { member -> !member.isInactive }
    }

    fun extractInactive(members: List<Member>): List<Member> {
        return members.filter { member -> member.isInactive }
    }

}