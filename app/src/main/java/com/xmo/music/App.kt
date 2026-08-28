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
     * ONE SHARED HAZE STATE
     * =========================================================
     */
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
     * AUDIO PERMISSION
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
                    PackageManager
                        .PERMISSION_GRANTED
            )
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) {
            allowed = it
        }

    /*
     * =========================================================
     * SETUP / PROFILE
     * =========================================================
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
     * MAIN APP
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
     * INITIAL DATA
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
            setupComplete ==
            true &&
            allowed
        ) {
            songs =
                Library.songs(
                    context
                )
        }
    }

    LaunchedEffect(
        allowed,
        setupComplete
    ) {
        if (
            allowed &&
            setupComplete ==
            true
        ) {
            songs =
                Library.songs(
                    context
                )
        }
    }

    LaunchedEffect(
        setupComplete
    ) {
        if (
            setupComplete ==
            true &&
            !allowed
        ) {
            permissionLauncher
                .launch(
                    audioPermission
                )
        }
    }

    /*
     * =========================================================
     * LOADING
     * =========================================================
     */
    if (
        setupComplete ==
        null
    ) {
        Box(
            Modifier.fillMaxSize()
        )

        return
    }

    /*
     * =========================================================
     * FIRST RUN SETUP
     * =========================================================
     */
    if (
        setupComplete ==
        false
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

                    val customIds =
                        nextCategories
                            .map {
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
     * SHARED PLAY COMMAND
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

        /*
         * Existing MiniPlayer remains exactly where it is while
         * NowPlaying enters.
         */
        miniVisible =
            playback.currentSongId !=
                null

        showNowPlaying =
            true

        player.play(
            queue,
            index
        )
    }

    CompositionLocalProvider(
        LocalXmoProfile provides
            profile
    ) {
        /*
         * =====================================================
         * SINGLE SHARED BACKDROP SOURCE
         * =====================================================
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
                             * HOME
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
                                        if (
                                            !allowed
                                        ) {
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

                                        startSong(
                                            song =
                                                song,

                                            source =
                                                source,

                                            isCategory =
                                                isCategory,

                                            queue =
                                                queue
                                        )
                                    }
                                )
                            }

                            /*
                             * SEARCH
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

                                        startSong(
                                            song =
                                                song,

                                            source =
                                                source,

                                            isCategory =
                                                isCategory,

                                            queue =
                                                queue
                                        )
                                    }
                                )
                            }

                            /*
                             * SETTINGS
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
                                        if (
                                            !allowed
                                        ) {
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
             * HAZE NAVBAR
             * =================================================
             */
            NavBar(
                selected =
                    tab,

                theme =
                    theme,

                hazeState =
                    hazeState
            ) {
                tab =
                    it
            }

            /*
             * =================================================
             * HAZE MINIPLAYER
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

                        hazeState =
                            hazeState,

                        riseKey =
                            miniRiseKey,

                        /*
                         * Tap / completed swipe-up:
                         *
                         * MiniPlayer itself does not move away.
                         * Full player simply rises over it.
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
                         * Full-screen entrance complete.
                         *
                         * Remove MiniPlayer silently from behind.
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
                         * NowPlaying calls this after FULL downward
                         * exit only.
                         */
                        dismiss = {
                            showNowPlaying =
                                false

                            /*
                             * New MiniPlayer composition starts
                             * below NavBar and rises to position.
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
             * RECOVERY
             * =================================================
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
