package com.vibhuti.orion

import app.cash.turbine.test
import com.vibhuti.orion.data.repository.Item
import com.vibhuti.orion.data.repository.ItemRepository
import com.vibhuti.orion.ui.home.HomeUiState
import com.vibhuti.orion.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private class FakeRepo(val result: Result<List<Item>>) : ItemRepository {
        override suspend fun getItems() = result
    }

    @Test fun successEmitsItems() = runTest(dispatcher) {
        val items = listOf(Item(1, "t", "b"))
        val vm = HomeViewModel(FakeRepo(Result.success(items)))
        vm.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            assertEquals(HomeUiState.Success(items), awaitItem())
        }
    }

    @Test fun failureEmitsError() = runTest(dispatcher) {
        val vm = HomeViewModel(FakeRepo(Result.failure(RuntimeException("boom"))))
        vm.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            assertTrue(awaitItem() is HomeUiState.Error)
        }
    }
}
