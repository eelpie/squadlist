package uk.co.squadlist.web.urls

import com.google.common.base.Joiner
import com.google.common.collect.Lists
import org.apache.commons.codec.digest.DigestUtils
import org.apache.http.client.utils.URIBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.co.squadlist.web.context.InstanceConfig
import uk.co.squadlist.web.localisation.GoverningBody
import uk.co.squadlist.web.model.*
import java.net.URISyntaxException

@Component
class UrlBuilder (@param:Value("\${baseUrl}") private val baseUrl: String,
                  val instanceConfig: InstanceConfig,
                  val seoLinkBuilder: SeoLinkBuilder,
                  @param:Value("\${apiUrl}") private val apiUrl: String,
                  @param:Value("\${profilePicturesUrl}") private val profilePicturesUrl: String) {

    val linkFacebookCallbackUrl: String
        get() = applicationUrl("/social/facebook/link/callback")

    fun applicationUrl(uri: String): String {
        return getBaseUrl() + uri
    }

    fun loginUrl(): String {
        return applicationUrl("/login")
    }

    fun resetPassword(): String {
        return applicationUrl("/reset-password")
    }

    fun staticUrl(uri: String): String {
        return applicationUrl("/assets/") + uri
    }

    fun boatUrl(boat: Boat): String {
        return applicationUrl("/boats/" + boat.id)
    }

    fun memberUrl(member: Member): String {
        return memberUrl(member.id)
    }

    fun memberResetPasswordUrl(member: Member): String {
        return memberUrl(member.id) + "/reset"
    }

    fun memberUrl(memberId: String): String {
        return applicationUrl("/member/$memberId")
    }

    fun makeActive(member: Member): String {
        return memberUrl(member.id) + "/make-active"
    }

    fun makeInactive(member: Member): String {
        return memberUrl(member.id) + "/make-inactive"
    }

    fun delete(member: Member): String {
        return memberUrl(member.id) + "/delete"
    }

    fun delete(squad: Squad): String {
        return applicationUrl("/squad/" + squad.id + "/delete")
    }

    fun newMemberUrl(): String {
        return applicationUrl("/member/new")
    }

    fun newSquadUrl(): String {
        return applicationUrl("/squad/new")
    }

    fun editSquadUrl(squad: Squad): String {
        return applicationUrl("/squad/" + squad.id + "/edit")
    }

    fun adminUrl(): String {
        return applicationUrl("/admin")
    }

    fun outingsUrl(): String {
        return applicationUrl("/outings")
    }

    fun outingUrl(outing: Outing): String {
        return outingsUrl() + "/" + outing.id
    }

    fun outingAvailabilityCsv(outing: Outing): String {
        return outingUrl(outing) + ".csv"
    }

    fun outingCloseUrl(outing: Outing): String {
        return outingUrl(outing) + "/close"
    }

    fun deleteOuting(outing: Outing): String {
        return outingUrl(outing) + "/delete"
    }

    fun outingReopenUrl(outing: Outing): String {
        return outingUrl(outing) + "/reopen"
    }

    fun outingEditUrl(outing: Outing): String {
        return outingUrl(outing) + "/edit"
    }

    fun entryDetails(squad: Squad): String {
        return applicationUrl("/entry-details?squad=" + squad.id)
    }

    fun outings(squad: Squad): String {
        return applicationUrl("/outings?squad=" + squad.id)
    }

    fun outings(squad: Squad, month: String): String {
        return outings(squad) + "&month=" + month
    }

    fun availability(squad: Squad): String {
        return applicationUrl("/availability/" + squad.id)
    }

    fun availability(squad: Squad, month: String): String {
        return availability(squad) + "?month=" + month
    }

    fun editInstanceSettings(): String {
        return adminUrl() + "/instance"
    }

    fun editMemberUrl(member: Member): String {
        return memberUrl(member) + "/edit"
    }

    fun editMemberUrl(memberId: String): String {
        return memberUrl(memberId) + "/edit"
    }

    fun entryDetailsCsv(squad: Squad): String {
        return applicationUrl("/entrydetails/" + squad.id + ".csv")
    }

    fun entryDetailsCsv(members: List<Member>): String {
        try {
            val url = URIBuilder(applicationUrl("/entrydetails/selected.csv"))

            val memberIds = Lists.newArrayList<String>()
            for (member in members) {
                memberIds.add(member.id)
            }
            url.addParameter("members", Joiner.on(",").join(memberIds))

            return url.toString()

        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

    }

    fun changePassword(): String {
        return applicationUrl("/change-password")
    }

    fun socialMediaAccounts(): String {
        return applicationUrl("/social")
    }

    fun linkFacebookUrl(): String {
        return applicationUrl("/social/facebook/link")
    }

    fun removeFacebookUrl(): String {
        return applicationUrl("/social/facebook/remove")
    }

    fun facebookSigninCallbackUrl(): String {
        return applicationUrl("/social/facebook/signin/callback")
    }

    fun facebookSignin(): String {
        return applicationUrl("/social/facebook/signin")
    }

    fun getBaseUrl(): String {
        return baseUrl.replace("INSTANCE", instanceConfig.vhost)
    }

    fun contactsUrl(): String {
        return applicationUrl("/contacts")
    }

    fun contactsUrl(prefferredSquad: Squad): String {
        return appendSquad(prefferredSquad, contactsUrl())
    }

    @Throws(URISyntaxException::class)
    fun outingsUrl(prefferredSquad: Squad?): String {
        val url = URIBuilder(outingsUrl())
        if (prefferredSquad != null) {
            url.addParameter("squad", prefferredSquad.id)
        }
        return url.build().toString()
    }

    @Throws(URISyntaxException::class)
    fun outingsRss(userid: String, instance: Instance): String {
        val url = URIBuilder(applicationUrl("/rss"))
        url.addParameter("user", userid)
        url.addParameter("key", generateFeedKeyFor(userid, instance))
        return url.build().toString()
    }

    @Throws(URISyntaxException::class)
    fun outingsIcal(userid: String, instance: Instance): String {
        val webcalBaseUrl = getBaseUrl().replace("^https?://".toRegex(), "webcal://")
        val url = URIBuilder("$webcalBaseUrl/ical")
        url.addParameter("user", userid)
        url.addParameter("key", generateFeedKeyFor(userid, instance))
        return url.build().toString()
    }

    fun availabilityUrl(prefferredSquad: Squad): String {
        return appendSquad(prefferredSquad, availabilityUrl())
    }

    fun governingBody(governingBody: GoverningBody): String {
        return applicationUrl("/governing-body/" + seoLinkBuilder.makeSeoLinkFor(governingBody.name))
    }

    fun staticImage(filename: String): String {
        return "$apiUrl/static/$filename.jpg"
    }

    fun profileImage(filename: String): String {
        return "$profilePicturesUrl/$filename.jpg"    // TODO ideally all of this would be provided by the API
    }

    fun newAvailabilityOptionUrl(): String {
        return applicationUrl("/availability-option/new")
    }

    fun deleteAvailabilityOptionUrl(availabilityOption: AvailabilityOption): String {
        return applicationUrl("/availability-option/" + availabilityOption.id + "/delete")
    }

    fun editAvailabilityOptionUrl(availabilityOption: AvailabilityOption): String {
        return applicationUrl("/availability-option/" + availabilityOption.id + "/edit")
    }

    fun editAdmins(): String {
        return adminUrl() + "/admins"
    }

    fun adminExportMembersAsCSV(): String {
        return adminUrl() + "/export/members.csv"
    }

    private fun availabilityUrl(): String {
        return applicationUrl("/availability")
    }

    private fun appendSquad(prefferredSquad: Squad?, baseUrl: String): String {
        return if (prefferredSquad != null) baseUrl + "/" + prefferredSquad.id else baseUrl
    }

    private fun generateFeedKeyFor(userid: String, instance: Instance): String {
        return DigestUtils.md5Hex(instance.id + userid)
    }

    fun mailto(emails: List<String>): String? {
        return if (emails.isEmpty()) {
            null
        } else "mailto:" + Joiner.on(",").join(emails)
    }

}
