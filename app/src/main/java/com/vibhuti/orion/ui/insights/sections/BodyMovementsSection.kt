package com.vibhuti.orion.ui.insights.sections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibhuti.orion.data.repository.BodyMovements
import com.vibhuti.orion.data.repository.SleepSession
import com.vibhuti.orion.data.repository.TimedEvent
import com.vibhuti.orion.ui.components.AxisLabels
import com.vibhuti.orion.ui.components.ChartLegend
import com.vibhuti.orion.ui.components.InsightCard
import com.vibhuti.orion.ui.components.LegendEntry
import com.vibhuti.orion.ui.components.MetricStat
import com.vibhuti.orion.ui.components.chart.ChartTooltip
import com.vibhuti.orion.ui.components.chart.EventTimelineChart
import com.vibhuti.orion.ui.components.chart.TimelineEvent
import com.vibhuti.orion.ui.components.chart.TimelineSelection
import com.vibhuti.orion.ui.components.chart.TimelineTrack
import com.vibhuti.orion.ui.insights.InsightsFormat
import com.vibhuti.orion.ui.theme.OrionColors

private const val TRACK_MOVEMENTS = 0
private const val TRACK_LEFT_BED = 1

@Composable
fun BodyMovementsSection(movements: BodyMovements, session: SleepSession, modifier: Modifier = Modifier) {
    val tracks = remember(movements, session) {
        listOf(
            // Movements last seconds (sub-pixel at true scale): thicker ticks for longer ones, like the design.
            TimelineTrack(movements.movements.map { it.toTimeline(session, thickness = it.duration.seconds / 15f) }, OrionColors.Movement),
            TimelineTrack(movements.leftBed.map { it.toTimeline(session) }, OrionColors.LeftBed),
        )
    }
    val gridFractions = remember(session) { InsightsFormat.gridTimes(session).map(session::fractionOf) }
    val axis = remember(session) { InsightsFormat.axisLabels(session) }
    var selection by remember(movements) { mutableStateOf<TimelineSelection?>(null) }

    InsightCard(modifier = modifier, title = "Body Movements") {
        Row {
            MetricStat("Restless Time", InsightsFormat.minutesSeconds(movements.restlessTime), Modifier.weight(1f))
            MetricStat("Rate", InsightsFormat.rate(movements.ratePerHour), Modifier.weight(1f))
        }
        Spacer(Modifier.height(32.dp))
        Box {
            EventTimelineChart(
                tracks = tracks,
                selection = selection,
                onSelectionChange = { selection = it },
                contentDescription = remember(movements) {
                    "Body movements chart. ${movements.movements.size} movements, " +
                        "left bed ${movements.leftBed.size} times. Tap or drag to inspect events."
                },
                verticalGridFractions = gridFractions,
            )
            selection?.let { sel ->
                val track = tracks[sel.track]
                val event = track.events[sel.event]
                val source = if (sel.track == TRACK_MOVEMENTS) movements.movements[sel.event] else movements.leftBed[sel.event]
                ChartTooltip(
                    text = tooltipText(sel.track, source),
                    fraction = (event.start + event.end) / 2,
                    accent = track.color,
                    modifier = Modifier.align(Alignment.TopStart).offset(y = (-28).dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        AxisLabels(axis)
        Spacer(Modifier.height(14.dp))
        ChartLegend(
            listOf(
                LegendEntry("Left Bed", OrionColors.LeftBed),
                LegendEntry("Movements", OrionColors.Movement),
            ),
        )
    }
}

private fun TimedEvent.toTimeline(session: SleepSession, thickness: Float = 1f) =
    TimelineEvent(session.fractionOf(start), session.fractionOf(end), thickness.coerceIn(1f, 2.5f))

private fun tooltipText(track: Int, event: TimedEvent): String = when (track) {
    TRACK_LEFT_BED ->
        "Left bed · ${InsightsFormat.clock(event.start)} · ${InsightsFormat.shortDuration(event.duration)}"
    else -> "Movement · ${InsightsFormat.clock(event.start)} · ${InsightsFormat.shortDuration(event.duration)}"
}
