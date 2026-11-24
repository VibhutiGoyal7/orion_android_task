package com.vibhuti.orion.ui.insights.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vibhuti.orion.data.repository.Vital
import com.vibhuti.orion.data.repository.VitalType
import com.vibhuti.orion.ui.components.InsightCard
import com.vibhuti.orion.ui.components.VitalRow
import com.vibhuti.orion.ui.theme.OrionColors

@Composable
fun SleepVitalsSection(vitals: List<Vital>, modifier: Modifier = Modifier) {
    InsightCard(modifier = modifier, title = "Sleep Vitals") {
        vitals.forEachIndexed { index, vital ->
            if (index > 0) Spacer(Modifier.height(14.dp))
            VitalRow(
                name = vital.type.displayName,
                detail = "Range ${vital.normalRange.first} - ${vital.normalRange.last}",
                value = "${vital.value} ${vital.type.unit}",
                accent = vital.type.accent,
            )
        }
    }
}

private val VitalType.displayName: String
    get() = when (this) {
        VitalType.HRV -> "HRV"
        VitalType.HEART_RATE -> "Heart Rate"
        VitalType.BREATH_RATE -> "Breath Rate"
    }

private val VitalType.unit: String
    get() = when (this) {
        VitalType.HRV -> "ms"
        VitalType.HEART_RATE -> "bpm"
        VitalType.BREATH_RATE -> "brpm"
    }

private val VitalType.accent: Color
    get() = when (this) {
        VitalType.HRV -> OrionColors.Hrv
        VitalType.HEART_RATE -> OrionColors.HeartRate
        VitalType.BREATH_RATE -> OrionColors.BreathRate
    }
