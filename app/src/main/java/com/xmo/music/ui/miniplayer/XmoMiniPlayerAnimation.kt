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

    const val axisThresholdPx = 9f
    const val horizontalThresholdPx = 48f
    const val openThresholdPx = -46f
    const val closeThresholdPx = 44f

    const val previewCommitDelayMs = 320L

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
                (distance - free) * .07f
            ) * sign(value)
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
                (distance - free) * .075f
            ) * sign(value)
    }

    private val playerTransitionSpec:
        AnimationSpec<Float>
        get() =
            spring(
                dampingRatio = .86f,
                stiffness = 320f
            )

    val riseSpec: AnimationSpec<Float>
        get() = playerTransitionSpec

    val openExitSpec: AnimationSpec<Float>
        get() = playerTransitionSpec

    val closeExitSpec: AnimationSpec<Float>
        get() = playerTransitionSpec

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
     * Short content motion is deliberate:
     * rapid swipes can retarget this without building a slow
     * visual backlog.
     */
    fun metadataChange(
        direction: Int
    ): ContentTransform {
        val normalized =
            direction.coerceIn(-1, 1)

        if (normalized == 0) {
            return fadeIn(
                tween(130)
            )
                .togetherWith(
                    fadeOut(
                        tween(110)
                    )
                )
        }

        /*
         * +1 = NEXT:
         * old goes down, new comes from top.
         *
         * -1 = PREVIOUS:
         * old goes up, new comes from bottom.
         */
        val enter =
            if (normalized > 0) {
                { height: Int ->
                    -height
                }
            } else {
                { height: Int ->
                    height
                }
            }

        val exit =
            if (normalized > 0) {
                { height: Int ->
                    height
                }
            } else {
                { height: Int ->
                    -height
                }
            }

        return (
            fadeIn(
                tween(125)
            ) +
                slideInVertically(
                    initialOffsetY = enter,
                    animationSpec =
                        tween(
                            durationMillis = 165,
                            easing =
                                FastOutSlowInEasing
                        )
                )
            )
            .togetherWith(
                fadeOut(
                    tween(105)
                ) +
                    slideOutVertically(
                        targetOffsetY = exit,
                        animationSpec =
                            tween(
                                durationMillis = 145,
                                easing =
                                    FastOutSlowInEasing
                            )
                    )
            )
    }

    /*
     * Artwork never moves position.
     * Only old/new cover alpha changes.
     */
    fun artworkChange():
        ContentTransform =
        fadeIn(
            tween(145)
        )
            .togetherWith(
                fadeOut(
                    tween(120)
                )
            )
}
