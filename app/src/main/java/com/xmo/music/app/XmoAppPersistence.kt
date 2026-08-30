package com.xmo.music.app

import android.content.Context
import com.xmo.music.data.Library
import com.xmo.music.data.Store
import com.xmo.music.player.PlaybackState
import com.xmo.music.player.XmoPlayer

internal class XmoAppPersistence(
    private val context: Context,
    private val state: XmoAppState
) {
    /*
     * =========================================================
     * INITIAL LOAD
     * =========================================================
     */

    suspend fun loadInitial() {
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
            Store.recentPlays(context)

        state.lyricsFiles =
            Store.lyricsFiles(context)

        state.libraryPreferences =
            Store.libraryPreferences(
                context
            )

        state.playbackPreferences =
            Store.playbackPreferences(
                context
            )

        state.resumeOnHeadphones =
            Store.resumeOnHeadphones(
                context
            )

        state.setupComplete =
            Store.setupComplete(
                context
            )

        state.loaded = true
    }

    /*
     * =========================================================
     * LIBRARY
     * =========================================================
     */

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
                Library.songs(context)

            state.songs =
                if (
                    state
                        .libraryPreferences
                        .ignoreShortAudio
                ) {
                    result.filter {
                        it.duration >=
                            state
                                .libraryPreferences
                                .minimumDurationMs
                    }
                } else {
                    result
                }
        } finally {
            state.scanning = false
        }
    }

    /*
     * =========================================================
     * PLAYER PREFERENCES
     * =========================================================
     */

    fun applyPlaybackPreferences(
        player: XmoPlayer
    ) {
        player.setPlaybackParameters(
            speed =
                state
                    .playbackPreferences
                    .playbackSpeed,
            pitch =
                state
                    .playbackPreferences
                    .playbackPitch
        )
    }

    suspend fun restorePlayerModes(
        player: XmoPlayer
    ) {
        player.setShuffle(
            Store.shuffleEnabled(
                context
            )
        )

        player.setRepeatMode(
            Store.repeatMode(
                context
            )
        )

        state.playerPersistenceReady =
            true
    }

    suspend fun persistPlayerModes(
        playback: PlaybackState
    ) {
        if (
            !state.playerPersistenceReady
        ) {
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

    /*
     * =========================================================
     * RECENT PLAYBACK
     * =========================================================
     */

    suspend fun recordCurrentSong(
        songId: Long
    ) {
        if (
            songId ==
            state.recordedSongId
        ) {
            return
        }

        state.recordedSongId =
            songId

        state.recentPlays =
            Store.recordPlay(
                context,
                songId
            )
    }

    /*
     * =========================================================
     * SMALL PERSISTENCE OPERATIONS
     * =========================================================
     */

    suspend fun reloadLyricsFiles() {
        state.lyricsFiles =
            Store.lyricsFiles(
                context
            )
    }
}
