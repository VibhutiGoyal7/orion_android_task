package com.vibhuti.orion.ui.insights.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibhuti.orion.data.repository.SleepAge
import com.vibhuti.orion.ui.components.AxisLabel
import com.vibhuti.orion.ui.components.AxisLabels
import com.vibhuti.orion.ui.components.InsightCard
import com.vibhuti.orion.ui.components.chart.RangeBar
import com.vibhuti.orion.ui.theme.OrionColors

@Composable
fun SleepAgeSection(age: SleepAge, modifier: Modifier = Modifier) {
    val min = age.scaleMin.toFloat()
    val max = age.scaleMax.toFloat()
    val labels = remember(age) {
        listOf(
            AxisLabel(0f, "${age.scaleMin}"),
            AxisLabel((age.actualAge - min) / (max - min), "${age.actualAge}", emphasized = true),
            AxisLabel(1f, "${age.scaleMax}"),
        )
    }
    InsightCard(modifier = modifier, title = "Biological Sleep Age") {
        Text("${age.rangeStart}-${age.rangeEnd} yrs", style = MaterialTheme.typography.titleLarge, color = OrionColors.TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(age.summary, style = MaterialTheme.typography.bodyMedium, color = OrionColors.TextSecondary)
        Spacer(Modifier.height(16.dp))
        RangeBar(
            scale = min..max,
            highlighted = age.rangeStart.toFloat()..age.rangeEnd.toFloat(),
            color = OrionColors.SleepAge,
            marker = age.actualAge.toFloat(),
            contentDescription = "Biological sleep age ${age.rangeStart} to ${age.rangeEnd} years. " +
                "Your reported age, ${age.actualAge}, is marked on a scale from ${age.scaleMin} to ${age.scaleMax}.",
        )
        Spacer(Modifier.height(6.dp))
        AxisLabels(labels)
    }
}
