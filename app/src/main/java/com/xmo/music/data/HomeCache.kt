package com.xmo.music.data

import android.content.Context
import android.net.Uri
import java.io.DataInputStream
import java.io.DataOutputStream

object HomeCache {
    private const val VERSION = 1
    private const val FILE_NAME = "xmo_library.cache"
    private const val PREFS = "xmo_home_cache"

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

        val temp =
            context.filesDir.resolve(
                "$FILE_NAME.tmp"
            )

        runCatching {
            DataOutputStream(
                temp.outputStream().buffered()
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

            if (!temp.renameTo(target)) {
                temp.copyTo(
                    target,
                    overwrite = true
                )
                temp.delete()
            }
        }.onFailure {
            temp.delete()
        }
    }

    fun homeMode(
        context: Context
    ): String =
        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).getString(
            "home_mode",
            "Home"
        ) ?: "Home"

    fun saveHomeMode(
        context: Context,
        mode: String
    ) {
        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                "home_mode",
                mode
            )
            .apply()
    }

    fun removedRecentIds(
        context: Context
    ): Set<Long> =
        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )
            .getString(
                "removed_recents",
                null
            )
            .orEmpty()
            .split(",")
            .mapNotNull {
                it.toLongOrNull()
            }
            .toSet()

    fun removeRecent(
        context: Context,
        songId: Long
    ) {
        saveRemovedRecentIds(
            context,
            removedRecentIds(context) + songId
        )
    }

    fun restoreRecent(
        context: Context,
        songId: Long
    ) {
        saveRemovedRecentIds(
            context,
            removedRecentIds(context) - songId
        )
    }

    fun filterRecent(
        context: Context,
        recent: List<RecentPlay>
    ): List<RecentPlay> {
        val removed =
            removedRecentIds(context)

        return if (removed.isEmpty()) {
            recent
        } else {
            recent.filterNot {
                it.songId in removed
            }
        }
    }

    private fun saveRemovedRecentIds(
        context: Context,
        ids: Set<Long>
    ) {
        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                "removed_recents",
                ids.sorted().joinToString(",")
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
        writeString(output, song.artwork?.toString())
        writeString(output, song.albumArtist)

        val metadata = song.metadata

        writeInt(output, metadata.trackNumber)
        writeInt(output, metadata.discNumber)
        writeInt(output, metadata.year)
        writeString(output, metadata.genre)
        writeString(output, metadata.composer)
        writeString(output, metadata.writer)
        writeString(output, metadata.mimeType)
        writeString(output, metadata.fileName)
        writeString(output, metadata.relativePath)
        writeString(output, metadata.absolutePath)
        writeLong(output, metadata.sizeBytes)
        writeLong(output, metadata.dateAddedSeconds)
        writeLong(output, metadata.dateModifiedSeconds)
        writeInt(output, metadata.bitrate)
        writeInt(output, metadata.sampleRate)
        writeInt(output, metadata.channelCount)
    }

    private fun readSong(
        input: DataInputStream
    ): Song =
        Song(
            id = input.readLong(),
            title = input.readUTF(),
            artist = input.readUTF(),
            album = input.readUTF(),
            albumId = input.readLong(),
            duration = input.readLong(),
            uri = Uri.parse(input.readUTF()),
            artwork =
                readString(input)?.let(Uri::parse),
            albumArtist = readString(input),
            metadata =
                SongMetadata(
                    trackNumber = readInt(input),
                    discNumber = readInt(input),
                    year = readInt(input),
                    genre = readString(input),
                    composer = readString(input),
                    writer = readString(input),
                    mimeType = readString(input),
                    fileName = readString(input),
                    relativePath = readString(input),
                    absolutePath = readString(input),
                    sizeBytes = readLong(input),
                    dateAddedSeconds = readLong(input),
                    dateModifiedSeconds = readLong(input),
                    bitrate = readInt(input),
                    sampleRate = readInt(input),
                    channelCount = readInt(input)
                )
        )

    private fun writeString(
        output: DataOutputStream,
        value: String?
    ) {
        output.writeBoolean(value != null)
        value?.let(output::writeUTF)
    }

    private fun readString(
        input: DataInputStream
    ): String? =
        if (input.readBoolean()) {
            input.readUTF()
        } else {
            null
        }

    private fun writeInt(
        output: DataOutputStream,
        value: Int?
    ) {
        output.writeBoolean(value != null)
        value?.let(output::writeInt)
    }

    private fun readInt(
        input: DataInputStream
    ): Int? =
        if (input.readBoolean()) {
            input.readInt()
        } else {
            null
        }

    private fun writeLong(
        output: DataOutputStream,
        value: Long?
    ) {
        output.writeBoolean(value != null)
        value?.let(output::writeLong)
    }

    private fun readLong(
        input: DataInputStream
    ): Long? =
        if (input.readBoolean()) {
            input.readLong()
        } else {
            null
        }
}
