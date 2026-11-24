package com.vibhuti.orion.data.repository

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/** One entry of the date strip. [score] is null when there is no tracked night (e.g. future days). */
data class DaySummary(val date: LocalDate, val score: Int?)

data class SleepSession(val start: LocalDateTime, val end: LocalDateTime) {
    val length: Duration get() = Duration.between(start, end)

    /** Position of [time] within the session, 0..1. */
    fun fractionOf(time: LocalDateTime): Float =
        (Duration.between(start, time).toMillis().toFloat() / length.toMillis()).coerceIn(0f, 1f)
}

data class TimedEvent(val start: LocalDateTime, val end: LocalDateTime) {
    val duration: Duration get() = Duration.between(start, end)
}

data class BodyMovements(
    val restlessTime: Duration,
    val ratePerHour: Float,
    val movements: List<TimedEvent>,
    val leftBed: List<TimedEvent>,
)

data class TemperatureSample(val time: LocalDateTime, val celsius: Float)

data class TemperatureSummary(
    val headline: String,
    val detail: String,
    val samples: List<TemperatureSample>,
)

data class SleepAge(
    val rangeStart: Int,
    val rangeEnd: Int,
    val actualAge: Int,
    val scaleMin: Int,
    val scaleMax: Int,
    val summary: String,
)

enum class VitalType { HRV, HEART_RATE, BREATH_RATE }

data class Vital(val type: VitalType, val value: Int, val normalRange: IntRange)

data class NightInsight(
    val date: LocalDate,
    val score: Int,
    val session: SleepSession,
    val bodyMovements: BodyMovements,
    val temperature: TemperatureSummary,
    val sleepAge: SleepAge,
    val vitals: List<Vital>,
)
