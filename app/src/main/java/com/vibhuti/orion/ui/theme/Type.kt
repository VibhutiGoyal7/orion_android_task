package com.vibhuti.orion.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.vibhuti.orion.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Aeonik (CoType trial files in `res/font/`). The trial is a subset: glyphs it lacks
 * (`/ : ? ° ·`) fall back to the system font per character. Swap in the licensed files before release.
 * No Medium cut ships in the trial; Medium requests resolve to the nearest available weight.
 */
val Aeonik: FontFamily = FontFamily(
    Font(R.font.aeonik_light, FontWeight.Light),
    Font(R.font.aeonik_regular, FontWeight.Normal),
    Font(R.font.aeonik_bold, FontWeight.Bold),
)

private fun style(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, tracking: Double = 0.0) =
    TextStyle(
        fontFamily = Aeonik,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = tracking.em,
    )

/** Sizes measured from the Figma frame (1x). */
val OrionTypography = Typography(
    displayLarge = style(52, 56, FontWeight.Light, -0.02),   // "72"
    headlineMedium = style(28, 34),                           // "Insights"
    headlineSmall = style(22, 28),                            // stat values, day numbers, "Excellent"
    titleLarge = style(20, 26),                               // "18-23 yrs"
    titleMedium = style(17, 22),                              // card headlines, vital values, date caption
    titleSmall = style(15, 20),                               // card titles, vital names
    bodyMedium = style(13, 18),                               // stat labels, descriptions
    bodySmall = style(12, 16),                                // ranges, subtitles
    labelMedium = style(11, 14, FontWeight.Medium),           // axis emphasised
    labelSmall = style(11, 14),                               // axis, legend, weekday
)
