package com.xmo.music.data

import android.net.Uri

/*
 * One timed lyric line.
 *
 * timeMs == null:
 * plain / untimed lyric line.
 */
data class LyricLine(
    val timeMs: Long? = null,
    val text: String
)

/*
 * Lyrics discovered from the audio file itself or attached
 * locally by the user.
 */
data class SongLyrics(
    val lines: List<LyricLine> = emptyList(),
    val synced: Boolean = false,
    val source: String = ""
) {
    val isEmpty: Boolean
        get() = lines.isEmpty()

    val isNotEmpty: Boolean
        get() = lines.isNotEmpty()
}

/*
 * Detailed local metadata.
 *
 * Nullable fields mean that the actual file / MediaStore did
 * not provide that information. XMO does not invent metadata.
 */
data class SongMetadata(
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val year: Int? = null,
    val genre: String? = null,
    val composer: String? = null,
    val writer: String? = null,
    val mimeType: String? = null,
    val fileName: String? = null,
    val relativePath: String? = null,
    val absolutePath: String? = null,
    val sizeBytes: Long? = null,
    val dateAddedSeconds: Long? = null,
    val dateModifiedSeconds: Long? = null,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
    val channelCount: Int? = null
)

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val uri: Uri,
    val artwork: Uri?,

    /*
     * Extended local metadata.
     *
     * Defaults keep old persisted/runtime call sites compatible.
     */
    val albumArtist: String? = null,
    val metadata: SongMetadata = SongMetadata(),
    val embeddedLyrics: SongLyrics? = null
)

data class Artist(
    val name: String,
    val songs: List<Song>
) {
    /*
     * Genuine local artwork only.
     *
     * MediaStore generally does not expose artist portraits,
     * therefore a song/album artwork is used only when one
     * actually exists locally.
     */
    val artwork: Uri?
        get() =
            songs.firstNotNullOfOrNull {
                it.artwork
            }
}

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

/*
 * Persistent playback history entry.
 *
 * Song details are intentionally not duplicated here. The ID is
 * resolved against the latest MediaStore library so renamed or
 * updated local metadata remains current.
 */
data class RecentPlay(
    val songId: Long,
    val playedAt: Long
)

/*
 * App appearance is persisted independently from the resolved
 * Compose theme. SYSTEM resolves at runtime from Android.
 */
enum class ThemeMode {
    System,
    Dark,
    Light,
    Amoled
}

enum class AccentMode {
    Red,
    Blue,
    Custom
}

data class CustomAccent(
    /*
     * ARGB packed as Long so it can safely survive persistence.
     */
    val argb: Long = 0xFFFF3B3BL,
    val lightness: Float = 0.5f,
    val alpha: Float = 1f
)

data class XmoAppearance(
    val themeMode: ThemeMode = ThemeMode.System,
    val accentMode: AccentMode = AccentMode.Red,
    val customAccent: CustomAccent = CustomAccent()
)

/*
 * Functional library preferences.
 */
data class LibraryPreferences(
    val ignoreShortAudio: Boolean = false,
    val minimumDurationMs: Long = 30_000L
)

/*
 * Playback preferences which have real backend meaning.
 */
data class PlaybackPreferences(
    val playbackSpeed: Float = 1f,
    val playbackPitch: Float = 1f
)
