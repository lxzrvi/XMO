package com.xmo.music.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Library {

    suspend fun songs(
        context: Context
    ): List<Song> =
        withContext(Dispatchers.IO) {

            val resolver =
                context.contentResolver

            val media =
                MediaStore.Audio.Media
                    .EXTERNAL_CONTENT_URI

            val albumArt =
                Uri.parse(
                    "content://media/external/audio/albumart"
                )

            /*
             * Keep the MediaStore query focused on columns which
             * Android exposes reliably for local audio.
             *
             * Deeper codec metadata is enriched afterwards from
             * the actual content URI.
             */
            val projection =
                arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.TRACK,
                    MediaStore.Audio.Media.YEAR,
                    MediaStore.Audio.Media.COMPOSER,
                    MediaStore.Audio.Media.MIME_TYPE,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.RELATIVE_PATH,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DATE_MODIFIED
                )

            val basicSongs =
                mutableListOf<Song>()

            resolver.query(
                media,
                projection,
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                null,
                "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
            )?.use { cursor ->

                val idColumn =
                    cursor.column(
                        MediaStore.Audio.Media._ID
                    )

                val titleColumn =
                    cursor.column(
                        MediaStore.Audio.Media.TITLE
                    )

                val artistColumn =
                    cursor.column(
                        MediaStore.Audio.Media.ARTIST
                    )

                val albumColumn =
                    cursor.column(
                        MediaStore.Audio.Media.ALBUM
                    )

                val albumIdColumn =
                    cursor.column(
                        MediaStore.Audio.Media.ALBUM_ID
                    )

                val durationColumn =
                    cursor.column(
                        MediaStore.Audio.Media.DURATION
                    )

                val trackColumn =
                    cursor.column(
                        MediaStore.Audio.Media.TRACK
                    )

                val yearColumn =
                    cursor.column(
                        MediaStore.Audio.Media.YEAR
                    )

                val composerColumn =
                    cursor.column(
                        MediaStore.Audio.Media.COMPOSER
                    )

                val mimeColumn =
                    cursor.column(
                        MediaStore.Audio.Media.MIME_TYPE
                    )

                val nameColumn =
                    cursor.column(
                        MediaStore.Audio.Media.DISPLAY_NAME
                    )

                val relativePathColumn =
                    cursor.column(
                        MediaStore.Audio.Media.RELATIVE_PATH
                    )

                val sizeColumn =
                    cursor.column(
                        MediaStore.Audio.Media.SIZE
                    )

                val addedColumn =
                    cursor.column(
                        MediaStore.Audio.Media.DATE_ADDED
                    )

                val modifiedColumn =
                    cursor.column(
                        MediaStore.Audio.Media.DATE_MODIFIED
                    )

                while (
                    cursor.moveToNext()
                ) {
                    if (
                        idColumn < 0
                    ) {
                        continue
                    }

                    val id =
                        cursor.getLong(
                            idColumn
                        )

                    val albumId =
                        cursor.longOrNull(
                            albumIdColumn
                        ) ?: 0L

                    val uri =
                        ContentUris
                            .withAppendedId(
                                media,
                                id
                            )

                    /*
                     * MediaStore TRACK commonly encodes:
                     *
                     * disc * 1000 + track
                     *
                     * Example:
                     * 2004 = disc 2, track 4.
                     */
                    val rawTrack =
                        cursor.intOrNull(
                            trackColumn
                        )

                    val mediaStoreDisc =
                        rawTrack
                            ?.takeIf {
                                it >= 1000
                            }
                            ?.div(1000)
                            ?.takeIf {
                                it > 0
                            }

                    val mediaStoreTrack =
                        rawTrack
                            ?.let {
                                if (it >= 1000) {
                                    it % 1000
                                } else {
                                    it
                                }
                            }
                            ?.takeIf {
                                it > 0
                            }

                    basicSongs +=
                        Song(
                            id = id,

                            title =
                                cursor.stringOrNull(
                                    titleColumn
                                ).clean(
                                    "Unknown title"
                                ),

                            artist =
                                cursor.stringOrNull(
                                    artistColumn
                                ).clean(
                                    "Unknown artist"
                                ),

                            album =
                                cursor.stringOrNull(
                                    albumColumn
                                ).clean(
                                    "Unknown album"
                                ),

                            albumId =
                                albumId,

                            duration =
                                cursor.longOrNull(
                                    durationColumn
                                )
                                    ?.coerceAtLeast(
                                        0L
                                    )
                                    ?: 0L,

                            uri =
                                uri,

                            artwork =
                                albumId
                                    .takeIf {
                                        it > 0L
                                    }
                                    ?.let {
                                        ContentUris
                                            .withAppendedId(
                                                albumArt,
                                                it
                                            )
                                    },

                            metadata =
                                SongMetadata(
                                    trackNumber =
                                        mediaStoreTrack,

                                    discNumber =
                                        mediaStoreDisc,

                                    year =
                                        cursor.intOrNull(
                                            yearColumn
                                        )
                                            ?.takeIf {
                                                it > 0
                                            },

                                    composer =
                                        cursor.stringOrNull(
                                            composerColumn
                                        ).meaningful(),

                                    mimeType =
                                        cursor.stringOrNull(
                                            mimeColumn
                                        ).meaningful(),

                                    fileName =
                                        cursor.stringOrNull(
                                            nameColumn
                                        ).meaningful(),

                                    relativePath =
                                        cursor.stringOrNull(
                                            relativePathColumn
                                        ).meaningful(),

                                    sizeBytes =
                                        cursor.longOrNull(
                                            sizeColumn
                                        )
                                            ?.takeIf {
                                                it >= 0L
                                            },

                                    dateAddedSeconds =
                                        cursor.longOrNull(
                                            addedColumn
                                        )
                                            ?.takeIf {
                                                it > 0L
                                            },

                                    dateModifiedSeconds =
                                        cursor.longOrNull(
                                            modifiedColumn
                                        )
                                            ?.takeIf {
                                                it > 0L
                                            }
                                )
                        )
                }
            }

            /*
             * Reading every file deeply during every normal
             * library scan would make startup unnecessarily
             * expensive.
             *
             * This pass adds metadata which MediaStore does not
             * reliably expose while keeping all work on IO.
             */
            basicSongs.map { song ->
                enrich(
                    context,
                    song
                )
            }
        }

    /*
     * Explicit single-song metadata refresh.
     *
     * Useful for details/lyrics screens without triggering a
     * complete MediaStore rescan.
     */
    suspend fun details(
        context: Context,
        song: Song
    ): Song =
        withContext(Dispatchers.IO) {
            enrich(
                context,
                song
            )
        }

    fun artists(
        songs: List<Song>
    ): List<Artist> =
        songs
            .groupBy {
                it.artist
            }
            .map {
                    (name, tracks) ->

                Artist(
                    name =
                        name,

                    songs =
                        tracks
                )
            }
            .sortedBy {
                it.name.lowercase()
            }

    fun albums(
        songs: List<Song>
    ): List<Album> =
        songs
            .groupBy {
                /*
                 * A missing/invalid album id must not merge every
                 * unknown album into one giant album.
                 */
                if (
                    it.albumId > 0L
                ) {
                    "id:${it.albumId}"
                } else {
                    "name:${it.album.lowercase()}|${it.artist.lowercase()}"
                }
            }
            .map {
                    (_, tracks) ->

                val first =
                    tracks.first()

                Album(
                    id =
                        first.albumId,

                    name =
                        first.album,

                    artist =
                        first.albumArtist
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: first.artist,

                    songs =
                        tracks.sortedWith(
                            compareBy<Song> {
                                it.metadata.discNumber
                                    ?: 1
                            }.thenBy {
                                it.metadata.trackNumber
                                    ?: Int.MAX_VALUE
                            }.thenBy {
                                it.title.lowercase()
                            }
                        ),

                    artwork =
                        tracks.firstNotNullOfOrNull {
                            it.artwork
                        }
                )
            }
            .sortedBy {
                it.name.lowercase()
            }

    private fun enrich(
        context: Context,
        song: Song
    ): Song {
        val retriever =
            MediaMetadataRetriever()

        return try {
            retriever.setDataSource(
                context,
                song.uri
            )

            val embeddedTitle =
                retriever.text(
                    MediaMetadataRetriever
                        .METADATA_KEY_TITLE
                )

            val embeddedArtist =
                retriever.text(
                    MediaMetadataRetriever
                        .METADATA_KEY_ARTIST
                )

            val embeddedAlbum =
                retriever.text(
                    MediaMetadataRetriever
                        .METADATA_KEY_ALBUM
                )

            val albumArtist =
                retriever.text(
                    MediaMetadataRetriever
                        .METADATA_KEY_ALBUMARTIST
                )

            val genre =
                retriever.text(
                    MediaMetadataRetriever
                        .METADATA_KEY_GENRE
                )

            val writer =
                retriever.text(
                    MediaMetadataRetriever
                        .METADATA_KEY_WRITER
                )

            val composer =
                retriever.text(
                    MediaMetadataRetriever
                        .METADATA_KEY_COMPOSER
                )

            val year =
                retriever
                    .text(
                        MediaMetadataRetriever
                            .METADATA_KEY_YEAR
                    )
                    ?.toIntOrNull()
                    ?.takeIf {
                        it > 0
                    }

            val bitrate =
                retriever
                    .text(
                        MediaMetadataRetriever
                            .METADATA_KEY_BITRATE
                    )
                    ?.toIntOrNull()
                    ?.takeIf {
                        it > 0
                    }

            val sampleRate =
                retriever
                    .text(
                        MediaMetadataRetriever
                            .METADATA_KEY_SAMPLERATE
                    )
                    ?.toIntOrNull()
                    ?.takeIf {
                        it > 0
                    }

            val channelCount =
                retriever
                    .text(
                        MediaMetadataRetriever
                            .METADATA_KEY_AUDIO_CHANNEL_COUNT
                    )
                    ?.toIntOrNull()
                    ?.takeIf {
                        it > 0
                    }

            val embeddedDuration =
                retriever
                    .text(
                        MediaMetadataRetriever
                            .METADATA_KEY_DURATION
                    )
                    ?.toLongOrNull()
                    ?.takeIf {
                        it >= 0L
                    }

            val track =
                retriever
                    .text(
                        MediaMetadataRetriever
                            .METADATA_KEY_CD_TRACK_NUMBER
                    )
                    .parseNumber()

            val disc =
                retriever
                    .text(
                        MediaMetadataRetriever
                            .METADATA_KEY_DISC_NUMBER
                    )
                    .parseNumber()

            /*
             * Android's platform MediaMetadataRetriever does not
             * expose a general ID3/Vorbis embedded-lyrics key.
             * Therefore lyrics are not fabricated here.
             *
             * A real parser/local .lrc attachment layer can fill
             * Song.embeddedLyrics later when actual lyric data
             * exists.
             */
            song.copy(
                title =
                    embeddedTitle
                        ?: song.title,

                artist =
                    embeddedArtist
                        ?: song.artist,

                album =
                    embeddedAlbum
                        ?: song.album,

                albumArtist =
                    albumArtist
                        ?: song.albumArtist,

                duration =
                    embeddedDuration
                        ?: song.duration,

                metadata =
                    song.metadata.copy(
                        trackNumber =
                            track
                                ?: song.metadata
                                    .trackNumber,

                        discNumber =
                            disc
                                ?: song.metadata
                                    .discNumber,

                        year =
                            year
                                ?: song.metadata.year,

                        genre =
                            genre
                                ?: song.metadata.genre,

                        composer =
                            composer
                                ?: song.metadata
                                    .composer,

                        writer =
                            writer
                                ?: song.metadata.writer,

                        bitrate =
                            bitrate
                                ?: song.metadata
                                    .bitrate,

                        sampleRate =
                            sampleRate
                                ?: song.metadata
                                    .sampleRate,

                        channelCount =
                            channelCount
                                ?: song.metadata
                                    .channelCount
                    )
            )
        } catch (_: Exception) {
            /*
             * Broken/unsupported files remain usable through
             * their MediaStore metadata.
             */
            song
        } finally {
            runCatching {
                retriever.release()
            }
        }
    }

    private fun android.database.Cursor.column(
        name: String
    ): Int =
        getColumnIndex(
            name
        )

    private fun android.database.Cursor.stringOrNull(
        column: Int
    ): String? {
        if (
            column < 0 ||
            isNull(column)
        ) {
            return null
        }

        return getString(
            column
        )
    }

    private fun android.database.Cursor.longOrNull(
        column: Int
    ): Long? {
        if (
            column < 0 ||
            isNull(column)
        ) {
            return null
        }

        return getLong(
            column
        )
    }

    private fun android.database.Cursor.intOrNull(
        column: Int
    ): Int? {
        if (
            column < 0 ||
            isNull(column)
        ) {
            return null
        }

        return getInt(
            column
        )
    }

    private fun String?.clean(
        fallback: String
    ): String =
        meaningful()
            ?: fallback

    private fun String?.meaningful():
        String? =
        this
            ?.trim()
            ?.takeUnless {
                it.isEmpty() ||
                    it.equals(
                        "<unknown>",
                        ignoreCase = true
                    ) ||
                    it.equals(
                        "unknown",
                        ignoreCase = true
                    )
            }

        private fun String?.parseNumber(): Int? {
            // existing code...
        }
    
        private fun MediaMetadataRetriever.text(
            key: Int
        ): String? =
            extractMetadata(key)
                ?.trim()
                ?.takeIf {
                    it.isNotEmpty() &&
                        !it.equals("<unknown>", ignoreCase = true) &&
                        !it.equals("unknown", ignoreCase = true)
                }
    }
}
