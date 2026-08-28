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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.xmo.music.data.Library
import com.xmo.music.data.Song
import com.xmo.music.data.Store
import com.xmo.music.data.UserCategory
import com.xmo.music.player.XmoPlayer
import com.xmo.music.ui.Home
import com.xmo.music.ui.MiniPlayer
import com.xmo.music.ui.NavBar
import com.xmo.music.ui.NowPlaying
import com.xmo.music.ui.Search
import com.xmo.music.ui.Settings
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
     * =========================================================
     * PLAYER NAVIGATION
     * =========================================================
     */
    var showPlayer by remember {
        mutableStateOf(false)
    }

    /*
     * While NowPlaying enters, MiniPlayer stays underneath.
     * onOpened() then removes it.
     */
    var miniVisible by remember {
        mutableStateOf(false)
    }

    /*
     * Increment after NowPlaying fully exits.
     * MiniPlayer uses it for bottom-rise animation.
     */
    var miniRiseKey by remember {
        mutableIntStateOf(0)
    }

    var source by remember {
        mutableStateOf(
            "All Songs"
        )
    }

    var sourceIsCategory by remember {
        mutableStateOf(false)
    }

    /*
     * =========================================================
     * INITIAL DATA
     * =========================================================
     */
    LaunchedEffect(Unit) {
        order =
            Store.order(
                context
            )

        categories =
            Store.categories(
                context
            )

        if (allowed) {
            songs =
                Library.songs(
                    context
                )
        }
    }

    LaunchedEffect(allowed) {
        if (allowed) {
            songs =
                Library.songs(
                    context
                )
        }
    }

    LaunchedEffect(Unit) {
        if (!allowed) {
            launcher.launch(
                permission
            )
        }
    }

    /*
     * Shared playback launcher for Home/Search.
     */
    fun playSong(
        song: Song,
        from: String,
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

        source =
            from

        sourceIsCategory =
            isCategory

        /*
         * If no old player exists yet, MiniPlayer doesn't need
         * to be visible under entrance.
         *
         * If a song was already playing, keep current MiniPlayer
         * until NowPlaying visually covers it.
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

    Box(
        Modifier.fillMaxSize()
    ) {
        /*
         * =====================================================
         * TAB CONTENT
         * =====================================================
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

                            order =
                                order,

                            categories =
                                categories,

                            setTheme = {
                                theme = it
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

                            onPlaySong = {
                                    song,
                                    from,
                                    isCategory,
                                    queue ->

                                playSong(
                                    song,
                                    from,
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
                                theme = it
                            },

                            onPlaySong = {
                                    song,
                                    from,
                                    isCategory,
                                    queue ->

                                playSong(
                                    song,
                                    from,
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
                                theme = it
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
         * =====================================================
         * APPROVED NAVBAR
         * =====================================================
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
                tab = it
            }
        }

        /*
         * =====================================================
         * MINI PLAYER
         * =====================================================
         *
         * zIndex below NowPlaying.
         *
         * During full-player entrance it can remain here until
         * onOpened() removes it, but is naturally covered by
         * NowPlaying as the sheet rises.
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
                         * DON'T hide MiniPlayer here.
                         *
                         * It stays still exactly where it is.
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
         * =====================================================
         * NOW PLAYING
         * =====================================================
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
                        source,

                    sourceIsCategory =
                        sourceIsCategory,

                    queue =
                        player.queue(),

                    /*
                     * Full player has completed entrance and now
                     * covers MiniPlayer. Remove MiniPlayer from
                     * background without any animation.
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
                        player.seekTo(it)
                    },

                    /*
                     * NowPlaying calls this only AFTER it is fully
                     * below the display.
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
         * First playback case:
         *
         * Song starts and NowPlaying is later dismissed ->
         * MiniPlayer becomes visible via dismiss callback.
         *
         * If playback exists after process state changes and no
         * player overlay is open, make MiniPlayer available.
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
