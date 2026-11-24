package com.vibhuti.orion.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vibhuti.orion.ui.theme.OrionColors

/** Big score, a divider, its verbal rating, and a caption (the date). Score animates on change. */
@Composable
fun ScoreHeader(score: Int, rating: String, caption: String, modifier: Modifier = Modifier) {
    Column(modifier.semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedContent(
                targetState = score,
                transitionSpec = {
                    val up = targetState > initialState
                    (slideInVertically { if (up) it / 2 else -it / 2 } + fadeIn()) togetherWith
                        (slideOutVertically { if (up) -it / 2 else it / 2 } + fadeOut())
                },
                label = "score",
            ) { value ->
                Text("$value", style = MaterialTheme.typography.displayLarge, color = OrionColors.TextPrimary)
            }
            Spacer(Modifier.width(14.dp))
            Box(Modifier.width(1.dp).height(26.dp).background(OrionColors.Divider))
            Spacer(Modifier.width(14.dp))
            Text(rating, style = MaterialTheme.typography.headlineSmall, color = OrionColors.TextPrimary)
        }
        Spacer(Modifier.height(4.dp))
        Text(caption, style = MaterialTheme.typography.titleMedium, color = OrionColors.TextTertiary)
    }
}
