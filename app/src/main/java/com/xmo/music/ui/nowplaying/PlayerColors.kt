package com.xmo.music.ui.nowplaying

import android.content.Context
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.xmo.music.XmoTheme
import com.xmo.music.ui.Artwork
import kotlin.math.abs

@Stable
internal class PlayerColorState(
    initial: Color
) {
    /*
     * Latest real Palette result for the Media3 current song.
     *
     * This can update before the visual carousel finishes, so it
     * is intentionally separate from rendered/committed.
     */
    var current by
        mutableStateOf(initial)
        internal set

    /*
     * Real Palette colors for adjacent queue artwork.
     */
    var previous by
        mutableStateOf(initial)
        internal set

    var next by
        mutableStateOf(initial)
        internal set

    /*
     * Color of the last visually completed song.
     *
     * Carousel interpolation always starts from this color.
     */
    var committed by
        mutableStateOf(initial)
        internal set

    /*
     * Color displayed while the carousel is resting.
     *
     * It animates towards the new Palette result only after the
     * visual song transaction has completed.
     */
    var rendered by
        mutableStateOf(initial)
        internal set
}

@Composable
internal fun rememberPlayerColors(
    context: Context,
    currentArtwork: Uri?,
    fallbackArtwork: Uri?,
    previousArtwork: Uri?,
    nextArtwork: Uri?,
    currentSongId: Long?,
    coverX: Animatable<Float, *>,
    transactionActive: Boolean
): PlayerColorState {
    val initial =
        Color(0xFF62656D)

    val state =
        remember {
            PlayerColorState(
                initial = initial
            )
        }

    /*
     * =========================================================
     * LOCAL ARTWORK COLOR EXTRACTION
     * =========================================================
     */

    suspend fun extract(
        uri: Uri?
    ): Color {
        val raw =
            Artwork.cached(uri)
                ?: Artwork.color(
                    context,
                    uri
                )

        return liftArtworkColor(
            raw
        )
    }

    /*
     * =========================================================
     * CURRENT ARTWORK
     * =========================================================
     *
     * Only update the latest real Palette result here.
     *
     * Do NOT change rendered/committed immediately. Media3 can
     * publish the new current song before the artwork carousel
     * has visually completed.
     */

    LaunchedEffect(
        currentSongId,
        currentArtwork,
        fallbackArtwork
    ) {
        state.current =
            extract(
                currentArtwork
                    ?: fallbackArtwork
            )
    }

    /*
     * =========================================================
     * PREVIOUS ARTWORK
     * =========================================================
     */

    LaunchedEffect(
        previousArtwork
    ) {
        state.previous =
            previousArtwork
                ?.let {
                    extract(it)
                }
                ?: state.current
    }

    /*
     * =========================================================
     * NEXT ARTWORK
     * =========================================================
     */

    LaunchedEffect(
        nextArtwork
    ) {
        state.next =
            nextArtwork
                ?.let {
                    extract(it)
                }
                ?: state.current
    }

    /*
     * =========================================================
     * RESTING COLOR COMMIT
     * =========================================================
     *
     * The visual transaction, not currentSongId, controls when
     * the new song is allowed to become the resting UI color.
     *
     * This prevents:
     *
     * song 1 color
     * -> swipe toward song 2
     * -> Media3 reports song 2
     * -> old/new flash
     *
     * Instead:
     *
     * committed song 1 color
     * -> carousel interpolation
     * -> transaction completes
     * -> smooth commit to song 2
     */

    LaunchedEffect(
        transactionActive,
        coverX.value,
        state.current
    ) {
        if (
            !transactionActive &&
            abs(coverX.value) < 1f &&
            state.current !=
            state.committed
        ) {
            val from =
                state.rendered

            val to =
                state.current

            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec =
                    tween<Float>(
                        durationMillis = 420
                    )
            ) { value, _ ->

                /*
                 * Compose version in this project exposes this
                 * animation callback value as nullable.
                 */
                val fraction =
                    value ?: 0f

                state.rendered =
                    mixColor(
                        from = from,
                        to = to,
                        fraction =
                            fraction
                    )
            }

            state.committed =
                to

            state.rendered =
                to
        }
    }

    return state
}

/*
 * =============================================================
 * CURRENT DISPLAY COLOR
 * =============================================================
 */

internal fun playerDisplayColor(
    colors: PlayerColorState,
    coverX: Float,
    coverWidth: Float
): Color {
    val width =
        coverWidth
            .coerceAtLeast(1f)

    val fraction =
        (
            coverX /
                width
            )
            .coerceIn(
                -1f,
                1f
            )

    /*
     * At carousel rest use only the transaction-controlled
     * rendered color.
     */
    if (
        abs(fraction) <
        .001f
    ) {
        return colors.rendered
    }

    /*
     * X < 0:
     * user/automatic carousel is moving toward Next.
     *
     * X > 0:
     * moving toward Previous.
     */
    val destination =
        if (
            fraction < 0f
        ) {
            colors.next
        } else {
            colors.previous
        }

    return mixColor(
        from =
            colors.committed,
        to =
            destination,
        fraction =
            abs(fraction)
    )
}

/*
 * =============================================================
 * THEME COLORS
 * =============================================================
 */

internal data class PlayerThemeColors(
    val overlayText: Color,
    val controls: Color,
    val playBackground: Color,
    val panel: Color,
    val border: Color,
    val softButton: Color
)

internal fun playerThemeColors(
    theme: XmoTheme,
    displayColor: Color
): PlayerThemeColors {
    /*
     * Top source/header must remain readable against current
     * artwork-derived background.
     *
     * Because displayColor itself is transaction-controlled, the
     * foreground cannot switch prematurely when Media3 reports
     * the incoming song.
     */
    val overlayText =
        if (
            displayColor.luminance() >
            .64f
        ) {
            Color(0xFF151519)
        } else {
            Color.White
        }

    /*
     * Main lower controls use the requested theme invariant.
     */
    val controls =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF151519)

            XmoTheme.Dark,
            XmoTheme.Amoled ->
                Color.White
        }

    /*
     * Neutral soft player-button surfaces.
     *
     * Play/Pause is deliberately NOT XMO-red.
     */
    val softButton =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .075f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .105f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .12f
                )
        }

    val panel =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .68f
                )

            XmoTheme.Dark ->
                Color(0xFF292A30)
                    .copy(
                        alpha = .68f
                    )

            XmoTheme.Amoled ->
                Color(0xFF111114)
                    .copy(
                        alpha = .76f
                    )
        }

    val border =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .11f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .14f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .17f
                )
        }

    return PlayerThemeColors(
        overlayText =
            overlayText,
        controls =
            controls,
        playBackground =
            softButton,
        panel =
            panel,
        border =
            border,
        softButton =
            softButton
    )
}

/*
 * =============================================================
 * ARTWORK COLOR LIFT
 * =============================================================
 */

internal fun liftArtworkColor(
    color: Color
): Color {
    /*
     * Palette can return very dark dominant colors.
     *
     * Raise only their RGB intensity while preserving hue. The
     * actual background still remains theme-based in
     * PlayerBackground.kt.
     */
    val minimum =
        .34f

    val maximum =
        maxOf(
            color.red,
            color.green,
            color.blue
        )

    if (
        maximum >= minimum
    ) {
        return color
    }

    if (
        maximum <= .001f
    ) {
        return Color(
            0xFF747780
        )
    }

    val multiplier =
        minimum /
            maximum

    return Color(
        red =
            (
                color.red *
                    multiplier
                )
                .coerceIn(
                    0f,
                    1f
                ),

        green =
            (
                color.green *
                    multiplier
                )
                .coerceIn(
                    0f,
                    1f
                ),

        blue =
            (
                color.blue *
                    multiplier
                )
                .coerceIn(
                    0f,
                    1f
                ),

        alpha = 1f
    )
}

/*
 * =============================================================
 * COLOR INTERPOLATION
 * =============================================================
 */

internal fun mixColor(
    from: Color,
    to: Color,
    fraction: Float
): Color {
    val value =
        fraction.coerceIn(
            0f,
            1f
        )

    return Color(
        red =
            from.red +
                (
                    to.red -
                        from.red
                    ) *
                value,

        green =
            from.green +
                (
                    to.green -
                        from.green
                    ) *
                value,

        blue =
            from.blue +
                (
                    to.blue -
                        from.blue
                    ) *
                value,

        alpha = 1f
    )
}
