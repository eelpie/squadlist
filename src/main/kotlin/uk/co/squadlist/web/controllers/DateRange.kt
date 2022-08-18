package uk.co.squadlist.web.controllers

import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.YearMonth
import uk.co.squadlist.web.views.DateHelper

data class DateRange(val start: LocalDate?, val end: LocalDate?, val month: YearMonth?, val current: Boolean) {

    fun isCurrent(): Boolean = current

    companion object {
        @JvmStatic
        fun from(month: YearMonth?, startDate: LocalDate?, endDate: LocalDate?): DateRange {
            if (month != null) {
                val monthStartDay = month.toLocalDate(1)
                return DateRange(
                    monthStartDay,
                    monthStartDay.plusMonths(1).minusDays(1),
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
