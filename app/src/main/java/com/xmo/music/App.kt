package com.xmo.music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.xmo.music.data.Library
import com.xmo.music.data.Song
import com.xmo.music.data.Store
import com.xmo.music.data.UserCategory
import com.xmo.music.data.XmoProfile
import com.xmo.music.player.XmoPlayer
import com.xmo.music.ui.Home
import com.xmo.music.ui.LocalXmoProfile
import com.xmo.music.ui.MiniPlayer
import com.xmo.music.ui.NavBar
import com.xmo.music.ui.NowPlaying
import com.xmo.music.ui.Search
import com.xmo.music.ui.Settings
import com.xmo.music.ui.Setup
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

    /*
     * =========================================================
     * ONE SHARED HAZE BACKDROP
     * =========================================================
     *
     * One state at application UI level.
     *
     * NavBar and MiniPlayer will reuse this same state.
     *
     * Never create a HazeState per button/tab/card.
     */
    val hazeState =
        rememberLiveBlurState()

    /*
     * =========================================================
     * MEDIA3 PLAYER
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
     * LOCAL AUDIO PERMISSION
     * =========================================================
     */
    val audioPermission =
        if (
            Build.VERSION.SDK_INT >= 33
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
                    PackageManager
                        .PERMISSION_GRANTED
            )
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) { granted ->

            allowed =
                granted
        }

    /*
     * =========================================================
     * FIRST RUN / PROFILE
     * =========================================================
     *
     * null = DataStore is still loading.
     */
    var setupComplete by
        remember {
            mutableStateOf<Boolean?>(
                null
            )
        }

    var profile by
        remember {
            mutableStateOf(
                XmoProfile()
            )
        }

    /*
     * =========================================================
     * MAIN APP STATE
     * =========================================================
     */
    var tab by
        remember {
            mutableIntStateOf(
                0
            )
        }

    var theme by
        remember {
            mutableStateOf(
                XmoTheme.Dark
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

    /*
     * =========================================================
     * PLAYER UI STATE
     * =========================================================
     */
    var showNowPlaying by
        remember {
            mutableStateOf(
                false
            )
        }

    /*
     * Existing MiniPlayer can remain under NowPlaying while
     * NowPlaying is entering.
     *
     * onOpened() removes it silently afterwards.
     */
    var miniVisible by
        remember {
            mutableStateOf(
                false
            )
        }

    /*
     * Increment only after NowPlaying completely leaves screen.
     *
     * MiniPlayer uses this to rise from behind the NavBar.
     */
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
     * INITIAL DATASTORE LOAD
     * =========================================================
     */
    LaunchedEffect(Unit) {
        profile =
            Store.profile(
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

        setupComplete =
            Store.setupComplete(
                context
            )

        if (
            setupComplete == true &&
            allowed
        ) {
            songs =
                Library.songs(
                    context
                )
        }
    }

    /*
     * Permission can become allowed after:
     *
     * - Setup
     * - main permission launcher
     */
    LaunchedEffect(
        allowed,
        setupComplete
    ) {
        if (
            allowed &&
            setupComplete == true
        ) {
            songs =
                Library.songs(
                    context
                )
        }
    }

    /*
     * Setup owns first-run audio permission.
     *
     * Existing users still get main app permission request
     * when needed.
     */
    LaunchedEffect(
        setupComplete
    ) {
        if (
            setupComplete == true &&
            !allowed
        ) {
            permissionLauncher.launch(
                audioPermission
            )
        }
    }

    /*
     * =========================================================
     * INITIAL DATASTORE LOADING
     * =========================================================
     */
    if (
        setupComplete == null
    ) {
        Box(
            Modifier.fillMaxSize()
        )

        return
    }

    /*
     * =========================================================
     * FIRST-RUN SETUP
     * =========================================================
     *
     * Setup itself does not need app glass backdrop.
     * It has its own clean first-run identity.
     */
    if (
        setupComplete == false
    ) {
        Setup(
            initialProfile =
                profile,

            existingCategories =
                categories,

            onCategoriesChanged = {
                    nextCategories ->

                categories =
                    nextCategories

                scope.launch {
                    Store.saveCategories(
                        context,
                        nextCategories
                    )

                    /*
                     * Keep initial custom category IDs in the
                     * same Home section/category ordering system.
                     */
                    val customIds =
                        nextCategories.map {
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

                    order =
                        nextOrder

                    Store.saveOrder(
                        context,
                        nextOrder
                    )
                }
            },

            finish = {
                    result ->

                scope.launch {
                    /*
                     * Setup owns its own launcher, so refresh
                     * actual permission here.
                     */
                    allowed =
                        ContextCompat
                            .checkSelfPermission(
                                context,
                                audioPermission
                            ) ==
                            PackageManager
                                .PERMISSION_GRANTED

                    profile =
                        result

                    Store.finishSetup(
                        context,
                        result
                    )

                    setupComplete =
                        true

                    if (allowed) {
                        songs =
                            Library.songs(
                                context
                            )
                    }
                }
            },

            setupLater = {
                    result ->

                scope.launch {
                    profile =
                        result

                    /*
                     * Setup Later still marks onboarding as done,
                     * otherwise user would be forced through it
                     * on every launch.
                     */
                    Store.finishSetup(
                        context,
                        result
                    )

                    setupComplete =
                        true
                }
            }
        )

        return
    }

    /*
     * =========================================================
     * SHARED PLAYBACK ENTRY POINT
     * =========================================================
     *
     * Used by:
     * Home
     * Search
     * future Album/Artist screens
     */
    fun playSong(
        song: Song,
        source: String,
        sourceIsCategory: Boolean,
        queue: List<Song>
    ) {
        val index =
            queue.indexOfFirst {
                it.id ==
                    song.id
            }

        if (
            index < 0
        ) {
            return
        }

        playingSource =
            source

        playingSourceIsCategory =
            sourceIsCategory

        /*
         * If audio was already playing, keep current MiniPlayer
         * exactly where it is during NowPlaying entrance.
         *
         * NowPlaying will cover it naturally.
         */
        miniVisible =
            playback.currentSongId !=
                null

        /*
         * Open UI immediately.
         */
        showNowPlaying =
            true

        /*
         * Real Media3 queue.
         */
        player.play(
            queue,
            index
        )
    }

    /*
     * =========================================================
     * MAIN APPLICATION
     * =========================================================
     *
     * Profile is supplied throughout UI without threading
     * profile arguments through giant Home.kt.
     */
    CompositionLocalProvider(
        LocalXmoProfile provides
            profile
    ) {
        /*
         * =====================================================
         * SHARED HAZE SOURCE
         * =====================================================
         *
         * Main app is the single backdrop capture source.
         *
         * Future NavBar/MiniPlayer hazeBlur() calls consume this
         * same state.
         */
        Box(
            Modifier
                .fillMaxSize()
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
                stateHolder
                    .SaveableStateProvider(
                        key =
                            "tab_$tab"
                    ) {
                        when (tab) {
                            /*
                             * ---------------------------------
                             * HOME
                             * ---------------------------------
                             */
                            0 -> {
                                Home(
                                    songs =
                                        songs,

                                    allowed =
                                        allowed,

                                    theme =
                                        theme,

                                    order =
                                        order,

                                    categories =
                                        categories,

                                    setTheme = {
                                        theme =
                                            it
                                    },

                                    refresh = {
                                        if (!allowed) {
                                            permissionLauncher
                                                .launch(
                                                    audioPermission
                                                )
                                        } else {
                                            scope.launch {
                                                songs =
                                                    Library.songs(
                                                        context
                                                    )
                                            }
                                        }
                                    },

                                    saveOrder = {
                                            next ->

                                        order =
                                            next

                                        scope.launch {
                                            Store.saveOrder(
                                                context,
                                                next
                                            )
                                        }
                                    },

                                    saveCategories = {
                                            next ->

                                        categories =
                                            next

                                        scope.launch {
                                            Store.saveCategories(
                                                context,
                                                next
                                            )
                                        }
                                    },

                                    onPlaySong = {
                                            song,
                                            source,
                                            isCategory,
                                            queue ->

                                        playSong(
                                            song =
                                                song,

                                            source =
                                                source,

                                            sourceIsCategory =
                                                isCategory,

                                            queue =
                                                queue
                                        )
                                    }
                                )
                            }

                            /*
                             * ---------------------------------
                             * SEARCH
                             * ---------------------------------
                             */
                            1 -> {
                                Search(
                                    songs =
                                        songs,

                                    categories =
                                        categories,

                                    theme =
                                        theme,

                                    setTheme = {
                                        theme =
                                            it
                                    },

                                    onPlaySong = {
                                            song,
                                            source,
                                            isCategory,
                                            queue ->

                                        playSong(
                                            song =
                                                song,

                                            source =
                                                source,

                                            sourceIsCategory =
                                                isCategory,

                                            queue =
                                                queue
                                        )
                                    }
                                )
                            }

                            /*
                             * ---------------------------------
                             * SETTINGS
                             * ---------------------------------
                             */
                            else -> {
                                Settings(
                                    theme =
                                        theme,

                                    setTheme = {
                                        theme =
                                            it
                                    },

                                    rescan = {
                                        if (!allowed) {
                                            permissionLauncher
                                                .launch(
                                                    audioPermission
                                                )
                                        } else {
                                            scope.launch {
                                                songs =
                                                    Library.songs(
                                                        context
                                                    )
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
             * APPROVED NAVBAR
             * =================================================
             *
             * Geometry / gesture system unchanged.
             *
             * Next file pass will give NavBar the shared
             * hazeState for rendering only.
             */
            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(
                        10f
                    )
            ) {
                NavBar(
                    selected =
                        tab,

                    theme =
                        theme
                ) {
                    tab =
                        it
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
                miniVisible
            ) {
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

                        riseKey =
                            miniRiseKey,

                        /*
                         * Do NOT hide/animate MiniPlayer here.
                         *
                         * It remains still.
                         * NowPlaying rises over it.
                         */
                        openPlayer = {
                            showNowPlaying =
                                true
                        },

                        togglePlay = {
                            player
                                .togglePlayPause()
                        },

                        previous = {
                            player
                                .previous()
                        },

                        next = {
                            player
                                .next()
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

                        /*
                         * Player entrance is complete.
                         *
                         * MiniPlayer can now disappear from
                         * background without visual animation.
                         */
                        onOpened = {
                            miniVisible =
                                false
                        },

                        refreshPosition = {
                            player
                                .refreshPosition()
                        },

                        togglePlay = {
                            player
                                .togglePlayPause()
                        },

                        previous = {
                            player
                                .previous()
                        },

                        next = {
                            player
                                .next()
                        },

                        seekTo = {
                            player
                                .seekTo(
                                    it
                                )
                        },

                        /*
                         * Called only after NowPlaying is
                         * completely below viewport.
                         */
                        dismiss = {
                            showNowPlaying =
                                false

                            /*
                             * Trigger new MiniPlayer rise.
                             */
                            miniRiseKey++

                            miniVisible =
                                playback.currentSongId !=
                                    null
                        }
                    )
                }
            }

            /*
             * =================================================
             * MINIPLAYER RECOVERY
             * =================================================
             *
             * Keeps MiniPlayer available after playback starts
             * or Activity state updates when NowPlaying isn't
             * visible.
             */
            LaunchedEffect(
                playback.currentSongId,
                showNowPlaying
            ) {
                if (
                    playback.currentSongId !=
                        null &&
                    !showNowPlaying &&
                    !miniVisible
                ) {
                    miniVisible =
                        true
                }
            }
        }
    }
}
