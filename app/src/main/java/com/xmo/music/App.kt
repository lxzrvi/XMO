package com.xmo.music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.xmo.music.data.AccentMode
import com.xmo.music.data.Library
import com.xmo.music.data.LibraryPreferences
import com.xmo.music.data.PlaybackPreferences
import com.xmo.music.data.RecentPlay
import com.xmo.music.data.Song
import com.xmo.music.data.Store
import com.xmo.music.data.ThemeMode
import com.xmo.music.data.UserCategory
import com.xmo.music.data.XmoAppearance
import com.xmo.music.data.XmoProfile
import com.xmo.music.player.XmoPlayer
import com.xmo.music.ui.Home
import com.xmo.music.ui.LocalXmoProfile
import com.xmo.music.ui.MiniPlayer
import com.xmo.music.ui.NavBar
import com.xmo.music.ui.NowPlaying
import com.xmo.music.ui.ProfileEditor
import com.xmo.music.ui.ProvideXmoAccent
import com.xmo.music.ui.Search
import com.xmo.music.ui.Settings
import com.xmo.music.ui.Setup
import com.xmo.music.ui.homeColors
import com.xmo.music.ui.blur.liveBlurSource
import com.xmo.music.ui.blur.rememberLiveBlurState
import kotlinx.coroutines.launch

enum class XmoTheme {
    Dark,
    Light,
    Amoled
}

@Composable
fun App() {
    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    val stateHolder =
        rememberSaveableStateHolder()

    val configuration =
        LocalConfiguration.current

    val hazeState =
        rememberLiveBlurState()

    /*
     * =========================================================
     * MEDIA3
     * =========================================================
     */

    val player =
        remember {
            XmoPlayer(
                context
            )
        }

    DisposableEffect(
        player
    ) {
        player.connect()

        onDispose {
            player.release()
        }
    }

    val playback by
        player.state
            .collectAsState()

    /*
     * =========================================================
     * PERMISSION
     * =========================================================
     */

    val audioPermission =
        if (
            Build.VERSION.SDK_INT >=
            33
        ) {
            Manifest.permission
                .READ_MEDIA_AUDIO
        } else {
            Manifest.permission
                .READ_EXTERNAL_STORAGE
        }

    var allowed by
        remember {
            mutableStateOf(
                ContextCompat
                    .checkSelfPermission(
                        context,
                        audioPermission
                    ) ==
                    PackageManager.PERMISSION_GRANTED
            )
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) {
            allowed =
                it
        }

    /*
     * =========================================================
     * PERSISTED APP STATE
     * =========================================================
     */

    var loaded by
        remember {
            mutableStateOf(
                false
            )
        }

    var setupComplete by
        remember {
            mutableStateOf(
                false
            )
        }

    var profile by
        remember {
            mutableStateOf(
                XmoProfile()
            )
        }

    var appearance by
        remember {
            mutableStateOf(
                XmoAppearance()
            )
        }

    var libraryPreferences by
        remember {
            mutableStateOf(
                LibraryPreferences()
            )
        }

    var playbackPreferences by
        remember {
            mutableStateOf(
                PlaybackPreferences()
            )
        }

    var resumeOnHeadphones by
        remember {
            mutableStateOf(
                false
            )
        }

    var songs by
        remember {
            mutableStateOf<List<Song>>(
                emptyList()
            )
        }

    var order by
        remember {
            mutableStateOf(
                Store.defaults
            )
        }

    var categories by
        remember {
            mutableStateOf<List<UserCategory>>(
                emptyList()
            )
        }

    var likedSongIds by
        remember {
            mutableStateOf<Set<Long>>(
                emptySet()
            )
        }

    var recentPlays by
        remember {
            mutableStateOf<List<RecentPlay>>(
                emptyList()
            )
        }

    var lyricsFiles by
        remember {
            mutableStateOf<Map<Long, String>>(
                emptyMap()
            )
        }

    var scanning by
        remember {
            mutableStateOf(
                false
            )
        }

    /*
     * =========================================================
     * NAVIGATION
     * =========================================================
     */

    var tab by
        remember {
            mutableIntStateOf(
                0
            )
        }

    var profileOpen by
        remember {
            mutableStateOf(
                false
            )
        }

    /*
     * =========================================================
     * PLAYER UI
     * =========================================================
     */

    var showNowPlaying by
        remember {
            mutableStateOf(
                false
            )
        }

    var miniVisible by
        remember {
            mutableStateOf(
                false
            )
        }

    var miniRiseKey by
        remember {
            mutableIntStateOf(
                0
            )
        }

    var playingSource by
        remember {
            mutableStateOf(
                "All Songs"
            )
        }

    var playingSourceIsCategory by
        remember {
            mutableStateOf(
                false
            )
        }

    /*
     * =========================================================
     * THEME RESOLUTION
     * =========================================================
     */

    val systemDark =
        (
            configuration.uiMode and
                android.content.res.Configuration
                    .UI_MODE_NIGHT_MASK
            ) ==
            android.content.res.Configuration
                .UI_MODE_NIGHT_YES

    val theme =
        when (
            appearance.themeMode
        ) {
            ThemeMode.System ->
                if (
                    systemDark
                ) {
                    XmoTheme.Dark
                } else {
                    XmoTheme.Light
                }

            ThemeMode.Dark ->
                XmoTheme.Dark

            ThemeMode.Light ->
                XmoTheme.Light

            ThemeMode.Amoled ->
                XmoTheme.Amoled
        }

    /*
     * =========================================================
     * INITIAL LOAD
     *
     * DataStore values are loaded together before normal UI is
     * shown. Library scan runs only after setup + permission.
     * =========================================================
     */

    LaunchedEffect(Unit) {
        profile =
            Store.profile(
                context
            )

        appearance =
            Store.appearance(
                context
            )

        categories =
            Store.categories(
                context
            )

        order =
            Store.order(
                context
            )

        likedSongIds =
            Store.likedSongIds(
                context
            )

        recentPlays =
            Store.recentPlays(
                context
            )

        lyricsFiles =
            Store.lyricsFiles(
                context
            )

        libraryPreferences =
            Store.libraryPreferences(
                context
            )

        playbackPreferences =
            Store.playbackPreferences(
                context
            )

        resumeOnHeadphones =
            Store.resumeOnHeadphones(
                context
            )

        setupComplete =
            Store.setupComplete(
                context
            )

        loaded =
            true
    }

    /*
     * Apply persisted player state after MediaController connects.
     */
    LaunchedEffect(
        playback.connected,
        playbackPreferences
    ) {
        if (
            playback.connected
        ) {
            player.setPlaybackParameters(
                speed =
                    playbackPreferences.playbackSpeed,

                pitch =
                    playbackPreferences.playbackPitch
            )
        }
    }

    LaunchedEffect(
        playback.connected
    ) {
        if (
            playback.connected
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
        }
    }

    /*
     * =========================================================
     * LIBRARY LOAD / FILTER
     * =========================================================
     */

    suspend fun loadLibrary() {
        if (
            !allowed ||
            !setupComplete
        ) {
            return
        }

        scanning =
            true

        try {
            val scanned =
                Library.songs(
                    context
                )

            songs =
                if (
                    libraryPreferences.ignoreShortAudio
                ) {
                    scanned.filter {
                        it.duration >=
                            libraryPreferences
                                .minimumDurationMs
                    }
                } else {
                    scanned
                }
        } finally {
            scanning =
                false
        }
    }

    LaunchedEffect(
        loaded,
        setupComplete,
        allowed
    ) {
        if (
            loaded &&
            setupComplete &&
            allowed &&
            songs.isEmpty()
        ) {
            loadLibrary()
        }
    }

    /*
     * Re-filter/reload when user changes the library filter.
     */
    LaunchedEffect(
        libraryPreferences
    ) {
        if (
            loaded &&
            setupComplete &&
            allowed
        ) {
            loadLibrary()
        }
    }

    /*
     * =========================================================
     * RECENT PLAY EVENT
     *
     * currentSongId changes only when Media3 actually changes
     * the current media item.
     * =========================================================
     */

    var recordedSongId by
        remember {
            mutableStateOf<Long?>(
                null
            )
        }

    LaunchedEffect(
        playback.currentSongId
    ) {
        val id =
            playback.currentSongId
                ?: return@LaunchedEffect

        if (
            id ==
            recordedSongId
        ) {
            return@LaunchedEffect
        }

        recordedSongId =
            id

        recentPlays =
            Store.recordPlay(
                context,
                id
            )
    }

    /*
     * Persist Media3 shuffle/repeat after a genuine state change.
     */
    var playbackPersistenceReady by
        remember {
            mutableStateOf(
                false
            )
        }

    LaunchedEffect(
        playback.connected
    ) {
        if (
            playback.connected
        ) {
            playbackPersistenceReady =
                true
        }
    }

    LaunchedEffect(
        playback.shuffleEnabled,
        playback.repeatMode,
        playbackPersistenceReady
    ) {
        if (
            playbackPersistenceReady
        ) {
            Store.saveShuffleEnabled(
                context,
                playback.shuffleEnabled
            )

            Store.saveRepeatMode(
                context,
                playback.repeatMode
            )
        }
    }

    /*
     * =========================================================
     * INITIAL SKELETON
     * =========================================================
     */

    if (
        !loaded
    ) {
        FirstLoadSkeleton(
            theme =
                XmoTheme.Dark
        )

        return
    }

    /*
     * =========================================================
     * FIRST-RUN SETUP
     * =========================================================
     */

    if (
        !setupComplete
    ) {
        Setup(
            initialProfile =
                profile,

            existingCategories =
                categories,

            onCategoriesChanged = {
                    next ->

                categories =
                    next

                scope.launch {
                    Store.saveCategories(
                        context,
                        next
                    )

                    val customIds =
                        next.map {
                            it.id
                        }

                    val builtIns =
                        order.filter {
                            it in
                                Store.defaults
                        }

                    val existingCustom =
                        order.filter {
                            it !in
                                Store.defaults &&
                                it in
                                customIds
                        }

                    val missing =
                        customIds.filterNot {
                            it in
                                existingCustom
                        }

                    order =
                        builtIns +
                            existingCustom +
                            missing

                    Store.saveOrder(
                        context,
                        order
                    )
                }
            },

            finish = {
                    result ->

                scope.launch {
                    /*
                     * Setup.kt already blocks completion without
                     * audio access; verify again at the boundary.
                     */
                    allowed =
                        ContextCompat
                            .checkSelfPermission(
                                context,
                                audioPermission
                            ) ==
                            PackageManager.PERMISSION_GRANTED

                    if (
                        !allowed
                    ) {
                        permissionLauncher.launch(
                            audioPermission
                        )

                        return@launch
                    }

                    profile =
                        result

                    Store.finishSetup(
                        context,
                        result
                    )

                    setupComplete =
                        true

                    loadLibrary()
                }
            },

            setupLater = {
                    result ->

                scope.launch {
                    allowed =
                        ContextCompat
                            .checkSelfPermission(
                                context,
                                audioPermission
                            ) ==
                            PackageManager.PERMISSION_GRANTED

                    if (
                        !allowed
                    ) {
                        permissionLauncher.launch(
                            audioPermission
                        )

                        return@launch
                    }

                    profile =
                        result

                    Store.finishSetup(
                        context,
                        result
                    )

                    setupComplete =
                        true

                    loadLibrary()
                }
            }
        )

        return
    }

    /*
     * =========================================================
     * SHARED COMMANDS
     * =========================================================
     */

    fun startSong(
        song: Song,
        source: String,
        isCategory: Boolean,
        queue: List<Song>
    ) {
        val index =
            queue.indexOfFirst {
                it.id ==
                    song.id
            }

        if (
            index <
            0
        ) {
            return
        }

        playingSource =
            source

        playingSourceIsCategory =
            isCategory

        player.play(
            queue,
            index
        )

        /*
         * SongTile may open NowPlaying directly.
         * Existing MiniPlayer is removed only when player entrance
         * reports completion.
         */
        showNowPlaying =
            true
    }

    fun toggleLike(
        songId: Long
    ) {
        scope.launch {
            likedSongIds =
                Store.toggleLiked(
                    context,
                    songId
                )
        }
    }

    fun updateCategoryMembership(
        song: Song,
        categoryId: String,
        added: Boolean
    ) {
        scope.launch {
            categories =
                Store.setSongInCategory(
                    context =
                        context,

                    categoryId =
                        categoryId,

                    songId =
                        song.id,

                    added =
                        added
                )
        }
    }

    fun currentSong():
        Song? {
        val id =
            playback.currentSongId
                ?: return null

        return songs.firstOrNull {
            it.id ==
                id
        } ?: player.currentSong()
    }

    /*
     * =========================================================
     * BACK POLICY
     *
     * NowPlaying handles itself internally.
     * Profile -> owning Home.
     * Search / Settings -> Home.
     * Home -> Android exits Activity naturally.
     * =========================================================
     */

    BackHandler(
        enabled =
            !showNowPlaying &&
                (
                    profileOpen ||
                        tab !=
                        0
                    )
    ) {
        when {
            profileOpen ->
                profileOpen =
                    false

            tab !=
                0 ->
                tab =
                    0
        }
    }

    CompositionLocalProvider(
        LocalXmoProfile provides
            profile
    ) {
        ProvideXmoAccent(
            appearance =
                appearance
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        homeColors(
                            theme
                        ).bg
                    )
                    .liveBlurSource(
                        hazeState
                    )
            ) {
                /*
                 * =================================================
                 * TAB CONTENT
                 * =================================================
                 */

                Box(
                    Modifier
                        .fillMaxSize()
                        .zIndex(
                            0f
                        )
                ) {
                    stateHolder.SaveableStateProvider(
                        key =
                            "tab_$tab"
                    ) {
                        when (
                            tab
                        ) {
                            0 -> {
                                Home(
                                    songs =
                                        songs,

                                    allowed =
                                        allowed,

                                    theme =
                                        theme,

                                    hazeState =
                                        hazeState,

                                    order =
                                        order,

                                    categories =
                                        categories,

                                    likedSongIds =
                                        likedSongIds,

                                    recentPlays =
                                        recentPlays,

                                    scanning =
                                        scanning,

                                    refresh = {
                                        if (
                                            !allowed
                                        ) {
                                            permissionLauncher.launch(
                                                audioPermission
                                            )
                                        } else {
                                            scope.launch {
                                                loadLibrary()
                                            }
                                        }
                                    },

                                    openProfile = {
                                        profileOpen =
                                            true
                                    },

                                    saveOrder = {
                                        order =
                                            it

                                        scope.launch {
                                            Store.saveOrder(
                                                context,
                                                it
                                            )
                                        }
                                    },

                                    saveCategories = {
                                        categories =
                                            it

                                        scope.launch {
                                            Store.saveCategories(
                                                context,
                                                it
                                            )
                                        }
                                    },

                                    toggleLike = {
                                        toggleLike(
                                            it.id
                                        )
                                    },

                                    setSongInCategory =
                                        ::updateCategoryMembership,

                                    onPlaySong =
                                        ::startSong
                                )
                            }

                            1 -> {
                                Search(
                                    songs =
                                        songs,

                                    categories =
                                        categories,

                                    theme =
                                        theme,

                                    onPlaySong =
                                        ::startSong
                                )
                            }

                            else -> {
                                Settings(
                                    theme =
                                        theme,

                                    appearance =
                                        appearance,

                                    libraryPreferences =
                                        libraryPreferences,

                                    playbackPreferences =
                                        playbackPreferences,

                                    resumeOnHeadphones =
                                        resumeOnHeadphones,

                                    onAppearanceChanged = {
                                        appearance =
                                            it

                                        scope.launch {
                                            Store.saveAppearance(
                                                context,
                                                it
                                            )
                                        }
                                    },

                                    onLibraryPreferencesChanged = {
                                        libraryPreferences =
                                            it

                                        scope.launch {
                                            Store.saveLibraryPreferences(
                                                context,
                                                it
                                            )
                                        }
                                    },

                                    onPlaybackPreferencesChanged = {
                                        playbackPreferences =
                                            it

                                        player.setPlaybackParameters(
                                            speed =
                                                it.playbackSpeed,

                                            pitch =
                                                it.playbackPitch
                                        )

                                        scope.launch {
                                            Store.savePlaybackPreferences(
                                                context,
                                                it
                                            )
                                        }
                                    },

                                    onResumeHeadphonesChanged = {
                                        resumeOnHeadphones =
                                            it

                                        scope.launch {
                                            Store.saveResumeOnHeadphones(
                                                context,
                                                it
                                            )
                                        }
                                    },

                                    rescan = {
                                        if (
                                            !allowed
                                        ) {
                                            permissionLauncher.launch(
                                                audioPermission
                                            )
                                        } else {
                                            scope.launch {
                                                loadLibrary()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                /*
                 * =================================================
                 * NAVBAR
                 * =================================================
                 */

                if (!profileOpen) {
                    NavBar(
                        selected = tab,
                        theme = theme
                    ) {
                        tab = it
                    }
                }

                /*
                 * =================================================
                 * MINIPLAYER
                 * =================================================
                 */

                if (
                    playback.currentSongId !=
                        null &&
                    miniVisible &&
                    !showNowPlaying &&
                    !profileOpen
                ) {
                    val song =
                        currentSong()

                    Box(
                        Modifier
                            .fillMaxSize()
                            .zIndex(
                                20f
                            )
                    ) {
                        MiniPlayer(
                            state =
                                playback,

                            theme =
                                theme,

                            hazeState =
                                hazeState,

                            riseKey =
                                miniRiseKey,

                            liked =
                                playback.currentSongId in
                                    likedSongIds,

                            /*
                             * MiniPlayer itself completes its
                             * downward exit before this callback.
                             */
                            openPlayer = {
                                miniVisible =
                                    false

                                showNowPlaying =
                                    true
                            },

                            togglePlay = {
                                player.togglePlayPause()
                            },

                            toggleLike = {
                                song?.let {
                                    toggleLike(
                                        it.id
                                    )
                                }
                            },

                            previous = {
                                player.previousItem()
                            },

                            next = {
                                player.next()
                            }
                        )
                    }
                }

                /*
                 * =================================================
                 * PROFILE
                 * =================================================
                 */

                if (
                    profileOpen
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .zIndex(
                                80f
                            )
                    ) {
                        ProfileEditor(
                            profile =
                                profile,

                            theme =
                                theme,

                            apply = {
                                profile =
                                    it

                                scope.launch {
                                    Store.saveProfile(
                                        context,
                                        it
                                    )
                                }

                                profileOpen =
                                    false
                            },

                            cancel = {
                                profileOpen =
                                    false
                            }
                        )
                    }
                }

                /*
                 * =================================================
                 * NOW PLAYING
                 * =================================================
                 */

                if (
                    showNowPlaying
                ) {
                    val song =
                        currentSong()

                    val id =
                        playback.currentSongId

                    Box(
                        Modifier
                            .fillMaxSize()
                            .zIndex(
                                100f
                            )
                    ) {
                        NowPlaying(
                            state =
                                playback,

                            theme =
                                theme,

                            source =
                                playingSource,

                            sourceIsCategory =
                                playingSourceIsCategory,

                            queue =
                                player.queue(),

                            liked =
                                id !=
                                null &&
                                    id in
                                    likedSongIds,

                            lyricsUri =
                                id?.let {
                                    lyricsFiles[
                                        it
                                    ]
                                },

                            /*
                             * SongTile opening may leave an existing
                             * MiniPlayer behind during entrance.
                             * Remove it only when full player reaches
                             * its final position.
                             */
                            onOpened = {
                                miniVisible =
                                    false
                            },

                            refreshPosition = {
                                player.refreshPosition()
                            },

                            togglePlay = {
                                player.togglePlayPause()
                            },

                            previous = {
                                player.previous()
                            },

                            previousItem = {
                                player.previousItem()
                            },

                            next = {
                                player.next()
                            },

                            seekTo = {
                                player.seekTo(
                                    it
                                )
                            },

                            toggleLike = {
                                id?.let {
                                    toggleLike(
                                        it
                                    )
                                }
                            },

                            toggleShuffle = {
                                player.toggleShuffle()
                            },

                            cycleRepeat = {
                                player.cycleRepeatMode()
                            },

                            setSleepTimer = {
                                player.setSleepTimer(
                                    it
                                )
                            },

                            cancelSleepTimer = {
                                player.cancelSleepTimer()
                            },

                            saveLyricsUri = {
                                if (
                                    id !=
                                    null
                                ) {
                                    scope.launch {
                                        Store.saveLyricsUri(
                                            context,
                                            id,
                                            it
                                        )

                                        lyricsFiles =
                                            Store.lyricsFiles(
                                                context
                                            )
                                    }
                                }
                            },

                            /*
                             * Called only after NowPlaying is fully
                             * below the screen.
                             */
                            dismiss = {
                                showNowPlaying =
                                    false

                                miniRiseKey++

                                miniVisible =
                                    playback.currentSongId !=
                                        null
                            }
                        )
                    }
                }

                /*
                 * First playback without NowPlaying still gets a
                 * MiniPlayer.
                 */
                LaunchedEffect(
                    playback.currentSongId,
                    showNowPlaying,
                    profileOpen
                ) {
                    if (
                        playback.currentSongId !=
                            null &&
                        !showNowPlaying &&
                        !profileOpen &&
                        !miniVisible
                    ) {
                        miniVisible =
                            true
                    }
                }
            }
        }
    }
}

/*
 * =============================================================
 * FIRST INITIAL LOAD ONLY
 * =============================================================
 */

@Composable
private fun FirstLoadSkeleton(
    theme: XmoTheme
) {
    val c =
        homeColors(
            theme
        )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                c.bg
            )
    ) {
        androidx.compose.foundation.layout.Column(
            Modifier
                .fillMaxSize()
                .padding(
                    start = 18.dp,
                    top = 72.dp,
                    end = 18.dp
                )
        ) {
            Box(
                Modifier
                    .fillMaxSize(
                        .08f
                    )
                    .background(
                        c.button,
                        androidx.compose.foundation.shape
                            .CircleShape
                    )
            )

            androidx.compose.foundation.layout.Spacer(
                Modifier
                    .fillMaxSize(
                        .025f
                    )
            )

            repeat(
                4
            ) {
                Box(
                    Modifier
                        .fillMaxSize(
                            .10f
                        )
                        .background(
                            c.button,
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(
                                    18.dp
                                )
                        )
                )

                androidx.compose.foundation.layout.Spacer(
                    Modifier
                        .fillMaxSize(
                            .018f
                        )
                )
            }
        }
    }
}
