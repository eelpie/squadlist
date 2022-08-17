package uk.co.squadlist.web.controllers

import org.joda.time.LocalDate

data class DateRange(val start: LocalDate?, val end: LocalDate?, val month: String?, val current: Boolean) {

    fun isCurrent(): Boolean = current

}
