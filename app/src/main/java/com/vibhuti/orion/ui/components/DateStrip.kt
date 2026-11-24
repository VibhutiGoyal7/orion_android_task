package com.vibhuti.orion.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first

@Immutable
data class DateStripItem(
    val key: Long,
    val weekday: String,
    val dayOfMonth: String,
    val indicator: DayIndicator,
    val enabled: Boolean,
    val selected: Boolean,
)

/**
 * Horizontally scrollable row of [DayChip]s sized so exactly [visibleDays] fill the width.
 * Opens on the trailing week and smoothly brings the selection into view when it changes off-screen.
 */
@Composable
fun DateStrip(
    items: List<DateStripItem>,
    onSelect: (DateStripItem) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 12.dp,
    spacing: Dp = 2.dp,
    visibleDays: Int = 7,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val chipWidth = (maxWidth - horizontalPadding * 2 - spacing * (visibleDays - 1)) / visibleDays
        DateStripRow(items, onSelect, chipWidth, PaddingValues(horizontal = horizontalPadding), spacing, visibleDays)
    }
}

@Composable
private fun DateStripRow(
    items: List<DateStripItem>,
    onSelect: (DateStripItem) -> Unit,
    chipWidth: Dp,
    contentPadding: PaddingValues,
    spacing: Dp,
    visibleDays: Int,
) {
    val selectedIndex = items.indexOfFirst { it.selected }.coerceAtLeast(0)
    // Open with the selected day centred (clamped at the ends), like the design.
    val maxFirst = (items.size - visibleDays).coerceAtLeast(0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedIndex - visibleDays / 2).coerceIn(0, maxFirst),
    )

    LaunchedEffect(selectedIndex) {
        snapshotFlow { listState.layoutInfo }.first { it.viewportSize.width > 0 && it.visibleItemsInfo.isNotEmpty() }
        val info = listState.layoutInfo
        val itemSize = info.visibleItemsInfo.first().size
        // Centre the selected day in the viewport (LazyList clamps at the list ends).
        listState.animateScrollToItem(selectedIndex, -(info.viewportSize.width - itemSize) / 2)
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        items(items, key = { it.key }) { item ->
            DayChip(
                weekday = item.weekday,
                dayOfMonth = item.dayOfMonth,
                indicator = item.indicator,
                selected = item.selected,
                enabled = item.enabled,
                onClick = { onSelect(item) },
                width = chipWidth,
            )
        }
    }
}
