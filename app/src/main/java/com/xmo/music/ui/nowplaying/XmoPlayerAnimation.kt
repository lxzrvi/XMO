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

internal object XmoPlayerAnimation {

    /*
     * =========================================================
     * CENTER OVERLAYS
     * =========================================================
     *
     * Options / Details / Artist / Sleep:
     * fade + center scale only.
     */

    val overlayRevealSpec
        get() =
            tween<Float>(
                durationMillis = 240,
                easing = FastOutSlowInEasing
            )

    val overlayHideSpec
        get() =
            tween<Float>(
                durationMillis = 175,
                easing = FastOutSlowInEasing
            )

    fun GraphicsLayerScope.centerOverlay(
        progress: Float
    ) {
        val value =
            progress.coerceIn(
                0f,
                1f
            )

        alpha = value

        val scale =
            .955f +
                .045f *
                value

        scaleX = scale
        scaleY = scale

        /*
         * Never move a center popup diagonally/vertically.
         */
        translationX = 0f
        translationY = 0f
    }

    /*
     * =========================================================
     * QUEUE
     * =========================================================
     */

    const val queueInitialOffsetFraction =
        .22f

    const val queueDismissThreshold =
        .12f

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
                easing = FastOutSlowInEasing
            )

    /*
     * =========================================================
     * OVERLAY HOST
     * =========================================================
     *
     * The actual child owns ALL visible motion.
     *
     * This prevents:
     * Options -> Queue getting a scale/zoom,
     * Queue -> Details getting a diagonal cross-animation, etc.
     */
    fun overlayHostTransition(): ContentTransform =
        EnterTransition.None
            .togetherWith(
                ExitTransition.None
            )

    /*
     * =========================================================
     * FULLSCREEN LYRICS
     * =========================================================
     */

    val fullLyricsEnter: EnterTransition
        get() =
            fadeIn(
                tween(310)
            ) +
                scaleIn(
                    initialScale = .94f,
                    animationSpec =
                        tween(
                            durationMillis = 390,
                            easing = FastOutSlowInEasing
                        )
                )

    val fullLyricsExit: ExitTransition
        get() =
            fadeOut(
                tween(250)
            ) +
                scaleOut(
                    targetScale = .95f,
                    animationSpec =
                        tween(
                            durationMillis = 330,
                            easing = FastOutSlowInEasing
                        )
                )

    /*
     * =========================================================
     * XMO POP
     * =========================================================
     */

    val popHostEnter: EnterTransition
        get() =
            fadeIn(
                tween(185)
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
                tween(170)
            ) +
                scaleOut(
                    targetScale = .96f,
                    animationSpec =
                        tween(180)
                )

    val popRevealSpec
        get() =
            tween<Float>(
                durationMillis = 220,
                easing = LinearOutSlowInEasing
            )

    fun GraphicsLayerScope.pop(
        progress: Float
    ) {
        val value =
            progress.coerceIn(
                0f,
                1f
            )

        alpha = value

        val scale =
            .97f +
                .03f *
                value

        scaleX = scale
        scaleY = scale

        translationX = 0f
        translationY = 0f
    }
}
