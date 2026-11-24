package com.vibhuti.orion.ui.components.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.vibhuti.orion.ui.theme.OrionColors
import kotlin.math.abs

/** A point on the chart: [x] is a 0..1 position along the width, [value] is in data units. */
@Immutable
data class LinePoint(val x: Float, val value: Float)

/**
 * Line chart whose colour follows the value: every point takes [colorScale]'s colour and the
 * stroke/fill interpolate between neighbouring points, so crossing a threshold blends smoothly.
 *
 * Stateless: selection is hoisted through [selectedIndex] / [onSelectedIndexChange].
 * Tap toggles a point, horizontal drag scrubs and clears on release.
 */
@Composable
fun BandedLineChart(
    points: List<LinePoint>,
    valueRange: ClosedFloatingPointRange<Float>,
    colorScale: ValueColorScale,
    selectedIndex: Int?,
    onSelectedIndexChange: (Int?) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    horizontalGridValues: List<Float> = emptyList(),
    verticalGridFractions: List<Float> = emptyList(),
    haloColor: Color = OrionColors.Surface,
) {
    // Draw-in animation runs once per data set; saveable so scrolling the chart off-screen and back
    // inside a lazy list doesn't replay it.
    var revealed by rememberSaveable(points) { mutableStateOf(false) }
    val reveal = remember(points) { Animatable(if (revealed) 1f else 0f) }
    LaunchedEffect(points) {
        reveal.animateTo(1f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
        revealed = true
    }

    val haptics = LocalHapticFeedback.current
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != null) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val latestPoints by rememberUpdatedState(points)
    val latestSelected by rememberUpdatedState(selectedIndex)
    val latestOnChange by rememberUpdatedState(onSelectedIndexChange)

    Canvas(
        modifier
            .semantics { this.contentDescription = contentDescription }
            .chartGestures(
                onTap = { t ->
                    val i = latestPoints.nearestIndexTo(t.x)
                    latestOnChange(if (i == latestSelected) null else i)
                },
                onScrub = { t -> latestPoints.nearestIndexTo(t.x)?.let { if (it != latestSelected) latestOnChange(it) } },
                onScrubEnd = { latestOnChange(null) },
            ),
    ) {
        val span = (valueRange.endInclusive - valueRange.start).takeIf { it > 0f } ?: 1f
        fun yOf(v: Float) = size.height * (1f - ((v - valueRange.start) / span).coerceIn(0f, 1f))

        drawDashedGrid(horizontalGridValues.map(::yOf), verticalGridFractions)
        if (points.size < 2) return@Canvas

        val offsets = points.map { Offset(it.x * size.width, yOf(it.value)) }
        val brush = Brush.horizontalGradient(
            colorStops = points.map { it.x.coerceIn(0f, 1f) to colorScale.colorFor(it.value) }.toTypedArray(),
            startX = 0f,
            endX = size.width,
        )
        val line = Path().apply {
            moveTo(offsets.first().x, offsets.first().y)
            for (i in 1 until offsets.size) lineTo(offsets[i].x, offsets[i].y)
        }
        val fill = Path().apply {
            addPath(line)
            lineTo(offsets.last().x, size.height)
            lineTo(offsets.first().x, size.height)
            close()
        }

        clipRect(right = size.width * reveal.value) {
            // Fill: band colours, faded to transparent towards the bottom (DstIn mask in its own layer).
            drawIntoCanvas { it.saveLayer(Rect(Offset.Zero, size), Paint()) }
            drawPath(fill, brush, alpha = 0.30f)
            drawRect(Brush.verticalGradient(listOf(Color.Black, Color.Transparent)), blendMode = BlendMode.DstIn)
            drawIntoCanvas { it.restore() }

            drawPath(line, brush, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        val sel = selectedIndex?.takeIf { it in offsets.indices } ?: return@Canvas
        val o = offsets[sel]
        val c = colorScale.colorFor(points[sel].value)
        drawLine(c.copy(alpha = 0.6f), Offset(o.x, 0f), Offset(o.x, size.height), strokeWidth = 1.dp.toPx())
        drawCircle(haloColor, radius = 6.dp.toPx(), center = o)
        drawCircle(c, radius = 4.dp.toPx(), center = o)
    }
}

/** Dashed horizontal lines at [ys] (px) and vertical lines at [xFractions] (0..1). */
internal fun DrawScope.drawDashedGrid(ys: List<Float>, xFractions: List<Float>, color: Color = OrionColors.Grid) {
    val stroke = 1.dp.toPx()
    val dash = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
    ys.forEach { raw ->
        val y = raw.coerceIn(stroke / 2, size.height - stroke / 2)
        drawLine(color, Offset(0f, y), Offset(size.width, y), stroke, pathEffect = dash)
    }
    xFractions.forEach { f ->
        val x = f * size.width
        drawLine(color, Offset(x, 0f), Offset(x, size.height), stroke, pathEffect = dash)
    }
}

internal fun List<LinePoint>.nearestIndexTo(fraction: Float): Int? =
    indices.minByOrNull { abs(this[it].x - fraction) }
