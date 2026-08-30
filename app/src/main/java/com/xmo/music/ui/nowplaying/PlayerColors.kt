package com.xmo.music.ui.nowplaying

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.xmo.music.XmoTheme
import com.xmo.music.ui.Artwork
import kotlin.math.abs
import kotlin.math.max

@Stable
internal class PlayerColorState(
    initial: Color
) {
    var settled by
        mutableStateOf(initial)
        internal set

    var current by
        mutableStateOf(initial)
        internal set

    var previous by
        mutableStateOf(initial)
        internal set

    var next by
        mutableStateOf(initial)
        internal set
}

@Composable
internal fun rememberPlayerColors(
    context: Context,
    carousel: PlayerCarouselState
): PlayerColorState {
    val initial =
        Color(0xFF747984)

    val state =
        remember {
            PlayerColorState(
                initial = initial
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
        if (uri == null) {
            return fallback
        }

        val key =
            uri.toString()

        cache[key]?.let {
            return it
        }

        val raw =
            Artwork.cached(uri)
                ?: Artwork.color(
                    context,
                    uri
                )

        val result =
            liftArtworkColor(raw)

        cache[key] = result

        return result
    }

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

        if (carousel.isResting) {
            state.settled =
                result
        }
    }

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
     * Destination color is committed while the destination cover
     * is still visually centered. This keeps the x reset from
     * flashing the old song's background.
     */
    LaunchedEffect(
        carousel.x.value,
        carousel.width
    ) {
        val width =
            carousel.width
                .coerceAtLeast(1f)

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
            fraction <= -.995f -> {
                state.settled =
                    state.next
            }

            fraction >= .995f -> {
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

    if (
        abs(fraction) <
        .001f
    ) {
        return colors.settled
    }

    val destination =
        if (fraction < 0f) {
            colors.next
        } else {
            colors.previous
        }

    return mixColor(
        from = colors.settled,
        to = destination,
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
    /*
     * Intentionally theme-owned.
     *
     * Artwork changes must never flip header/control foreground
     * from white to black or vice versa.
     */
    val foreground =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF15161A)

            XmoTheme.Dark,
            XmoTheme.Amoled ->
                Color.White
        }

    /*
     * Panel remains translucent so the distributed artwork color
     * remains visible underneath it.
     */
    val panel =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .58f
                )

            XmoTheme.Dark ->
                Color(0xFF111318)
                    .copy(
                        alpha = .60f
                    )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .68f
                )
        }

    /*
     * Action capsules need more separation than the panel itself.
     * These remain neutral and do not inherit the artwork color.
     */
    val softButton =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFF5F6F8)
                    .copy(
                        alpha = .82f
                    )

            XmoTheme.Dark ->
                Color(0xFF303238)
                    .copy(
                        alpha = .78f
                    )

            XmoTheme.Amoled ->
                Color(0xFF242529)
                    .copy(
                        alpha = .86f
                    )
        }

    /*
     * Play/Pause stays neutral but has slightly stronger visual
     * presence than the smaller action capsules.
     */
    val playBackground =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFE9EAED)
                    .copy(
                        alpha = .92f
                    )

            XmoTheme.Dark ->
                Color(0xFF35373D)
                    .copy(
                        alpha = .90f
                    )

            XmoTheme.Amoled ->
                Color(0xFF292A2F)
                    .copy(
                        alpha = .94f
                    )
        }

    val border =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF15161A)
                    .copy(
                        alpha = .13f
                    )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .18f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .20f
                )
        }

    return PlayerThemeColors(
        overlayText =
            foreground,
        controls =
            foreground,
        playBackground =
            playBackground,
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

    if (peak <= .002f) {
        return Color(
            0xFF747984
        )
    }

    val targetPeak =
        when {
            peak < .16f ->
                .50f

            peak < .26f ->
                .47f

            peak < .38f ->
                .44f

            else ->
                peak
        }

    val multiplier =
        if (targetPeak > peak) {
            targetPeak / peak
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
        if (peak < .38f) {
            .055f
        } else {
            .018f
        }

    red +=
        (1f - red) *
            lift

    green +=
        (1f - green) *
            lift

    blue +=
        (1f - blue) *
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
                (to.red - from.red) *
                value,

        green =
            from.green +
                (to.green - from.green) *
                value,

        blue =
            from.blue +
                (to.blue - from.blue) *
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
