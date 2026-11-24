package com.vibhuti.orion.ui.components.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vibhuti.orion.ui.theme.OrionColors
import kotlin.math.roundToInt

/**
 * Small value pill shown only while a chart point is selected. Horizontally centred on [fraction]
 * and clamped to the bounds. Meant to be overlaid on the chart so it never shifts the layout.
 *
 * With [anchorY] (px from the top of this layout) the pill floats [anchorGap] above that point,
 * clamped inside the layout's height, so it tracks the selected point without covering text outside
 * the chart. Give the modifier a fixed size (e.g. `matchParentSize()`) when using it.
 */
@Composable
fun ChartTooltip(
    text: String,
    fraction: Float,
    accent: Color,
    modifier: Modifier = Modifier,
    anchorY: Float? = null,
    anchorGap: Dp = 10.dp,
) {
    Layout(
        modifier = modifier.fillMaxWidth(),
        content = {
            Row(
                Modifier
                    .background(OrionColors.SurfaceRaised, RoundedCornerShape(8.dp))
                    .border(1.dp, OrionColors.SurfaceRaisedBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(6.dp).background(accent, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(text, style = MaterialTheme.typography.labelMedium, color = OrionColors.TextPrimary, maxLines = 1)
            }
        },
    ) { measurables, constraints ->
        val p = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
        val width = constraints.maxWidth
        val height = if (anchorY != null && constraints.hasBoundedHeight) constraints.maxHeight else p.height
        layout(width, height) {
            val x = (fraction * width - p.width / 2f).roundToInt().coerceIn(0, (width - p.width).coerceAtLeast(0))
            val y = if (anchorY == null) 0 else {
                (anchorY - anchorGap.toPx() - p.height).roundToInt().coerceIn(0, (height - p.height).coerceAtLeast(0))
            }
            p.placeRelative(x, y)
        }
    }
}
