package com.xmo.music.app

import android.content.Context
import com.xmo.music.data.HomeCache
import com.xmo.music.data.Library
import com.xmo.music.data.Store
import com.xmo.music.player.PlaybackState
import com.xmo.music.player.XmoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class XmoAppPersistence(
    private val context: Context,
    private val state: XmoAppState
) {
    suspend fun loadInitial() {
        val cachedSongs =
            withContext(Dispatchers.IO) {
                HomeCache.readSongs(context)
            }

        if (cachedSongs.isNotEmpty()) {
            state.songs = cachedSongs
        }

        state.profile =
            Store.profile(context)

        state.appearance =
            Store.appearance(context)

        state.categories =
            Store.categories(context)

        state.order =
            Store.order(context)

        state.likedSongIds =
            Store.likedSongIds(context)

        state.recentPlays =
            HomeCache.filterRecent(
                context,
                Store.recentPlays(context)
            )

        state.lyricsFiles =
            Store.lyricsFiles(context)

        state.libraryPreferences =
            Store.libraryPreferences(context)

        state.playbackPreferences =
            Store.playbackPreferences(context)

        state.resumeOnHeadphones =
            Store.resumeOnHeadphones(context)

        state.setupComplete =
            Store.setupComplete(context)

        state.loaded = true
    }

    suspend fun loadLibrary() {
        if (
            !state.allowed ||
            !state.setupComplete ||
            state.scanning
        ) {
            return
        }

        state.scanning = true

        try {
            val result =
                withContext(Dispatchers.IO) {
                    Library.songs(context)
                }

            val filtered =
                if (
                    state.libraryPreferences
                        .ignoreShortAudio
                ) {
                    result.filter {
                        it.duration >=
                            state.libraryPreferences
                                .minimumDurationMs
                    }
                } else {
                    result
                }

            state.songs = filtered

            withContext(Dispatchers.IO) {
                HomeCache.writeSongs(
                    context,
                    filtered
                )
            }
        } finally {
            state.scanning = false
        }
    }

    fun applyPlaybackPreferences(
        player: XmoPlayer
    ) {
        player.setPlaybackParameters(
            speed =
                state.playbackPreferences
                    .playbackSpeed,
            pitch =
                state.playbackPreferences
                    .playbackPitch
        )
    }

    suspend fun restorePlayerModes(
        player: XmoPlayer
    ) {
        player.setShuffle(
            Store.shuffleEnabled(context)
        )

        player.setRepeatMode(
            Store.repeatMode(context)
        )

        state.playerPersistenceReady = true
    }

    suspend fun persistPlayerModes(
        playback: PlaybackState
    ) {
        if (!state.playerPersistenceReady) {
            return
        }

        Store.saveShuffleEnabled(
            context,
            playback.shuffleEnabled
        )

        Store.saveRepeatMode(
            context,
            playback.repeatMode
        )
    }

    suspend fun recordCurrentSong(
        songId: Long
    ) {
        if (songId == state.recordedSongId) {
            return
        }

        state.recordedSongId = songId

        HomeCache.restoreRecent(
            context,
            songId
        )

        state.recentPlays =
            Store.recordPlay(
                context,
                songId
            )
    }

    suspend fun removeRecent(
        songId: Long
    ) {
        HomeCache.removeRecent(
            context,
            songId
        )

        state.recentPlays =
            state.recentPlays.filterNot {
                it.songId == songId
            }
    }

    suspend fun reloadLyricsFiles() {
        state.lyricsFiles =
            Store.lyricsFiles(context)
    }
}
