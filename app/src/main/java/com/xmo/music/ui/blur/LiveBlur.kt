package com.xmo.music.ui.blur

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.xmo.music.XmoTheme
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeSampling
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

enum class BlurStyle {
    Light,
    Dark,
    Amoled
}

fun XmoTheme.toBlurStyle():
    BlurStyle {

    return when (this) {
        XmoTheme.Light ->
            BlurStyle.Light

        XmoTheme.Dark ->
            BlurStyle.Dark

        XmoTheme.Amoled ->
            BlurStyle.Amoled
    }
}

/*
 * ONE shared state.
 *
 * App.kt creates this once.
 */
@Composable
fun rememberLiveBlurState():
    HazeState {

    return rememberHazeState()
}

/*
 * Main application content becomes the shared source.
 */
fun Modifier.liveBlurSource(
    state: HazeState
): Modifier {

    return this.hazeSource(
        state
    )
}

/*
 * Cheap tint sits above actual Haze blur.
 */
fun glassTint(
    theme: XmoTheme
): Color {

    return when (theme) {
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
}

fun glassBorder(
    theme: XmoTheme
): Color {

    return when (theme) {
        XmoTheme.Light ->
            Color.Black.copy(
                alpha = .10f
            )

        XmoTheme.Dark ->
            Color.White.copy(
                alpha = .13f
            )

        XmoTheme.Amoled ->
            Color.White.copy(
                alpha = .18f
            )
    }
}

fun glassHighlight(
    theme: XmoTheme
): Color {

    return when (theme) {
        XmoTheme.Light ->
            Color.White.copy(
                alpha = .58f
            )

        XmoTheme.Dark ->
            Color.White.copy(
                alpha = .15f
            )

        XmoTheme.Amoled ->
            Color.White.copy(
                alpha = .12f
            )
    }
}
