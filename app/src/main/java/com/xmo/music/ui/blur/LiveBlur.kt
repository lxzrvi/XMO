package com.xmo.music.ui.blur

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xmo.music.XmoTheme
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeSampling
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur

@Composable
fun rememberLiveBlurState(): HazeState =
    rememberHazeState()

fun Modifier.liveBlurSource(
    state: HazeState
): Modifier =
    hazeSource(state)

fun Modifier.liveBlur(
    state: HazeState,
    theme: XmoTheme
): Modifier =
    hazeBlur(
        input = HazeInput.Sources(state),
        style = HazeBlurStyle {
            blurRadius(20.dp)
        },
        sampling = HazeSampling.Adaptive
    )
        .background(
            glassTint(theme)
        )

fun glassTint(
    theme: XmoTheme
): Color =
    when (theme) {
        XmoTheme.Light ->
            Color.White.copy(
                alpha = .22f
            )

        XmoTheme.Dark ->
            Color.Black.copy(
                alpha = .18f
            )

        XmoTheme.Amoled ->
            Color.Black.copy(
                alpha = .34f
            )
    }

fun glassBorder(
    theme: XmoTheme
): Color =
    when (theme) {
        XmoTheme.Light ->
            Color.Black.copy(.10f)

        XmoTheme.Dark ->
            Color.White.copy(.13f)

        XmoTheme.Amoled ->
            Color.White.copy(.18f)
    }

fun glassHighlight(
    theme: XmoTheme
): Color =
    when (theme) {
        XmoTheme.Light ->
            Color.White.copy(.55f)

        XmoTheme.Dark ->
            Color.White.copy(.15f)

        XmoTheme.Amoled ->
            Color.White.copy(.12f)
    }
