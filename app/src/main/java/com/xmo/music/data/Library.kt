package com.xmo.music.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Library {
    suspend fun songs(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val media = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val art = Uri.parse("content://media/external/audio/albumart")
        val out = mutableListOf<Song>()

        context.contentResolver.query(
            media,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
            ),
            "${MediaStore.Audio.Media.IS_MUSIC}!=0",
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE"
        )?.use { c ->
            val id = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val title = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val album = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val duration = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (c.moveToNext()) {
                val sid = c.getLong(id)
                val aid = c.getLong(albumId)

                out += Song(
                    sid,
                    c.getString(title).clean("Unknown title"),
                    c.getString(artist).clean("Unknown artist"),
                    c.getString(album).clean("Unknown album"),
                    aid,
                    c.getLong(duration),
                    ContentUris.withAppendedId(media, sid),
                    aid.takeIf { it > 0 }?.let {
                        ContentUris.withAppendedId(art, it)
                    }
                )
            }
        }
        out
    }

    fun artists(songs: List<Song>) =
        songs.groupBy { it.artist }.map { Artist(it.key, it.value) }

    fun albums(songs: List<Song>) =
        songs.groupBy { it.albumId }.map { (_, tracks) ->
            Album(
                tracks.first().albumId,
                tracks.first().album,
                tracks.first().artist,
                tracks,
                tracks.firstNotNullOfOrNull { it.artwork }
            )
        }

    private fun String?.clean(fallback: String) =
        this?.takeUnless { it.isBlank() || it == "<unknown>" } ?: fallback
}
