package com.xmo.music.ui.nowplaying

import android.content.Context
import android.net.Uri
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
    /*
     * Color at x == 0.
     */
    var settled by
        mutableStateOf(
            initial
        )
        internal set

    /*
     * Color belonging to the frozen visual-current cover.
     */
    var current by
        mutableStateOf(
            initial
        )
        internal set

    /*
     * Colors belonging to the exact same frozen neighbors used
     * by PlayerArtwork.
     */
    var previous by
        mutableStateOf(
            initial
        )
        internal set

    var next by
        mutableStateOf(
            initial
        )
        internal set
}

@Composable
internal fun rememberPlayerColors(
    context: Context,
    carousel: PlayerCarouselState
): PlayerColorState {
    val initial =
        Color(
            0xFF747984
        )

    val state =
        remember {
            PlayerColorState(
                initial =
                    initial
            )
        }

    val cache =
        remember {
            mutableStateMapOf<String, Color>()
        }

    suspend fun extract(
        uri: Uri?,
        fallback: Color
    ): Color {
        if (
            uri == null
        ) {
            return fallback
        }

        val key =
            uri.toString()

        cache[key]?.let {
            return it
        }

        val raw =
            Artwork.cached(
                uri
            )
                ?: Artwork.color(
                    context,
                    uri
                )

        val result =
            liftArtworkColor(
                raw
            )

        cache[key] =
            result

        return result
    }

    /*
     * =========================================================
     * FROZEN CURRENT COLOR
     * =========================================================
     */

    LaunchedEffect(
        carousel.visualSongId,
        carousel.visualCurrent
    ) {
        val result =
            extract(
                uri =
                    carousel.visualCurrent,
                fallback =
                    state.settled
            )

        state.current =
            result

        /*
         * At visual rest the frozen current cover is allowed to
         * define the resting background.
         */
        if (
            carousel.isResting
        ) {
            state.settled =
                result
        }
    }

    /*
     * =========================================================
     * FROZEN NEIGHBORS
     * =========================================================
     */

    LaunchedEffect(
        carousel.visualPrevious
    ) {
        state.previous =
            extract(
                uri =
                    carousel.visualPrevious,
                fallback =
                    state.settled
            )
    }

    LaunchedEffect(
        carousel.visualNext
    ) {
        state.next =
            extract(
                uri =
                    carousel.visualNext,
                fallback =
                    state.settled
            )
    }

    /*
     * =========================================================
     * EDGE COMMIT
     * =========================================================
     *
     * The destination becomes settled while its cover is still
     * centered at +/- pageWidth.
     *
     * PlayerArtwork then adopts the confirmed window and snaps x
     * back to zero. Since settled is already destination-colored,
     * reset cannot reveal the old song color.
     *
     * There is no competing independent resting-color animation.
     */

    LaunchedEffect(
        carousel.x.value,
        carousel.width
    ) {
        val width =
            carousel.width
                .coerceAtLeast(
                    1f
                )

        val fraction =
            (
                carousel.x.value /
                    width
                )
                .coerceIn(
                    -1f,
                    1f
                )

        when {
            fraction <=
                -.995f -> {
                state.settled =
                    state.next
            }

            fraction >=
                .995f -> {
                state.settled =
                    state.previous
            }
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
        abs(
            fraction
        ) <
        .001f
    ) {
        return colors.settled
    }

    val destination =
        if (
            fraction <
            0f
        ) {
            colors.next
        } else {
            colors.previous
        }

    /*
     * While a manual swipe is returning to x=0 this naturally
     * interpolates back to settled. While completing a real
     * transaction it reaches destination continuously.
     */
    return mixColor(
        from =
            colors.settled,
        to =
            destination,
        fraction =
            smoothPlayerFraction(
                abs(
                    fraction
                )
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
            displayColor
                .luminance() >
            .66f
        ) {
            Color(
                0xFF15161A
            )
        } else {
            Color.White
        }

    val controls =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color(
                    0xFF151519
                )

            XmoTheme.Dark,
            XmoTheme.Amoled ->
                Color.White
        }

    val softButton =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha =
                        .34f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha =
                        .095f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha =
                        .10f
                )
        }

    val panel =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha =
                        .54f
                )

            XmoTheme.Dark ->
                Color(
                    0xFF17191E
                ).copy(
                    alpha =
                        .52f
                )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha =
                        .60f
                )
        }

    val border =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha =
                        .55f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha =
                        .15f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha =
                        .16f
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
    val peak =
        max(
            color.red,
            max(
                color.green,
                color.blue
            )
        )

    if (
        peak <=
        .002f
    ) {
        return Color(
            0xFF747984
        )
    }

    val targetPeak =
        when {
            peak <
                .16f ->
                .50f

            peak <
                .26f ->
                .47f

            peak <
                .38f ->
                .44f

            else ->
                peak
        }

    val multiplier =
        if (
            targetPeak >
            peak
        ) {
            targetPeak /
                peak
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

    val lift =
        if (
            peak <
            .38f
        ) {
            .055f
        } else {
            .018f
        }

    red +=
        (
            1f -
                red
            ) *
            lift

    green +=
        (
            1f -
                green
            ) *
            lift

    blue +=
        (
            1f -
                blue
            ) *
            lift

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
        alpha =
            1f
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

        alpha =
            1f
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
