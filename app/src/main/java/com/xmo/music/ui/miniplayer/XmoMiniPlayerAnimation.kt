package com.xmo.music.ui.miniplayer

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import kotlin.math.abs
import kotlin.math.roundToInt
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

    /*
     * Reference travel used for matching exit movement to the
     * perceived pace of the Now Playing -> MiniPlayer entrance.
     */
    private const val referenceDurationMs =
        360f

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
         * Upward gesture gets slightly more free movement than
         * downward dismissal.
         */
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
     * Existing Now Playing -> MiniPlayer entrance character.
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
     * Exit duration follows actual distance.
     *
     * Example:
     *
     * referenceDistance = normal resting -> completely hidden
     * distance
     *
     * If swipe-up starts 50px above rest, it has 50px farther to
     * travel. Duration increases proportionally instead of making
     * the card move faster.
     */
    fun exitSpec(
        distancePx: Float,
        referenceDistancePx: Float
    ): AnimationSpec<Float> {
        val reference =
            referenceDistancePx
                .coerceAtLeast(1f)

        val distance =
            distancePx
                .coerceAtLeast(1f)

        val duration =
            (
                referenceDurationMs *
                    (
                        distance /
                            reference
                        )
                )
                .roundToInt()
                .coerceIn(
                    280,
                    520
                )

        return tween(
            durationMillis =
                duration,
            easing =
                FastOutSlowInEasing
        )
    }
}
