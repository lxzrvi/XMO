package com.xmo.music.app

import android.content.Context
import com.xmo.music.data.Song
import com.xmo.music.data.Store
import com.xmo.music.data.UserCategory
import com.xmo.music.player.XmoPlayer
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun buildXmoAppActions(
    context: Context,
    state: XmoAppState,
    player: XmoPlayer,
    scope: CoroutineScope,
    persistence: XmoAppPersistence,
    requestAudioPermission: () -> Unit
): XmoAppActions {
    fun currentSong(): Song? =
        state.songs.firstOrNull {
            it.id == player.state.value.currentSongId
        } ?: player.currentSong()

    fun toggleLike(songId: Long) {
        scope.launch {
            state.likedSongIds =
                Store.toggleLiked(
                    context,
                    songId
                )
        }
    }

    fun membership(
        songId: Long,
        categoryId: String,
        added: Boolean
    ) {
        scope.launch {
            state.categories =
                Store.setSongInCategory(
                    context,
                    categoryId,
                    songId,
                    added
                )
        }
    }

    fun createCategory(
        rawName: String,
        song: Song?
    ): UserCategory? {
        val name =
            rawName.trim()
                .replace("\n", " ")
                .take(24)

        if (name.isBlank()) return null

        val category =
            UserCategory(
                id = "cat_${UUID.randomUUID()}",
                name = name,
                icon = state.categories.size % 4,
                songIds =
                    song?.let {
                        setOf(it.id)
                    } ?: emptySet()
            )

        val next =
            state.categories + category

        state.categories = next

        scope.launch {
            Store.saveCategories(
                context,
                next
            )
        }

        return category
    }

    fun startQueue(
        queue: List<Song>,
        index: Int,
        source: String,
        category: Boolean
    ) {
        if (queue.isEmpty()) return

        val hadPlayback =
            player.state.value.currentSongId != null

        val miniWasVisible =
            state.miniVisible &&
                !state.showNowPlaying

        state.playingSource = source
        state.playingSourceIsCategory = category

        player.play(queue, index)

        state.showNowPlaying = false

        if (!hadPlayback || !miniWasVisible) {
            state.miniRiseKey++
        }

        state.miniVisible = true
    }

    return XmoAppActions(
        requestAudioPermission =
            requestAudioPermission,

        refreshLibrary = {
            scope.launch {
                persistence.loadLibrary()
            }
        },

        saveHomeMode = {
            persistence.saveHomeMode(it)
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
            state.profile = it
            state.profileOpen = false

            scope.launch {
                Store.saveProfile(context, it)
            }
        },

        saveOrder = {
            state.order = it

            scope.launch {
                Store.saveOrder(context, it)
            }
        },

        saveCategories = {
            state.categories = it

            scope.launch {
                Store.saveCategories(context, it)
            }
        },

        playSong = {
                song,
                source,
                isCategory,
                queue ->

            val index =
                queue.indexOfFirst {
                    it.id == song.id
                }

            if (index >= 0) {
                startQueue(
                    queue,
                    index,
                    source,
                    isCategory
                )
            }
        },

        shuffleSongs = {
                songs,
                source,
                isCategory ->

            if (songs.isNotEmpty()) {
                startQueue(
                    songs.shuffled(),
                    0,
                    source,
                    isCategory
                )
            }
        },

        playNext = {
            player.playNext(it)
        },

        removeRecent = {
            song ->

            scope.launch {
                persistence.removeRecent(song.id)
            }
        },

        toggleLike = {
            toggleLike(it.id)
        },

        toggleSongLikeById = {
            toggleLike(it)
        },

        setSongInCategory = {
                song,
                categoryId,
                added ->

            membership(
                song.id,
                categoryId,
                added
            )
        },

        createCategoryForSong = {
                name,
                song ->

            createCategory(name, song)
        },

        changeAppearance = {
            state.appearance = it

            scope.launch {
                Store.saveAppearance(context, it)
            }
        },

        changeLibraryPreferences = {
            state.libraryPreferences = it

            scope.launch {
                Store.saveLibraryPreferences(
                    context,
                    it
                )
                persistence.loadLibrary()
            }
        },

        changePlaybackPreferences = {
            state.playbackPreferences = it

            player.setPlaybackParameters(
                it.playbackSpeed,
                it.playbackPitch
            )

            scope.launch {
                Store.savePlaybackPreferences(
                    context,
                    it
                )
            }
        },

        changeResumeOnHeadphones = {
            state.resumeOnHeadphones = it

            scope.launch {
                Store.saveResumeOnHeadphones(
                    context,
                    it
                )
            }
        },

        openNowPlayingFromMini = {
            state.miniVisible = false
            state.showNowPlaying = true
        },

        closePlaybackFromMini = {
            player.closePlayback()
            state.miniVisible = false
            state.showNowPlaying = false
        },

        togglePlay = player::togglePlayPause,

        playQueueIndex = {
            player.playQueueIndex(it)
        },

        next = player::next,
        previous = player::previous,
        previousItem = player::previousItem,

        nowPlayingOpened = {
            state.miniVisible = false
        },

        refreshPosition =
            player::refreshPosition,

        seekTo = player::seekTo,

        toggleCurrentLike = {
            player.state.value.currentSongId
                ?.let(::toggleLike)
        },

        toggleShuffle =
            player::toggleShuffle,

        cycleRepeat =
            player::cycleRepeatMode,

        setSleepTimer =
            player::setSleepTimer,

        cancelSleepTimer =
            player::cancelSleepTimer,

        saveLyricsUri = { uri ->
            val id =
                player.state.value.currentSongId

            if (id != null) {
                scope.launch {
                    Store.saveLyricsUri(
                        context,
                        id,
                        uri
                    )
                    persistence.reloadLyricsFiles()
                }
            }
        },

        setCurrentSongInCategory = {
                categoryId,
                added ->

            currentSong()?.let {
                membership(
                    it.id,
                    categoryId,
                    added
                )
            }
        },

        createCategoryForCurrentSong = {
            createCategory(
                it,
                currentSong()
            )
        },

        dismissNowPlaying = {
            state.showNowPlaying = false

            if (
                player.state.value.currentSongId !=
                null
            ) {
                state.miniRiseKey++
                state.miniVisible = true
            } else {
                state.miniVisible = false
            }
        }
    )
}
