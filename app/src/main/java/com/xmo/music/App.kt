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
import com.xmo.music.ui.blur.liveBlurSource
import com.xmo.music.ui.blur.rememberLiveBlurState
import com.xmo.music.ui.homeColors
import kotlinx.coroutines.launch
import java.util.UUID

enum class XmoTheme {
    Dark,
    Light,
    Amoled
}

@Composable
fun App() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val stateHolder = rememberSaveableStateHolder()
    val configuration = LocalConfiguration.current

    /*
     * Still retained while older Home/MiniPlayer components use
     * LiveBlur. NavBar + NowPlaying no longer depend on it.
     */
    val hazeState =
        rememberLiveBlurState()

    /*
     * =========================================================
     * PLAYER
     * =========================================================
     */

    val player =
        remember {
            XmoPlayer(context)
        }

    DisposableEffect(player) {
        player.connect()

        onDispose {
            player.release()
        }
    }

    val playback by
        player.state.collectAsState()

    /*
     * =========================================================
     * AUDIO PERMISSION
     * =========================================================
     */

    val audioPermission =
        if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    var allowed by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                audioPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            allowed = it
        }

    /*
     * =========================================================
     * PERSISTED STATE
     * =========================================================
     */

    var loaded by remember {
        mutableStateOf(false)
    }

    var setupComplete by remember {
        mutableStateOf(false)
    }

    var profile by remember {
        mutableStateOf(XmoProfile())
    }

    var appearance by remember {
        mutableStateOf(XmoAppearance())
    }

    var libraryPreferences by remember {
        mutableStateOf(LibraryPreferences())
    }

    var playbackPreferences by remember {
        mutableStateOf(PlaybackPreferences())
    }

    var resumeOnHeadphones by remember {
        mutableStateOf(false)
    }

    var songs by remember {
        mutableStateOf<List<Song>>(emptyList())
    }

    var order by remember {
        mutableStateOf(Store.defaults)
    }

    var categories by remember {
        mutableStateOf<List<UserCategory>>(emptyList())
    }

    var likedSongIds by remember {
        mutableStateOf<Set<Long>>(emptySet())
    }

    var recentPlays by remember {
        mutableStateOf<List<RecentPlay>>(emptyList())
    }

    var lyricsFiles by remember {
        mutableStateOf<Map<Long, String>>(emptyMap())
    }

    var scanning by remember {
        mutableStateOf(false)
    }

    /*
     * =========================================================
     * NAVIGATION
     * =========================================================
     */

    var tab by remember {
        mutableIntStateOf(0)
    }

    var profileOpen by remember {
        mutableStateOf(false)
    }

    /*
     * =========================================================
     * NOW PLAYING
     * =========================================================
     */

    var showNowPlaying by remember {
        mutableStateOf(false)
    }

    var miniVisible by remember {
        mutableStateOf(false)
    }

    var miniRiseKey by remember {
        mutableIntStateOf(0)
    }

    var playingSource by remember {
        mutableStateOf("All Songs")
    }

    var playingSourceIsCategory by remember {
        mutableStateOf(false)
    }

    /*
     * =========================================================
     * THEME
     * =========================================================
     */

    val systemDark =
        (
            configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
            ) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

    val theme =
        when (appearance.themeMode) {
            ThemeMode.System ->
                if (systemDark) {
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
     * INITIAL DATA
     * =========================================================
     */

    LaunchedEffect(Unit) {
        profile =
            Store.profile(context)

        appearance =
            Store.appearance(context)

        categories =
            Store.categories(context)

        order =
            Store.order(context)

        likedSongIds =
            Store.likedSongIds(context)

        recentPlays =
            Store.recentPlays(context)

        lyricsFiles =
            Store.lyricsFiles(context)

        libraryPreferences =
            Store.libraryPreferences(context)

        playbackPreferences =
            Store.playbackPreferences(context)

        resumeOnHeadphones =
            Store.resumeOnHeadphones(context)

        setupComplete =
            Store.setupComplete(context)

        loaded = true
    }

    /*
     * =========================================================
     * PLAYER PREFERENCES
     * =========================================================
     */

    LaunchedEffect(
        playback.connected,
        playbackPreferences
    ) {
        if (playback.connected) {
            player.setPlaybackParameters(
                speed =
                    playbackPreferences.playbackSpeed,
                pitch =
                    playbackPreferences.playbackPitch
            )
        }
    }

    LaunchedEffect(playback.connected) {
        if (playback.connected) {
            player.setShuffle(
                Store.shuffleEnabled(context)
            )

            player.setRepeatMode(
                Store.repeatMode(context)
            )
        }
    }

    var playerPersistenceReady by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(playback.connected) {
        if (playback.connected) {
            playerPersistenceReady = true
        }
    }

    LaunchedEffect(
        playback.shuffleEnabled,
        playback.repeatMode,
        playerPersistenceReady
    ) {
        if (playerPersistenceReady) {
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
     * LIBRARY
     * =========================================================
     */

    suspend fun loadLibrary() {
        if (
            !allowed ||
            !setupComplete
        ) {
            return
        }

        scanning = true

        try {
            val result =
                Library.songs(context)

            songs =
                if (
                    libraryPreferences.ignoreShortAudio
                ) {
                    result.filter {
                        it.duration >=
                            libraryPreferences.minimumDurationMs
                    }
                } else {
                    result
                }
        } finally {
            scanning = false
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

    LaunchedEffect(libraryPreferences) {
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
     * RECENT PLAYBACK
     * =========================================================
     */

    var recordedSongId by remember {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(playback.currentSongId) {
        val id =
            playback.currentSongId
                ?: return@LaunchedEffect

        if (id == recordedSongId) {
            return@LaunchedEffect
        }

        recordedSongId = id

        recentPlays =
            Store.recordPlay(
                context,
                id
            )
    }

    /*
     * =========================================================
     * FIRST LOAD
     * =========================================================
     */

    if (!loaded) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    homeColors(
                        XmoTheme.Dark
                    ).bg
                )
        )

        return
    }

    /*
     * =========================================================
     * SETUP
     * =========================================================
     */

    if (!setupComplete) {
        Setup(
            initialProfile =
                profile,

            existingCategories =
                categories,

            onCategoriesChanged = { next ->
                categories = next

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
                            it in Store.defaults
                        }

                    val existingCustom =
                        order.filter {
                            it !in Store.defaults &&
                                it in customIds
                        }

                    val missing =
                        customIds.filterNot {
                            it in existingCustom
                        }

                    val nextOrder =
                        builtIns +
                            existingCustom +
                            missing

                    order = nextOrder

                    Store.saveOrder(
                        context,
                        nextOrder
                    )
                }
            },

            finish = { result ->
                scope.launch {
                    allowed =
                        ContextCompat.checkSelfPermission(
                            context,
                            audioPermission
                        ) ==
                            PackageManager.PERMISSION_GRANTED

                    if (!allowed) {
                        permissionLauncher.launch(
                            audioPermission
                        )

                        return@launch
                    }

                    profile = result

                    Store.finishSetup(
                        context,
                        result
                    )

                    setupComplete = true

                    loadLibrary()
                }
            },

            setupLater = { result ->
                scope.launch {
                    allowed =
                        ContextCompat.checkSelfPermission(
                            context,
                            audioPermission
                        ) ==
                            PackageManager.PERMISSION_GRANTED

                    if (!allowed) {
                        permissionLauncher.launch(
                            audioPermission
                        )

                        return@launch
                    }

                    profile = result

                    Store.finishSetup(
                        context,
                        result
                    )

                    setupComplete = true

                    loadLibrary()
                }
            }
        )

        return
    }

    /*
     * =========================================================
     * COMMANDS
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
                it.id == song.id
            }

        if (index < 0) return

        playingSource = source
        playingSourceIsCategory = isCategory

        player.play(
            queue,
            index
        )

        showNowPlaying = true
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

    /*
     * Real category creation used by NowPlaying XmoCenterBox.
     *
     * Newly created categories are also appended to Home's
     * persisted reorderable section order.
     */
    fun createCategory(
        name: String,
        song: Song?
    ): UserCategory? {
        val clean =
            name.trim()
                .replace("\n", " ")
                .take(24)

        if (clean.isBlank()) {
            return null
        }

        val category =
            UserCategory(
                id =
                    "cat_${UUID.randomUUID()}",
                name =
                    clean,
                icon =
                    categories.size % 4,
                songIds =
                    if (song != null) {
                        setOf(song.id)
                    } else {
                        emptySet()
                    }
            )

        val nextCategories =
            categories + category

        val nextOrder =
            (
                order +
                    category.id
                ).distinct()

        /*
         * Compose state updates immediately, persistence follows
         * without making the options UI wait on disk.
         */
        categories =
            nextCategories

        order =
            nextOrder

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

    fun currentSong():
        Song? {
        val id =
            playback.currentSongId
                ?: return null

        return songs.firstOrNull {
            it.id == id
        } ?: player.currentSong()
    }

    /*
     * =========================================================
     * APP BACK
     * =========================================================
     */

    BackHandler(
        enabled =
            !showNowPlaying &&
                (
                    profileOpen ||
                        tab != 0
                    )
    ) {
        when {
            profileOpen ->
                profileOpen = false

            tab != 0 ->
                tab = 0
        }
    }

    CompositionLocalProvider(
        LocalXmoProfile provides profile
    ) {
        ProvideXmoAccent(
            appearance = appearance
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        homeColors(theme).bg
                    )
                    /*
                     * Temporary while remaining older layouts
                     * still depend on LiveBlur.
                     */
                    .liveBlurSource(
                        hazeState
                    )
            ) {
                /*
                 * =================================================
                 * SCREENS
                 * =================================================
                 */

                Box(
                    Modifier
                        .fillMaxSize()
                        .zIndex(0f)
                ) {
                    stateHolder.SaveableStateProvider(
                        key =
                            "tab_$tab"
                    ) {
                        when (tab) {
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
                                        if (!allowed) {
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
                                        profileOpen = true
                                    },

                                    saveOrder = {
                                        order = it

                                        scope.launch {
                                            Store.saveOrder(
                                                context,
                                                it
                                            )
                                        }
                                    },

                                    saveCategories = {
                                        categories = it

                                        scope.launch {
                                            Store.saveCategories(
                                                context,
                                                it
                                            )
                                        }
                                    },

                                    toggleLike = {
                                        toggleLike(it.id)
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
                                        appearance = it

                                        scope.launch {
                                            Store.saveAppearance(
                                                context,
                                                it
                                            )
                                        }
                                    },

                                    onLibraryPreferencesChanged = {
                                        libraryPreferences = it

                                        scope.launch {
                                            Store.saveLibraryPreferences(
                                                context,
                                                it
                                            )
                                        }
                                    },

                                    onPlaybackPreferencesChanged = {
                                        playbackPreferences = it

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
                                        resumeOnHeadphones = it

                                        scope.launch {
                                            Store.saveResumeOnHeadphones(
                                                context,
                                                it
                                            )
                                        }
                                    },

                                    rescan = {
                                        if (!allowed) {
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
                    playback.currentSongId != null &&
                    miniVisible &&
                    !showNowPlaying &&
                    !profileOpen
                ) {
                    val song =
                        currentSong()

                    Box(
                        Modifier
                            .fillMaxSize()
                            .zIndex(20f)
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

                            openPlayer = {
                                miniVisible = false
                                showNowPlaying = true
                            },

                            togglePlay = {
                                player.togglePlayPause()
                            },

                            toggleLike = {
                                song?.let {
                                    toggleLike(it.id)
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

                if (profileOpen) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .zIndex(80f)
                    ) {
                        ProfileEditor(
                            profile =
                                profile,

                            theme =
                                theme,

                            apply = {
                                profile = it

                                scope.launch {
                                    Store.saveProfile(
                                        context,
                                        it
                                    )
                                }

                                profileOpen = false
                            },

                            cancel = {
                                profileOpen = false
                            }
                        )
                    }
                }

                /*
                 * =================================================
                 * NOW PLAYING
                 * =================================================
                 */

                if (showNowPlaying) {
                    val song =
                        currentSong()

                    val id =
                        playback.currentSongId

                    Box(
                        Modifier
                            .fillMaxSize()
                            .zIndex(100f)
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
                                id != null &&
                                    id in likedSongIds,

                            lyricsUri =
                                id?.let {
                                    lyricsFiles[it]
                                },

                            /*
                             * Real categories exposed to
                             * XmoCenterBox.
                             */
                            categories =
                                categories,

                            onOpened = {
                                miniVisible = false
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
                                player.seekTo(it)
                            },

                            /*
                             * Current media item is resolved when
                             * action fires, not captured from a
                             * stale composition.
                             */
                            toggleLike = {
                                playback.currentSongId?.let {
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
                                val songId =
                                    playback.currentSongId

                                if (songId != null) {
                                    scope.launch {
                                        Store.saveLyricsUri(
                                            context,
                                            songId,
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
                             * REAL category membership.
                             */
                            setSongInCategory = {
                                    categoryId,
                                    added ->

                                currentSong()?.let {
                                    updateCategoryMembership(
                                        it,
                                        categoryId,
                                        added
                                    )
                                }
                            },

                            /*
                             * REAL category creation.
                             * Created category includes the current
                             * song immediately.
                             */
                            createCategory = { name ->
                                createCategory(
                                    name,
                                    currentSong()
                                )
                            },

                            dismiss = {
                                showNowPlaying = false

                                miniRiseKey++

                                miniVisible =
                                    playback.currentSongId != null
                            }
                        )
                    }
                }

                /*
                 * =================================================
                 * MINIPLAYER RECOVERY
                 * =================================================
                 */

                LaunchedEffect(
                    playback.currentSongId,
                    showNowPlaying,
                    profileOpen
                ) {
                    if (
                        playback.currentSongId != null &&
                        !showNowPlaying &&
                        !profileOpen &&
                        !miniVisible
                    ) {
                        miniVisible = true
                    }
                }
            }
        }
    }
}
