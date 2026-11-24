package com.vibhuti.orion.ui.components.chart

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/** Maps a data value to the colour it should be drawn with. */
fun interface ValueColorScale {
    fun colorFor(value: Float): Color
}

/**
 * Three bands split at [low] and [high]:
 * value < low -> [below], low..high (inclusive) -> [between], value > high -> [above].
 *
 * With a positive [blendWidth], the middle band fades at its edges instead of switching hard:
 * from [below] to [between] across `low..low+blendWidth`, and from [between] to [above] across
 * `high-blendWidth..high`. Values outside `low..high` stay exactly [below] / [above].
 */
@Immutable
data class ThresholdColorScale(
    val low: Float,
    val high: Float,
    val below: Color,
    val between: Color,
    val above: Color,
    val blendWidth: Float = 0f,
) : ValueColorScale {
    init {
        require(low <= high) { "low ($low) must be <= high ($high)" }
        require(blendWidth >= 0f) { "blendWidth must be >= 0" }
    }

    override fun colorFor(value: Float): Color = when {
        value < low -> below
        value > high -> above
        blendWidth <= 0f -> between
        value < low + blendWidth -> lerp(below, between, (value - low) / blendWidth)
        value > high - blendWidth -> lerp(between, above, (value - (high - blendWidth)) / blendWidth)
        else -> between
    }
}
