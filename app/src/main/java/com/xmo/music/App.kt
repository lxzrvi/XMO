package com.xmo.music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import androidx.compose.runtime.CompositionLocalProvider
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
        player.state
            .collectAsState()

    /*
     * =========================================================
     * AUDIO PERMISSION
     * =========================================================
     */
    val permission =
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

    var allowed by remember {
        mutableStateOf(
            ContextCompat
                .checkSelfPermission(
                    context,
                    permission
                ) ==
                PackageManager
                    .PERMISSION_GRANTED
        )
    }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) {
            allowed = it
        }

    /*
     * =========================================================
     * BOOT / SETUP
     * =========================================================
     *
     * null:
     * DataStore is still loading.
     */
    var setupComplete by remember {
        mutableStateOf<Boolean?>(
            null
        )
    }

    var profile by remember {
        mutableStateOf(
            XmoProfile()
        )
    }

    /*
     * =========================================================
     * MAIN APP STATE
     * =========================================================
     */
    var tab by remember {
        mutableIntStateOf(0)
    }

    var theme by remember {
        mutableStateOf(
            XmoTheme.Dark
        )
    }

    var songs by remember {
        mutableStateOf<List<Song>>(
            emptyList()
        )
    }

    var order by remember {
        mutableStateOf(
            Store.defaults
        )
    }

    var categories by remember {
        mutableStateOf<List<UserCategory>>(
            emptyList()
        )
    }

    /*
     * Player navigation.
     */
    var showPlayer by remember {
        mutableStateOf(false)
    }

    var miniVisible by remember {
        mutableStateOf(false)
    }

    var miniRiseKey by remember {
        mutableIntStateOf(0)
    }

    var playingSource by remember {
        mutableStateOf(
            "All Songs"
        )
    }

    var sourceIsCategory by remember {
        mutableStateOf(false)
    }

    /*
     * =========================================================
     * INITIAL DATA LOAD
     * =========================================================
     */
    LaunchedEffect(Unit) {
        /*
         * Read profile/setup before deciding which screen appears.
         */
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
     * Permission may become granted from Setup or later.
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
     * Do NOT automatically launch audio permission before Setup.
     *
     * Setup owns its own permission interaction on first run.
     *
     * Returning users still receive the existing permission flow.
     */
    LaunchedEffect(
        setupComplete
    ) {
        if (
            setupComplete == true &&
            !allowed
        ) {
            launcher.launch(
                permission
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
     * FIRST-RUN SETUP
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

            /*
             * Categories appear immediately in Setup and are
             * also persisted immediately.
             */
            onCategoriesChanged = {
                categories =
                    it

                scope.launch {
                    Store.saveCategories(
                        context,
                        it
                    )

                    /*
                     * Ensure custom IDs exist in home order.
                     */
                    val customIds =
                        it.map {
                                category ->
                            category.id
                        }

                    val builtIns =
                        order.filter {
                            id ->
                            id in
                                Store.defaults
                        }

                    val existingCustom =
                        order.filter {
                            id ->
                            id !in
                                Store.defaults &&
                                id in
                                customIds
                        }

                    val missing =
                        customIds.filterNot {
                            id ->
                            id in
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
                    /*
                     * Re-check actual permission because Setup's
                     * launcher is local to Setup composable.
                     */
                    allowed =
                        ContextCompat
                            .checkSelfPermission(
                                context,
                                permission
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
                    /*
                     * Setup Later means:
                     *
                     * persist current profile/defaults and don't
                     * force onboarding every launch.
                     *
                     * Audio permission can be requested by main app.
                     */
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
     * MAIN APPLICATION
     * =========================================================
     */

    fun playSong(
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

        if (index < 0) {
            return
        }

        playingSource =
            source

        sourceIsCategory =
            isCategory

        /*
         * Existing MiniPlayer remains behind NowPlaying while
         * entrance is running.
         */
        miniVisible =
            playback.currentSongId !=
                null

        showPlayer =
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
        Box(
            Modifier.fillMaxSize()
        ) {
            /*
             * =================================================
             * TAB CONTENT
             * =================================================
             */
            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(0f)
            ) {
                stateHolder
                    .SaveableStateProvider(
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
                                            launcher.launch(
                                                permission
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

                                    onPlaySong = {
                                            song,
                                            source,
                                            isCategory,
                                            queue ->

                                        playSong(
                                            song,
                                            source,
                                            isCategory,
                                            queue
                                        )
                                    }
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
                                            song,
                                            source,
                                            isCategory,
                                            queue
                                        )
                                    }
                                )
                            }

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
                                            launcher.launch(
                                                permission
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
             */
            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(10f)
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
                        .zIndex(20f)
                ) {
                    MiniPlayer(
                        state =
                            playback,

                        theme =
                            theme,

                        riseKey =
                            miniRiseKey,

                        openPlayer = {
                            /*
                             * Do not animate/remove MiniPlayer here.
                             * NowPlaying rises over it.
                             */
                            showPlayer =
                                true
                        },

                        togglePlay = {
                            player
                                .togglePlayPause()
                        },

                        previous = {
                            player.previous()
                        },

                        next = {
                            player.next()
                        }
                    )
                }
            }

            /*
             * =================================================
             * NOW PLAYING
             * =================================================
             */
            if (showPlayer) {
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
                            sourceIsCategory,

                        queue =
                            player.queue(),

                        /*
                         * Entrance complete: MiniPlayer can now be
                         * removed invisibly from background.
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
                            player.previous()
                        },

                        next = {
                            player.next()
                        },

                        seekTo = {
                            player.seekTo(
                                it
                            )
                        },

                        /*
                         * Called only after player fully leaves
                         * viewport.
                         */
                        dismiss = {
                            showPlayer =
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
             * Keep MiniPlayer available whenever playback exists
             * and no full player is active.
             */
            LaunchedEffect(
                playback.currentSongId,
                showPlayer
            ) {
                if (
                    playback.currentSongId !=
                        null &&
                    !showPlayer &&
                    !miniVisible
                ) {
                    miniVisible =
                        true
                }
            }
        }
    }
}
