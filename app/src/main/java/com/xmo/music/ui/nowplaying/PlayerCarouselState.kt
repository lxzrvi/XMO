package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs

@Stable
internal class PlayerCarouselState {

    val x =
        Animatable(0f)

    /*
     * Actual artwork width in pixels.
     *
     * Keep setter public for compatibility with existing
     * PlayerArtwork usage.
     */
    var width by
        mutableStateOf(1f)

    /*
     *  0 = idle
     *  1 = next
     * -1 = previous
     */
    var manualDirection by
        mutableIntStateOf(0)

    /*
     * Song that was current when manual navigation started.
     */
    var manualSongId by
        mutableStateOf<Long?>(null)

    /*
     * True while Media3/external transition is visually moving.
     */
    var autoAnimating by
        mutableStateOf(false)

    /*
     *  0 = none
     *  1 = next
     * -1 = previous
     */
    var autoDirection by
        mutableIntStateOf(0)

    /*
     * Explicit visual transaction IDs.
     *
     * They are intentionally independent from the live queue
     * window because Media3 may publish the new song before the
     * visual carousel has completed.
     */
    var visualFromSongId by
        mutableStateOf<Long?>(null)

    var visualToSongId by
        mutableStateOf<Long?>(null)

    /*
     * Lets consumers distinguish consecutive transactions
     * without timers.
     */
    var transactionGeneration by
        mutableLongStateOf(0L)
        private set

    val transactionActive: Boolean
        get() =
            manualDirection != 0 ||
                autoAnimating

    val direction: Int
        get() =
            when {
                manualDirection != 0 ->
                    manualDirection

                autoAnimating ->
                    autoDirection

                else ->
                    0
            }

    val isResting: Boolean
        get() =
            !transactionActive &&
                abs(x.value) < 1f

    fun beginManual(
        direction: Int,
        currentSongId: Long?
    ) {
        val normalized =
            direction.coerceIn(
                -1,
                1
            )

        if (normalized == 0) {
            return
        }

        transactionGeneration++

        manualDirection =
            normalized

        manualSongId =
            currentSongId

        visualFromSongId =
            currentSongId

        visualToSongId =
            null

        autoAnimating =
            false

        autoDirection =
            0
    }

    fun confirmManualTarget(
        songId: Long?
    ) {
        if (
            manualDirection != 0 &&
            songId != null &&
            songId != manualSongId
        ) {
            visualToSongId =
                songId
        }
    }

    fun finishManual() {
        manualDirection =
            0

        manualSongId =
            null

        visualFromSongId =
            visualToSongId

        visualToSongId =
            null
    }

    fun cancelManual() {
        manualDirection =
            0

        manualSongId =
            null

        visualFromSongId =
            null

        visualToSongId =
            null
    }

    fun beginAutomatic(
        fromSongId: Long?,
        toSongId: Long?,
        direction: Int
    ) {
        val normalized =
            direction.coerceIn(
                -1,
                1
            )

        transactionGeneration++

        autoAnimating =
            true

        autoDirection =
            normalized

        visualFromSongId =
            fromSongId

        visualToSongId =
            toSongId

        manualDirection =
            0

        manualSongId =
            null
    }

    fun finishAutomatic() {
        autoAnimating =
            false

        autoDirection =
            0

        visualFromSongId =
            visualToSongId

        visualToSongId =
            null
    }

    fun clearVisualTransaction() {
        manualDirection =
            0

        manualSongId =
            null

        autoAnimating =
            false

        autoDirection =
            0

        visualFromSongId =
            null

        visualToSongId =
            null
    }
}
