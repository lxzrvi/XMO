package com.xmo.music.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.xmo.music.ui.LocalXmoProfile
import com.xmo.music.ui.NavBar
import com.xmo.music.ui.ProfileEditor
import com.xmo.music.ui.ProvideXmoAccent
import com.xmo.music.ui.Search
import com.xmo.music.ui.Settings
import com.xmo.music.ui.blur.liveBlurSource
import com.xmo.music.ui.home.Home
import com.xmo.music.ui.home.homeColors
import com.xmo.music.ui.miniplayer.XmoMiniPlayer
import com.xmo.music.ui.nowplaying.NowPlaying

@Composable
internal fun XmoAppContent(
    state: XmoAppUiState,
    actions: XmoAppActions
) {
    val stateHolder =
        rememberSaveableStateHolder()

    BackHandler(
        enabled =
            !state.showNowPlaying &&
                (
                    state.profileOpen ||
                        state.tab != 0
                    )
    ) {
        when {
            state.profileOpen ->
                actions.closeProfile()

            state.tab != 0 ->
                actions.selectTab(0)
        }
    }

    CompositionLocalProvider(
        LocalXmoProfile provides state.profile
    ) {
        ProvideXmoAccent(
            appearance = state.appearance
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        homeColors(state.theme).bg
                    )
                    .liveBlurSource(
                        state.hazeState
                    )
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .zIndex(0f)
                ) {
                    stateHolder.SaveableStateProvider(
                        key = "tab_${state.tab}"
                    ) {
                        when (state.tab) {
                            0 -> {
                                Home(
                                    songs = state.songs,
                                    allowed = state.allowed,
                                    theme = state.theme,
                                    hazeState = state.hazeState,
                                    order = state.order,
                                    categories = state.categories,
                                    likedSongIds =
                                        state.likedSongIds,
                                    recentPlays =
                                        state.recentPlays,
                                    scanning = state.scanning,
                                    loaded = state.loaded,
                                    refresh = {
                                        if (state.allowed) {
                                            actions.refreshLibrary()
                                        } else {
                                            actions.requestAudioPermission()
                                        }
                                    },
                                    openProfile =
                                        actions.openProfile,
                                    saveOrder =
                                        actions.saveOrder,
                                    saveCategories =
                                        actions.saveCategories,
                                    toggleLike =
                                        actions.toggleLike,
                                    playNext =
                                        actions.playNext,
                                    removeRecent =
                                        actions.removeRecent,
                                    setSongInCategory =
                                        actions.setSongInCategory,
                                    onPlaySong =
                                        actions.playSong
                                )
                            }

                            1 -> {
                                Search(
                                    songs = state.songs,
                                    categories =
                                        state.categories,
                                    theme = state.theme,
                                    onPlaySong =
                                        actions.playSong
                                )
                            }

                            else -> {
                                Settings(
                                    theme = state.theme,
                                    appearance =
                                        state.appearance,
                                    libraryPreferences =
                                        state.libraryPreferences,
                                    playbackPreferences =
                                        state.playbackPreferences,
                                    resumeOnHeadphones =
                                        state.resumeOnHeadphones,
                                    onAppearanceChanged =
                                        actions.changeAppearance,
                                    onLibraryPreferencesChanged =
                                        actions.changeLibraryPreferences,
                                    onPlaybackPreferencesChanged =
                                        actions.changePlaybackPreferences,
                                    onResumeHeadphonesChanged =
                                        actions.changeResumeOnHeadphones,
                                    rescan = {
                                        if (state.allowed) {
                                            actions.refreshLibrary()
                                        } else {
                                            actions.requestAudioPermission()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (
                    state.playback.currentSongId != null &&
                    state.miniVisible &&
                    !state.showNowPlaying &&
                    !state.profileOpen
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .zIndex(10f)
                    ) {
                        XmoMiniPlayer(
                            state = state.playback,
                            theme = state.theme,
                            queue = state.playbackQueue,
                            categories = state.categories,
                            riseKey = state.miniRiseKey,
                            likedSongIds =
                                state.likedSongIds,
                            openPlayer =
                                actions.openNowPlayingFromMini,
                            closePlayer =
                                actions.closePlaybackFromMini,
                            togglePlay =
                                actions.togglePlay,
                            toggleLike =
                                actions.toggleSongLikeById,
                            playQueueIndex =
                                actions.playQueueIndex,
                            setSongInCategory =
                                actions.setSongInCategory,
                            createCategory =
                                actions.createCategoryForSong
                        )
                    }
                }

                if (!state.profileOpen) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .zIndex(20f)
                    ) {
                        NavBar(
                            selected = state.tab,
                            theme = state.theme,
                            onSelect = actions.selectTab
                        )
                    }
                }

                if (state.profileOpen) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .zIndex(80f)
                    ) {
                        ProfileEditor(
                            profile = state.profile,
                            theme = state.theme,
                            apply = actions.saveProfile,
                            cancel = actions.closeProfile
                        )
                    }
                }

                if (state.showNowPlaying) {
                    val id =
                        state.playback.currentSongId

                    Box(
                        Modifier
                            .fillMaxSize()
                            .zIndex(100f)
                    ) {
                        NowPlaying(
                            state = state.playback,
                            theme = state.theme,
                            source = state.playingSource,
                            sourceIsCategory =
                                state.playingSourceIsCategory,
                            queue = state.playbackQueue,
                            songs = state.songs,
                            liked =
                                id != null &&
                                    id in state.likedSongIds,
                            lyricsUri =
                                id?.let {
                                    state.lyricsFiles[it]
                                },
                            categories =
                                state.categories,
                            onOpened =
                                actions.nowPlayingOpened,
                            refreshPosition =
                                actions.refreshPosition,
                            togglePlay =
                                actions.togglePlay,
                            previous =
                                actions.previous,
                            previousItem =
                                actions.previousItem,
                            next = actions.next,
                            playQueueIndex =
                                actions.playQueueIndex,
                            seekTo = actions.seekTo,
                            toggleLike =
                                actions.toggleCurrentLike,
                            toggleShuffle =
                                actions.toggleShuffle,
                            cycleRepeat =
                                actions.cycleRepeat,
                            setSleepTimer =
                                actions.setSleepTimer,
                            cancelSleepTimer =
                                actions.cancelSleepTimer,
                            saveLyricsUri =
                                actions.saveLyricsUri,
                            setSongInCategory =
                                actions.setCurrentSongInCategory,
                            createCategory =
                                actions.createCategoryForCurrentSong,
                            dismiss =
                                actions.dismissNowPlaying
                        )
                    }
                }
            }
        }
    }
}
