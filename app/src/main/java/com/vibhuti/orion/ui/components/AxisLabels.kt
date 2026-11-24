package com.vibhuti.orion.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import com.vibhuti.orion.ui.theme.OrionColors
import kotlin.math.roundToInt

/** A label centred on [fraction] (0..1) of the available width. */
@Immutable
data class AxisLabel(val fraction: Float, val text: String, val emphasized: Boolean = false)

/**
 * Labels positioned by fraction under a chart. Each label is centred on its fraction and clamped
 * so the first/last ones stay inside the bounds. On narrow widths, non-emphasized labels are nudged
 * to avoid overlapping a neighbour, and hidden if they still can't fit. Expects [labels] sorted by fraction.
 */
@Composable
fun AxisLabels(labels: List<AxisLabel>, modifier: Modifier = Modifier) {
    Layout(
        modifier = modifier.fillMaxWidth(),
        content = {
            labels.forEach { label ->
                Text(
                    text = label.text,
                    style = if (label.emphasized) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                    color = if (label.emphasized) OrionColors.TextPrimary else OrionColors.TextTertiary,
                    maxLines = 1,
                )
            }
        },
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(loose) }
        val width = constraints.maxWidth
        val height = placeables.maxOfOrNull { it.height } ?: 0
        val gap = 6.dp.roundToPx()

        val xs = IntArray(placeables.size) { i ->
            val p = placeables[i]
            (labels[i].fraction * width - p.width / 2f).roundToInt().coerceIn(0, (width - p.width).coerceAtLeast(0))
        }
        val visible = BooleanArray(placeables.size) { true }
        var prevEnd = Int.MIN_VALUE
        for (i in placeables.indices) {
            val nextStart = (i + 1 until placeables.size).firstOrNull { labels[it].emphasized }?.let { xs[it] } ?: Int.MAX_VALUE
            if (!labels[i].emphasized && prevEnd != Int.MIN_VALUE && xs[i] < prevEnd + gap) xs[i] = prevEnd + gap
            if (!labels[i].emphasized && xs[i] + placeables[i].width + gap > nextStart) visible[i] = false
            if (visible[i]) prevEnd = xs[i] + placeables[i].width
        }

        layout(width, height) {
            placeables.forEachIndexed { i, p -> if (visible[i]) p.placeRelative(xs[i], 0) }
        }
    }
}
