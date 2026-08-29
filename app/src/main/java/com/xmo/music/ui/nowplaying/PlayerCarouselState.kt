package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
internal class PlayerCarouselState {

    val x =
        Animatable(0f)

    var width: Float =
        1f

    /*
     *  0 = no manual transaction
     *  1 = manually moving to next
     * -1 = manually moving to previous
     */
    var manualDirection by
        mutableIntStateOf(0)

    /*
     * Song that was current when the manual transaction began.
     * Media3 currentSongId changing away from this ID confirms
     * the real playback transition.
     */
    var manualSongId by
        mutableStateOf<Long?>(null)

    /*
     * True while a Media3-driven automatic/external song change
     * is being visually animated.
     */
    var autoAnimating by
        mutableStateOf(false)

    val transactionActive: Boolean
        get() =
            manualDirection != 0 ||
                autoAnimating
}
