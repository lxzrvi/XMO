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
    var current by
        mutableStateOf(initial)
        internal set

    var previous by
        mutableStateOf(initial)
        internal set

    var next by
        mutableStateOf(initial)
        internal set

    /*
     * Last visually completed song color.
     */
    var committed by
        mutableStateOf(initial)
        internal set

    /*
     * What the UI actually renders.
     *
     * This is intentionally independent from the latest Palette
     * extraction so metadata changes cannot instantly recolor the
     * header/background before the artwork transaction completes.
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
     * Palette state may update before the cover finishes moving.
     * Only "current" changes here — rendered/committed do not.
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
     * Finger/automatic carousel owns color while moving.
     */
    LaunchedEffect(
        coverX.value,
        state.previous,
        state.next,
        state.committed
    ) {
        val width =
            1f.coerceAtLeast(
                abs(coverX.upperBound)
                    .takeIf {
                        it.isFinite()
                    }
                    ?: 1f
            )

        /*
         * Width is not reliably represented by Animatable bounds,
         * so actual interpolation is performed by
         * playerDisplayColor(). This effect intentionally doesn't
         * commit anything while displaced.
         */
    }

    /*
     * A new Palette color becomes the visual baseline only when
     * the carousel transaction has actually completed.
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
                    tween(420)
            ) { fraction, _ ->
                state.rendered =
                    mixColor(
                        from = from,
                        to = to,
                        fraction = fraction
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

internal fun playerDisplayColor(
    colors: PlayerColorState,
    coverX: Float,
    coverWidth: Float
): Color {
    val width =
        coverWidth.coerceAtLeast(
            1f
        )

    val fraction =
        (
            coverX /
                width
            )
            .coerceIn(
                -1f,
                1f
            )

    if (
        abs(fraction) <
        .001f
    ) {
        return colors.rendered
    }

    val destination =
        if (fraction < 0f) {
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
     * Header readability follows the artwork color, but changes
     * only through displayColor, which is transaction-controlled.
     */
    val overlay =
        if (
            displayColor.luminance() >
            .64f
        ) {
            Color(0xFF151519)
        } else {
            Color.White
        }

    /*
     * Requested invariant:
     * Light -> black controls
     * Dark  -> white controls
     * AMOLED -> white controls
     */
    val controls =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF151519)

            XmoTheme.Dark,
            XmoTheme.Amoled ->
                Color.White
        }

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

    return PlayerThemeColors(
        overlayText = overlay,
        controls = controls,

        playBackground =
            softButton,

        panel =
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
            },

        border =
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
            },

        softButton =
            softButton
    )
}

internal fun liftArtworkColor(
    color: Color
): Color {
    /*
     * Raise very dark Palette results while retaining their hue.
     * Higher floor than before because the requested Now Playing
     * background should feel light/tinted rather than muddy.
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

internal fun mixColor(
    from: Color,
    to: Color,
    fraction: Float
): Color {
    val f =
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
                    ) * f,

        green =
            from.green +
                (
                    to.green -
                        from.green
                    ) * f,

        blue =
            from.blue +
                (
                    to.blue -
                        from.blue
                    ) * f,

        alpha = 1f
    )
}
