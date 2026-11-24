package com.vibhuti.orion.data.repository

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Deterministic, realistic mock data. Today's night reproduces the Figma frame's values exactly;
 * every other night is seeded by its date so it stays stable across launches.
 */
object MockInsightsData {

    private const val HISTORY_DAYS = 27L
    private const val USER_AGE = 25

    /** About four weeks of history plus the rest of the current week (Sun..Sat), like the design. */
    fun days(today: LocalDate): List<DaySummary> {
        val first = today.minusDays(HISTORY_DAYS).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val last = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
        return generateSequence(first) { it.plusDays(1) }
            .takeWhile { !it.isAfter(last) }
            .map { DaySummary(it, if (it.isAfter(today)) null else scoreFor(it, today)) }
            .toList()
    }

    fun night(date: LocalDate, today: LocalDate): NightInsight {
        require(!date.isAfter(today)) { "No data for a future night" }
        return if (date == today) designNight(date) else generatedNight(date, today)
    }

    private fun scoreFor(date: LocalDate, today: LocalDate): Int =
        if (date == today) 72 else seeded(date).nextInt(40, 93)

    private fun seeded(date: LocalDate) = Random(date.toEpochDay() * 7919)

    private fun session(date: LocalDate) =
        SleepSession(start = date.minusDays(1).atTime(23, 30), end = date.atTime(8, 30))

    // ---- The night shown in Figma -------------------------------------------------------------

    private fun designNight(date: LocalDate): NightInsight {
        val s = session(date)
        fun at(minute: Int, second: Int = 0) = s.start.plusMinutes(minute.toLong()).plusSeconds(second.toLong())
        // Positions follow the ticks in the frame; durations sum to the design's 3m 24s restless time.
        val movementSpecs = listOf(
            9 to 30, 76 to 12, 165 to 12, 203 to 12, 263 to 12,
            284 to 40, 365 to 14, 372 to 14, 379 to 14, 443 to 44,
        )
        val movements = movementSpecs.map { (min, secs) -> TimedEvent(at(min), at(min, secs)) }
        val leftBed = listOf(
            TimedEvent(at(226), at(226, 50)),
            TimedEvent(at(339), at(348)),
        )
        return NightInsight(
            date = date,
            score = 72,
            session = s,
            bodyMovements = BodyMovements(
                restlessTime = movements.fold(Duration.ZERO) { acc, e -> acc + e.duration },
                ratePerHour = 3.2f,
                movements = movements,
                leftBed = leftBed,
            ),
            temperature = onTarget(
                temperatureCurve(s, Random(42), base = 27.4f, cool = 25.05f, warm = 29.35f, dropAt = 88, riseAt = 405),
            ),
            sleepAge = SleepAge(18, 23, USER_AGE, 10, 70, "Your sleep quality resembled someone in their early 20s."),
            vitals = listOf(
                Vital(VitalType.HRV, 32, 11..20),
                Vital(VitalType.HEART_RATE, 58, 56..64),
                Vital(VitalType.BREATH_RATE, 14, 12..16),
            ),
        )
    }

    // ---- Other nights -------------------------------------------------------------------------

    private fun generatedNight(date: LocalDate, today: LocalDate): NightInsight {
        val rnd = seeded(date)
        val score = scoreFor(date, today)
        val s = session(date)
        val minutes = s.length.toMinutes().toInt()

        // Bed exits first (non-overlapping), then movements that never fall inside an exit or overlap each other.
        val leftBed = mutableListOf<TimedEvent>()
        repeat(if (score < 65) rnd.nextInt(1, 3) else rnd.nextInt(0, 2)) {
            val start = s.start.plusMinutes(rnd.nextInt(60, minutes - 60).toLong())
            val exit = TimedEvent(start, start.plusMinutes(rnd.nextLong(2, 12)))
            if (leftBed.none { it.overlaps(exit, paddingSeconds = 600) }) leftBed += exit
        }
        leftBed.sortBy { it.start }

        // Restless nights move more: the rate (per hour, including micro-movements) tracks the score,
        // and the timeline shows the most significant movements.
        val ratePerHour = ((4.6f - (score - 48) * 0.055f + (rnd.nextFloat() - 0.5f) * 0.6f).coerceIn(1.5f, 5f) * 10).roundToInt() / 10f
        val shownCount = (ratePerHour * 3).roundToInt().coerceIn(4, 14)
        val movements = mutableListOf<TimedEvent>()
        var attempts = 0
        while (movements.size < shownCount && attempts++ < 200) {
            val start = s.start.plusMinutes(rnd.nextInt(5, minutes - 5).toLong()).plusSeconds(rnd.nextLong(0, 60))
            val candidate = TimedEvent(start, start.plusSeconds(rnd.nextLong(8, 45)))
            val clashes = leftBed.any { it.overlaps(candidate, paddingSeconds = 60) } ||
                movements.any { it.overlaps(candidate, paddingSeconds = 30) }
            if (!clashes) movements += candidate
        }
        movements.sortBy { it.start }
        val restless = movements.fold(Duration.ZERO) { acc, e -> acc + e.duration }

        val ageLow = when {
            score >= 80 -> rnd.nextInt(18, 21)
            score >= 65 -> rnd.nextInt(19, 25)
            else -> rnd.nextInt(26, 36)
        }
        val ageHigh = ageLow + 5

        val curve = temperatureCurve(
            s, rnd,
            base = 26.9f + rnd.nextFloat() * 0.8f,
            cool = 24.7f + rnd.nextFloat() * 0.7f,
            warm = 28.8f + rnd.nextFloat() * 0.8f,
            dropAt = rnd.nextInt(70, 110),
            riseAt = rnd.nextInt(380, 430),
        )
        val maxDeviationF = (rnd.nextFloat() * 2.4f * 10).roundToInt() / 10f
        val temperature = if (maxDeviationF <= 1f) onTarget(curve) else TemperatureSummary(
            headline = "Mostly on target",
            detail = "Your Orion followed your schedule within ${String.format(Locale.US, "%.1f", maxDeviationF)}°F accuracy.",
            samples = curve,
        )

        return NightInsight(
            date = date,
            score = score,
            session = s,
            bodyMovements = BodyMovements(
                restlessTime = restless,
                ratePerHour = ratePerHour,
                movements = movements,
                leftBed = leftBed,
            ),
            temperature = temperature,
            sleepAge = SleepAge(
                ageLow, ageHigh, USER_AGE, 10, 70,
                "Your sleep quality resembled someone in their ${decadeText((ageLow + ageHigh) / 2)}.",
            ),
            vitals = listOf(
                Vital(VitalType.HRV, rnd.nextInt(12, 27), 11..20),
                Vital(VitalType.HEART_RATE, rnd.nextInt(54, 66), 56..64),
                Vital(VitalType.BREATH_RATE, rnd.nextInt(12, 17), 12..16),
            ),
        )
    }

    private fun onTarget(samples: List<TemperatureSample>) = TemperatureSummary(
        headline = "On target all night",
        detail = "Your Orion followed your schedule within 1°F accuracy.",
        samples = samples,
    )

    private fun decadeText(age: Int): String {
        val part = when (age % 10) {
            in 0..3 -> "early"
            in 4..6 -> "mid"
            else -> "late"
        }
        return "$part ${age / 10 * 10}s"
    }

    /**
     * Bed temperature schedule: warm at sleep onset, cool through deep sleep, warming before wake.
     * Sampled every 4 minutes with small sensor noise, clamped to the 24..30 °C chart range.
     */
    private fun temperatureCurve(
        s: SleepSession,
        rnd: Random,
        base: Float,
        cool: Float,
        warm: Float,
        dropAt: Int,
        riseAt: Int,
    ): List<TemperatureSample> {
        val minutes = s.length.toMinutes().toInt()
        return (0..minutes step 4).map { m ->
            val target = when {
                m < dropAt -> base
                m < dropAt + 16 -> lerp(base, cool, smooth((m - dropAt) / 16f))
                m < riseAt -> cool + 0.25f * (m - dropAt) / (riseAt - dropAt)
                m < riseAt + 64 -> lerp(cool + 0.25f, warm, smooth((m - riseAt) / 64f))
                else -> warm
            }
            val noise = (rnd.nextFloat() - 0.5f) * 0.22f
            TemperatureSample(s.start.plusMinutes(m.toLong()), (target + noise).coerceIn(24f, 30f))
        }
    }

    private fun TimedEvent.overlaps(other: TimedEvent, paddingSeconds: Long): Boolean =
        start.minusSeconds(paddingSeconds) < other.end && other.start < end.plusSeconds(paddingSeconds)

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
    private fun smooth(t: Float) = t * t * (3 - 2 * t)
}
