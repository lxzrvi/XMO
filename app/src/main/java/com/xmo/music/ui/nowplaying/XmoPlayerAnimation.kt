package com.xmo.music.ui.nowplaying

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.GraphicsLayerScope

internal object XmoPlayerAnimation {

    /*
     * =========================================================
     * CENTERED PLAYER OVERLAYS
     * =========================================================
     */

    const val overlayBackdropAlpha = .29f

    const val overlayStartScale = .95f

    const val overlayStartTranslationY = 22f

    val overlayRevealSpec
        get() =
            tween<Float>(
                durationMillis = 260,
                easing = FastOutSlowInEasing
            )

    val overlayHideSpec
        get() =
            tween<Float>(
                durationMillis = 190,
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
            overlayStartScale +
                (1f - overlayStartScale) *
                value

        scaleX = scale
        scaleY = scale

        translationY =
            (1f - value) *
                overlayStartTranslationY
    }

    /*
     * =========================================================
     * QUEUE
     * =========================================================
     */

    const val queueInitialOffsetFraction = .14f

    const val queueDismissThreshold = .12f

    val queueBackdropEnterSpec
        get() =
            tween<Float>(
                durationMillis = 240,
                easing = FastOutSlowInEasing
            )

    val queueBackdropExitSpec
        get() =
            tween<Float>(
                durationMillis = 180
            )

    val queueEnterSpec
        get() =
            spring<Float>(
                dampingRatio = .9f,
                stiffness = 320f
            )

    val queueSettleSpec
        get() =
            spring<Float>(
                dampingRatio = .86f,
                stiffness = 360f
            )

    val queueExitSpec
        get() =
            tween<Float>(
                durationMillis = 260,
                easing = FastOutSlowInEasing
            )

    /*
     * =========================================================
     * ANIMATEDCONTENT HOST
     * =========================================================
     */

    fun overlayHostTransition(
        opening: Boolean,
        closing: Boolean
    ): ContentTransform {
        return when {
            opening -> {
                (
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis = 190
                            )
                    ) +
                        scaleIn(
                            initialScale = .985f,
                            animationSpec =
                                spring(
                                    dampingRatio = .9f,
                                    stiffness = 420f
                                )
                        )
                    )
                    .togetherWith(
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis = 110
                                )
                        )
                    )
            }

            closing -> {
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = 90
                        )
                )
                    .togetherWith(
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis = 180
                                )
                        ) +
                            scaleOut(
                                targetScale = .985f,
                                animationSpec =
                                    tween(
                                        durationMillis = 180,
                                        easing =
                                            FastOutSlowInEasing
                                    )
                            )
                    )
            }

            else -> {
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = 170
                        )
                )
                    .togetherWith(
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis = 140
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
     */

    val fullLyricsEnter: EnterTransition
        get() =
            fadeIn(
                animationSpec =
                    tween(310)
            ) +
                scaleIn(
                    initialScale = .94f,
                    animationSpec =
                        tween(390)
                )

    val fullLyricsExit: ExitTransition
        get() =
            fadeOut(
                animationSpec =
                    tween(250)
            ) +
                scaleOut(
                    targetScale = .95f,
                    animationSpec =
                        tween(330)
                )

    /*
     * =========================================================
     * XMO POP HOST
     * =========================================================
     */

    val popHostEnter: EnterTransition
        get() =
            fadeIn(
                animationSpec =
                    tween(210)
            ) +
                scaleIn(
                    initialScale = .96f,
                    animationSpec =
                        tween(
                            durationMillis = 260,
                            easing =
                                FastOutSlowInEasing
                        )
                ) +
                slideInVertically(
                    initialOffsetY = {
                        -it / 6
                    },
                    animationSpec =
                        tween(
                            durationMillis = 260,
                            easing =
                                FastOutSlowInEasing
                        )
                )

    val popHostExit: ExitTransition
        get() =
            fadeOut(
                animationSpec =
                    tween(180)
            ) +
                scaleOut(
                    targetScale = .97f,
                    animationSpec =
                        tween(190)
                ) +
                slideOutVertically(
                    targetOffsetY = {
                        -it / 8
                    },
                    animationSpec =
                        tween(
                            durationMillis = 190,
                            easing =
                                FastOutSlowInEasing
                        )
                )

    /*
     * Internal XmoPop arrival is deliberately subtle because
     * AnimatedVisibility already handles its host transition.
     */

    val popRevealSpec
        get() =
            tween<Float>(
                durationMillis = 260,
                easing = FastOutSlowInEasing
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

        translationY =
            (1f - value) *
                10f

        val scale =
            .98f +
                .02f *
                value

        scaleX = scale
        scaleY = scale
    }
}
