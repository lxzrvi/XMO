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

    var width by
        mutableStateOf(1f)
        private set

    /*
     *  0 = idle
     *  1 = next
     * -1 = previous
     */
    var manualDirection by
        mutableIntStateOf(0)

    /*
     * Real Media3 song that was current when manual navigation
     * started. A different real currentSongId confirms playback.
     */
    var manualSongId by
        mutableStateOf<Long?>(null)

    /*
     * Media3 / external transition animation.
     */
    var autoAnimating by
        mutableStateOf(false)

    /*
     * Direction of the automatic visual transaction.
     *
     *  0 = none / unknown
     *  1 = next
     * -1 = previous
     */
    var autoDirection by
        mutableIntStateOf(0)

    /*
     * Song IDs belonging to the visual transaction.
     *
     * These are intentionally independent from whatever queue
     * window Compose receives while Media3 is changing items.
     */
    var visualFromSongId by
        mutableStateOf<Long?>(null)

    var visualToSongId by
        mutableStateOf<Long?>(null)

    /*
     * Increments for every real visual transaction.
     *
     * Consumers can distinguish:
     *
     * old transaction ended
     * -> new transaction immediately began
     *
     * without relying on delays/timers.
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

    fun updateWidth(
        value: Float
    ) {
        if (value > 0f) {
            width = value
        }
    }

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

        /*
         * A manual transaction owns the carousel now.
         */
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

        /*
         * Automatic/external change takes ownership only when a
         * manual transaction is not being confirmed by this same
         * Media3 change.
         */
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
