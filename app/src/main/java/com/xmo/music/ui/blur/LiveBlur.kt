package com.xmo.music.ui.blur

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xmo.music.XmoTheme
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/*
 * =============================================================
 * SHARED HAZE STATE
 * =============================================================
 */

@Composable
fun rememberLiveBlurState():
    HazeState =
    rememberHazeState()

fun Modifier.liveBlurSource(
    state: HazeState
): Modifier =
    hazeSource(
        state
    )

/*
 * =============================================================
 * PRIMARY XMO GLASS
 *
 * NavBar, Home header, category dock, player controls and
 * MiniPlayer all use this same material language.
 * =============================================================
 */

fun Modifier.liveBlur(
    state: HazeState,
    theme: XmoTheme
): Modifier =
    this
        .hazeBlur(
            input =
                HazeInput.Sources(
                    state
                ),

            style =
                HazeBlurStyle {
                    blurRadius(
                        20.dp
                    )
                }
        )
        .background(
            glassTint(
                theme
            )
        )

/*
 * Slightly stronger material for surfaces placed over busy
 * artwork, such as Now Playing controls.
 */
fun Modifier.liveBlurStrong(
    state: HazeState,
    theme: XmoTheme
): Modifier =
    this
        .hazeBlur(
            input =
                HazeInput.Sources(
                    state
                ),

            style =
                HazeBlurStyle {
                    blurRadius(
                        26.dp
                    )
                }
        )
        .background(
            strongGlassTint(
                theme
            )
        )

/*
 * Lighter material for large surfaces such as the sticky Home
 * header/category region. It keeps text readable without making
 * the whole surface look opaque.
 */
fun Modifier.liveBlurSoft(
    state: HazeState,
    theme: XmoTheme
): Modifier =
    this
        .hazeBlur(
            input =
                HazeInput.Sources(
                    state
                ),

            style =
                HazeBlurStyle {
                    blurRadius(
                        16.dp
                    )
                }
        )
        .background(
            softGlassTint(
                theme
            )
        )

/*
 * =============================================================
 * GLASS COLORS
 * =============================================================
 */

fun glassTint(
    theme: XmoTheme
): Color =
    when (
        theme
    ) {
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

fun softGlassTint(
    theme: XmoTheme
): Color =
    when (
        theme
    ) {
        XmoTheme.Light ->
            Color.White.copy(
                alpha = .17f
            )

        XmoTheme.Dark ->
            Color.Black.copy(
                alpha = .13f
            )

        XmoTheme.Amoled ->
            Color.Black.copy(
                alpha = .27f
            )
    }

fun strongGlassTint(
    theme: XmoTheme
): Color =
    when (
        theme
    ) {
        XmoTheme.Light ->
            Color.White.copy(
                alpha = .30f
            )

        XmoTheme.Dark ->
            Color.Black.copy(
                alpha = .27f
            )

        XmoTheme.Amoled ->
            Color.Black.copy(
                alpha = .43f
            )
    }

fun glassBorder(
    theme: XmoTheme
): Color =
    when (
        theme
    ) {
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

fun glassHighlight(
    theme: XmoTheme
): Color =
    when (
        theme
    ) {
        XmoTheme.Light ->
            Color.White.copy(
                alpha = .55f
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

/*
 * Secondary divider used inside connected glass pills.
 */
fun glassDivider(
    theme: XmoTheme
): Color =
    when (
        theme
    ) {
        XmoTheme.Light ->
            Color.Black.copy(
                alpha = .08f
            )

        XmoTheme.Dark ->
            Color.White.copy(
                alpha = .09f
            )

        XmoTheme.Amoled ->
            Color.White.copy(
                alpha = .12f
            )
    }
