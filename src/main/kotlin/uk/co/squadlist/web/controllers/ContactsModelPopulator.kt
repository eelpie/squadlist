package uk.co.squadlist.web.controllers

import com.google.common.base.Function
import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Ordering
import com.google.common.collect.Sets
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.web.annotations.RequiresSquadPermission
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.api.SquadlistApi
import uk.co.squadlist.web.api.SquadlistApiFactory
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.model.Member
import uk.co.squadlist.web.model.Squad
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.services.PermissionsService
import uk.co.squadlist.web.services.filters.ActiveMemberFilter

@Component
class ContactsModelPopulator(val instanceSpecificApiClient: InstanceSpecificApiClient,
                             val loggedInUserService: LoggedInUserService,
                             val permissionsService: PermissionsService,
                             val activeMemberFilter: ActiveMemberFilter, val squadlistApiFactory: SquadlistApiFactory) {

    private val squadlistApi: SquadlistApi = squadlistApiFactory.createClient()

    val role = Function<Member, String> { i -> i?.getRole() }
    val firstName = Function<Member, String> { i -> i?.getFirstName() }
    val lastName = Function<Member, String> { i -> i?.getLastName() }

    val byLastName = Ordering.natural<String>().nullsLast<String>().onResultOf(lastName)
    val byFirstName = Ordering.natural<String>().nullsLast<String>().onResultOf(firstName)

    val byRole = Ordering.natural<String>().nullsLast<String>().onResultOf(role)
    val byRoleThenFirstName = byRole.compound(byFirstName)
    val byRoleThenLastName = byRole.compound(byLastName)


    @RequiresSquadPermission(permission = Permission.VIEW_SQUAD_CONTACT_DETAILS)
    fun populateModel(squad: Squad, mv: ModelAndView) {
        mv.addObject("title", squad.name + " contacts")
        mv.addObject("squad", squad)

        val instance = instanceSpecificApiClient.instance
        val byRoleThenName = if (instance.memberOrdering != null && instance.memberOrdering == "firstName") byRoleThenFirstName else byRoleThenLastName

        val activeMembers = byRoleThenName.sortedCopy(activeMemberFilter.extractActive(squadlistApi.getSquadMembers(squad.id)))
        val redactedMembers = redactContentDetailsForMembers(loggedInUserService.loggedInMember, activeMembers)
        mv.addObject("members", redactedMembers)

        val emails = Sets.newHashSet<String>()
        for (member in redactedMembers) {
            if (!Strings.isNullOrEmpty(member.emailAddress)) {
                emails.add(member.emailAddress)
            }
        }
        if (!emails.isEmpty()) {
            mv.addObject("emails", Lists.newArrayList(emails))
        }
    }

    private fun redactContentDetailsForMembers(loggedInMember: Member, members: List<Member>): List<Member> {
        val redactedMembers = Lists.newArrayList<Member>()
        for (member in members) {
            if (!permissionsService.canSeePhoneNumberForRower(loggedInMember, member)) {
                member.contactNumber = null
            }
            redactedMembers.add(member)
        }
        return redactedMembers
    }

}