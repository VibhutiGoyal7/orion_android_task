package com.vibhuti.orion.ui.insights

import com.vibhuti.orion.data.repository.SleepSession
import com.vibhuti.orion.ui.components.AxisLabel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Formatting helpers for the Insights screen (kept out of composables so they're testable). */
internal object InsightsFormat {

    private val clock = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    fun clock(time: LocalDateTime): String = time.format(clock)

    /** "Wednesday, Jan 21st" */
    fun longDate(date: LocalDate): String {
        val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)
        val month = date.month.getDisplayName(TextStyle.SHORT, Locale.US)
        return "$weekday, $month ${date.dayOfMonth}${ordinalSuffix(date.dayOfMonth)}"
    }

    fun weekdayShort(date: LocalDate): String = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)

    fun ordinalSuffix(day: Int): String = if (day in 11..13) "th" else when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }

    /** "3m 24s" */
    fun minutesSeconds(d: Duration): String = "${d.toMinutes()}m ${d.seconds % 60}s"

    /** "42s", "9m", "1m 4s" (same style as [minutesSeconds]) */
    fun shortDuration(d: Duration): String {
        val m = d.toMinutes()
        val s = d.seconds % 60
        return when {
            m == 0L -> "${s}s"
            s == 0L -> "${m}m"
            else -> "${m}m ${s}s"
        }
    }

    fun rate(perHour: Float): String = "${"%.1f".format(Locale.US, perHour)}/hr"

    fun celsius(v: Float): String = "${"%.1f".format(Locale.US, v)}°C"

    private const val EXCELLENT_MIN = 70
    private const val GOOD_MIN = 55
    private const val FAIR_MIN = 40

    fun scoreRating(score: Int): String = when {
        score >= EXCELLENT_MIN -> "Excellent"
        score >= GOOD_MIN -> "Good"
        score >= FAIR_MIN -> "Fair"
        else -> "Poor"
    }

    /** Red dot in the date strip: the same nights the header rates Fair or Poor. */
    fun isPoorNight(score: Int): Boolean = score < GOOD_MIN

    /** Session start, on-the-hour marks the design uses (1, 3, 6 AM) and session end. */
    fun gridTimes(session: SleepSession): List<LocalDateTime> =
        listOf(1, 3, 6)
            .map { session.end.toLocalDate().atTime(it, 0) }
            .filter { it.isAfter(session.start) && it.isBefore(session.end) }

    fun axisLabels(session: SleepSession): List<AxisLabel> = buildList {
        add(AxisLabel(0f, clock(session.start), emphasized = true))
        gridTimes(session).forEach { add(AxisLabel(session.fractionOf(it), clock(it))) }
        add(AxisLabel(1f, clock(session.end), emphasized = true))
    }
}
