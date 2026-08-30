package com.xmo.music.ui.nowplaying

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.GraphicsLayerScope

/*
 * =============================================================
 * XMO NOW PLAYING MOTION SYSTEM
 * =============================================================
 *
 * This object owns only reusable motion definitions.
 *
 * It does NOT own:
 * - queue state
 * - queue drag state
 * - overlay state
 * - carousel state
 * - playback state
 *
 * Those remain in their responsible components.
 */
internal object XmoPlayerAnimation {

    /*
     * =========================================================
     * CENTERED OVERLAYS
     * =========================================================
     *
     * Song Options / Details / Artist:
     *
     * - remain centered
     * - no vertical movement
     * - fade + subtle center scale
     * - no visible black backdrop
     */

    const val overlayBackdropAlpha =
        0f

    private const val overlayStartScale =
        .955f

    val overlayRevealSpec
        get() =
            tween<Float>(
                durationMillis = 240,
                easing =
                    FastOutSlowInEasing
            )

    val overlayHideSpec
        get() =
            tween<Float>(
                durationMillis = 175,
                easing =
                    FastOutSlowInEasing
            )

    fun GraphicsLayerScope.centerOverlay(
        progress: Float
    ) {
        val value =
            progress.coerceIn(
                0f,
                1f
            )

        alpha =
            value

        val scale =
            overlayStartScale +
                (
                    1f -
                        overlayStartScale
                    ) *
                value

        scaleX =
            scale

        scaleY =
            scale

        /*
         * Explicitly no vertical translation.
         */
        translationY =
            0f
    }

    /*
     * =========================================================
     * QUEUE SHEET
     * =========================================================
     *
     * Queue itself comes from below and leaves downward.
     *
     * No visible black backdrop is drawn.
     */

    const val queueBackdropAlpha =
        0f

    /*
     * Starts sufficiently below its resting point to read as a
     * real bottom-sheet arrival instead of a tiny bounce.
     *
     * It is still initialized before first frame, preserving the
     * no giant-sheet flash behavior.
     */
    const val queueInitialOffsetFraction =
        .22f

    const val queueDismissThreshold =
        .12f

    val queueBackdropEnterSpec
        get() =
            tween<Float>(
                durationMillis = 160
            )

    val queueBackdropExitSpec
        get() =
            tween<Float>(
                durationMillis = 140
            )

    val queueEnterSpec
        get() =
            spring<Float>(
                dampingRatio = .90f,
                stiffness = 310f
            )

    val queueSettleSpec
        get() =
            spring<Float>(
                dampingRatio = .87f,
                stiffness = 360f
            )

    val queueExitSpec
        get() =
            tween<Float>(
                durationMillis = 245,
                easing =
                    FastOutSlowInEasing
            )

    /*
     * =========================================================
     * OVERLAY HOST
     * =========================================================
     *
     * Individual cards/sheets own their real visible motion.
     *
     * AnimatedContent only retains outgoing content long enough
     * for its own exit animation to complete. Keeping host scale
     * almost neutral prevents double-animation.
     */

    fun overlayHostTransition(
        opening: Boolean,
        closing: Boolean
    ): ContentTransform {
        return when {
            opening -> {
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = 135
                        )
                )
                    .togetherWith(
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis = 90
                                )
                        )
                    )
            }

            closing -> {
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = 80
                        )
                )
                    .togetherWith(
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis = 180
                                )
                        )
                    )
            }

            else -> {
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = 145
                        )
                )
                    .togetherWith(
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis = 125
                                )
                        )
                    )
            }
        }
    }

    /*
     * =========================================================
     * FULLSCREEN LYRICS
     * =========================================================
     *
     * Existing approved fade + scale character retained.
     */

    val fullLyricsEnter: EnterTransition
        get() =
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = 310
                    )
            ) +
                scaleIn(
                    initialScale = .94f,
                    animationSpec =
                        tween(
                            durationMillis = 390,
                            easing =
                                FastOutSlowInEasing
                        )
                )

    val fullLyricsExit: ExitTransition
        get() =
            fadeOut(
                animationSpec =
                    tween(
                        durationMillis = 250
                    )
            ) +
                scaleOut(
                    targetScale = .95f,
                    animationSpec =
                        tween(
                            durationMillis = 330,
                            easing =
                                FastOutSlowInEasing
                        )
                )

    /*
     * =========================================================
     * XMO POP
     * =========================================================
     *
     * Pop uses center scale + fade. No toast-like vertical travel.
     */

    val popHostEnter: EnterTransition
        get() =
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = 185
                    )
            ) +
                scaleIn(
                    initialScale = .94f,
                    animationSpec =
                        spring(
                            dampingRatio = .86f,
                            stiffness = 420f
                        )
                )

    val popHostExit: ExitTransition
        get() =
            fadeOut(
                animationSpec =
                    tween(
                        durationMillis = 170
                    )
            ) +
                scaleOut(
                    targetScale = .96f,
                    animationSpec =
                        tween(
                            durationMillis = 180,
                            easing =
                                FastOutSlowInEasing
                        )
                )

    /*
     * XmoPop has its own tiny inner reveal because each new
     * message receives a fresh composition.
     */
    val popRevealSpec
        get() =
            tween<Float>(
                durationMillis = 220,
                easing =
                    LinearOutSlowInEasing
            )

    fun GraphicsLayerScope.pop(
        progress: Float
    ) {
        val value =
            progress.coerceIn(
                0f,
                1f
            )

        alpha =
            value

        val scale =
            .97f +
                .03f *
                value

        scaleX =
            scale

        scaleY =
            scale

        translationY =
            0f
    }
}
