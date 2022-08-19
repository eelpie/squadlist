package uk.co.squadlist.web.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.squadlist.client.swagger.ApiException
import uk.co.squadlist.web.api.SquadlistApiFactory

@Component
class PasswordGenerator @Autowired constructor(private val squadlistApiFactory: SquadlistApiFactory) {
    fun generateRandomPassword(): String {
        return try {
            val suggestions = squadlistApiFactory.createUnauthenticatedSwaggerClient().passwordSuggestionsGet()
            suggestions[0] // This is ok because the list is randomised.
        } catch (e: ApiException) {
            throw RuntimeException(e)
        }
    }
}