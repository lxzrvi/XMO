package com.xmo.music.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Library {
    suspend fun songs(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val audio = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val albumArt = "content://media/external/audio/albumart"
        val out = mutableListOf<Song>()

        val columns = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )

        context.contentResolver.query(
            audio,
            columns,
            "${MediaStore.Audio.Media.IS_MUSIC}!=0",
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { c ->
            val id = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val title = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val album = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val duration = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (c.moveToNext()) {
                val songId = c.getLong(id)
                val artId = c.getLong(albumId)

                out += Song(
                    id = songId,
                    title = c.getString(title).orEmpty().ifBlank { "Unknown title" },
                    artist = c.getString(artist).orEmpty()
                        .takeUnless { it.isBlank() || it == "<unknown>" }
                        ?: "Unknown artist",
                    album = c.getString(album).orEmpty()
                        .takeUnless { it.isBlank() || it == "<unknown>" }
                        ?: "Unknown album",
                    duration = c.getLong(duration),
                    uri = ContentUris.withAppendedId(audio, songId),
                    artwork = artId.takeIf { it > 0 }?.let {
                        ContentUris.withAppendedId(
                            android.net.Uri.parse(albumArt),
                            it
                        )
                    }
                )
            }
        }

        out
    }
}
