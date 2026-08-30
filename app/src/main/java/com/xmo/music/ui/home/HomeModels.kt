package com.xmo.music.ui

import androidx.compose.ui.graphics.Color
import com.xmo.music.data.Song

internal data class HomeSectionModel(
    val id: String,
    val title: String,
    val icon: Int,
    val tint: Color? = null
)

internal sealed interface HomeLayer {
    data object Menu : HomeLayer
    data object Scanner : HomeLayer

    data class SongList(
        val title: String,
        val source: String,
        val category: Boolean,
        val songs: List<Song>
    ) : HomeLayer
}
