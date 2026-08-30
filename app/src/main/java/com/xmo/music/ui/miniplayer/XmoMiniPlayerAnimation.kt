package com.xmo.music.ui.miniplayer

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
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
     * One shared spring for:
     *
     * Now Playing -> MiniPlayer rise
     * MiniPlayer -> Now Playing exit
     * MiniPlayer -> playback close exit
     *
     * Therefore the motion uses the exact same spring character,
     * not an approximated millisecond duration.
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
}
