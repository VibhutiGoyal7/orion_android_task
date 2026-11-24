package com.vibhuti.orion.ui.insights

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vibhuti.orion.data.repository.DaySummary
import com.vibhuti.orion.data.repository.MockInsightsData
import com.vibhuti.orion.data.repository.NightInsight
import com.vibhuti.orion.ui.components.ActionCard
import com.vibhuti.orion.ui.components.DateStrip
import com.vibhuti.orion.ui.components.DateStripItem
import com.vibhuti.orion.ui.components.DayIndicator
import com.vibhuti.orion.ui.components.ScoreHeader
import com.vibhuti.orion.ui.insights.sections.BodyMovementsSection
import com.vibhuti.orion.ui.insights.sections.SleepAgeSection
import com.vibhuti.orion.ui.insights.sections.SleepVitalsSection
import com.vibhuti.orion.ui.insights.sections.TemperatureSection
import com.vibhuti.orion.ui.theme.OrionColors
import com.vibhuti.orion.ui.theme.OrionTheme
import java.time.LocalDate

private val ScreenPadding = 16.dp

@Composable
fun InsightsRoute(
    modifier: Modifier = Modifier,
    viewModel: InsightsViewModel = viewModel(factory = InsightsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InsightsScreen(
        state = state,
        onSelectDate = viewModel::selectDate,
        onRetry = viewModel::load,
        onRetryNight = viewModel::retryNight,
        onSleptOtherSide = {},
        modifier = modifier,
    )
}

/** Stateless: everything comes in through [state] and leaves through the lambdas. */
@Composable
fun InsightsScreen(
    state: InsightsUiState,
    onSelectDate: (LocalDate) -> Unit,
    onRetry: () -> Unit,
    onRetryNight: () -> Unit,
    onSleptOtherSide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(OrionColors.Background)) {
        when (state) {
            InsightsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            is InsightsUiState.Error -> ErrorMessage(state.message, onRetry, Modifier.align(Alignment.Center))
            is InsightsUiState.Success -> InsightsContent(state, onSelectDate, onRetryNight, onSleptOtherSide)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InsightsContent(
    state: InsightsUiState.Success,
    onSelectDate: (LocalDate) -> Unit,
    onRetryNight: () -> Unit,
    onSleptOtherSide: () -> Unit,
) {
    val stripItems = remember(state.days, state.selectedDate) {
        state.days.map { it.toStripItem(selected = it.date == state.selectedDate) }
    }
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp + navBarInset),
    ) {
        item(key = "title") {
            Text(
                text = "Insights",
                style = MaterialTheme.typography.headlineMedium,
                color = OrionColors.TextPrimary,
                modifier = Modifier
                    .padding(start = ScreenPadding + 4.dp, end = ScreenPadding, top = 24.dp, bottom = 16.dp)
                    .semantics { heading() },
            )
        }
        stickyHeader(key = "dates") {
            DateStrip(
                items = stripItems,
                onSelect = { onSelectDate(LocalDate.ofEpochDay(it.key)) },
                modifier = Modifier
                    .background(OrionColors.Background)
                    .padding(bottom = 8.dp),
            )
        }
        when (val night = state.night) {
            is NightState.Loading -> when (val previous = night.previous) {
                null -> item(key = "night-loading") {
                    Box(Modifier.fillParentMaxWidth().height(360.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                // Keep the same keyed items composed so values animate into the new night.
                else -> nightItems(previous, onSleptOtherSide, dimmed = true)
            }
            is NightState.Error -> item(key = "night-error") {
                Box(Modifier.fillParentMaxWidth().height(360.dp), contentAlignment = Alignment.Center) {
                    ErrorMessage(night.message, onRetryNight)
                }
            }
            is NightState.Loaded -> nightItems(night.insight, onSleptOtherSide, dimmed = false)
        }
    }
}

private fun LazyListScope.nightItems(insight: NightInsight, onSleptOtherSide: () -> Unit, dimmed: Boolean) {
    val cardModifier = Modifier.padding(horizontal = ScreenPadding)
    val gap = Modifier.padding(top = 12.dp)
    fun dimmedItem(key: String, content: @Composable () -> Unit) = item(key = key) {
        val alpha by animateFloatAsState(if (dimmed) 0.45f else 1f, label = "dim")
        Box(Modifier.graphicsLayer { this.alpha = alpha }) { content() }
    }
    dimmedItem(key = "score") {
        ScoreHeader(
            score = insight.score,
            rating = InsightsFormat.scoreRating(insight.score),
            caption = InsightsFormat.longDate(insight.date),
            modifier = Modifier.padding(start = ScreenPadding + 4.dp, end = ScreenPadding, top = 16.dp, bottom = 12.dp),
        )
    }
    dimmedItem(key = "body-movements") {
        BodyMovementsSection(insight.bodyMovements, insight.session, cardModifier.then(gap))
    }
    dimmedItem(key = "temperature") {
        TemperatureSection(insight.temperature, insight.session, cardModifier.then(gap))
    }
    dimmedItem(key = "sleep-age") {
        SleepAgeSection(insight.sleepAge, cardModifier.then(gap))
    }
    dimmedItem(key = "vitals") {
        SleepVitalsSection(insight.vitals, cardModifier.then(gap))
    }
    dimmedItem(key = "other-side") {
        ActionCard(
            icon = Icons.Rounded.Repeat,
            title = "Slept on the other side?",
            subtitle = "Update your side to fix your insight.",
            onClick = onSleptOtherSide,
            modifier = cardModifier.then(gap),
        )
    }
}

@Composable
private fun ErrorMessage(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = OrionColors.TextSecondary)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}

private fun DaySummary.toStripItem(selected: Boolean) = DateStripItem(
    key = date.toEpochDay(),
    weekday = InsightsFormat.weekdayShort(date),
    dayOfMonth = date.dayOfMonth.toString(),
    indicator = when {
        score == null -> DayIndicator.None
        InsightsFormat.isPoorNight(score) -> DayIndicator.Poor
        else -> DayIndicator.Good
    },
    enabled = score != null,
    selected = selected,
)

@Preview(widthDp = 393, heightDp = 1400)
@Composable
private fun InsightsScreenPreview() {
    val today = LocalDate.of(2026, 1, 21)
    OrionTheme {
        InsightsScreen(
            state = InsightsUiState.Success(
                days = MockInsightsData.days(today),
                selectedDate = today,
                night = NightState.Loaded(MockInsightsData.night(today, today)),
            ),
            onSelectDate = {},
            onRetry = {},
            onRetryNight = {},
            onSleptOtherSide = {},
        )
    }
}
