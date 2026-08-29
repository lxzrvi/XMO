package com.xmo.music.ui.nowplaying

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs

@Stable
internal class PlayerCarouselState(
    initialId: Long?,
    initialIndex: Int,
    initialCurrent: Uri?,
    initialPrevious: Uri?,
    initialNext: Uri?
) {
    val x =
        Animatable(0f)

    /*
     * Carousel page width.
     *
     * Important:
     * This is the HOST width again, not artwork width.
     *
     * The artwork itself keeps its 17dp inset. Previous and next
     * covers therefore retain the original visual separation.
     */
    var width by
        mutableStateOf(1f)

    var manualDirection by
        mutableIntStateOf(0)

    var manualSongId by
        mutableStateOf<Long?>(null)

    var autoAnimating by
        mutableStateOf(false)

    var autoDirection by
        mutableIntStateOf(0)

    /*
     * ONE frozen visual queue window.
     *
     * Artwork and Palette both consume these values. Live queue
     * recomposition cannot replace the destination halfway
     * through an animation.
     */
    var visualSongId by
        mutableStateOf(initialId)
        private set

    var visualIndex by
        mutableIntStateOf(initialIndex)
        private set

    var visualCurrent by
        mutableStateOf(initialCurrent)
        private set

    var visualPrevious by
        mutableStateOf(initialPrevious)
        private set

    var visualNext by
        mutableStateOf(initialNext)
        private set

    val transactionActive: Boolean
        get() =
            manualDirection != 0 ||
                autoAnimating

    val isResting: Boolean
        get() =
            !transactionActive &&
                abs(x.value) < 1f

    fun updateIdleWindow(
        id: Long?,
        index: Int,
        current: Uri?,
        previous: Uri?,
        next: Uri?
    ) {
        if (transactionActive) {
            return
        }

        if (id != visualSongId) {
            return
        }

        visualIndex = index
        visualCurrent = current
        visualPrevious = previous
        visualNext = next
    }

    fun beginManual(
        direction: Int
    ) {
        if (transactionActive) {
            return
        }

        val normalized =
            direction.coerceIn(
                -1,
                1
            )

        if (normalized == 0) {
            return
        }

        manualDirection =
            normalized

        manualSongId =
            visualSongId
    }

    fun finishManual(
        id: Long?,
        index: Int,
        current: Uri?,
        previous: Uri?,
        next: Uri?
    ) {
        adoptWindow(
            id = id,
            index = index,
            current = current,
            previous = previous,
            next = next
        )

        manualDirection = 0
        manualSongId = null
    }

    fun cancelManual() {
        manualDirection = 0
        manualSongId = null
    }

    fun beginAutomatic(
        direction: Int
    ) {
        if (transactionActive) {
            return
        }

        autoDirection =
            direction.coerceIn(
                -1,
                1
            )

        autoAnimating = true
    }

    fun finishAutomatic(
        id: Long?,
        index: Int,
        current: Uri?,
        previous: Uri?,
        next: Uri?
    ) {
        adoptWindow(
            id = id,
            index = index,
            current = current,
            previous = previous,
            next = next
        )

        autoDirection = 0
        autoAnimating = false
    }

    private fun adoptWindow(
        id: Long?,
        index: Int,
        current: Uri?,
        previous: Uri?,
        next: Uri?
    ) {
        visualSongId = id
        visualIndex = index
        visualCurrent = current
        visualPrevious = previous
        visualNext = next
    }
}
