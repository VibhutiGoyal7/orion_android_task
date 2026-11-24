package com.vibhuti.orion

import androidx.compose.ui.graphics.Color
import com.vibhuti.orion.ui.components.chart.ThresholdColorScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThresholdColorScaleTest {
    private val blue = Color(0xFF0000FF)
    private val purple = Color(0xFF800080)
    private val red = Color(0xFFFF0000)
    private val scale = ThresholdColorScale(low = 26f, high = 28f, below = blue, between = purple, above = red)

    @Test fun belowLowIsBlue() {
        assertEquals(blue, scale.colorFor(24f))
        assertEquals(blue, scale.colorFor(25.99f))
    }

    @Test fun boundariesAndMiddleArePurple() {
        assertEquals(purple, scale.colorFor(26f))
        assertEquals(purple, scale.colorFor(27f))
        assertEquals(purple, scale.colorFor(28f))
    }

    @Test fun aboveHighIsRed() {
        assertEquals(red, scale.colorFor(28.01f))
        assertEquals(red, scale.colorFor(30f))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invertedThresholdsRejected() {
        ThresholdColorScale(low = 28f, high = 26f, below = blue, between = purple, above = red)
    }
}

class BlendedThresholdColorScaleTest {
    private val blue = Color(0xFF0000FF)
    private val purple = Color(0xFF800080)
    private val red = Color(0xFFFF0000)
    private val scale = ThresholdColorScale(26f, 28f, blue, purple, red, blendWidth = 0.5f)

    @Test fun outsideBandsStayPure() {
        assertEquals(blue, scale.colorFor(25.99f))
        assertEquals(blue, scale.colorFor(24f))
        assertEquals(red, scale.colorFor(28.01f))
        assertEquals(red, scale.colorFor(30f))
    }

    @Test fun middleOfBandIsPurple() {
        assertEquals(purple, scale.colorFor(26.5f))
        assertEquals(purple, scale.colorFor(27f))
        assertEquals(purple, scale.colorFor(27.5f))
    }

    @Test fun edgesBlendTowardsNeighbours() {
        val nearLow = scale.colorFor(26.1f)
        assertTrue(nearLow.blue > purple.blue && nearLow.blue < blue.blue)
        assertTrue(nearLow.red < purple.red)
        val nearHigh = scale.colorFor(27.9f)
        assertTrue(nearHigh.red > purple.red && nearHigh.red < red.red)
        assertTrue(nearHigh.blue < purple.blue)
    }
}
