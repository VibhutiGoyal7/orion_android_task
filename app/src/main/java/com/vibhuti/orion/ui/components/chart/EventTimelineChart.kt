package com.vibhuti.orion.ui.components.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vibhuti.orion.ui.theme.OrionColors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * An interval on the timeline, as 0..1 fractions of the width. [thickness] scales the minimum
 * marker width for events too short to be visible at true scale (1 = [EventTimelineChart]'s minEventWidth).
 */
@Immutable
data class TimelineEvent(val start: Float, val end: Float, val thickness: Float = 1f)

@Immutable
data class TimelineTrack(val events: List<TimelineEvent>, val color: Color)

@Immutable
data class TimelineSelection(val track: Int, val event: Int)

/**
 * Stacked horizontal tracks with event markers (e.g. movements, bed exits).
 * Tap selects/deselects the nearest event on the touched track; horizontal drag scrubs and clears on release.
 */
@Composable
fun EventTimelineChart(
    tracks: List<TimelineTrack>,
    selection: TimelineSelection?,
    onSelectionChange: (TimelineSelection?) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    verticalGridFractions: List<Float> = emptyList(),
    trackHeight: Dp = 24.dp,
    trackSpacing: Dp = 10.dp,
    minEventWidth: Dp = 2.dp,
    hitSlopFraction: Float = 0.04f,
) {
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(selection) {
        if (selection != null) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val density = LocalDensity.current
    val trackStepPx = with(density) { (trackHeight + trackSpacing).toPx() }
    val latestTracks by rememberUpdatedState(tracks)
    val latestSelection by rememberUpdatedState(selection)
    val latestOnChange by rememberUpdatedState(onSelectionChange)
    val latestStep by rememberUpdatedState(trackStepPx)
    val height = trackHeight * tracks.size + trackSpacing * (tracks.size - 1).coerceAtLeast(0)

    fun hit(touch: ChartTouch): TimelineSelection? {
        val touched = (touch.y / latestStep).toInt().coerceIn(0, (latestTracks.size - 1).coerceAtLeast(0))
        return latestTracks.nearestEvent(touch.x, hitSlopFraction, onlyTrack = touched)
    }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .semantics { this.contentDescription = contentDescription }
            .chartGestures(
                onTap = { t ->
                    val h = hit(t)
                    latestOnChange(if (h == latestSelection) null else h)
                },
                onScrub = { t -> hit(t)?.let { if (it != latestSelection) latestOnChange(it) } },
                onScrubEnd = { latestOnChange(null) },
            ),
    ) {
        val th = trackHeight.toPx()
        val step = th + trackSpacing.toPx()
        val radius = CornerRadius(3.dp.toPx())

        tracks.indices.forEach { ti ->
            drawRoundRect(OrionColors.Track, Offset(0f, ti * step), Size(size.width, th), radius)
        }
        drawDashedGrid(ys = emptyList(), xFractions = verticalGridFractions)

        val minW = minEventWidth.toPx()
        val inset = 2.dp.toPx()
        tracks.forEachIndexed { ti, track ->
            val top = ti * step
            track.events.forEachIndexed { ei, e ->
                val selected = selection == TimelineSelection(ti, ei)
                val dimmed = selection != null && !selected
                val w = max((e.end - e.start) * size.width, minW * e.thickness.coerceAtLeast(1f))
                val grow = if (selected) 1.5.dp.toPx() else 0f
                val x = (e.start * size.width - grow).coerceIn(0f, (size.width - w).coerceAtLeast(0f))
                drawRoundRect(
                    color = track.color.copy(alpha = if (dimmed) 0.4f else 1f),
                    topLeft = Offset(x, top + inset - grow),
                    size = Size(w + grow * 2, th - inset * 2 + grow * 2),
                    cornerRadius = CornerRadius(min(w, 2.dp.toPx())),
                )
            }
        }
    }
}

/** Nearest event to [fraction] within [maxDistance], searching only [onlyTrack] when given. */
internal fun List<TimelineTrack>.nearestEvent(
    fraction: Float,
    maxDistance: Float,
    onlyTrack: Int? = null,
): TimelineSelection? {
    var best: TimelineSelection? = null
    var bestDistance = Float.MAX_VALUE
    forEachIndexed { ti, track ->
        if (onlyTrack != null && ti != onlyTrack) return@forEachIndexed
        track.events.forEachIndexed { ei, e ->
            val d = if (fraction in e.start..e.end) 0f else min(abs(fraction - e.start), abs(fraction - e.end))
            if (d < bestDistance) {
                bestDistance = d
                best = TimelineSelection(ti, ei)
            }
        }
    }
    return best.takeIf { bestDistance <= maxDistance }
}
