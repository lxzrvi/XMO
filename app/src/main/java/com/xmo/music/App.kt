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
     * ---------------------------------------------------------
     * MEDIA3 PLAYER
     * ---------------------------------------------------------
     *
     * UI owns MediaController wrapper.
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

    /*
     * Keep this lifecycle aware because playback may continue
     * while Activity is backgrounded.
     */
    val playbackState by
        player.state
            .collectAsState()

    /*
     * ---------------------------------------------------------
     * PERMISSION
     * ---------------------------------------------------------
     */
    val audioPermission =
        if (
            Build.VERSION.SDK_INT >= 33
        ) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
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
        ) { granted ->
            allowed =
                granted
        }

    /*
     * ---------------------------------------------------------
     * APP STATE
     * ---------------------------------------------------------
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
     * ---------------------------------------------------------
     * NOW PLAYING NAVIGATION STATE
     * ---------------------------------------------------------
     *
     * Playing screen is NOT a NavBar tab.
     * It is a full-screen overlay above Home/Search/Settings.
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
     * ---------------------------------------------------------
     * LOAD DATASTORE + LIBRARY
     * ---------------------------------------------------------
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
     * Permission may be granted after first composition.
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
            permissionLauncher
                .launch(
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
         * MAIN APP
         * -----------------------------------------------------
         */
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
                            theme = it
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

                        /*
                         * -------------------------------------
                         * REAL SONG TAP
                         * -------------------------------------
                         *
                         * Home gives:
                         *
                         * selected song
                         * real source/category
                         * source queue
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

                                if (index >= 0) {
                                    /*
                                     * Keep source metadata before
                                     * opening player UI.
                                     */
                                    playingSource =
                                        source

                                    playingFromCategory =
                                        isCategory

                                    /*
                                     * Actual Media3 playback.
                                     */
                                    player.play(
                                        songs =
                                            queue,
                                        index =
                                            index
                                    )

                                    /*
                                     * Player enters from bottom.
                                     */
                                    showNowPlaying =
                                        true
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

        /*
         * -----------------------------------------------------
         * APPROVED NAVBAR
         * -----------------------------------------------------
         *
         * No geometry changes.
         *
         * It remains below the NowPlaying layer because player
         * is composed later in this Box.
         */
        NavBar(
            selected =
                tab,

            theme =
                theme
        ) { newTab ->
            tab =
                newTab
        }

        /*
         * =====================================================
         * NOW PLAYING
         * =====================================================
         *
         * Last Box child = above Home + NavBar.
         */
        if (
            showNowPlaying
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

                /*
                 * Reads real MediaController position.
                 * NowPlaying calls this periodically.
                 */
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
                        .seekTo(it)
                },

                /*
                 * Dismiss UI only.
                 *
                 * Playback intentionally continues.
                 */
                dismiss = {
                    showNowPlaying =
                        false
                }
            )
        }
    }
}
