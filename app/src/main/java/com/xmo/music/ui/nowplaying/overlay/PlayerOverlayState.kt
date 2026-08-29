package com.xmo.music.ui.nowplaying

internal sealed interface PlayerOverlay {
    data object Options : PlayerOverlay
    data object Queue : PlayerOverlay
    data object Sleep : PlayerOverlay
    data object Details : PlayerOverlay
    data object Artist : PlayerOverlay
}

internal data class PopMessage(
    val text: String,
    val key: Long = System.nanoTime()
)
