package com.xmo.music.data

import android.content.Context
import android.net.Uri
import java.io.DataInputStream
import java.io.DataOutputStream

object HomeCache {
    private const val VERSION = 1
    private const val FILE_NAME = "xmo_library.cache"
    private const val REMOVED_RECENTS = "xmo_removed_recents"

    fun readSongs(
        context: Context
    ): List<Song> {
        val file =
            context.filesDir.resolve(FILE_NAME)

        if (!file.exists()) {
            return emptyList()
        }

        return runCatching {
            DataInputStream(
                file.inputStream().buffered()
            ).use { input ->
                if (input.readInt() != VERSION) {
                    return@use emptyList()
                }

                val count =
                    input.readInt()
                        .coerceIn(0, 100_000)

                buildList(count) {
                    repeat(count) {
                        add(readSong(input))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun writeSongs(
        context: Context,
        songs: List<Song>
    ) {
        val target =
            context.filesDir.resolve(FILE_NAME)

        val temporary =
            context.filesDir.resolve(
                "$FILE_NAME.tmp"
            )

        runCatching {
            DataOutputStream(
                temporary.outputStream().buffered()
            ).use { output ->
                output.writeInt(VERSION)
                output.writeInt(songs.size)

                songs.forEach {
                    writeSong(output, it)
                }
            }

            if (target.exists()) {
                target.delete()
            }

            if (!temporary.renameTo(target)) {
                temporary.copyTo(
                    target,
                    overwrite = true
                )
                temporary.delete()
            }
        }.onFailure {
            temporary.delete()
        }
    }

    fun removedRecentIds(
        context: Context
    ): Set<Long> {
        val prefs =
            context.getSharedPreferences(
                REMOVED_RECENTS,
                Context.MODE_PRIVATE
            )

        return prefs
            .getString("ids", null)
            .orEmpty()
            .split(",")
            .mapNotNull {
                it.toLongOrNull()
            }
            .toSet()
    }

    fun removeRecent(
        context: Context,
        songId: Long
    ) {
        val ids =
            removedRecentIds(context) +
                songId

        saveRemovedRecentIds(
            context,
            ids
        )
    }

    fun restoreRecent(
        context: Context,
        songId: Long
    ) {
        val ids =
            removedRecentIds(context) -
                songId

        saveRemovedRecentIds(
            context,
            ids
        )
    }

    fun filterRecent(
        context: Context,
        recent: List<RecentPlay>
    ): List<RecentPlay> {
        val removed =
            removedRecentIds(context)

        if (removed.isEmpty()) {
            return recent
        }

        return recent.filterNot {
            it.songId in removed
        }
    }

    private fun saveRemovedRecentIds(
        context: Context,
        ids: Set<Long>
    ) {
        context.getSharedPreferences(
            REMOVED_RECENTS,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                "ids",
                ids.sorted()
                    .joinToString(",")
            )
            .apply()
    }

    private fun writeSong(
        output: DataOutputStream,
        song: Song
    ) {
        output.writeLong(song.id)
        output.writeUTF(song.title)
        output.writeUTF(song.artist)
        output.writeUTF(song.album)
        output.writeLong(song.albumId)
        output.writeLong(song.duration)
        output.writeUTF(song.uri.toString())
        writeNullable(output, song.artwork?.toString())
        writeNullable(output, song.albumArtist)

        val metadata = song.metadata

        writeNullableInt(output, metadata.trackNumber)
        writeNullableInt(output, metadata.discNumber)
        writeNullableInt(output, metadata.year)
        writeNullable(output, metadata.genre)
        writeNullable(output, metadata.composer)
        writeNullable(output, metadata.writer)
        writeNullable(output, metadata.mimeType)
        writeNullable(output, metadata.fileName)
        writeNullable(output, metadata.relativePath)
        writeNullable(output, metadata.absolutePath)
        writeNullableLong(output, metadata.sizeBytes)
        writeNullableLong(output, metadata.dateAddedSeconds)
        writeNullableLong(output, metadata.dateModifiedSeconds)
        writeNullableInt(output, metadata.bitrate)
        writeNullableInt(output, metadata.sampleRate)
        writeNullableInt(output, metadata.channelCount)
    }

    private fun readSong(
        input: DataInputStream
    ): Song {
        val id = input.readLong()
        val title = input.readUTF()
        val artist = input.readUTF()
        val album = input.readUTF()
        val albumId = input.readLong()
        val duration = input.readLong()
        val uri = Uri.parse(input.readUTF())
        val artwork =
            readNullable(input)?.let(Uri::parse)

        val albumArtist =
            readNullable(input)

        val metadata =
            SongMetadata(
                trackNumber = readNullableInt(input),
                discNumber = readNullableInt(input),
                year = readNullableInt(input),
                genre = readNullable(input),
                composer = readNullable(input),
                writer = readNullable(input),
                mimeType = readNullable(input),
                fileName = readNullable(input),
                relativePath = readNullable(input),
                absolutePath = readNullable(input),
                sizeBytes = readNullableLong(input),
                dateAddedSeconds = readNullableLong(input),
                dateModifiedSeconds = readNullableLong(input),
                bitrate = readNullableInt(input),
                sampleRate = readNullableInt(input),
                channelCount = readNullableInt(input)
            )

        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            albumId = albumId,
            duration = duration,
            uri = uri,
            artwork = artwork,
            albumArtist = albumArtist,
            metadata = metadata
        )
    }

    private fun writeNullable(
        output: DataOutputStream,
        value: String?
    ) {
        output.writeBoolean(value != null)

        if (value != null) {
            output.writeUTF(value)
        }
    }

    private fun readNullable(
        input: DataInputStream
    ): String? =
        if (input.readBoolean()) {
            input.readUTF()
        } else {
            null
        }

    private fun writeNullableInt(
        output: DataOutputStream,
        value: Int?
    ) {
        output.writeBoolean(value != null)

        if (value != null) {
            output.writeInt(value)
        }
    }

    private fun readNullableInt(
        input: DataInputStream
    ): Int? =
        if (input.readBoolean()) {
            input.readInt()
        } else {
            null
        }

    private fun writeNullableLong(
        output: DataOutputStream,
        value: Long?
    ) {
        output.writeBoolean(value != null)

        if (value != null) {
            output.writeLong(value)
        }
    }

    private fun readNullableLong(
        input: DataInputStream
    ): Long? =
        if (input.readBoolean()) {
            input.readLong()
        } else {
            null
        }
}
