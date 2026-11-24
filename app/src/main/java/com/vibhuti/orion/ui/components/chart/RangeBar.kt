package com.vibhuti.orion.ui.components.chart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vibhuti.orion.ui.theme.OrionColors

/**
 * A track spanning [scale] with a highlighted sub-range and an optional dashed [marker].
 * The highlight animates when the range changes.
 */
@Composable
fun RangeBar(
    scale: ClosedFloatingPointRange<Float>,
    highlighted: ClosedFloatingPointRange<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    marker: Float? = null,
    contentDescription: String? = null,
    barHeight: Dp = 18.dp,
    markerOverhang: Dp = 4.dp,
) {
    val span = (scale.endInclusive - scale.start).takeIf { it > 0f } ?: 1f
    fun frac(v: Float) = ((v - scale.start) / span).coerceIn(0f, 1f)
    val start by animateFloatAsState(frac(highlighted.start), tween(600), label = "rangeStart")
    val end by animateFloatAsState(frac(highlighted.endInclusive), tween(600), label = "rangeEnd")

    Canvas(
        modifier
            .fillMaxWidth()
            .height(barHeight + markerOverhang * 2)
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
    ) {
        val top = markerOverhang.toPx()
        val h = barHeight.toPx()
        val radius = CornerRadius(6.dp.toPx())
        drawRoundRect(OrionColors.Track, Offset(0f, top), Size(size.width, h), radius)
        val x0 = start * size.width
        val w = ((end - start) * size.width).coerceAtLeast(h / 2)
        drawRoundRect(color, Offset(x0, top), Size(w, h), radius)

        if (marker != null) {
            val x = frac(marker) * size.width
            drawLine(
                color = OrionColors.TextSecondary,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx())),
            )
        }
    }
}
