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
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max

@Stable
internal class PlayerColorState(
    initial: Color
) {
    var current by mutableStateOf(initial)
        internal set

    var previous by mutableStateOf(initial)
        internal set

    var next by mutableStateOf(initial)
        internal set

    /*
     * Last fully accepted visual song color.
     */
    var committed by mutableStateOf(initial)
        internal set

    /*
     * Actual resting color drawn by the player.
     */
    var rendered by mutableStateOf(initial)
        internal set

    /*
     * Used to make the header foreground less eager to flip
     * around the luminance boundary.
     */
    var useDarkOverlayText by mutableStateOf(false)
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
    val initial = Color(0xFF6D7078)

    val state = remember {
        PlayerColorState(initial)
    }

    /*
     * Small in-memory color cache local to Now Playing.
     *
     * Artwork also has its own cache, but this prevents repeated
     * Compose-side extraction while queue windows are moving.
     */
    val paletteCache = remember {
        mutableStateMapOf<String, Color>()
    }

    suspend fun extract(uri: Uri?): Color {
        if (uri == null) {
            return state.committed
        }

        val key = uri.toString()

        paletteCache[key]?.let {
            return it
        }

        val raw =
            Artwork.cached(uri)
                ?: Artwork.color(
                    context = context,
                    uri = uri
                )

        val lifted = liftArtworkColor(raw)

        paletteCache[key] = lifted

        return lifted
    }

    /*
     * ---------------------------------------------------------
     * CURRENT
     * ---------------------------------------------------------
     *
     * This represents Media3 truth, NOT necessarily what is
     * already visually committed.
     */
    LaunchedEffect(
        currentSongId,
        currentArtwork,
        fallbackArtwork
    ) {
        val requestedSongId = currentSongId
        val requestedArtwork =
            currentArtwork ?: fallbackArtwork

        val extracted =
            if (requestedArtwork != null) {
                extract(requestedArtwork)
            } else {
                state.committed
            }

        /*
         * LaunchedEffect cancellation normally protects us, but
         * keeping the request identity explicit makes the visual
         * contract clear.
         */
        if (requestedSongId == currentSongId) {
            state.current = extracted
        }
    }

    /*
     * ---------------------------------------------------------
     * PREVIOUS
     * ---------------------------------------------------------
     *
     * Never fallback to state.current here. current may already
     * belong to the newly reported Media3 item.
     */
    LaunchedEffect(previousArtwork) {
        state.previous =
            if (previousArtwork != null) {
                extract(previousArtwork)
            } else {
                state.committed
            }
    }

    /*
     * ---------------------------------------------------------
     * NEXT
     * ---------------------------------------------------------
     */
    LaunchedEffect(nextArtwork) {
        state.next =
            if (nextArtwork != null) {
                extract(nextArtwork)
            } else {
                state.committed
            }
    }

    /*
     * ---------------------------------------------------------
     * RESTING COMMIT
     * ---------------------------------------------------------
     *
     * Do not immediately accept a new current color simply
     * because Media3 changed currentSongId.
     *
     * Wait until:
     * 1. carousel transaction is finished
     * 2. artwork displacement is back at zero
     * 3. state remains stable for a short visual boundary
     *
     * The small delay is NOT a fake song-transition timer. It
     * only debounces Compose/Media3 state publication ordering.
     */
    LaunchedEffect(
        currentSongId,
        transactionActive,
        state.current
    ) {
        if (transactionActive) {
            return@LaunchedEffect
        }

        if (abs(coverX.value) >= 1f) {
            return@LaunchedEffect
        }

        val expectedSongId = currentSongId
        val expectedColor = state.current

        delay(32)

        if (
            expectedSongId != currentSongId ||
            transactionActive ||
            abs(coverX.value) >= 1f ||
            expectedColor != state.current ||
            expectedColor == state.committed
        ) {
            return@LaunchedEffect
        }

        val from = state.rendered
        val to = expectedColor

        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 500
            )
        ) { value, _ ->
            val rawFraction = value ?: 0f

            state.rendered =
                mixColor(
                    from = from,
                    to = to,
                    fraction = smoothFraction(rawFraction)
                )
        }

        /*
         * Only commit if the visual target is still valid.
         */
        if (
            expectedSongId == currentSongId &&
            !transactionActive &&
            abs(coverX.value) < 1f &&
            expectedColor == state.current
        ) {
            state.committed = to
            state.rendered = to
        }
    }

    return state
}

internal fun playerDisplayColor(
    colors: PlayerColorState,
    coverX: Float,
    coverWidth: Float
): Color {
    val width = coverWidth.coerceAtLeast(1f)

    val fraction =
        (coverX / width)
            .coerceIn(-1f, 1f)

    /*
     * Resting UI is always the controlled rendered color.
     */
    if (abs(fraction) < .001f) {
        return colors.rendered
    }

    val destination =
        if (fraction < 0f) {
            colors.next
        } else {
            colors.previous
        }

    return mixColor(
        from = colors.committed,
        to = destination,
        fraction = smoothFraction(abs(fraction))
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
        if (displayColor.luminance() > .67f) {
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
                Color.Black.copy(alpha = .065f)

            XmoTheme.Dark ->
                Color.White.copy(alpha = .105f)

            XmoTheme.Amoled ->
                Color.White.copy(alpha = .12f)
        }

    /*
     * More translucent than before so artwork color remains
     * visible through the lower player surface.
     *
     * Do not make AMOLED completely opaque black here; the
     * artwork-derived field should still be perceptible.
     */
    val panel =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(alpha = .52f)

            XmoTheme.Dark ->
                Color(0xFF202126).copy(alpha = .52f)

            XmoTheme.Amoled ->
                Color(0xFF08090B).copy(alpha = .62f)
        }

    val border =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(alpha = .42f)

            XmoTheme.Dark ->
                Color.White.copy(alpha = .13f)

            XmoTheme.Amoled ->
                Color.White.copy(alpha = .15f)
        }

    return PlayerThemeColors(
        overlayText = overlayText,
        controls = controls,
        playBackground = softButton,
        panel = panel,
        border = border,
        softButton = softButton
    )
}

internal fun liftArtworkColor(
    color: Color
): Color {
    /*
     * Preserve hue but stop dark Palette dominant colors from
     * producing a nearly-black Now Playing background.
     */
    val strongest =
        max(
            color.red,
            max(
                color.green,
                color.blue
            )
        )

    if (strongest <= .001f) {
        return Color(0xFF777A84)
    }

    /*
     * Lift genuinely dark artwork more strongly.
     */
    val targetPeak =
        when {
            strongest < .18f -> .48f
            strongest < .30f -> .44f
            strongest < .40f -> .42f
            else -> strongest
        }

    val multiplier =
        if (strongest < targetPeak) {
            targetPeak / strongest
        } else {
            1f
        }

    var red =
        (color.red * multiplier)
            .coerceIn(0f, 1f)

    var green =
        (color.green * multiplier)
            .coerceIn(0f, 1f)

    var blue =
        (color.blue * multiplier)
            .coerceIn(0f, 1f)

    /*
     * Slight white lift keeps very saturated Palette values
     * premium/bright rather than muddy without washing the hue.
     */
    val whiteLift =
        if (strongest < .42f) {
            .07f
        } else {
            .025f
        }

    red += (1f - red) * whiteLift
    green += (1f - green) * whiteLift
    blue += (1f - blue) * whiteLift

    return Color(
        red = red.coerceIn(0f, 1f),
        green = green.coerceIn(0f, 1f),
        blue = blue.coerceIn(0f, 1f),
        alpha = 1f
    )
}

internal fun mixColor(
    from: Color,
    to: Color,
    fraction: Float
): Color {
    val value =
        fraction.coerceIn(0f, 1f)

    return Color(
        red =
            from.red +
                (to.red - from.red) * value,

        green =
            from.green +
                (to.green - from.green) * value,

        blue =
            from.blue +
                (to.blue - from.blue) * value,

        alpha = 1f
    )
}

private fun smoothFraction(
    fraction: Float
): Float {
    val value =
        fraction.coerceIn(0f, 1f)

    /*
     * Smoothstep:
     * no abrupt velocity change at either end.
     */
    return value * value * (3f - 2f * value)
}
