package com.xmo.music.ui.miniplayer

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import kotlin.math.abs
import kotlin.math.sign

internal enum class XmoMiniAxis {
    None,
    Horizontal,
    Vertical
}

internal object XmoMiniPlayerAnimation {

    const val axisThresholdPx =
        9f

    const val horizontalThresholdPx =
        48f

    const val openThresholdPx =
        -46f

    const val closeThresholdPx =
        44f

    fun horizontalResistance(
        value: Float
    ): Float {
        val free = 76f
        val distance = abs(value)

        if (distance <= free) {
            return value
        }

        return (
            free +
                (distance - free) *
                .07f
            ) *
            sign(value)
    }

    fun verticalResistance(
        value: Float
    ): Float {
        val free =
            if (value < 0f) {
                66f
            } else {
                44f
            }

        val distance =
            abs(value)

        if (distance <= free) {
            return value
        }

        return (
            free +
                (distance - free) *
                .075f
            ) *
            sign(value)
    }

    /*
     * Shared MiniPlayer <-> Now Playing spring family.
     */
    private val playerTransitionSpec:
        AnimationSpec<Float>
        get() =
            spring(
                dampingRatio = .86f,
                stiffness = 320f
            )

    val riseSpec: AnimationSpec<Float>
        get() =
            playerTransitionSpec

    val openExitSpec: AnimationSpec<Float>
        get() =
            playerTransitionSpec

    val closeExitSpec: AnimationSpec<Float>
        get() =
            playerTransitionSpec

    val horizontalReturnSpec: AnimationSpec<Float>
        get() =
            spring(
                dampingRatio = .80f,
                stiffness = 470f
            )

    val verticalReturnSpec: AnimationSpec<Float>
        get() =
            spring(
                dampingRatio = .84f,
                stiffness = 440f
            )

    /*
     * =========================================================
     * SONG METADATA CHANGE
     * =========================================================
     *
     * Old metadata falls downward.
     * New metadata enters from above.
     *
     * AnimatedContent itself clips inside the metadata viewport.
     */
    fun metadataChange(): ContentTransform =
        (
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = 190,
                        easing =
                            FastOutSlowInEasing
                    )
            ) +
                slideInVertically(
                    initialOffsetY = {
                        -it
                    },
                    animationSpec =
                        tween(
                            durationMillis = 230,
                            easing =
                                FastOutSlowInEasing
                        )
                )
            )
            .togetherWith(
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis = 160
                        )
                ) +
                    slideOutVertically(
                        targetOffsetY = {
                            it
                        },
                        animationSpec =
                            tween(
                                durationMillis = 210,
                                easing =
                                    FastOutSlowInEasing
                            )
                    )
            )

    /*
     * Artwork uses the same compact duration family but moves
     * horizontally inside its own clipped 50dp square.
     */
    fun artworkChange(): ContentTransform =
        (
            fadeIn(
                animationSpec =
                    tween(190)
            ) +
                androidx.compose.animation
                    .slideInHorizontally(
                        initialOffsetX = {
                            it
                        },
                        animationSpec =
                            tween(
                                durationMillis = 230,
                                easing =
                                    FastOutSlowInEasing
                            )
                    )
            )
            .togetherWith(
                fadeOut(
                    animationSpec =
                        tween(160)
                ) +
                    androidx.compose.animation
                        .slideOutHorizontally(
                            targetOffsetX = {
                                -it
                            },
                            animationSpec =
                                tween(
                                    durationMillis = 210,
                                    easing =
                                        FastOutSlowInEasing
                                )
                        )
            )
}
