package com.vibhuti.orion.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate

interface InsightsRepository {
    /** Days for the date strip, oldest first, ending on the Saturday of [today]'s week. */
    suspend fun getDays(today: LocalDate): Result<List<DaySummary>>

    suspend fun getNight(date: LocalDate, today: LocalDate): Result<NightInsight>
}

/** Serves [MockInsightsData] with a small artificial latency, standing in for a backend. */
class MockInsightsRepository(
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val latencyMillis: Long = 250,
) : InsightsRepository {

    override suspend fun getDays(today: LocalDate): Result<List<DaySummary>> = withContext(io) {
        runCatching { MockInsightsData.days(today) }
    }

    override suspend fun getNight(date: LocalDate, today: LocalDate): Result<NightInsight> = withContext(io) {
        runCatching {
            delay(latencyMillis)
            MockInsightsData.night(date, today)
        }
    }
}
