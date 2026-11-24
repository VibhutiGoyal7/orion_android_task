package com.vibhuti.orion.ui.theme

import androidx.compose.ui.graphics.Color

/** Palette sampled from the "Insights - Tracking Complete/Single" Figma frame. */
object OrionColors {
    val Background = Color(0xFF04080D)
    val Surface = Color(0xFF0C1218)
    val SurfaceBorder = Color(0xFF19212A)
    val SurfaceRaised = Color(0xFF1A222C)
    val SurfaceRaisedBorder = Color(0xFF2A3440)
    val Track = Color(0xFF1C2530)
    val Grid = Color(0xFF2A333E)
    val Divider = Color(0xFF2A323C)

    val TextPrimary = Color(0xFFF2F4F7)
    val TextSecondary = Color(0xFF8D96A3)
    val TextTertiary = Color(0xFF5F6874)

    val Good = Color(0xFF5DC98E)
    val Poor = Color(0xFFF07F7F)

    // Temperature bands: below 26 = light blue, 26..28 = purple, above 28 = red.
    val TempCool = Color(0xFF9DBBFF)
    val TempTarget = Color(0xFF9A8CF0)
    val TempWarm = Color(0xFFF0737D)

    val Movement = Color(0xFFC3D5FF)   // top track in the frame
    val LeftBed = Color(0xFF8EA6FF)    // bottom track in the frame
    val SleepAge = Color(0xFF7FA7F8)

    val Hrv = Color(0xFF9AA6B6)
    val HeartRate = Color(0xFFE88E88)
    val BreathRate = Color(0xFF9FC2FF)
}
