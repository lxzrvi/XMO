package com.xmo.music.app

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.xmo.music.data.LibraryPreferences
import com.xmo.music.data.PlaybackPreferences
import com.xmo.music.data.RecentPlay
import com.xmo.music.data.Song
import com.xmo.music.data.Store
import com.xmo.music.data.UserCategory
import com.xmo.music.data.XmoAppearance
import com.xmo.music.data.XmoProfile

@Stable
internal class XmoAppState {
    var loaded by mutableStateOf(false)
    var setupComplete by mutableStateOf(false)
    var allowed by mutableStateOf(false)

    var profile by
        mutableStateOf(XmoProfile())

    var appearance by
        mutableStateOf(XmoAppearance())

    var libraryPreferences by
        mutableStateOf(LibraryPreferences())

    var playbackPreferences by
        mutableStateOf(PlaybackPreferences())

    var resumeOnHeadphones by
        mutableStateOf(false)

    var songs by
        mutableStateOf<List<Song>>(emptyList())

    var scanning by
        mutableStateOf(false)

    var order by
        mutableStateOf(Store.defaults)

    var categories by
        mutableStateOf<List<UserCategory>>(emptyList())

    var likedSongIds by
        mutableStateOf<Set<Long>>(emptySet())

    var recentPlays by
        mutableStateOf<List<RecentPlay>>(emptyList())

    var lyricsFiles by
        mutableStateOf<Map<Long, String>>(emptyMap())

    var homeMode by
        mutableStateOf("Home")

    var tab by
        mutableIntStateOf(0)

    var profileOpen by
        mutableStateOf(false)

    var showNowPlaying by
        mutableStateOf(false)

    var miniVisible by
        mutableStateOf(false)

    var miniRiseKey by
        mutableIntStateOf(0)

    var playingSource by
        mutableStateOf("All Songs")

    var playingSourceIsCategory by
        mutableStateOf(false)

    var recordedSongId by
        mutableStateOf<Long?>(null)

    var playerPersistenceReady by
        mutableStateOf(false)
}
