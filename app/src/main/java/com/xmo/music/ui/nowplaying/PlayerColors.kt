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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.xmo.music.XmoTheme
import com.xmo.music.ui.Artwork
import kotlin.math.abs
import kotlin.math.max

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

    var committed by
        mutableStateOf(initial)
        internal set

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
        Color(0xFF6D7078)

    val state =
        remember {
            PlayerColorState(
                initial = initial
            )
        }

    val paletteCache =
        remember {
            mutableStateMapOf<String, Color>()
        }

    suspend fun extract(
        uri: Uri?
    ): Color {
        if (uri == null) {
            return state.committed
        }

        val key =
            uri.toString()

        paletteCache[key]?.let {
            return it
        }

        val raw =
            Artwork.cached(uri)
                ?: Artwork.color(
                    context,
                    uri
                )

        val lifted =
            liftArtworkColor(
                raw
            )

        paletteCache[key] =
            lifted

        return lifted
    }

    /*
     * Real Media3-current Palette result.
     *
     * This may update before the visual carousel completes, so it
     * must not directly replace committed/rendered.
     */
    LaunchedEffect(
        currentSongId,
        currentArtwork,
        fallbackArtwork
    ) {
        val requestedId =
            currentSongId

        val artwork =
            currentArtwork
                ?: fallbackArtwork

        val result =
            if (artwork != null) {
                extract(
                    artwork
                )
            } else {
                state.committed
            }

        if (
            requestedId ==
            currentSongId
        ) {
            state.current =
                result
        }
    }

    /*
     * Adjacent Palette window remains frozen for the same period
     * as PlayerArtwork's visual queue snapshot.
     */
    LaunchedEffect(
        previousArtwork,
        transactionActive
    ) {
        if (
            transactionActive
        ) {
            return@LaunchedEffect
        }

        state.previous =
            previousArtwork
                ?.let {
                    extract(it)
                }
                ?: state.committed
    }

    LaunchedEffect(
        nextArtwork,
        transactionActive
    ) {
        if (
            transactionActive
        ) {
            return@LaunchedEffect
        }

        state.next =
            nextArtwork
                ?.let {
                    extract(it)
                }
                ?: state.committed
    }

    /*
     * Commit only after the actual visual transaction returns to
     * rest. No arbitrary song-transition timer is used.
     */
    LaunchedEffect(
        currentSongId,
        transactionActive,
        state.current
    ) {
        if (
            transactionActive ||
            abs(coverX.value) >= 1f ||
            state.current == state.committed
        ) {
            return@LaunchedEffect
        }

        val expectedSongId =
            currentSongId

        val from =
            state.rendered

        val to =
            state.current

        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                tween<Float>(
                    durationMillis = 460
                )
        ) { value, _ ->

            /*
             * Kept compatible with the animation callback type in
             * the existing project.
             */
            val raw =
                value ?: 0f

            state.rendered =
                mixColor(
                    from = from,
                    to = to,
                    fraction =
                        smoothPlayerFraction(
                            raw
                        )
                )
        }

        if (
            !transactionActive &&
            expectedSongId ==
            currentSongId &&
            state.current == to
        ) {
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
        coverWidth
            .coerceAtLeast(
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
            smoothPlayerFraction(
                abs(fraction)
            )
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
    val overlayText =
        if (
            displayColor.luminance() >
            .67f
        ) {
            Color(0xFF151519)
        } else {
            Color.White
        }

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
                    alpha = .065f
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

    /*
     * Artwork remains visible below the lower controls panel.
     */
    val panel =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .52f
                )

            XmoTheme.Dark ->
                Color(0xFF202126)
                    .copy(
                        alpha = .52f
                    )

            XmoTheme.Amoled ->
                Color(0xFF08090B)
                    .copy(
                        alpha = .62f
                    )
        }

    val border =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .42f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .13f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .15f
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

internal fun liftArtworkColor(
    color: Color
): Color {
    val strongest =
        max(
            color.red,
            max(
                color.green,
                color.blue
            )
        )

    if (
        strongest <= .001f
    ) {
        return Color(
            0xFF777A84
        )
    }

    val targetPeak =
        when {
            strongest < .18f ->
                .48f

            strongest < .30f ->
                .44f

            strongest < .40f ->
                .42f

            else ->
                strongest
        }

    val multiplier =
        if (
            strongest <
            targetPeak
        ) {
            targetPeak /
                strongest
        } else {
            1f
        }

    var red =
        (
            color.red *
                multiplier
            )
            .coerceIn(
                0f,
                1f
            )

    var green =
        (
            color.green *
                multiplier
            )
            .coerceIn(
                0f,
                1f
            )

    var blue =
        (
            color.blue *
                multiplier
            )
            .coerceIn(
                0f,
                1f
            )

    val whiteLift =
        if (
            strongest < .42f
        ) {
            .07f
        } else {
            .025f
        }

    red +=
        (1f - red) *
            whiteLift

    green +=
        (1f - green) *
            whiteLift

    blue +=
        (1f - blue) *
            whiteLift

    return Color(
        red =
            red.coerceIn(
                0f,
                1f
            ),
        green =
            green.coerceIn(
                0f,
                1f
            ),
        blue =
            blue.coerceIn(
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

private fun smoothPlayerFraction(
    fraction: Float
): Float {
    val value =
        fraction.coerceIn(
            0f,
            1f
        )

    return value *
        value *
        (
            3f -
                2f *
                value
            )
}
