package com.vibhuti.orion.ui.insights.sections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.vibhuti.orion.data.repository.SleepSession
import com.vibhuti.orion.data.repository.TemperatureSummary
import com.vibhuti.orion.ui.components.AxisLabels
import com.vibhuti.orion.ui.components.InsightCard
import com.vibhuti.orion.ui.components.chart.BandedLineChart
import com.vibhuti.orion.ui.components.chart.ChartTooltip
import com.vibhuti.orion.ui.components.chart.LinePoint
import com.vibhuti.orion.ui.components.chart.ThresholdColorScale
import com.vibhuti.orion.ui.insights.InsightsFormat
import com.vibhuti.orion.ui.theme.OrionColors

/**
 * Chart range and colour bands: < 26 °C light blue, 26..28 °C purple, > 28 °C red.
 * The purple band blends in from blue over 26..26.5 and out to red over 27.5..28, so slopes
 * grade blue -> purple -> red instead of snapping (as in the design).
 */
val TemperatureRange = 24f..30f
private val ChartHeight = 116.dp
val TemperatureColorScale = ThresholdColorScale(
    low = 26f,
    high = 28f,
    below = OrionColors.TempCool,
    between = OrionColors.TempTarget,
    above = OrionColors.TempWarm,
    blendWidth = 0.5f,
)

private fun temperatureDescription(t: TemperatureSummary): String {
    val values = t.samples.map { it.celsius }
    if (values.isEmpty()) return "Temperature chart, no data"
    return "Temperature chart. ${t.headline}. Ranged from ${InsightsFormat.celsius(values.min())} " +
        "to ${InsightsFormat.celsius(values.max())}. Tap or drag to inspect values."
}

@Composable
fun TemperatureSection(temperature: TemperatureSummary, session: SleepSession, modifier: Modifier = Modifier) {
    val samples = temperature.samples
    val points = remember(samples, session) { samples.map { LinePoint(session.fractionOf(it.time), it.celsius) } }
    val gridFractions = remember(session) { InsightsFormat.gridTimes(session).map(session::fractionOf) }
    val axis = remember(session) { InsightsFormat.axisLabels(session) }
    var selected by remember(temperature) { mutableStateOf<Int?>(null) }

    InsightCard(modifier = modifier, title = "Temperature") {
        Text(temperature.headline, style = MaterialTheme.typography.titleMedium, color = OrionColors.TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(temperature.detail, style = MaterialTheme.typography.bodyMedium, color = OrionColors.TextSecondary)
        Spacer(Modifier.height(18.dp))
        Box {
            BandedLineChart(
                points = points,
                valueRange = TemperatureRange,
                colorScale = TemperatureColorScale,
                selectedIndex = selected,
                onSelectedIndexChange = { selected = it },
                contentDescription = remember(temperature) { temperatureDescription(temperature) },
                horizontalGridValues = listOf(26f, 28f, 30f),
                verticalGridFractions = gridFractions,
                modifier = Modifier.fillMaxWidth().height(ChartHeight),
            )
            selected?.let { i ->
                val sample = samples[i]
                val span = TemperatureRange.endInclusive - TemperatureRange.start
                val yFraction = 1f - ((sample.celsius - TemperatureRange.start) / span).coerceIn(0f, 1f)
                ChartTooltip(
                    text = "${InsightsFormat.celsius(sample.celsius)} · ${InsightsFormat.clock(sample.time)}",
                    fraction = points[i].x,
                    accent = TemperatureColorScale.colorFor(sample.celsius),
                    anchorY = with(LocalDensity.current) { ChartHeight.toPx() } * yFraction,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        AxisLabels(axis)
    }
}
