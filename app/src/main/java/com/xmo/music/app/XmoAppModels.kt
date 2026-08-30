package com.xmo.music.app

import com.xmo.music.XmoTheme
import com.xmo.music.data.LibraryPreferences
import com.xmo.music.data.PlaybackPreferences
import com.xmo.music.data.RecentPlay
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.data.XmoAppearance
import com.xmo.music.data.XmoProfile
import com.xmo.music.player.PlaybackState
import dev.chrisbanes.haze.HazeState

internal data class XmoAppUiState(
    val playback: PlaybackState,
    val theme: XmoTheme,
    val hazeState: HazeState,

    val profile: XmoProfile,
    val appearance: XmoAppearance,
    val libraryPreferences: LibraryPreferences,
    val playbackPreferences: PlaybackPreferences,
    val resumeOnHeadphones: Boolean,

    val songs: List<Song>,
    val playbackQueue: List<Song>,
    val currentSong: Song?,

    val order: List<String>,
    val categories: List<UserCategory>,
    val likedSongIds: Set<Long>,
    val recentPlays: List<RecentPlay>,
    val lyricsFiles: Map<Long, String>,

    val allowed: Boolean,
    val scanning: Boolean,

    val tab: Int,
    val profileOpen: Boolean,

    val showNowPlaying: Boolean,
    val miniVisible: Boolean,
    val miniRiseKey: Int,

    val playingSource: String,
    val playingSourceIsCategory: Boolean
)

internal class XmoAppActions(
    val requestAudioPermission: () -> Unit,
    val refreshLibrary: () -> Unit,

    val selectTab: (Int) -> Unit,

    val openProfile: () -> Unit,
    val closeProfile: () -> Unit,
    val saveProfile: (XmoProfile) -> Unit,

    val saveOrder: (List<String>) -> Unit,
    val saveCategories: (List<UserCategory>) -> Unit,

    val playSong: (
        song: Song,
        source: String,
        isCategory: Boolean,
        queue: List<Song>
    ) -> Unit,

    val toggleLike: (Song) -> Unit,

    val toggleSongLikeById: (Long) -> Unit,

    val setSongInCategory: (
        song: Song,
        categoryId: String,
        added: Boolean
    ) -> Unit,

    val changeAppearance: (XmoAppearance) -> Unit,

    val changeLibraryPreferences:
        (LibraryPreferences) -> Unit,

    val changePlaybackPreferences:
        (PlaybackPreferences) -> Unit,

    val changeResumeOnHeadphones:
        (Boolean) -> Unit,

    val openNowPlayingFromMini: () -> Unit,
    val closePlaybackFromMini: () -> Unit,

    val togglePlay: () -> Unit,

    val playQueueIndex: (Int) -> Unit,

    val next: () -> Unit,
    val previous: () -> Unit,
    val previousItem: () -> Unit,

    val nowPlayingOpened: () -> Unit,
    val refreshPosition: () -> Unit,

    val seekTo: (Long) -> Unit,

    val toggleCurrentLike: () -> Unit,

    val toggleShuffle: () -> Unit,
    val cycleRepeat: () -> Unit,

    val setSleepTimer: (Long) -> Unit,
    val cancelSleepTimer: () -> Unit,

    val saveLyricsUri: (String?) -> Unit,

    val setCurrentSongInCategory: (
        categoryId: String,
        added: Boolean
    ) -> Unit,

    val createCategoryForCurrentSong:
        (String) -> UserCategory?,

    val dismissNowPlaying: () -> Unit
)
