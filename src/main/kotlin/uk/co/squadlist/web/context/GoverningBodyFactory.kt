package uk.co.squadlist.web.context

import org.springframework.stereotype.Component
import uk.co.squadlist.model.swagger.Instance
import uk.co.squadlist.web.localisation.BritishRowing
import uk.co.squadlist.web.localisation.GoverningBody
import uk.co.squadlist.web.localisation.RowingIreland

@Component
class GoverningBodyFactory {
    fun getGoverningBody(instance: Instance): GoverningBody {
        return governingBodyFor(instance.governingBody)
    }

    fun governingBodyFor(id: String): GoverningBody {
        return when (id) {
            "rowing-ireland" -> RowingIreland() // TODO these ids want to be on the governing bodies
            "british-rowing" -> BritishRowing()
            else -> {
                throw NotImplementedError()
            }
        }
    }

}
