package com.vibhuti.orion

import app.cash.turbine.test
import com.vibhuti.orion.data.repository.DaySummary
import com.vibhuti.orion.data.repository.InsightsRepository
import com.vibhuti.orion.data.repository.MockInsightsData
import com.vibhuti.orion.data.repository.NightInsight
import com.vibhuti.orion.ui.insights.InsightsUiState
import com.vibhuti.orion.ui.insights.InsightsViewModel
import com.vibhuti.orion.ui.insights.NightState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val today = LocalDate.of(2026, 1, 21) // Wednesday, like the design

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private class FakeRepo(
        var daysResult: (LocalDate) -> Result<List<DaySummary>> = { Result.success(MockInsightsData.days(it)) },
        var nightResult: (LocalDate, LocalDate) -> Result<NightInsight> = { d, t -> Result.success(MockInsightsData.night(d, t)) },
        val nightDelays: MutableMap<LocalDate, Long> = mutableMapOf(),
    ) : InsightsRepository {
        val requestedNights = mutableListOf<LocalDate>()
        val cancelledNights = mutableListOf<LocalDate>()
        override suspend fun getDays(today: LocalDate) = daysResult(today)
        override suspend fun getNight(date: LocalDate, today: LocalDate): Result<NightInsight> {
            requestedNights += date
            try {
                nightDelays[date]?.let { delay(it) }
            } catch (e: CancellationException) {
                cancelledNights += date
                throw e
            }
            return nightResult(date, today)
        }
    }

    private fun vm(repo: FakeRepo = FakeRepo()) = InsightsViewModel(repo, clock = { today })

    @Test fun initialLoadSelectsTodayAndLoadsItsNight() = runTest(dispatcher) {
        val vm = vm()
        vm.uiState.test {
            assertEquals(InsightsUiState.Loading, awaitItem())
            val days = awaitItem() as InsightsUiState.Success
            assertEquals(today, days.selectedDate)
            assertEquals(NightState.Loading(previous = null), days.night)
            val loaded = awaitItem() as InsightsUiState.Success
            val night = (loaded.night as NightState.Loaded).insight
            assertEquals(today, night.date)
            assertEquals(72, night.score)
        }
    }

    @Test fun selectingPastDayLoadsThatNight() = runTest(dispatcher) {
        val vm = vm()
        advanceUntilIdle()
        val yesterday = today.minusDays(1)
        vm.uiState.test {
            skipItems(1)
            vm.selectDate(yesterday)
            val loading = awaitItem() as InsightsUiState.Success
            assertEquals(yesterday, loading.selectedDate)
            // The previously shown night stays visible while the new one loads.
            assertEquals(today, (loading.night as NightState.Loading).previous?.date)
            val loaded = awaitItem() as InsightsUiState.Success
            assertEquals(yesterday, (loaded.night as NightState.Loaded).insight.date)
        }
    }

    @Test fun futureAndCurrentDaysAreIgnored() = runTest(dispatcher) {
        val repo = FakeRepo()
        val vm = vm(repo)
        advanceUntilIdle()
        val before = vm.uiState.value
        vm.selectDate(today.plusDays(1))
        vm.selectDate(today)
        advanceUntilIdle()
        assertEquals(before, vm.uiState.value)
        assertEquals(listOf(today), repo.requestedNights)
    }

    @Test fun rapidSelectionKeepsOnlyLatestNight() = runTest(dispatcher) {
        val first = today.minusDays(2)
        val second = today.minusDays(3)
        val repo = FakeRepo(nightDelays = mutableMapOf(first to 1_000L, second to 10L))
        val vm = vm(repo)
        advanceUntilIdle()
        vm.selectDate(first)
        runCurrent() // first request is now in flight (suspended in its delay)
        vm.selectDate(second)
        advanceUntilIdle()
        val state = vm.uiState.value as InsightsUiState.Success
        assertEquals(second, state.selectedDate)
        assertEquals(second, (state.night as NightState.Loaded).insight.date)
        assertEquals(listOf(first), repo.cancelledNights)
    }

    @Test fun reloadRecoversFromDaysFailure() = runTest(dispatcher) {
        val repo = FakeRepo(daysResult = { Result.failure(RuntimeException("offline")) })
        val vm = vm(repo)
        advanceUntilIdle()
        assertEquals(InsightsUiState.Error("offline"), vm.uiState.value)

        repo.daysResult = { Result.success(MockInsightsData.days(it)) }
        vm.load()
        assertEquals(InsightsUiState.Loading, vm.uiState.value)
        advanceUntilIdle()
        val state = vm.uiState.value as InsightsUiState.Success
        assertTrue(state.night is NightState.Loaded)
    }

    @Test fun daysFailureEmitsError() = runTest(dispatcher) {
        val vm = vm(FakeRepo(daysResult = { Result.failure(RuntimeException("offline")) }))
        vm.uiState.test {
            assertEquals(InsightsUiState.Loading, awaitItem())
            assertEquals(InsightsUiState.Error("offline"), awaitItem())
        }
    }

    @Test fun nightFailureEmitsNightErrorAndRetryRecovers() = runTest(dispatcher) {
        val repo = FakeRepo(nightResult = { _, _ -> Result.failure(RuntimeException("boom")) })
        val vm = vm(repo)
        advanceUntilIdle()
        val failed = vm.uiState.value as InsightsUiState.Success
        assertEquals(NightState.Error("boom"), failed.night)

        repo.nightResult = { d, t -> Result.success(MockInsightsData.night(d, t)) }
        vm.retryNight()
        advanceUntilIdle()
        val recovered = vm.uiState.value as InsightsUiState.Success
        assertTrue(recovered.night is NightState.Loaded)
    }
}
