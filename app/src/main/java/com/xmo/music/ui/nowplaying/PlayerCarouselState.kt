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
        Animatable(
            0f
        )

    /*
     * One page is the complete square artwork host.
     */
    var width by
        mutableStateOf(
            1f
        )

    /*
     *  0 = idle
     *  1 = next
     * -1 = previous
     */
    var manualDirection by
        mutableIntStateOf(
            0
        )

    var manualSongId by
        mutableStateOf<Long?>(
            null
        )

    var autoAnimating by
        mutableStateOf(
            false
        )

    var autoDirection by
        mutableIntStateOf(
            0
        )

    /*
     * =========================================================
     * FROZEN VISUAL QUEUE WINDOW
     * =========================================================
     *
     * Both artwork rendering and Palette extraction consume this
     * exact same window.
     */
    var visualSongId by
        mutableStateOf(
            initialId
        )
        private set

    var visualIndex by
        mutableIntStateOf(
            initialIndex
        )
        private set

    var visualCurrent by
        mutableStateOf(
            initialCurrent
        )
        private set

    var visualPrevious by
        mutableStateOf(
            initialPrevious
        )
        private set

    var visualNext by
        mutableStateOf(
            initialNext
        )
        private set

    val transactionActive: Boolean
        get() =
            manualDirection !=
                0 ||
                autoAnimating

    val isResting: Boolean
        get() =
            !transactionActive &&
                abs(
                    x.value
                ) <
                1f

    /*
     * Handles first composition if Media3 current item was not
     * available when this state object was created.
     */
    fun initializeIfEmpty(
        id: Long?,
        index: Int,
        current: Uri?,
        previous: Uri?,
        next: Uri?
    ) {
        if (
            transactionActive ||
            id == null ||
            visualSongId !=
            null
        ) {
            return
        }

        adoptWindow(
            id =
                id,
            index =
                index,
            current =
                current,
            previous =
                previous,
            next =
                next
        )
    }

    /*
     * Artwork/neighbor metadata may refresh for the SAME visual
     * song. A different live Media3 song cannot overwrite the
     * frozen transaction window through this method.
     */
    fun updateIdleWindow(
        id: Long?,
        index: Int,
        current: Uri?,
        previous: Uri?,
        next: Uri?
    ) {
        if (
            transactionActive ||
            id !=
            visualSongId
        ) {
            return
        }

        visualIndex =
            index

        visualCurrent =
            current

        visualPrevious =
            previous

        visualNext =
            next
    }

    fun beginManual(
        direction: Int
    ) {
        if (
            transactionActive
        ) {
            return
        }

        val normalized =
            direction.coerceIn(
                -1,
                1
            )

        if (
            normalized ==
            0
        ) {
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
            id =
                id,
            index =
                index,
            current =
                current,
            previous =
                previous,
            next =
                next
        )

        manualDirection =
            0

        manualSongId =
            null
    }

    fun cancelManual() {
        manualDirection =
            0

        manualSongId =
            null
    }

    fun beginAutomatic(
        direction: Int
    ) {
        if (
            transactionActive
        ) {
            return
        }

        val normalized =
            direction.coerceIn(
                -1,
                1
            )

        if (
            normalized ==
            0
        ) {
            return
        }

        autoDirection =
            normalized

        autoAnimating =
            true
    }

    fun finishAutomatic(
        id: Long?,
        index: Int,
        current: Uri?,
        previous: Uri?,
        next: Uri?
    ) {
        adoptWindow(
            id =
                id,
            index =
                index,
            current =
                current,
            previous =
                previous,
            next =
                next
        )

        autoDirection =
            0

        autoAnimating =
            false
    }

    /*
     * For a real external queue jump where the new item is not
     * one of the frozen adjacent covers.
     */
    fun adoptExternalWindow(
        id: Long?,
        index: Int,
        current: Uri?,
        previous: Uri?,
        next: Uri?
    ) {
        if (
            transactionActive
        ) {
            return
        }

        adoptWindow(
            id =
                id,
            index =
                index,
            current =
                current,
            previous =
                previous,
            next =
                next
        )
    }

    private fun adoptWindow(
        id: Long?,
        index: Int,
        current: Uri?,
        previous: Uri?,
        next: Uri?
    ) {
        visualSongId =
            id

        visualIndex =
            index

        visualCurrent =
            current

        visualPrevious =
            previous

        visualNext =
            next
    }
}
