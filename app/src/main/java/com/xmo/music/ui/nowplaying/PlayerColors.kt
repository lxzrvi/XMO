package com.xmo.music.ui.nowplaying

import android.content.Context
import android.net.Uri
import androidx.compose.animation.core.Animatable
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
    current: Color,
    previous: Color,
    next: Color,
    committed: Color
) {
    var current by
        mutableStateOf(current)
        internal set

    var previous by
        mutableStateOf(previous)
        internal set

    var next by
        mutableStateOf(next)
        internal set

    var committed by
        mutableStateOf(committed)
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
    coverX: Animatable<Float, *>
): PlayerColorState {
    val initial =
        Color(0xFF52545A)

    val state =
        remember {
            PlayerColorState(
                current = initial,
                previous = initial,
                next = initial,
                committed = initial
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

    LaunchedEffect(
        currentArtwork,
        fallbackArtwork
    ) {
        val result =
            extract(
                currentArtwork
                    ?: fallbackArtwork
            )

        state.current =
            result

        if (
            abs(coverX.value) <
            1f
        ) {
            state.committed =
                result
        }
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

    LaunchedEffect(
        currentSongId,
        state.current
    ) {
        if (
            abs(coverX.value) <
            1f
        ) {
            state.committed =
                state.current
        }
    }

    return state
}

internal fun playerDisplayColor(
    colors: PlayerColorState,
    coverX: Float,
    coverWidth: Float
): Color {
    val fraction =
        (
            coverX /
                coverWidth
                    .coerceAtLeast(1f)
            )
            .coerceIn(
                -1f,
                1f
            )

    if (
        abs(fraction) <=
        .001f
    ) {
        return colors.current
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
    val border: Color
)

internal fun playerThemeColors(
    theme: XmoTheme,
    displayColor: Color
): PlayerThemeColors {
    val bright =
        displayColor.luminance() >
            .58f

    val overlay =
        if (bright) {
            Color(0xFF111214)
        } else {
            Color.White
        }

    val controls =
        if (
            theme ==
            XmoTheme.Light &&
            displayColor.luminance() >
            .72f
        ) {
            Color(0xFF171719)
        } else {
            Color.White
        }

    return PlayerThemeColors(
        overlayText = overlay,
        controls = controls,
        playBackground =
            when (theme) {
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
                        alpha = .15f
                    )
            },
        panel =
            when (theme) {
                XmoTheme.Light ->
                    Color.White.copy(
                        alpha = .40f
                    )

                XmoTheme.Dark ->
                    Color.Black.copy(
                        alpha = .27f
                    )

                XmoTheme.Amoled ->
                    Color.Black.copy(
                        alpha = .45f
                    )
            },
        border =
            when (theme) {
                XmoTheme.Light ->
                    Color.Black.copy(
                        alpha = .14f
                    )

                XmoTheme.Dark ->
                    Color.White.copy(
                        alpha = .16f
                    )

                XmoTheme.Amoled ->
                    Color.White.copy(
                        alpha = .22f
                    )
            }
    )
}
internal fun liftArtworkColor(
    color: Color
): Color {
    val minimum = .22f

    val maximum =
        maxOf(
            color.red,
            color.green,
            color.blue
        )

    if (maximum >= minimum) {
        return color
    }

    if (maximum <= .001f) {
        return Color(
            0xFF4A4D55
        )
    }

    val multiplier =
        minimum / maximum

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
