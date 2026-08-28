package com.xmo.music.ui.blur

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xmo.music.XmoTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

enum class XmoBlurStyle {
    Light,
    Dark,
    Amoled
}

fun XmoTheme.toBlurStyle():
    XmoBlurStyle {

    return when (this) {
        XmoTheme.Light ->
            XmoBlurStyle.Light

        XmoTheme.Dark ->
            XmoBlurStyle.Dark

        XmoTheme.Amoled ->
            XmoBlurStyle.Amoled
    }
}

@Composable
fun rememberLiveBlurState():
    HazeState {

    /*
     * Exactly ONE state is created at App level.
     */
    return rememberHazeState()
}

fun Modifier.liveBlurSource(
    state: HazeState
): Modifier {
    return this.hazeSource(
        state
    )
}
