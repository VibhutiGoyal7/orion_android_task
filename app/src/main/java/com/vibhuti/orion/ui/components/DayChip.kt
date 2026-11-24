package com.vibhuti.orion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vibhuti.orion.ui.theme.OrionColors

enum class DayIndicator { Good, Poor, None }

/** One day in the date strip: weekday, day number and a status mark (dot or dash). */
@Composable
fun DayChip(
    weekday: String,
    dayOfMonth: String,
    indicator: DayIndicator,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 52.dp,
) {
    val shape = RoundedCornerShape(10.dp)
    val textColor = if (enabled) OrionColors.TextPrimary else OrionColors.TextTertiary
    Column(
        modifier
            .width(width)
            .clip(shape)
            .then(
                if (selected) {
                    Modifier
                        .background(OrionColors.SurfaceRaised, shape)
                        .border(1.dp, OrionColors.SurfaceRaisedBorder, shape)
                } else {
                    Modifier
                },
            )
            .selectable(selected = selected, enabled = enabled, role = Role.Tab, onClick = onClick)
            .semantics {
                stateDescription = when (indicator) {
                    DayIndicator.Good -> "Good night"
                    DayIndicator.Poor -> "Poor night"
                    DayIndicator.None -> "No data"
                }
            }
            .padding(top = 8.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            weekday.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) OrionColors.TextPrimary.copy(alpha = 0.78f) else OrionColors.TextTertiary,
        )
        Spacer(Modifier.height(2.dp))
        Text(dayOfMonth, style = MaterialTheme.typography.headlineSmall, color = textColor)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.height(6.dp), contentAlignment = Alignment.Center) {
            when (indicator) {
                DayIndicator.Good -> Box(Modifier.size(6.dp).background(OrionColors.Good, CircleShape))
                DayIndicator.Poor -> Box(Modifier.size(6.dp).background(OrionColors.Poor, CircleShape))
                DayIndicator.None -> Box(
                    Modifier
                        .size(width = 7.dp, height = 1.5.dp)
                        .background(OrionColors.TextTertiary),
                )
            }
        }
    }
}
