package com.xmo.music.ui.miniplayer

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
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

    /*
     * Upward swipe can travel a little farther before strong
     * resistance starts.
     */
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
     * Return from Now Playing still rises naturally from behind
     * the NavBar.
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

    /*
     * Only a rejected/short vertical swipe uses this.
     */
    val verticalReturnSpec: AnimationSpec<Float>
        get() =
            spring(
                dampingRatio = .84f,
                stiffness = 440f
            )

    /*
     * Successful tap / swipe-up:
     *
     * direct constant-speed downward exit.
     * No spring and no ease-in/ease-out.
     */
    val openExitSpec: AnimationSpec<Float>
        get() =
            tween(
                durationMillis = 175,
                easing = LinearEasing
            )

    /*
     * Successful swipe-down close uses the same direct motion.
     */
    val closeExitSpec: AnimationSpec<Float>
        get() =
            tween(
                durationMillis = 175,
                easing = LinearEasing
            )
}
