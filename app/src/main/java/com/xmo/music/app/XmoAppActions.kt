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
        rawName: String,
        song: Song?
    ): UserCategory? {
        val name =
            rawName.trim()
                .replace("\n", " ")
                .take(24)

        if (name.isBlank()) {
            return null
        }

        val category =
            UserCategory(
                id = "cat_${UUID.randomUUID()}",
                name = name,
                icon = state.categories.size % 4,
                songIds =
                    if (song != null) {
                        setOf(song.id)
                    } else {
                        emptySet()
                    }
            )

        val nextCategories =
            state.categories + category

        val nextOrder =
            (state.order + category.id)
                .distinct()

        state.categories = nextCategories
        state.order = nextOrder

        scope.launch {
            Store.saveCategories(
                context,
                nextCategories
            )

            Store.saveOrder(
                context,
                nextOrder
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

        saveProfile = { profile ->
            state.profile = profile
            state.profileOpen = false

            scope.launch {
                Store.saveProfile(
                    context,
                    profile
                )
            }
        },

        saveOrder = { order ->
            state.order = order

            scope.launch {
                Store.saveOrder(
                    context,
                    order
                )
            }
        },

        saveCategories = { categories ->
            state.categories = categories

            scope.launch {
                Store.saveCategories(
                    context,
                    categories
                )
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
                val hadPlayback =
                    player.state.value
                        .currentSongId != null

                val miniWasVisible =
                    state.miniVisible &&
                        !state.showNowPlaying

                state.playingSource = source
                state.playingSourceIsCategory =
                    isCategory

                player.play(queue, index)

                state.showNowPlaying = false

                if (
                    !hadPlayback ||
                    !miniWasVisible
                ) {
                    state.miniRiseKey++
                }

                state.miniVisible = true
            }
        },

        playNext = { song ->
            player.playNext(song)

            if (
                player.state.value.currentSongId !=
                null
            ) {
                state.miniVisible = true
            }
        },

        removeRecent = { song ->
            scope.launch {
                persistence.removeRecent(song.id)
            }
        },

        toggleLike = { song ->
            toggleLike(song.id)
        },

        toggleSongLikeById = { songId ->
            toggleLike(songId)
        },

        setSongInCategory = {
                song,
                categoryId,
                added ->

            updateCategoryMembership(
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

        changeAppearance = { appearance ->
            state.appearance = appearance

            scope.launch {
                Store.saveAppearance(
                    context,
                    appearance
                )
            }
        },

        changeLibraryPreferences = { preferences ->
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

        changePlaybackPreferences = { preferences ->
            state.playbackPreferences =
                preferences

            player.setPlaybackParameters(
                preferences.playbackSpeed,
                preferences.playbackPitch
            )

            scope.launch {
                Store.savePlaybackPreferences(
                    context,
                    preferences
                )
            }
        },

        changeResumeOnHeadphones = { enabled ->
            state.resumeOnHeadphones = enabled

            scope.launch {
                Store.saveResumeOnHeadphones(
                    context,
                    enabled
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

        togglePlay = {
            player.togglePlayPause()
        },

        playQueueIndex = { index ->
            val queue = player.queue()

            if (index in queue.indices) {
                player.play(queue, index)
            }
        },

        next = {
            player.next()
        },

        previous = {
            player.previous()
        },

        previousItem = {
            player.previousItem()
        },

        nowPlayingOpened = {
            state.miniVisible = false
        },

        refreshPosition = {
            player.refreshPosition()
        },

        seekTo = {
            player.seekTo(it)
        },

        toggleCurrentLike = {
            player.state.value
                .currentSongId
                ?.let(::toggleLike)
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

        saveLyricsUri = { uri ->
            val songId =
                player.state.value.currentSongId

            if (songId != null) {
                scope.launch {
                    Store.saveLyricsUri(
                        context,
                        songId,
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
                updateCategoryMembership(
                    it.id,
                    categoryId,
                    added
                )
            }
        },

        createCategoryForCurrentSong = { name ->
            createCategory(
                name,
                currentSong()
            )
        },

        dismissNowPlaying = {
            state.showNowPlaying = false

            if (
                player.state.value
                    .currentSongId != null
            ) {
                state.miniRiseKey++
                state.miniVisible = true
            } else {
                state.miniVisible = false
            }
        }
    )
}
