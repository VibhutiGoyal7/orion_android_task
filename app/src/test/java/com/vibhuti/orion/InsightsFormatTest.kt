package com.vibhuti.orion

import com.vibhuti.orion.data.repository.MockInsightsData
import com.vibhuti.orion.ui.insights.InsightsFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate

class InsightsFormatTest {
    @Test fun longDateMatchesDesign() {
        assertEquals("Wednesday, Jan 21st", InsightsFormat.longDate(LocalDate.of(2026, 1, 21)))
        assertEquals("Sunday, Jan 11th", InsightsFormat.longDate(LocalDate.of(2026, 1, 11)))
        assertEquals("Thursday, Jan 22nd", InsightsFormat.longDate(LocalDate.of(2026, 1, 22)))
    }

    @Test fun durations() {
        assertEquals("3m 24s", InsightsFormat.minutesSeconds(Duration.ofSeconds(204)))
        assertEquals("42s", InsightsFormat.shortDuration(Duration.ofSeconds(42)))
        assertEquals("1m 4s", InsightsFormat.shortDuration(Duration.ofSeconds(64)))
        assertEquals("9m", InsightsFormat.shortDuration(Duration.ofMinutes(9)))
    }

    @Test fun ratingAndDotAgree() {
        for (score in 0..100) {
            val rating = InsightsFormat.scoreRating(score)
            assertEquals(rating == "Fair" || rating == "Poor", InsightsFormat.isPoorNight(score))
        }
    }

    @Test fun designNightReproducesFigmaValues() {
        val today = LocalDate.of(2026, 1, 21)
        val night = MockInsightsData.night(today, today)
        assertEquals(72, night.score)
        assertEquals("Excellent", InsightsFormat.scoreRating(night.score))
        assertEquals("3m 24s", InsightsFormat.minutesSeconds(night.bodyMovements.restlessTime))
        assertEquals("3.2/hr", InsightsFormat.rate(night.bodyMovements.ratePerHour))
        assertEquals(listOf("11:30 PM", "1:00 AM", "3:00 AM", "6:00 AM", "8:30 AM"),
            InsightsFormat.axisLabels(night.session).map { it.text })
        assertTrue(night.temperature.samples.all { it.celsius in 24f..30f })
    }

    @Test fun generatedNightsAreConsistent() {
        val today = LocalDate.of(2026, 1, 21)
        MockInsightsData.days(today).filter { it.score != null && it.date != today }.forEach { day ->
            val n = MockInsightsData.night(day.date, today)
            val bm = n.bodyMovements
            bm.movements.forEach { m -> assertTrue(bm.leftBed.none { m.start < it.end && it.start < m.end }) }
            bm.movements.zipWithNext().forEach { (a, b) -> assertTrue(a.end <= b.start) }
            assertTrue(n.sleepAge.rangeStart >= 18)
            assertTrue(n.temperature.samples.all { it.celsius in 24f..30f })
        }
    }
}
