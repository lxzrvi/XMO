package com.xmo.music.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(
    name = "xmo"
)

data class XmoProfile(
    val name: String = "XMO User",
    val avatarUri: String? = null,
    val avatarIndex: Int = 0
)

object Store {

    /*
     * =========================================================
     * EXISTING KEYS
     *
     * Never rename these. Existing installations keep their
     * current data.
     * =========================================================
     */

    private val orderKey =
        stringPreferencesKey(
            "home_order"
        )

    private val catsKey =
        stringPreferencesKey(
            "categories"
        )

    private val searchHistoryKey =
        stringPreferencesKey(
            "search_history"
        )

    private val setupCompleteKey =
        booleanPreferencesKey(
            "setup_complete"
        )

    private val profileNameKey =
        stringPreferencesKey(
            "profile_name"
        )

    private val profileAvatarUriKey =
        stringPreferencesKey(
            "profile_avatar_uri"
        )

    private val profileAvatarIndexKey =
        intPreferencesKey(
            "profile_avatar_index"
        )

    /*
     * =========================================================
     * APPEARANCE
     * =========================================================
     */

    private val themeModeKey =
        stringPreferencesKey(
            "theme_mode"
        )

    private val accentModeKey =
        stringPreferencesKey(
            "accent_mode"
        )

    private val customAccentArgbKey =
        longPreferencesKey(
            "custom_accent_argb"
        )

    private val customAccentLightnessKey =
        floatPreferencesKey(
            "custom_accent_lightness"
        )

    private val customAccentAlphaKey =
        floatPreferencesKey(
            "custom_accent_alpha"
        )

    /*
     * =========================================================
     * FAVORITES / RECENTS
     * =========================================================
     */

    private val likedSongIdsKey =
        stringPreferencesKey(
            "liked_song_ids"
        )

    private val recentPlaysKey =
        stringPreferencesKey(
            "recent_plays"
        )

    /*
     * =========================================================
     * LOCAL LYRICS
     * =========================================================
     */

    private val lyricsFilesKey =
        stringPreferencesKey(
            "lyrics_files"
        )

    /*
     * =========================================================
     * LIBRARY
     * =========================================================
     */

    private val ignoreShortAudioKey =
        booleanPreferencesKey(
            "ignore_short_audio"
        )

    private val minimumDurationKey =
        longPreferencesKey(
            "minimum_audio_duration"
        )

    /*
     * =========================================================
     * PLAYBACK
     * =========================================================
     */

    private val playbackSpeedKey =
        floatPreferencesKey(
            "playback_speed"
        )

    private val playbackPitchKey =
        floatPreferencesKey(
            "playback_pitch"
        )

    private val repeatModeKey =
        intPreferencesKey(
            "repeat_mode"
        )

    private val shuffleEnabledKey =
        booleanPreferencesKey(
            "shuffle_enabled"
        )

    /*
     * =========================================================
     * GENERAL FUNCTIONAL SETTINGS
     * =========================================================
     */

    private val resumeHeadphonesKey =
        booleanPreferencesKey(
            "resume_headphones"
        )

    /*
     * Fixed built-in category order.
     */
    val defaults =
        listOf(
            "songs",
            "albums",
            "liked",
            "artists"
        )

    /*
     * =========================================================
     * HOME ORDER
     * =========================================================
     */

    suspend fun order(
        context: Context
    ): List<String> {
        val raw =
            context.dataStore
                .data
                .first()[orderKey]
                ?: return defaults

        return raw
            .split("|")
            .map {
                it.trim()
            }
            .filter {
                it.isNotEmpty()
            }
            .distinct()
            .ifEmpty {
                defaults
            }
    }

    suspend fun saveOrder(
        context: Context,
        order: List<String>
    ) {
        val clean =
            order
                .filter {
                    it.isNotBlank()
                }
                .distinct()

        context.dataStore.edit {
            it[orderKey] =
                clean.joinToString(
                    "|"
                )
        }
    }

    /*
     * =========================================================
     * CUSTOM CATEGORIES
     * =========================================================
     */

    suspend fun categories(
        context: Context
    ): List<UserCategory> {
        val raw =
            context.dataStore
                .data
                .first()[catsKey]
                ?: return emptyList()

        return decodeCategories(
            raw
        )
    }

    suspend fun saveCategories(
        context: Context,
        cats: List<UserCategory>
    ) {
        context.dataStore.edit {
            it[catsKey] =
                encodeCategories(
                    cats
                )
        }
    }

    /*
     * Add/remove membership without requiring UI code to perform
     * its own read/modify/write sequence.
     */
    suspend fun setSongInCategory(
        context: Context,
        categoryId: String,
        songId: Long,
        added: Boolean
    ): List<UserCategory> {
        val prefs =
            context.dataStore
                .data
                .first()

        val current =
            decodeCategories(
                prefs[catsKey]
                    .orEmpty()
            )

        val next =
            current.map { category ->

                if (
                    category.id !=
                    categoryId
                ) {
                    category
                } else {
                    category.copy(
                        songIds =
                            if (added) {
                                category.songIds +
                                    songId
                            } else {
                                category.songIds -
                                    songId
                            }
                    )
                }
            }

        context.dataStore.edit {
            it[catsKey] =
                encodeCategories(
                    next
                )
        }

        return next
    }

    suspend fun addSongToCategory(
        context: Context,
        categoryId: String,
        songId: Long
    ): List<UserCategory> =
        setSongInCategory(
            context,
            categoryId,
            songId,
            true
        )

    suspend fun removeSongFromCategory(
        context: Context,
        categoryId: String,
        songId: Long
    ): List<UserCategory> =
        setSongInCategory(
            context,
            categoryId,
            songId,
            false
        )

    private fun encodeCategories(
        cats: List<UserCategory>
    ): String =
        cats.joinToString(
            "\n"
        ) { category ->

            val safeId =
                category.id
                    .replace(
                        ";",
                        ""
                    )
                    .replace(
                        "\n",
                        ""
                    )

            val safeName =
                category.name
                    .replace(
                        ";",
                        ""
                    )
                    .replace(
                        "\n",
                        " "
                    )
                    .trim()
                    .take(24)

            "$safeId;$safeName;${category.icon};${
                category.songIds
                    .sorted()
                    .joinToString(",")
            }"
        }

    private fun decodeCategories(
        raw: String
    ): List<UserCategory> =
        raw
            .lines()
            .mapNotNull { line ->

                val parts =
                    line.split(
                        ";",
                        limit = 4
                    )

                if (
                    parts.size < 3 ||
                    parts[0].isBlank()
                ) {
                    return@mapNotNull null
                }

                UserCategory(
                    id =
                        parts[0],

                    name =
                        parts[1]
                            .trim()
                            .ifBlank {
                                "Category"
                            },

                    icon =
                        parts[2]
                            .toIntOrNull()
                            ?: 0,

                    songIds =
                        parts
                            .getOrNull(3)
                            .orEmpty()
                            .split(",")
                            .mapNotNull {
                                it.trim()
                                    .toLongOrNull()
                            }
                            .toSet()
                )
            }
            .distinctBy {
                it.id
            }

    /*
     * =========================================================
     * SEARCH HISTORY
     * =========================================================
     */

    suspend fun searchHistory(
        context: Context
    ): List<String> {
        val raw =
            context.dataStore
                .data
                .first()[searchHistoryKey]
                ?: return emptyList()

        return decodeSearchHistory(
            raw
        )
    }

    suspend fun addSearch(
        context: Context,
        query: String
    ): List<String> {
        val clean =
            query
                .trim()
                .replace(
                    "\n",
                    " "
                )
                .take(120)

        if (
            clean.isBlank()
        ) {
            return searchHistory(
                context
            )
        }

        var result =
            emptyList<String>()

        context.dataStore.edit { prefs ->

            val current =
                decodeSearchHistory(
                    prefs[searchHistoryKey]
                        .orEmpty()
                )

            result =
                buildList {
                    add(clean)

                    current.forEach {
                        if (
                            !it.equals(
                                clean,
                                ignoreCase = true
                            )
                        ) {
                            add(it)
                        }
                    }
                }
                    .take(30)

            prefs[searchHistoryKey] =
                result.joinToString(
                    "\n"
                )
        }

        return result
    }

    suspend fun removeSearch(
        context: Context,
        query: String
    ): List<String> {
        var result =
            emptyList<String>()

        context.dataStore.edit { prefs ->

            result =
                decodeSearchHistory(
                    prefs[searchHistoryKey]
                        .orEmpty()
                )
                    .filterNot {
                        it.equals(
                            query,
                            ignoreCase = true
                        )
                    }

            if (
                result.isEmpty()
            ) {
                prefs.remove(
                    searchHistoryKey
                )
            } else {
                prefs[searchHistoryKey] =
                    result.joinToString(
                        "\n"
                    )
            }
        }

        return result
    }

    suspend fun clearSearchHistory(
        context: Context
    ) {
        context.dataStore.edit {
            it.remove(
                searchHistoryKey
            )
        }
    }

    private fun decodeSearchHistory(
        raw: String
    ): List<String> =
        raw
            .lines()
            .map {
                it.trim()
            }
            .filter {
                it.isNotEmpty()
            }
            .distinctBy {
                it.lowercase()
            }
            .take(30)

    /*
     * =========================================================
     * SETUP
     * =========================================================
     */

    suspend fun setupComplete(
        context: Context
    ): Boolean =
        context.dataStore
            .data
            .first()[setupCompleteKey]
            ?: false

    suspend fun setSetupComplete(
        context: Context,
        complete: Boolean
    ) {
        context.dataStore.edit {
            it[setupCompleteKey] =
                complete
        }
    }

    /*
     * =========================================================
     * PROFILE
     * =========================================================
     */

    suspend fun profile(
        context: Context
    ): XmoProfile {
        val prefs =
            context.dataStore
                .data
                .first()

        return XmoProfile(
            name =
                prefs[profileNameKey]
                    ?.trim()
                    ?.takeIf {
                        it.isNotEmpty()
                    }
                    ?: "XMO User",

            avatarUri =
                prefs[profileAvatarUriKey]
                    ?.trim()
                    ?.takeIf {
                        it.isNotEmpty()
                    },

            avatarIndex =
                (
                    prefs[profileAvatarIndexKey]
                        ?: 0
                    )
                    .coerceIn(
                        0,
                        5
                    )
        )
    }

    suspend fun saveProfile(
        context: Context,
        profile: XmoProfile
    ) {
        context.dataStore.edit {
            writeProfile(
                it,
                profile
            )
        }
    }

    suspend fun finishSetup(
        context: Context,
        profile: XmoProfile
    ) {
        context.dataStore.edit {
            writeProfile(
                it,
                profile
            )

            it[setupCompleteKey] =
                true
        }
    }

    private fun writeProfile(
        prefs:
            androidx.datastore.preferences.core
                .MutablePreferences,
        profile: XmoProfile
    ) {
        prefs[profileNameKey] =
            profile.name
                .trim()
                .ifBlank {
                    "XMO User"
                }
                .take(32)

        prefs[profileAvatarIndexKey] =
            profile.avatarIndex
                .coerceIn(
                    0,
                    5
                )

        val uri =
            profile.avatarUri
                ?.trim()
                ?.takeIf {
                    it.isNotEmpty()
                }

        if (
            uri == null
        ) {
            prefs.remove(
                profileAvatarUriKey
            )
        } else {
            prefs[profileAvatarUriKey] =
                uri
        }
    }

    /*
     * =========================================================
     * APPEARANCE
     * =========================================================
     */

    suspend fun appearance(
        context: Context
    ): XmoAppearance {
        val prefs =
            context.dataStore
                .data
                .first()

        val theme =
            runCatching {
                ThemeMode.valueOf(
                    prefs[themeModeKey]
                        ?: ThemeMode.System.name
                )
            }.getOrDefault(
                ThemeMode.System
            )

        val accent =
            runCatching {
                AccentMode.valueOf(
                    prefs[accentModeKey]
                        ?: AccentMode.Red.name
                )
            }.getOrDefault(
                AccentMode.Red
            )

        return XmoAppearance(
            themeMode =
                theme,

            accentMode =
                accent,

            customAccent =
                CustomAccent(
                    argb =
                        prefs[customAccentArgbKey]
                            ?: 0xFFFF3B3BL,

                    lightness =
                        (
                            prefs[
                                customAccentLightnessKey
                            ] ?: .5f
                            )
                            .coerceIn(
                                0f,
                                1f
                            ),

                    alpha =
                        (
                            prefs[
                                customAccentAlphaKey
                            ] ?: 1f
                            )
                            .coerceIn(
                                0f,
                                1f
                            )
                )
        )
    }

    suspend fun saveAppearance(
        context: Context,
        appearance: XmoAppearance
    ) {
        context.dataStore.edit {
            it[themeModeKey] =
                appearance.themeMode.name

            it[accentModeKey] =
                appearance.accentMode.name

            it[customAccentArgbKey] =
                appearance
                    .customAccent
                    .argb

            it[customAccentLightnessKey] =
                appearance
                    .customAccent
                    .lightness
                    .coerceIn(
                        0f,
                        1f
                    )

            it[customAccentAlphaKey] =
                appearance
                    .customAccent
                    .alpha
                    .coerceIn(
                        0f,
                        1f
                    )
        }
    }

    /*
     * =========================================================
     * LIKED SONGS
     * =========================================================
     */

    suspend fun likedSongIds(
        context: Context
    ): Set<Long> {
        val raw =
            context.dataStore
                .data
                .first()[likedSongIdsKey]

        return decodeLongSet(
            raw
        )
    }

    suspend fun setLiked(
        context: Context,
        songId: Long,
        liked: Boolean
    ): Set<Long> {
        var result =
            emptySet<Long>()

        context.dataStore.edit { prefs ->

            val current =
                decodeLongSet(
                    prefs[likedSongIdsKey]
                )

            result =
                if (liked) {
                    current + songId
                } else {
                    current - songId
                }

            if (
                result.isEmpty()
            ) {
                prefs.remove(
                    likedSongIdsKey
                )
            } else {
                prefs[likedSongIdsKey] =
                    result
                        .sorted()
                        .joinToString(",")
            }
        }

        return result
    }

    suspend fun toggleLiked(
        context: Context,
        songId: Long
    ): Set<Long> {
        var result =
            emptySet<Long>()

        context.dataStore.edit { prefs ->

            val current =
                decodeLongSet(
                    prefs[likedSongIdsKey]
                )

            result =
                if (
                    songId in current
                ) {
                    current - songId
                } else {
                    current + songId
                }

            if (
                result.isEmpty()
            ) {
                prefs.remove(
                    likedSongIdsKey
                )
            } else {
                prefs[likedSongIdsKey] =
                    result
                        .sorted()
                        .joinToString(",")
            }
        }

        return result
    }

    /*
     * =========================================================
     * RECENTLY PLAYED
     * =========================================================
     */

    suspend fun recentPlays(
        context: Context
    ): List<RecentPlay> {
        val raw =
            context.dataStore
                .data
                .first()[recentPlaysKey]

        return decodeRecentPlays(
            raw
        )
    }

    suspend fun recordPlay(
        context: Context,
        songId: Long,
        playedAt: Long =
            System.currentTimeMillis()
    ): List<RecentPlay> {
        var result =
            emptyList<RecentPlay>()

        context.dataStore.edit { prefs ->

            val current =
                decodeRecentPlays(
                    prefs[recentPlaysKey]
                )

            /*
             * One latest entry per song. Replaying a song brings
             * it back to the beginning.
             */
            result =
                buildList {
                    add(
                        RecentPlay(
                            songId =
                                songId,

                            playedAt =
                                playedAt
                        )
                    )

                    current.forEach {
                        if (
                            it.songId !=
                            songId
                        ) {
                            add(it)
                        }
                    }
                }
                    .take(100)

            prefs[recentPlaysKey] =
                encodeRecentPlays(
                    result
                )
        }

        return result
    }

    suspend fun clearRecentPlays(
        context: Context
    ) {
        context.dataStore.edit {
            it.remove(
                recentPlaysKey
            )
        }
    }

    private fun decodeRecentPlays(
        raw: String?
    ): List<RecentPlay> =
        raw
            .orEmpty()
            .lines()
            .mapNotNull { line ->

                val parts =
                    line.split(
                        ",",
                        limit = 2
                    )

                val songId =
                    parts
                        .getOrNull(0)
                        ?.toLongOrNull()
                        ?: return@mapNotNull null

                val time =
                    parts
                        .getOrNull(1)
                        ?.toLongOrNull()
                        ?: 0L

                RecentPlay(
                    songId,
                    time
                )
            }
            .distinctBy {
                it.songId
            }
            .sortedByDescending {
                it.playedAt
            }
            .take(100)

    private fun encodeRecentPlays(
        recent: List<RecentPlay>
    ): String =
        recent
            .take(100)
            .joinToString(
                "\n"
            ) {
                "${it.songId},${it.playedAt}"
            }

    /*
     * =========================================================
     * USER-ATTACHED LOCAL LYRICS
     * =========================================================
     *
     * URI is a persisted local document URI, never a network URL.
     * =========================================================
     */

    suspend fun lyricsUri(
        context: Context,
        songId: Long
    ): String? {
        val raw =
            context.dataStore
                .data
                .first()[lyricsFilesKey]

        return decodeLyricsFiles(
            raw
        )[songId]
    }

    suspend fun lyricsFiles(
        context: Context
    ): Map<Long, String> {
        val raw =
            context.dataStore
                .data
                .first()[lyricsFilesKey]

        return decodeLyricsFiles(
            raw
        )
    }

    suspend fun saveLyricsUri(
        context: Context,
        songId: Long,
        uri: String?
    ) {
        context.dataStore.edit { prefs ->

            val current =
                decodeLyricsFiles(
                    prefs[lyricsFilesKey]
                )
                    .toMutableMap()

            val clean =
                uri
                    ?.trim()
                    ?.takeIf {
                        it.isNotEmpty()
                    }

            if (
                clean == null
            ) {
                current.remove(
                    songId
                )
            } else {
                current[songId] =
                    clean
            }

            if (
                current.isEmpty()
            ) {
                prefs.remove(
                    lyricsFilesKey
                )
            } else {
                prefs[lyricsFilesKey] =
                    current.entries
                        .sortedBy {
                            it.key
                        }
                        .joinToString(
                            "\n"
                        ) {
                            "${it.key}|${it.value}"
                        }
            }
        }
    }

    private fun decodeLyricsFiles(
        raw: String?
    ): Map<Long, String> =
        raw
            .orEmpty()
            .lines()
            .mapNotNull { line ->

                val divider =
                    line.indexOf(
                        '|'
                    )

                if (
                    divider <= 0
                ) {
                    return@mapNotNull null
                }

                val songId =
                    line.substring(
                        0,
                        divider
                    )
                        .toLongOrNull()
                        ?: return@mapNotNull null

                val uri =
                    line.substring(
                        divider + 1
                    )
                        .trim()

                if (
                    uri.isEmpty()
                ) {
                    null
                } else {
                    songId to uri
                }
            }
            .toMap()

    /*
     * =========================================================
     * LIBRARY SETTINGS
     * =========================================================
     */

    suspend fun libraryPreferences(
        context: Context
    ): LibraryPreferences {
        val prefs =
            context.dataStore
                .data
                .first()

        return LibraryPreferences(
            ignoreShortAudio =
                prefs[ignoreShortAudioKey]
                    ?: false,

            minimumDurationMs =
                (
                    prefs[minimumDurationKey]
                        ?: 30_000L
                    )
                    .coerceIn(
                        0L,
                        10L * 60L * 1000L
                    )
        )
    }

    suspend fun saveLibraryPreferences(
        context: Context,
        preferences: LibraryPreferences
    ) {
        context.dataStore.edit {
            it[ignoreShortAudioKey] =
                preferences.ignoreShortAudio

            it[minimumDurationKey] =
                preferences
                    .minimumDurationMs
                    .coerceIn(
                        0L,
                        10L * 60L * 1000L
                    )
        }
    }

    /*
     * =========================================================
     * PLAYBACK SETTINGS
     * =========================================================
     */

    suspend fun playbackPreferences(
        context: Context
    ): PlaybackPreferences {
        val prefs =
            context.dataStore
                .data
                .first()

        return PlaybackPreferences(
            playbackSpeed =
                (
                    prefs[playbackSpeedKey]
                        ?: 1f
                    )
                    .coerceIn(
                        .25f,
                        3f
                    ),

            playbackPitch =
                (
                    prefs[playbackPitchKey]
                        ?: 1f
                    )
                    .coerceIn(
                        .5f,
                        2f
                    )
        )
    }

    suspend fun savePlaybackPreferences(
        context: Context,
        preferences: PlaybackPreferences
    ) {
        context.dataStore.edit {
            it[playbackSpeedKey] =
                preferences
                    .playbackSpeed
                    .coerceIn(
                        .25f,
                        3f
                    )

            it[playbackPitchKey] =
                preferences
                    .playbackPitch
                    .coerceIn(
                        .5f,
                        2f
                    )
        }
    }

    suspend fun shuffleEnabled(
        context: Context
    ): Boolean =
        context.dataStore
            .data
            .first()[shuffleEnabledKey]
            ?: false

    suspend fun saveShuffleEnabled(
        context: Context,
        enabled: Boolean
    ) {
        context.dataStore.edit {
            it[shuffleEnabledKey] =
                enabled
        }
    }

    /*
     * Media3 repeat values:
     * 0 = OFF
     * 1 = ONE
     * 2 = ALL
     */
    suspend fun repeatMode(
        context: Context
    ): Int =
        (
            context.dataStore
                .data
                .first()[repeatModeKey]
                ?: 0
            )
            .coerceIn(
                0,
                2
            )

    suspend fun saveRepeatMode(
        context: Context,
        mode: Int
    ) {
        context.dataStore.edit {
            it[repeatModeKey] =
                mode.coerceIn(
                    0,
                    2
                )
        }
    }

    suspend fun resumeOnHeadphones(
        context: Context
    ): Boolean =
        context.dataStore
            .data
            .first()[resumeHeadphonesKey]
            ?: false

    suspend fun saveResumeOnHeadphones(
        context: Context,
        enabled: Boolean
    ) {
        context.dataStore.edit {
            it[resumeHeadphonesKey] =
                enabled
        }
    }

    /*
     * =========================================================
     * HELPERS
     * =========================================================
     */

    private fun decodeLongSet(
        raw: String?
    ): Set<Long> =
        raw
            .orEmpty()
            .split(",")
            .mapNotNull {
                it.trim()
                    .toLongOrNull()
            }
            .toSet()
}
