package com.xmo.music.data

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val uri: Uri,
    val artwork: Uri?
)

data class Artist(val name: String, val songs: List<Song>)

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val songs: List<Song>,
    val artwork: Uri?
)

data class UserCategory(
    val id: String,
    val name: String,
    val icon: Int,
    val songIds: Set<Long> = emptySet()
)
