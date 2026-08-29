package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

@Stable
internal class PlayerCarouselState {
    val x =
        Animatable(0f)

    var width =
        1f

    var manualDirection by
        mutableIntStateOf(0)

    var manualSongId by
        mutableStateOf<Long?>(null)

    var autoAnimating by
        mutableStateOf(false)

    val transactionActive: Boolean
        get() =
            manualDirection != 0 ||
                autoAnimating
}
