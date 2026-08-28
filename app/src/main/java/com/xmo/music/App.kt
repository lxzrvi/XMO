package com.xmo.music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
import com.xmo.music.ui.NavBar
import com.xmo.music.ui.NowPlaying
import com.xmo.music.ui.Search
import com.xmo.music.ui.Settings
import kotlinx.coroutines.launch
import com.xmo.music.ui.MiniPlayer

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
     *
     * XmoPlayer owns MediaController.
     * PlaybackService owns ExoPlayer.
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

    val playbackState by
        player.state.collectAsState()

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
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    var allowed by
        remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    audioPermission
                ) ==
                    PackageManager.PERMISSION_GRANTED
            )
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            allowed =
                granted
        }

    /*
     * =========================================================
     * APP STATE
     * =========================================================
     */
    var tab by
        remember {
            mutableIntStateOf(0)
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
     * NOW PLAYING STATE
     * =========================================================
     *
     * NowPlaying is NOT a NavBar tab.
     * It sits above the entire app.
     */
    var showNowPlaying by
        remember {
            mutableStateOf(false)
        }

    var playingSource by
        remember {
            mutableStateOf(
                "All Songs"
            )
        }

    var playingFromCategory by
        remember {
            mutableStateOf(false)
        }

    /*
     * =========================================================
     * INITIAL LOAD
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

    /*
     * Permission can be granted after the first composition.
     */
    LaunchedEffect(
        allowed
    ) {
        if (allowed) {
            songs =
                Library.songs(
                    context
                )
        }
    }

    LaunchedEffect(Unit) {
        if (!allowed) {
            permissionLauncher.launch(
                audioPermission
            )
        }
    }

    /*
     * =========================================================
     * ROOT
     * =========================================================
     */
    Box(
        Modifier.fillMaxSize()
    ) {
        /*
         * -----------------------------------------------------
         * MAIN TAB CONTENT
         * -----------------------------------------------------
         */
        Box(
            Modifier
                .fillMaxSize()
                .zIndex(0f)
        ) {
            stateHolder.SaveableStateProvider(
                key = "tab_$tab"
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
                                    permissionLauncher.launch(
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

                            /*
                             * ---------------------------------
                             * SONG TAP
                             * ---------------------------------
                             *
                             * Do NOT wait for playbackState.
                             * Open NowPlaying immediately.
                             */
                            onPlaySong = {
                                    song,
                                    source,
                                    isCategory,
                                    queue ->

                                if (
                                    queue.isNotEmpty()
                                ) {
                                    val index =
                                        queue.indexOfFirst {
                                            it.id ==
                                                song.id
                                        }

                                    if (
                                        index >=
                                        0
                                    ) {
                                        /*
                                         * Store source first.
                                         */
                                        playingSource =
                                            source

                                        playingFromCategory =
                                            isCategory

                                        /*
                                         * Open UI immediately.
                                         *
                                         * This is deliberately
                                         * BEFORE player.play().
                                         */
                                        showNowPlaying =
                                            true

                                        /*
                                         * Real Media3 playback.
                                         */
                                        player.play(
                                            songs =
                                                queue,

                                            index =
                                                index
                                        )
                                    }
                                }
                            }
                        )
                    }

                    1 -> {
                        Search()
                    }

                    else -> {
                        Settings()
                    }
                }
            }
        }

        /*
         * -----------------------------------------------------
         * APPROVED NAVBAR
         * -----------------------------------------------------
         */
        Box(
            Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            NavBar(
                selected =
                    tab,

                theme =
                    theme
            ) { value ->
                tab =
                    value
            }
        }

        MiniPlayer(
    state = playbackState,
            theme = theme,
        
            /*
             * Player closes fully first.
             * Then showNowPlaying becomes false,
             * triggering MiniPlayer's bottom entrance.
             */
            visible =
                playbackState.currentSongId != null &&
                    !showNowPlaying,
        
            openPlayer = {
                showNowPlaying = true
            },
        
            togglePlay = {
                player.togglePlayPause()
            },
        
            previous = {
                player.previous()
            },
        
            next = {
                player.next()
            }
        )
        /*
         * -----------------------------------------------------
         * NOW PLAYING
         * -----------------------------------------------------
         *
         * Highest z-index.
         * Therefore NavBar can never draw over it.
         */
        if (
            showNowPlaying
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(100f)
            ) {
                NowPlaying(
                    state =
                        playbackState,

                    theme =
                        theme,

                    source =
                        playingSource,

                    sourceIsCategory =
                        playingFromCategory,

                    refreshPosition = {
                        player.refreshPosition()
                    },

                    togglePlay = {
                        player.togglePlayPause()
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
                     * Closing UI does NOT stop audio.
                     */
                    dismiss = {
                        showNowPlaying =
                            false
                    }
                )
            }
        }
    }
}
