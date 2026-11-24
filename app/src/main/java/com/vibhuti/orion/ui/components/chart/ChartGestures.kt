package com.vibhuti.orion.ui.components.chart

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

/** A touch on a chart: [x] as a 0..1 fraction of the width, [y] in px from the top. */
data class ChartTouch(val x: Float, val y: Float)

/**
 * Touch handling shared by the charts.
 *
 * - Tap -> [onTap] (charts toggle a persistent selection).
 * - Horizontal drag -> [onScrub] continuously, then [onScrubEnd] on release. Only horizontal drags are
 *   consumed, so the parent's vertical scroll keeps working when the gesture starts on a chart.
 *
 * The lambdas are captured once; callers should read changing values through `rememberUpdatedState`.
 */
fun Modifier.chartGestures(
    onTap: (ChartTouch) -> Unit,
    onScrub: (ChartTouch) -> Unit,
    onScrubEnd: () -> Unit,
): Modifier = this
    .pointerInput(Unit) {
        detectTapGestures { onTap(touch(it, size.width)) }
    }
    .pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = { onScrub(touch(it, size.width)) },
            onDragEnd = onScrubEnd,
            onDragCancel = onScrubEnd,
        ) { change, _ ->
            change.consume()
            onScrub(touch(change.position, size.width))
        }
    }

private fun touch(position: Offset, width: Int) =
    ChartTouch(x = if (width <= 0) 0f else (position.x / width).coerceIn(0f, 1f), y = position.y)
