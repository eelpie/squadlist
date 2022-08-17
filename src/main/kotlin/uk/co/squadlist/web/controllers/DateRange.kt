package uk.co.squadlist.web.controllers

import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import uk.co.squadlist.web.views.DateHelper

data class DateRange(val start: LocalDate?, val end: LocalDate?, val month: String?, val current: Boolean) {

    fun isCurrent(): Boolean = current

    companion object {
        @JvmStatic
        fun from(month: String?, startDate: LocalDate?, endDate: LocalDate?): DateRange {
            if (month != null) {
                val monthDateTime =
                    ISODateTimeFormat.yearMonth().parseLocalDateTime(month) // TODO Can be moved to spring?
                return DateRange(
                    monthDateTime.toLocalDate(),
                    monthDateTime.plusMonths(1).minusDays(1).toLocalDate(),
                    month,
                    false
                )

            } else if (startDate != null && endDate != null) {
                val duration = Duration(startDate.toDateTimeAtCurrentTime(), endDate.toDateTimeAtCurrentTime())
                if (duration.standardDays > 0 && duration.standardDays < 50) {
                    return DateRange(
                        startDate,
                        endDate,
                        null,
                        false
                    )
                }
            }

            return DateRange(
                DateHelper.startOfCurrentOutingPeriod().toLocalDate(),
                DateHelper.endOfCurrentOutingPeriod().toLocalDate(),
                null,
                true
            )

        }
    }

}
