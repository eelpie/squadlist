package uk.co.squadlist.web.services

import com.google.common.collect.Lists
import org.joda.time.DateTime
import org.springframework.stereotype.Component
import uk.co.squadlist.client.swagger.api.DefaultApi
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.model.swagger.Squad


@Component
class OutingMonthsService {
    fun getOutingMonthsFor(instance: Instance, squad: Squad, swaggerApiClientForLoggedInUser: DefaultApi): List<String> {
        val stringBigDecimalMap = swaggerApiClientForLoggedInUser.outingsMonthsGet(
            instance.id,
            squad.id,
            DateTime.now().toDateMidnight().minusDays(1).toLocalDate(),
            DateTime.now().plusYears(20).toLocalDate()
        ) // TODO timezone
        return Lists.newArrayList(stringBigDecimalMap.keys).sorted()
    }

}