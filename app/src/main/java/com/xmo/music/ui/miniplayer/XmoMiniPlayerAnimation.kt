package com.xmo.music.ui.miniplayer

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
        val free =
            76f

        val distance =
            abs(value)

        if (distance <= free) {
            return value
        }

        return (
            free +
                (
                    distance -
                        free
                    ) *
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
                (
                    distance -
                        free
                    ) *
                .075f
            ) *
            sign(value)
    }

    /*
     * Now Playing -> MiniPlayer entrance.
     */
    val riseSpec: AnimationSpec<Float>
        get() =
            spring(
                dampingRatio = .86f,
                stiffness = 320f
            )

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
     * MiniPlayer -> Now Playing / close.
     *
     * Previous 175ms exit was too abrupt.
     * This duration is intentionally close to the perceived
     * speed of the MiniPlayer rise animation.
     *
     * No bounce/overshoot on disappearance.
     */
    val openExitSpec: AnimationSpec<Float>
        get() =
            tween(
                durationMillis = 315,
                easing = FastOutSlowInEasing
            )

    val closeExitSpec: AnimationSpec<Float>
        get() =
            tween(
                durationMillis = 315,
                easing = FastOutSlowInEasing
            )
}
