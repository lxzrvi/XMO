package com.xmo.music.app

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.xmo.music.data.Store
import com.xmo.music.data.UserCategory
import com.xmo.music.player.XmoPlayer
import java.util.UUID

internal fun buildXmoAppActions(
    context: Context,
    state: XmoAppState,
    player: XmoPlayer,
    scope: CoroutineScope,
    persistence: XmoAppPersistence,
    requestAudioPermission: () -> Unit
): XmoAppActions {

    fun currentSong() =
        state.songs
            .firstOrNull {
                it.id ==
                    player.state
                        .value
                        .currentSongId
            }
            ?: player.currentSong()

    fun toggleLike(
        songId: Long
    ) {
        scope.launch {
            state.likedSongIds =
                Store.toggleLiked(
                    context,
                    songId
                )
        }
    }

    fun updateCategoryMembership(
        songId: Long,
        categoryId: String,
        added: Boolean
    ) {
        scope.launch {
            state.categories =
                Store.setSongInCategory(
                    context = context,
                    categoryId = categoryId,
                    songId = songId,
                    added = added
                )
        }
    }

    fun createCategory(
        rawName: String
    ): UserCategory? {
        val name =
            rawName
                .trim()
                .replace(
                    "\n",
                    " "
                )
                .take(24)

        if (name.isBlank()) {
            return null
        }

        val song =
            currentSong()

        val category =
            UserCategory(
                id =
                    "cat_${UUID.randomUUID()}",
                name = name,
                icon =
                    state.categories.size %
                        4,
                songIds =
                    if (song != null) {
                        setOf(song.id)
                    } else {
                        emptySet()
                    }
            )

        val categories =
            state.categories +
                category

        val order =
            (
                state.order +
                    category.id
                )
                .distinct()

        state.categories =
            categories

        state.order =
            order

        scope.launch {
            Store.saveCategories(
                context,
                categories
            )

            Store.saveOrder(
                context,
                order
            )
        }

        return category
    }

    return XmoAppActions(
        requestAudioPermission =
            requestAudioPermission,

        refreshLibrary = {
            scope.launch {
                persistence.loadLibrary()
            }
        },

        selectTab = {
            state.tab = it
        },

        openProfile = {
            state.profileOpen = true
        },

        closeProfile = {
            state.profileOpen = false
        },

        saveProfile = {
            profile ->

            state.profile =
                profile

            state.profileOpen =
                false

            scope.launch {
                Store.saveProfile(
                    context,
                    profile
                )
            }
        },

        saveOrder = {
            order ->

            state.order =
                order

            scope.launch {
                Store.saveOrder(
                    context,
                    order
                )
            }
        },

        saveCategories = {
            categories ->

            state.categories =
                categories

            scope.launch {
                Store.saveCategories(
                    context,
                    categories
                )
            }
        },

        /*
         * =====================================================
         * GLOBAL SONG OPENING POLICY
         * =====================================================
         *
         * Home / Search / category / anywhere:
         *
         * play song
         * -> MiniPlayer
         * -> user decides when to open Now Playing
         */
        playSong = {
                song,
                source,
                isCategory,
                queue ->

            val index =
                queue.indexOfFirst {
                    it.id ==
                        song.id
                }

            if (index >= 0) {
                state.playingSource =
                    source

                state.playingSourceIsCategory =
                    isCategory

                player.play(
                    queue,
                    index
                )

                /*
                 * Never force Now Playing from a song-row tap.
                 */
                state.showNowPlaying =
                    false

                state.miniVisible =
                    true
            }
        },

        toggleLike = {
            song ->

            toggleLike(
                song.id
            )
        },

        setSongInCategory = {
                song,
                categoryId,
                added ->

            updateCategoryMembership(
                songId = song.id,
                categoryId = categoryId,
                added = added
            )
        },

        changeAppearance = {
            appearance ->

            state.appearance =
                appearance

            scope.launch {
                Store.saveAppearance(
                    context,
                    appearance
                )
            }
        },

        changeLibraryPreferences = {
            preferences ->

            state.libraryPreferences =
                preferences

            scope.launch {
                Store.saveLibraryPreferences(
                    context,
                    preferences
                )

                persistence.loadLibrary()
            }
        },

        changePlaybackPreferences = {
            preferences ->

            state.playbackPreferences =
                preferences

            player.setPlaybackParameters(
                speed =
                    preferences.playbackSpeed,
                pitch =
                    preferences.playbackPitch
            )

            scope.launch {
                Store.savePlaybackPreferences(
                    context,
                    preferences
                )
            }
        },

        changeResumeOnHeadphones = {
            enabled ->

            state.resumeOnHeadphones =
                enabled

            scope.launch {
                Store.saveResumeOnHeadphones(
                    context,
                    enabled
                )
            }
        },

        /*
         * =====================================================
         * MINIPLAYER
         * =====================================================
         */

        openNowPlayingFromMini = {
            /*
             * XmoMiniPlayer has already completed its downward
             * disappearance before this callback fires.
             */
            state.miniVisible =
                false

            state.showNowPlaying =
                true
        },

        closePlaybackFromMini = {
            player.closePlayback()

            state.miniVisible =
                false

            state.showNowPlaying =
                false
        },

        togglePlay = {
            player.togglePlayPause()
        },

        miniPrevious = {
            player.previousItem()
        },

        next = {
            player.next()
        },

        /*
         * =====================================================
         * NOW PLAYING
         * =====================================================
         */

        nowPlayingOpened = {
            state.miniVisible =
                false
        },

        refreshPosition = {
            player.refreshPosition()
        },

        previous = {
            player.previous()
        },

        previousItem = {
            player.previousItem()
        },

        playQueueIndex = {
            index ->

            val queue =
                player.queue()

            if (
                index in
                queue.indices
            ) {
                /*
                 * Required real queue callback behavior.
                 */
                player.play(
                    queue,
                    index
                )
            }
        },

        seekTo = {
            player.seekTo(it)
        },

        toggleCurrentLike = {
            player.state
                .value
                .currentSongId
                ?.let {
                    toggleLike(it)
                }
        },

        toggleShuffle = {
            player.toggleShuffle()
        },

        cycleRepeat = {
            player.cycleRepeatMode()
        },

        setSleepTimer = {
            player.setSleepTimer(it)
        },

        cancelSleepTimer = {
            player.cancelSleepTimer()
        },

        saveLyricsUri = {
            uri ->

            val songId =
                player.state
                    .value
                    .currentSongId

            if (songId != null) {
                scope.launch {
                    Store.saveLyricsUri(
                        context,
                        songId,
                        uri
                    )

                    persistence
                        .reloadLyricsFiles()
                }
            }
        },

        setCurrentSongInCategory = {
                categoryId,
                added ->

            currentSong()?.let {
                updateCategoryMembership(
                    songId = it.id,
                    categoryId = categoryId,
                    added = added
                )
            }
        },

        createCategoryForCurrentSong = {
            createCategory(it)
        },

        dismissNowPlaying = {
            state.showNowPlaying =
                false

            if (
                player.state
                    .value
                    .currentSongId !=
                null
            ) {
                /*
                 * This key is reserved specifically for the
                 * Now Playing -> MiniPlayer rise animation.
                 *
                 * Normal song changes do not touch it.
                 */
                state.miniRiseKey++

                state.miniVisible =
                    true
            } else {
                state.miniVisible =
                    false
            }
        }
    )
}
