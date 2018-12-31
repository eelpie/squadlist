package uk.co.squadlist.web.controllers

import com.google.common.collect.Lists
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.web.annotations.RequiresSquadPermission
import uk.co.squadlist.web.api.SquadlistApi
import uk.co.squadlist.web.api.SquadlistApiFactory
import uk.co.squadlist.web.context.GoverningBodyFactory
import uk.co.squadlist.web.model.Member
import uk.co.squadlist.web.model.Squad
import uk.co.squadlist.web.services.Permission
import uk.co.squadlist.web.services.filters.ActiveMemberFilter
import uk.co.squadlist.web.views.DateFormatter
import java.util.*

@Component
class EntryDetailsModelPopulator (val dateFormatter: DateFormatter, val activeMemberFilter: ActiveMemberFilter,
                val governingBodyFactory: GoverningBodyFactory, squadlistApiFactory: SquadlistApiFactory) {

    @RequiresSquadPermission(permission = Permission.VIEW_SQUAD_ENTRY_DETAILS)
    fun populateModel(squadToShow: Squad, mv: ModelAndView, squadlistApi: SquadlistApi) {
        mv.addObject("squad", squadToShow)
        mv.addObject("title", squadToShow.name + " entry details")
        mv.addObject("members", activeMemberFilter.extractActive(squadlistApi.getSquadMembers(squadToShow.id)))
    }

    @RequiresSquadPermission(permission = Permission.VIEW_SQUAD_ENTRY_DETAILS)
    fun getEntryDetailsRows(squadToShow: Squad, squadlistApi: SquadlistApi): List<List<String>> {
        return getEntryDetailsRows(activeMemberFilter.extractActive(squadlistApi.getSquadMembers(squadToShow.id)))
    }

    fun getEntryDetailsRows(members: List<Member>): List<List<String>> {    // TOOD permissions
        val governingBody = governingBodyFactory.governingBody

        val rows = Lists.newArrayList<List<String>>()
        for (member in members) {
            val effectiveAge = if (member.dateOfBirth != null) governingBody.getEffectiveAge(member.dateOfBirth) else null
            val ageGrade = if (effectiveAge != null) governingBody.getAgeGrade(effectiveAge) else null

            rows.add(Arrays.asList(member.firstName, member.lastName,
                    if (member.dateOfBirth != null) dateFormatter.dayMonthYear(member.dateOfBirth) else "",
                    effectiveAge?.toString() ?: "",
                    ageGrade ?: "",
                    if (member.weight != null) member.weight!!.toString() else "",
                    member.rowingPoints,
                    governingBody.getRowingStatus(member.rowingPoints),
                    member.scullingPoints,
                    governingBody.getScullingStatus(member.scullingPoints),
                    member.registrationNumber))
        }
        return rows
    }

}
