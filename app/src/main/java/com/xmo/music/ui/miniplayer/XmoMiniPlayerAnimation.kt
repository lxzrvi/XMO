package com.xmo.music.ui.miniplayer

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
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

    const val horizontalThresholdPx =
        48f

    const val openThresholdPx =
        -38f

    const val closeThresholdPx =
        44f

    const val axisThresholdPx =
        9f

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
        /*
         * Both directions are allowed:
         *
         * up   -> open Now Playing
         * down -> close playback
         */
        val free =
            if (value < 0f) {
                48f
            } else {
                42f
            }

        val distance =
            abs(value)

        if (distance <= free) {
            return value
        }

        val resisted =
            free +
                (
                    distance -
                        free
                    ) *
                .065f

        return resisted *
            sign(value)
    }

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
                dampingRatio = .82f,
                stiffness = 450f
            )

    /*
     * Once an upward-open gesture succeeds, do not return the
     * MiniPlayer to rest first. It leaves immediately downward,
     * then Now Playing is allowed to appear.
     */
    val openExitSpec: AnimationSpec<Float>
        get() =
            tween(
                durationMillis = 185
            )

    val closeExitSpec: AnimationSpec<Float>
        get() =
            tween(
                durationMillis = 190
            )
}
